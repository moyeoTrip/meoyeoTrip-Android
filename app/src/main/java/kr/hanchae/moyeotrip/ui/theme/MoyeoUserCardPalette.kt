package kr.hanchae.moyeotrip.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.lerp

/**
 * 서버 `NicknameCandidate.color` 의 10가지 값.
 *
 * 공개 프로필(`PublicProfileResponse.nicknameColor`)과 받은 평가
 * (`ReceivedTravelReviewResponse.reviewerNicknameColor`)로 내려온다.
 * 도감·멤버·동행자 목록 응답에는 없어서 목록에서는 색을 쓸 수 없다.
 */
private val USER_COLORS = mapOf(
    "RED" to Color(0xFFD9534F),
    "ORANGE" to Color(0xFFE8853A),
    "YELLOW" to Color(0xFFD9A81C),
    "GREEN" to Color(0xFF2D8F5A),
    "BLUE" to Color(0xFF3B7DD8),
    "NAVY" to Color(0xFF2F4A86),
    "PURPLE" to Color(0xFF7E5BC4),
    "PINK" to Color(0xFFDE5D97),
    "SKY_BLUE" to Color(0xFF3FA9D6),
    "MINT" to Color(0xFF2FB79A)
)

/**
 * 프로필 카드(25)의 색 층.
 *
 * 지정색을 그대로 쓰면 프로필 이미지 배경과 붙어 구분이 안 된다.
 * 그래서 테마색과 섞어 진한 톤(테두리) > 중간 톤(프레임) > 흐린 톤(배경) 3단으로 벌린다.
 * 웹은 `color-mix(in oklab, ...)` 을 쓰는데, 여기서는 Oklab 로 보간해 결과를 맞춘다.
 */
data class MoyeoUserCardPalette(
    val base: Color,
    val border: Color,
    val frame: Color,
    val background: Color,
    val plate: Color,
    val chipContainer: Color,
    val chipContent: Color,
    val glow: Color
)

@Composable
@ReadOnlyComposable
fun rememberUserCardPalette(colorName: String?): MoyeoUserCardPalette {
    val scheme = MaterialTheme.colorScheme
    val base = USER_COLORS[colorName?.uppercase()] ?: ForestGreen
    return MoyeoUserCardPalette(
        base = base,
        border = base.mixOklab(scheme.outlineVariant, 0.76f),
        frame = base.mixOklab(scheme.surface, 0.44f),
        background = base.mixOklab(scheme.surface, 0.22f),
        plate = base.mixOklab(scheme.surface, 0.18f),
        chipContainer = base.mixOklab(scheme.surface, 0.14f),
        chipContent = base.mixOklab(scheme.onSurface, 0.66f),
        glow = base.copy(alpha = 0.52f)
    )
}

/**
 * `color-mix(in oklab, this <ratio>, other)` 와 같은 결과를 낸다.
 *
 * sRGB 에서 그냥 섞으면 감마 때문에 중간색이 탁해진다 —
 * 특히 진한 유저 색과 밝은 표면색을 섞는 테두리 톤에서 눈에 띈다.
 */
private fun Color.mixOklab(other: Color, ratio: Float): Color {
    val a = convert(ColorSpaces.Oklab)
    val b = other.convert(ColorSpaces.Oklab)
    return lerp(b, a, ratio).convert(ColorSpaces.Srgb)
}
