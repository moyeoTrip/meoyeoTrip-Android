package kr.hanchae.moyeotrip.ui

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 번호별 비교 캡처(`moyeo_screen`)로 실행됐는지. `MoyeoTripApp` 의 `startScreen != null` 을 그대로 내려준다.
 *
 * 캡처에서는 기기 상태(저장된 최근 검색어·테마 설정)나 실제 빌드 버전이 아니라
 * **화면기획의 목데이터**를 보여줘야 픽셀·텍스트가 일치한다.
 *
 * 라이브 캡처(`moyeo_live_data`)에서는 false 다 — 실데이터를 보러 찍는 캡처에 목데이터를 섞지 않는다.
 */
val LocalCaptureMode = staticCompositionLocalOf { false }
