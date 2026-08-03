package kr.hanchae.moyeotrip.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.hanchae.moyeotrip.R

@Composable
fun StartupSplashScreen() {
    val isDark = isSystemInDarkTheme()
    val imageRes = if (isDark) R.drawable.splash_generated_night else R.drawable.splash_generated
    val background = if (isDark) Color(0xFF071712) else Color(0xFFEFF7EF)
    val titleColor = if (isDark) Color(0xFFE8F6ED) else Color(0xFF0F5C3D)
    val subtitleColor = if (isDark) Color(0xFFBFE8CB) else Color(0xFF0F4D35)
    val overlay = if (isDark) {
        Brush.verticalGradient(
            0f to Color(0x4D040C0A),
            0.42f to Color(0x05040C0A),
            1f to Color(0x2E040C0A)
        )
    } else {
        Brush.verticalGradient(
            0f to Color(0x1AFFFFFF),
            0.38f to Color.Transparent
        )
    }
    val textShadow = Shadow(
        color = if (isDark) Color.Black.copy(alpha = 0.46f) else Color.Transparent,
        offset = Offset(0f, 2f),
        blurRadius = if (isDark) 18f else 0f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .testTag("screen.splash")
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(overlay)
        )
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 156.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "모여트립 in 경북",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 42.sp,
                    shadow = textShadow
                ),
                color = titleColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = "경상북도 특화 반패키지 매칭 플랫폼",
                modifier = Modifier.padding(top = 10.dp),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    shadow = textShadow
                ),
                color = subtitleColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
