package kr.hanchae.moyeotrip.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 코스 미리보기 지도에 찍을 방문지 한 곳. 좌표가 없는 방문지는 지도에 올리지 않는다. */
data class CourseRoutePoint(val id: String, val title: String, val position: MoyeoLatLng)

/**
 * 코스 미리보기 지도 (화면기획 14·15).
 *
 * 손으로 그린 가짜 지도를 대신한다 — 좌표가 있는 방문지를 실제 카카오 지도에 순번 마커 + 경로선으로 올린다.
 * 좌표가 하나도 없으면 아무것도 그리지 않는다(호출부가 `points` 를 비워 보내면 섹션째 사라진다).
 */
@Composable
fun CourseRouteMap(points: List<CourseRoutePoint>, modifier: Modifier = Modifier, height: Dp = 160.dp) {
    if (points.isEmpty()) return
    val markers = points.mapIndexed { index, point ->
        MapMarker(
            id = point.id,
            position = point.position,
            shape = MapMarkerShape.Numbered,
            badge = "${index + 1}"
        )
    }
    KakaoMapView(
        center = points.first().position,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .testTag("course-route-map"),
        markers = markers,
        polyline = points.map(CourseRoutePoint::position),
        zoomLevel = 11,
        fallback = { fallbackModifier -> MapUnavailablePlaceholder(fallbackModifier) }
    )
}

/**
 * 실지도를 띄울 수 없을 때(키 없음 · SDK 초기화 실패 · 캡처/계측 실행) 남기는 빈 자리.
 *
 * 예전에는 여기에 손으로 그린 가짜 지도를 그렸다. 가짜 지도는 좌표와 무관한 그림이라
 * "지도가 되는지"를 가려버린다 — 자리만 남기고 이유를 적는다.
 */
@Composable
fun MapUnavailablePlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .testTag("map-unavailable"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "지도를 표시할 수 없어요.",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
