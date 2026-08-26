package kr.hanchae.moyeotrip.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import kr.hanchae.moyeotrip.data.FeedPost
import kr.hanchae.moyeotrip.data.FeedVisibility
import kr.hanchae.moyeotrip.data.MockTripRepository
import kr.hanchae.moyeotrip.data.TripCourse
import kr.hanchae.moyeotrip.data.TripRecruitment
import kr.hanchae.moyeotrip.data.notifications.ServerNotification
import kr.hanchae.moyeotrip.data.rooms.RoomKickHistory
import kr.hanchae.moyeotrip.data.search.InMemoryRecentSearchStore
import kr.hanchae.moyeotrip.data.search.PLANNING_RECENT_SEARCHES
import kr.hanchae.moyeotrip.data.search.PersistedRecentSearchStore
import kr.hanchae.moyeotrip.ui.LocalCaptureMode
import kr.hanchae.moyeotrip.ui.LocalServerData
import kr.hanchae.moyeotrip.ui.components.AnimalAvatar
import kr.hanchae.moyeotrip.ui.components.emphasized
import kr.hanchae.moyeotrip.ui.theme.Coral
import kr.hanchae.moyeotrip.ui.theme.ForestGreen
import kr.hanchae.moyeotrip.ui.theme.MoyeoTheme

@Composable
fun NotificationCenterScreen(
    onBack: () -> Unit,
    onOpenTrip: (String) -> Unit,
    onOpenPost: (String) -> Unit,
    onOpenCourse: (String) -> Unit,
    onOpenTripConfirmed: () -> Unit = {},
    onOpenTripMessage: () -> Unit = {},
    onOpenRemovalReason: (Long?) -> Unit = {}
) {
    // 로그인 상태면 실서버 알림(GET notifications)으로 대체한다 — 실패 시 목데이터 유지
    val server = LocalServerData.current
    var serverItems by remember(server) { mutableStateOf<List<NotificationItem>?>(null) }
    var serverUnreadCount by remember(server) { mutableStateOf(0) }
    val serverScope = rememberCoroutineScope()
    LaunchedEffect(server) {
        if (server == null) {
            serverItems = null
            return@LaunchedEffect
        }
        runCatching { server.notifications.notifications() }
            .onSuccess { page ->
                serverItems = page.notifications.map { it.toNotificationItem() }
                serverUnreadCount = page.unreadCount
            }
            .onFailure { serverItems = null }
    }
    // 항목 구성은 화면기획 13 알림과 동일하다 (오늘 5 · 어제 2, 안읽음 4)
    val notifications = listOf(
        NotificationItem(
            "주왕산 & 주산지 여행이 확정됐어요 🎉",
            "",
            "방금 전",
            "confirmed",
            "trip-cheongsong-juwangsan",
            unread = true,
            emphasis = listOf("주왕산 & 주산지")
        ),
        NotificationItem(
            "경주 단풍·야경 모임이 만들어졌어요 ✨",
            "",
            "10분 전",
            "trip",
            "trip-gyeongju-night",
            unread = true,
            emphasis = listOf("경주 단풍·야경")
        ),
        NotificationItem(
            "우직한 곰 7821님이 메시지를 보냈어요",
            "",
            "1시간 전",
            "feed",
            "feed-1",
            unread = true,
            emphasis = listOf("메시지")
        ),
        NotificationItem(
            "여행 잘 마치셨죠? 함께 걸은 친구에게 한 줄 남겨볼까요",
            "",
            "2시간 전",
            "message",
            "trip-gyeongju-night",
            unread = true,
            emphasis = listOf("한 줄")
        ),
        // 마감 임박 알림은 시계 아이콘 + 주의 노랑이다 (화면기획 13)
        NotificationItem(
            "마감 D-1 · 현재 4/8명이에요",
            "",
            "3시간 전",
            "deadline",
            "trip-gyeongju-night",
            emphasis = listOf("D-1")
        ),
        NotificationItem(
            "엉뚱한 토끼 1457님이 친구 요청을 보냈어요",
            "",
            "어제 오후 4시",
            "friend-request",
            "friend-rabbit",
            group = "어제",
            emphasis = listOf("친구 요청")
        ),
        // changeLog14 — 강퇴 통보는 알림 센터의 한 행으로 도착하고, 탭하면 사유(13-1)로 간다
        NotificationItem(
            "감포 바다 일출 모임에서 내보내졌어요 · 사유 확인",
            "",
            "어제 오후 6시",
            "removal",
            "trip-gampo-sunrise",
            group = "어제",
            emphasis = listOf("감포 바다 일출 모임")
        ),
        NotificationItem(
            "3명이 내 피드에 좋아요를 눌렀어요",
            "",
            "어제 오전 11시",
            "likes",
            "feed-1",
            group = "어제",
            emphasis = listOf("3명")
        )
    )

    var showsUnreadOnly by rememberSaveable { mutableStateOf(false) }
    var readAll by rememberSaveable { mutableStateOf(false) }
    val isServerList = serverItems != null
    val activeItems = serverItems ?: notifications
    val unreadCount = if (isServerList) serverUnreadCount else notifications.count { it.unread && !readAll }
    val visible = if (showsUnreadOnly) {
        activeItems.filter { it.unread && (isServerList || !readAll) }
    } else {
        activeItems
    }
    val grouped = visible.groupBy { it.group }
    val groupOrder = visible.map { it.group }.distinct()

    fun markServerItemRead(item: NotificationItem) {
        val serverId = item.serverId ?: return
        if (!item.unread) return
        serverScope.launch {
            runCatching { server?.notifications?.markRead(serverId) }
        }
        serverItems = serverItems?.map { if (it.serverId == serverId) it.copy(unread = false) else it }
        serverUnreadCount = (serverUnreadCount - 1).coerceAtLeast(0)
    }

    // 알림은 항목마다 카드를 두지 않고 테이블처럼 한 줄씩 수직으로 쌓는다 (화면기획 기준).
    // 카드가 겹치면 목록을 훑을 때 어디까지 읽었는지 잡히지 않는다.
    SupportScaffold(
        title = "알림",
        onBack = onBack,
        itemSpacing = 0.dp,
        trailingTitle = "모두 읽음",
        onTrailingClick = {
            if (isServerList) {
                serverScope.launch {
                    runCatching { server?.notifications?.markAllRead() }
                        .onSuccess {
                            serverItems = serverItems?.map { it.copy(unread = false) }
                            serverUnreadCount = 0
                        }
                }
            } else {
                readAll = true
            }
        }
    ) {
        if (isServerList && activeItems.isEmpty()) {
            item {
                Text(
                    text = "아직 도착한 알림이 없어요.",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        // 화면기획·웹과 같은 전체 / 안읽음 필터
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NotificationFilterChip("전체", !showsUnreadOnly) { showsUnreadOnly = false }
                NotificationFilterChip("안읽음 $unreadCount", showsUnreadOnly) { showsUnreadOnly = true }
            }
        }
        groupOrder.forEach { group ->
            item(key = "group-$group") {
                Text(
                    text = group,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 2.dp)
                        .testTag("notification-group-$group"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(grouped[group].orEmpty()) { item ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (item.serverId != null) {
                                    markServerItemRead(item)
                                    when (item.type) {
                                        "removal" -> onOpenRemovalReason(item.serverId)

                                        "likes" ->
                                            if (item.targetId.isNotBlank()) onOpenPost("srv-${item.targetId}")

                                        "friend-request" -> Unit

                                        else -> item.chatRoomId?.let { onOpenTrip("room-$it") }
                                    }
                                } else {
                                    when (item.type) {
                                        "feed", "likes" -> onOpenPost(item.targetId)
                                        "course" -> onOpenCourse(item.targetId)
                                        "confirmed" -> onOpenTripConfirmed()
                                        "message" -> onOpenTripMessage()
                                        "removal" -> onOpenRemovalReason(null)
                                        "friend-request" -> Unit
                                        else -> onOpenTrip(item.targetId)
                                    }
                                }
                            }
                            .testTag("notification-${item.type}-${item.targetId}")
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 알림 종류마다 원 배경과 아이콘 색이 다르다 (화면기획 13).
                        // 전부 초록이면 확정·메시지·마감·강퇴가 목록에서 구분되지 않는다.
                        val tone = notificationTone(item.type)
                        IconBubble(color = tone.container) {
                            Icon(
                                imageVector = when (item.type) {
                                    "confirmed" -> Icons.Filled.Celebration

                                    // 서버 알림도 같은 종류면 같은 아이콘이다 —
                                    // CHAT_MESSAGE_RECEIVED / TRAVEL_COURSE_UPDATED 가 목데이터의
                                    // 메시지·코스 행과 어긋나면 목록에서 종류를 구분할 수 없다.
                                    "feed", "chat-server" -> Icons.Filled.ChatBubbleOutline

                                    "course", "course-server" -> Icons.Filled.WbSunny

                                    "message" -> Icons.Filled.Description

                                    "friend-request" -> Icons.Filled.PersonAdd

                                    "removal" -> Icons.Filled.WarningAmber

                                    "likes" -> Icons.Filled.FavoriteBorder

                                    "deadline" -> Icons.Filled.Schedule

                                    else -> Icons.Filled.Groups
                                },
                                contentDescription = null,
                                tint = tone.content,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(
                                text = emphasized(item.title, *item.emphasis.toTypedArray()),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = item.time,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            // 친구 요청은 목록 안에서 바로 처리한다 (화면기획)
                            if (item.type == "friend-request") {
                                fun answerServerRequest(accept: Boolean) {
                                    val requestId = item.targetId.toLongOrNull()
                                    if (item.serverId == null || requestId == null) return
                                    serverScope.launch {
                                        runCatching {
                                            if (accept) {
                                                server?.social?.acceptRequest(requestId)
                                            } else {
                                                server?.social?.rejectRequest(requestId)
                                            }
                                        }.onSuccess {
                                            markServerItemRead(item)
                                            serverItems = serverItems?.filterNot { it.serverId == item.serverId }
                                        }
                                    }
                                }
                                Row(
                                    modifier = Modifier.padding(top = 5.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { answerServerRequest(accept = false) },
                                        modifier = Modifier.height(32.dp)
                                            .testTag("notification-friend-decline"),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp)
                                    ) { Text("거절", style = MaterialTheme.typography.labelMedium) }
                                    Button(
                                        onClick = { answerServerRequest(accept = true) },
                                        modifier = Modifier.height(32.dp)
                                            .testTag("notification-friend-accept"),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp)
                                    ) { Text("수락", style = MaterialTheme.typography.labelMedium) }
                                }
                            }
                        }
                        if (item.type != "friend-request") {
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

// / 13-1 내보내기 안내 (changeLog14) — 강퇴 알림을 탭했을 때만 진입한다.
// / 채팅방은 이미 사라진 뒤라 사유와 이후 정책을 이 화면 하나로 전달하고, 하단 동작은 확인 하나뿐이다.
@Composable
fun RemovalReasonScreen(onBack: () -> Unit, notificationId: Long? = null) {
    // 강퇴 알림에서 진입하면 실서버 사유(GET notifications/{id}/kick-history)를 보여준다
    val server = LocalServerData.current
    var kickHistory by remember(notificationId) { mutableStateOf<RoomKickHistory?>(null) }
    LaunchedEffect(server, notificationId) {
        if (server != null && notificationId != null) {
            kickHistory = runCatching { server.notifications.kickHistory(notificationId) }.getOrNull()
        }
    }
    val tints = MoyeoTheme.tints
    // iOS 다크 표면 위계를 기준으로 맞춘다: 페이지 < 인용 박스 < 카드(+softLine 테두리).
    // 라이트에서는 페이지·카드가 흰색으로 같아지고 테두리·회색 인용 박스만 남는다 — iOS와 동일.
    val pageColor = if (MoyeoTheme.isDark) {
        MaterialTheme.colorScheme.background
    } else {
        MaterialTheme.colorScheme.surface
    }
    val cardColor = if (MoyeoTheme.isDark) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface
    }
    val quoteColor = if (MoyeoTheme.isDark) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("removal-reason-screen")
    ) {
        SupportScaffold(
            title = "내보내기 안내",
            containerColor = pageColor,
            onBack = onBack,
            bottomBar = {
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("removal-reason-confirm"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "확인",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 26.dp, bottom = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Surface(modifier = Modifier.size(64.dp), shape = CircleShape, color = tints.dangerTint) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.WarningAmber,
                                contentDescription = null,
                                tint = tints.onDangerTint,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                    Text(
                        text = kickHistory?.let { "${it.roomTitle} 모임에서\n내보내졌어요" }
                            ?: "감포 바다 일출 모임에서\n내보내졌어요",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = kickHistory?.kickedAt?.kickedAtLabel()
                            ?: "2026.08.22 (토) 오후 6:02 · 호스트 결정",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
            item {
                // 사유는 호스트가 남긴 서술 하나만 — 정형 카테고리 태그는 두지 않는다 (changeLog14)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    border = BorderStroke(1.dp, tints.softLine),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "호스트가 남긴 사유",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = quoteColor
                        ) {
                            Text(
                                text = kickHistory?.reason?.takeIf(String::isNotBlank)?.let { "“$it”" }
                                    ?: "“모임 컨셉과 맞지 않는 대화가 반복되어, 남은 멤버들을 위해 함께하기 어렵다고 판단했어요.”",
                                modifier = Modifier.padding(14.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            item {
                // 이후 정책 3가지 고지 — 이의 제기·고객센터 경로는 화면기획에 없으므로 두지 않는다
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    border = BorderStroke(1.dp, tints.softLine),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "내보내진 뒤에는",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold
                        )
                        listOf(
                            "이 모임에는 다시 신청할 수 없어요.",
                            "채팅방이 내 목록에서 사라져요. 이미 남긴 대화는 모임 채팅방에 그대로 남아요.",
                            "다른 모임을 찾고 신청하는 데에는 아무 영향이 없어요."
                        ).forEach { line ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = line,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// / 알림 목록 상단의 전체 / 안읽음 필터 칩
@Composable
private fun NotificationFilterChip(title: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.height(32.dp).clip(RoundedCornerShape(50)).clickable(onClick = onClick)
            .testTag("notification-filter-$title"),
        shape = RoundedCornerShape(50),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 13.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CreateRecruitmentScreen(
    courseId: String,
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit,
    onOpenManage: (String) -> Unit
) {
    val course = remember(courseId) { MockTripRepository.findCourse(courseId) }
    var createdTripId by remember { mutableStateOf<String?>(null) }
    var scheduleDate by rememberSaveable(courseId) { mutableStateOf(course.startLabel) }
    var scheduleTime by rememberSaveable(courseId) {
        mutableStateOf(if (course.duration == "2박 3일") "09:00 - 2박 3일" else "08:00 - 18:00")
    }
    var meetingPoint by rememberSaveable(courseId) { mutableStateOf(course.meetingPoint) }
    var capacityText by rememberSaveable(courseId) { mutableStateOf(course.capacity.toString()) }
    var recruitmentNote by rememberSaveable(courseId) { mutableStateOf(course.recruitmentNote) }
    val created = createdTripId != null
    val capacity = capacityText.toIntOrNull()?.coerceIn(course.minParticipants, 12) ?: course.capacity

    SupportScaffold(title = "모집 만들기", onBack = onBack) {
        item {
            CourseSummaryCard(course = course)
        }
        item {
            SupportCard {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "모집 정보",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                    OutlinedTextField(
                        value = scheduleDate,
                        onValueChange = { scheduleDate = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create-recruitment-date"),
                        label = { Text("일정") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = scheduleTime,
                        onValueChange = { scheduleTime = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create-recruitment-time"),
                        label = { Text("시간") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = meetingPoint,
                        onValueChange = { meetingPoint = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create-recruitment-place"),
                        label = { Text("모이는 곳") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = capacityText,
                        onValueChange = { value -> capacityText = value.filter { it.isDigit() }.take(2) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create-recruitment-capacity"),
                        label = { Text("모집 정원") },
                        supportingText = { Text("최소 ${course.minParticipants}명, 최대 12명") },
                        singleLine = true
                    )
                    SupportField(label = "참가비", value = course.price)
                }
            }
        }
        item {
            SupportCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "소개글",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                    OutlinedTextField(
                        value = recruitmentNote,
                        onValueChange = { recruitmentNote = it.take(160) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create-recruitment-note"),
                        minLines = 3,
                        label = { Text("함께 갈 사람들에게 보여줄 안내") },
                        supportingText = { Text("${recruitmentNote.length}/160자") }
                    )
                    SupportField(
                        label = "미리보기",
                        value = "${scheduleDate.trim()} · $meetingPoint · 1/${capacity}명 모집"
                    )
                }
            }
        }
        item {
            if (created) {
                SupportCard {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = ForestGreen)
                            Text(
                                text = "모집이 준비됐어요",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Button(
                            onClick = {
                                val tripId = createdTripId ?: MockTripRepository.tripIdForCourse(course.id)
                                onOpenManage(tripId)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("create-recruitment-open-manage"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("모집 관리")
                        }
                        Button(
                            onClick = {
                                val tripId = createdTripId ?: MockTripRepository.tripIdForCourse(course.id)
                                onOpenChat(MockTripRepository.chatThreadIdForTrip(tripId))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("채팅방 미리보기")
                        }
                    }
                }
            } else {
                Button(
                    onClick = {
                        createdTripId = MockTripRepository.createRecruitment(
                            courseId = course.id,
                            scheduleDate = scheduleDate,
                            scheduleTime = scheduleTime,
                            meetingPoint = meetingPoint,
                            capacity = capacity,
                            note = recruitmentNote
                        ).id
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("create-recruitment-submit"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Filled.GroupAdd, contentDescription = null)
                    Text(
                        text = "모집 만들기",
                        modifier = Modifier.padding(start = 8.dp),
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun HostManageScreen(
    tripId: String,
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit,
    onOpenRoute: (String) -> Unit = {}
) {
    val trip = MockTripRepository.findTrip(tripId)
    var pendingApplicants by remember {
        // 승인 대기 2명은 화면기획 18과 같은 인물·한마디다
        mutableStateOf(
            listOf(
                HostApplicant(
                    id = "applicant-bear",
                    name = "우직한 곰 7821",
                    avatar = "🐻",
                    note = "단풍 보러 가요. 사진 좋아해서 풍경 잘 담아드릴 수 있어요!",
                    meta = "31세 · 남성 · 매너 4.9 · 여행 8회"
                ),
                HostApplicant(
                    id = "applicant-raccoon",
                    name = "호기심 많은 너구리 9027",
                    avatar = "🦝",
                    note = "당일치기로 조용히 걷고 싶어요.",
                    meta = "26세 · 여성 · 매너 4.7"
                )
            )
        )
    }
    var approvedApplicants by remember {
        mutableStateOf(
            listOf(
                HostApplicant("approved-turtle", "잔잔한 거북이 9032", "🐢", "기존 참여자"),
                HostApplicant("approved-rabbit", "엉뚱한 토끼 1457", "🐰", "기존 참여자"),
                HostApplicant("approved-crane", "고요한 두루미 1130", "🪽", "기존 참여자")
            )
        )
    }
    var rejectedApplicants by remember { mutableStateOf(emptyList<HostApplicant>()) }
    var expandedApplicantId by remember { mutableStateOf("applicant-bear") }
    var isRecruitmentClosed by remember(tripId) { mutableStateOf(trip.statusLabel == "모집취소") }

    SupportScaffold(
        title = "모집 관리",
        onBack = onBack,
        bottomBar = {
            Button(
                onClick = { onOpenChat(MockTripRepository.chatThreadIdForTrip(trip.id)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("host-manage-open-chat"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    Icons.Filled.ChatBubbleOutline,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
                )
                Text("채팅방 들어가기", modifier = Modifier.padding(start = 7.dp), fontWeight = FontWeight.ExtraBold)
            }
        }
    ) {
        item {
            HostManageSummaryCard(
                trip = trip,
                approvedCount = approvedApplicants.size + 1,
                pendingCount = pendingApplicants.size,
                isRecruitmentClosed = isRecruitmentClosed
            )
        }
        item {
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { onOpenRoute(trip.id) }.testTag("host-manage-route"),
                shape = RoundedCornerShape(12.dp),
                color = MoyeoTheme.tints.primaryTint
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Route, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.padding(start = 12.dp).weight(1f)) {
                        Text(
                            "여행 경로 · 방문지 ${trip.routeStops.size.takeIf {
                                it > 0
                            } ?: MockTripRepository.findCourseForTrip(trip).stops.size}곳",
                            color = MoyeoTheme.tints.onPrimaryTint,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            if (trip.courseSource ==
                                kr.hanchae.moyeotrip.data.CourseSource.Custom
                            ) {
                                "확정 전(${trip.ddayLabel})까지 수정할 수 있어요 · 호스트 직접 코스"
                            } else {
                                "등록된 코스 · 방문지와 순서 수정 불가"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MoyeoTheme.tints.onPrimaryTint.copy(alpha = .8f)
                        )
                    }
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        item {
            HostManageSectionTitle(
                title = "승인 대기",
                count = pendingApplicants.size,
                trailingNote = "48시간 후 자동 거절"
            )
        }
        if (pendingApplicants.isEmpty()) {
            item {
                SupportCard {
                    Text(
                        text = "새 신청이 오면 이곳에서 승인하거나 거절할 수 있어요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(pendingApplicants, key = { it.id }) { applicant ->
                // 화면기획 18은 첫 신청자만 펼쳐 보여주고 나머지는 접어둔다
                if (applicant.id == expandedApplicantId) {
                    HostApplicantCard(
                        applicant = applicant,
                        primaryLabel = "승인",
                        secondaryLabel = "거절",
                        onPrimary = {
                            pendingApplicants = pendingApplicants.filterNot { it.id == applicant.id }
                            approvedApplicants = approvedApplicants + applicant
                            MockTripRepository.approveHostApplicant(
                                tripId = trip.id,
                                applicantName = applicant.name
                            )
                        },
                        onSecondary = {
                            pendingApplicants = pendingApplicants.filterNot { it.id == applicant.id }
                            rejectedApplicants = rejectedApplicants + applicant
                            MockTripRepository.rejectHostApplicant(
                                tripId = trip.id,
                                applicantName = applicant.name
                            )
                        }
                    )
                } else {
                    SupportCard(
                        modifier = Modifier
                            .clickable { expandedApplicantId = applicant.id }
                            .testTag("host-applicant-${applicant.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.weight(1f)) { HostApplicantHeader(applicant = applicant) }
                            Icon(
                                Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
        item {
            HostManageSectionTitle(title = "승인된 동행자", count = approvedApplicants.size + 1)
        }
        item {
            // 화면기획 18: 승인된 동행자는 아바타 무리 + "본인 외 N명" 한 줄이다
            SupportCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                        (listOf("🦌") + approvedApplicants.map { it.avatar }).forEach { emoji ->
                            AnimalAvatar(emoji, modifier = Modifier.size(34.dp))
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "본인 외 ${approvedApplicants.size}명",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        if (rejectedApplicants.isNotEmpty()) {
            item {
                HostManageSectionTitle(title = "거절 기록", count = rejectedApplicants.size)
            }
            items(rejectedApplicants, key = { it.id }) { applicant ->
                SupportCard {
                    HostApplicantHeader(applicant = applicant)
                    Text(
                        text = "거절 사유: 일정과 동선 조건이 맞지 않아요.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun HostManageSummaryCard(
    trip: TripRecruitment,
    approvedCount: Int,
    pendingCount: Int,
    isRecruitmentClosed: Boolean
) {
    // 화면기획 18의 머리글은 제목과 인원, D-day 배지만 둔다
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = trip.title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "$approvedCount / ${trip.capacity}명",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Surface(shape = RoundedCornerShape(50), color = MoyeoTheme.tints.accentTint) {
                Text(
                    text = trip.ddayLabel,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MoyeoTheme.tints.onAccentTint,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            if (isRecruitmentClosed) {
                SupportChip(text = "모집 취소됨")
            }
            if (pendingCount > 0) {
                Spacer(Modifier.weight(1f))
                Text(
                    text = "대기 $pendingCount",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HostManageSectionTitle(title: String, count: Int, trailingNote: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$title ($count)",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            // 화면기획은 대기 목록 옆에 자동 거절 정책을 함께 알려준다
            text = trailingNote ?: "${count}명",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HostApplicantCard(
    applicant: HostApplicant,
    primaryLabel: String,
    secondaryLabel: String,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit
) {
    SupportCard(modifier = Modifier.testTag("host-applicant-${applicant.id}")) {
        HostApplicantHeader(applicant = applicant)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = "\u201C${applicant.note}\u201D",
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onSecondary,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .testTag("host-applicant-${applicant.id}-reject"),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(secondaryLabel, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onPrimary,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .testTag("host-applicant-${applicant.id}-approve"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(primaryLabel, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HostApplicantHeader(applicant: HostApplicant) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(text = applicant.avatar, fontSize = 24.sp)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = applicant.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = applicant.meta,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class HostApplicant(
    val id: String,
    val name: String,
    val avatar: String,
    val note: String,
    val meta: String = "매너 4.8 · 최근 동행 2회"
)

@Composable
fun FeedWriteScreen(onBack: () -> Unit, onPostCreated: (String) -> Unit, initialStep: Int = 1) {
    // 24-1~24-5 단계별 캡처를 위해 시작 단계를 지정할 수 있다
    var currentStep by rememberSaveable { mutableStateOf(initialStep.coerceIn(1, 5)) }
    var title by rememberSaveable { mutableStateOf("첫 반패키지 단풍 여행") }
    var story by rememberSaveable {
        mutableStateOf("처음 반패키지 여행이었는데 동행분들이 너무 좋으셨어요.\n첨성대 야경이 진짜 인생샷...")
    }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var createdPostId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedVisibility by rememberSaveable { mutableStateOf(FeedVisibility.Friends) }
    // 화면기획 24-1은 경주 감성 코스가 선택된 상태에서 시작한다 (두 번째 후보가 주왕산)
    var selectedCourseId by rememberSaveable { mutableStateOf("gyeongju-healing") }
    val colorScheme = MaterialTheme.colorScheme
    val course = remember(selectedCourseId) { MockTripRepository.findCourse(selectedCourseId) }
    val post = MockTripRepository.findFeedPost("feed-3")

    fun submitPost() {
        val existingPostId = createdPostId
        if (existingPostId != null) {
            onPostCreated(existingPostId)
            return
        }

        val createdPost = MockTripRepository.createFeedPost(
            courseId = course.id,
            title = title,
            story = story,
            visibility = selectedVisibility
        )
        createdPostId = createdPost.id
        submitted = true
        onPostCreated(createdPost.id)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .testTag("feed-write-screen")
    ) {
        FeedWriteHeader(
            currentStep = currentStep,
            submitted = submitted,
            onBack = onBack,
            onSubmit = { submitPost() }
        )
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .testTag("feed-write-scroll"),
            contentPadding = PaddingValues(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                FeedWriteStepIntro(currentStep = currentStep, submitted = submitted)
            }
            when (currentStep) {
                1 -> {
                    item {
                        FeedWriteCourseSelector(
                            // 화면기획 24-1과 같은 순서: 경주 감성 코스 → 주왕산
                            courses = listOf("gyeongju-healing", "cheongsong-juwangsan")
                                .map(MockTripRepository::findCourse) +
                                MockTripRepository.courses.filterNot {
                                    it.id in setOf("gyeongju-healing", "cheongsong-juwangsan")
                                },
                            selectedCourseId = selectedCourseId,
                            onCourseSelected = { selectedCourseId = it }
                        )
                    }
                    item { FeedWriteRouteCard(course = course) }
                    item { FeedWriteMembersCard() }
                }

                2 -> {
                    item { FeedWritePhotoGrid(post = post) }
                    item { FeedWriteRouteCard(course = course) }
                }

                3 -> {
                    // 화면기획은 3단계에서 메모와 사진을 함께 보여준다
                    item {
                        FeedWriteMemoCard(
                            title = title,
                            story = story,
                            onTitleChange = { title = it },
                            onStoryChange = { story = it }
                        )
                    }
                    item { FeedWritePhotoGrid(post = post) }
                }

                4 -> {
                    item {
                        FeedWriteVisibilityCard(
                            selectedVisibility = selectedVisibility,
                            onVisibilitySelected = { selectedVisibility = it }
                        )
                    }
                    item { FeedWriteMemberMeta(course = course, visibility = selectedVisibility) }
                }

                else -> {
                    // 화면기획 24-5: 제목·본문 카드 → 사진 그리드 → 경로 → 메타 순서
                    item {
                        FeedWritePreviewCard(
                            title = title,
                            story = story,
                            submitted = submitted
                        )
                    }
                    item { FeedWritePhotoGrid(post = post) }
                    item { FeedWriteRouteCard(course = course) }
                    item { FeedWriteMemberMeta(course = course, visibility = selectedVisibility) }
                }
            }
        }
        FeedWriteBottomActions(
            currentStep = currentStep,
            submitted = submitted,
            onPrevious = {
                if (currentStep > 1) {
                    currentStep -= 1
                } else {
                    onBack()
                }
            },
            onNext = {
                if (currentStep < 5) {
                    currentStep += 1
                } else {
                    submitPost()
                }
            }
        )
    }
}

@Composable
private fun FeedWriteHeader(currentStep: Int, submitted: Boolean, onBack: () -> Unit, onSubmit: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val canSubmit = currentStep >= 5 && !submitted

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 12.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "닫기",
                    tint = colorScheme.onBackground
                )
            }
            Text(
                text = "피드 글쓰기",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.titleSmall,
                color = colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = if (submitted) "완료" else "게시",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clip(RoundedCornerShape(8.dp))
                    .testTag("feed-write-submit")
                    .clickable(enabled = canSubmit, onClick = onSubmit)
                    .padding(horizontal = 9.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = if (canSubmit) colorScheme.primary else colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.ExtraBold
            )
        }
        FeedWriteProgress(currentStep = currentStep)
        HorizontalDivider(
            modifier = Modifier.padding(top = 14.dp),
            color = colorScheme.outline.copy(alpha = 0.45f)
        )
    }
}

@Composable
private fun FeedWriteProgress(currentStep: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(5) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (index < currentStep) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.48f)
                        }
                    )
            )
        }
    }
}

@Composable
private fun FeedWriteStepIntro(currentStep: Int, submitted: Boolean) {
    val colorScheme = MaterialTheme.colorScheme
    val stepLabel = when {
        submitted -> "STEP 5 · 게시 완료"
        currentStep == 1 -> "STEP 1 · 코스 확인"
        currentStep == 2 -> "STEP 2 · 사진 선택"
        currentStep == 3 -> "STEP 3 · 사진 & 메모"
        currentStep == 4 -> "STEP 4 · 공개 설정"
        else -> "STEP 5 · 최종 확인"
    }
    val title = when {
        submitted -> "기록이 저장됐어요"
        currentStep == 1 -> "어떤 여행을 기록할까요?"
        currentStep == 2 -> "대표 사진을 골라요"
        currentStep == 3 -> "여행 어땠어요?"
        currentStep == 4 -> "누구에게 보여줄까요?"
        else -> "게시 전 마지막 확인이에요"
    }
    val helper = when {
        submitted -> "피드에서 방금 만든 여행 기록을 확인할 수 있어요."
        currentStep == 1 -> "방문 코스와 함께한 멤버를 먼저 확인해요."
        currentStep == 2 -> "사진과 지도 조합이 피드 첫 화면에 함께 보여요."
        currentStep == 3 -> "대부분 자동으로 채워졌어요. 한 줄만 남겨주세요."
        currentStep == 4 -> "친구에게만 공개하고 경로와 멤버 정보를 함께 보여줘요."
        else -> "사진, 경로, 멤버가 한 장의 피드처럼 구성됐어요."
    }

    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            text = stepLabel,
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Black
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = colorScheme.onBackground,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = helper,
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FeedWriteMemoCard(
    title: String,
    story: String,
    onTitleChange: (String) -> Unit,
    onStoryChange: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surface,
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BasicTextField(
                value = title,
                onValueChange = { value -> onTitleChange(value.take(40)) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleSmall.copy(
                    color = colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold
                ),
                singleLine = true,
                cursorBrush = SolidColor(colorScheme.primary)
            )
            BasicTextField(
                value = story,
                onValueChange = { value -> onStoryChange(value.take(500)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(78.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = colorScheme.onSurface,
                    lineHeight = 24.sp
                ),
                cursorBrush = SolidColor(colorScheme.primary)
            )
            Text(
                text = "${story.length} / 500",
                modifier = Modifier.align(Alignment.End),
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FeedWritePhotoGrid(post: FeedPost) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FeedWritePhotoTile(
                post = post,
                representative = true,
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp)
            )
            FeedWritePhotoTile(
                post = post,
                muted = true,
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FeedWritePhotoTile(
                post = post,
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp)
            )
            FeedWriteAddPhotoTile(
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp)
            )
        }
    }
}

@Composable
private fun FeedWriteCourseSelector(
    courses: List<TripCourse>,
    selectedCourseId: String,
    onCourseSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "기록할 코스",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(end = 4.dp)
        ) {
            items(courses.take(6), key = { it.id }) { course ->
                val selected = course.id == selectedCourseId
                Card(
                    modifier = Modifier
                        .width(188.dp)
                        .clickable { onCourseSelected(course.id) }
                        .testTag("feed-write-course-${course.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (selected) ForestGreen else MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 화면기획 24-1: 코스 이미지는 글 위에 전폭으로 놓인다
                        CourseScenicPanel(
                            course = course,
                            modifier = Modifier.fillMaxWidth().height(76.dp),
                            cornerRadius = 12.dp
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = course.title,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${course.region} · ${course.duration}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = if (selected) "선택됨" else "이 코스로 기록",
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (selected) {
                                        ForestGreen
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedWritePhotoTile(
    post: FeedPost,
    modifier: Modifier = Modifier,
    representative: Boolean = false,
    muted: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colorScheme.surfaceVariant)
    ) {
        FeedPhotoPanel(post = post, modifier = Modifier.fillMaxSize(), cornerRadius = 10.dp)
        if (muted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorScheme.background.copy(alpha = 0.46f))
            )
        }
        if (representative) {
            Surface(
                modifier = Modifier
                    .padding(6.dp)
                    .align(Alignment.TopStart),
                shape = RoundedCornerShape(50),
                color = colorScheme.primary
            ) {
                Text(
                    text = "대표",
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun FeedWriteAddPhotoTile(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = colorScheme.surface,
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.62f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "사진 추가",
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FeedWriteRouteCard(course: TripCourse) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surface,
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.20f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "경로 (자동)",
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            FeedRouteMapPanel(
                stops = course.stops,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp),
                cornerRadius = 14.dp
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(
                    imageVector = Icons.Filled.Place,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${course.region} · ${course.stops.size} stops · 11/8 ~ 11/9",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FeedWriteVisibilityCard(
    selectedVisibility: FeedVisibility,
    onVisibilitySelected: (FeedVisibility) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surface,
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.20f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "공개 범위",
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FeedVisibility.entries.forEach { visibility ->
                    FeedVisibilityOption(
                        visibility = visibility,
                        selected = visibility == selectedVisibility,
                        onClick = { onVisibilitySelected(visibility) }
                    )
                }
            }
            Text(
                text = selectedVisibility.helperText(),
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FeedVisibilityOption(visibility: FeedVisibility, selected: Boolean, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val background = if (selected) colorScheme.primary else colorScheme.primaryContainer
    val content = if (selected) colorScheme.onPrimary else colorScheme.primary

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .testTag("feed-write-visibility-${visibility.name.lowercase()}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = background,
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = if (selected) 0.0f else 0.35f))
    ) {
        Text(
            text = visibility.label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelSmall,
            color = content,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun FeedWriteMemberMeta(course: TripCourse, visibility: FeedVisibility) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surface,
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.20f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FeedWriteMetaRow(label = "코스", value = course.title, icon = Icons.Filled.Map)
            FeedWriteMetaRow(
                label = "지역",
                value = "${course.region} · ${course.duration} · 42.6km",
                icon = Icons.Filled.Place
            )
            FeedWriteMetaRow(label = "공개", value = "${visibility.label} · 경로지도 포함")
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("🦌", "🐻", "🐢").forEach { emoji -> AnimalAvatar(emoji, modifier = Modifier.size(24.dp)) }
                Text(
                    text = "함께한 멤버 3명",
                    modifier = Modifier.padding(start = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                (listOf(visibility.label, "경로지도", course.region) + course.tags.take(2)).forEach { tag ->
                    FeedWriteTinyPill(text = tag)
                }
            }
        }
    }
}

/** 화면기획 24-1의 "함께 간 멤버 (4)" 카드 — 아바타 칩 4개, 첫 칩이 나. */
@Composable
private fun FeedWriteMembersCard() {
    val colorScheme = MaterialTheme.colorScheme
    val members = listOf(
        Triple("🦌", "따스한 사슴 3492 (나)", true),
        Triple("🐻", "우직한 곰 7821", false),
        Triple("🐢", "잔잔한 거북이 9032", false),
        Triple("🪽", "고요한 두루미 1130", false)
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surface,
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.20f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "함께 간 멤버 (${members.size})",
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                members.take(2).forEach { (emoji, name, isMe) -> FeedWriteMemberChip(emoji, name, isMe) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                members.drop(2).forEach { (emoji, name, isMe) -> FeedWriteMemberChip(emoji, name, isMe) }
            }
        }
    }
}

@Composable
private fun FeedWriteMemberChip(emoji: String, name: String, isMe: Boolean) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(50),
        color = if (isMe) MoyeoTheme.tints.primaryTint else colorScheme.surfaceVariant,
        border = BorderStroke(
            1.dp,
            if (isMe) colorScheme.primary.copy(alpha = .4f) else colorScheme.outline.copy(alpha = .5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(start = 5.dp, top = 4.dp, end = 11.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AnimalAvatar(emoji, modifier = Modifier.size(22.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall,
                color = if (isMe) colorScheme.primary else colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun FeedWriteMetaRow(label: String, value: String, icon: ImageVector? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Spacer(modifier = Modifier.size(15.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FeedWritePreviewCard(title: String, story: String, submitted: Boolean) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = colorScheme.surface,
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.28f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = story,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface
            )
            Text(
                text = "${story.length} / 500",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant
            )
            if (submitted) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "피드에 기록이 올라갔어요",
                        style = MaterialTheme.typography.labelLarge,
                        color = colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedWriteTinyPill(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

private fun FeedVisibility.helperText(): String = when (this) {
    FeedVisibility.Public -> "발견 탭에서도 보이고, 경북 여행자 누구나 볼 수 있어요."
    FeedVisibility.Friends -> "서로 친구인 사람에게만 보여요. 기본값이에요."
    FeedVisibility.Private -> "나만 볼 수 있는 기록으로 저장돼요."
}

@Composable
private fun FeedWriteBottomActions(currentStep: Int, submitted: Boolean, onPrevious: () -> Unit, onNext: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val nextLabel = when {
        submitted -> "완료"
        currentStep < 5 -> "다음 (${currentStep + 1}/5)"
        else -> "게시하기"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colorScheme.background,
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.28f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "이전",
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onPrevious)
                    .padding(horizontal = 6.dp, vertical = 12.dp),
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold
            )
            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(1f)
                    .testTag("feed-write-next")
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text(
                    text = nextLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(onBack: () -> Unit, onOpenCourse: (String) -> Unit, initialQuery: String = "") {
    var query by rememberSaveable { mutableStateOf(initialQuery) }
    var submittedQuery by rememberSaveable { mutableStateOf("") }
    // 최근 검색어는 서버 API가 없는 클라이언트 전용 기능이다 — 기기에 영구 저장한다.
    // 캡처에서는 기기에 남은 기록이 아니라 화면기획 목데이터를 보여준다.
    val context = LocalContext.current
    val captureMode = LocalCaptureMode.current
    val recentSearchStore = remember(context, captureMode) {
        if (captureMode) {
            InMemoryRecentSearchStore(PLANNING_RECENT_SEARCHES)
        } else {
            PersistedRecentSearchStore(context.applicationContext)
        }
    }
    val recentSearches by recentSearchStore.keywords.collectAsState()
    val runSearch: (String) -> Unit = { keyword ->
        query = keyword
        submittedQuery = keyword
        recentSearchStore.record(keyword)
    }
    val trimmedQuery = submittedQuery.trim()
    val courses = remember(trimmedQuery) {
        MockTripRepository.courses.filter { course ->
            course.title.contains(trimmedQuery, ignoreCase = true) ||
                course.region.contains(trimmedQuery, ignoreCase = true) ||
                course.tags.any { it.contains(trimmedQuery, ignoreCase = true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
            }
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    submittedQuery = it
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("search-query-field"),
                leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null) },
                placeholder = { Text("지역, 테마, 코스 검색") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                // 검색을 실행한 순간에만 최근 검색어에 남긴다 — 글자마다 쌓으면 목록이 조각난다
                keyboardActions = KeyboardActions(onSearch = { recentSearchStore.record(query) })
            )
            Text(
                text = "취소",
                modifier = Modifier
                    .clickable {
                        query = ""
                        submittedQuery = ""
                    }
                    .padding(horizontal = 4.dp, vertical = 12.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .testTag("support-list"),
            contentPadding = PaddingValues(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (submittedQuery.isBlank()) {
                // 화면기획에 빈 상태 문구가 없어서, 최근 검색어가 없으면 섹션 전체를 숨긴다
                if (recentSearches.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search-recent-section"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "최근 검색어",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "전체 삭제",
                                modifier = Modifier
                                    .clickable(onClick = recentSearchStore::clear)
                                    .testTag("search-recent-clear-all"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    item {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            recentSearches.forEach { keyword ->
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    color = MaterialTheme.colorScheme.background
                                ) {
                                    Row(
                                        modifier = Modifier.padding(start = 11.dp, end = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = keyword,
                                            modifier = Modifier
                                                // 사용자 입력은 길이가 임의라 캡슐 밖으로 넘칠 수 있다
                                                .weight(1f, fill = false)
                                                .clickable { runSearch(keyword) }
                                                .testTag("search-recent-$keyword")
                                                .padding(vertical = 8.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        IconButton(
                                            onClick = { recentSearchStore.remove(keyword) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Close,
                                                contentDescription = "$keyword 삭제",
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Text(
                        text = "인기 검색어",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                items(
                    listOf(
                        Triple(1, "주왕산", true),
                        Triple(2, "안동 한옥마을", true),
                        Triple(3, "경주 야경", false),
                        Triple(4, "포항 호미곶", true),
                        Triple(5, "문경 새재", false)
                    )
                ) { (rank, keyword, isRising) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clickable { runSearch(keyword) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = rank.toString(),
                            modifier = Modifier.width(18.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (rank <= 3) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = keyword,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isRising) "▲" else "−",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isRising) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            } else if (courses.isEmpty()) {
                item {
                    SupportCard(modifier = Modifier.testTag("search-empty-state")) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "검색 결과가 없어요",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "지역 이름이나 자연, 야경, 고택 같은 테마로 다시 찾아보세요.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(listOf("경주", "청송", "야경")) { keyword ->
                                    SupportChip(
                                        text = keyword,
                                        modifier = Modifier.testTag("search-recovery-$keyword"),
                                        onClick = { runSearch(keyword) }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                items(courses, key = { it.id }) { course ->
                    SupportCard(modifier = Modifier.clickable { onOpenCourse(course.id) }) {
                        CourseSummaryCard(course = course, compact = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun SupportScaffold(
    title: String,
    onBack: () -> Unit,
    itemSpacing: Dp = 16.dp,
    // / 헤더 오른쪽 텍스트 버튼 (예: 알림의 "모두 읽음")
    trailingTitle: String? = null,
    onTrailingClick: (() -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    // 기본 배경(#F7F8F7)은 surfaceVariant와 같은 값이라, 회색 채움 카드를 쓰는 화면은
    // 화면기획처럼 흰 배경(surface)으로 바꿔야 카드가 읽힌다.
    containerColor: Color? = null,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(containerColor ?: MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            if (trailingTitle != null && onTrailingClick != null) {
                TextButton(onClick = onTrailingClick, modifier = Modifier.testTag("support-list-trailing")) {
                    Text(
                        text = trailingTitle,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(40.dp))
            }
        }
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .then(if (bottomBar == null) Modifier.navigationBarsPadding() else Modifier)
                .testTag("support-list"),
            contentPadding = PaddingValues(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
            content = content
        )
        if (bottomBar != null) {
            Surface(color = MaterialTheme.colorScheme.background, shadowElevation = 8.dp) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    bottomBar()
                }
            }
        }
    }
}

@Composable
private fun CourseSummaryCard(course: TripCourse, compact: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 64.dp else 82.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(text = course.imageEmoji, fontSize = if (compact) 30.sp else 42.sp)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                text = course.title,
                style = if (compact) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${course.region} · ${course.duration} · ${course.rating}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!compact) {
                Text(
                    text = course.oneLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SupportCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
private fun SupportField(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(imageVector = Icons.Filled.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun IconBubble(color: Color = MaterialTheme.colorScheme.primaryContainer, content: @Composable () -> Unit) {
    Surface(modifier = Modifier.size(42.dp), shape = CircleShape, color = color) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

@Composable
private fun SupportChip(text: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Surface(
        modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.32f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = ForestGreen,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

/** 알림 한 줄의 아이콘 원 색 조합. */
private data class NotificationTone(val container: Color, val content: Color)

@Composable
private fun notificationTone(type: String): NotificationTone {
    val tints = MoyeoTheme.tints
    return when (type) {
        // 메시지·좋아요는 코랄 계열, 좋아요만 한 단 밝은 코랄이다 (화면기획 13)
        // 서버 알림(chat-server)도 같은 메시지 종류이므로 같은 색을 쓴다
        "feed", "chat-server" -> NotificationTone(tints.accentTint, tints.onAccentTint)

        "likes" -> NotificationTone(tints.accentTint, Coral)

        "deadline" -> NotificationTone(tints.warningTint, tints.onWarningTint)

        "friend-request" -> NotificationTone(tints.infoTint, tints.onInfoTint)

        "removal" -> NotificationTone(tints.dangerTint, tints.onDangerTint)

        else -> NotificationTone(tints.primaryTintStrong, tints.primaryEmphasis)
    }
}

private data class NotificationItem(
    val title: String,
    val body: String,
    val time: String,
    val type: String,
    val targetId: String,
    // / 화면기획처럼 오늘/어제로 묶어서 보여준다
    val group: String = "오늘",
    val unread: Boolean = false,
    // / 화면기획 본문의 `<b>` 구간 — 모임 이름·핵심 단어만 굵게 남긴다
    val emphasis: List<String> = emptyList(),
    // / 실서버 알림이면 notificationId — 탭할 때 읽음 처리(PUT read)에 쓴다
    val serverId: Long? = null,
    val chatRoomId: Long? = null
)

private fun String.kickedAtLabel(): String = runCatching {
    LocalDateTime.parse(this).format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"))
}.getOrDefault(this)

/** 실서버 알림 → 목록 행. 서버 type 을 화면기획 13의 아이콘 종류로 대응시킨다. */
private fun ServerNotification.toNotificationItem(): NotificationItem {
    val itemType = when (type) {
        "CHAT_ROOM_KICKED" -> "removal"
        "CHAT_MESSAGE_RECEIVED" -> "chat-server"
        "TRAVEL_COURSE_UPDATED" -> "course-server"
        "RECRUITMENT_DEADLINE" -> "deadline"
        "FRIEND_REQUEST" -> "friend-request"
        "FEED_LIKE" -> "likes"
        else -> "trip-server"
    }
    val createdDate = runCatching { LocalDateTime.parse(createdAt).toLocalDate() }.getOrNull()
    val today = LocalDate.now()
    val group = when (createdDate) {
        null -> "이전"
        today -> "오늘"
        today.minusDays(1) -> "어제"
        else -> "이전"
    }
    val timeLabel = runCatching {
        val created = LocalDateTime.parse(createdAt)
        when (group) {
            "오늘" -> created.format(DateTimeFormatter.ofPattern("HH:mm"))
            "어제" -> created.format(DateTimeFormatter.ofPattern("어제 HH:mm"))
            else -> created.format(DateTimeFormatter.ofPattern("M월 d일"))
        }
    }.getOrDefault(createdAt)
    return NotificationItem(
        title = content,
        body = "",
        time = timeLabel,
        type = itemType,
        targetId = referenceId?.toString().orEmpty(),
        group = group,
        unread = !read,
        serverId = notificationId,
        chatRoomId = chatRoomId
    )
}
