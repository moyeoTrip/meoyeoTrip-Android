package kr.hanchae.moyeotrip.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
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
 * SDK가 없거나 초기화에 실패하면 지도 자리가 비지만, 좌표 값 자체는 그대로 쓰인다.
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
 * 깨진다 — 캡처 모드에서는 [KakaoMapView] 가 실지도를 아예 만들지 않고 폴백만 그린다.
 */
val LocalMapCaptureMode = staticCompositionLocalOf { false }

/**
 * 번호별 비교 캡처로 떠 있는지. `LocalMapCaptureMode` 와 달리 **라이브 캡처도 포함한다** —
 * 저쪽은 "지도를 비울지"를 정하는 값이고, 이쪽은 "움직임을 멈출지"를 정하는 값이다.
 * 움직이는 중간을 찍으면 같은 아트보드가 회차마다 달라진다.
 */
val LocalCaptureMode = staticCompositionLocalOf { false }

/**
 * 카카오 지도 SDK 초기화 상태. 네이티브 앱 키가 비어 있으면 초기화를 건너뛰고 지도 자리는 비운다.
 */
object MoyeoMapSdk {
    private const val LOG_TAG = "MoyeoMap"

    @Volatile
    private var initialized = false

    val isReady: Boolean
        get() = initialized

    /** 키가 비어 있거나 초기화가 실패하면 false — 호출부는 예외를 신경 쓰지 않아도 된다. */
    fun init(context: Context, appKey: String): Boolean {
        if (initialized) return true
        if (appKey.isBlank()) {
            // 조용히 폴백으로 떨어지면 "지도가 왜 안 뜨는지"를 알 수 없다. 원인을 남긴다.
            Log.w(LOG_TAG, "카카오 지도 초기화 건너뜀 — 네이티브 앱 키가 비어 있습니다.")
            return false
        }
        initialized = runCatching {
            KakaoMapSdk.init(context, appKey)
            KakaoMapSdk.isInitialized()
        }.onFailure { error ->
            Log.w(LOG_TAG, "카카오 지도 초기화 실패 — 지도 자리를 비웁니다.", error)
        }.getOrDefault(false)
        if (!initialized) {
            Log.w(LOG_TAG, "카카오 지도 초기화가 완료되지 않았습니다 (isInitialized=false).")
        } else {
            Log.i(LOG_TAG, "카카오 지도 초기화 완료")
        }
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

/** 여러 방문지를 한 화면에 담을 때 두는 최대 여백 — 전체 화면 지도(기획 11)의 기준값이다. */
private const val FIT_POINTS_MAX_PADDING_DP = 56

/** 지도가 낮아도 마커가 잘리지 않을 최소 여백. */
private const val FIT_POINTS_MIN_PADDING_DP = 20

/** 여백이 지도 짧은 변에서 차지하는 비율. 양쪽에 두므로 24%가 남는 폭의 절반을 넘지 않는다. */
private const val FIT_POINTS_PADDING_RATIO = 0.12f

/**
 * 카메라 맞춤 여백. **지도 크기에 비례**해야 한다.
 *
 * 고정 56dp 는 코스 미리보기 지도(높이 118~140dp)에서 위아래 여백만으로 지도 높이를 넘겨
 * 맞춤을 무너뜨렸다 — 청송 3곳(19.9km)이 남한 전체가 보이는 배율로 찍혔다(14·15·18-3·24 공통).
 * 반대로 여백을 일괄로 줄이면 전체 화면 지도(11)에서 가장자리 마커가 잘린다.
 */
private fun fitPointsPadding(density: Density, mapSize: IntSize): Int {
    val minPadding = with(density) { FIT_POINTS_MIN_PADDING_DP.dp.toPx() }
    val maxPadding = with(density) { FIT_POINTS_MAX_PADDING_DP.dp.toPx() }
    val shortSide = minOf(mapSize.width, mapSize.height)
    if (shortSide <= 0) return maxPadding.toInt()
    return (shortSide * FIT_POINTS_PADDING_RATIO).coerceIn(minPadding, maxPadding).toInt()
}

/** 기획의 경로선·마커 초록. 다크/라이트 공통으로 같은 초록을 쓴다 (기획 11·17-3·18-3). */
private val MapRouteGreen = Color(0xFF4E9B6B)

/**
 * 카카오 지도 실지도. 마커 목록 + 경로선 + "중앙 핀" 모드를 지원하는 앱 공용 지도 컴포저블이다.
 *
 * 폴백은 [fallback] 으로 받는다 — 키 없음 / SDK 초기화 실패 / 지도 인증·렌더 오류 / QA 캡처 모드에서는
 * 실지도를 만들지 않고 [fallback] 만 그린다.
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
    /**
     * 핀을 눌렀을 때 그 핀의 `MapMarker.id`. 기본값은 아무 것도 안 하는 것이라
     * 핀 탭이 필요 없는 지도(코스 미리보기·집합 장소)는 예전 그대로 동작한다.
     */
    onMarkerClick: (String) -> Unit = {},
    fallback: @Composable (Modifier) -> Unit
) {
    val captureMode = LocalMapCaptureMode.current
    var mapFailed by remember { mutableStateOf(false) }
    if (captureMode || runningInstrumentedTest || mapFailed || !MoyeoMapSdk.isReady) {
        // 어떤 이유로 폴백이 그려졌는지 남긴다 — 캡처에 빈 지도가 찍혔을 때 원인을 짚을 수 있어야 한다.
        Log.i(
            "MoyeoMap",
            "지도 폴백 사용 — captureMode=$captureMode instrumented=$runningInstrumentedTest " +
                "mapFailed=$mapFailed sdkReady=${MoyeoMapSdk.isReady}"
        )
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
                        // 인증 실패(패키지명·키해시 미등록)나 렌더 오류 — 폴백으로 내려간다
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

    // 핀 탭. `drawMarkers` 가 `MapMarker.id` 를 라벨 id 로 쓰므로 눌린 라벨에서 그대로 되받는다.
    // `rememberUpdatedState` 로 최신 콜백을 잡는다 — 리스너는 한 번만 붙고 화면은 다시 그려진다.
    val currentOnMarkerClick by rememberUpdatedState(onMarkerClick)
    LaunchedEffect(kakaoMap) {
        val map = kakaoMap ?: return@LaunchedEffect
        runCatching {
            map.setOnLabelClickListener { _, _, label ->
                currentOnMarkerClick(label.labelId)
                true
            }
        }
    }

    val markerArgb = MapRouteGreen.toArgb()
    // 카메라 여백은 지도 크기를 알아야 정할 수 있다 — 고정 여백은 낮은 지도에서 화면을 넘겨 맞춤을 깬다.
    var mapSize by remember { mutableStateOf(IntSize.Zero) }
    LaunchedEffect(kakaoMap, markers, polyline, mapSize) {
        val map = kakaoMap ?: return@LaunchedEffect
        runCatching {
            map.drawMarkers(density, markers, markerArgb)
            map.drawPolyline(density, polyline, markerArgb)
            if (!draggablePin) {
                map.frameCamera(markers, polyline, center, zoomLevel, density, mapSize)
            }
        }.onFailure { mapFailed = true }
    }

    AndroidView(
        factory = { touchHost },
        modifier = modifier.onSizeChanged { mapSize = it }
    )
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
            // `setClickable(true)` 가 없으면 리스너를 붙여도 라벨이 탭을 받지 않는다.
            LabelOptions.from(marker.id, LatLng.from(marker.position.latitude, marker.position.longitude))
                .setStyles(styles)
                .setClickable(true)
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
    density: Density,
    mapSize: IntSize
) {
    val points = (markers.map(MapMarker::position) + polyline).distinct()
    if (points.size >= 2) {
        val padding = fitPointsPadding(density, mapSize)
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
    // 지름 24dp · 글자 11dp. 예전 32dp/13dp 는 **코스 미리보기(높이 160dp)에서 너무 컸다** —
    // 방문지가 10곳이면 마커가 지도를 덮는다. iOS 도 같은 값(24/11)이다.
    val radius = with(density) { 12.dp.toPx() }
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
        textSize = with(density) { 11.dp.toPx() }
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
