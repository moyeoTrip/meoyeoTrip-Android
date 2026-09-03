package kr.hanchae.moyeotrip.ui

import androidx.compose.runtime.staticCompositionLocalOf
import kr.hanchae.moyeotrip.data.ServerDataDependencies

/**
 * 로그인 완료 상태에서만 non-null 로 내려온다.
 * null 이면 읽어올 근거가 없다는 뜻이라 화면은 빈 상태를 그린다
 * (미로그인, 캡처 라우트, 데모 모드 전부 null).
 * 라이브 캡처(`moyeo_live_data`)는 캡처 라우트여도 non-null 로 내려온다.
 */
val LocalServerData = staticCompositionLocalOf<ServerDataDependencies?> { null }
