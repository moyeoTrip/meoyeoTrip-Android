package kr.hanchae.moyeotrip.ui.screens

import android.content.Context
import android.net.Uri
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.hanchae.moyeotrip.data.ServerDataDependencies
import kr.hanchae.moyeotrip.data.api.MoyeoApiException
import kr.hanchae.moyeotrip.data.courses.TravelCourse
import kr.hanchae.moyeotrip.data.feed.FeedComment
import kr.hanchae.moyeotrip.data.feed.FeedReportReason
import kr.hanchae.moyeotrip.data.feed.ServerFeed
import kr.hanchae.moyeotrip.data.notifications.NotificationSettingsUpdate
import kr.hanchae.moyeotrip.data.profile.ServerUserProfile
import kr.hanchae.moyeotrip.data.rooms.ChatRoomDetail
import kr.hanchae.moyeotrip.data.rooms.RoadmapPlace
import kr.hanchae.moyeotrip.data.rooms.RoomCompanion
import kr.hanchae.moyeotrip.data.rooms.RoomMember
import kr.hanchae.moyeotrip.data.rooms.RoomMembers
import kr.hanchae.moyeotrip.data.rooms.RoomMessage
import kr.hanchae.moyeotrip.data.rooms.RoomNotices
import kr.hanchae.moyeotrip.data.rooms.RoomRoadmap
import kr.hanchae.moyeotrip.data.rooms.recruitmentDDayText
import kr.hanchae.moyeotrip.ui.LocalServerData
import kr.hanchae.moyeotrip.ui.MoyeoContact
import kr.hanchae.moyeotrip.ui.components.CachedRemoteImage
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyState
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyText
import kr.hanchae.moyeotrip.ui.components.MoyeoPlaceholderShape
import kr.hanchae.moyeotrip.ui.components.OverlayBackdrop
import kr.hanchae.moyeotrip.ui.components.emphasized
import kr.hanchae.moyeotrip.ui.navigation.AppRoutes
import kr.hanchae.moyeotrip.ui.theme.MoyeoTheme

private data class MenuEntry(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val danger: Boolean = false,
    /** 화면기획 20-1 알림 설정 행처럼 우측에 토글이 붙는 행만 채운다. 없으면 셰브런이다. */
    val trailing: (@Composable () -> Unit)? = null,
    val onClick: () -> Unit
)

private data class FriendEntry(
    val emoji: String,
    val name: String,
    val subtitle: String,
    // 실서버 사용자면 프로필 이미지 URL·신청 id·사용자 id 를 함께 든다
    val imageUrl: String? = null,
    val requestId: Long? = null,
    val userId: Long? = null
)

/** 방해금지 요일 — 서버 enum(MONDAY…)과 화면 라벨(월…) 대응. */
private val apiDayToKorean = mapOf(
    "MONDAY" to "월",
    "TUESDAY" to "화",
    "WEDNESDAY" to "수",
    "THURSDAY" to "목",
    "FRIDAY" to "금",
    "SATURDAY" to "토",
    "SUNDAY" to "일"
)

private val koreanDayToApi = apiDayToKorean.entries.associate { (api, korean) -> korean to api }

/** 실서버 친구 관리 데이터 (GET friends · friend-requests/received · /sent). */
private data class ServerFriendLists(
    val friends: List<kr.hanchae.moyeotrip.data.social.Friend>,
    val received: List<kr.hanchae.moyeotrip.data.social.FriendRequest>,
    val sent: List<kr.hanchae.moyeotrip.data.social.FriendRequest>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangeLogScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = { actions() },
                windowInsets = WindowInsets(0.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = bottomBar,
        content = content
    )
}

@Composable
private fun RoundedPanel(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(14.dp), content = content)
    }
}

@Composable
private fun ActionRow(entry: MenuEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clickable(role = Role.Button, onClick = entry.onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val tint = if (entry.danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        Icon(entry.icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = tint)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                entry.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (entry.danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text(
                entry.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        val trailing = entry.trailing
        if (trailing == null) {
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = tint)
        } else {
            trailing()
        }
    }
}

@Composable
fun ChatMenuScreen(
    threadId: String,
    onBack: () -> Unit,
    onOpenSpecialMessages: () -> Unit,
    /** 20-1c 이 모임 알림. 전역 방해금지(29-2)가 아니라 **이 방의** 설정으로 간다. */
    onOpenNotificationSettings: (String) -> Unit,
    onOpenNotices: (String) -> Unit,
    onOpenRoute: (String) -> Unit,
    /**
     * 20-1a 멤버 액션 · 20-1b 내보내기 사유는 20-1 위에 뜨는 **시트**다.
     *
     * 두 화면 번호로 들어왔을 때는 시트가 열린 상태로 시작한다 — 예전에는 20-1 목록만 그려져
     * "시트가 없는 화면"이 찍혔다. 웹도 20-1b 를 20-1 본문 위에 시트를 얹은 별도 라우트로 둔다.
     */
    initialSheet: ChatMenuSheet = ChatMenuSheet.None
) {
    // "room-{id}" 는 실서버 모임이다 — 서버 멤버·공지·로드맵을 읽어 보여준다
    val server = LocalServerData.current
    val serverRoomId = threadId.serverRoomIdOrNull()
    if (serverRoomId == null || server == null) {
        ChangeLogScaffold(
            title = "모임 정보",
            onBack = onBack,
            modifier = Modifier.testTag("chat-menu-screen")
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                MoyeoEmptyState(MoyeoEmptyText.NO_JOINED_ROOMS, testTag = "chat-menu-empty")
            }
        }
        return
    }
    ServerChatMenu(
        roomId = serverRoomId,
        server = server,
        onBack = onBack,
        onOpenNotices = onOpenNotices,
        onOpenRoute = onOpenRoute,
        onOpenSpecialMessages = onOpenSpecialMessages,
        onOpenNotificationSettings = onOpenNotificationSettings,
        initialSheet = initialSheet
    )
}

/** 20-1 위에 얹힌 시트 중 어느 것으로 열 것인지. */
enum class ChatMenuSheet { None, MemberActions, MemberRemove }

/**
 * 실서버 모임 정보(화면기획 20-1) — GET chat-rooms/{id} · {id}/members · {id}/notices · {id}/roadmap/current.
 * 서버가 주지 않는 값(멤버 매너 점수·대기 큐 상세·공유된 항목 수)은 표시하지 않는다.
 *
 * 이 모임 알림은 GET·PUT notifications/settings/chat-rooms/{roomId} 로 토글한다(화면기획 20-1 우측 토글).
 * 나가기는 DELETE {id}/members/me, 내보내기는 DELETE {id}/members/{memberId} 로 보낸다 —
 * 둘 다 되돌릴 수 없어 확인 단계(화면기획 31 · 20-1b)를 지난 뒤에만 호출한다.
 */
@Composable
private fun ServerChatMenu(
    roomId: Long,
    server: ServerDataDependencies,
    onBack: () -> Unit,
    onOpenNotices: (String) -> Unit,
    onOpenRoute: (String) -> Unit,
    onOpenSpecialMessages: () -> Unit,
    onOpenNotificationSettings: (String) -> Unit,
    initialSheet: ChatMenuSheet
) {
    var detail by remember(roomId) { mutableStateOf<ChatRoomDetail?>(null) }
    var members by remember(roomId) { mutableStateOf<RoomMembers?>(null) }
    var notices by remember(roomId) { mutableStateOf<RoomNotices?>(null) }
    var roadmap by remember(roomId) { mutableStateOf<RoomRoadmap?>(null) }
    var actionTarget by remember(roomId) { mutableStateOf<RoomMember?>(null) }
    var removeTarget by remember(roomId) { mutableStateOf<RoomMember?>(null) }
    var actionMessage by remember(roomId) { mutableStateOf<String?>(null) }
    // 20-1a 의 "친구 요청하기" 는 한 번만 보낼 수 있다 — 시트를 닫으면 다시 Idle 로 돌아간다.
    var friendRequestState by remember(roomId, actionTarget) { mutableStateOf(FriendRequestState.Idle) }
    var friendRequestError by remember(roomId, actionTarget) { mutableStateOf<String?>(null) }
    var roomAlertsEnabled by remember(roomId) { mutableStateOf<Boolean?>(null) }
    var showLeaveConfirm by remember(roomId) { mutableStateOf(false) }
    // 20-1 「신고 · 문의」 — 접수 API 가 없어 누른 자리에서 안내한다(정본 §3).
    var showReportNotice by remember(roomId) { mutableStateOf(false) }
    var leaveBusy by remember(roomId) { mutableStateOf(false) }
    val actionScope = rememberCoroutineScope()
    val me = members?.members?.firstOrNull(RoomMember::me)
    val amHost = me?.host == true

    LaunchedEffect(roomId, server) {
        detail = runCatching { server.chatRooms.room(roomId) }.getOrNull()
        members = runCatching { server.chatRooms.members(roomId) }.getOrNull()
        notices = runCatching { server.chatRooms.notices(roomId) }.getOrNull()
        roadmap = runCatching { server.chatRooms.currentRoadmap(roomId) }.getOrNull()
        roomAlertsEnabled = runCatching { server.notifications.roomSetting(roomId) }.getOrNull()?.enabled
    }

    // 20-1a·20-1b 로 들어왔으면 멤버가 도착한 뒤 그 시트를 연다. 대상은 **내가 아닌 다른 참가자**다 —
    // 호스트를 내보내는 시트는 성립하지 않으므로 호스트도 건너뛴다. 대상이 없으면 시트를 열지 않는다
    // (없는 멤버를 지어내지 않는다).
    LaunchedEffect(initialSheet, members) {
        if (initialSheet == ChatMenuSheet.None) return@LaunchedEffect
        val candidate = members?.members?.firstOrNull { !it.me && !it.host } ?: return@LaunchedEffect
        when (initialSheet) {
            ChatMenuSheet.MemberActions -> if (removeTarget == null) actionTarget = candidate

            ChatMenuSheet.MemberRemove -> {
                actionTarget = null
                removeTarget = candidate
            }

            ChatMenuSheet.None -> Unit
        }
    }

    ChangeLogScaffold(
        title = "모임 정보",
        onBack = onBack,
        modifier = Modifier.testTag("chat-menu-screen")
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                    Text(
                        detail?.title.orEmpty(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    detail?.let { room ->
                        val dates = listOfNotNull(room.startDate.takeIf(String::isNotBlank), room.endDate)
                            .joinToString(" ~ ") { it.replace('-', '.') }
                        val hours = listOfNotNull(room.dayTripStartTime, room.dayTripEndTime)
                            .map { it.take(5) }
                            .takeIf { it.size == 2 }
                            ?.joinToString(" – ")
                        Text(
                            listOfNotNull(dates.takeIf(String::isNotBlank), hours).joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                        val chips = buildList {
                            room.participationFee?.let { add("1인 ${"%,d".format(it)}원") }
                            recruitmentDDayText(room.recruitmentDDay)?.let { add("마감 $it") }
                            if (room.minimumAge != null || room.maximumAge != null) {
                                add("${room.minimumAge ?: ""}~${room.maximumAge ?: ""}세")
                            }
                        }
                        if (chips.isNotEmpty()) {
                            Row(
                                modifier = Modifier.padding(top = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                chips.forEach { label ->
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            label,
                                            Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                        room.meetingDetails?.let { meeting ->
                            Text(
                                meeting,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
                HorizontalDivider(thickness = 8.dp, color = MaterialTheme.colorScheme.surfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("동행자  ${members?.members?.size ?: 0}", fontWeight = FontWeight.ExtraBold)
                    members?.let {
                        Text(
                            "최대 ${it.maxParticipants}명 · 대기 ${it.waitlistCount}명",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            items(members?.members.orEmpty(), key = { it.userId }) { member ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    UserAvatar(
                        imageUrl = member.profileImageUrl,
                        nickname = member.nickname,
                        modifier = Modifier.size(42.dp),
                        fallbackFontSize = 19.sp
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(member.nickname, fontWeight = FontWeight.Bold)
                        Text(
                            "여행 ${member.completedTripCount}회",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    val role = when {
                        member.host -> "호스트"
                        member.me -> "나"
                        else -> ""
                    }
                    if (role.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (role == "호스트") {
                                MoyeoTheme.tints.primaryTint
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            border = BorderStroke(
                                1.dp,
                                if (role == "호스트") {
                                    MaterialTheme.colorScheme.primary.copy(alpha = .4f)
                                } else {
                                    MaterialTheme.colorScheme.outline
                                }
                            )
                        ) {
                            Text(
                                role,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (role == "호스트") {
                                    MoyeoTheme.tints.onPrimaryTint
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { actionTarget = member },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Filled.MoreHoriz, contentDescription = "${member.nickname} 관리")
                        }
                    }
                }
            }
            actionMessage?.let { message ->
                item {
                    Text(
                        message,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            val places = roadmap?.places.orEmpty()
            if (places.isNotEmpty()) {
                item {
                    HorizontalDivider(thickness = 8.dp, color = MaterialTheme.colorScheme.surfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("여행 경로  ${places.size}", fontWeight = FontWeight.ExtraBold)
                        roadmap?.let { current ->
                            val dayLabel = current.dayNumber?.let { "Day $it / ${current.totalDays}" }
                            Text(
                                dayLabel ?: "총 ${current.totalDays}일",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                items(places, key = { "place-${it.sequence}-${it.contentId}" }) { place ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "${place.sequence}",
                            modifier = Modifier.size(22.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        Text(place.title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        place.scheduledAt?.let { scheduled ->
                            Text(
                                scheduled.takeLast(8).take(5),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            item {
                HorizontalDivider(thickness = 8.dp, color = MaterialTheme.colorScheme.surfaceVariant)
                val noticeCount = notices?.all?.size ?: 0
                val pinnedCount = notices?.pinned?.size ?: 0
                val alerts = roomAlertsEnabled
                listOf(
                    MenuEntry(
                        Icons.AutoMirrored.Filled.StickyNote2,
                        "공지",
                        "고정 ${pinnedCount}개 · 전체 ${noticeCount}개"
                    ) { onOpenNotices("room-$roomId") },
                    MenuEntry(
                        Icons.Filled.Map,
                        "여행 경로",
                        "방문지와 집합 정보를 확인해요"
                    ) { onOpenRoute("room-$roomId") },
                    // 21 특수 메시지 — 사진·장소·투표·정산 카드를 모아 본다
                    MenuEntry(
                        Icons.Filled.Image,
                        "공유된 항목",
                        "사진·장소·투표·정산 카드",
                        onClick = onOpenSpecialMessages
                    ),
                    MenuEntry(
                        Icons.Filled.Notifications,
                        "알림 설정",
                        "이 모임의 알림만 끄기",
                        // 서버 설정을 못 읽었으면 토글을 만들지 않고 기존 셰브런(세부 설정 진입)으로 둔다
                        trailing = alerts?.let { enabled ->
                            {
                                Switch(
                                    checked = enabled,
                                    onCheckedChange = { next ->
                                        roomAlertsEnabled = next
                                        actionScope.launch {
                                            runCatching { server.notifications.updateRoomSetting(roomId, next) }
                                                .onSuccess { saved -> roomAlertsEnabled = saved.enabled }
                                                .onFailure { error ->
                                                    roomAlertsEnabled = !next
                                                    actionMessage = error.message ?: "알림 설정을 바꾸지 못했어요."
                                                }
                                        }
                                    },
                                    modifier = Modifier.testTag("server-room-alerts-toggle")
                                )
                            }
                        },
                        onClick = { onOpenNotificationSettings("room-$roomId") }
                    ),
                    // 채팅방·멤버 신고는 접수 API 가 없다 — 화면을 옮기지 않고 그 자리에서 안내한다.
                    // 차단은 20-1a 멤버 액션에서 한다(대상이 확실한 자리 · 정본 §3).
                    MenuEntry(
                        Icons.Filled.Flag,
                        "신고 · 문의",
                        "부적절한 대화는 GitHub 이슈나 이메일로 알려주세요",
                        onClick = { showReportNotice = true }
                    )
                ).forEach { ActionRow(it) }
                HorizontalDivider(thickness = 8.dp, color = MaterialTheme.colorScheme.surfaceVariant)
                ActionRow(
                    MenuEntry(
                        Icons.Filled.Close,
                        "채팅방 나가기",
                        if (amHost) "호스트가 나가면 이 모임은 종료돼요" else "나가면 대기 중인 다음 신청자가 자동으로 합류해요",
                        danger = true
                    ) { showLeaveConfirm = true }
                )
            }
        }
    }
    if (showReportNotice) {
        ReportUnsupportedDialog(onDismiss = { showReportNotice = false })
    }
    val target = actionTarget
    if (target != null) {
        // 화면기획 20-1a·웹과 같은 **바텀시트**다. 예전에는 여기에 AlertDialog 를 띄워
        // 20-1a 로 만들어 둔 [MemberActionsSheet] 가 아무 데서도 쓰이지 않았고,
        // 그 안의 "친구 요청하기" 는 빈 onClick 이었다(감사 도구가 잡은 자리).
        MemberActionsSheet(
            member = FriendEntry(
                emoji = "🙂",
                name = target.nickname,
                subtitle = "여행 ${target.completedTripCount}회",
                imageUrl = target.profileImageUrl,
                userId = target.userId
            ),
            friendRequestState = friendRequestState,
            friendRequestError = friendRequestError,
            onSendFriendRequest = {
                if (friendRequestState == FriendRequestState.Idle) {
                    friendRequestState = FriendRequestState.Sending
                    friendRequestError = null
                    actionScope.launch {
                        runCatching { server.social.sendRequest(target.userId) }
                            .onSuccess {
                                friendRequestState = FriendRequestState.Sent
                                actionMessage = "${target.nickname}님에게 친구 신청을 보냈어요."
                            }
                            .onFailure { error ->
                                friendRequestState = FriendRequestState.Idle
                                // 문구는 웹 20-1a 와 같은 것을 쓴다
                                friendRequestError = error.message ?: "친구 요청을 보내지 못했어요."
                            }
                    }
                }
            },
            // 신고 접수 API 는 서버에 없다(웹 30-2 도 같은 이유로 차단만 반영한다) —
            // 대상이 확실한 이 경로에서 차단만 실제로 보낸다.
            onBlock = {
                actionTarget = null
                actionScope.launch {
                    runCatching { server.social.block(target.userId) }
                        .onSuccess {
                            actionMessage = "${target.nickname}님을 차단했어요."
                            members = runCatching { server.chatRooms.members(roomId) }.getOrNull() ?: members
                        }
                        .onFailure { error -> actionMessage = error.message ?: "차단에 실패했어요." }
                }
            },
            onRemove = {
                removeTarget = target
                actionTarget = null
            },
            onDismiss = { actionTarget = null }
        )
    }
    // 20-1b 사유 입력 시트 — 확인에서만 서버를 부른다
    removeTarget?.let { member ->
        MemberRemoveSheet(
            member = FriendEntry(
                emoji = "🙂",
                name = member.nickname,
                subtitle = "여행 ${member.completedTripCount}회",
                imageUrl = member.profileImageUrl
            ),
            onDismiss = { removeTarget = null },
            onConfirm = { reason ->
                removeTarget = null
                actionScope.launch {
                    runCatching { server.chatRooms.kickMember(roomId, member.userId, reason) }
                        .onSuccess {
                            actionMessage = "${member.nickname}님을 내보냈어요."
                            members = runCatching { server.chatRooms.members(roomId) }.getOrNull() ?: members
                        }
                        .onFailure { error -> actionMessage = error.message ?: "내보내기에 실패했어요." }
                }
            }
        )
    }
    if (showLeaveConfirm) {
        // 화면기획 31 · 31-1 — 호스트와 참가자는 결과가 전혀 다르다. 문구는 정본 한 곳에서 온다.
        val leaveCopy = LeaveCopy.of(amHost)
        AlertDialog(
            onDismissRequest = { showLeaveConfirm = false },
            title = { Text(leaveCopy.spokenTitle()) },
            text = { Text(leaveCopy.description) },
            confirmButton = {
                TextButton(
                    enabled = !leaveBusy,
                    onClick = {
                        leaveBusy = true
                        actionScope.launch {
                            runCatching { server.chatRooms.leaveRoom(roomId) }
                                .onSuccess {
                                    showLeaveConfirm = false
                                    onBack()
                                }
                                .onFailure { error ->
                                    showLeaveConfirm = false
                                    actionMessage = error.message ?: "나가기에 실패했어요."
                                }
                            leaveBusy = false
                        }
                    },
                    modifier = Modifier.testTag("server-room-leave-confirm")
                ) {
                    Text(leaveCopy.confirm, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveConfirm = false }) { Text("취소") }
            }
        )
    }
}

/**
 * 오버레이(31 모임 종료 경고) 배경으로 쓰는 20-1 채팅방 사이드 메뉴 본문 — changeLog14 `ChatMenuBody`.
 *
 * 화면과 오버레이 배경이 같은 코드를 쓰도록 [ChatMenuScreen]을 그대로 재사용한다.
 * 상호작용은 위에 얹히는 스크림이 차단하므로 콜백은 비워 둔다.
 */
@Composable
internal fun ChatMenuBody(threadId: String = OVERLAY_BACKDROP_THREAD_ID) {
    ChatMenuScreen(
        threadId = threadId,
        onBack = {},
        onOpenSpecialMessages = {},
        onOpenNotificationSettings = {},
        onOpenNotices = {},
        onOpenRoute = {}
    )
}

/** 20-1a "친구 요청하기" 행의 세 상태. 웹과 같은 라벨을 쓴다. */
private enum class FriendRequestState { Idle, Sending, Sent }

// / 20-1a 멤버 액션 시트 (changeLog14) — ⋯의 첫 단계.
// / 내보내기 행은 호스트에게만 보인다는 전제이고, 캡처 화면에서는 항상 표시한다.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemberActionsSheet(
    member: FriendEntry,
    friendRequestState: FriendRequestState,
    friendRequestError: String?,
    onSendFriendRequest: () -> Unit,
    onBlock: () -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MoyeoTheme.sheetSurface,
        modifier = Modifier.testTag("member-actions-sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 20-1 목록과 같은 아바타다 — 서버가 준 프로필 이미지가 있으면 그것을 쓴다.
                // 이모지 폴백을 그대로 두면 목록에는 실제 사진이, 시트에는 🙂 가 보였다.
                UserAvatar(
                    imageUrl = member.imageUrl,
                    nickname = member.name,
                    modifier = Modifier.size(40.dp),
                    fallbackFontSize = 18.sp
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(member.name, fontWeight = FontWeight.ExtraBold)
                    Text(
                        text = member.subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            MemberActionRow(
                icon = Icons.Outlined.AccountCircle,
                // POST users/me/friend-requests/{userId}. 라벨은 웹 20-1a 와 같다.
                title = when (friendRequestState) {
                    FriendRequestState.Idle -> "친구 요청하기"
                    FriendRequestState.Sending -> "보내는 중..."
                    FriendRequestState.Sent -> "친구 요청을 보냈어요"
                },
                iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                titleColor = if (friendRequestState == FriendRequestState.Sent) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                tag = "member-actions-friend",
                actionable = friendRequestState == FriendRequestState.Idle,
                onClick = onSendFriendRequest
            )
            friendRequestError?.let { message ->
                // 서버가 거절한 이유를 시트 안에서 보여준다. 시트 뒤 목록에 적으면 딤에 가려 안 보인다.
                Text(
                    message,
                    modifier = Modifier.padding(start = 34.dp, bottom = 8.dp)
                        .testTag("member-actions-friend-error"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            MemberActionRow(
                icon = Icons.Filled.Flag,
                // 신고 접수는 서버가 받지 않는다 — 실제로 반영되는 것은 차단뿐이라 행 이름도 차단이다.
                title = "차단하기",
                iconTint = MaterialTheme.colorScheme.error,
                titleColor = MaterialTheme.colorScheme.error,
                tag = "member-actions-block",
                onClick = onBlock
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            MemberActionRow(
                icon = Icons.Filled.WarningAmber,
                title = "내보내기",
                iconTint = MaterialTheme.colorScheme.error,
                titleColor = MaterialTheme.colorScheme.error,
                tag = "member-actions-remove",
                onClick = onRemove
            )
            Text(
                text = "내보내기는 호스트에게만 보여요.",
                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("member-actions-close"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text("닫기", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// / 멤버 액션 시트의 한 행 — 아이콘 + 제목 + 셰브론
@Composable
private fun MemberActionRow(
    icon: ImageVector,
    title: String,
    iconTint: Color,
    titleColor: Color,
    tag: String,
    onClick: () -> Unit,
    /** 아직 누를 수 있는 행인지. 보내는 중·이미 보낸 뒤에는 셰브런도 지운다. */
    actionable: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clickable(role = Role.Button, enabled = actionable, onClick = onClick)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = iconTint)
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Bold,
            color = titleColor
        )
        if (actionable) {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// / 20-1b 멤버 내보내기 사유 시트 (changeLog14) — 사유는 자유 서술 하나(10자 이상)이고,
// / 입력 전에는 내보내기가 비활성이다. 이 사유가 그대로 상대의 13-1 내보내기 안내에 보인다.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemberRemoveSheet(
    member: FriendEntry,
    onDismiss: () -> Unit,
    /** 실서버 방에서만 채운다 — 방을 모르면 닫기만 한다. */
    onConfirm: ((String) -> Unit)? = null
) {
    var reason by rememberSaveable { mutableStateOf("") }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MoyeoTheme.sheetSurface,
        modifier = Modifier.testTag("member-remove-sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "멤버 내보내기",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "내보낸 자리는 대기 큐에서 자동으로 채워져요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 프로필 이미지가 있으면 그 이미지다 — 이모지는 없을 때의 폴백이다(R5).
                    UserAvatar(
                        imageUrl = member.imageUrl,
                        nickname = member.name,
                        modifier = Modifier.size(40.dp),
                        fallbackFontSize = 18.sp
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(member.name, fontWeight = FontWeight.ExtraBold)
                        Text(
                            // 합류 시점은 서버 멤버 응답에 없다 — `어제 합류` 는 지어낸 값이라 뺐다.
                            text = member.subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row {
                    Text(
                        text = "내보내는 사유",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = " *",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Box {
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { if (it.length <= 200) reason = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(108.dp)
                            .testTag("member-remove-reason"),
                        placeholder = {
                            Text(
                                text = "사유를 남겨주세요. 상대에게 알림으로 그대로 전달돼요. (10자 이상)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        textStyle = MaterialTheme.typography.bodySmall,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Text(
                        text = "${reason.length}/200",
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 14.dp, bottom = 10.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // 정책 안내 3줄 — 재신청 불가 · 즉시 제외·기록 비공개 · 사유 알림 전달
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "내보내면 이 모임에 다시 신청할 수 없어요.",
                        "내보내는 즉시 채팅방에서 제외돼요. 이미 남긴 대화는 채팅방에 그대로 남아요.",
                        "사유는 상대에게 알림으로 전달돼요."
                    ).forEach { line ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = line,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("member-remove-cancel")
                ) {
                    Text("취소", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Button(
                    onClick = { onConfirm?.invoke(reason.trim()) ?: onDismiss() },
                    enabled = reason.trim().length >= 10,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("member-remove-submit"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("내보내기", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/** 사진 선택기에서 고른 파일 한 건 — multipart 파트에 그대로 넣을 값만 담는다. */
internal data class PickedImage(val fileName: String, val mimeType: String, val bytes: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PickedImage) return false
        return fileName == other.fileName && mimeType == other.mimeType && bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int = 31 * (31 * fileName.hashCode() + mimeType.hashCode()) + bytes.contentHashCode()
}

/**
 * 서버가 받는 사진 상한은 20MB 다(화면기획 20-2 "최대 20MB · 1장씩").
 * 그보다 큰 파일은 올려도 거절되므로 읽지 않고 null 을 준다.
 */
internal const val CHAT_IMAGE_MAX_BYTES = 20 * 1024 * 1024

/** 선택기가 준 content:// URI 를 바이트로 읽는다. 읽기 실패·용량 초과는 null 이다. */
internal suspend fun readPickedImage(context: Context, uri: Uri): PickedImage? = withContext(Dispatchers.IO) {
    runCatching {
        val mimeType = context.contentResolver.getType(uri) ?: "image/*"
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@runCatching null
        if (bytes.isEmpty() || bytes.size > CHAT_IMAGE_MAX_BYTES) return@runCatching null
        PickedImage(
            fileName = uri.lastPathSegment?.substringAfterLast('/')?.takeIf(String::isNotBlank) ?: "image",
            mimeType = mimeType,
            bytes = bytes
        )
    }.getOrNull()
}

/** 20-2 첨부 타일 한 칸 — 아이콘·이름·한 줄 설명 + 그 타일이 여는 작성 화면 라우트. */
private data class AttachTile(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String,
    val hint: String,
    val route: String
)

/**
 * 20-2 첨부 시트.
 *
 * 타일 6개는 각자의 **작성 화면**(20-2a~20-2f)으로 간다 — `ATTACH-COMPOSER-CANON.md` §0·§1.
 * 예전에는 여섯 개가 전부 21(특수 메시지 견본)로 갔는데, 21 은 "보내고 나면 이렇게 보인다"는
 * 결과 카드 모음이라 무엇을 어떻게 만드는지를 대신하지 못한다.
 *
 * 방(`room-{id}`)은 작성 화면으로 그대로 넘긴다. 실제 전송은 각 작성 화면이 한다.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatAttachmentScreen(
    onBack: () -> Unit,
    isOnline: Boolean,
    /** 공유 대상 방. "room-{id}" 면 작성 화면이 실서버로 보낸다. */
    threadId: String? = null,
    /** 타일이 여는 20-2a~20-2f 라우트. */
    onOpenComposer: (String) -> Unit = {}
) {
    val backdropThreadId = threadId ?: OVERLAY_BACKDROP_THREAD_ID
    val items = listOf(
        AttachTile(Icons.Filled.CameraAlt, "사진", "최대 20MB · 1장씩", AppRoutes.attachPhoto(threadId)),
        AttachTile(Icons.Filled.LocationOn, "장소", "관광 정보에서 찾기", AppRoutes.attachPlace(threadId)),
        AttachTile(Icons.Filled.Map, "지도", "만날 위치 핀 공유", AppRoutes.attachMap(threadId)),
        AttachTile(Icons.Filled.Poll, "투표", "2~5개 · 익명 기본", AppRoutes.attachPoll(threadId)),
        AttachTile(Icons.Filled.Payments, "정산", "메모용 · 송금 아님", AppRoutes.attachSettlement(threadId)),
        AttachTile(
            Icons.AutoMirrored.Filled.StickyNote2,
            "메모",
            "상단 고정 공지",
            AppRoutes.attachNotice(threadId)
        )
    )
    // changeLog14 "오버레이 배경 일괄" — 채팅 버블 실루엣 대신 실제 채팅방 본문을 깐다.
    OverlayBackdrop(
        modifier = Modifier.testTag("chat-attach-screen"),
        scrimAlpha = 0.45f,
        onScrimClick = onBack,
        background = { ChatRoomBody(threadId = backdropThreadId, isOnline = isOnline) }
    ) {
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 16.dp
        ) {
            Column(modifier = Modifier.navigationBarsPadding().padding(horizontal = 20.dp, vertical = 14.dp)) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(38.dp)
                        .height(4.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(2.dp))
                )
                Text("무엇을 공유할까요?", style = MaterialTheme.typography.titleLarge)
                Text(
                    if (isOnline) "일반 메시지와 달리 카드로 크게 보여요." else "연결되면 사진과 장소를 공유할 수 있어요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                )
                FlowRow(
                    maxItemsInEachRow = 3,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items.forEach { item ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(116.dp)
                                .clickable(enabled = isOnline) { onOpenComposer(item.route) }
                                .testTag("chat-attach-${item.label}")
                                .semantics {
                                    role = Role.Button
                                    contentDescription = if (isOnline) item.label else "${item.label}, 오프라인에서 사용 불가"
                                },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Icon(
                                        item.icon,
                                        contentDescription = null,
                                        modifier = Modifier.padding(10.dp).size(24.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Text(item.label, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
                                Text(
                                    item.hint,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                ) {
                    // 닫기는 강조 동작이 아니다 — 플랫폼 강조색 대신 중립 글자색을 쓴다
                    Text("닫기", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FriendsScreen(
    onBack: () -> Unit,
    onOpenDex: () -> Unit,
    /** 27-2a 친구 정리 — ⋯ 는 onClick 이 없어 눌러도 아무 일이 없었다(정본 §6-5). */
    onOpenFriendManage: (Long, String, String) -> Unit = { _, _, _ -> }
) {
    var tab by rememberSaveable { mutableStateOf(0) }
    // 우측 상단 검색은 눌러도 아무 일이 없던 자리다 — 이 목록을 닉네임으로 좁히는 입력줄을 연다.
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    // 친구·신청 목록은 실서버가 근거다 — 못 받아오면 비어 있는 상태로 둔다
    val server = LocalServerData.current
    var serverFriends by remember(server) { mutableStateOf<ServerFriendLists?>(null) }
    val friendScope = rememberCoroutineScope()
    // 27-2a 에서 친구를 끊고 돌아오면 목록이 달라져 있다 — 화면이 다시 보일 때마다 읽는다.
    var reloadKey by remember(server) { mutableIntStateOf(0) }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) reloadKey++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(server, reloadKey) {
        serverFriends = if (server == null) {
            null
        } else {
            runCatching {
                ServerFriendLists(
                    friends = server.social.friends(),
                    received = server.social.receivedRequests(),
                    sent = server.social.sentRequests()
                )
            }.getOrNull()
        }
    }

    suspend fun reloadFriendLists() {
        runCatching {
            serverFriends = server?.let {
                ServerFriendLists(
                    friends = it.social.friends(),
                    received = it.social.receivedRequests(),
                    sent = it.social.sentRequests()
                )
            }
        }
    }

    fun answerRequest(requestId: Long, accept: Boolean) {
        friendScope.launch {
            runCatching {
                if (accept) server?.social?.acceptRequest(requestId) else server?.social?.rejectRequest(requestId)
            }.onSuccess { reloadFriendLists() }
        }
    }

    /** 보낸 신청 취소 — DELETE users/me/friend-requests/{requestId}. */
    fun cancelRequest(requestId: Long) {
        friendScope.launch {
            runCatching { server?.social?.cancelRequest(requestId) }.onSuccess { reloadFriendLists() }
        }
    }

    val tabs = serverFriends?.let { data ->
        listOf("내 친구 ${data.friends.size}", "받은 신청 ${data.received.size}", "보낸 신청 ${data.sent.size}")
    } ?: listOf("내 친구 3", "받은 신청 2", "보낸 신청 1")
    val lists = serverFriends?.let { data ->
        listOf(
            data.friends.map { friend ->
                FriendEntry(
                    emoji = "🐻",
                    name = friend.user.nickname,
                    subtitle = friend.user.introduction ?: friend.lastActive.orEmpty(),
                    imageUrl = friend.user.profileImageUrl,
                    userId = friend.user.userId
                )
            },
            data.received.map { request ->
                FriendEntry(
                    emoji = "🐻",
                    name = request.user.nickname,
                    subtitle = request.user.introduction.orEmpty(),
                    imageUrl = request.user.profileImageUrl,
                    requestId = request.requestId
                )
            },
            data.sent.map { request ->
                FriendEntry(
                    emoji = "🐻",
                    name = request.user.nickname,
                    subtitle = request.user.introduction.orEmpty(),
                    imageUrl = request.user.profileImageUrl,
                    requestId = request.requestId,
                    userId = request.user.userId
                )
            }
        )
    } ?: listOf(emptyList(), emptyList(), emptyList())
    // 이 목록 안에서 닉네임으로 좁힌다. 서버에는 사용자 검색 API 가 없다
    // (GET users/{userId}/profile 만 있다) — 없는 API 를 부르는 대신 받아 둔 목록을 거른다.
    val shownList = if (searchQuery.isBlank()) {
        lists[tab]
    } else {
        lists[tab].filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
    }
    ChangeLogScaffold(
        title = "친구 관리",
        onBack = onBack,
        modifier = Modifier.testTag("friends-screen"),
        actions = {
            IconButton(
                onClick = {
                    searchOpen = !searchOpen
                    if (!searchOpen) searchQuery = ""
                },
                modifier = Modifier.size(48.dp).testTag("friends-search-toggle")
            ) {
                Icon(
                    if (searchOpen) Icons.Filled.Close else Icons.Filled.Search,
                    contentDescription = if (searchOpen) "친구 검색 닫기" else "친구 검색"
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
                tabs.forEachIndexed { index, label ->
                    TextButton(onClick = { tab = index }, modifier = Modifier.weight(1f).height(48.dp)) {
                        Text(
                            label,
                            color = if (tab ==
                                index
                            ) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
            HorizontalDivider()
            if (searchOpen) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .testTag("friends-search-input"),
                    placeholder = { Text("닉네임으로 찾기") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp)
                )
            }
            LazyColumn(contentPadding = PaddingValues(vertical = 8.dp, horizontal = 20.dp)) {
                if (tab == 1) {
                    item {
                        Text(
                            "거절해도 상대방에게는 알려지지 않아요.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                if (shownList.isEmpty()) {
                    item {
                        Text(
                            when {
                                // 검색으로 비었을 때 "아직 친구가 없어요" 라고 하면 목록이 빈 것처럼 읽힌다
                                searchQuery.isNotBlank() -> "‘${searchQuery.trim()}’ 과 맞는 사람이 없어요."

                                tab == 0 -> "아직 친구가 없어요."

                                tab == 1 -> "받은 친구 신청이 없어요."

                                else -> "보낸 친구 신청이 없어요."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 24.dp).testTag("friends-empty")
                        )
                    }
                }
                items(shownList) { friend ->
                    Row(
                        modifier = Modifier.fillMaxWidth().height(68.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        UserAvatar(
                            imageUrl = friend.imageUrl,
                            nickname = friend.name,
                            modifier = Modifier.size(44.dp),
                            fallbackFontSize = 20.sp
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(friend.name, fontWeight = FontWeight.ExtraBold)
                            if (friend.subtitle.isNotBlank()) {
                                Text(
                                    friend.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        when (tab) {
                            0 -> IconButton(
                                // 27-2a 친구 정리 시트로 간다 — 친구 끊기는 되돌리기 어렵다
                                onClick = {
                                    friend.userId?.let { onOpenFriendManage(it, friend.name, friend.subtitle) }
                                },
                                modifier = Modifier.size(48.dp).testTag("friend-manage-open")
                            ) {
                                Icon(Icons.Filled.MoreHoriz, contentDescription = "${friend.name} 관리")
                            }

                            1 -> Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(onClick = {
                                    friend.requestId?.let { answerRequest(it, accept = false) }
                                }) { Text("거절") }
                                Button(
                                    onClick = {
                                        friend.requestId?.let { answerRequest(it, accept = true) }
                                    },
                                    contentPadding = PaddingValues(
                                        horizontal = 12.dp
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("수락")
                                }
                            }

                            else -> if (serverFriends != null && friend.requestId != null) {
                                TextButton(onClick = { cancelRequest(friend.requestId) }) { Text("신청 취소") }
                            } else {
                                Text("요청 중", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                item {
                    // 화면기획 형태: 브랜드 틴트 카드 + 경계선, 아이콘 좌측, 본문 아래 링크 한 줄
                    val tints = MoyeoTheme.tints
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = tints.primaryTint,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = .35f))
                    ) {
                        Row(Modifier.padding(13.dp), verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.Filled.Bookmark,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = tints.onPrimaryTint
                            )
                            Column(
                                Modifier.padding(start = 9.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    emphasized(
                                        "함께 여행한 친구는 친구가 아니어도 도감에 남아요. " +
                                            "친구 신청은 피드를 구독하고 싶을 때만 하면 돼요.",
                                        "친구가 아니어도 도감에 남아요."
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tints.onPrimaryTint
                                )
                                Text(
                                    "도감 열어보기 →",
                                    modifier = Modifier.clickable(onClick = onOpenDex),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tints.onPrimaryTint,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TripMessageScreen(
    onBack: () -> Unit,
    onOpenFeedWrite: () -> Unit,
    onOpenCoursePublish: () -> Unit,
    onOpenDex: () -> Unit,
    // 27-4 코스 평가 — 14 코스 상세가 보여주는 평점을 만드는 **유일한** 자리다.
    // 여기서 권하지 않으면 아무도 평가하지 않아 평점이 영원히 비어 있다(정본 §6-4).
    onOpenCourseRating: (String) -> Unit = {}
) {
    // 20-4 는 "방금 끝난 여행"의 동행자에게 한 줄을 남기는 화면이다.
    // 대상은 서버가 준다 — 가장 최근에 끝난 내 모임의 동행자(GET chat-rooms/{id}/companions).
    val server = LocalServerData.current
    var roomId by remember(server) { mutableStateOf<Long?>(null) }
    var companions by remember(server) { mutableStateOf<List<RoomCompanion>>(emptyList()) }
    val messages = remember { mutableStateMapOf<Long, String>() }
    // 27-1 매너 점수 — ReviewTravelCompanionRequest 의 `mannerScore` (1~5, 필수).
    // 별점 UI 가 한 픽셀도 없어서 앱 곳곳의 "매너 4.7" 을 만드는 사람이 아무도 없었다(정본 §6-2).
    val scores = remember { mutableStateMapOf<Long, Int>() }
    var saveError by remember { mutableStateOf<String?>(null) }
    val saveScope = rememberCoroutineScope()
    val presets = listOf("덕분에 즐거웠어요", "사진 고마워요!", "다음에도 잘 부탁드려요")

    LaunchedEffect(server) {
        if (server == null) return@LaunchedEffect
        // 불발된 방(`CANCELLED`)도 `ended` 다. 그런 방의 동행자를 물으면 서버가 409 40915 를 주고
        // 화면은 통째로 빈 상태가 된다 — 평가할 수 있는 것은 **확정돼서 끝난** 여행뿐이다.
        val latestEnded = runCatching {
            server.chatRooms.myRooms()
                .filter { it.ended && it.status == "CONFIRMED" }
        }.getOrElse { emptyList() }.firstOrNull()
        roomId = latestEnded?.roomId
        val id = latestEnded?.roomId ?: return@LaunchedEffect
        companions = runCatching { server.chatRooms.companions(id) }.getOrElse { emptyList() }
        companions.forEach { companion ->
            companion.oneLineReview?.let { messages[companion.userId] = it }
            // 이미 매긴 점수만 채운다 — 안 매긴 사람에게 기본 점수를 넣지 않는다.
            companion.mannerScore?.let { scores[companion.userId] = it }
        }
    }

    fun saveReviews() {
        val id = roomId
        if (server == null || id == null) {
            onOpenDex()
            return
        }
        saveError = null
        saveScope.launch {
            var failed = false
            companions.forEach { companion ->
                val text = messages[companion.userId]?.trim().orEmpty()
                val score = scores[companion.userId] ?: 0
                // `mannerScore` 는 필수다. 점수를 안 매긴 동행자는 아예 보내지 않는다 —
                // 안 매긴 점수를 만점으로 채워 보내면 없는 평가를 앱이 지어내는 것이 된다.
                val changed = score != companion.mannerScore || text != companion.oneLineReview.orEmpty()
                if (score in 1..5 && changed) {
                    runCatching {
                        server.chatRooms.reviewCompanion(
                            id,
                            companion.userId,
                            score,
                            text.takeIf(String::isNotBlank)
                        )
                    }.onFailure { failed = true }
                }
            }
            if (failed) saveError = "평가를 저장하지 못한 동행자가 있어요." else onOpenDex()
        }
    }

    ChangeLogScaffold(
        title = "여행 마무리",
        onBack = onBack,
        modifier = Modifier.testTag("trip-message-screen"),
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onOpenDex) { Text("나중에") }
                    Button(
                        onClick = { saveReviews() },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("메시지 남기고 도감 보기")
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("함께 걸어준 친구들,\n어떠셨어요?", style = MaterialTheme.typography.headlineSmall)
                Text(
                    emphasized(
                        "매너 점수는 다음 모임의 호스트가 보고, 한 줄 메시지는 상대방의 도감 카드 뒷면에 적혀요.",
                        "도감 카드 뒷면"
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (companions.isEmpty()) {
                item { MoyeoEmptyState("아직 함께 여행한 친구가 없어요.", testTag = "trip-message-empty") }
            }
            items(companions, key = { it.userId }) { companion ->
                val text = messages[companion.userId].orEmpty()
                RoundedPanel(
                    containerColor = if (text.isNotBlank()) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        UserAvatar(
                            imageUrl = companion.profileImageUrl,
                            nickname = companion.nickname,
                            modifier = Modifier.size(40.dp),
                            fallbackFontSize = 18.sp
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(companion.nickname, fontWeight = FontWeight.ExtraBold)
                            Text(
                                if (text.isBlank()) "아직 안 남겼어요" else "메시지를 남겼어요",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (text.isNotBlank()) Icon(Icons.Filled.Check, contentDescription = "작성 완료")
                    }
                    // 27-1 매너 점수 (정본 §6-2) — 서버가 받는 값이 정수 1~5 라 반 개는 두지 않는다
                    MannerScoreRow(
                        score = scores[companion.userId] ?: 0,
                        enabled = roomId != null,
                        onScoreChange = { scores[companion.userId] = it },
                        testTag = "manner-score-${companion.userId}"
                    )
                    OutlinedTextField(
                        value = text,
                        onValueChange = { messages[companion.userId] = it.take(40) },
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        placeholder = { Text("한 줄 메시지를 남겨주세요 (최대 40자)") },
                        minLines = 2,
                        supportingText = { Text("${text.length}/40") }
                    )
                    if (text.isBlank()) {
                        FlowRow(
                            modifier = Modifier.padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            presets.forEach { preset ->
                                OutlinedButton(onClick = {
                                    messages[companion.userId] = preset
                                }, shape = RoundedCornerShape(12.dp)) { Text(preset) }
                            }
                        }
                    }
                }
            }
            item {
                // 웹·iOS 20-4 와 같은 안내 — 메시지를 왜 남기는지 알려준다
                RoundedPanel(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Filled.Bookmark,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "메시지를 남기면 서로의 도감 카드가 완성돼요. 가끔 도감을 펼쳐 보면 그날의 여행이 다시 떠올라요.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            saveError?.let { message ->
                item {
                    Text(
                        message,
                        modifier = Modifier.testTag("trip-message-error"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            item {
                // 27-4 코스 평가 진입점 — 화면만 만들고 연결을 안 하면 막다른 길이 하나 더 생긴다
                RelatedActionCard(
                    Icons.Filled.Star,
                    "다녀온 코스는 어떠셨어요?",
                    "코스 평가",
                    { roomId?.let { onOpenCourseRating("room-$it") } }
                )
                Spacer(Modifier.height(10.dp))
                RelatedActionCard(
                    Icons.Filled.ChatBubbleOutline,
                    "경로가 담긴 피드도 이어서 써볼까요?",
                    "피드 쓰기",
                    onOpenFeedWrite
                )
                Spacer(Modifier.height(10.dp))
                RelatedActionCard(
                    Icons.Filled.Map,
                    "이 코스를 다른 여행자에게 열어둘 수도 있어요",
                    "코스 공개",
                    onOpenCoursePublish
                )
            }
        }
    }
}

@Composable
private fun RelatedActionCard(icon: ImageVector, text: String, action: String, onClick: () -> Unit) {
    RoundedPanel {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = onClick) { Text(action) }
        }
    }
}

/** 접수 API 가 없는 신고 안내 문구. 네 표면이 **글자까지 같은 것**을 쓴다(정본 §3). */
private const val REPORT_UNSUPPORTED_TITLE = "신고를 접수하지 못해요"

private const val REPORT_UNSUPPORTED_BODY =
    "멤버·채팅방 신고는 아직 앱에서 받지 못해요. GitHub 이슈나 이메일로 알려주시면 확인할게요."

/**
 * 접수 API 가 없는 신고(멤버·채팅방·댓글)를 **누른 자리에서** 안내한다.
 *
 * 화면을 옮기지 않는다 — 문의 버튼은 29 설정 안에 있어서 거기로 보내면 사용자가 하려던 일에서
 * 멀어진다. 접수되는 신고는 피드뿐이고([ReportScreen]) 나머지는 이 다이얼로그다 —
 * 한 화면에 두 결과를 섞지 않는다(정본 `docs/alignment/REPORT-CANON.md` §3).
 */
@Composable
fun ReportUnsupportedDialog(onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("report-unsupported-dialog"),
        title = { Text(REPORT_UNSUPPORTED_TITLE, fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(REPORT_UNSUPPORTED_BODY, style = MaterialTheme.typography.bodyMedium)
                // 안내만 하고 끝나면 아무 일도 일어나지 않는다 — 실제로 갈 수 있는 두 창구를 버튼으로 둔다.
                OutlinedButton(
                    onClick = { uriHandler.openUri(MoyeoContact.ISSUES_URL) },
                    modifier = Modifier.fillMaxWidth().testTag("report-unsupported-issues")
                ) { Text(MoyeoContact.ISSUES_LABEL, fontWeight = FontWeight.ExtraBold) }
                OutlinedButton(
                    onClick = { uriHandler.openUri(MoyeoContact.MAILTO_URL) },
                    modifier = Modifier.fillMaxWidth().testTag("report-unsupported-email")
                ) { Text(MoyeoContact.EMAIL_LABEL, fontWeight = FontWeight.ExtraBold) }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("report-unsupported-close")) { Text("닫기") }
        }
    )
}

/** 30-2 상세 입력 상한. 서버 `details` 가 0~300자다(실서버 초과 시 400 `40000`). */
private const val REPORT_DETAILS_LIMIT = 300

/**
 * 30-2 피드 신고 시트. **피드 전용이다** — 서버가 접수하는 신고는 피드뿐이다
 * (`POST /api/v1/feeds/{feedId}/reports` → 204). 멤버·채팅방·댓글은 [ReportUnsupportedDialog].
 *
 * 사유는 `GET /api/v1/feeds/report-reasons` 가 코드와 표시 문구를 함께 준다 —
 * 클라이언트가 문구를 갖지 않는다(정본 §2). 목록을 못 받으면 사유를 지어내지 않고 오류를 드러낸다.
 */
@Composable
fun ReportScreen(onBack: () -> Unit, feedId: Long? = null) {
    var details by rememberSaveable(feedId) { mutableStateOf("") }
    var selected by rememberSaveable(feedId) { mutableStateOf<String?>(null) }
    var block by rememberSaveable(feedId) { mutableStateOf(true) }
    val server = LocalServerData.current
    val scope = rememberCoroutineScope()
    var reasons by remember(server, feedId) { mutableStateOf<List<FeedReportReason>?>(null) }
    var reasonsFailed by remember(server, feedId) { mutableStateOf(false) }
    var reasonsAttempt by remember(server, feedId) { mutableIntStateOf(0) }
    var feed by remember(server, feedId) { mutableStateOf<ServerFeed?>(null) }
    var submitting by remember(feedId) { mutableStateOf(false) }
    // 접수 결과(중립)와 오류(붉은색)를 나눈다 — 409 는 오류가 아니라 "이미 신고한 피드" 안내다.
    var resultText by remember(feedId) { mutableStateOf<String?>(null) }
    var errorText by remember(feedId) { mutableStateOf<String?>(null) }
    var blockNotice by remember(feedId) { mutableStateOf<String?>(null) }
    var submitted by remember(feedId) { mutableStateOf(false) }
    LaunchedEffect(server, feedId, reasonsAttempt) {
        if (server == null || feedId == null) return@LaunchedEffect
        reasonsFailed = false
        val loaded = runCatching { server.feeds.reportReasons() }.getOrNull()?.takeIf { it.isNotEmpty() }
        reasons = loaded
        reasonsFailed = loaded == null
        // 첫 사유를 미리 고른다 — 특정 코드를 클라가 골라 두면 서버 목록이 바뀔 때 어긋난다.
        if (loaded != null && selected == null) selected = loaded.first().reason
        // 차단 대상(작성자 userId)과 시트 상단 미리보기는 피드 응답에서 온다.
        feed = runCatching { server.feeds.feed(feedId) }.getOrNull()
    }
    val submit: () -> Unit = submit@{
        val reason = selected
        if (server == null || feedId == null || reason == null || submitting) return@submit
        submitting = true
        errorText = null
        resultText = null
        blockNotice = null
        scope.launch {
            runCatching { server.feeds.reportFeed(feedId, reason, details) }
                .onSuccess {
                    submitted = true
                    resultText = "신고를 접수했어요."
                }
                .onFailure { error ->
                    // 409 = 이미 신고한 피드. 붉은 오류로 띄우지 않는다(정본 §2).
                    if ((error as? MoyeoApiException)?.statusCode == 409) {
                        submitted = true
                        resultText = "이미 신고한 피드예요"
                    } else {
                        errorText = error.message ?: "신고를 접수하지 못했어요."
                    }
                }
            // 차단은 신고와 **따로** 보낸다 — 신고가 204 여도 차단은 실패할 수 있고,
            // 차단은 되돌릴 수 있으므로(29-1a) 실패해도 신고를 되돌리지 않는다.
            val author = feed?.author
            if (submitted && block) {
                if (author == null) {
                    // 작성자를 못 읽었으면 누구를 차단할지 알 수 없다 — 조용히 넘기지 않고 적는다.
                    blockNotice = "차단할 대상을 확인하지 못했어요."
                } else {
                    runCatching { server.social.block(author.userId) }
                        .onSuccess { blockNotice = "${author.nickname}님을 차단했어요." }
                        .onFailure { error -> blockNotice = error.message ?: "차단하지 못했어요." }
                }
            }
            submitting = false
        }
    }
    // 30-2 는 전체 화면이 아니라 피드 상세 위로 올라오는 바텀시트다 —
    // changeLog14 "오버레이 배경 일괄": 빈 딤 대신 실제 피드 상세를 깐다.
    OverlayBackdrop(
        modifier = Modifier.testTag("report-screen"),
        scrimAlpha = .45f,
        onScrimClick = onBack,
        background = {
            if (feedId == null) {
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
            } else {
                FeedDetailScreen(postId = "srv-$feedId", onBack = {})
            }
        }
    ) {
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            // 시트 표면은 화면 배경(background)과 구분되는 카드 표면(surface)이다
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 16.dp
        ) {
            Column(Modifier.navigationBarsPadding()) {
                Box(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 10.dp)
                        .size(width = 36.dp, height = 4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.outline)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            "신고 사유를 알려주세요",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        // 신고 대상 미리보기. 서버 피드 응답이 오기 전에는 없는 내용을 지어내지 않는다.
                        feed?.let { target ->
                            RoundedPanel(modifier = Modifier.padding(top = 12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.ChatBubbleOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "해당 피드 · “${target.trip?.courseTitle ?: target.content}”",
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                    // 사유를 못 받았으면 지어내지 않는다 — 로딩·오류를 그대로 드러낸다(정본 §2 · NO-MOCK R1).
                    if (reasons == null || feedId == null) {
                        item {
                            when {
                                feedId == null -> Text(
                                    "신고할 피드를 찾지 못했어요.",
                                    modifier = Modifier.testTag("report-no-target"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )

                                reasonsFailed -> Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        MoyeoEmptyText.FAILED,
                                        modifier = Modifier.weight(1f).testTag("report-reasons-failed"),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    TextButton(onClick = { reasonsAttempt += 1 }) { Text("다시 시도") }
                                }

                                else -> Text(
                                    MoyeoEmptyText.LOADING,
                                    modifier = Modifier.testTag("report-reasons-loading"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    items(reasons.orEmpty()) { reason ->
                        val chosen = selected == reason.reason
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clickable { selected = reason.reason },
                            shape = RoundedCornerShape(11.dp),
                            border = BorderStroke(
                                1.5.dp,
                                if (chosen) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                }
                            ),
                            color = if (chosen) MoyeoTheme.tints.primaryTint else MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // 화면기획은 라디오 대신 초록 체크 원으로 선택을 표시한다
                                Icon(
                                    if (chosen) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (chosen) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outline
                                    }
                                )
                                // 표시 문구는 서버 displayName 을 그대로 쓴다 — 클라가 문구를 갖지 않는다.
                                Text(
                                    reason.displayName,
                                    fontWeight = FontWeight.Bold,
                                    color = if (chosen) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                    item {
                        // 상세 입력 — 서버 `details` 는 선택이고 0~300자다. 특히 「기타」를 골랐을 때 필요하다.
                        OutlinedTextField(
                            value = details,
                            onValueChange = { next -> details = next.take(REPORT_DETAILS_LIMIT) },
                            modifier = Modifier.fillMaxWidth().testTag("report-details"),
                            enabled = !submitted,
                            placeholder = { Text("어떤 점이 문제인지 알려주세요 (선택)") },
                            minLines = 3,
                            maxLines = 5,
                            supportingText = {
                                Text(
                                    "${details.length} / $REPORT_DETAILS_LIMIT",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.End,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { block = !block }.padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = block, onCheckedChange = { block = it })
                            Text("이 유저를 차단할게요", fontWeight = FontWeight.Bold)
                        }
                        if (block) {
                            Text(
                                "차단하면 이 유저가 만들었거나 참여한 모집이 홈·탐색에서 모두 숨겨져요.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = onBack,
                                modifier = Modifier.width(72.dp).height(52.dp).testTag("report-cancel")
                            ) {
                                Text(
                                    if (submitted) "닫기" else "취소",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (!submitted) {
                                Button(
                                    onClick = submit,
                                    enabled = !submitting && selected != null && feedId != null,
                                    modifier = Modifier.weight(1f).height(52.dp).testTag("report-submit"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text(if (submitting) "보내는 중..." else "신고하기") }
                            }
                        }
                        // 접수 결과는 중립 문구로 남긴다 — 409(이미 신고한 피드)도 여기로 온다.
                        resultText?.let { message ->
                            Text(
                                message,
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                    .testTag("report-result"),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // 차단은 신고와 별개다 — 결과도 따로 적는다.
                        blockNotice?.let { message ->
                            Text(
                                message,
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                    .testTag("report-block-result"),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        errorText?.let { message ->
                            Text(
                                message,
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                    .testTag("report-error"),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BlockedUsersScreen(onBack: () -> Unit, onOpenUnblockConfirm: (Long, String) -> Unit = { _, _ -> }) {
    // 로그인 상태면 실서버 차단 목록(GET users/me/blocks)으로 대체한다
    val server = LocalServerData.current
    var serverBlocked by remember(server) {
        mutableStateOf<List<kr.hanchae.moyeotrip.data.social.BlockedUser>?>(null)
    }
    LaunchedEffect(server) {
        serverBlocked = if (server == null) null else runCatching { server.social.blocks() }.getOrNull()
    }
    ChangeLogScaffold(
        title = "차단한 사용자",
        onBack = onBack,
        modifier = Modifier.testTag("blocked-users-screen")
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
        ) {
            item {
                RoundedPanel(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                    Text(
                        emphasized(
                            "차단하면 그 사람이 만들었거나 참여한 모집이 홈·탐색·코스 상세에서 모두 숨겨져요. " +
                                "상대방에게는 알려지지 않아요.",
                            "만들었거나 참여한 모집"
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            val serverList = serverBlocked.orEmpty()
            if (serverList.isEmpty()) {
                item { MoyeoEmptyState("차단한 사용자가 없어요.", testTag = "blocked-users-empty") }
            }
            items(serverList, key = { it.userId }) { user ->
                Row(
                    modifier = Modifier.fillMaxWidth().height(72.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    UserAvatar(
                        imageUrl = user.profileImageUrl,
                        nickname = user.nickname,
                        modifier = Modifier.size(42.dp),
                        fallbackFontSize = 19.sp
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.nickname, fontWeight = FontWeight.ExtraBold)
                        if (user.blockedAt.isNotBlank()) {
                            Text(
                                "${user.blockedAt.take(10).replace('-', '.')} 차단",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    OutlinedButton(
                        // 29-1a — 되돌리기 어려운 행동이라 확인 화면을 먼저 지난다(정본 §6-1).
                        // 예전에는 여기서 바로 DELETE 가 나갔다.
                        onClick = { onOpenUnblockConfirm(user.userId, user.nickname) },
                        modifier = Modifier.height(34.dp).testTag("blocked-unblock-${user.userId}"),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(
                            "차단 해제",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            item {
                Text(
                    "차단을 해제하면 서로의 모집·피드를 다시 볼 수 있어요. 해제 전에 한 번 더 확인해요.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 14.dp)
                )
            }
        }
    }
}

/**
 * 화면기획 27-3 여행 코스 공개.
 *
 * 미리보기 카드는 **가장 최근에 끝난 내 모임의 코스**(GET chat-rooms/my → travel-courses/chat-rooms/{id})와
 * 내 서버 프로필이 채운다. 다녀온 여행이 없으면 공개할 코스가 없으므로 미리보기는 빈 상태이고 CTA 는 잠긴다.
 *
 * 공개(여행자 코스 등록) 자체는 서버 API 가 아직 없다 — §4 BE 요청 대상이다.
 * 되돌릴 수 없는 동작이라 두 번 확인하는 흐름은 화면기획 그대로 남겨 둔다.
 */
@Composable
fun CoursePublishScreen(onBack: () -> Unit, onPublished: () -> Unit) {
    var showConfirmation by rememberSaveable { mutableStateOf(false) }
    var showFinalConfirmation by rememberSaveable { mutableStateOf(false) }
    var credit by rememberSaveable { mutableStateOf(true) }
    var title by rememberSaveable { mutableStateOf("") }
    var summary by rememberSaveable { mutableStateOf("") }
    val server = LocalServerData.current
    var course by remember(server) { mutableStateOf<TravelCourse?>(null) }
    var myNickname by remember(server) { mutableStateOf<String?>(null) }
    var myProfileImageUrl by remember(server) { mutableStateOf<String?>(null) }
    var publishBusy by remember { mutableStateOf(false) }
    var publishError by remember { mutableStateOf<String?>(null) }
    val publishScope = rememberCoroutineScope()
    LaunchedEffect(server) {
        if (server == null) return@LaunchedEffect
        // 불발된 방(`CANCELLED`)도 `ended` 다. 그 방의 코스를 공개하려 하면 서버가 409 40914
        // ("공개 여부를 선택할 수 있는 완료 코스가 아닙니다")를 준다 — 공개할 수 있는 것은
        // **확정돼서 끝난** 여행뿐이다. 27-1 여행 마무리와 같은 규칙이다.
        val lastTrip = runCatching {
            server.chatRooms.myRooms().firstOrNull { it.ended && it.status == "CONFIRMED" }
        }.getOrNull()
        course = lastTrip?.roomId?.let { runCatching { server.courses.roomCourse(it) }.getOrNull() }
        // 코스 이름은 서버 코스 제목에서 시작하고, 사용자가 고칠 수 있다
        course?.title?.let { if (title.isBlank()) title = it }
        runCatching { server.userProfile.profile() }.getOrNull()?.let { profile ->
            myNickname = profile.nickname
            myProfileImageUrl = profile.profileImageUrl
        }
    }
    val warningContainer = MoyeoTheme.tints.warningTint
    val warningContent = MoyeoTheme.tints.onWarningTint
    ChangeLogScaffold(
        title = "코스 공개",
        onBack = onBack,
        modifier = Modifier.testTag("course-publish-screen"),
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onBack) { Text("지금은 안 할래요") }
                    Button(
                        onClick = { showConfirmation = true },
                        // 공개할 코스가 없으면 누를 수 없다. 서버는 소개도 필수로 받는다(@NotBlank).
                        enabled = course != null && title.isNotBlank() && summary.isNotBlank() && !publishBusy,
                        modifier = Modifier.weight(1f).height(50.dp).testTag("course-publish-start"),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text(if (publishBusy) "공개하는 중..." else "코스 공개하기") }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            publishError?.let { message ->
                item {
                    Text(
                        message,
                        modifier = Modifier.testTag("course-publish-error"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            item {
                Text("이번 여행 코스,\n다른 여행자에게도 열어둘까요?", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "공개하면 탐색과 코스 목록에 올라가고, 다른 사람이 이 코스로 모집을 열 수 있어요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            item {
                Text("공개하면 이렇게 보여요", fontWeight = FontWeight.ExtraBold)
                val loadedCourse = course
                if (loadedCourse == null) {
                    MoyeoEmptyState("아직 다녀온 여행 기록이 없어요.", testTag = "course-publish-empty")
                } else {
                    CoursePublishPreviewCard(
                        course = loadedCourse,
                        title = title,
                        credit = credit,
                        nickname = myNickname,
                        profileImageUrl = myProfileImageUrl
                    )
                }
            }
            item {
                CompactPublishField("코스 이름 *", title, Icons.AutoMirrored.Filled.StickyNote2) { title = it }
                CompactPublishField(
                    label = "한 줄 소개",
                    value = summary,
                    icon = Icons.Filled.AutoAwesome,
                    modifier = Modifier.padding(top = 14.dp),
                    caption = "다녀온 사람만 쓸 수 있는 한 줄이 코스의 값어치예요."
                ) { summary = it.take(60) }
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(top = 14.dp).clickable { credit = !credit },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = credit, onCheckedChange = { credit = it })
                        Column {
                            Text("내 닉네임을 함께 보여주기", fontWeight = FontWeight.Bold)
                            Text("끄면 익명 여행자 코스로 올라가요.", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                RoundedPanel(modifier = Modifier.padding(top = 14.dp), containerColor = warningContainer) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = null,
                            tint = warningContent,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "한 번 공개한 코스는 다시 내릴 수 없어요. 다른 여행자가 이 코스로 모집을 열거나 찜해둘 수 있기 때문이에요.",
                            style = MaterialTheme.typography.bodySmall,
                            color = warningContent
                        )
                    }
                }
                Text(
                    "공개는 지난 여행에서 언제든 다시 열 수 있어요.",
                    modifier = Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            icon = { Icon(Icons.Filled.Lock, contentDescription = null) },
            title = { Text("한 번 공개하면 다시 내릴 수 없어요") },
            text = { Text("코스명 · 경로 · 한 줄 소개가 공개돼요. 채팅 내용과 사진은 공개되지 않아요. 계속할까요?") },
            dismissButton = { TextButton(onClick = { showConfirmation = false }) { Text("취소") } },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmation = false
                        showFinalConfirmation = true
                    },
                    modifier = Modifier.testTag("course-publish-confirm-first"),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("계속") }
            }
        )
    }
    if (showFinalConfirmation) {
        AlertDialog(
            onDismissRequest = { showFinalConfirmation = false },
            title = { Text("정말 공개할까요?") },
            text = { Text("공개 후에는 비공개로 되돌릴 수 없습니다.") },
            dismissButton = { TextButton(onClick = { showFinalConfirmation = false }) { Text("다시 볼게요") } },
            confirmButton = {
                Button(
                    // 여기서 **처음으로** 서버를 부른다. 예전에는 화면 상태만 바꾸고 끝나서
                    // 공개했다고 알려놓고 실제로는 아무 데도 올라가지 않았다.
                    onClick = {
                        showFinalConfirmation = false
                        val courseId = course?.courseId
                        if (server == null || courseId == null) {
                            publishError = "공개할 코스를 찾지 못했어요."
                            return@Button
                        }
                        publishBusy = true
                        publishError = null
                        publishScope.launch {
                            runCatching {
                                server.courses.publishCourse(
                                    courseId = courseId,
                                    title = title.trim(),
                                    description = summary.trim(),
                                    showCreatorNickname = credit
                                )
                            }
                                .onSuccess { onPublished() }
                                .onFailure { publishError = it.message ?: "코스를 공개하지 못했어요." }
                            publishBusy = false
                        }
                    },
                    enabled = !publishBusy,
                    modifier = Modifier.testTag("course-publish-confirm-final"),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("공개할게요") }
            }
        )
    }
}

/** 27-3 미리보기 카드 — 썸네일·메타·작성자는 모두 서버 값이다. */
@Composable
private fun CoursePublishPreviewCard(
    course: TravelCourse,
    title: String,
    credit: Boolean,
    nickname: String?,
    profileImageUrl: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(132.dp)) {
            CachedRemoteImage(
                url = course.thumbnail,
                contentDescription = course.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                fallbackShape = MoyeoPlaceholderShape.LANDSCAPE
            ) {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
            }
            Surface(
                modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(13.dp))
                    Text(
                        "여행자 코스",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
            // 서버가 주지 않는 조각은 빠진다 — 거리·소요 시간을 지어내지 않는다
            val meta = listOfNotNull(
                course.travelTime,
                course.distanceKm?.let { "${it}km" },
                "방문지 ${course.places.size}"
            ).joinToString(" · ")
            Text(
                meta,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            Row(
                modifier = Modifier.padding(top = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                if (credit && nickname != null) {
                    UserAvatar(
                        imageUrl = profileImageUrl,
                        nickname = nickname,
                        modifier = Modifier.size(24.dp),
                        fallbackFontSize = 11.sp
                    )
                }
                Text(
                    if (credit && nickname != null) "$nickname 님이 다녀온 코스" else "익명 여행자가 다녀온 코스",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CompactPublishField(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    caption: String? = null,
    onValueChange: (String) -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            singleLine = true,
            leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
            shape = RoundedCornerShape(11.dp)
        )
        caption?.let {
            Text(
                it,
                modifier = Modifier.padding(top = 5.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 화면기획 20-5 여행 날 채팅방.
 *
 * 진행 위젯은 서버 로드맵(`GET chat-rooms/{id}/roadmap/current`)이 근거다 — 방문지마다
 * `progress`(COMPLETED·CURRENT·UPCOMING)와 `scheduledAt` 이 함께 온다. 예전에는 "현재 방문지 2/4"를
 * 앱이 지어내 그렸다. 로드맵이 없거나(`active=false`) 방문지가 비면 위젯 자체를 그리지 않는다.
 *
 * 대화는 채팅방(20)과 같은 메시지 API(POST chat-rooms/{id}/messages)를 쓴다 — 20-5 도 같은 방이라
 * 여기서 보낸 메시지가 20 에도 그대로 보인다. 방을 모르면(캡처 라우트) 입력줄만 잠긴다.
 */
@Composable
fun TripDayScreen(
    threadId: String,
    onBack: () -> Unit,
    onOpenMenu: () -> Unit,
    onOpenAttachment: () -> Unit,
    onOpenRoute: () -> Unit
) {
    val server = LocalServerData.current
    val roomId = threadId.serverRoomIdOrNull()
    var detail by remember(roomId) { mutableStateOf<ChatRoomDetail?>(null) }
    var members by remember(roomId) { mutableStateOf<RoomMembers?>(null) }
    var roadmap by remember(roomId) { mutableStateOf<RoomRoadmap?>(null) }
    var courseTitle by remember(roomId) { mutableStateOf<String?>(null) }
    var messages by remember(roomId) { mutableStateOf<List<RoomMessage>?>(null) }
    var draft by rememberSaveable(threadId) { mutableStateOf("") }
    var sending by remember(roomId) { mutableStateOf(false) }
    var sendError by remember(roomId) { mutableStateOf<String?>(null) }
    val sendScope = rememberCoroutineScope()

    LaunchedEffect(roomId, server) {
        if (roomId == null || server == null) return@LaunchedEffect
        detail = runCatching { server.chatRooms.room(roomId) }.getOrNull()
        members = runCatching { server.chatRooms.members(roomId) }.getOrNull()
        roadmap = runCatching { server.chatRooms.currentRoadmap(roomId) }.getOrNull()
        courseTitle = runCatching { server.courses.roomCourse(roomId).title }.getOrNull()
        messages = runCatching { server.chatRooms.messages(roomId).messages }.getOrNull()
    }

    // 채팅방(20)과 같은 이유로 내 id 는 액세스 토큰에서 동기로 읽는다 —
    // 멤버 응답을 기다리면 내 메시지가 한 프레임 동안 왼쪽에 그려진다(정본 R6).
    val myUserId = remember(server) { server?.signedInUserId?.invoke() }
    val places = roadmap?.takeIf(RoomRoadmap::active)?.places.orEmpty()

    ChangeLogScaffold(
        // 여행 날 채팅방 제목은 코스 이름이다 (화면기획 20-5)
        title = courseTitle ?: detail?.title.orEmpty(),
        onBack = onBack,
        modifier = Modifier.testTag("trip-day-screen"),
        actions = {
            IconButton(onClick = onOpenMenu, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Filled.Menu, contentDescription = "모임 정보")
            }
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Column(modifier = Modifier.fillMaxWidth().imePadding().navigationBarsPadding()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = onOpenAttachment, modifier = Modifier.size(48.dp)) {
                            // 화면기획 20-5의 입력창 좌측은 클립이 아니라 + 다
                            Icon(Icons.Filled.Add, contentDescription = "첨부")
                        }
                        // 예전에는 껍데기 입력줄 + 영구 비활성 보내기였다 — 첨부만 되고 말은 못 하는
                        // 방이었다. 20-5 도 채팅방이라 대화는 20 과 같은 API(POST {id}/messages)로 보낸다.
                        OutlinedTextField(
                            value = draft,
                            onValueChange = { draft = it },
                            modifier = Modifier.weight(1f).testTag("trip-day-message-input"),
                            placeholder = { Text("메시지 입력") },
                            singleLine = true,
                            enabled = roomId != null && server != null && !sending,
                            shape = RoundedCornerShape(24.dp)
                        )
                        FilledIconButton(
                            onClick = {
                                val trimmed = draft.trim()
                                if (trimmed.isEmpty() || sending || roomId == null || server == null) {
                                    return@FilledIconButton
                                }
                                sending = true
                                sendScope.launch {
                                    runCatching { server.chatRooms.sendMessage(roomId, trimmed) }
                                        .onSuccess { sent ->
                                            messages = messages.orEmpty() + sent
                                            draft = ""
                                            sendError = null
                                        }
                                        .onFailure { error ->
                                            sendError = error.message ?: "메시지를 보내지 못했어요."
                                        }
                                    sending = false
                                }
                            },
                            enabled = draft.isNotBlank() && !sending && roomId != null && server != null,
                            modifier = Modifier.size(48.dp).testTag("trip-day-message-send")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "보내기")
                        }
                    }
                    sendError?.let { message ->
                        Text(
                            text = message,
                            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, bottom = 8.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("여행 중", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                    // 인원·출발 시각은 서버가 준 값만 붙인다 — 없으면 그 조각이 빠진다
                    val meta = listOfNotNull(
                        members?.let { "${it.participantCount}명" },
                        detail?.dayTripStartTime?.take(5)?.let { "$it 출발" }
                    ).joinToString(" · ")
                    if (meta.isNotBlank()) {
                        Text(" · $meta", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            if (places.isNotEmpty()) {
                item { TripDayProgressPanel(places = places, onOpenRoute = onOpenRoute) }
            }
            when {
                roomId == null || server == null -> item {
                    MoyeoEmptyState(MoyeoEmptyText.NO_JOINED_ROOMS, testTag = "trip-day-empty")
                }

                messages == null -> item { MoyeoEmptyState(MoyeoEmptyText.LOADING) }

                messages.orEmpty().isEmpty() -> item {
                    MoyeoEmptyState(MoyeoEmptyText.NO_JOINED_ROOMS, testTag = "trip-day-empty")
                }

                else -> items(messages.orEmpty(), key = { it.messageId }) { message ->
                    Box(modifier = Modifier.padding(horizontal = 18.dp, vertical = 5.dp)) {
                        if (message.type == "SYSTEM") {
                            SystemPillMessage(message.content)
                        } else {
                            MessageBubble(message = message.toChatBubble(myUserId))
                        }
                    }
                }
            }
        }
    }
}

/** 20-5 진행 위젯 — 서버 로드맵의 `progress` 를 그대로 그린다. */
@Composable
private fun TripDayProgressPanel(places: List<RoadmapPlace>, onOpenRoute: () -> Unit) {
    val currentIndex = places.indexOfFirst { it.progress == "CURRENT" }
    val nextPlace = places.firstOrNull { it.progress == "UPCOMING" }

    Column(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(
                // 현재 방문지는 서버가 CURRENT 로 알려줄 때만 적는다
                text = if (currentIndex >= 0) {
                    "현재 방문지 ${currentIndex + 1}/${places.size} · ${places[currentIndex].title}"
                } else {
                    "방문지 ${places.size}곳"
                },
                modifier = Modifier.weight(1f).padding(start = 6.dp),
                fontWeight = FontWeight.ExtraBold
            )
            TextButton(onClick = onOpenRoute) { Text("코스 전체") }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            places.forEachIndexed { index, place ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(58.dp)
                ) {
                    val done = place.progress == "COMPLETED"
                    Surface(
                        modifier = Modifier.size(22.dp),
                        shape = CircleShape,
                        color = if (done) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(if (done) "✓" else "${index + 1}", fontSize = 10.sp)
                        }
                    }
                    Text(place.title, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                }
                if (index < places.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f).padding(top = 10.dp),
                        thickness = 2.dp,
                        color = if (places[index].progress == "COMPLETED") {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                        }
                    )
                }
            }
        }
        // 다음 일정은 서버가 UPCOMING 으로 알려준 방문지다 — 없으면 줄째로 빠진다
        nextPlace?.let { place ->
            Row(modifier = Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(
                    text = listOfNotNull(
                        "다음 일정",
                        place.scheduledAt?.takeLast(8)?.take(5),
                        place.title
                    ).joinToString(" · "),
                    modifier = Modifier.padding(start = 6.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotificationDetailScreen(onBack: () -> Unit) {
    // 화면기획 29-2는 옵션마다 무엇이 오는지 부제로 알려준다 — 제목만 두면 차이를 알 수 없다
    val modes = listOf(
        "모든 메시지" to "모임의 모든 대화를 알려드려요",
        "멘션·답글만" to "나를 부르거나 내 메시지에 답할 때만",
        "받지 않기" to "앱을 열었을 때만 확인해요"
    )
    var mode by rememberSaveable { mutableStateOf(modes.first().first) }
    var dnd by rememberSaveable { mutableStateOf(true) }
    val selectedDays = remember { mutableStateListOf("월", "화", "수", "목", "금") }
    var dndStart by rememberSaveable { mutableStateOf("22:30") }
    var dndEnd by rememberSaveable { mutableStateOf("07:00") }

    // 로그인 상태면 실서버 설정(GET notifications/settings + users/me/profile)으로 초기화하고,
    // 바뀔 때마다 PUT notifications/settings 로 저장한다.
    val server = LocalServerData.current
    var serverProfileModes by remember(server) {
        mutableStateOf<kr.hanchae.moyeotrip.data.profile.ServerUserProfile?>(null)
    }
    var serverLoaded by remember(server) { mutableStateOf(false) }
    val settingsScope = rememberCoroutineScope()
    LaunchedEffect(server) {
        serverLoaded = false
        if (server == null) return@LaunchedEffect
        val settings = runCatching { server.notifications.settings() }.getOrNull() ?: return@LaunchedEffect
        val profile = runCatching { server.userProfile.profile() }.getOrNull() ?: return@LaunchedEffect
        serverProfileModes = profile
        mode = when (profile.chatNotificationMode) {
            "MENTIONS_AND_REPLIES" -> "멘션·답글만"
            "NONE" -> "받지 않기"
            else -> "모든 메시지"
        }
        dnd = settings.doNotDisturbEnabled
        settings.doNotDisturbStartTime?.let { dndStart = it.take(5) }
        settings.doNotDisturbEndTime?.let { dndEnd = it.take(5) }
        selectedDays.clear()
        selectedDays.addAll(settings.doNotDisturbDays.mapNotNull(apiDayToKorean::get))
        serverLoaded = true
    }

    fun pushServerSettings() {
        val profile = serverProfileModes
        if (server == null || profile == null || !serverLoaded) return
        val update = NotificationSettingsUpdate(
            chatNotificationMode = when (mode) {
                "멘션·답글만" -> "MENTIONS_AND_REPLIES"
                "받지 않기" -> "NONE"
                else -> "ALL"
            },
            recruitmentDeadlineEnabled = profile.recruitmentDeadlineEnabled,
            socialActivityEnabled = profile.socialActivityEnabled,
            marketingEnabled = profile.marketingEnabled,
            doNotDisturbEnabled = dnd,
            doNotDisturbStartTime = if (dnd) dndStart else null,
            doNotDisturbEndTime = if (dnd) dndEnd else null,
            doNotDisturbDays = selectedDays.mapNotNull(koreanDayToApi::get)
        )
        settingsScope.launch {
            runCatching { server.notifications.updateSettings(update) }
        }
    }
    ChangeLogScaffold(
        title = "채팅 알림",
        onBack = onBack,
        modifier = Modifier.testTag("notification-detail-screen")
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("모임이 여러 개면 알림이 금방 쌓여요. 받고 싶은 만큼만 켜두세요.", style = MaterialTheme.typography.bodySmall)
            }
            items(modes) { (title, description) ->
                val selected = mode == title
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable {
                        mode = title
                        pushServerSettings()
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(end = 14.dp, top = 10.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selected, onClick = {
                            mode = title
                            pushServerSettings()
                        })
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(title, fontWeight = FontWeight.Bold)
                            Text(
                                description,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            item {
                // 방해금지 시간대 — 화면기획: 토글 + 시작/종료 필드 + 요일 7칸.
                // 시간을 텍스트 한 줄로만 두면 어디서 고치는지 알 수 없다.
                val tints = MoyeoTheme.tints
                RoundedPanel {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("방해금지 시간대", fontWeight = FontWeight.ExtraBold)
                            Text(
                                "이 시간엔 소리·진동 없이 조용히 쌓여요",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = dnd, onCheckedChange = {
                            dnd = it
                            pushServerSettings()
                        })
                    }
                    if (dnd) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            QuietHourField("시작", dndStart, Modifier.weight(1f))
                            QuietHourField("종료", dndEnd, Modifier.weight(1f))
                        }
                        Text(
                            "요일",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(top = 14.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("월", "화", "수", "목", "금", "토", "일").forEach { day ->
                                val on = day in selectedDays
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clickable {
                                            if (on) selectedDays.remove(day) else selectedDays.add(day)
                                            pushServerSettings()
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (on) tints.primaryTint else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        1.dp,
                                        if (on) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outline
                                        }
                                    )
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            day,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (on) {
                                                tints.onPrimaryTint
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        Text(
                            emphasized(
                                "집합 30분 전 알림처럼 여행 당일 안내는 방해금지 시간에도 전달돼요.",
                                "여행 당일 안내는 방해금지 시간에도"
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
                // 모임별 알림은 각 채팅방 설정에서 바꾼다 — 여기서 다루지 않는다
            }
        }
    }
}

@Composable
fun AccountDeleteScreen(onBack: () -> Unit, onDelete: () -> Unit) {
    // 화면기획과 같은 사유·삭제범위·참여 목록
    val reasons = listOf("여행을 자주 가지 않게 됐어요", "마음에 드는 모집이 없어요", "불쾌한 경험이 있었어요", "알림이 너무 많아요", "기타")
    // 참여 중인 여행은 서버 내 모임(GET chat-rooms/my)이 근거다 — 목록이 비면 경고 카드도 사라진다
    val server = LocalServerData.current
    var joinedTrips by remember(server) { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(server) {
        joinedTrips = if (server == null) {
            emptyList()
        } else {
            runCatching {
                server.chatRooms.myRooms()
                    .filterNot(kr.hanchae.moyeotrip.data.rooms.MyChatRoom::ended)
                    .map { room ->
                        listOfNotNull(
                            room.title,
                            recruitmentDDayText(room.recruitmentDDay)
                        ).joinToString(" · ")
                    }
            }.getOrElse { emptyList() }
        }
    }
    val deletionScope = listOf(
        "피드·도감·친구·여행 기록이 모두 삭제돼요",
        "내가 공개한 여행자 코스는 남지만 닉네임은 지워져요",
        "30일 안에 다시 로그인하면 계정을 되살릴 수 있어요",
        "30일이 지나면 완전히 삭제되고 되돌릴 수 없어요"
    )
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    var understood by rememberSaveable { mutableStateOf(false) }
    var confirm by rememberSaveable { mutableStateOf(false) }
    var finalConfirm by rememberSaveable { mutableStateOf(false) }
    ChangeLogScaffold(
        title = "계정 탈퇴",
        onBack = onBack,
        modifier = Modifier.testTag("account-delete-screen"),
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.width(96.dp).height(52.dp).testTag("account-delete-back")
                    ) {
                        Text(
                            "돌아가기",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = { confirm = true },
                        enabled = selected != null && understood,
                        modifier = Modifier.weight(1f).height(52.dp).testTag("account-delete-submit"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("탈퇴하기") }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                // 참여 중인 여행을 목록으로 — 어떤 여행을 정리해야 하는지 이 화면에서 알아야 한다
                val tints = MoyeoTheme.tints
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = tints.warningTint
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.People,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = tints.onWarningTint
                            )
                            Text(
                                "참여 중인 여행이 ${joinedTrips.size}개 있어요",
                                Modifier.padding(start = 8.dp),
                                fontWeight = FontWeight.ExtraBold,
                                color = tints.onWarningTint
                            )
                        }
                        Text(
                            "탈퇴하면 동행자들에게 갑자기 빈자리가 생겨요. 나가기 처리를 먼저 해주세요.",
                            style = MaterialTheme.typography.bodySmall,
                            color = tints.onWarningTint
                        )
                        joinedTrips.forEach { trip ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    Modifier.padding(11.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.CalendarToday,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = tints.onWarningTint
                                    )
                                    Text(
                                        trip,
                                        Modifier.padding(start = 8.dp).weight(1f),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "관리 →",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = tints.onWarningTint,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }
                Text(
                    "떠나는 이유를 알려주세요",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 18.dp)
                )
                Text(
                    "서비스를 고치는 데만 쓰여요. (필수)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(reasons) { reason ->
                Row(
                    modifier = Modifier.fillMaxWidth().height(52.dp).clickable { selected = reason },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selected == reason, onClick = { selected = reason })
                    Text(reason)
                }
            }
            item {
                RoundedPanel(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                    Text("탈퇴하면 이렇게 돼요", fontWeight = FontWeight.ExtraBold)
                    deletionScope.forEach { line ->
                        Row(
                            modifier = Modifier.padding(top = 6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                Modifier
                                    .padding(top = 7.dp)
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Text(
                                line,
                                Modifier.padding(start = 8.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { understood = !understood }.padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = understood, onCheckedChange = { understood = it })
                    Text("삭제 범위와 30일 대기 정책을 확인했어요", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("계정 탈퇴를 계속할까요?") },
            text = { Text("탈퇴하면 즉시 로그아웃되고 30일 복구 대기 상태가 됩니다.") },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("취소") } },
            confirmButton = {
                Button(
                    onClick = {
                        confirm = false
                        finalConfirm = true
                    },
                    shape = RoundedCornerShape(12.dp)
                ) { Text("계속") }
            }
        )
    }
    if (finalConfirm) {
        AlertDialog(
            onDismissRequest = { finalConfirm = false },
            icon = { Icon(Icons.Filled.DeleteForever, contentDescription = null) },
            title = { Text("마지막 확인") },
            text = { Text("30일 뒤 계정과 개인정보가 영구 삭제됩니다. 탈퇴할까요?") },
            dismissButton = { TextButton(onClick = { finalConfirm = false }) { Text("돌아가기") } },
            confirmButton = {
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("계정 탈퇴")
                }
            }
        )
    }
}

enum class SystemNoticeMode { Maintenance, Error }

@Composable
fun SystemNoticeScreen(mode: SystemNoticeMode, onRetry: () -> Unit, onBack: () -> Unit) {
    val maintenance = mode == SystemNoticeMode.Maintenance
    val tints = MoyeoTheme.tints
    // 화면기획 33·34: 본문은 세로 중앙, CTA와 캡션은 화면 바닥에 붙는다
    // 화면기획 33·34의 페이지는 흰색(bgBase)이고 점검 안내 박스만 회색 채움이다.
    // 기본 배경(#F7F8F7)은 surfaceVariant와 같은 값이라 박스가 배경에 묻힌다.
    Column(
        modifier = Modifier.fillMaxSize().background(MoyeoTheme.pageSurface).padding(28.dp)
            .testTag(if (maintenance) "system-maintenance-screen" else "system-error-screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = CircleShape,
                color = if (maintenance) tints.warningTint else tints.dangerTint
            ) {
                Icon(
                    if (maintenance) Icons.Filled.Settings else Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.padding(18.dp).size(38.dp),
                    tint = if (maintenance) tints.onWarningTint else tints.onDangerTint
                )
            }
            Text(
                if (maintenance) "잠시 점검 중이에요" else "무언가 살짝\n잘못됐어요",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 22.dp)
            )
            Text(
                if (maintenance) {
                    "더 안정적인 서비스를 위해 정비하고 있어요."
                } else {
                    "잠시 후 다시 시도해주세요. 계속 이러면 문의해주세요."
                },
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp)
            )
            if (maintenance) {
                // 점검 중 제약을 목록으로 알려준다 (화면기획)
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MoyeoTheme.subtleSurface
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("예상 종료 · 오늘 오전 4:00", "점검 중에는 모집·채팅이 열리지 않아요").forEach { line ->
                            Row(verticalAlignment = Alignment.Top) {
                                Box(
                                    Modifier
                                        .padding(top = 7.dp)
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                                Text(
                                    line,
                                    Modifier.padding(start = 8.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (maintenance) "지금 확인" else "새로고침")
        }
        if (!maintenance) {
            TextButton(onClick = onBack, modifier = Modifier.padding(top = 4.dp)) {
                Text(
                    "돌아가기",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Text(
            if (maintenance) "10분마다 자동으로 다시 확인해요" else "ERR-500 · 2026-08-17 14:22",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

/** 23-1 이 한 번에 받는 최상위 댓글 수. 서버 상한은 50 이다. */
private const val FEED_COMMENTS_PAGE_SIZE = 20

/**
 * 23-1 댓글 — 서버 피드 댓글(GET feeds/{id}/comments)이다.
 * 라우트 식별자는 `srv-{feedId}`. 그 형태가 아니거나 미로그인이면 빈 상태다.
 *
 * 2026-09-02 서버 변경으로 응답이 `{comments, nextId}` 객체가 되면서 **무한 스크롤**이 된다.
 * 커서는 응답의 `nextId` 를 다음 요청의 `beforeCommentId` 로 넘기는 방식이고,
 * `nextId` 가 `null` 이면 마지막 묶음이라 더 부르지 않는다(GET /feeds 와 같은 커서 규약).
 */
@Composable
fun FeedCommentsScreen(postId: String, onBack: () -> Unit) {
    val server = LocalServerData.current
    val feedId = postId.removePrefix("srv-").toLongOrNull()?.takeIf { postId.startsWith("srv-") }
    var feed by remember(feedId) { mutableStateOf<ServerFeed?>(null) }
    var comments by remember(feedId) { mutableStateOf<List<FeedComment>>(emptyList()) }
    // 다음 묶음 커서. null 이면 더 받을 게 없다는 뜻이라 화면 끝에 닿아도 부르지 않는다.
    var nextCommentCursor by remember(feedId) { mutableStateOf<Long?>(null) }
    var loadingMoreComments by remember(feedId) { mutableStateOf(false) }
    val commentListState = rememberLazyListState()
    var loadFailed by remember(feedId) { mutableStateOf(false) }
    var draft by rememberSaveable(postId) { mutableStateOf("") }
    var myProfile by remember(server) { mutableStateOf<ServerUserProfile?>(null) }
    // 댓글 「신고」 — 확인 단계를 거친다. 되돌리기 어려운 차단이 실제로 일어나는 자리다.
    // 댓글 신고는 접수 API 가 없다 — 안내만 한다. 차단은 신고와 분리해 따로 확인받는다(정본 §3).
    var showReportNotice by remember(feedId) { mutableStateOf(false) }
    var blockTarget by remember(feedId) { mutableStateOf<FeedComment?>(null) }
    var reportMessage by remember(feedId) { mutableStateOf<String?>(null) }
    val commentScope = rememberCoroutineScope()

    LaunchedEffect(feedId, server) {
        if (feedId == null || server == null) return@LaunchedEffect
        val loaded = runCatching { server.feeds.feed(feedId) }.getOrNull()
        feed = loaded
        loadFailed = loaded == null
        val firstPage = runCatching { server.feeds.comments(feedId, limit = FEED_COMMENTS_PAGE_SIZE) }.getOrNull()
        comments = firstPage?.comments.orEmpty()
        nextCommentCursor = firstPage?.nextId
        myProfile = runCatching { server.userProfile.profile() }.getOrNull()
    }

    // 목록 끝이 보이면 다음 묶음을 받는다. `nextId` 가 null 이면 아무 것도 하지 않는다.
    val reachedCommentListEnd by remember(commentListState) {
        derivedStateOf { !commentListState.canScrollForward }
    }
    LaunchedEffect(reachedCommentListEnd, nextCommentCursor, feedId, server) {
        val cursor = nextCommentCursor ?: return@LaunchedEffect
        if (!reachedCommentListEnd || feedId == null || server == null || loadingMoreComments) return@LaunchedEffect
        loadingMoreComments = true
        val page = runCatching {
            server.feeds.comments(feedId, beforeCommentId = cursor, limit = FEED_COMMENTS_PAGE_SIZE)
        }.getOrNull()
        if (page != null) {
            // 이미 가진 댓글과 겹치면 버린다 — 커서가 같은 값으로 되돌아와도 중복 키로 깨지지 않는다.
            val known = comments.mapTo(mutableSetOf()) { it.commentId }
            comments = comments + page.comments.filterNot { it.commentId in known }
            // 커서가 앞으로 나아가지 못하면 멈춘다(같은 묶음을 무한히 다시 받지 않는다).
            //
            // 순수한 안전망이다. 예전 주석은 "서버가 페이지가 꽉 차면 다음 페이지 유무와
            // 무관하게 마지막 ID 를 nextId 로 준다" 고 적혀 있었는데 **사실이 아니다** —
            // 실서버로 끝까지 따라가 확인했다(2026-09-03, 피드 1 · limit=1):
            //   ?limit=1                    → [3] nextId 3
            //   ?beforeCommentId=3&limit=1  → [2] nextId 2
            //   ?beforeCommentId=2&limit=1  → [1] nextId null   ← 정확히 끝난다
            // `limit=3` (꽉 찬 마지막 페이지)에서도 nextId 는 null 이다.
            // 그래도 커서가 되돌아오는 응답을 받으면 화면이 멈춰야 하므로 가드는 남긴다.
            nextCommentCursor = page.nextId?.takeIf { it < cursor }
        } else {
            // 실패하면 커서를 버려 같은 요청을 스크롤마다 되풀이하지 않는다.
            nextCommentCursor = null
        }
        loadingMoreComments = false
    }

    ChangeLogScaffold(
        title = "댓글 ${feed?.commentCount ?: 0}",
        onBack = onBack,
        modifier = Modifier.testTag("feed-comments-screen-$postId"),
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UserAvatar(
                        imageUrl = myProfile?.profileImageUrl,
                        nickname = myProfile?.nickname,
                        modifier = Modifier.size(34.dp),
                        fallbackFontSize = 16.sp
                    )
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        placeholder = { Text("댓글을 남겨주세요") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp)
                    )
                    FilledIconButton(
                        onClick = {
                            val trimmed = draft.trim()
                            if (trimmed.isNotEmpty() && feedId != null && server != null) {
                                commentScope.launch {
                                    runCatching { server.feeds.addComment(feedId, trimmed) }
                                        .onSuccess {
                                            // 댓글을 달면 **첫 묶음부터 다시 읽는다**(웹과 같은 결정).
                                            // 이미 받아 둔 여러 묶음 사이에 새 댓글을 손으로 끼워
                                            // 넣으면 서버 순서를 클라가 흉내내야 해서 어긋난다.
                                            draft = ""
                                            feed = feed?.let { it.copy(commentCount = it.commentCount + 1) }
                                            val reloaded = runCatching {
                                                server.feeds.comments(feedId, limit = FEED_COMMENTS_PAGE_SIZE)
                                            }.getOrNull()
                                            if (reloaded != null) {
                                                comments = reloaded.comments
                                                nextCommentCursor = reloaded.nextId
                                                commentListState.scrollToItem(0)
                                            }
                                        }
                                }
                            }
                        },
                        enabled = draft.isNotBlank() && feedId != null && server != null,
                        modifier = Modifier.size(48.dp)
                    ) { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "댓글 보내기") }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).testTag("feed-comments-scroll"),
            state = commentListState,
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            feed?.let { loadedFeed ->
                item {
                    // 어떤 게시물의 댓글인지 위에서 알려준다 (화면기획 23-1)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        UserAvatar(
                            imageUrl = loadedFeed.author.profileImageUrl,
                            nickname = loadedFeed.author.nickname,
                            modifier = Modifier.size(44.dp),
                            fallbackFontSize = 20.sp
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                loadedFeed.trip?.courseTitle ?: loadedFeed.content,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "${loadedFeed.author.nickname} · 좋아요 ${loadedFeed.likeCount}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            reportMessage?.let { message ->
                item {
                    Text(
                        message,
                        modifier = Modifier.testTag("feed-comment-report-result"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            when {
                feedId == null || server == null -> item {
                    MoyeoEmptyState(MoyeoEmptyText.NO_COMMENTS, testTag = "feed-comments-empty")
                }

                feed == null -> item {
                    MoyeoEmptyState(if (loadFailed) MoyeoEmptyText.FAILED else MoyeoEmptyText.LOADING)
                }

                comments.isEmpty() -> item {
                    MoyeoEmptyState(MoyeoEmptyText.NO_COMMENTS, testTag = "feed-comments-empty")
                }

                else -> {
                    items(comments, key = { it.commentId }) { comment ->
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            FeedCommentRow(
                                comment,
                                onReport = { showReportNotice = true },
                                onBlock = { blockTarget = comment }
                            )
                            comment.replies.forEach { reply ->
                                // 대댓글은 들여쓰기로 부모와의 관계를 보여준다
                                Box(Modifier.padding(start = 34.dp)) {
                                    FeedCommentRow(
                                        reply,
                                        compact = true,
                                        onReport = { showReportNotice = true },
                                        onBlock = { blockTarget = reply }
                                    )
                                }
                            }
                        }
                    }
                    // 다음 묶음이 남아 있을 때만 꼬리 자리를 둔다. 이 자리가 보이면 위 커서가 돈다.
                    if (nextCommentCursor != null) {
                        item(key = "feed-comments-more") {
                            MoyeoEmptyState(MoyeoEmptyText.LOADING, testTag = "feed-comments-loading-more")
                        }
                    }
                }
            }
        }
    }
    if (showReportNotice) {
        // 댓글 신고 접수 API 는 서버에 없다 — 접수된 것처럼 보이게 하지 않고 문의로 안내한다.
        ReportUnsupportedDialog(onDismiss = { showReportNotice = false })
    }
    blockTarget?.let { comment ->
        // 차단은 신고와 별개로 **실제로 반영된다**(POST users/me/blocks/{userId}).
        AlertDialog(
            onDismissRequest = { blockTarget = null },
            modifier = Modifier.testTag("feed-comment-block-dialog"),
            title = { Text("${comment.author.nickname}님을 차단할까요?") },
            text = {
                Text("차단하면 이 유저가 만들었거나 참여한 모집이 홈·탐색에서 모두 숨겨져요.")
            },
            confirmButton = {
                TextButton(onClick = {
                    blockTarget = null
                    if (server != null) {
                        commentScope.launch {
                            runCatching { server.social.block(comment.author.userId) }
                                .onSuccess {
                                    reportMessage = "${comment.author.nickname}님을 차단했어요."
                                    // 차단하면 목록이 달라질 수 있다 — 첫 묶음부터 다시 읽는다.
                                    // 커서도 새 응답 값으로 되돌린다(예전 커서를 들고 있으면
                                    // 사라진 댓글 뒤부터 이어 받아 구멍이 생긴다).
                                    if (feedId != null) {
                                        val reloaded = runCatching {
                                            server.feeds.comments(feedId, limit = FEED_COMMENTS_PAGE_SIZE)
                                        }.getOrNull()
                                        if (reloaded != null) {
                                            comments = reloaded.comments
                                            nextCommentCursor = reloaded.nextId
                                        }
                                    }
                                }
                                .onFailure { error ->
                                    reportMessage = error.message ?: "차단에 실패했어요."
                                }
                        }
                    }
                }) { Text("차단하기", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { blockTarget = null }) { Text("취소") }
            }
        )
    }
}

/**
 * 댓글 한 줄.
 *
 * 우측 점 세 개 메뉴는 두지 않는다 — 메뉴 안에 숨으면 신고 경로가 있는지조차 알 수 없다.
 * 좋아요·"함께 간 친구" 배지는 서버 댓글 응답에 없어 두지 않는다(§4 BE 요청).
 */
@Composable
private fun FeedCommentRow(comment: FeedComment, compact: Boolean = false, onReport: () -> Unit, onBlock: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
        UserAvatar(
            imageUrl = comment.author.profileImageUrl,
            nickname = comment.author.nickname,
            modifier = Modifier.size(if (compact) 28.dp else 36.dp),
            fallbackFontSize = if (compact) 13.sp else 16.sp
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    comment.author.nickname,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    comment.createdAt.take(10).replace('-', '.'),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(comment.content, style = MaterialTheme.typography.bodyMedium)
            // 신고와 차단은 결과가 다르다 — 한 줄에 섞지 않고 두 행동으로 나눈다(정본 §3).
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "신고",
                    modifier = Modifier
                        .clickable(role = Role.Button, onClick = onReport)
                        .testTag("feed-comment-report"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "차단",
                    modifier = Modifier
                        .clickable(role = Role.Button, onClick = onBlock)
                        .testTag("feed-comment-block"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/** 방해금지 시작·종료 시간 필드. 라벨 위 / 값 아래 (화면기획). */
@Composable
private fun QuietHourField(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold)
        Surface(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            color = MoyeoTheme.cardSurface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                Modifier.padding(horizontal = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    value,
                    Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
