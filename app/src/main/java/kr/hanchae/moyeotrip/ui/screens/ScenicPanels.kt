package kr.hanchae.moyeotrip.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kr.hanchae.moyeotrip.ui.theme.MoyeoTheme

internal data class ScenicPalette(
    val sky: Color,
    val haze: Color,
    val ground: Color,
    val farHill: Color,
    val midHill: Color,
    val nearHill: Color
)

/**
 * 방문지 목록·상세의 썸네일 대체 이미지.
 *
 * 원격 이미지가 없을 때 카테고리 아이콘을 크게 띄우면 "사진이 있어야 하는 자리"가
 * 아이콘 자리로 읽힌다. 코스 카드와 같은 풍경 패널을 그려 사진 자리임을 유지한다.
 */
@Composable
internal fun PlaceScenicPanel(kind: PlaceScenicKind, modifier: Modifier = Modifier, cornerRadius: Dp = 10.dp) {
    val isDark = MoyeoTheme.isDark
    val palette = kind.scenicPalette(isDark)

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(palette.sky)
            .fillMaxSize()
    ) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(palette.sky, palette.haze, palette.ground),
                startY = 0f,
                endY = size.height
            )
        )

        val far = Path().apply {
            moveTo(0f, size.height * 0.50f)
            lineTo(size.width * 0.22f, size.height * 0.30f)
            lineTo(size.width * 0.44f, size.height * 0.47f)
            lineTo(size.width * 0.66f, size.height * 0.27f)
            lineTo(size.width, size.height * 0.44f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(far, palette.farHill)

        val mid = Path().apply {
            moveTo(0f, size.height * 0.70f)
            quadraticTo(size.width * 0.28f, size.height * 0.50f, size.width * 0.54f, size.height * 0.68f)
            quadraticTo(size.width * 0.78f, size.height * 0.82f, size.width, size.height * 0.62f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(mid, palette.midHill)

        val near = Path().apply {
            moveTo(0f, size.height * 0.84f)
            quadraticTo(size.width * 0.34f, size.height * 0.74f, size.width * 0.62f, size.height * 0.86f)
            quadraticTo(size.width * 0.84f, size.height * 0.94f, size.width, size.height * 0.82f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(near, palette.nearHill)
    }
}

/** 방문지 썸네일 풍경 종류. 웹 프로토타입의 `Photo hue` 와 같은 축이다. */
internal enum class PlaceScenicKind { Forest, Coast, Autumn, Hanok, Pebble }

internal fun PlaceScenicKind.scenicPalette(isDark: Boolean): ScenicPalette = when {
    isDark && this == PlaceScenicKind.Coast -> ScenicPalette(
        sky = Color(0xFF263447),
        haze = Color(0xFF1D2A32),
        ground = Color(0xFF16201D),
        farHill = Color(0xFF52606A),
        midHill = Color(0xFF37484E),
        nearHill = Color(0xFF17221F)
    )

    isDark && (this == PlaceScenicKind.Autumn || this == PlaceScenicKind.Hanok) -> ScenicPalette(
        sky = Color(0xFF3A3329),
        haze = Color(0xFF28241F),
        ground = Color(0xFF151B17),
        farHill = Color(0xFF776A58),
        midHill = Color(0xFF4D4438),
        nearHill = Color(0xFF1A211B)
    )

    isDark -> ScenicPalette(
        sky = Color(0xFF304A41),
        haze = Color(0xFF22342E),
        ground = Color(0xFF14201B),
        farHill = Color(0xFF789084),
        midHill = Color(0xFF4B6658),
        nearHill = Color(0xFF17241D)
    )

    this == PlaceScenicKind.Coast -> ScenicPalette(
        sky = Color(0xFFD9E1E5),
        haze = Color(0xFFC8D7DB),
        ground = Color(0xFFE7E7DC),
        farHill = Color(0xFF8D9AA0),
        midHill = Color(0xFF626F72),
        nearHill = Color(0xFF25332F)
    )

    this == PlaceScenicKind.Autumn || this == PlaceScenicKind.Hanok -> ScenicPalette(
        sky = Color(0xFFE0D3BF),
        haze = Color(0xFFE8E3D6),
        ground = Color(0xFFEFEFE9),
        farHill = Color(0xFFC0A887),
        midHill = Color(0xFF917E62),
        nearHill = Color(0xFF3C352A)
    )

    this == PlaceScenicKind.Pebble -> ScenicPalette(
        sky = Color(0xFFE4E6E0),
        haze = Color(0xFFEDEEE9),
        ground = Color(0xFFE9EBE5),
        farHill = Color(0xFFAFB4AB),
        midHill = Color(0xFF7E857A),
        nearHill = Color(0xFF2E332C)
    )

    else -> ScenicPalette(
        sky = Color(0xFFDCE9DF),
        haze = Color(0xFFEAF0E5),
        ground = Color(0xFFE8EFE4),
        farHill = Color(0xFF9CAFA2),
        midHill = Color(0xFF708777),
        nearHill = Color(0xFF25382F)
    )
}
