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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.hanchae.moyeotrip.data.rooms.MyChatRoom
import kr.hanchae.moyeotrip.data.rooms.MyWaitingRoom
import kr.hanchae.moyeotrip.data.rooms.RoomMessage
import kr.hanchae.moyeotrip.data.rooms.RoomNotice
import kr.hanchae.moyeotrip.data.rooms.recruitmentDDayText
import kr.hanchae.moyeotrip.ui.LocalServerData
import kr.hanchae.moyeotrip.ui.components.CachedRemoteImage
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyState
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyText
import kr.hanchae.moyeotrip.ui.components.MoyeoPlaceholderShape
import kr.hanchae.moyeotrip.ui.components.ServerListState
import kr.hanchae.moyeotrip.ui.state.LocalTabDataStore
import kr.hanchae.moyeotrip.ui.theme.MoyeoTheme

@Composable
fun MeetingsScreen(
    onOpenRoom: (String) -> Unit,
    onOpenTrip: (String) -> Unit = {},
    onOpenSpecialMessages: () -> Unit = {},
    /** 19-2 참가 신청 취소 확인. 대기 순번을 잃는 행동이라 확인 없이 실행하지 않는다(정본 §6-1). */
    onOpenApplyCancel: (String) -> Unit = {},
    /** 19-1 처럼 특정 세그먼트로 바로 여는 진입만 값을 준다. null 이면 사용자가 마지막에 보던 세그먼트다. */
    initialTab: MeetingChatTab? = null
) {
    MeetingChatList(
        onOpenRoom = onOpenRoom,
        onOpenTrip = onOpenTrip,
        initialTab = initialTab,
        onOpenSpecialMessages = onOpenSpecialMessages,
        onOpenApplyCancel = onOpenApplyCancel
    )
}

@Composable
internal fun MeetingChatList(
    onOpenRoom: (String) -> Unit,
    onOpenTrip: (String) -> Unit = {},
    initialTab: MeetingChatTab? = null,
    showTitle: Boolean = true,
    onOpenSpecialMessages: () -> Unit = {},
    onOpenApplyCancel: (String) -> Unit = {}
) {
    val server = LocalServerData.current
    // 모임도 "보던 상태 유지" 탭이다 — 목록과 고른 세그먼트를 탭 바깥 보관소에 둔다(정본 R1·R3).
    val meetings = LocalTabDataStore.current.meetings
    LaunchedEffect(initialTab) {
        // 세그먼트를 지정해 들어온 진입만 골라 준다. 지정 없이 들어오면 보던 세그먼트를 그대로 둔다.
        if (initialTab != null) meetings.selectedTab = initialTab
    }
    val selectedTab = meetings.selectedTab
    val rooms = meetings.rooms
    val waiting = meetings.waiting
    LaunchedEffect(server, meetings.reloadKey) {
        if (server == null) {
            meetings.rooms = ServerListState.Loaded(emptyList())
            meetings.waiting = emptyList()
            return@LaunchedEffect
        }
        // 성공해서 보여줄 목록이 있을 때만 그대로 그린다 — 재진입에 다시 부르지 않는다(정본 R3).
        // 직전 조회가 실패했으면 보여줄 게 없으므로 다시 부른다(R3-1).
        if (meetings.loaded) return@LaunchedEffect
        // 여기까지 왔다는 것은 캐시가 없다는 뜻이라 로딩 문구가 맞다(정본 R2).
        meetings.rooms = ServerListState.Loading
        val myRooms = runCatching { server.chatRooms.myRooms() }
        meetings.rooms = myRooms.fold({ ServerListState.Loaded(it) }, { ServerListState.Failed })
        meetings.waiting = runCatching { server.chatRooms.myWaitingRooms() }.getOrElse { meetings.waiting }
        if (myRooms.isSuccess) meetings.markLoaded()
    }
    val loadedRooms = (rooms as? ServerListState.Loaded)?.items.orEmpty()
    val tabRooms = loadedRooms.filter { room ->
        when (selectedTab) {
            MeetingChatTab.Active -> !room.ended
            MeetingChatTab.Confirmed -> !room.ended && room.status == "CONFIRMED"
            MeetingChatTab.Ended -> room.ended
            MeetingChatTab.Applied -> false
        }
    }
    val counts = MeetingChatTab.entries.associateWith { tab ->
        when (tab) {
            MeetingChatTab.Active -> loadedRooms.count { !it.ended }
            MeetingChatTab.Applied -> waiting.size
            MeetingChatTab.Confirmed -> loadedRooms.count { !it.ended && it.status == "CONFIRMED" }
            MeetingChatTab.Ended -> loadedRooms.count(MyChatRoom::ended)
        }
    }
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        if (showTitle) {
            Text(
                text = "모임",
                modifier = Modifier
                    .padding(horizontal = 18.dp)
                    .padding(top = 18.dp, bottom = 10.dp),
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Column(
            modifier = Modifier.padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SpecialMessagesEntry(onClick = onOpenSpecialMessages)
            MeetingChatTabRow(
                selectedTab = selectedTab,
                counts = counts,
                onSelect = { meetings.selectedTab = it }
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, top = 0.dp, end = 18.dp, bottom = 132.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            val state = rooms
            when {
                state is ServerListState.Loading -> item { MoyeoEmptyState(MoyeoEmptyText.LOADING) }

                state is ServerListState.Failed -> item {
                    MoyeoEmptyState(MoyeoEmptyText.FAILED, onRetry = meetings::reload)
                }

                selectedTab == MeetingChatTab.Applied -> if (waiting.isEmpty()) {
                    item { MoyeoEmptyState(MoyeoEmptyText.NO_JOINED_ROOMS, testTag = "meetings-empty") }
                } else {
                    items(waiting, key = { it.roomId }) { room ->
                        ServerWaitingRoomCard(
                            room = room,
                            // 19-2 — 예전에는 여기서 바로 DELETE 가 나갔다. 대기 순번을 잃는 행동이라
                            // 되돌릴 수 없어 확인 화면을 먼저 지난다(정본 §6-1).
                            onCancel = { onOpenApplyCancel("room-${room.roomId}") },
                            onOpenDetail = { onOpenTrip("room-${room.roomId}") }
                        )
                    }
                    item(key = "applied-footnote") {
                        Text(
                            text = "신청 상태에서는 아직 채팅방에 들어갈 수 없어요. " +
                                "승인되거나 자리가 나면 알림으로 알려드릴게요.",
                            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                tabRooms.isEmpty() -> item {
                    MoyeoEmptyState(MoyeoEmptyText.NO_JOINED_ROOMS, testTag = "meetings-empty")
                }

                else -> items(tabRooms, key = { it.roomId }) { room ->
                    ServerMeetingRoomRow(
                        room = room,
                        onClick = { onOpenRoom("room-${room.roomId}") }
                    )
                }
            }
        }
    }
}

/** 실서버 내 모임 행 (GET chat-rooms/my). 탭하면 모집 상세(서버)를 연다. */
@Composable
private fun ServerMeetingRoomRow(room: MyChatRoom, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .testTag("meeting-server-room-${room.roomId}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CachedRemoteImage(
                url = room.thumbnail,
                contentDescription = room.title,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop,
                fallbackShape = MoyeoPlaceholderShape.SQUARE
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = room.title,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val subtitle = room.latestMessage ?: room.description
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = if (room.endDate != null) "${room.startDate} ~ ${room.endDate}" else room.startDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                recruitmentDDayText(room.recruitmentDDay)?.let { dday ->
                    Text(
                        text = dday,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                val unread = room.unreadMessageCount ?: 0
                if (unread > 0) {
                    Badge { Text(text = unread.toString()) }
                }
            }
        }
    }
}

/** 실서버 신청중 카드 (GET chat-rooms/my-waiting). 신청 취소는 DELETE applications/me 다. */
@Composable
private fun ServerWaitingRoomCard(room: MyWaitingRoom, onCancel: () -> Unit, onOpenDetail: () -> Unit) {
    val tints = MoyeoTheme.tints
    val waitlisted = room.applicationStatus == "WAITLISTED"
    val rejected = room.applicationStatus == "REJECTED"
    val badgeContainer = if (waitlisted) tints.primaryTint else tints.warningTint
    val badgeContent = if (waitlisted) tints.onPrimaryTint else tints.onWarningTint
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .testTag("meeting-server-application-${room.roomId}"),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        room.title,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        if (room.endDate != null) "${room.startDate} ~ ${room.endDate}" else room.startDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${room.participantCount}/${room.maxParticipants}명",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(shape = RoundedCornerShape(50), color = badgeContainer) {
                    Text(
                        when {
                            rejected -> "거절됨"
                            waitlisted -> "대기열 ${room.waitlistPosition ?: 1}번"
                            else -> "승인 대기"
                        },
                        Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = badgeContent,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!rejected) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.height(42.dp).testTag("server-application-cancel-${room.roomId}"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("신청 취소")
                    }
                }
                OutlinedButton(
                    onClick = onOpenDetail,
                    modifier = Modifier.height(42.dp),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("모집 상세") }
            }
        }
    }
}

@Composable
private fun MeetingChatTabRow(
    selectedTab: MeetingChatTab,
    counts: Map<MeetingChatTab, Int>,
    onSelect: (MeetingChatTab) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MeetingChatTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .testTag("meetings-tab-${tab.name.lowercase()}")
                    .clickable { onSelect(tab) },
                contentAlignment = Alignment.BottomCenter
            ) {
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tab.label,
                        fontSize = 14.sp,
                        lineHeight = 19.sp,
                        color = if (selected) colorScheme.primary else colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = (counts[tab] ?: 0).toString(),
                        modifier = Modifier.padding(start = 5.dp),
                        fontSize = 13.sp,
                        lineHeight = 17.sp,
                        color = if (selected) colorScheme.primary else colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(if (selected) colorScheme.primary else Color.Transparent)
                )
            }
        }
    }
}

@Composable
private fun SpecialMessagesEntry(onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag("meetings-special-messages-entry")
            .clickable(onClick = onClick),
        color = colorScheme.background,
        shape = RoundedCornerShape(0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✦",
                        fontSize = 17.sp,
                        color = colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "특수 메시지",
                        fontSize = 15.sp,
                        lineHeight = 19.sp,
                        color = colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "사진·장소·투표·정산 카드를 모아봐요.",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colorScheme.outline.copy(alpha = 0.45f))
            )
        }
    }
}

/**
 * 화면기획 21 특수 메시지 카드 6종.
 *
 * 예시 카드 6장을 늘어놓던 화면이었다. 이제는 **실제 서버 메시지**를 채팅방(20)과 **같은 카드
 * 컴포저블**로 그린다 — [ServerSpecialMessage] · [SystemPillMessage] · [ChatUtilityBar].
 * 두 화면이 다르게 생기면 그게 결함이라, 여기서 카드를 새로 만들지 않는다.
 *
 * [threadId] (`room-121`) 를 주면 그 방의 `GET chat-rooms/{id}/messages` 만 읽는다. 없으면 예전처럼
 * 내 모임 앞쪽 몇 개를 훑는다 — 특수 메시지만 모아 주는 전용 API 가 없다(§4 BE 요청).
 *
 * 서버가 주지 않는 종류(예: 사진이 한 장도 없는 방의 IMAGE)는 **그 카드를 그리지 않는다**.
 * 순서는 카드 먼저, 시스템 안내 나중이다 — 21 은 대화 타임라인이 아니라 카드 견본이고,
 * 기획도 시스템 안내를 맨 아래에 둔다.
 *
 * "여행 확정" 카드는 확정된 모임이 있을 때만 나오고 20-4 확정 안내로 이어진다.
 */
@Composable
fun SpecialMessagesScreen(
    threadId: String? = null,
    onBack: () -> Unit,
    onOpenTripConfirmed: () -> Unit = {},
    onOpenNotices: (String) -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val server = LocalServerData.current
    val targetRoomId = remember(threadId) { threadId?.serverRoomIdOrNull() }
    var cards by remember(server, targetRoomId) {
        mutableStateOf<ServerListState<RoomMessage>>(ServerListState.Loading)
    }
    var hasConfirmedTrip by remember(server, targetRoomId) { mutableStateOf(false) }
    var pinnedNotice by remember(server, targetRoomId) { mutableStateOf<RoomNotice?>(null) }
    var noticeCount by remember(server, targetRoomId) { mutableIntStateOf(0) }
    var reloadKey by remember(server, targetRoomId) { mutableIntStateOf(0) }

    LaunchedEffect(server, targetRoomId, reloadKey) {
        if (server == null) {
            cards = ServerListState.Loaded(emptyList())
            hasConfirmedTrip = false
            pinnedNotice = null
            return@LaunchedEffect
        }
        cards = ServerListState.Loading
        if (targetRoomId != null) {
            val page = runCatching { server.chatRooms.messages(targetRoomId) }.getOrNull()
            if (page == null) {
                // 비참여 방이면 서버가 403 을 준다 — 카드를 지어내지 않고 실패로 알린다
                cards = ServerListState.Failed
                return@LaunchedEffect
            }
            val notices = runCatching { server.chatRooms.notices(targetRoomId) }.getOrNull()
            pinnedNotice = notices?.pinned?.firstOrNull()
            noticeCount = notices?.all?.size ?: 0
            hasConfirmedTrip = runCatching { server.chatRooms.room(targetRoomId) }.getOrNull()?.status == "CONFIRMED"
            cards = ServerListState.Loaded(page.messages.filter { it.isSpecialCardOrSystem }.sortedForSampleSheet())
            return@LaunchedEffect
        }
        val rooms = runCatching { server.chatRooms.myRooms() }.getOrNull()
        if (rooms == null) {
            cards = ServerListState.Failed
            return@LaunchedEffect
        }
        hasConfirmedTrip = rooms.any { !it.ended && it.status == "CONFIRMED" }
        pinnedNotice = null
        noticeCount = 0
        // 최근 방부터 몇 개만 훑는다 — 모든 방을 도는 전용 API 가 없다(§4 BE 요청).
        val collected = rooms.take(SPECIAL_MESSAGE_ROOM_SCAN)
            .flatMap { room ->
                runCatching { server.chatRooms.messages(room.roomId).messages }
                    .getOrElse { emptyList() }
                    .filter { it.isSpecialCard }
            }
        cards = ServerListState.Loaded(collected)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .testTag("special-messages-screen")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로",
                    tint = colorScheme.onSurface
                )
            }
            Text(
                text = "채팅방 · 특수 메시지",
                modifier = Modifier.weight(1f),
                fontSize = 13.sp,
                lineHeight = 17.sp,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            Box(modifier = Modifier.size(48.dp))
        }

        // 공지 카드(기획 ④ `공지 · 호스트`)는 방의 고정 공지가 근거다 — 채팅방 20 과 같은 띠를 쓴다.
        pinnedNotice?.content?.takeIf(String::isNotBlank)?.let { notice ->
            ChatUtilityBar(
                icon = Icons.Filled.Description,
                title = notice,
                subtitle = "공지 ${noticeCount}개 · 이력 보기",
                tinted = true,
                tag = "special-messages-notice",
                onClick = { threadId?.let(onOpenNotices) },
                trailing = {
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = colorScheme.onSurfaceVariant
                    )
                }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, top = 14.dp, end = 18.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (hasConfirmedTrip) {
                item {
                    ConfirmedTripEntryCard(onClick = onOpenTripConfirmed)
                }
            }
            val state = cards
            when {
                state is ServerListState.Loading -> item { MoyeoEmptyState(MoyeoEmptyText.LOADING) }

                state is ServerListState.Failed -> item {
                    MoyeoEmptyState(MoyeoEmptyText.FAILED, onRetry = { reloadKey++ })
                }

                state is ServerListState.Loaded && state.items.isEmpty() -> item {
                    // 방을 지정해 들어왔으면 "그 방에 대화가 없다"가, 아니면 "참여 중인 모임이 없다"가 사실이다.
                    MoyeoEmptyState(
                        text = if (targetRoomId == null) MoyeoEmptyText.NO_JOINED_ROOMS else NO_ROOM_MESSAGES,
                        testTag = "special-messages-empty"
                    )
                }

                state is ServerListState.Loaded -> items(state.items, key = { it.messageId }) { message ->
                    if (message.type == "SYSTEM") {
                        // 시스템 안내는 채팅방 20 과 같은 중앙 정렬 알약이다
                        SystemPillMessage(message.content)
                    } else {
                        // 읽기 전용 — busy=true 로 투표 버튼을 잠근다. 투표는 채팅방(20)에서 한다.
                        ServerSpecialMessage(
                            message = message,
                            mine = false,
                            busy = true,
                            onVote = {},
                            onCancelVote = {}
                        )
                    }
                }
            }
        }
    }
}

/** 21 에 싣는 메시지 — 카드로 그리는 특수 메시지와 시스템 안내다. 일반 텍스트 버블은 20 의 것이다. */
private val RoomMessage.isSpecialCardOrSystem: Boolean get() = isSpecialCard || type == "SYSTEM"

/** 카드 견본이라 카드를 먼저, 시스템 안내를 나중에 둔다. 카드끼리·시스템끼리는 서버가 준 순서를 지킨다. */
private fun List<RoomMessage>.sortedForSampleSheet(): List<RoomMessage> =
    filter { it.type != "SYSTEM" } + filter { it.type == "SYSTEM" }

/** 지정한 방에 특수 메시지도 시스템 안내도 없을 때. 채팅방 20 의 빈 대화 문구와 같은 말이다. */
private const val NO_ROOM_MESSAGES = "아직 대화가 없어요."

/** 화면기획 21 의 "확정" 카드 — 확정된 모임이 있을 때만 20-4 확정 안내로 이어진다. */
@Composable
private fun ConfirmedTripEntryCard(onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val tints = MoyeoTheme.tints

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("special-messages-confirmed")
            .clickable(onClick = onClick),
        color = tints.primaryTint,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, colorScheme.primary.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = tints.onPrimaryTint
                )
                Text(
                    text = "확정",
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    color = tints.onPrimaryTint,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Text(
                text = "여행이 확정됐어요!",
                fontSize = 15.sp,
                lineHeight = 19.sp,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "확정 안내에서 일정과 함께 갈 사람을 확인해요",
                fontSize = 12.sp,
                lineHeight = 15.sp,
                color = tints.onPrimaryTint,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/** 특수 메시지를 모을 때 훑는 방 수. 전용 조회 API 가 없어 최근 방만 본다. */
private const val SPECIAL_MESSAGE_ROOM_SCAN = 5

enum class MeetingChatTab(val label: String) {
    Active("진행중"),
    Applied("신청중"),
    Confirmed("확정"),
    Ended("종료")
}
