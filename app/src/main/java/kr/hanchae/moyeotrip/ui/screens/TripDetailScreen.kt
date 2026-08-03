package kr.hanchae.moyeotrip.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.hanchae.moyeotrip.R
import kr.hanchae.moyeotrip.data.MockTripRepository
import kr.hanchae.moyeotrip.data.TripCourse
import kr.hanchae.moyeotrip.data.TripRecruitment
import kr.hanchae.moyeotrip.domain.ApplicationNotePolicy
import kr.hanchae.moyeotrip.domain.recruitmentSummary

@Composable
fun TripDetailScreen(tripId: String, onBack: () -> Unit, onOpenChatRoom: (String) -> Unit) {
    val trip = MockTripRepository.findTrip(tripId)
    val course = MockTripRepository.findCourseForTrip(trip)
    var showApplySheet by rememberSaveable { mutableStateOf(false) }
    var isFavorite by rememberSaveable(trip.id) { mutableStateOf(false) }
    var actionMessage by rememberSaveable(trip.id) { mutableStateOf<String?>(null) }
    val isApplied = MockTripRepository.isAppliedToTrip(trip.id)
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 118.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                GroupDetailHero(
                    course = course,
                    trip = trip,
                    onBack = onBack,
                    onOpenChatRoom = { onOpenChatRoom(MockTripRepository.chatThreadIdForTrip(trip.id)) }
                )
            }
            actionMessage?.let { message ->
                item {
                    TripActionBanner(
                        message = message,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
            item {
                GroupDetailPanel(course = course, trip = trip)
            }
        }

        TripDetailBottomBar(
            actionLabel = trip.applyActionLabel(isApplied),
            isFavorite = isFavorite,
            onToggleFavorite = {
                isFavorite = !isFavorite
                actionMessage = if (isFavorite) {
                    "찜한 모임에 담았어요."
                } else {
                    "찜한 모임에서 제외했어요."
                }
            },
            onApply = {
                if (isApplied) {
                    onOpenChatRoom(MockTripRepository.chatThreadIdForTrip(trip.id))
                } else if (trip.statusLabel == "모집취소") {
                    actionMessage = "모집이 취소되어 신청할 수 없어요."
                } else {
                    showApplySheet = true
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (showApplySheet) {
            ApplicationSheet(
                course = course,
                onDismiss = { showApplySheet = false },
                onApplicationSubmit = {
                    if (!MockTripRepository.isAppliedToTrip(trip.id)) {
                        MockTripRepository.applyToTrip(trip.id)
                    }
                },
                onOpenChat = {
                    showApplySheet = false
                    onOpenChatRoom(MockTripRepository.chatThreadIdForTrip(trip.id))
                }
            )
        }
    }
}

@Composable
private fun GroupDetailHero(course: TripCourse, trip: TripRecruitment, onBack: () -> Unit, onOpenChatRoom: () -> Unit) {
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(284.dp)
    ) {
        Image(
            painter = painterResource(id = course.tripHeroImageResId(isDark)),
            contentDescription = "${course.title} 대표 이미지",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.14f),
                            Color.Black.copy(alpha = 0.18f),
                            Color.Black.copy(alpha = 0.56f)
                        )
                    )
                )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로",
                    tint = Color.White
                )
            }
            Text(
                text = "모집 상세",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onOpenChatRoom) {
                Icon(
                    imageVector = Icons.Filled.ChatBubble,
                    contentDescription = "채팅",
                    tint = Color.White
                )
            }
        }
        StatusChip(
            text = trip.detailStatusText(),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 48.dp)
        )
    }
}

@Composable
private fun GroupDetailPanel(course: TripCourse, trip: TripRecruitment) {
    val colors = MaterialTheme.colorScheme
    val recruitment = trip.recruitmentSummary()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 0.dp),
        shape = RoundedCornerShape(24.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = trip.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = course.detailMetaText(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarStack(avatars = trip.participantAvatars())
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "최소 ${trip.minParticipants}명 이상",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onSurfaceVariant
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = recruitment.displayText,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                    StatusChip(
                        text = when {
                            trip.statusLabel == "모집취소" -> "모집 종료"
                            trip.joined < trip.capacity -> "신청 가능"
                            else -> "대기 가능"
                        },
                        container = colors.primaryContainer,
                        content = colors.primary
                    )
                }
                LinearProgressIndicator(
                    progress = { recruitment.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = colors.primary,
                    trackColor = colors.outline.copy(alpha = 0.45f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceVariant)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailInfoRow(label = "일정", value = trip.scheduleDate)
                DetailInfoRow(label = "시간", value = trip.scheduleTime)
                DetailInfoRow(label = "모이는 곳", value = trip.meetingPoint)
            }

            HostSummary(trip = trip, rating = course.rating)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "모임 소개",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = course.recruitmentNote,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            }

            RoutePreview(stops = course.stops)
        }
    }
}

@Composable
private fun DetailInfoRow(label: String, value: String) {
    val colors = MaterialTheme.colorScheme

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
            modifier = Modifier.size(width = 64.dp, height = 20.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = colors.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun HostSummary(trip: TripRecruitment, rating: Double) {
    val colors = MaterialTheme.colorScheme

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        EmojiCircle(emoji = trip.hostAvatar, size = 44.dp, container = colors.primaryContainer)
        Column {
            Text(
                text = "호스트",
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant
            )
            Text(
                text = trip.host,
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "매너 점수 ${rating}점",
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TripActionBanner(message: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun RoutePreview(stops: List<String>) {
    val colors = MaterialTheme.colorScheme

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "코스 미리보기",
            style = MaterialTheme.typography.titleMedium,
            color = colors.onSurface,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surfaceVariant)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            stops.take(4).forEachIndexed { index, stop ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (index == 0) colors.secondary else colors.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        text = stop,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun TripDetailBottomBar(
    actionLabel: String,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(width = 52.dp, height = 48.dp)
                    .clickable(onClick = onToggleFavorite)
                    .testTag("trip-favorite-button"),
                shape = RoundedCornerShape(9.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.outline)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isFavorite) "찜 해제" else "찜",
                        tint = if (isFavorite) Color(0xFFFF7259) else colors.onSurface
                    )
                }
            }
            Button(
                onClick = onApply,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("trip-apply-button"),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                shape = RoundedCornerShape(9.dp)
            ) {
                Text(text = actionLabel, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun ApplicationSheet(
    course: TripCourse,
    onDismiss: () -> Unit,
    onApplicationSubmit: () -> Unit,
    onOpenChat: () -> Unit
) {
    var message by rememberSaveable {
        mutableStateOf("처음 참여라 집결지에서 같이 움직이고 싶어요.")
    }
    var isSubmitted by rememberSaveable { mutableStateOf(false) }
    val isMessageValid = ApplicationNotePolicy.isValid(message)
    val colors = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Image(
            painter = painterResource(id = course.tripHeroImageResId(isDark)),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(330.dp)
                .align(Alignment.TopCenter),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(330.dp)
                .align(Alignment.TopCenter)
                .background(Color.Black.copy(alpha = 0.44f))
        )
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 14.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로",
                tint = Color.White
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = colors.surface
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "함께 가기 신청",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "닫기")
                    }
                }

                if (isSubmitted) {
                    ApplicationCompletionCard(message = message)
                    Button(
                        onClick = onOpenChat,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("application-open-chat"),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        shape = RoundedCornerShape(9.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.ChatBubble, contentDescription = null)
                        Text(
                            text = "모임 채팅으로 이동",
                            modifier = Modifier.padding(start = 8.dp),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Text(
                            text = "한마디를 남겨주세요!",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = message,
                            onValueChange = { message = ApplicationNotePolicy.sanitize(it) },
                            placeholder = { Text("간단한 인사나 기대하는 마음을 남겨주세요.") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4,
                            supportingText = {
                                Text(
                                    text = ApplicationNotePolicy.helperText(message),
                                    color = if (isMessageValid) colors.onSurfaceVariant else colors.error
                                )
                            },
                            isError = !isMessageValid
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "내 소개 카드",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceVariant)
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EmojiCircle(emoji = "🐻", size = 52.dp, container = colors.primaryContainer)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "모여트립이",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.onSurface,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "자연 속에서 편안한 걸 좋아해요! 사진 찍는 것도 좋아합니다.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            onApplicationSubmit()
                            isSubmitted = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isMessageValid,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        shape = RoundedCornerShape(9.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null)
                        Text(
                            text = "신청하기",
                            modifier = Modifier.padding(start = 8.dp),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ApplicationCompletionCard(message: String) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.primaryContainer)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(34.dp)
            )
            Column {
                Text(
                    text = "모집에 참여됐어요",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onPrimaryContainer,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "이제 모임 채팅에서 인사하고 집결 정보를 확인해요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onPrimaryContainer.copy(alpha = 0.78f)
                )
            }
        }
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onPrimaryContainer
        )
    }
}

@Composable
private fun AvatarStack(avatars: List<String>, limit: Int = 3) {
    Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
        avatars.take(limit).forEach { avatar ->
            EmojiCircle(
                emoji = avatar,
                size = 32.dp,
                container = MaterialTheme.colorScheme.primaryContainer,
                borderColor = MaterialTheme.colorScheme.surface
            )
        }
        if (avatars.size > limit) {
            EmojiCircle(
                emoji = "+${avatars.size - limit}",
                size = 32.dp,
                container = MaterialTheme.colorScheme.primary,
                content = Color.White,
                textSize = 13.sp,
                borderColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Composable
private fun EmojiCircle(
    emoji: String,
    size: androidx.compose.ui.unit.Dp,
    container: Color,
    content: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    textSize: androidx.compose.ui.unit.TextUnit = 20.sp,
    borderColor: Color? = null
) {
    Surface(
        modifier = Modifier.size(size),
        shape = CircleShape,
        color = container,
        border = borderColor?.let { BorderStroke(2.dp, it) }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = emoji,
                fontSize = textSize,
                color = content,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatusChip(
    text: String,
    modifier: Modifier = Modifier,
    container: Color = Color.Black.copy(alpha = 0.38f),
    content: Color = Color.White
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = container
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = content,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun TripCourse.tripHeroImageResId(isDark: Boolean): Int = when (id) {
    "cheongsong-juwangsan" -> weatherRes(
        isDark = isDark,
        light = R.drawable.weather_fog_seokguram,
        dark = R.drawable.weather_fog_seokguram_night
    )

    "andong-hahoe" -> weatherRes(
        isDark = isDark,
        light = R.drawable.weather_rain_hahoe,
        dark = R.drawable.weather_rain_hahoe_night
    )

    "gyeongju-healing" -> weatherRes(
        isDark = isDark,
        light = R.drawable.weather_sunny_cheomseongdae,
        dark = R.drawable.weather_sunny_cheomseongdae_night
    )

    "pohang-sea",
    "ulleung-island" -> weatherRes(
        isDark = isDark,
        light = R.drawable.weather_wind_homigot,
        dark = R.drawable.weather_wind_homigot_night
    )

    "mungyeong-saejae" -> weatherRes(
        isDark = isDark,
        light = R.drawable.weather_cloudy_bulguksa,
        dark = R.drawable.weather_cloudy_bulguksa_night
    )

    "yeongju-buseoksa" -> weatherRes(
        isDark = isDark,
        light = R.drawable.weather_snow_buseoksa,
        dark = R.drawable.weather_snow_buseoksa_night
    )

    "andong-dosan" -> weatherRes(
        isDark = isDark,
        light = R.drawable.weather_heatwave_dosan,
        dark = R.drawable.weather_heatwave_dosan_night
    )

    else -> weatherRes(
        isDark = isDark,
        light = R.drawable.weather_sunny_cheomseongdae,
        dark = R.drawable.weather_sunny_cheomseongdae_night
    )
}

private fun weatherRes(isDark: Boolean, light: Int, dark: Int): Int = if (isDark) dark else light

private fun TripCourse.detailMetaText(): String = when (id) {
    "cheongsong-juwangsan" -> "청송 · 자연 · 트레킹"

    else -> listOf(region)
        .plus(tags.take(2))
        .joinToString(" · ")
}

private fun TripRecruitment.detailStatusText(): String = when {
    statusLabel == "모집취소" -> "모집취소"
    joined >= capacity -> "마감"
    joined >= minParticipants -> statusLabel
    else -> "모집중"
}

private fun TripRecruitment.applyActionLabel(isApplied: Boolean): String = when {
    isApplied -> "모임 채팅으로 이동"
    statusLabel == "모집취소" -> "모집 종료"
    joined >= capacity -> "대기 신청"
    else -> "함께 가기 신청"
}

private fun TripRecruitment.participantAvatars(): List<String> {
    val pool = listOf(hostAvatar, "🐻", "🦌", "🐢", "🪽", "🐰")
    return pool.take(joined.coerceAtLeast(1))
}
