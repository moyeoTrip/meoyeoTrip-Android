package kr.hanchae.moyeotrip.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput

/**
 * 오버레이(바텀시트 · 경고 팝업) 공통 배경 — changeLog14 "오버레이 배경 일괄".
 *
 * 딤만 남기고 배경을 비우지 않는다: [background]에 실제로 그 화면에서 열렸을 이전 화면을
 * 그대로 깔고 그 위에 스크림을 얹는다. 스크림 Box가 포인터 입력을 받아 배경 화면으로
 * 상호작용이 새지 않고, 스크림 탭은 [onScrimClick]으로만 처리된다.
 *
 * 스크림은 `clickable` 이 아니라 `detectTapGestures` 로 받는다. `clickable` 은 눌린 자리에서
 * 손가락이 **움직여도** 손을 뗄 때 클릭으로 처리해서, 스크림 위를 위아래로 쓸면 오버레이가 닫혔다.
 * 라이브 캡처(31 나가기)에서 스크롤 스와이프가 팝업을 닫아 다음 페이지에 홈 화면이 찍힌 원인이다.
 *
 * [content]는 스크림 위에 그려지므로 `Modifier.align`으로 시트(하단)·팝업(가운데) 위치를 정한다.
 */
@Composable
fun OverlayBackdrop(
    modifier: Modifier = Modifier,
    scrimAlpha: Float = 0.45f,
    onScrimClick: () -> Unit = {},
    background: @Composable () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier.fillMaxSize()) {
        Box(Modifier.matchParentSize()) { background() }
        Box(
            Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = scrimAlpha))
                .pointerInput(onScrimClick) { detectTapGestures(onTap = { onScrimClick() }) }
        )
        content()
    }
}
