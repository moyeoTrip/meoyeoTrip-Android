package kr.hanchae.moyeotrip.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import kr.hanchae.moyeotrip.data.MockTripRepository
import kr.hanchae.moyeotrip.data.ServerDataDependencies
import kr.hanchae.moyeotrip.data.notifications.NotificationSettingsUpdate
import kr.hanchae.moyeotrip.data.rooms.ChatRoomDetail
import kr.hanchae.moyeotrip.data.rooms.RoomMember
import kr.hanchae.moyeotrip.data.rooms.RoomMembers
import kr.hanchae.moyeotrip.data.rooms.RoomNotices
import kr.hanchae.moyeotrip.data.rooms.RoomRoadmap
import kr.hanchae.moyeotrip.data.rooms.recruitmentDDayText
import kr.hanchae.moyeotrip.ui.LocalServerData
import kr.hanchae.moyeotrip.ui.components.AnimalAvatar
import kr.hanchae.moyeotrip.ui.components.OverlayBackdrop
import kr.hanchae.moyeotrip.ui.components.emphasized
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
    onOpenNotificationSettings: () -> Unit,
    onOpenReport: () -> Unit,
    onOpenNotices: (String) -> Unit,
    onOpenRoute: (String) -> Unit,
    // / 20-1a 캡처용 — 멤버 액션 시트를 처음부터 열어 둔다 (qaApply 선례)
    showActionsSheetInitially: Boolean = false,
    // / 20-1b 캡처용 — 사유 입력 시트를 처음부터 열어 둔다
    showRemoveSheetInitially: Boolean = false
) {
    // "room-{id}" 는 실서버 모임이다 — 서버 멤버·공지·로드맵을 읽어 보여준다
    val server = LocalServerData.current
    val serverRoomId = threadId.serverRoomIdOrNull()
    if (serverRoomId != null && server != null) {
        ServerChatMenu(
            roomId = serverRoomId,
            server = server,
            onBack = onBack,
            onOpenNotices = onOpenNotices,
            onOpenNotificationSettings = onOpenNotificationSettings,
            onOpenReport = onOpenReport
        )
        return
    }
    val thread = MockTripRepository.findThread(threadId)
    val trip = thread.tripId?.let(MockTripRepository::findTrip)
    // 화면기획 20-1은 전원 "매너 4.8 · 여행 8회"로 표기하고, 역할(호스트·나)은 우측 칩으로 둔다
    val members = listOf(
        FriendEntry("🐻", "숲속여행자", "매너 4.8 · 여행 8회"),
        FriendEntry("🦌", "따스한 사슴 3492", "매너 4.8 · 여행 8회"),
        FriendEntry("🐰", "엉뚱한 토끼 1457", "매너 4.8 · 여행 8회"),
        FriendEntry("🐢", "잔잔한 거북이 9032", "매너 4.8 · 여행 8회"),
        FriendEntry("🦝", "호기심 많은 너구리 9027", "매너 4.8 · 여행 8회")
    )
    val memberRoles = mapOf("숲속여행자" to "호스트", "따스한 사슴 3492" to "나")
    // ⋯은 두 단계다 — 20-1a 멤버 액션 시트를 먼저 열고, 내보내기를 고르면 20-1b 사유 시트로 넘어간다.
    // 화면기획 캡처의 기본 대상은 둘 다 너구리 9027.
    var actionTargetName by rememberSaveable {
        mutableStateOf(if (showActionsSheetInitially) "호기심 많은 너구리 9027" else null)
    }
    var removeTargetName by rememberSaveable {
        mutableStateOf(if (showRemoveSheetInitially) "호기심 많은 너구리 9027" else null)
    }
    val actionTarget = members.firstOrNull { it.name == actionTargetName }
    val removeTarget = members.firstOrNull { it.name == removeTargetName }
    ChangeLogScaffold(title = "모임 정보", onBack = onBack, modifier = Modifier.testTag("chat-menu-screen")) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                    Text(
                        trip?.recruitmentName ?: thread.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    trip?.let {
                        Text(
                            "🗺 ${it.title}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Text(
                        "5/25(토) 당일치기 · 08:00 – 18:00",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    if (trip != null) {
                        Row(
                            modifier = Modifier.padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "1인 ${"%,d".format(trip.estimatedCostPerPerson)}원",
                                if (trip.ddayLabel.startsWith("마감")) trip.ddayLabel else "마감 ${trip.ddayLabel}",
                                "${trip.minimumAge}~${trip.maximumAge}세",
                                trip.genderCondition
                            ).forEach { label ->
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
                    Text(
                        "07:50 청송 시외버스터미널 정문 앞 집합",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.padding(top = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 화면기획 순서: 모집 상세 → 여행 경로, 아이콘 없이 초록 외곽선
                        OutlinedButton(
                            onClick = { trip?.id?.let(onOpenNotices) },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Text("모집 상세", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { trip?.id?.let(onOpenRoute) },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Text("여행 경로", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                HorizontalDivider(thickness = 8.dp, color = MaterialTheme.colorScheme.surfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("동행자  ${members.size}", fontWeight = FontWeight.ExtraBold)
                    Text("최대 5명 · 대기 1명", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(members) { member ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AnimalAvatar(member.emoji, modifier = Modifier.size(42.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(member.name, fontWeight = FontWeight.Bold)
                        Text(
                            member.subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // 화면기획 20-1: 호스트·나는 역할 칩, 나머지 멤버만 ⋯ 로 액션 시트를 연다
                    val role = memberRoles[member.name].orEmpty()
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
                            onClick = { actionTargetName = member.name },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Filled.MoreHoriz, contentDescription = "${member.name} 관리")
                        }
                    }
                }
            }
            item {
                Text(
                    "호스트는 멤버 우측 메뉴에서 내보내기를 할 수 있어요. 내보낸 자리는 대기 큐에서 자동으로 채워져요.",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(thickness = 8.dp, color = MaterialTheme.colorScheme.surfaceVariant)
                listOf(
                    MenuEntry(Icons.AutoMirrored.Filled.StickyNote2, "공지", "고정 2개 · 전체 4개") {
                        trip?.id?.let(onOpenNotices)
                    },
                    MenuEntry(Icons.Filled.Image, "공유된 항목", "사진 12 · 장소 4 · 투표 2", onClick = onOpenSpecialMessages),
                    MenuEntry(
                        Icons.Filled.Notifications,
                        "알림 설정",
                        "이 모임의 알림과 방해금지 시간",
                        onClick = onOpenNotificationSettings
                    ),
                    MenuEntry(Icons.Filled.Flag, "신고 · 차단", "부적절한 대화나 멤버를 신고해요", onClick = onOpenReport),
                    MenuEntry(Icons.Filled.Close, "채팅방 나가기", "다음 신청자가 자동으로 합류해요", danger = true, onClick = onBack)
                ).forEach { ActionRow(it) }
            }
        }
    }
    if (actionTarget != null) {
        MemberActionsSheet(
            member = actionTarget,
            onRemove = {
                removeTargetName = actionTarget.name
                actionTargetName = null
            },
            onDismiss = { actionTargetName = null }
        )
    }
    if (removeTarget != null) {
        MemberRemoveSheet(member = removeTarget, onDismiss = { removeTargetName = null })
    }
}

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
    onOpenNotificationSettings: () -> Unit,
    onOpenReport: () -> Unit
) {
    var detail by remember(roomId) { mutableStateOf<ChatRoomDetail?>(null) }
    var members by remember(roomId) { mutableStateOf<RoomMembers?>(null) }
    var notices by remember(roomId) { mutableStateOf<RoomNotices?>(null) }
    var roadmap by remember(roomId) { mutableStateOf<RoomRoadmap?>(null) }
    var actionTarget by remember(roomId) { mutableStateOf<RoomMember?>(null) }
    var removeTarget by remember(roomId) { mutableStateOf<RoomMember?>(null) }
    var actionMessage by remember(roomId) { mutableStateOf<String?>(null) }
    var roomAlertsEnabled by remember(roomId) { mutableStateOf<Boolean?>(null) }
    var showLeaveConfirm by remember(roomId) { mutableStateOf(false) }
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
                        onClick = onOpenNotificationSettings
                    ),
                    MenuEntry(Icons.Filled.Flag, "신고 · 차단", "부적절한 대화나 멤버를 신고해요", onClick = onOpenReport)
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
    val target = actionTarget
    if (target != null) {
        AlertDialog(
            onDismissRequest = { actionTarget = null },
            title = { Text(target.nickname) },
            text = {
                Text(
                    if (amHost) {
                        "친구 신청을 보내거나 이 사용자를 차단하거나, 모임에서 내보낼 수 있어요."
                    } else {
                        "친구 신청을 보내거나 이 사용자를 차단할 수 있어요."
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    actionTarget = null
                    actionScope.launch {
                        runCatching { server.social.sendRequest(target.userId) }
                            .onSuccess { actionMessage = "${target.nickname}님에게 친구 신청을 보냈어요." }
                            .onFailure { error -> actionMessage = error.message ?: "친구 신청에 실패했어요." }
                    }
                }) { Text("친구 신청") }
            },
            dismissButton = {
                Row {
                    // 내보내기는 호스트에게만 보인다 (화면기획 20-1a)
                    if (amHost) {
                        TextButton(
                            onClick = {
                                removeTarget = target
                                actionTarget = null
                            },
                            modifier = Modifier.testTag("server-member-remove")
                        ) {
                            Text("내보내기", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    TextButton(onClick = {
                        actionTarget = null
                        actionScope.launch {
                            runCatching { server.social.block(target.userId) }
                                .onSuccess { actionMessage = "${target.nickname}님을 차단했어요." }
                                .onFailure { error -> actionMessage = error.message ?: "차단에 실패했어요." }
                        }
                    }) { Text("차단", color = MaterialTheme.colorScheme.error) }
                }
            }
        )
    }
    // 20-1b 사유 입력 시트 — 목데이터 화면과 같은 시트를 재사용하고 확인에서만 서버를 부른다
    removeTarget?.let { member ->
        MemberRemoveSheet(
            member = FriendEntry(emoji = "🙂", name = member.nickname, subtitle = "여행 ${member.completedTripCount}회"),
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
        // 화면기획 31 — 호스트가 나가면 모임이 종료된다는 경고를 먼저 보여준다
        AlertDialog(
            onDismissRequest = { showLeaveConfirm = false },
            title = { Text(if (amHost) "호스트가 나가면 이 모임은 종료돼요" else "이 모임에서 나갈까요?") },
            text = {
                Text(
                    if (amHost) {
                        "참여한 멤버 모두에게 알림이 가고, 채팅방은 읽기 전용으로 남아요."
                    } else {
                        "나가면 대기 중인 다음 신청자가 자동으로 합류해요."
                    }
                )
            },
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
                    Text(if (amHost) "모임 종료" else "나가기", color = MaterialTheme.colorScheme.error)
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
        onOpenReport = {},
        onOpenNotices = {},
        onOpenRoute = {}
    )
}

// / 20-1a 멤버 액션 시트 (changeLog14) — ⋯의 첫 단계.
// / 내보내기 행은 호스트에게만 보인다는 전제이고, 캡처 화면에서는 항상 표시한다.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemberActionsSheet(member: FriendEntry, onRemove: () -> Unit, onDismiss: () -> Unit) {
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
                AnimalAvatar(member.emoji, modifier = Modifier.size(40.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(member.name, fontWeight = FontWeight.ExtraBold)
                    Text(
                        text = "${member.subtitle} · 어제 합류",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            MemberActionRow(
                icon = Icons.Outlined.AccountCircle,
                title = "친구 요청하기",
                iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                titleColor = MaterialTheme.colorScheme.onSurface,
                tag = "member-actions-friend",
                onClick = {}
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
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clickable(role = Role.Button, onClick = onClick)
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
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// / 20-1b 멤버 내보내기 사유 시트 (changeLog14) — 사유는 자유 서술 하나(10자 이상)이고,
// / 입력 전에는 내보내기가 비활성이다. 이 사유가 그대로 상대의 13-1 내보내기 안내에 보인다.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemberRemoveSheet(
    member: FriendEntry,
    onDismiss: () -> Unit,
    /** 실서버 방에서만 채운다 — 목데이터·캡처 경로는 닫기만 한다. */
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
                    AnimalAvatar(member.emoji, modifier = Modifier.size(40.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(member.name, fontWeight = FontWeight.ExtraBold)
                        Text(
                            text = "${member.subtitle} · 어제 합류",
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

/**
 * 20-2 첨부 시트.
 *
 * 실서버 방(`room-{id}`)에서는 기획에 이미 자리가 있는 두 타일이 바로 서버를 부른다 —
 * "지도"는 POST chat-rooms/{id}/messages/locations(본문 없음), "사진"은 시스템 사진 선택기로 고른 파일을
 * POST chat-rooms/{id}/messages/images(multipart)로 보낸다.
 * "장소"·"투표"·"정산"은 서버 API 는 있지만 기획에 입력 화면(장소 고르기·질문/선택지·메모)이 없어
 * 기존 진입(특수 메시지 6종)을 그대로 두고 저장소 배선만 해뒀다.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatAttachmentScreen(
    onBack: () -> Unit,
    onOpenSpecialMessages: () -> Unit,
    isOnline: Boolean,
    /** 공유 대상 방. "room-{id}" 면 실서버로 보낸다. 캡처 라우트는 null 로 들어와 목데이터 방을 쓴다. */
    threadId: String? = null
) {
    val backdropThreadId = threadId ?: OVERLAY_BACKDROP_THREAD_ID
    val items = listOf(
        Triple(Icons.Filled.CameraAlt, "사진", "최대 20MB · 1장씩"),
        Triple(Icons.Filled.LocationOn, "장소", "TourAPI 장소 카드"),
        Triple(Icons.Filled.Map, "지도", "만날 위치 핀 공유"),
        Triple(Icons.Filled.Poll, "투표", "2~5개 · 익명 기본"),
        Triple(Icons.Filled.Payments, "정산", "메모용 · 송금 아님"),
        Triple(Icons.AutoMirrored.Filled.StickyNote2, "메모", "상단 고정 공지")
    )
    val server = LocalServerData.current
    val serverRoomId = threadId?.serverRoomIdOrNull()
    val context = LocalContext.current
    val shareScope = rememberCoroutineScope()
    var shareBusy by remember(serverRoomId) { mutableStateOf(false) }
    var shareMessage by remember(serverRoomId) { mutableStateOf<String?>(null) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null || serverRoomId == null || server == null) return@rememberLauncherForActivityResult
        shareBusy = true
        shareScope.launch {
            val picked = readPickedImage(context, uri)
            if (picked == null) {
                shareMessage = "사진을 읽지 못했어요."
                shareBusy = false
                return@launch
            }
            runCatching {
                server.chatRooms.shareImage(
                    roomId = serverRoomId,
                    fileName = picked.fileName,
                    mimeType = picked.mimeType,
                    bytes = picked.bytes
                )
            }
                .onSuccess { onBack() }
                .onFailure { error -> shareMessage = error.message ?: "사진을 보내지 못했어요." }
            shareBusy = false
        }
    }

    fun shareMeetingLocation() {
        if (serverRoomId == null || server == null || shareBusy) return
        shareBusy = true
        shareScope.launch {
            runCatching { server.chatRooms.shareMeetingLocation(serverRoomId) }
                .onSuccess { onBack() }
                .onFailure { error -> shareMessage = error.message ?: "만날 위치를 보내지 못했어요." }
            shareBusy = false
        }
    }

    val serverActions: Map<String, () -> Unit> = if (serverRoomId != null && server != null) {
        mapOf(
            "지도" to ::shareMeetingLocation,
            "사진" to {
                photoPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )
    } else {
        emptyMap()
    }

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
                        val serverAction = serverActions[item.second]
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(116.dp)
                                .clickable(enabled = isOnline && !shareBusy) {
                                    serverAction?.invoke() ?: onOpenSpecialMessages()
                                }
                                .testTag("chat-attach-${item.second}")
                                .semantics {
                                    role = Role.Button
                                    contentDescription = if (isOnline) item.second else "${item.second}, 오프라인에서 사용 불가"
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
                                        item.first,
                                        contentDescription = null,
                                        modifier = Modifier.padding(10.dp).size(24.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Text(item.second, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
                                Text(
                                    item.third,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
                shareMessage?.let { message ->
                    Text(
                        message,
                        modifier = Modifier.padding(top = 10.dp).testTag("chat-attach-error"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
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
fun FriendsScreen(onBack: () -> Unit, onOpenDex: () -> Unit) {
    var tab by rememberSaveable { mutableStateOf(0) }
    var removeTarget by remember { mutableStateOf<FriendEntry?>(null) }
    // 로그인 상태면 실서버 친구·신청 목록으로 대체한다 — 실패 시 목데이터 유지
    val server = LocalServerData.current
    var serverFriends by remember(server) { mutableStateOf<ServerFriendLists?>(null) }
    val friendScope = rememberCoroutineScope()
    LaunchedEffect(server) {
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

    /** 친구 삭제 — DELETE users/me/friends/{userId} (friendshipId 가 아니다). */
    fun removeFriend(userId: Long) {
        friendScope.launch {
            runCatching { server?.social?.removeFriend(userId) }.onSuccess { reloadFriendLists() }
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
    } ?: listOf(
        listOf(
            FriendEntry("🐻", "우직한 곰 7821", "함께 여행 3회 · 어제 접속"),
            FriendEntry("🐰", "엉뚱한 토끼 1457", "함께 여행 1회 · 3일 전 접속"),
            FriendEntry("🐢", "잔잔한 거북이 9032", "함께 여행 2회 · 오늘 접속")
        ),
        listOf(
            FriendEntry("🦝", "호기심 많은 너구리 9027", "포항·영덕 드라이브에서 만났어요"),
            FriendEntry("🪽", "고요한 두루미 1130", "경주 단풍·야경에서 만났어요")
        ),
        listOf(FriendEntry("🦌", "따스한 사슴 3492", "어제 신청 · 수락 대기 중"))
    )
    ChangeLogScaffold(
        title = "친구 관리",
        onBack = onBack,
        modifier = Modifier.testTag("friends-screen"),
        actions = {
            IconButton(onClick = {}, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Filled.Search, contentDescription = "친구 검색")
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
                if (serverFriends != null && lists[tab].isEmpty()) {
                    item {
                        Text(
                            when (tab) {
                                0 -> "아직 친구가 없어요."
                                1 -> "받은 친구 신청이 없어요."
                                else -> "보낸 친구 신청이 없어요."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                }
                items(lists[tab]) { friend ->
                    Row(
                        modifier = Modifier.fillMaxWidth().height(68.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (serverFriends != null) {
                            UserAvatar(
                                imageUrl = friend.imageUrl,
                                nickname = friend.name,
                                modifier = Modifier.size(44.dp),
                                fallbackFontSize = 20.sp
                            )
                        } else {
                            AnimalAvatar(friend.emoji, modifier = Modifier.size(44.dp))
                        }
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
                                onClick = { if (serverFriends != null) removeTarget = friend },
                                modifier = Modifier.size(48.dp)
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
    val friendToRemove = removeTarget
    if (friendToRemove?.userId != null) {
        AlertDialog(
            onDismissRequest = { removeTarget = null },
            title = { Text("${friendToRemove.name}님을 친구에서 삭제할까요?") },
            text = { Text("친구를 삭제하면 서로의 피드 구독이 끊겨요. 함께한 여행 기록(도감)은 그대로 남아요.") },
            confirmButton = {
                TextButton(onClick = {
                    removeTarget = null
                    removeFriend(friendToRemove.userId)
                }) { Text("친구 삭제", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { removeTarget = null }) { Text("취소") }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TripMessageScreen(
    onBack: () -> Unit,
    onOpenFeedWrite: () -> Unit,
    onOpenCoursePublish: () -> Unit,
    onOpenDex: () -> Unit
) {
    val names = listOf("우직한 곰 7821", "엉뚱한 토끼 1457", "잔잔한 거북이 9032")
    val emojis = listOf("🐻", "🐰", "🐢")
    val messages = remember { mutableStateListOf("핑크뮬리 사진 잘 찍어주셔서 고마워요!", "", "") }
    val presets = listOf("덕분에 즐거웠어요", "사진 고마워요!", "다음에도 잘 부탁드려요")
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
                        onClick = onOpenDex,
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
                Text("함께 걸어준 친구들에게\n한 줄 남겨볼까요?", style = MaterialTheme.typography.headlineSmall)
                Text(
                    emphasized(
                        "남긴 메시지는 상대방의 도감 카드 뒷면에 적혀요. 안 남겨도 카드는 그대로 모여요.",
                        "도감 카드 뒷면"
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(names.indices.toList()) { index ->
                RoundedPanel(
                    containerColor = if (messages[index].isNotBlank()) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AnimalAvatar(emojis[index], modifier = Modifier.size(40.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(names[index], fontWeight = FontWeight.ExtraBold)
                            Text(
                                if (messages[index].isBlank()) "아직 안 남겼어요" else "메시지를 남겼어요",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (messages[index].isNotBlank()) Icon(Icons.Filled.Check, contentDescription = "작성 완료")
                    }
                    OutlinedTextField(
                        value = messages[index],
                        onValueChange = { messages[index] = it.take(40) },
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        placeholder = { Text("한 줄 메시지를 남겨주세요 (최대 40자)") },
                        minLines = 2,
                        supportingText = { Text("${messages[index].length}/40") }
                    )
                    if (messages[index].isBlank()) {
                        FlowRow(
                            modifier = Modifier.padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            presets.forEach { preset ->
                                OutlinedButton(onClick = {
                                    messages[index] = preset
                                }, shape = RoundedCornerShape(12.dp)) { Text(preset) }
                            }
                        }
                    }
                }
            }
            item {
                RelatedActionCard(Icons.Filled.ChatBubbleOutline, "경로가 담긴 피드도 이어서 써볼까요?", "피드 쓰기", onOpenFeedWrite)
                Spacer(Modifier.height(10.dp))
                RelatedActionCard(Icons.Filled.Map, "이 코스를 다른 여행자에게 열어둘 수도 있어요", "코스 공개", onOpenCoursePublish)
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

@Composable
fun ReportScreen(onBack: () -> Unit, backdropThreadId: String = OVERLAY_BACKDROP_THREAD_ID) {
    val reasons = listOf("스팸 · 도박", "성희롱 · 불쾌한 언행", "돈거래 유도", "허위 정보", "부적절한 내용", "기타")
    var selected by rememberSaveable { mutableStateOf(reasons[1]) }
    var block by rememberSaveable { mutableStateOf(true) }
    // 화면기획 32는 전체 화면이 아니라 채팅방 위로 올라오는 바텀시트다 —
    // changeLog14 "오버레이 배경 일괄": 빈 딤 대신 실제 채팅방 본문을 깐다.
    OverlayBackdrop(
        modifier = Modifier.testTag("report-screen"),
        scrimAlpha = .45f,
        onScrimClick = onBack,
        background = { ChatRoomBody(threadId = backdropThreadId) }
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
                                Text("해당 메시지 · “계좌로 먼저 보내주시면…”", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    items(reasons) { reason ->
                        val chosen = selected == reason
                        Surface(
                            modifier = Modifier.fillMaxWidth().height(50.dp).clickable { selected = reason },
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
                                Text(
                                    reason,
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
                                    "취소",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Button(
                                onClick = onBack,
                                modifier = Modifier.weight(1f).height(52.dp).testTag("report-submit"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("신고하기") }
                        }
                        Text(
                            "24시간 이내에 검토해 드릴게요.",
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BlockedUsersScreen(onBack: () -> Unit) {
    // 로그인 상태면 실서버 차단 목록(GET users/me/blocks)으로 대체한다
    val server = LocalServerData.current
    var serverBlocked by remember(server) {
        mutableStateOf<List<kr.hanchae.moyeotrip.data.social.BlockedUser>?>(null)
    }
    val blockScope = rememberCoroutineScope()
    LaunchedEffect(server) {
        serverBlocked = if (server == null) null else runCatching { server.social.blocks() }.getOrNull()
    }
    val blocked = remember {
        mutableStateListOf(
            FriendEntry("🦝", "말많은 너구리 7791", "2026.07.28 차단 · 채팅방에서 신고와 함께 차단"),
            FriendEntry("🪽", "청아한 두루미 2024", "2026.06.02 차단 · 프로필에서 차단")
        )
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
            val serverList = serverBlocked
            if (serverList != null) {
                if (serverList.isEmpty()) {
                    item {
                        Text(
                            "차단한 사용자가 없어요.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
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
                            onClick = {
                                blockScope.launch {
                                    runCatching { server?.social?.unblock(user.userId) }
                                        .onSuccess {
                                            serverBlocked = serverBlocked?.filterNot { it.userId == user.userId }
                                        }
                                }
                            },
                            modifier = Modifier.height(34.dp),
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
            } else {
                items(blocked, key = { it.name }) { user ->
                    Row(
                        modifier = Modifier.fillMaxWidth().height(72.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnimalAvatar(user.emoji, modifier = Modifier.size(42.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.name, fontWeight = FontWeight.ExtraBold)
                            Text(
                                user.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = { blocked.remove(user) },
                            modifier = Modifier.height(34.dp),
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

@Composable
fun CoursePublishScreen(onBack: () -> Unit, onPublished: () -> Unit) {
    var showConfirmation by rememberSaveable { mutableStateOf(false) }
    var showFinalConfirmation by rememberSaveable { mutableStateOf(false) }
    var credit by rememberSaveable { mutableStateOf(true) }
    var title by rememberSaveable { mutableStateOf("주왕산 & 주산지 힐링 트레킹") }
    var summary by rememberSaveable { mutableStateOf("기암절벽과 주산지 물안개를 천천히 걷는 코스") }
    val course = remember { MockTripRepository.findCourse("cheongsong-juwangsan") }
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
                        modifier = Modifier.weight(1f).height(50.dp).testTag("course-publish-start"),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("코스 공개하기") }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
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
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(132.dp)) {
                        CourseScenicPanel(course = course, modifier = Modifier.fillMaxSize(), cornerRadius = 0.dp)
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
                        Text(
                            "청송 · 당일 6.2km · 방문지 4",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Row(
                            modifier = Modifier.padding(top = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            AnimalAvatar("🐻", modifier = Modifier.size(24.dp))
                            Text(
                                if (credit) "숲속여행자 님이 다녀온 코스" else "익명 여행자가 다녀온 코스",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
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
                    onClick = onPublished,
                    modifier = Modifier.testTag("course-publish-confirm-final"),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("공개할게요") }
            }
        )
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

@Composable
fun TripDayScreen(
    threadId: String,
    onBack: () -> Unit,
    onOpenMenu: () -> Unit,
    onOpenAttachment: () -> Unit,
    onOpenRoute: () -> Unit
) {
    val stops = listOf("청송터미널", "주왕산", "주산지", "달기약수탕")
    val thread = MockTripRepository.findThread(threadId)
    ChangeLogScaffold(
        // 여행 날 채팅방 제목은 코스 이름이다 (화면기획 20-5)
        title = thread.courseLine.ifBlank { thread.title },
        onBack = onBack,
        modifier = Modifier.testTag("trip-day-screen"),
        actions = {
            IconButton(onClick = {}, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Filled.Search, contentDescription = "채팅 검색")
            }
            IconButton(onClick = onOpenMenu, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Filled.Menu, contentDescription = "모임 정보")
            }
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onOpenAttachment, modifier = Modifier.size(48.dp)) {
                        // 화면기획 20-5의 입력창 좌측은 클립이 아니라 + 다
                        Icon(Icons.Filled.Add, contentDescription = "첨부")
                    }
                    Surface(
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.padding(horizontal = 14.dp)) {
                            Text("메시지 입력", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    // 빈 입력 진입 상태에서는 전송이 비활성이다 (changeLog16)
                    FilledIconButton(onClick = {}, enabled = false, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "보내기")
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
                    Text(" · 5명 · 오늘 08:00 출발", style = MaterialTheme.typography.bodySmall)
                }
                Column(
                    modifier = Modifier.fillMaxWidth().background(
                        MaterialTheme.colorScheme.primaryContainer
                    ).padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("현재 방문지 2/4 · 주왕산", modifier = Modifier.weight(1f), fontWeight = FontWeight.ExtraBold)
                        TextButton(onClick = onOpenRoute) { Text("코스 전체") }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        stops.forEachIndexed { index, stop ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(58.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(22.dp),
                                    shape = CircleShape,
                                    color = if (index <
                                        2
                                    ) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(if (index < 2) "✓" else "${index + 1}", fontSize = 10.sp)
                                    }
                                }
                                Text(stop, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                            }
                            if (index < stops.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f).padding(top = 10.dp),
                                    thickness = 2.dp,
                                    color = if (index == 0) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                    }
                                )
                            }
                        }
                    }
                    Row(modifier = Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("다음 일정 · 14:00 주산지 왕버들 산책로", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            item {
                // 시스템 안내는 연초록 pill 안에 들어간다 (화면기획 20-5)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MoyeoTheme.tints.systemMessage
                    ) {
                        Text(
                            "오늘 여행이 시작됐어요 🎒",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                RoundedPanel(modifier = Modifier.padding(horizontal = 18.dp)) {
                    Text("엉뚱한 토끼 1457", style = MaterialTheme.typography.labelSmall)
                    Text("주왕산 3폭포 도착! 생각보다 사람 적어요 👍", modifier = Modifier.padding(top = 4.dp))
                }
                RoundedPanel(modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
                    TripDayMiniMap()
                    Text("주산지 주차장", fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 10.dp))
                    Text("14:00 도착 예정 · 차로 22분", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "길 찾기 →",
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text("저는 주차장에서 기다릴게요~", modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TripDayMiniMap() {
    val line = MaterialTheme.colorScheme.primary
    val canvas = MaterialTheme.colorScheme.surfaceVariant
    Canvas(
        modifier = Modifier.fillMaxWidth().height(92.dp).background(canvas, RoundedCornerShape(10.dp))
    ) {
        drawLine(
            color = line,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.12f, size.height * 0.80f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.88f, size.height * 0.18f),
            strokeWidth = 8f,
            cap = StrokeCap.Round
        )
        drawCircle(
            color = line,
            radius = 13f,
            center = androidx.compose.ui.geometry.Offset(size.width * 0.16f, size.height * 0.75f)
        )
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
    var muted by rememberSaveable { mutableStateOf(setOf("경주 단풍·야경 1박 2일")) }
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
    val joinedTrips = listOf("주왕산 & 주산지 힐링 트레킹 · D-2", "포항·영덕 동해 드라이브 · D-9")
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

@Composable
fun FeedCommentsScreen(postId: String, onBack: () -> Unit) {
    // 댓글은 대댓글까지 보여야 구조가 검수된다 (화면기획 기준 목데이터)
    val comments = remember {
        mutableStateListOf(
            FeedCommentItem(
                avatar = "🐰",
                author = "엉뚱한 토끼 1457",
                badge = "함께 간 친구",
                time = "2시간 전",
                body = "이날 진짜 좋았어요! 주산지 물안개 사진 저도 올릴게요 📷",
                likes = 4,
                replies = listOf(
                    FeedCommentItem(
                        avatar = "🐻",
                        author = "숲속여행자",
                        badge = "작성자",
                        time = "1시간 전",
                        body = "토끼님 사진이 훨씬 잘 나왔어요 ㅎㅎ",
                        likes = 0
                    )
                )
            ),
            FeedCommentItem(
                avatar = "🐢",
                author = "잔잔한 거북이 9032",
                badge = "함께 간 친구",
                time = "3시간 전",
                body = "달기약수탕 백숙 진짜 맛있었죠",
                likes = 2
            ),
            FeedCommentItem(
                avatar = "🕊",
                author = "고요한 두루미 1130",
                time = "5시간 전",
                body = "이 코스 저도 가보고 싶네요. 당일치기로 충분할까요?",
                likes = 1,
                replies = listOf(
                    FeedCommentItem(
                        avatar = "🐻",
                        author = "숲속여행자",
                        badge = "작성자",
                        time = "4시간 전",
                        body = "네 08시 출발이면 여유로워요!",
                        likes = 0
                    )
                )
            )
        )
    }
    var draft by rememberSaveable { mutableStateOf("") }
    // 제목의 숫자는 이 게시물의 전체 댓글 수다 (보이는 목록은 일부 샘플).
    // 목록 길이를 세면 같은 게시물인데 플랫폼마다 다른 숫자가 나온다.
    val totalCount = MockTripRepository.findFeedPost(postId).comments

    ChangeLogScaffold(
        title = "댓글 $totalCount",
        onBack = onBack,
        modifier = Modifier.testTag("feed-comments-screen-$postId"),
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnimalAvatar("🦌", modifier = Modifier.size(34.dp))
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
                            if (draft.isNotBlank()) {
                                comments.add(
                                    FeedCommentItem(
                                        avatar = "🦌",
                                        author = "따스한 사슴 3492",
                                        time = "방금",
                                        body = draft.trim(),
                                        likes = 0
                                    )
                                )
                                draft = ""
                            }
                        },
                        enabled = draft.isNotBlank(),
                        modifier = Modifier.size(48.dp)
                    ) { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "댓글 보내기") }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // 어떤 게시물의 댓글인지 위에서 알려준다 (화면기획 23-1)
                val post = MockTripRepository.findFeedPost(postId)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CourseScenicPanel(
                        course = MockTripRepository.findCourse("cheongsong-juwangsan"),
                        modifier = Modifier.size(44.dp),
                        cornerRadius = 10.dp
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            post.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "${post.author} · 좋아요 ${post.likes}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            items(comments) { comment ->
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FeedCommentRow(comment)
                    comment.replies.forEach { reply ->
                        // 대댓글은 들여쓰기로 부모와의 관계를 보여준다
                        Box(Modifier.padding(start = 34.dp)) { FeedCommentRow(reply, compact = true) }
                    }
                }
            }
            item {
                Text(
                    "함께 간 친구의 댓글이 먼저 보여요",
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private data class FeedCommentItem(
    val avatar: String,
    val author: String,
    val badge: String? = null,
    val time: String,
    val body: String,
    val likes: Int,
    val replies: List<FeedCommentItem> = emptyList()
)

/**
 * 댓글 한 줄.
 *
 * 우측 점 세 개 메뉴는 두지 않는다 — 메뉴 안에 숨으면 신고 경로가 있는지조차 알 수 없다.
 * 답글과 신고를 본문 아래 글자 동작으로 함께 노출한다.
 */
@Composable
private fun FeedCommentRow(comment: FeedCommentItem, compact: Boolean = false) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
        AnimalAvatar(comment.avatar, modifier = Modifier.size(if (compact) 28.dp else 36.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    comment.author,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.bodySmall
                )
                comment.badge?.let { badge ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MoyeoTheme.tints.primaryTint
                    ) {
                        Text(
                            badge,
                            Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MoyeoTheme.tints.onPrimaryTint,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    comment.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(comment.body, style = MaterialTheme.typography.bodyMedium)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Filled.FavoriteBorder,
                        contentDescription = "좋아요",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (comment.likes > 0) {
                        Text(
                            comment.likes.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    "답글 달기",
                    modifier = Modifier.clickable {},
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "신고",
                    modifier = Modifier.clickable {}.testTag("feed-comment-report"),
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
