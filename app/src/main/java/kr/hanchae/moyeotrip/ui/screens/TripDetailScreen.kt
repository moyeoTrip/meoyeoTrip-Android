package kr.hanchae.moyeotrip.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch
import kr.hanchae.moyeotrip.R
import kr.hanchae.moyeotrip.data.MockTripRepository
import kr.hanchae.moyeotrip.data.ServerDataDependencies
import kr.hanchae.moyeotrip.data.TripCourse
import kr.hanchae.moyeotrip.data.TripRecruitment
import kr.hanchae.moyeotrip.data.courses.TravelCourse
import kr.hanchae.moyeotrip.data.rooms.ChatRoomDetail
import kr.hanchae.moyeotrip.data.rooms.RoomApplicationResult
import kr.hanchae.moyeotrip.data.rooms.recruitmentDDayText
import kr.hanchae.moyeotrip.data.rooms.roomClockText
import kr.hanchae.moyeotrip.data.rooms.roomDateTimeClockText
import kr.hanchae.moyeotrip.domain.ApplicationNotePolicy
import kr.hanchae.moyeotrip.domain.recruitmentSummary
import kr.hanchae.moyeotrip.ui.LocalServerData
import kr.hanchae.moyeotrip.ui.components.CachedRemoteImage
import kr.hanchae.moyeotrip.ui.components.MoyeoLinearProgress
import kr.hanchae.moyeotrip.ui.theme.MoyeoTheme

@Composable
fun TripDetailScreen(
    tripId: String,
    onBack: () -> Unit,
    onOpenChatRoom: (String) -> Unit,
    showApplicationSheetInitially: Boolean = false
) {
    // "room-{id}" 는 실서버 모집이다 — 탐색(서버 목록)에서만 이 형태로 진입한다
    val server = LocalServerData.current
    val serverRoomId = tripId.removePrefix("room-").toLongOrNull()?.takeIf { tripId.startsWith("room-") }
    if (serverRoomId != null && server != null) {
        ServerTripDetail(roomId = serverRoomId, server = server, onBack = onBack)
        return
    }
    val trip = MockTripRepository.findTrip(tripId)
    val course = MockTripRepository.findCourseForTrip(trip)
    var showApplySheet by rememberSaveable(tripId) { mutableStateOf(showApplicationSheetInitially) }
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
                    actionMessage = "모임 탭의 신청중에서 승인 상태를 확인할 수 있어요."
                } else if (trip.statusLabel == "모집취소") {
                    actionMessage = "모집이 취소되어 신청할 수 없어요."
                } else {
                    showApplySheet = true
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (showApplySheet) {
            val isDark = MoyeoTheme.isDark
            ApplicationSheet(
                heroContent = {
                    Image(
                        painter = painterResource(id = course.tripHeroImageResId(isDark)),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                },
                onDismiss = { showApplySheet = false },
                onApplicationSubmit = {
                    if (!MockTripRepository.isAppliedToTrip(trip.id)) {
                        MockTripRepository.applyToTrip(trip.id)
                    }
                    true
                },
                onDone = {
                    showApplySheet = false
                    actionMessage = "신청을 보냈어요. 호스트 승인 후 채팅방이 열려요."
                }
            )
        }
    }
}

/**
 * 실서버 모집 상세 (GET chat-rooms/{id} + join-eligibility + travel-courses/chat-rooms/{id}).
 * 서버가 내려주지 않는 값(호스트 닉네임·최소 인원·매너 점수)은 표시하지 않는다.
 */
@Composable
private fun ServerTripDetail(roomId: Long, server: ServerDataDependencies, onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    var room by remember(roomId) { mutableStateOf<ChatRoomDetail?>(null) }
    var course by remember(roomId) { mutableStateOf<TravelCourse?>(null) }
    var canApply by remember(roomId) { mutableStateOf<Boolean?>(null) }
    var loadFailed by remember(roomId) { mutableStateOf(false) }
    var isFavorite by remember(roomId) { mutableStateOf(false) }
    var isApplied by rememberSaveable(roomId) { mutableStateOf(false) }
    var actionMessage by rememberSaveable(roomId) { mutableStateOf<String?>(null) }
    var showApplySheet by rememberSaveable(roomId) { mutableStateOf(false) }
    val actionScope = rememberCoroutineScope()

    LaunchedEffect(roomId, server) {
        val loaded = runCatching { server.chatRooms.room(roomId) }.getOrNull()
        room = loaded
        loadFailed = loaded == null
        isFavorite = loaded?.favorite == true
        if (loaded != null) {
            canApply = runCatching { server.chatRooms.canApply(roomId) }.getOrNull()
            course = runCatching { server.courses.roomCourse(roomId) }.getOrNull()
        }
    }

    val detail = room
    if (detail == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로",
                        tint = colors.onSurface
                    )
                }
                Text(
                    text = "모집 상세",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            if (loadFailed) {
                Text(
                    text = "모집 정보를 불러오지 못했어요.\n네트워크 상태를 확인해 주세요.",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            } else {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .testTag("server-trip-detail-$roomId")
    ) {
        LazyColumn(contentPadding = PaddingValues(bottom = 118.dp)) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(284.dp)
                ) {
                    CachedRemoteImage(
                        url = detail.thumbnail,
                        contentDescription = "${detail.title} 대표 이미지",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(colors.surfaceVariant)
                        )
                    }
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
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
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
                ServerTripDetailPanel(detail = detail, course = course)
            }
        }

        TripDetailBottomBar(
            actionLabel = when {
                isApplied -> "신청 상태 보기"
                detail.status != "RECRUITING" -> "모집 종료"
                canApply == false -> "신청 불가"
                detail.participantCount >= detail.maxParticipants -> "대기 신청"
                else -> "함께 가기 신청"
            },
            isFavorite = isFavorite,
            onToggleFavorite = {
                actionScope.launch {
                    runCatching { server.chatRooms.toggleFavorite(roomId) }
                        .onSuccess { favorite ->
                            isFavorite = favorite
                            actionMessage = if (favorite) "찜한 모임에 담았어요." else "찜한 모임에서 제외했어요."
                        }
                        .onFailure { actionMessage = "찜 처리에 실패했어요. 잠시 후 다시 시도해 주세요." }
                }
            },
            onApply = {
                when {
                    isApplied -> actionMessage = "모임 탭의 신청중에서 승인 상태를 확인할 수 있어요."
                    detail.status != "RECRUITING" -> actionMessage = "모집이 종료되어 신청할 수 없어요."
                    canApply == false -> actionMessage = "지금은 이 모임에 신청할 수 없어요."
                    else -> showApplySheet = true
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (showApplySheet) {
            ApplicationSheet(
                heroContent = {
                    CachedRemoteImage(
                        url = detail.thumbnail,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(colors.surfaceVariant)
                        )
                    }
                },
                onDismiss = { showApplySheet = false },
                onApplicationSubmit = { message ->
                    runCatching { server.chatRooms.apply(roomId, message) }.fold(
                        onSuccess = { result ->
                            isApplied = true
                            actionMessage = when (result) {
                                RoomApplicationResult.JOINED -> "참여가 확정됐어요. 모임 탭에서 확인할 수 있어요."
                                RoomApplicationResult.WAITLISTED -> "대기열에 등록됐어요. 자리가 나면 알려드릴게요."
                                RoomApplicationResult.PENDING_APPROVAL -> "신청을 보냈어요. 호스트 승인 후 채팅방이 열려요."
                            }
                            true
                        },
                        onFailure = { error ->
                            showApplySheet = false
                            actionMessage = error.message ?: "신청에 실패했어요. 잠시 후 다시 시도해 주세요."
                            false
                        }
                    )
                },
                onDone = { showApplySheet = false }
            )
        }
    }
}

@Composable
private fun ServerTripDetailPanel(detail: ChatRoomDetail, course: TravelCourse?) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
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
                    text = detail.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
                course?.title?.let { courseTitle ->
                    Text(
                        text = courseTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            if (detail.participantImageUrls.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                    detail.participantImageUrls.take(5).forEach { imageUrl ->
                        UserAvatar(
                            imageUrl = imageUrl,
                            nickname = null,
                            modifier = Modifier.size(32.dp),
                            fallbackFontSize = 15.sp
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${detail.participantCount}/${detail.maxParticipants}명",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                    StatusChip(
                        text = when {
                            detail.status != "RECRUITING" -> "모집 종료"
                            detail.participantCount < detail.maxParticipants -> "신청 가능"
                            else -> "대기 가능"
                        },
                        container = colors.primaryContainer,
                        content = colors.primary
                    )
                }
                MoyeoLinearProgress(
                    progress = if (detail.maxParticipants == 0) {
                        0f
                    } else {
                        detail.participantCount.toFloat() / detail.maxParticipants
                    },
                    modifier = Modifier.fillMaxWidth()
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
                DetailInfoRow(label = "일정", value = detail.scheduleText())
                detail.travelHoursText()?.let { DetailInfoRow(label = "여행 시간", value = it) }
                detail.meetingText()?.let { DetailInfoRow(label = "집합", value = it) }
                if (detail.meetingLatitude != null && detail.meetingLongitude != null) {
                    HorizontalDivider(color = colors.outline.copy(alpha = .5f))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Map,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = colors.onSurfaceVariant
                        )
                        Text(
                            text = "${detail.meetingLatitude}, ${detail.meetingLongitude}",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceVariant)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    detail.participationFee?.let { fee ->
                        DetailInfoRow("예상 비용", "1인 ${"%,d".format(fee)}원", Modifier.weight(1f))
                    }
                    detail.deadlineText()?.let { deadline ->
                        DetailInfoRow("모집 마감", deadline, Modifier.weight(1f))
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (detail.minimumAge != null && detail.maximumAge != null) {
                        DetailInfoRow("나이대", "${detail.minimumAge}~${detail.maximumAge}세", Modifier.weight(1f))
                    }
                    detail.genderText()?.let { gender ->
                        DetailInfoRow("성별", gender, Modifier.weight(1f))
                    }
                }
            }

            detail.hostProfileImageUrl?.let { hostImage ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    UserAvatar(
                        imageUrl = hostImage,
                        nickname = null,
                        modifier = Modifier.size(44.dp),
                        fallbackFontSize = 20.sp
                    )
                    Text(
                        text = "호스트",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            detail.description?.let { description ->
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "모임 소개",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            val stops = course?.places?.map { it.title }.orEmpty()
            if (stops.isNotEmpty()) {
                RoutePreview(stops = stops)
            }
        }
    }
}

private fun ChatRoomDetail.scheduleText(): String {
    val period = if (endDate != null) "$startDate ~ $endDate" else startDate
    val typeLabel = when {
        tripType == "DAY_TRIP" -> "당일치기"
        tripNights > 0 -> "${tripNights}박 ${tripDays}일"
        else -> null
    }
    return listOfNotNull(period.takeIf(String::isNotBlank), typeLabel).joinToString(" · ")
}

/** 서버가 `HH:mm:ss` 를 주고 문서는 `HH:mm` 이다 — 정규화는 검색 카드와 같은 함수를 쓴다. */
private fun ChatRoomDetail.travelHoursText(): String? {
    val start = roomClockText(dayTripStartTime) ?: return null
    val end = roomClockText(dayTripEndTime) ?: return null
    return "$start - $end"
}

private fun ChatRoomDetail.meetingText(): String? = listOfNotNull(
    roomDateTimeClockText(meetingDateTime),
    meetingDetails?.takeIf(String::isNotBlank)
).joinToString(" · ").takeIf(String::isNotBlank)

/** 마감이 지난 방은 서버가 음수 D-day 를 주는데 화면기획에 "D--95" 같은 표기가 없어 날짜만 남긴다. */
private fun ChatRoomDetail.deadlineText(): String? =
    listOfNotNull(recruitmentDDayText(recruitmentDDay), recruitmentDeadlineDate)
        .joinToString(" · ")
        .takeIf(String::isNotBlank)

private fun ChatRoomDetail.genderText(): String? = when (genderRestriction) {
    null -> null
    "NONE" -> "제한 없음"
    "MALE" -> "남성만"
    "FEMALE" -> "여성만"
    else -> genderRestriction
}

@Composable
private fun GroupDetailHero(course: TripCourse, trip: TripRecruitment, onBack: () -> Unit, onOpenChatRoom: () -> Unit) {
    val isDark = MoyeoTheme.isDark

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
        // 화면기획 15 히어로에는 상태 배지가 없다 — 상태는 아래 본문에서 보여준다
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
                    text = trip.recruitmentName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = course.title,
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
                StatusChip(
                    text = trip.courseSource.label,
                    container = colors.primaryContainer,
                    content = colors.primary
                )
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
                MoyeoLinearProgress(
                    progress = recruitment.progress,
                    modifier = Modifier.fillMaxWidth()
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
                DetailInfoRow(label = "일정", value = "${trip.scheduleDate} · ${trip.scheduleType.label}")
                DetailInfoRow(label = "여행 시간", value = trip.scheduleTime)
                DetailInfoRow(
                    label = "집합",
                    value =
                        "${trip.meetingLocation.meetingTime} · ${trip.meetingLocation.name} " +
                            trip.meetingLocation.detail
                )
                // 좌표는 라벨 행이 아니라 구분선 아래 작은 보조 정보다 (웹·화면기획 15)
                HorizontalDivider(color = colors.outline.copy(alpha = .5f))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Map,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = colors.onSurfaceVariant
                    )
                    Text(
                        text = "${trip.meetingLocation.latitude}, ${trip.meetingLocation.longitude}",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant
                    )
                    Text(
                        text = "길 찾기",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // 조건 4종은 한 카드 안에서 2x2 — 4줄로 세우면 카드만 길어진다 (웹 기준)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceVariant)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailInfoRow(
                        "예상 비용",
                        "1인 ${"%,d".format(trip.estimatedCostPerPerson)}원",
                        Modifier.weight(1f)
                    )
                    DetailInfoRow(
                        "모집 마감",
                        trip.deadlineDisplayText(),
                        Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailInfoRow("나이대", "${trip.minimumAge}~${trip.maximumAge}세", Modifier.weight(1f))
                    DetailInfoRow(
                        "성별",
                        if (trip.genderCondition == "성별 무관") "제한 없음" else trip.genderCondition,
                        Modifier.weight(1f)
                    )
                }
            }

            if (trip.courseSource == kr.hanchae.moyeotrip.data.CourseSource.Custom) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.primaryContainer
                ) {
                    Text(
                        "호스트가 직접 만든 코스예요. 여행 확정 전에는 경로가 바뀔 수 있으며, 변경 내용은 채팅방에 안내돼요.",
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onPrimaryContainer
                    )
                }
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

            RoutePreview(stops = trip.routeStops.map { it.name }.ifEmpty { course.stops })
        }
    }
}

@Composable
private fun DetailInfoRow(label: String, value: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme

    // 2x2 배치에서는 라벨 위 / 값 아래로 읽어야 좁은 폭에서 값이 잘리지 않는다
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = colors.onSurface,
            fontWeight = FontWeight.Bold
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
    heroContent: @Composable () -> Unit,
    onDismiss: () -> Unit,
    /** true 를 돌려주면 완료 카드로 전환한다 — 서버 신청 실패 시 false 로 시트를 유지한다. */
    onApplicationSubmit: suspend (String) -> Boolean,
    onDone: () -> Unit
) {
    // 화면기획 16 — 진입 시 입력은 비어 있고 카운터는 0/200이다
    var message by rememberSaveable { mutableStateOf("") }
    var isSubmitted by rememberSaveable { mutableStateOf(false) }
    var submitAttempted by rememberSaveable { mutableStateOf(false) }
    var isSubmitting by rememberSaveable { mutableStateOf(false) }
    val isMessageValid = ApplicationNotePolicy.isValid(message)
    val colors = MaterialTheme.colorScheme
    val submitScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("application-sheet")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(330.dp)
                .align(Alignment.TopCenter)
        ) {
            heroContent()
        }
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
            // 화면기획 16 — 시트 표면은 bgRaised(다크 #18231E)다 (changeLog15)
            color = MoyeoTheme.sheetSurface
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
                        onClick = onDone,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("application-done"),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        shape = RoundedCornerShape(9.dp)
                    ) {
                        Text(
                            text = "신청 상태 확인하기",
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
                        // 화면기획 16 — 자수 안내는 플레이스홀더에, 카운터는 박스 안 우하단에
                        Box {
                            OutlinedTextField(
                                value = message,
                                onValueChange = { message = ApplicationNotePolicy.sanitize(it) },
                                placeholder = {
                                    Text("간단한 인사나 기대하는 마음을\n남겨주세요 😊 (10자 이상 200자 이하)")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 4,
                                isError = submitAttempted && !isMessageValid
                            )
                            Text(
                                text = ApplicationNotePolicy.counterText(message),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 14.dp, bottom = 10.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant
                            )
                        }
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
                                // 시트(bgRaised) 위 카드라 다크에서는 한 단 어두운 surface가 기획과 맞다
                                .background(if (MoyeoTheme.isDark) colors.surface else colors.surfaceVariant)
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
                                    text = "자연 속에서 힐링하는 걸 좋아해요!\n사진 찍는 것도 좋아합니다",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // 화면기획 16 — 버튼은 텍스트만, 진입 상태(빈 입력)에서도 초록 활성으로 보인다
                    Button(
                        onClick = {
                            submitAttempted = true
                            if (isMessageValid && !isSubmitting) {
                                isSubmitting = true
                                submitScope.launch {
                                    val accepted = onApplicationSubmit(message)
                                    isSubmitting = false
                                    if (accepted) isSubmitted = true
                                }
                            }
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        shape = RoundedCornerShape(9.dp)
                    ) {
                        Text(
                            text = if (isSubmitting) "신청 중…" else "신청하기",
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
                    text = "신청을 보냈어요",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onPrimaryContainer,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "호스트가 승인하면 채팅방이 열려요. 모임 탭에서 상태를 확인할 수 있어요.",
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

/** 화면기획 15의 마감 표기: "D-3 · 5/22(금)". 전체 일시는 데이터로만 둔다. */
private fun TripRecruitment.deadlineDisplayText(): String {
    val date = Regex("""\d{4}\.(\d{2})\.(\d{2}) \(([^)]+)\)""").find(recruitmentDeadline)
    val dday = ddayLabel.removePrefix("마감 ")
    return if (date != null) {
        val (month, day, weekday) = date.destructured
        "$dday · ${month.toInt()}/${day.toInt()}($weekday)"
    } else {
        recruitmentDeadline.ifBlank { dday }
    }
}

private fun TripRecruitment.detailStatusText(): String = when {
    statusLabel == "모집취소" -> "모집취소"
    joined >= capacity -> "마감"
    joined >= minParticipants -> statusLabel
    else -> "모집중"
}

private fun TripRecruitment.applyActionLabel(isApplied: Boolean): String = when {
    isApplied -> "신청 상태 보기"
    statusLabel == "모집취소" -> "모집 종료"
    joined >= capacity -> "대기 신청"
    else -> "함께 가기 신청"
}

private fun TripRecruitment.participantAvatars(): List<String> {
    val pool = listOf(hostAvatar, "🐻", "🦌", "🐢", "🪽", "🐰")
    return pool.take(joined.coerceAtLeast(1))
}
