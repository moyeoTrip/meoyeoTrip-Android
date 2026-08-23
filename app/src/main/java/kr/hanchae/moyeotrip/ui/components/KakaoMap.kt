package kr.hanchae.moyeotrip.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.KakaoMapSdk
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.route.RouteLineOptions
import com.kakao.vectormap.route.RouteLineSegment
import com.kakao.vectormap.route.RouteLineStyle

/**
 * 지도 좌표. 카카오 SDK 타입(`com.kakao.vectormap.LatLng`)을 화면 코드까지 퍼뜨리지 않기 위한 래퍼다.
 * SDK가 없거나 초기화에 실패하면 화면은 목업 지도로 폴백하는데, 그때도 좌표 값 자체는 그대로 쓰인다.
 */
data class MoyeoLatLng(val latitude: Double, val longitude: Double)

/** 마커 모양. 시각 언어는 기획 캡처 기준이다 (순번=초록 원+흰 숫자, 집합 장소=단일 핀). */
enum class MapMarkerShape {
    /** 집합 장소 — 흰 테두리를 두른 초록 핀 (기획 17-3). */
    Pin,

    /** 방문지 순번 — 초록 원 + 흰 숫자 (기획 18-x). */
    Numbered,

    /** 탐색 지도의 지역 묶음 — 초록 원 + 흰 개수 (기획 11). */
    Cluster
}

data class MapMarker(
    val id: String,
    val position: MoyeoLatLng,
    val shape: MapMarkerShape,
    /** 원 안에 넣을 글자 (순번/개수). Pin 모양에서는 무시한다. */
    val badge: String? = null
)

/**
 * QA 캡처(`moyeo_screen` 인텐트) 진입 여부. 실지도는 타일 로딩이 비결정적이라 번호별 비교 캡처가
 * 깨진다 — 캡처 모드에서는 [KakaoMapView] 가 실지도를 아예 만들지 않고 기존 목업을 그린다.
 */
val LocalMapCaptureMode = staticCompositionLocalOf { false }

/**
 * 카카오 지도 SDK 초기화 상태. 네이티브 앱 키가 비어 있으면 초기화를 건너뛰고 지도는 목업으로 폴백한다.
 */
object MoyeoMapSdk {
    @Volatile
    private var initialized = false

    val isReady: Boolean
        get() = initialized

    /** 키가 비어 있거나 초기화가 실패하면 false — 호출부는 예외를 신경 쓰지 않아도 된다. */
    fun init(context: Context, appKey: String): Boolean {
        if (initialized) return true
        if (appKey.isBlank()) return false
        initialized = runCatching {
            KakaoMapSdk.init(context, appKey)
            KakaoMapSdk.isInitialized()
        }.getOrDefault(false)
        return initialized
    }
}

/**
 * 계측(UI) 테스트에서도 실지도를 타지 않는다. GL 서피스 + 타일 네트워크는 비결정적이라
 * `composeRule.waitForIdle()` 기반 테스트를 흔들 수 있다.
 */
private val runningInstrumentedTest: Boolean by lazy {
    runCatching { Class.forName("androidx.test.espresso.Espresso") }.isSuccess
}

private const val DEFAULT_ZOOM_LEVEL = 12
private const val FIT_POINTS_PADDING_DP = 56

/** 기획의 경로선·마커 초록. 다크/라이트 공통으로 같은 초록을 쓴다 (기획 11·17-3·18-3). */
private val MapRouteGreen = Color(0xFF4E9B6B)

/**
 * 카카오 지도 실지도. 마커 목록 + 경로선 + "중앙 핀" 모드를 지원하는 앱 공용 지도 컴포저블이다.
 *
 * 폴백은 [fallback] 으로 받는다 — 키 없음 / SDK 초기화 실패 / 지도 인증·렌더 오류 / QA 캡처 모드에서는
 * 실지도를 만들지 않고 화면이 원래 쓰던 목업 지도를 그대로 그린다.
 *
 * @param draggablePin true면 지도를 끌어 중앙 좌표를 고르는 모드다. 핀 그림 자체는 호출 화면이 지도 위에
 *   겹쳐 그리고(기획 17-3), 이 컴포저블은 카메라가 멈출 때마다 중앙 좌표를 [onPinMove] 로 알린다.
 */
@Composable
fun KakaoMapView(
    center: MoyeoLatLng,
    modifier: Modifier = Modifier,
    markers: List<MapMarker> = emptyList(),
    polyline: List<MoyeoLatLng> = emptyList(),
    zoomLevel: Int = DEFAULT_ZOOM_LEVEL,
    draggablePin: Boolean = false,
    onPinMove: (MoyeoLatLng) -> Unit = {},
    fallback: @Composable (Modifier) -> Unit
) {
    val captureMode = LocalMapCaptureMode.current
    var mapFailed by remember { mutableStateOf(false) }
    if (captureMode || runningInstrumentedTest || mapFailed || !MoyeoMapSdk.isReady) {
        fallback(modifier)
        return
    }

    val context = LocalContext.current
    val density = LocalDensity.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }
    val mapView = remember(context) { MapView(context) }
    val touchHost = remember(mapView) {
        MapTouchHost(context).apply {
            addView(
                mapView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        }
    }
    val initialCenter = remember(mapView) { center }
    val initialZoom = remember(mapView) { zoomLevel }
    val currentOnPinMove by rememberUpdatedState(onPinMove)

    DisposableEffect(mapView) {
        runCatching {
            mapView.start(
                object : MapLifeCycleCallback() {
                    override fun onMapDestroy() = Unit

                    override fun onMapError(error: Exception) {
                        // 인증 실패(패키지명·키해시 미등록)나 렌더 오류 — 빈 사각형을 남기지 않고 목업으로 내려간다
                        mapFailed = true
                    }
                },
                object : KakaoMapReadyCallback() {
                    override fun onMapReady(map: KakaoMap) {
                        kakaoMap = map
                    }

                    override fun getPosition(): LatLng = LatLng.from(initialCenter.latitude, initialCenter.longitude)

                    override fun getZoomLevel(): Int = initialZoom
                }
            )
        }.onFailure { mapFailed = true }
        onDispose {
            kakaoMap = null
            runCatching { mapView.finish() }
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> runCatching { mapView.resume() }
                Lifecycle.Event.ON_PAUSE -> runCatching { mapView.pause() }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(kakaoMap, draggablePin) {
        val map = kakaoMap ?: return@LaunchedEffect
        if (!draggablePin) return@LaunchedEffect
        runCatching {
            map.setOnCameraMoveEndListener { _, cameraPosition, _ ->
                val position = cameraPosition.position
                currentOnPinMove(MoyeoLatLng(position.latitude, position.longitude))
            }
        }
    }

    val markerArgb = MapRouteGreen.toArgb()
    LaunchedEffect(kakaoMap, markers, polyline) {
        val map = kakaoMap ?: return@LaunchedEffect
        runCatching {
            map.drawMarkers(density, markers, markerArgb)
            map.drawPolyline(density, polyline, markerArgb)
            if (!draggablePin) {
                map.frameCamera(markers, polyline, center, zoomLevel, density)
            }
        }.onFailure { mapFailed = true }
    }

    AndroidView(factory = { touchHost }, modifier = modifier)
}

/**
 * 지도를 LazyColumn 안(기획 17-3)에 넣으면 지도를 끄는 제스처를 리스트 스크롤이 가로챈다.
 * 터치가 들어오는 순간 부모(Compose)에게 인터셉트하지 말라고 알려 지도가 제스처를 온전히 받게 한다.
 */
private class MapTouchHost(context: Context) : FrameLayout(context) {
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        parent?.requestDisallowInterceptTouchEvent(true)
        return false
    }
}

private fun KakaoMap.drawMarkers(density: Density, markers: List<MapMarker>, argb: Int) {
    val layer = labelManager?.layer ?: return
    layer.removeAll()
    markers.forEach { marker ->
        val bitmap = marker.toBitmap(density, argb)
        val anchorY = if (marker.shape == MapMarkerShape.Pin) 1f else 0.5f
        val styles = labelManager?.addLabelStyles(
            LabelStyles.from(
                "moyeo-${marker.shape.name}-${marker.badge ?: "pin"}",
                LabelStyle.from(bitmap).setAnchorPoint(0.5f, anchorY)
            )
        ) ?: return
        layer.addLabel(
            LabelOptions.from(marker.id, LatLng.from(marker.position.latitude, marker.position.longitude))
                .setStyles(styles)
        )
    }
}

private fun KakaoMap.drawPolyline(density: Density, polyline: List<MoyeoLatLng>, argb: Int) {
    val layer = routeLineManager?.layer ?: return
    layer.removeAll()
    if (polyline.size < 2) return
    val lineWidth = with(density) { 7.dp.toPx() }
    val strokeWidth = with(density) { 2.dp.toPx() }
    layer.addRouteLine(
        RouteLineOptions.from(
            RouteLineSegment.from(
                polyline.map { LatLng.from(it.latitude, it.longitude) },
                RouteLineStyle.from(lineWidth, argb, strokeWidth, AndroidColor.WHITE)
            )
        )
    )
}

private fun KakaoMap.frameCamera(
    markers: List<MapMarker>,
    polyline: List<MoyeoLatLng>,
    center: MoyeoLatLng,
    zoomLevel: Int,
    density: Density
) {
    val points = (markers.map(MapMarker::position) + polyline).distinct()
    if (points.size >= 2) {
        val padding = with(density) { FIT_POINTS_PADDING_DP.dp.toPx() }.toInt()
        moveCamera(
            CameraUpdateFactory.fitMapPoints(
                points.map { LatLng.from(it.latitude, it.longitude) }.toTypedArray(),
                padding
            )
        )
    } else {
        val target = points.firstOrNull() ?: center
        moveCamera(CameraUpdateFactory.newCenterPosition(LatLng.from(target.latitude, target.longitude), zoomLevel))
    }
}

/**
 * 마커 아이콘을 코드로 그린다. 기획 캡처의 마커는 초록 원 + 흰 숫자(또는 흰 테두리 핀)라서
 * drawable 리소스를 새로 만들지 않고 같은 시각 언어를 그대로 재현한다.
 */
private fun MapMarker.toBitmap(density: Density, argb: Int): Bitmap {
    val radius = with(density) { 16.dp.toPx() }
    val ringWidth = with(density) { 2.dp.toPx() }
    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = argb
        style = Paint.Style.FILL
    }
    val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        style = Paint.Style.STROKE
        strokeWidth = ringWidth
    }
    val white = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        style = Paint.Style.FILL
    }
    val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        textAlign = Paint.Align.CENTER
        textSize = with(density) { 13.dp.toPx() }
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    val width = (radius * 2).toInt().coerceAtLeast(1)
    val stem = if (shape == MapMarkerShape.Pin) with(density) { 9.dp.toPx() } else 0f
    val bitmap = Bitmap.createBitmap(width, (radius * 2 + stem).toInt().coerceAtLeast(1), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    if (shape == MapMarkerShape.Pin) {
        canvas.drawRect(radius - ringWidth, radius, radius + ringWidth, radius * 2 + stem, fill)
    }
    canvas.drawCircle(radius, radius, radius - ringWidth, fill)
    canvas.drawCircle(radius, radius, radius - ringWidth, ring)
    when (shape) {
        MapMarkerShape.Pin -> canvas.drawCircle(radius, radius, with(density) { 4.dp.toPx() }, white)

        MapMarkerShape.Numbered, MapMarkerShape.Cluster ->
            badge?.let { canvas.drawText(it, radius, radius + text.textSize * 0.36f, text) }
    }
    return bitmap
}
