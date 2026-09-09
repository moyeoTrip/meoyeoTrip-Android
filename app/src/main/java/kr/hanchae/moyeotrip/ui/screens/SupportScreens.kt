package kr.hanchae.moyeotrip.ui.screens

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.History
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import kotlin.math.abs
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kr.hanchae.moyeotrip.data.FeedVisibility
import kr.hanchae.moyeotrip.data.api.MultipartFile
import kr.hanchae.moyeotrip.data.courses.TravelCourse
import kr.hanchae.moyeotrip.data.notifications.ServerNotification
import kr.hanchae.moyeotrip.data.rooms.ChatRoomDetail
import kr.hanchae.moyeotrip.data.rooms.ChatRoomSearchResult
import kr.hanchae.moyeotrip.data.rooms.MyChatRoom
import kr.hanchae.moyeotrip.data.rooms.RoomApplication
import kr.hanchae.moyeotrip.data.rooms.RoomCompanion
import kr.hanchae.moyeotrip.data.rooms.RoomKickHistory
import kr.hanchae.moyeotrip.data.rooms.RoomMembers
import kr.hanchae.moyeotrip.data.rooms.recruitmentDDayText
import kr.hanchae.moyeotrip.data.search.PersistedRecentSearchStore
import kr.hanchae.moyeotrip.data.search.PopularKeyword
import kr.hanchae.moyeotrip.data.search.RankTrend
import kr.hanchae.moyeotrip.ui.LocalServerData
import kr.hanchae.moyeotrip.ui.components.CachedRemoteImage
import kr.hanchae.moyeotrip.ui.components.CourseRouteMap
import kr.hanchae.moyeotrip.ui.components.MOYEO_CTA_HEIGHT
import kr.hanchae.moyeotrip.ui.components.MOYEO_CTA_RADIUS
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyState
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyText
import kr.hanchae.moyeotrip.ui.components.MoyeoPlaceholderShape
import kr.hanchae.moyeotrip.ui.components.ServerListState
import kr.hanchae.moyeotrip.ui.components.emphasized
import kr.hanchae.moyeotrip.ui.components.moyeoDashedOutline
import kr.hanchae.moyeotrip.ui.components.moyeoRelativeTime
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
    // 알림은 실서버(GET notifications)가 근거다 — 못 받아오면 빈 상태를 그린다
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
    var showsUnreadOnly by rememberSaveable { mutableStateOf(false) }
    val activeItems = serverItems.orEmpty()
    val unreadCount = serverUnreadCount
    val visible = if (showsUnreadOnly) activeItems.filter { it.unread } else activeItems
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
            serverScope.launch {
                runCatching { server?.notifications?.markAllRead() }
                    .onSuccess {
                        serverItems = serverItems?.map { it.copy(unread = false) }
                        serverUnreadCount = 0
                    }
            }
        }
    ) {
        if (activeItems.isEmpty()) {
            item { MoyeoEmptyState(MoyeoEmptyText.NO_NOTIFICATIONS, testTag = "notifications-empty") }
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

                                    // 같은 종류의 알림은 같은 아이콘이다 —
                                    // CHAT_MESSAGE_RECEIVED / TRAVEL_COURSE_UPDATED 가 서로 다른
                                    // 아이콘을 쓰면 목록에서 종류를 구분할 수 없다.
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
        if (server == null) return@LaunchedEffect
        // 알림에서 들어오면 그 알림의 이력을, **id 없이 열리면(딥링크·캡처) 내 가장 최근
        // 강퇴 알림**의 이력을 읽는다. 지어내지 않고 실제 이력을 쓴다.
        val targetId = notificationId ?: runCatching {
            server.notifications.notifications(size = 50).notifications
                .firstOrNull { it.type == "CHAT_ROOM_KICKED" }?.notificationId
        }.getOrNull()
        if (targetId != null) {
            kickHistory = runCatching { server.notifications.kickHistory(targetId) }.getOrNull()
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
                        // 모임 이름은 서버 강퇴 이력이 근거다 — 없으면 이름 없이 사실만 남긴다
                        // 서버 `roomTitle` 을 그대로 쓴다 — 「모임」을 덧붙이면
                        // 「QA 자동승인 모임 **모임**에서」처럼 겹친다 (사용자 지적, 2026-09-09).
                        text = kickHistory?.let { "${it.roomTitle}에서\n내보내졌어요" } ?: "모임에서 내보내졌어요",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = kickHistory?.kickedAt?.kickedAtLabel() ?: "호스트 결정",
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
                                    ?: "호스트가 남긴 사유가 없어요.",
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

/**
 * 화면기획 18 모집 관리 — 서버 모임(`room-{roomId}`)의 신청자를 그대로 다룬다.
 *
 * 신청 목록은 GET chat-rooms/{id}/applications, 승인·거절은 POST approve · reject 다.
 * `room-` 형태가 아니거나 미로그인이면 다룰 신청이 없으므로 빈 상태다.
 */
@Composable
fun HostManageScreen(
    tripId: String,
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit,
    onOpenRoute: (String) -> Unit = {},
    /** 18-1 여행 확정 / 불발 — 호스트가 그 버튼을 누르는 화면이 없었다(정본 §6-1). */
    onOpenTripStatus: (String) -> Unit = {},
    /** 18-2 집합 정보 수정 — 모집을 연 뒤 집합 장소·시간을 고칠 길이 없었다. */
    onOpenMeetingEdit: (String) -> Unit = {}
) {
    val server = LocalServerData.current
    val roomId = tripId.serverRoomIdOrNull()
    if (roomId == null || server == null) {
        SupportScaffold(title = "모집 관리", onBack = onBack) {
            item { MoyeoEmptyState(MoyeoEmptyText.NO_JOINED_ROOMS, testTag = "host-manage-empty") }
        }
        return
    }

    var room by remember(roomId) { mutableStateOf<ChatRoomDetail?>(null) }
    var members by remember(roomId) { mutableStateOf<RoomMembers?>(null) }
    var applications by remember(roomId) {
        mutableStateOf<ServerListState<RoomApplication>>(ServerListState.Loading)
    }
    var expandedApplicationId by remember(roomId) { mutableStateOf<Long?>(null) }
    var actionError by remember(roomId) { mutableStateOf<String?>(null) }
    var reloadKey by remember(roomId) { mutableIntStateOf(0) }
    val actionScope = rememberCoroutineScope()

    LaunchedEffect(roomId, server, reloadKey) {
        room = runCatching { server.chatRooms.room(roomId) }.getOrNull()
        members = runCatching { server.chatRooms.members(roomId) }.getOrNull()
        applications = runCatching { server.chatRooms.applications(roomId) }
            .fold({ ServerListState.Loaded(it) }, { ServerListState.Failed })
        expandedApplicationId = (applications as? ServerListState.Loaded)?.items?.firstOrNull()?.applicationId
    }

    fun decide(application: RoomApplication, approve: Boolean) {
        actionScope.launch {
            runCatching {
                if (approve) {
                    server.chatRooms.approveApplication(roomId, application.applicationId)
                } else {
                    server.chatRooms.rejectApplication(roomId, application.applicationId)
                }
            }.onSuccess { reloadKey++ }
                .onFailure { error -> actionError = error.message ?: "신청을 처리하지 못했어요." }
        }
    }

    val pending = (applications as? ServerListState.Loaded)?.items.orEmpty()

    SupportScaffold(
        title = "모집 관리",
        onBack = onBack,
        bottomBar = {
            Button(
                onClick = { onOpenChat("room-$roomId") },
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
        room?.let { detail ->
            item {
                HostManageSummaryCard(
                    title = detail.title,
                    participantCount = members?.participantCount ?: detail.participantCount,
                    maxParticipants = members?.maxParticipants ?: detail.maxParticipants,
                    dDayLabel = recruitmentDDayText(detail.recruitmentDDay),
                    pendingCount = pending.size,
                    isRecruitmentClosed = detail.status != "RECRUITING"
                )
            }
        }
        item {
            // 코스 카드를 먼저 두고, 그 아래에 **나란한 버튼 두 개**를 둔다 — 기획·웹이 그렇다.
            // 예전에는 셰브런이 달린 목록 줄 세 개였고 순서도 달라서, 같은 화면인데
            // 안드로이드만 「메뉴 화면」처럼 보였다 (18, 사용자 지적 2026-09-09).
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { onOpenRoute("room-$roomId") }
                    .testTag("host-manage-route"),
                shape = RoundedCornerShape(12.dp),
                color = MoyeoTheme.tints.primaryTint
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Route, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        "여행 경로 보기",
                        modifier = Modifier.padding(start = 12.dp).weight(1f),
                        color = MoyeoTheme.tints.onPrimaryTint,
                        fontWeight = FontWeight.ExtraBold
                    )
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
            // 18-1 여행 확정 / 불발 · 18-2 집합 정보 수정 — 확정은 모집 중일 때만 의미가 있다.
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onOpenTripStatus("room-$roomId") },
                    modifier = Modifier.weight(1f).height(46.dp).testTag("host-manage-status"),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("여행 확정하기", fontWeight = FontWeight.Bold) }
                OutlinedButton(
                    onClick = { onOpenMeetingEdit("room-$roomId") },
                    modifier = Modifier.weight(1f).height(46.dp).testTag("host-manage-meeting"),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("집합 정보 수정", fontWeight = FontWeight.Bold) }
            }
        }
        actionError?.let { message ->
            item {
                Text(
                    text = message,
                    modifier = Modifier.testTag("host-manage-error"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        item { HostManageSectionTitle(title = "승인 대기", count = pending.size, trailingNote = "48시간 후 자동 거절") }
        when {
            applications is ServerListState.Loading -> item { MoyeoEmptyState(MoyeoEmptyText.LOADING) }

            applications is ServerListState.Failed -> item {
                MoyeoEmptyState(MoyeoEmptyText.FAILED, onRetry = { reloadKey++ })
            }

            pending.isEmpty() -> item {
                SupportCard {
                    Text(
                        text = "새 신청이 오면 이곳에서 승인하거나 거절할 수 있어요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> items(pending, key = { it.applicationId }) { application ->
                // 화면기획 18은 첫 신청자만 펼쳐 보여주고 나머지는 접어둔다
                if (application.applicationId == expandedApplicationId) {
                    HostApplicantCard(
                        application = application,
                        onApprove = { decide(application, approve = true) },
                        onReject = { decide(application, approve = false) }
                    )
                } else {
                    SupportCard(
                        modifier = Modifier
                            .clickable { expandedApplicationId = application.applicationId }
                            .testTag("host-applicant-${application.applicationId}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.weight(1f)) { HostApplicantHeader(application = application) }
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
        members?.let { loadedMembers ->
            item { HostManageSectionTitle(title = "승인된 동행자", count = loadedMembers.members.size) }
            item {
                SupportCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                            loadedMembers.members.take(6).forEach { member ->
                                UserAvatar(
                                    imageUrl = member.profileImageUrl,
                                    nickname = member.nickname,
                                    modifier = Modifier.size(34.dp),
                                    fallbackFontSize = 16.sp
                                )
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "${loadedMembers.participantCount}/${loadedMembers.maxParticipants}명",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HostManageSummaryCard(
    title: String,
    participantCount: Int,
    maxParticipants: Int,
    dDayLabel: String?,
    pendingCount: Int,
    isRecruitmentClosed: Boolean
) {
    // 화면기획 18의 머리글은 제목과 인원, D-day 배지만 둔다
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "$participantCount / ${maxParticipants}명",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            dDayLabel?.let { dday ->
                Surface(shape = RoundedCornerShape(50), color = MoyeoTheme.tints.accentTint) {
                    Text(
                        text = dday,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MoyeoTheme.tints.onAccentTint,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            if (isRecruitmentClosed) {
                SupportChip(text = "모집 종료")
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

/** 18 모집 관리에서 다른 화면으로 나가는 줄. `여행 경로 보기` 와 같은 위계다. */
@Composable
private fun HostManageEntryRow(icon: ImageVector, label: String, testTag: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
                label,
                modifier = Modifier.padding(start = 12.dp).weight(1f),
                fontWeight = FontWeight.ExtraBold
            )
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
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
private fun HostApplicantCard(application: RoomApplication, onApprove: () -> Unit, onReject: () -> Unit) {
    SupportCard(modifier = Modifier.testTag("host-applicant-${application.applicationId}")) {
        HostApplicantHeader(application = application)
        // 한마디는 선택이다 — 없으면 인용 카드 자체를 그리지 않는다
        application.applicationMessage?.takeIf(String::isNotBlank)?.let { message ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "\u201C$message\u201D",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onReject,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .testTag("host-applicant-${application.applicationId}-reject"),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("거절", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onApprove,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .testTag("host-applicant-${application.applicationId}-approve"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("승인", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HostApplicantHeader(application: RoomApplication) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        UserAvatar(
            imageUrl = application.profileImageUrl,
            nickname = application.nickname,
            modifier = Modifier.size(44.dp),
            fallbackFontSize = 22.sp
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = application.nickname,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            // 서버가 주지 않는 값(나이·성별·매너)은 줄에서 빠진다 — 지어내지 않는다
            val meta = listOfNotNull(
                application.age?.let { "${it}세" },
                application.gender?.applicantGenderLabel(),
                application.mannerRating?.let { "매너 $it" },
                "여행 ${application.completedTripCount}회"
            ).joinToString(" · ")
            Text(
                text = meta,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun String.applicantGenderLabel(): String? = when (this) {
    "M", "MALE" -> "남성"
    "F", "FEMALE" -> "여성"
    else -> null
}

/**
 * 화면기획 24 피드 작성 — 다녀온 여행 하나를 골라 POST /api/v1/feeds 로 올린다.
 *
 * 서버는 사진을 **최소 한 장** 요구하므로 게시 버튼은 사진을 고르기 전까지 잠긴다.
 *
 * 후보는 `ended && status == "CONFIRMED"` 인 내 모임뿐이다 — 취소된 방(`CANCELLED`)은
 * 끝난 것이지 **다녀온 것이 아니다**. 예전에는 `ended` 만 봐서 `검증용 방 0825`(41) 같은
 * 취소 방이 "기록할 여행"으로 올라왔다.
 *
 * 경로·코스 요약은 고른 여행의 코스(GET travel-courses/chat-rooms/{roomId})가 근거다 —
 * 웹·iOS 와 같은 API 다. 좌표가 없으면 지도를 그리지 않고, 코스를 못 읽으면 요약 줄이 빠진다.
 * 함께 간 멤버는 GET chat-rooms/{roomId}/companions 가 근거다 — 0명이면 `(0)` 이 사실이고
 * 예시 멤버를 채우지 않는다(정본 R1).
 */
@Composable
fun FeedWriteScreen(
    onBack: () -> Unit,
    onPostCreated: (String) -> Unit,
    initialStep: Int = 1,
    // 캡처는 특정 방(`feedwrite1:room-101`)을 지정한다. 지정이 없으면 첫 후보를 고른다.
    requestedRoomId: Long? = null
) {
    // 24-1~24-5 단계별 캡처를 위해 시작 단계를 지정할 수 있다
    var currentStep by rememberSaveable { mutableStateOf(initialStep.coerceIn(1, 5)) }
    var story by rememberSaveable { mutableStateOf("") }
    var feedTitle by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var createdPostId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedVisibility by rememberSaveable { mutableStateOf(FeedVisibility.Friends) }
    var selectedRoomId by rememberSaveable { mutableStateOf<Long?>(null) }
    var submitError by remember { mutableStateOf<String?>(null) }
    var submitting by remember { mutableStateOf(false) }
    val pickedImages = remember { mutableStateListOf<PickedImage>() }
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val server = LocalServerData.current
    val writeScope = rememberCoroutineScope()

    // 피드는 "다녀온 여행"에만 붙는다 — 끝났고(ended) 확정된(CONFIRMED) 내 모임만 후보다.
    // 취소된 방은 끝난 것이지 다녀온 것이 아니다.
    var completedRooms by remember(server) { mutableStateOf<List<MyChatRoom>>(emptyList()) }
    LaunchedEffect(server) {
        completedRooms = if (server == null) {
            emptyList()
        } else {
            runCatching {
                server.chatRooms.myRooms().filter { it.ended && it.status == CONFIRMED_ROOM_STATUS }
            }.getOrElse { emptyList() }
        }
        if (selectedRoomId == null) {
            selectedRoomId = requestedRoomId?.takeIf { requested -> completedRooms.any { it.roomId == requested } }
                ?: completedRooms.firstOrNull()?.roomId
        }
    }

    // 후보 카드·경로·요약이 모두 "그 여행의 코스"를 근거로 한다 — 후보마다 한 번씩 읽어 둔다.
    var roomCourses by remember(server) { mutableStateOf<Map<Long, TravelCourse>>(emptyMap()) }
    LaunchedEffect(server, completedRooms) {
        if (server == null) {
            roomCourses = emptyMap()
            return@LaunchedEffect
        }
        // 후보마다 **동시에** 받는다. 순차로 돌리면 완료된 여행 수에 비례해 카드가 늦게 채워진다
        // (iOS 는 같은 순차 조회 때문에 빈 화면이 먼저 찍히기까지 했다).
        roomCourses = coroutineScope {
            completedRooms
                .map { room ->
                    async {
                        runCatching { server.courses.roomCourse(room.roomId) }
                            .getOrNull()
                            ?.let { room.roomId to it }
                    }
                }
                .awaitAll()
                .filterNotNull()
                .toMap()
        }
    }
    val selectedCourse = selectedRoomId?.let { roomCourses[it] }

    // 24-1 함께 간 멤버 — 완료 여행 전용 API 다. 미완료 방은 409 라 빈 목록으로 떨어진다.
    var companions by remember(server) { mutableStateOf<List<RoomCompanion>>(emptyList()) }
    LaunchedEffect(server, selectedRoomId) {
        val roomId = selectedRoomId
        companions = if (server == null || roomId == null) {
            emptyList()
        } else {
            runCatching { server.chatRooms.companions(roomId) }.getOrElse { emptyList() }
        }
    }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        writeScope.launch {
            val picked = readPickedImage(context, uri)
            if (picked == null) {
                submitError = "사진을 읽지 못했어요."
            } else if (pickedImages.size < FEED_IMAGE_LIMIT) {
                pickedImages.add(picked)
            }
        }
    }

    fun submitPost() {
        val existingPostId = createdPostId
        if (existingPostId != null) {
            onPostCreated(existingPostId)
            return
        }
        val roomId = selectedRoomId
        if (server == null || roomId == null) {
            submitError = "기록할 여행을 먼저 골라 주세요."
            return
        }
        if (pickedImages.isEmpty()) {
            submitError = "사진을 최소 한 장 골라 주세요."
            return
        }
        submitting = true
        submitError = null
        writeScope.launch {
            runCatching {
                server.feeds.createFeed(
                    chatRoomId = roomId,
                    content = feedContentWithTitle(feedTitle, story),
                    visibility = selectedVisibility.serverValue,
                    images = pickedImages.mapIndexed { index, image ->
                        MultipartFile(
                            partName = "images",
                            fileName = image.fileName.ifBlank { "feed-$index" },
                            mimeType = image.mimeType,
                            bytes = image.bytes
                        )
                    }
                )
            }.fold(
                onSuccess = { feed ->
                    submitting = false
                    submitted = true
                    createdPostId = "srv-${feed.feedId}"
                    onPostCreated("srv-${feed.feedId}")
                },
                onFailure = { error ->
                    submitting = false
                    submitError = error.message ?: "피드를 올리지 못했어요."
                }
            )
        }
    }

    val canSubmit = !submitting && !submitted && selectedRoomId != null &&
        pickedImages.isNotEmpty() && story.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .testTag("feed-write-screen")
    ) {
        FeedWriteHeader(
            currentStep = currentStep,
            canSubmit = canSubmit,
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
            item { FeedWriteStepIntro(currentStep = currentStep, submitted = submitted) }
            submitError?.let { message ->
                item {
                    Text(
                        text = message,
                        modifier = Modifier.testTag("feed-write-error"),
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.error
                    )
                }
            }
            when (currentStep) {
                // 24-1 은 코스 후보 · 경로 · 함께 간 멤버 세 블록이다 (기획 정본 · iOS 와 같다)
                1 -> {
                    item {
                        FeedWriteTripSelector(
                            rooms = completedRooms,
                            courses = roomCourses,
                            selectedRoomId = selectedRoomId,
                            onSelect = { selectedRoomId = it }
                        )
                    }
                    item { FeedWriteRouteCard(course = selectedCourse) }
                    item { FeedWriteMembersCard(companions = companions) }
                }

                2 -> {
                    item {
                        FeedWritePhotoGrid(
                            images = pickedImages,
                            onAdd = {
                                photoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onRemove = { index -> pickedImages.removeAt(index) }
                        )
                    }
                    // 기획 24-2 · iOS 모두 사진 아래에 같은 경로 카드를 둔다
                    item { FeedWriteRouteCard(course = selectedCourse) }
                }

                3 -> {
                    item {
                        FeedWriteMemoCard(
                            title = feedTitle,
                            story = story,
                            onTitleChange = { feedTitle = it },
                            onStoryChange = { story = it }
                        )
                    }
                    item {
                        FeedWritePhotoGrid(
                            images = pickedImages,
                            onAdd = {
                                photoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onRemove = { index -> pickedImages.removeAt(index) }
                        )
                    }
                }

                4 -> {
                    item {
                        FeedWriteVisibilityCard(
                            selectedVisibility = selectedVisibility,
                            onVisibilitySelected = { selectedVisibility = it }
                        )
                    }
                    // 기획 24-4 · iOS 모두 공개 범위 아래에 코스·일정·공개 요약 카드를 둔다
                    item {
                        FeedWriteMetaCard(
                            course = selectedCourse,
                            visibility = selectedVisibility,
                            routeIncluded = selectedCourse?.places?.toRoutePoints().orEmpty().isNotEmpty(),
                            companionCount = companions.size
                        )
                    }
                }

                else -> {
                    item { FeedWritePreviewCard(story = story, submitted = submitted) }
                    item {
                        FeedWritePhotoGrid(
                            images = pickedImages,
                            onAdd = {
                                photoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onRemove = { index -> pickedImages.removeAt(index) }
                        )
                    }
                    item { FeedWriteRouteCard(course = selectedCourse) }
                    item {
                        FeedWriteMetaCard(
                            course = selectedCourse,
                            visibility = selectedVisibility,
                            routeIncluded = selectedCourse?.places?.toRoutePoints().orEmpty().isNotEmpty(),
                            companionCount = companions.size
                        )
                    }
                }
            }
        }
        FeedWriteBottomActions(
            currentStep = currentStep,
            submitted = submitted,
            onPrevious = {
                if (currentStep > 1) currentStep -= 1 else onBack()
            },
            onNext = {
                if (currentStep < 5) currentStep += 1 else submitPost()
            }
        )
    }
}

/** 서버가 받는 피드 사진 최대 장수. */
private const val FEED_IMAGE_LIMIT = 10

private val FeedVisibility.serverValue: String
    get() = when (this) {
        FeedVisibility.Public -> "PUBLIC"
        FeedVisibility.Friends -> "FRIENDS"
        FeedVisibility.Private -> "PRIVATE"
    }

/** POST chat-rooms/{id}/status 의 확정 상태. 다녀온 여행은 확정된 방에서만 나온다. */
private const val CONFIRMED_ROOM_STATUS = "CONFIRMED"

/**
 * 24-1 카드 부제 — `2026.08.28 · 당일치기` / `2026.09.05 ~ 2026.09.06 · 숙박`.
 *
 * 서버 `tripType` 은 `GET /travel-courses/chat-rooms/{roomId}` 의 `room` 래퍼에 있는데
 * 우리 매퍼가 `course` 만 취해 버린다. `endDate` 유무가 같은 값을 주므로 그것으로 만든다 —
 * 당일치기는 `endDate` 가 null 이다.
 */
private fun feedWriteTripSubtitle(room: MyChatRoom): String {
    val start = room.startDate.replace('-', '.')
    val end = room.endDate?.replace('-', '.')
    return if (end == null) "$start · 당일치기" else "$start ~ $end · 숙박"
}

/**
 * 24-1 기록할 코스 고르기 — 끝났고 확정된 내 모임만 후보다.
 *
 * 카드에 적는 제목·소요 시간은 그 여행의 **코스**에서 온다(기획 정본·iOS 와 같다).
 * 코스를 아직 못 읽었으면 방 제목·날짜로 남는다 — 없는 값을 지어내지 않는다.
 */
@Composable
private fun FeedWriteTripSelector(
    rooms: List<MyChatRoom>,
    courses: Map<Long, TravelCourse>,
    selectedRoomId: Long?,
    onSelect: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "기록할 코스",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold
        )
        if (rooms.isEmpty()) {
            // 웹·iOS 와 **글자 그대로** 같은 문구를 쓴다 — 새 문구를 만들면 그게 곧 불일치다.
            // 예전 문구 `아직 공개된 코스가 없어요.` 는 이 화면 맥락(다녀온 여행 고르기)과 달랐다.
            MoyeoEmptyState("아직 다녀온 여행 기록이 없어요.", testTag = "feed-write-no-trip")
            return@Column
        }
        // 고른 코스가 가로 목록 밖에 있으면 화면에는 안 고른 카드만 보인다 — 선택한 카드로 옮겨 둔다
        val rowState = rememberLazyListState()
        val selectedIndex = rooms.indexOfFirst { it.roomId == selectedRoomId }
        LaunchedEffect(selectedIndex) {
            if (selectedIndex >= 0) rowState.animateScrollToItem(selectedIndex)
        }
        LazyRow(
            state = rowState,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(end = 4.dp)
        ) {
            items(rooms, key = { it.roomId }) { room ->
                val selected = room.roomId == selectedRoomId
                Card(
                    modifier = Modifier
                        .width(188.dp)
                        .clickable { onSelect(room.roomId) }
                        .testTag("feed-write-trip-${room.roomId}"),
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
                        // 코스 썸네일이 먼저다 — 없을 때만 방 썸네일로 떨어진다(웹·iOS 와 같은 순서).
                        val cardCourse = courses[room.roomId]
                        CachedRemoteImage(
                            url = cardCourse?.thumbnail ?: room.thumbnail,
                            contentDescription = cardCourse?.title ?: room.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(76.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop,
                            fallbackShape = MoyeoPlaceholderShape.LANDSCAPE
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(76.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                        }
                        val course = cardCourse
                        Text(
                            text = course?.title ?: room.title,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            // `2026.08.28 · 당일치기` — 웹·iOS 와 같은 모양이다.
                            // 예전에는 `10시간`(코스 소요시간)이라 **같은 코스로 떠난 여행 둘을 구분할 수 없었다.**
                            text = feedWriteTripSubtitle(room),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        FeedWriteCoursePill(selected = selected)
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedWriteHeader(
    currentStep: Int,
    canSubmit: Boolean,
    submitted: Boolean,
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val submitEnabled = canSubmit && currentStep >= 5

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
                    .clickable(enabled = submitEnabled, onClick = onSubmit)
                    .padding(horizontal = 9.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = if (submitEnabled) colorScheme.primary else colorScheme.onSurfaceVariant,
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
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.20f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // **입력은 두 칸이다** (기획 24-3·24-5: 굵은 제목 줄 + 본문 줄).
            // 예전에는 본문 칸 하나뿐이고 라벨도 없어 무엇을 쓰는 칸인지 알 수 없었다
            // (사용자 지적 「제목과 내용 입력 뷰가 둘 다」, 2026-09-09).
            // 서버는 아직 `content` 하나만 받는다 — 제목은 본문 첫 줄로 실어 보낸다 (BE §8-7).
            Text(
                text = "제목",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = title,
                onValueChange = { onTitleChange(it.take(FEED_TITLE_LIMIT)) },
                modifier = Modifier.fillMaxWidth().testTag("feed-write-title"),
                placeholder = { Text("이번 여행을 한마디로") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                // 카드 **안**의 입력이라 칸마다 테두리를 두르지 않는다 — 두르면 안드로이드만
                // 「박스 두 개」로 보인다(기획·웹·iOS 는 한 카드에 구분선 하나다).
                colors = feedWriteFieldColors()
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = colorScheme.outline.copy(alpha = 0.4f)
            )
            Text(
                text = "내용",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = story,
                onValueChange = { onStoryChange(it.take(FEED_BODY_LIMIT)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("feed-write-story"),
                placeholder = { Text("여행 어땠는지 한 줄 남겨주세요.") },
                minLines = 5,
                shape = RoundedCornerShape(12.dp),
                colors = feedWriteFieldColors()
            )
            Text(
                text = "${story.length} / %,d".format(FEED_BODY_LIMIT),
                modifier = Modifier.align(Alignment.End),
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

/** 카드 안에 놓는 입력 칸 색 — 테두리·배경을 지운다(카드가 이미 테두리를 갖고 있다). */
@Composable
private fun feedWriteFieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor = Color.Transparent,
    focusedBorderColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    focusedContainerColor = Color.Transparent
)

/** 서버에는 제목 필드가 없다 — 제목은 **본문 첫 줄**로 올려 보낸다 (BE 요청 §8-7). */
private const val FEED_TITLE_LIMIT = 60
private const val FEED_BODY_LIMIT = 500

private fun feedContentWithTitle(title: String, body: String): String {
    val head = title.trim()
    val tail = body.trim()
    if (head.isEmpty()) return tail
    return if (tail.isEmpty()) head else "$head\n\n$tail"
}

/** 고른 사진 그리드. 서버가 실제로 올릴 파일만 보여준다 — 예시 사진 타일을 채우지 않는다. */
@Composable
private fun FeedWritePhotoGrid(images: List<PickedImage>, onAdd: () -> Unit, onRemove: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        images.chunked(2).forEachIndexed { rowIndex, rowImages ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowImages.forEachIndexed { columnIndex, image ->
                    FeedWritePhotoTile(
                        image = image,
                        representative = rowIndex == 0 && columnIndex == 0,
                        onRemove = { onRemove(rowIndex * 2 + columnIndex) },
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp)
                    )
                }
                repeat(2 - rowImages.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        if (images.size < FEED_IMAGE_LIMIT) {
            FeedWriteAddPhotoTile(
                onClick = onAdd,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            )
        }
    }
}

@Composable
private fun FeedWritePhotoTile(
    image: PickedImage,
    representative: Boolean,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val bitmap = remember(image) {
        runCatching { BitmapFactory.decodeByteArray(image.bytes, 0, image.bytes.size) }.getOrNull()
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colorScheme.surfaceVariant)
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
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
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "사진 빼기",
                modifier = Modifier.size(15.dp),
                tint = colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun FeedWriteAddPhotoTile(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = modifier
            .clickable(onClick = onClick)
            // 「담을 자리」는 **점선**이다 — 기획·웹·iOS 모두 점선 박스다(안드만 실선이었다).
            .moyeoDashedOutline(colorScheme.outline.copy(alpha = 0.62f), radius = 10.dp)
            .testTag("feed-write-add-photo"),
        shape = RoundedCornerShape(10.dp),
        color = colorScheme.surface
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

/**
 * 24-5 경로 (자동). 좌표가 있는 방문지만 실제 카카오 지도에 올린다 —
 * 좌표가 하나도 없거나 코스를 못 읽으면 카드째 그리지 않는다(정본 R4).
 */
@Composable
private fun FeedWriteRouteCard(course: TravelCourse?) {
    val points = course?.places?.toRoutePoints().orEmpty()
    if (points.isEmpty()) return
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "경로 (자동)",
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            CourseRouteMap(points = points, height = 118.dp)
            Text(
                text = listOfNotNull(
                    "방문지 ${points.size}곳",
                    course?.distanceKm?.let { "${it}km" }
                ).joinToString(" · "),
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 24-5 최종 확인 요약. 코스·일정은 서버 코스에서, 공개 범위는 STEP 4 에서 고른 값에서 온다.
 * 서버가 주지 않는 줄은 아예 빼고, 값을 지어내지 않는다.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FeedWriteMetaCard(
    course: TravelCourse?,
    visibility: FeedVisibility,
    routeIncluded: Boolean,
    companionCount: Int
) {
    val colorScheme = MaterialTheme.colorScheme
    val schedule = listOfNotNull(course?.travelTime, course?.distanceKm?.let { "${it}km" })
        .joinToString(" · ")
    val tags = listOfNotNull(
        visibility.label,
        "경로지도".takeIf { routeIncluded }
    ) + course?.tags.orEmpty().map { it.name }

    Surface(
        modifier = Modifier.fillMaxWidth().testTag("feed-write-meta"),
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surface,
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.20f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            course?.let { FeedWriteMetaRow(Icons.Filled.Map, "코스", it.title) }
            if (schedule.isNotBlank()) FeedWriteMetaRow(Icons.Filled.Route, "일정", schedule)
            FeedWriteMetaRow(
                Icons.Filled.Visibility,
                "공개",
                if (routeIncluded) "${visibility.label} · 경로지도 포함" else visibility.label
            )
            // 웹·iOS 는 이 줄이 있는데 안드로이드만 없었다. 0명이면 `0명` 이 사실이다.
            FeedWriteMetaRow(Icons.Filled.Groups, "멤버", "함께한 멤버 ${companionCount}명")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tags.forEach { tag ->
                    Surface(shape = RoundedCornerShape(50), color = MoyeoTheme.tints.primaryTint) {
                        Text(
                            text = tag,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MoyeoTheme.tints.onPrimaryTint,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedWriteMetaRow(icon: ImageVector, label: String, value: String) {
    val colorScheme = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp), tint = colorScheme.onSurfaceVariant)
        Text(
            text = label,
            modifier = Modifier.width(40.dp),
            style = MaterialTheme.typography.labelMedium,
            color = colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            color = colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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
private fun FeedWritePreviewCard(story: String, submitted: Boolean) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = colorScheme.surface,
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.28f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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

/** 24-1 코스 카드의 상태 알약 — `선택됨` / `이 코스로 기록` (기획 정본·iOS 와 같은 문구). */
@Composable
private fun FeedWriteCoursePill(selected: Boolean) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) colorScheme.primary else colorScheme.primaryContainer
    ) {
        Text(
            text = if (selected) "선택됨" else "이 코스로 기록",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) colorScheme.onPrimary else colorScheme.primary,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

/**
 * 24-1 함께 간 멤버 — `GET chat-rooms/{roomId}/companions` 가 유일한 근거다.
 *
 * 동행자가 0명이면 `(0)` 이 사실이다(혼자 다녀온 여행이 실제로 있다). 예시 멤버를 지어내지 않는다.
 * 서버 응답에는 "나"가 들어 있지 않아 기획의 `(나)` 칩은 그리지 않는다.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FeedWriteMembersCard(companions: List<RoomCompanion>) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("feed-write-members"),
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surface,
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.20f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "함께 간 멤버 (${companions.size})",
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            // 0명이면 제목의 `(0)` 이 곧 사실이다 — 웹도 칩 없이 제목만 남긴다. 문구를 지어내지 않는다.
            if (companions.isEmpty()) return@Column
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                companions.forEach { companion ->
                    Surface(shape = RoundedCornerShape(50), color = colorScheme.surfaceVariant) {
                        Row(
                            modifier = Modifier.padding(start = 6.dp, top = 6.dp, end = 10.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            UserAvatar(
                                imageUrl = companion.profileImageUrl,
                                nickname = companion.nickname,
                                modifier = Modifier.size(24.dp),
                                fallbackFontSize = 12.sp
                            )
                            Text(
                                text = companion.nickname,
                                style = MaterialTheme.typography.labelMedium,
                                color = colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
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
                    .clip(RoundedCornerShape(MOYEO_CTA_RADIUS))
                    .clickable(onClick = onPrevious)
                    // 「이전」 자리를 기획·웹과 같은 폭으로 잡는다 — 예전에는 글자 폭이라
                    // 안드로이드만 초록 버튼이 왼쪽으로 18pt 더 나와 있었다
                    // (측정: `docs/ui-comparison/BUTTON-GEOMETRY.md` 24-1~24-5).
                    .widthIn(min = 56.dp)
                    .padding(horizontal = 6.dp, vertical = 12.dp),
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(1f)
                    .testTag("feed-write-next")
                    .height(MOYEO_CTA_HEIGHT),
                shape = RoundedCornerShape(MOYEO_CTA_RADIUS),
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

/**
 * 화면기획 12 검색 — 서버 모집 검색(GET chat-rooms/search?keyword=)이다.
 *
 * 인기 검색어는 `GET /api/v1/search/popular-keywords` 가 근거다(2026-08-30 서버 추가).
 * 서버가 주는 것은 `rank`·`keyword`·`searchCount` 뿐이라 그 셋만 그린다 —
 * 화면기획의 상승·하락 화살표는 목데이터였다. 0건이면 **섹션 자체를 그리지 않는다**
 * (집계 전이거나 Redis 가 죽은 경우다 — 새 빈 상태 문구를 만들지 않는다).
 * 최근 검색어는 서버에 없는 기기 기능이라 그대로 기기에 저장한다.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onOpenRoom: (Long) -> Unit,
    initialQuery: String = "",
    /** 12-1 검색 결과로 넘긴다. 검색어를 **확정**했을 때만 부른다 — 글자마다 화면을 열지 않는다. */
    onSubmitQuery: (String) -> Unit = {}
) {
    var query by rememberSaveable { mutableStateOf(initialQuery) }
    var submittedQuery by rememberSaveable { mutableStateOf(initialQuery) }
    val context = LocalContext.current
    val server = LocalServerData.current
    val recentSearchStore = remember(context) { PersistedRecentSearchStore(context.applicationContext) }
    val recentSearches by recentSearchStore.keywords.collectAsState()
    var results by remember(server) {
        mutableStateOf<ServerListState<ChatRoomSearchResult>>(
            ServerListState.Loaded(emptyList())
        )
    }
    // 인기 검색어는 실패도 0건도 "섹션 없음"이다 — 로딩·오류 문구를 따로 그리지 않는다.
    var popularKeywords by remember(server) { mutableStateOf<List<PopularKeyword>>(emptyList()) }
    var reloadKey by remember { mutableIntStateOf(0) }
    val runSearch: (String) -> Unit = { keyword ->
        val trimmed = keyword.trim()
        if (trimmed.isNotEmpty()) {
            query = trimmed
            submittedQuery = trimmed
            recentSearchStore.record(trimmed)
            // 결과는 12-1 이 그린다 — 코스·모집 두 탭을 함께 보여주는 화면이다
            onSubmitQuery(trimmed)
        }
    }
    val trimmedQuery = submittedQuery.trim()
    LaunchedEffect(server) {
        popularKeywords = if (server == null) {
            emptyList()
        } else {
            runCatching { server.popularKeywords.popularKeywords() }.getOrDefault(emptyList())
        }
    }
    LaunchedEffect(server, trimmedQuery, reloadKey) {
        if (server == null || trimmedQuery.isBlank()) {
            results = ServerListState.Loaded(emptyList())
            return@LaunchedEffect
        }
        results = ServerListState.Loading
        results = runCatching { server.chatRooms.search(keyword = trimmedQuery) }
            .fold({ ServerListState.Loaded(it) }, { ServerListState.Failed })
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
                // 안내 문구는 네 표면이 같아야 한다 — 11 탐색의 진입 칸과도 같은 말이다.
                placeholder = { Text("어디로 떠나고 싶나요?") },
                singleLine = true,
                // 다른 세 표면은 **테두리 없는 회색 칸**이다 (기획 `T.bgSubtle` · 반지름 12).
                // 기본 `OutlinedTextField` 는 흰 칸에 선이 둘려 안드로이드만 달라 보였다.
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                // 검색을 실행한 순간에만 최근 검색어에 남긴다 — 글자마다 쌓으면 목록이 조각난다
                keyboardActions = KeyboardActions(onSearch = { runSearch(query) })
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
            if (trimmedQuery.isBlank()) {
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
                        if (recentSearches.isNotEmpty()) {
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
                }
                if (recentSearches.isEmpty()) {
                    item {
                        MoyeoEmptyState(
                            MoyeoEmptyText.NO_RECENT_SEARCHES,
                            testTag = "search-recent-empty",
                            // iOS 의 `clock.arrow.circlepath` 와 같은 뜻의 아이콘 (2026-09-09 네 표면 통일)
                            icon = Icons.Outlined.History
                        )
                    }
                } else {
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
                // 인기 검색어 — 0건이면 머리글도 목록도 그리지 않는다(정본 §2-1).
                if (popularKeywords.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search-popular-section"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 글자 크기는 화면기획 12 를 따른다 (제목 13 · 줄 14 · 등락 11)
                            Text(
                                text = "인기 검색어",
                                modifier = Modifier.weight(1f),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            // 등락의 기준 시점. 서버 집계가 **전일 순위 대비**다 (API 스펙).
                            Text(
                                text = "전일 대비",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    items(popularKeywords, key = { it.rank }) { row ->
                        PopularKeywordRow(row = row, onClick = { runSearch(row.keyword) })
                    }
                }
                return@LazyColumn
            }
            val state = results
            when {
                server == null -> item {
                    MoyeoEmptyState(MoyeoEmptyText.SIGN_IN_SEARCH, testTag = "search-signed-out")
                }

                state is ServerListState.Loading -> item { MoyeoEmptyState(MoyeoEmptyText.LOADING) }

                state is ServerListState.Failed -> item {
                    MoyeoEmptyState(MoyeoEmptyText.FAILED, onRetry = { reloadKey++ })
                }

                state is ServerListState.Loaded && state.items.isEmpty() -> item {
                    MoyeoEmptyState(MoyeoEmptyText.NO_SEARCH_RESULTS, testTag = "search-empty-state")
                }

                state is ServerListState.Loaded -> items(state.items, key = { it.roomId }) { room ->
                    SupportCard(modifier = Modifier.clickable { onOpenRoom(room.roomId) }) {
                        SearchRoomRow(room = room)
                    }
                }
            }
        }
    }
}

/**
 * 인기 검색어 한 줄 — 순위 · 검색어 · 등락.
 *
 * 등락은 서버가 준 `rankTrend`/`rankChange` 뿐이다(기준은 전일 순위). 검색 횟수는 감춘다 —
 * QA 검색이 만든 숫자가 실제 인기와 무관해 보였다(사용자 결정 2026-09-09).
 * 줄 높이 44 · 간격 16 · 순위 3위까지 초록은 화면기획 12 를 그대로 따른 값이다.
 */
@Composable
private fun PopularKeywordRow(row: PopularKeyword, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clickable(onClick = onClick)
            .testTag("search-popular-${row.rank}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = row.rank.toString(),
            modifier = Modifier.width(16.dp),
            fontSize = 14.sp,
            // 3위까지만 초록으로 물들인다 — 기획이 그렇게 나눈다.
            color = if (row.rank <= 3) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = FontWeight.Bold
        )
        Text(
            text = row.keyword,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        popularRankTrendMark(row)?.let { mark ->
            Text(
                text = mark.text,
                modifier = Modifier
                    .testTag("search-popular-trend-${row.rank}")
                    .semantics { contentDescription = mark.label },
                fontSize = 11.sp,
                color = mark.color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** 등락 글자·색·읽어줄 말. */
private data class RankTrendMark(val text: String, val color: Color, val label: String)

/**
 * 등락 표시를 고른다. 서버가 등락을 주지 않은 줄은 `null` 이고 **그 자리를 비운다** —
 * 지어내지 않는다(정본 R1).
 */
@Composable
private fun popularRankTrendMark(row: PopularKeyword): RankTrendMark? {
    val step = abs(row.rankChange ?: 0)
    return when (row.rankTrend) {
        RankTrend.UP -> RankTrendMark(
            text = if (step > 0) "▲$step" else "▲",
            color = MaterialTheme.colorScheme.secondary,
            label = "전일 대비 ${step}단계 상승"
        )

        RankTrend.DOWN -> RankTrendMark(
            text = if (step > 0) "▼$step" else "▼",
            color = MoyeoTheme.tints.onInfoTint,
            label = "전일 대비 ${step}단계 하락"
        )

        RankTrend.SAME -> RankTrendMark(
            text = "—",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            label = "전일과 같은 순위"
        )

        RankTrend.NEW -> RankTrendMark(
            text = "NEW",
            color = MaterialTheme.colorScheme.primary,
            label = "새로 진입"
        )

        RankTrend.UNKNOWN -> null
    }
}

/** 검색 결과 한 줄 — 서버 검색 응답이 주는 값만 쓴다. */
@Composable
private fun SearchRoomRow(room: ChatRoomSearchResult) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CachedRemoteImage(
            url = room.thumbnail,
            contentDescription = room.title,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
            fallbackShape = MoyeoPlaceholderShape.SQUARE
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                text = room.title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            val meta = listOfNotNull(
                room.courseTitle,
                room.startDate.takeIf(String::isNotBlank)?.replace('-', '.'),
                "${room.participantCount}/${room.maxParticipants}명"
            ).joinToString(" · ")
            Text(
                text = meta,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
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

/** 강퇴 일시 — iOS 와 **같은 형식**이다: `2026.08.24 (월) 오전 8:06 · 호스트 결정`. */
private fun String.kickedAtLabel(): String = runCatching {
    val formatted = LocalDateTime.parse(this)
        .format(DateTimeFormatter.ofPattern("yyyy.MM.dd (E) a h:mm", java.util.Locale.KOREAN))
    "$formatted · 호스트 결정"
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
    // 알림 한 줄의 시각은 **상대 시각**이다 (`3시간 전`). 웹이 그렇게 쓰고 있었는데
    // 앱 둘만 `HH:mm` · `M월 d일` 처럼 절대 시각이었다 (사용자 지적, 2026-09-09).
    // 피드·댓글과 **같은 부품**을 쓴다 — 화면마다 반올림이 다르면 같은 시각이 갈린다.
    val timeLabel = moyeoRelativeTime(createdAt)
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
