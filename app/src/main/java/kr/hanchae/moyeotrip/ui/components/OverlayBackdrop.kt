package kr.hanchae.moyeotrip.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * 오버레이(바텀시트 · 경고 팝업) 공통 배경 — changeLog14 "오버레이 배경 일괄".
 *
 * 딤만 남기고 배경을 비우지 않는다: [background]에 실제로 그 화면에서 열렸을 이전 화면을
 * 그대로 깔고 그 위에 스크림을 얹는다. 스크림 Box가 포인터 입력을 받아 배경 화면으로
 * 상호작용이 새지 않고, 스크림 탭은 [onScrimClick]으로만 처리된다.
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
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onScrimClick
                )
        )
        content()
    }
}
