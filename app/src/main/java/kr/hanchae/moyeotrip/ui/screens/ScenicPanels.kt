package kr.hanchae.moyeotrip.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import kr.hanchae.moyeotrip.data.TripCourse

@Composable
internal fun CourseScenicPanel(course: TripCourse, modifier: Modifier = Modifier, cornerRadius: Dp = 10.dp) {
    val isDark = isSystemInDarkTheme()
    val palette = course.scenicPalette(isDark)

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
            moveTo(0f, size.height * 0.48f)
            lineTo(size.width * 0.18f, size.height * 0.30f)
            lineTo(size.width * 0.37f, size.height * 0.44f)
            lineTo(size.width * 0.56f, size.height * 0.26f)
            lineTo(size.width * 0.78f, size.height * 0.45f)
            lineTo(size.width, size.height * 0.34f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(far, palette.farHill)

        val mid = Path().apply {
            moveTo(0f, size.height * 0.68f)
            lineTo(size.width * 0.23f, size.height * 0.48f)
            lineTo(size.width * 0.43f, size.height * 0.62f)
            lineTo(size.width * 0.66f, size.height * 0.42f)
            lineTo(size.width * 0.90f, size.height * 0.61f)
            lineTo(size.width, size.height * 0.52f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(mid, palette.midHill)

        val near = Path().apply {
            moveTo(0f, size.height * 0.76f)
            quadraticTo(size.width * 0.26f, size.height * 0.62f, size.width * 0.48f, size.height * 0.75f)
            quadraticTo(size.width * 0.70f, size.height * 0.88f, size.width, size.height * 0.70f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(near, palette.nearHill)
    }
}

private data class ScenicPalette(
    val sky: Color,
    val haze: Color,
    val ground: Color,
    val farHill: Color,
    val midHill: Color,
    val nearHill: Color
)

private fun TripCourse.scenicPalette(isDark: Boolean): ScenicPalette {
    val autumn = region in listOf("안동", "경주")
    val sea = region in listOf("포항", "울릉")

    return when {
        isDark && sea -> ScenicPalette(
            sky = Color(0xFF263447),
            haze = Color(0xFF1D2A32),
            ground = Color(0xFF16201D),
            farHill = Color(0xFF52606A),
            midHill = Color(0xFF37484E),
            nearHill = Color(0xFF17221F)
        )

        isDark && autumn -> ScenicPalette(
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

        sea -> ScenicPalette(
            sky = Color(0xFFD9E1E5),
            haze = Color(0xFFC8D7DB),
            ground = Color(0xFFE7E7DC),
            farHill = Color(0xFF8D9AA0),
            midHill = Color(0xFF626F72),
            nearHill = Color(0xFF25332F)
        )

        autumn -> ScenicPalette(
            sky = Color(0xFFE0D3BF),
            haze = Color(0xFFE8E3D6),
            ground = Color(0xFFEFEFE9),
            farHill = Color(0xFFC0A887),
            midHill = Color(0xFF917E62),
            nearHill = Color(0xFF3C352A)
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
}
