package kr.hanchae.moyeotrip.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEFE3),
    onPrimaryContainer = Color(0xFF155735),
    secondary = Coral,
    onSecondary = Color.White,
    secondaryContainer = CoralSoft,
    onSecondaryContainer = Ink,
    tertiary = SkyBlue,
    onTertiary = Color.White,
    background = CreamBackground,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF7F8F7),
    onSurfaceVariant = MutedInk,
    outline = Color(0xFFD9DDD9),
    // 화면기획 `line100`. 정의하지 않으면 Material 기본값(#CAC4D0)이 들어와 구분선이 보라색으로 보인다
    outlineVariant = Color(0xFFEEF0EE),
    // 웹 토큰의 danger 계열 — 기본 M3 값을 쓰면 다크에서 연분홍 버튼이 된다
    error = Color(0xFFE85547),
    onError = Color.White,
    errorContainer = Color(0xFFFFDDD8),
    onErrorContainer = Color(0xFFB73520)
)

private val DarkColorScheme = darkColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1C3728),
    onPrimaryContainer = Color(0xFFB4DDC3),
    secondary = Coral,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4A241B),
    onSecondaryContainer = Color(0xFFFFE7DF),
    tertiary = SkyBlue,
    onTertiary = Color.White,
    background = Color(0xFF0D1411),
    onBackground = Color(0xFFF4F8F5),
    surface = Color(0xFF141D19),
    onSurface = Color(0xFFF4F8F5),
    surfaceVariant = Color(0xFF18231E),
    onSurfaceVariant = Color(0xFFA7B6AE),
    outline = Color(0xFF34453D),
    outlineVariant = Color(0xFF24332D),
    error = Color(0xFFE85547),
    onError = Color.White,
    errorContainer = Color(0xFF44201A),
    onErrorContainer = Color(0xFFFFAEA0)
)

/**
 * 화면기획 디자인 시스템의 브랜드 틴트/시맨틱 색.
 *
 * Material 의 colorScheme 만으로는 "연한 초록 안내 카드", "주의 노랑 배너" 같은
 * 틴트 표면을 표현할 수 없어서 화면마다 밝은 색을 하드코딩하게 되고,
 * 그 결과 다크 모드에서 카드만 하얗게 남는다. 여기서 한 번만 뒤집는다.
 */
data class MoyeoTints(
    val primaryTint: Color,
    val primaryTintStrong: Color,
    val onPrimaryTint: Color,
    /** 화면기획 `primary600` — 틴트 위 본문/보조 텍스트와 아이콘에 쓰는 한 단 밝은 초록. */
    val primaryEmphasis: Color,
    val warningTint: Color,
    val onWarningTint: Color,
    val dangerTint: Color,
    val onDangerTint: Color,
    val accentTint: Color,
    val onAccentTint: Color,
    /** 화면기획 `info` 계열 — 친구 요청처럼 파란 톤을 쓰는 알림 배경/아이콘. */
    val infoTint: Color,
    val onInfoTint: Color,
    val mapGreen: Color,
    val mapWater: Color,
    val softLine: Color,
    /** 내가 보낸 채팅 말풍선. */
    val chatMine: Color,
    /** 시스템 안내 메시지 말풍선. */
    val systemMessage: Color
)

private val LightTints = MoyeoTints(
    primaryTint = Color(0xFFF0F8F4),
    primaryTintStrong = Color(0xFFDCEFE3),
    onPrimaryTint = Color(0xFF155735),
    primaryEmphasis = Color(0xFF1F7346),
    warningTint = Color(0xFFFFF1D6),
    onWarningTint = Color(0xFFA67318),
    dangerTint = Color(0xFFFFDDD8),
    onDangerTint = Color(0xFFB73520),
    accentTint = Color(0xFFFFE4DA),
    onAccentTint = Color(0xFFB64227),
    infoTint = Color(0xFFE8F1FB),
    onInfoTint = Color(0xFF4A90E2),
    mapGreen = Color(0xFFE4F0E7),
    mapWater = Color(0xFFCFE2EA),
    softLine = Color(0xFFEEF0EE),
    chatMine = Color(0xFFE5F4E8),
    systemMessage = Color(0xFFE7F3E7)
)

private val DarkTints = MoyeoTints(
    primaryTint = Color(0xFF12251C),
    primaryTintStrong = Color(0xFF1C3728),
    onPrimaryTint = Color(0xFFB4DDC3),
    primaryEmphasis = Color(0xFF7EC49B),
    warningTint = Color(0xFF3A2B12),
    onWarningTint = Color(0xFFF0C77A),
    dangerTint = Color(0xFF44201A),
    onDangerTint = Color(0xFFFFAEA0),
    accentTint = Color(0xFF46231A),
    onAccentTint = Color(0xFFFFAE95),
    infoTint = Color(0xFF1B2B3D),
    onInfoTint = Color(0xFF7FB3EE),
    mapGreen = Color(0xFF263B31),
    mapWater = Color(0xFF203946),
    softLine = Color(0xFF24332D),
    chatMine = Color(0xFF163B2A),
    systemMessage = Color(0xFF1C3728)
)

private val LocalMoyeoTints = staticCompositionLocalOf { LightTints }

/** 화면에서 `MoyeoTints.current.primaryTint` 처럼 쓴다. */
val MoyeoTintsProvider: androidx.compose.runtime.ProvidableCompositionLocal<MoyeoTints>
    get() = LocalMoyeoTints

private val LocalMoyeoDarkTheme = staticCompositionLocalOf { false }

object MoyeoTheme {
    val tints: MoyeoTints
        @Composable
        get() = LocalMoyeoTints.current

    /**
     * 현재 적용된 테마가 다크인지. `isSystemInDarkTheme()` 는 시스템 설정만 읽어서
     * 캡처 도구가 인텐트로 강제한 테마(moyeo_force_theme)를 무시한다 —
     * 낮/밤 이미지 선택은 반드시 이 값을 봐야 한다.
     */
    val isDark: Boolean
        @Composable
        get() = LocalMoyeoDarkTheme.current

    /**
     * 딤 위에 뜨는 표면(바텀시트·중앙 팝업)의 컨테이너 색 — 화면기획 `bgRaised` (changeLog15).
     *
     * 다크에서 `surface`(#141D19)는 화면 배경과 거의 같아 시트 경계가 사라진다.
     * 기획의 시트 표면은 한 단 밝은 #18231E(= 다크 `surfaceVariant`)이고,
     * 라이트에서는 두 토큰이 사실상 같은 흰색이라 `surface`를 그대로 쓴다.
     */
    val sheetSurface: Color
        @Composable
        get() = cardSurface

    /**
     * 화면기획 `bgBase` — 페이지 바탕.
     *
     * 라이트의 기본 배경(#F7F8F7 = `background`)은 기획의 `bgSubtle`과 같은 값이라,
     * 회색 채움 박스를 올리면 박스가 배경에 묻힌다. 기획의 페이지는 흰색이므로
     * 그런 화면에서는 이 값을 컨테이너 색으로 쓴다. 다크는 기획과 같은 #0D1411.
     */
    val pageSurface: Color
        @Composable
        get() = if (isDark) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surface

    /** 화면기획 `bgSubtle` — 페이지 위에 얹는 회색 인용·안내 박스. */
    val subtleSurface: Color
        @Composable
        get() = if (isDark) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant

    /** 화면기획 `bgRaised` — 카드·행 그룹 표면. */
    val cardSurface: Color
        @Composable
        get() = if (isDark) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
}

private val MoyeoShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun MoyeoTripTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalMoyeoTints provides if (darkTheme) DarkTints else LightTints,
        LocalMoyeoDarkTheme provides darkTheme
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = MoyeoTypography,
            shapes = MoyeoShapes,
            content = content
        )
    }
}
