package kr.hanchae.moyeotrip.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 점선 테두리 — 「방문지 추가」·「사진 추가」처럼 **아직 담기지 않은 자리**를 나타내는 박스에 쓴다.
 *
 * 왜 필요한가: 기획·웹·iOS 는 이런 자리를 모두 **점선**으로 그리는데
 * (`border: 1px dashed` · `StrokeStyle(dash: [4])`), Compose 의 `BorderStroke` 로는
 * 점선을 그릴 수 없어 안드로이드만 **실선**이었다 — 같은 화면이 플랫폼마다 달라 보였다
 * (2026-09-09 사용자 지적으로 시작한 전수 확인에서 4곳이 나왔다).
 *
 * 점선 간격(4 on · 4 off)과 선 두께 1dp 는 iOS `DesignOutlineButtonStyle(dashed:)` 와 같은 값이다.
 * `Surface` 의 `border` 는 **주지 않고** 이 modifier 만 건다 — 둘 다 주면 선이 겹쳐 두 겹으로 보인다.
 */
fun Modifier.moyeoDashedOutline(color: Color, radius: Dp = 12.dp) = drawBehind {
    val dash = 4.dp.toPx()
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(radius.toPx()),
        style = Stroke(
            width = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash))
        )
    )
}
