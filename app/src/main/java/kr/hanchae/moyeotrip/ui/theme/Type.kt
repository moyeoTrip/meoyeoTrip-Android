package kr.hanchae.moyeotrip.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kr.hanchae.moyeotrip.R

private val LineSeedSansKr = FontFamily(
    Font(R.font.line_seed_sans_kr_regular, FontWeight.Normal),
    Font(R.font.line_seed_sans_kr_bold, FontWeight.Bold),
    Font(R.font.line_seed_sans_kr_bold, FontWeight.ExtraBold),
    Font(R.font.line_seed_sans_kr_bold, FontWeight.Black)
)

private fun moyeoTextStyle(
    fontSize: androidx.compose.ui.unit.TextUnit,
    lineHeight: androidx.compose.ui.unit.TextUnit,
    fontWeight: FontWeight
) = TextStyle(
    fontFamily = LineSeedSansKr,
    fontSize = fontSize,
    lineHeight = lineHeight,
    fontWeight = fontWeight
)

val MoyeoTypography = Typography(
    headlineLarge = moyeoTextStyle(
        fontSize = 28.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight.ExtraBold
    ),
    headlineMedium = moyeoTextStyle(
        fontSize = 21.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.ExtraBold
    ),
    headlineSmall = moyeoTextStyle(
        fontSize = 19.sp,
        lineHeight = 25.sp,
        fontWeight = FontWeight.ExtraBold
    ),
    titleLarge = moyeoTextStyle(
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.ExtraBold
    ),
    titleMedium = moyeoTextStyle(
        fontSize = 17.sp,
        lineHeight = 23.sp,
        fontWeight = FontWeight.Bold
    ),
    titleSmall = moyeoTextStyle(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Bold
    ),
    bodyLarge = moyeoTextStyle(
        fontSize = 15.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Normal
    ),
    bodyMedium = moyeoTextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal
    ),
    bodySmall = moyeoTextStyle(
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Normal
    ),
    labelLarge = moyeoTextStyle(
        fontSize = 14.sp,
        lineHeight = 19.sp,
        fontWeight = FontWeight.Bold
    ),
    labelMedium = moyeoTextStyle(
        fontSize = 12.sp,
        lineHeight = 17.sp,
        fontWeight = FontWeight.Bold
    ),
    labelSmall = moyeoTextStyle(
        fontSize = 11.sp,
        lineHeight = 15.sp,
        fontWeight = FontWeight.Bold
    )
)
