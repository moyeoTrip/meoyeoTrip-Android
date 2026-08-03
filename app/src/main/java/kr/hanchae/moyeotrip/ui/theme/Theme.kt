package kr.hanchae.moyeotrip.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE5F4E8),
    onPrimaryContainer = ForestDeep,
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
    outline = Color(0xFFD9DDD9)
)

private val DarkColorScheme = darkColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF07110E),
    onPrimaryContainer = Color(0xFFF4F8F5),
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
    outline = Color(0xFF34453D)
)

private val MoyeoShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun MoyeoTripTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = MoyeoTypography,
        shapes = MoyeoShapes,
        content = content
    )
}
