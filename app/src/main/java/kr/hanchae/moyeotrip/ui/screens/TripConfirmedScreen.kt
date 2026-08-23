package kr.hanchae.moyeotrip.ui.screens

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kr.hanchae.moyeotrip.R
import kr.hanchae.moyeotrip.ui.components.emphasized
import kr.hanchae.moyeotrip.ui.theme.MoyeoTheme

@Composable
fun TripConfirmedScreen(onBack: () -> Unit, onOpenChat: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .testTag("trip-confirmed-screen")
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                }
                Text(
                    text = "주왕산 & 주산지 힐링 트레킹",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.size(48.dp))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 92.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 18.dp)
                        .width(252.dp)
                        .height(142.dp)
                        .clip(RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.trip_confirmed_mascots),
                        contentDescription = "여행 확정을 함께 축하하는 곰, 토끼, 너구리 캐릭터",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(scaleX = 1.14f, scaleY = 1.14f),
                        contentScale = ContentScale.Crop
                    )
                }
                Text(
                    text = "여행이 확정됐어요!",
                    modifier = Modifier.padding(top = 18.dp),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    // 화면기획 20-4는 모인 인원만 굵은 초록으로 강조한다
                    text = emphasized(
                        "5월 22일 마감까지 5명이 모였어요.\n이제 함께 떠나기만 하면 돼요.",
                        "5명",
                        boldWeight = FontWeight.ExtraBold,
                        boldColor = MoyeoTheme.tints.primaryEmphasis
                    ),
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                ConfirmedTripCard()
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.outline.copy(alpha = .6f))
                ) {
                    Row(
                        modifier = Modifier.padding(13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = RoundedCornerShape(11.dp), color = colors.surfaceVariant) {
                            Icon(
                                Icons.Filled.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.padding(10.dp).size(20.dp)
                            )
                        }
                        Text(
                            text = "확정 카드를 이미지로 저장해 공유할 수 있어요",
                            modifier = Modifier.weight(1f).padding(horizontal = 11.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                        Icon(Icons.Filled.Share, contentDescription = "확정 카드 공유", modifier = Modifier.size(19.dp))
                    }
                }
                Text(
                    text = "확정 이후에는 경로가 잠겨요. 변경이 필요하면 채팅방 공지로 알려주세요.",
                    modifier = Modifier.padding(top = 14.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        ConfettiBurst(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 108.dp)
                .height(202.dp)
        )
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            color = colors.surface,
            shadowElevation = 8.dp
        ) {
            Button(
                onClick = onOpenChat,
                modifier = Modifier.fillMaxWidth().padding(
                    start = 20.dp,
                    top = 10.dp,
                    end = 20.dp,
                    bottom = 24.dp
                ).height(54.dp),
                // 다른 플랫폼의 CTA는 꼭지점만 둥근 사각형이다. Material 기본 알약형은 혼자 튄다.
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("채팅방으로 가기")
            }
        }
    }
}

@Composable
private fun ConfirmedTripCard() {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        shape = RoundedCornerShape(16.dp),
        color = colors.primaryContainer,
        border = BorderStroke(1.dp, colors.primary.copy(alpha = .25f))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(colors.primary, CircleShape))
                Text(
                    "확정된 여행",
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.primary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            ConfirmedInfoRow(Icons.Filled.CalendarMonth, "5/25(토) 당일치기 · 08:00 - 18:00")
            ConfirmedInfoRow(Icons.Filled.LocationOn, "07:50 청송 시외버스터미널 정문 앞")
            ConfirmedInfoRow(Icons.Filled.Groups, "3명 · 최소 3명 충족")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                listOf("🐻", "🦌", "🐰", "🐢").forEachIndexed { index, emoji ->
                    Surface(
                        modifier = Modifier.size(30.dp).graphicsLayer { translationX = (-index * 4).dp.toPx() },
                        shape = CircleShape,
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.primaryContainer)
                    ) {
                        Box(contentAlignment = Alignment.Center) { Text(emoji) }
                    }
                }
                Surface(modifier = Modifier.size(30.dp), shape = CircleShape, color = colors.surfaceVariant) {
                    Box(contentAlignment = Alignment.Center) { Text("+1", style = MaterialTheme.typography.labelSmall) }
                }
            }
        }
    }
}

@Composable
private fun ConfirmedInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(17.dp), tint = MaterialTheme.colorScheme.primary)
        Text(
            text,
            modifier = Modifier.padding(start = 9.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ConfettiBurst(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val motionEnabled = remember(context) {
        runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
        }.getOrDefault(true)
    }
    val progress = remember { Animatable(if (motionEnabled) 0f else .35f) }
    val palette = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        Color(0xFF6CB08B),
        Color(0xFFE4A43A)
    )
    LaunchedEffect(motionEnabled) {
        if (motionEnabled) progress.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
    }
    Canvas(modifier = modifier) {
        repeat(18) { index ->
            val startX = size.width * (((index * 37) % 96) / 100f)
            val startY = size.height * ((6 + ((index * 53) % 34)) / 100f)
            val drift = (-34 + ((index * 29) % 72)).dp.toPx() * progress.value
            val fall = 84.dp.toPx() * progress.value
            val pieceWidth = (6 + (index % 3) * 3).dp.toPx()
            val alpha = if (motionEnabled) (1f - progress.value * .45f).coerceAtLeast(0f) else .7f
            rotate((index * 47f) + progress.value * 220f, pivot = androidx.compose.ui.geometry.Offset(startX, startY)) {
                drawRoundRect(
                    color = palette[index % palette.size].copy(alpha = alpha),
                    topLeft = androidx.compose.ui.geometry.Offset(startX + drift, startY + fall),
                    size = androidx.compose.ui.geometry.Size(pieceWidth, pieceWidth * .5f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
                )
            }
        }
    }
}
