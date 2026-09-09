package kr.hanchae.moyeotrip.ui.components

import androidx.compose.ui.unit.dp

/**
 * 하단 주 버튼(초록 CTA)의 **정본 크기**. 네 표면이 같은 값을 써야 한다.
 *
 * 기획 `Btn`(height 48 · radius 12) · 웹 같은 값 · iOS `MoyeoTheme.ctaHeight`/`ctaRadius`.
 * 값을 주지 않으면 Material3 기본값(40dp · 알약)이 쓰여 표면마다 최대 6pt 씩 갈렸다 —
 * 측정표는 `docs/ui-comparison/BUTTON-GEOMETRY.md` 다
 * (사용자 요청 「버튼 색상과 크기 정밀 전수조사」, 2026-09-09).
 */
val MOYEO_CTA_HEIGHT = 48.dp
val MOYEO_CTA_RADIUS = 12.dp
