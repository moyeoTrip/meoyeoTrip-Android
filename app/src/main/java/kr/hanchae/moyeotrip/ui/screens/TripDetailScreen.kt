package kr.hanchae.moyeotrip.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kr.hanchae.moyeotrip.data.ServerDataDependencies
import kr.hanchae.moyeotrip.data.courses.TravelCourse
import kr.hanchae.moyeotrip.data.courses.TravelCoursePlace
import kr.hanchae.moyeotrip.data.profile.ServerUserProfile
import kr.hanchae.moyeotrip.data.rooms.ChatRoomDetail
import kr.hanchae.moyeotrip.data.rooms.RoomApplicationResult
import kr.hanchae.moyeotrip.data.rooms.recruitmentDDayText
import kr.hanchae.moyeotrip.data.rooms.roomClockText
import kr.hanchae.moyeotrip.data.rooms.roomDateTimeClockText
import kr.hanchae.moyeotrip.domain.ApplicationNotePolicy
import kr.hanchae.moyeotrip.ui.LocalServerData
import kr.hanchae.moyeotrip.ui.components.CachedRemoteImage
import kr.hanchae.moyeotrip.ui.components.CourseRouteMap
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyState
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyText
import kr.hanchae.moyeotrip.ui.components.MoyeoLinearProgress
import kr.hanchae.moyeotrip.ui.components.MoyeoPlaceholderShape
import kr.hanchae.moyeotrip.ui.theme.MoyeoTheme

/**
 * 화면기획 15 모집 상세 — 서버 모집(GET chat-rooms/{id})만 그린다.
 *
 * 라우트가 들고 오는 식별자는 `room-{roomId}` 다. 그 형태가 아니거나 미로그인이면 빈 상태다.
 */
@Composable
fun TripDetailScreen(
    tripId: String,
    onBack: () -> Unit,
    onOpenChatRoom: (String) -> Unit,
    showApplicationSheetInitially: Boolean = false
) {
    val server = LocalServerData.current
    val serverRoomId = tripId.removePrefix("room-").toLongOrNull()?.takeIf { tripId.startsWith("room-") }
    if (serverRoomId == null || server == null) {
        TripDetailShell(onBack = onBack) {
            MoyeoEmptyState(MoyeoEmptyText.SIGN_IN_EXPLORE, testTag = "trip-detail-signed-out")
        }
        return
    }
    ServerTripDetail(
        roomId = serverRoomId,
        server = server,
        onBack = onBack,
        onOpenChatRoom = onOpenChatRoom,
        showApplicationSheetInitially = showApplicationSheetInitially
    )
}

/** 헤더만 있는 껍데기 — 빈 상태·로딩·오류에서 화면 구조를 유지한다. */
@Composable
private fun TripDetailShell(onBack: () -> Unit, content: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
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
        Box(modifier = Modifier.align(Alignment.Center)) { content() }
    }
}

/**
 * 실서버 모집 상세 (GET chat-rooms/{id} (canApply 포함) + travel-courses/chat-rooms/{id}).
 * 서버가 내려주지 않는 값(호스트 닉네임·최소 인원·매너 점수)은 표시하지 않는다.
 */
@Composable
private fun ServerTripDetail(
    roomId: Long,
    server: ServerDataDependencies,
    onBack: () -> Unit,
    onOpenChatRoom: (String) -> Unit,
    showApplicationSheetInitially: Boolean
) {
    val colors = MaterialTheme.colorScheme
    var room by remember(roomId) { mutableStateOf<ChatRoomDetail?>(null) }
    var course by remember(roomId) { mutableStateOf<TravelCourse?>(null) }
    var canApply by remember(roomId) { mutableStateOf<Boolean?>(null) }
    var loadFailed by remember(roomId) { mutableStateOf(false) }
    var isFavorite by remember(roomId) { mutableStateOf(false) }
    var isApplied by rememberSaveable(roomId) { mutableStateOf(false) }
    var actionMessage by rememberSaveable(roomId) { mutableStateOf<String?>(null) }
    var showApplySheet by rememberSaveable(roomId) { mutableStateOf(showApplicationSheetInitially) }
    // 신청 시트의 "내 소개 카드"는 내 서버 프로필이다 — 예시 프로필을 그리지 않는다.
    var myProfile by remember(server) { mutableStateOf<ServerUserProfile?>(null) }
    val actionScope = rememberCoroutineScope()

    LaunchedEffect(roomId, server) {
        val loaded = runCatching { server.chatRooms.room(roomId) }.getOrNull()
        room = loaded
        loadFailed = loaded == null
        isFavorite = loaded?.favorite == true
        if (loaded != null) {
            // 참가 가능 여부는 상세 응답의 canApply 다. 예전 /join-eligibility 는 서버에서 삭제됐다.
            canApply = loaded.canApply
            course = runCatching { server.courses.roomCourse(roomId) }.getOrNull()
        }
        myProfile = runCatching { server.userProfile.profile() }.getOrNull()
    }

    val detail = room
    if (detail == null) {
        TripDetailShell(onBack = onBack) {
            if (loadFailed) {
                MoyeoEmptyState(MoyeoEmptyText.FAILED)
            } else {
                MoyeoEmptyState(MoyeoEmptyText.LOADING)
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
                        contentScale = ContentScale.Crop,
                        fallbackShape = MoyeoPlaceholderShape.LANDSCAPE
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
                        IconButton(onClick = { onOpenChatRoom("room-$roomId") }) {
                            Icon(
                                imageVector = Icons.Filled.ChatBubble,
                                contentDescription = "채팅",
                                tint = Color.White
                            )
                        }
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
                        contentScale = ContentScale.Crop,
                        fallbackShape = MoyeoPlaceholderShape.LANDSCAPE
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(colors.surfaceVariant)
                        )
                    }
                },
                nickname = myProfile?.nickname,
                introduction = myProfile?.introduction,
                profileImageUrl = myProfile?.profileImageUrl,
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
                        // 좌표는 길 찾기 링크의 입력값이지 사용자에게 보일 값이 아니다 —
                        // 기획에도 없고, 웹·iOS 는 이 자리에 집합 장소 이름을 둔다.
                        Text(
                            text = detail.meetingDetails?.takeIf(String::isNotBlank) ?: "집합 위치",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        // 15 상세의 `길 찾기` — 웹·iOS 에는 있는데 안드로이드만 없었다.
                        // 좌표를 지도 앱으로 넘긴다. 서버 호출은 없다.
                        val uriHandler = LocalUriHandler.current
                        TextButton(
                            onClick = {
                                uriHandler.openUri(
                                    kakaoDirectionsUrl(
                                        detail.meetingLatitude,
                                        detail.meetingLongitude,
                                        detail.meetingDetails
                                    )
                                )
                            },
                            modifier = Modifier.testTag("trip-detail-directions")
                        ) {
                            Text(
                                "길 찾기",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
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

            val places = course?.places.orEmpty()
            if (places.isNotEmpty()) {
                RoutePreview(places = places)
            }
        }
    }
}

internal fun ChatRoomDetail.scheduleText(): String {
    val period = if (endDate != null) "$startDate ~ $endDate" else startDate
    val typeLabel = when {
        tripType == "DAY_TRIP" -> "당일치기"
        tripNights > 0 -> "${tripNights}박 ${tripDays}일"
        else -> null
    }
    return listOfNotNull(period.takeIf(String::isNotBlank), typeLabel).joinToString(" · ")
}

/** 서버가 `HH:mm:ss` 를 주고 문서는 `HH:mm` 이다 — 정규화는 검색 카드와 같은 함수를 쓴다. */
internal fun ChatRoomDetail.travelHoursText(): String? {
    val start = roomClockText(dayTripStartTime) ?: return null
    val end = roomClockText(dayTripEndTime) ?: return null
    return "$start - $end"
}

internal fun ChatRoomDetail.meetingText(): String? = listOfNotNull(
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

/**
 * 코스 미리보기 (화면기획 15).
 *
 * 예전에는 번호 원을 가로로 늘어놓아 지도처럼 보이게 했다 — 좌표가 있는데도 버리고 있었다.
 * 이제 방문지 좌표를 그대로 실지도에 올린다. 좌표가 없으면 이름만 나열한다.
 */
@Composable
private fun RoutePreview(places: List<TravelCoursePlace>) {
    val colors = MaterialTheme.colorScheme
    val points = remember(places) { places.toRoutePoints() }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "코스 미리보기",
            style = MaterialTheme.typography.titleMedium,
            color = colors.onSurface,
            fontWeight = FontWeight.Bold
        )
        CourseRouteMap(points = points)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            places.forEachIndexed { index, place ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(colors.primary),
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
                        text = place.title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    place.visitTime?.take(5)?.let { time ->
                        Text(
                            text = time,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant
                        )
                    }
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
    nickname: String?,
    introduction: String?,
    profileImageUrl: String?,
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
                                // 입력 중에는 길이만 자른다. trim 을 걸면 방금 친 공백·줄바꿈이
                                // 그 자리에서 지워져 스페이스와 엔터가 안 먹는 것처럼 보인다.
                                onValueChange = { message = ApplicationNotePolicy.clampWhileTyping(it) },
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

                    // 내 소개 카드는 서버 프로필이 있을 때만 그린다 — 예시 프로필을 대신 세우지 않는다
                    if (nickname != null) {
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
                                UserAvatar(
                                    imageUrl = profileImageUrl,
                                    nickname = nickname,
                                    modifier = Modifier.size(52.dp),
                                    fallbackFontSize = 24.sp
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = nickname,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = colors.onSurface,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    introduction?.takeIf(String::isNotBlank)?.let { intro ->
                                        Text(
                                            text = intro,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.onSurfaceVariant
                                        )
                                    }
                                }
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
                                    // 다듬기는 여기서 한다 — 입력 중이 아니라 보낼 때.
                                    val accepted = onApplicationSubmit(ApplicationNotePolicy.sanitize(message))
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

/**
 * 15 모집 상세 `길 찾기` 링크. 웹 `kakao-map.js` 의 `directionsUrl` 과 **같은 규칙**이다 —
 * `link/to/{이름},{위도},{경도}`. 이름에 쉼표가 있으면 경로가 깨지므로 지운다.
 * 카카오 지도 앱이 깔려 있으면 앱이, 없으면 웹 지도가 받는다.
 */
private fun kakaoDirectionsUrl(latitude: Double?, longitude: Double?, name: String?): String {
    val label = (name?.takeIf(String::isNotBlank) ?: "집합 장소").replace(",", " ").trim()
    val encoded = java.net.URLEncoder.encode(label, "UTF-8").replace("+", "%20")
    return "https://map.kakao.com/link/to/$encoded,$latitude,$longitude"
}
