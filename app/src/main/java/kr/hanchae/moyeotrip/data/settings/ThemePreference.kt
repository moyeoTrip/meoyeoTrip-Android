package kr.hanchae.moyeotrip.data.settings

/**
 * 29 설정 › 화면 › 테마 의 3상태. 값 문구는 화면기획 그대로다.
 *
 * 화면기획에 테마 선택 시트가 없으므로 행을 탭하면 [next] 순서로 순환한다(changeLog 브리프 W2).
 */
enum class ThemePreference(val storageValue: String, val label: String) {
    System("system", "시스템 기본"),
    Light("light", "라이트"),
    Dark("dark", "다크");

    fun next(): ThemePreference = when (this) {
        System -> Light
        Light -> Dark
        Dark -> System
    }

    companion object {
        fun parse(raw: String?): ThemePreference =
            entries.firstOrNull { it.storageValue.equals(raw?.trim(), ignoreCase = true) } ?: System
    }
}

/**
 * 실제로 적용할 테마.
 *
 * 캡처 도구가 지정한 강제 테마(`moyeo_force_theme`)는 **사용자 설정보다 항상 우선한다** —
 * 이 순서가 깨지면 라이트 캡처에 다크 화면이 섞였던 과거 회귀가 재발한다.
 */
fun resolveDarkTheme(forcedDarkTheme: Boolean?, preference: ThemePreference, systemDarkTheme: Boolean): Boolean =
    forcedDarkTheme ?: when (preference) {
        ThemePreference.System -> systemDarkTheme
        ThemePreference.Light -> false
        ThemePreference.Dark -> true
    }
