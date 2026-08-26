package kr.hanchae.moyeotrip.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Place
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kr.hanchae.moyeotrip.data.ChatThread
import kr.hanchae.moyeotrip.data.MockTripRepository
import kr.hanchae.moyeotrip.data.TripApplication
import kr.hanchae.moyeotrip.data.TripApplicationStatus
import kr.hanchae.moyeotrip.data.TripCourse
import kr.hanchae.moyeotrip.data.rooms.MyChatRoom
import kr.hanchae.moyeotrip.data.rooms.MyWaitingRoom
import kr.hanchae.moyeotrip.data.rooms.recruitmentDDayText
import kr.hanchae.moyeotrip.ui.LocalServerData
import kr.hanchae.moyeotrip.ui.components.CachedRemoteImage
import kr.hanchae.moyeotrip.ui.theme.MoyeoTheme

@Composable
fun MeetingsScreen(
    onOpenRoom: (String) -> Unit,
    onOpenTrip: (String) -> Unit = {},
    onOpenSpecialMessages: () -> Unit = {},
    initialTab: MeetingChatTab = MeetingChatTab.Active
) {
    MeetingChatList(
        onOpenRoom = onOpenRoom,
        onOpenTrip = onOpenTrip,
        initialTab = initialTab,
        onOpenSpecialMessages = onOpenSpecialMessages
    )
}

@Composable
internal fun MeetingChatList(
    onOpenRoom: (String) -> Unit,
    onOpenTrip: (String) -> Unit = {},
    initialTab: MeetingChatTab = MeetingChatTab.Active,
    showTitle: Boolean = true,
    onOpenSpecialMessages: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    val allThreads = MockTripRepository.chatThreads.sortedForMeetings()
    val threads = allThreads.filterFor(selectedTab)
    var applications by remember { mutableStateOf(MockTripRepository.applications.toList()) }

    // 로그인 상태면 실서버 내 모임(GET chat-rooms/my · my-waiting)으로 대체한다
    val server = LocalServerData.current
    var serverRooms by remember(server) { mutableStateOf<List<MyChatRoom>?>(null) }
    var serverWaiting by remember(server) { mutableStateOf<List<MyWaitingRoom>?>(null) }
    val meetingScope = rememberCoroutineScope()
    LaunchedEffect(server) {
        if (server == null) {
            serverRooms = null
            serverWaiting = null
            return@LaunchedEffect
        }
        serverRooms = runCatching { server.chatRooms.myRooms() }.getOrNull()
        serverWaiting = runCatching { server.chatRooms.myWaitingRooms() }.getOrNull()
    }
    val isServerList = serverRooms != null
    val serverTabRooms = serverRooms.orEmpty().filter { room ->
        when (selectedTab) {
            MeetingChatTab.Active -> !room.ended
            MeetingChatTab.Confirmed -> !room.ended && room.status == "CONFIRMED"
            MeetingChatTab.Ended -> room.ended
            MeetingChatTab.Applied -> false
        }
    }
    val counts = MeetingChatTab.entries.associateWith { tab ->
        if (isServerList) {
            when (tab) {
                MeetingChatTab.Active -> serverRooms.orEmpty().count { !it.ended }
                MeetingChatTab.Applied -> serverWaiting.orEmpty().size
                MeetingChatTab.Confirmed -> serverRooms.orEmpty().count { !it.ended && it.status == "CONFIRMED" }
                MeetingChatTab.Ended -> serverRooms.orEmpty().count(MyChatRoom::ended)
            }
        } else {
            when (tab) {
                MeetingChatTab.Active -> allThreads.count {
                    !it.isReadOnly
                }

                MeetingChatTab.Applied -> applications.size

                MeetingChatTab.Confirmed -> allThreads.count { !it.isReadOnly && it.statusText.contains("확정") }

                MeetingChatTab.Ended -> allThreads.count { it.isReadOnly }
            }
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
                onSelect = { selectedTab = it }
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, top = 0.dp, end = 18.dp, bottom = 132.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            if (isServerList) {
                if (selectedTab == MeetingChatTab.Applied) {
                    val waiting = serverWaiting.orEmpty()
                    if (waiting.isEmpty()) {
                        item { EmptyMeetingChatState(tab = selectedTab) }
                    } else {
                        items(waiting, key = { it.roomId }) { room ->
                            ServerWaitingRoomCard(
                                room = room,
                                onCancel = {
                                    meetingScope.launch {
                                        runCatching { server?.chatRooms?.cancelApplication(room.roomId) }
                                            .onSuccess {
                                                serverWaiting =
                                                    serverWaiting?.filterNot { it.roomId == room.roomId }
                                            }
                                    }
                                },
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
                } else if (serverTabRooms.isEmpty()) {
                    item { EmptyMeetingChatState(tab = selectedTab) }
                } else {
                    items(serverTabRooms, key = { it.roomId }) { room ->
                        // 목데이터와 같은 동작 — 모임 행을 누르면 그 방의 채팅방(화면기획 20)이 열린다
                        ServerMeetingRoomRow(
                            room = room,
                            onClick = { onOpenRoom("room-${room.roomId}") }
                        )
                    }
                }
            } else if (selectedTab == MeetingChatTab.Applied && applications.isNotEmpty()) {
                // 화면기획·웹처럼 상태별 섹션 머리말을 둔다
                val waiting = applications.filter { it.status != TripApplicationStatus.Waitlisted }
                val queued = applications.filter { it.status == TripApplicationStatus.Waitlisted }
                listOf("호스트 승인을 기다리는 중" to waiting, "대기열에 있는 모임" to queued)
                    .filter { it.second.isNotEmpty() }
                    .forEach { (header, group) ->
                        item(key = "applied-header-$header") {
                            Text(
                                text = header,
                                modifier = Modifier.fillMaxWidth().padding(top = 14.dp)
                                    .testTag("meeting-applied-header-$header"),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(group, key = { it.id }) { application ->
                            ApplicationStatusCard(
                                application = application,
                                onCancel = {
                                    MockTripRepository.cancelApplication(application.tripId)
                                    applications = MockTripRepository.applications.toList()
                                },
                                onOpenDetail = { onOpenTrip(application.tripId) }
                            )
                        }
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
            } else if (threads.isEmpty()) {
                item {
                    EmptyMeetingChatState(tab = selectedTab)
                }
            } else {
                items(threads, key = { it.id }) { thread ->
                    MeetingChatThreadCard(thread = thread, onClick = { onOpenRoom(thread.id) })
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
                contentScale = ContentScale.Crop
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
private fun ApplicationStatusCard(application: TripApplication, onCancel: () -> Unit, onOpenDetail: () -> Unit) {
    val trip = MockTripRepository.findTrip(application.tripId)
    val tints = MoyeoTheme.tints
    val waitlisted = application.status == TripApplicationStatus.Waitlisted
    // 승인 대기는 "기다리는 중"이라 경고 틴트, 대기열은 "자리가 나면 합류"라 브랜드 틴트
    val badgeContainer = if (waitlisted) tints.primaryTint else tints.warningTint
    val badgeContent = if (waitlisted) tints.onPrimaryTint else tints.onWarningTint
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp).testTag("meeting-application-${application.tripId}"),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        trip.recruitmentName,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "🗺 ${trip.title}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${trip.scheduleDate} · ${trip.scheduleType.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(shape = RoundedCornerShape(50), color = badgeContainer) {
                    Text(
                        if (waitlisted) "대기열 ${application.waitlistPosition ?: 1}번" else "승인 대기",
                        Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = badgeContent,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            Surface(shape = RoundedCornerShape(10.dp), color = badgeContainer) {
                Text(
                    if (waitlisted) {
                        "정원이 차서 대기 중이에요. 자리가 나면 순서대로 자동 합류돼요."
                    } else {
                        "호스트가 확인하면 채팅방이 열려요. 보통 24시간 이내에 응답해요."
                    },
                    Modifier.padding(11.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = badgeContent
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.height(42.dp).testTag("application-cancel-${trip.id}"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("신청 취소")
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
                        text = "친구 도감 메시지",
                        fontSize = 15.sp,
                        lineHeight = 19.sp,
                        color = colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "여행 뒤 남는 특별 메시지를 모아봐요.",
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

@Composable
fun SpecialMessagesScreen(onBack: () -> Unit, onOpenTripConfirmed: () -> Unit = {}) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
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
                text = "채팅방 · 특수 메시지 6종",
                modifier = Modifier.weight(1f),
                fontSize = 13.sp,
                lineHeight = 17.sp,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            Box(modifier = Modifier.size(48.dp))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, top = 14.dp, end = 18.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                SpecialMessageCard(
                    eyebrow = "장소",
                    eyebrowIcon = Icons.Filled.Place,
                    title = "동궁과 월지",
                    subtitle = "경북 경주시 원화로 102",
                    body = "09:00-22:00 · 지도 보기"
                ) {
                    CourseScenicPanel(
                        course = MockTripRepository.findCourse("gyeongju-healing"),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp),
                        cornerRadius = 8.dp
                    )
                }
            }
            item {
                SpecialMessageCard(
                    eyebrow = "11/8 14:00 만남",
                    eyebrowIcon = Icons.Filled.Map,
                    title = "경주역 2번 출구",
                    subtitle = "함께 출발하면 좋아요",
                    body = "길 찾기"
                ) {
                    SpecialRoutePreview(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(88.dp)
                    )
                }
            }
            item {
                SpecialMessageCard(
                    eyebrow = "정산",
                    eyebrowIcon = Icons.Filled.Payments,
                    title = "우직한 곰 7821님이 결제했어요",
                    subtitle = "한옥스테이 1박",
                    body = "120,000원 · 4명",
                    trailingValue = "1인 30,000원"
                )
            }
            item {
                SpecialMessageCard(
                    eyebrow = "공지 · 호스트",
                    eyebrowIcon = Icons.AutoMirrored.Filled.Article,
                    title = "집합: 경주역 2번 출구",
                    subtitle = "시간: 11/8 (토) 14:00",
                    body = "함께 출발하면 좋아요"
                )
            }
            item {
                SpecialMessageCard(
                    eyebrow = "확정",
                    eyebrowIcon = Icons.Filled.AutoAwesome,
                    tinted = true,
                    title = "여행이 확정됐어요!",
                    subtitle = "좋은 여행 되세요",
                    body = "모임 채팅방에서 준비물을 확인해요",
                    onClick = onOpenTripConfirmed
                )
            }
            item {
                SpecialMessageCard(
                    eyebrow = "종료",
                    eyebrowIcon = Icons.Filled.Lock,
                    title = "아쉬운 모임이에요. 다음에 또 봐요!",
                    subtitle = "14일 후 자동으로 사라져요",
                    body = "친구 도감에는 추억이 남아요"
                )
            }
        }
    }
}

@Composable
private fun SpecialMessageCard(
    eyebrow: String,
    title: String,
    subtitle: String,
    body: String,
    /** 라벨 앞 아이콘. 화면기획은 카드 종류를 아이콘으로 먼저 구분한다. */
    eyebrowIcon: ImageVector? = null,
    /** 확정 카드처럼 브랜드 틴트를 쓰는 카드. */
    tinted: Boolean = false,
    /** 정산 카드의 우측 강조 값. */
    trailingValue: String? = null,
    onClick: (() -> Unit)? = null,
    preview: (@Composable () -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    val tints = MoyeoTheme.tints

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        color = if (tinted) tints.primaryTint else colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (tinted) {
                colorScheme.primary.copy(alpha = 0.35f)
            } else {
                colorScheme.outline.copy(alpha = 0.55f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                if (eyebrowIcon != null) {
                    Icon(
                        imageVector = eyebrowIcon,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = if (tinted) tints.onPrimaryTint else colorScheme.primary
                    )
                }
                Text(
                    text = eyebrow,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    color = if (tinted) tints.onPrimaryTint else colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Text(
                text = title,
                fontSize = 15.sp,
                lineHeight = 19.sp,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                color = colorScheme.onSurfaceVariant
            )
            if (preview != null) {
                preview()
            }
            if (trailingValue != null) {
                // 1인당 금액은 우측 정렬 강조 (화면기획)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = body,
                        modifier = Modifier.weight(1f),
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        color = colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = trailingValue,
                        fontSize = 14.sp,
                        color = colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            } else {
                Text(
                    text = body,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    color = if (tinted) tints.onPrimaryTint else colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SpecialRoutePreview(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val mapBase = colorScheme.primaryContainer.copy(alpha = 0.45f)
    val routeColor = colorScheme.primary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(mapBase)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path().apply {
                moveTo(size.width * 0.16f, size.height * 0.68f)
                cubicTo(
                    size.width * 0.30f,
                    size.height * 0.52f,
                    size.width * 0.56f,
                    size.height * 0.44f,
                    size.width * 0.84f,
                    size.height * 0.24f
                )
            }
            drawPath(
                path = path,
                color = routeColor,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )

            listOf(
                Offset(size.width * 0.16f, size.height * 0.68f),
                Offset(size.width * 0.56f, size.height * 0.44f),
                Offset(size.width * 0.84f, size.height * 0.24f)
            ).forEach { point ->
                drawCircle(color = routeColor, radius = 8.dp.toPx(), center = point)
                drawCircle(
                    color = mapBase,
                    radius = 8.dp.toPx(),
                    center = point,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

@Composable
private fun MeetingChatThreadCard(thread: ChatThread, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val course = thread.previewCourse()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(94.dp)
            .testTag("meeting-thread-${thread.id}")
            .clickable(onClick = onClick),
        color = colorScheme.background,
        shape = RoundedCornerShape(0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MeetingChatPreview(thread = thread)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = thread.title,
                                fontSize = 15.sp,
                                lineHeight = 19.sp,
                                color = colorScheme.onSurface,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (thread.unreadCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(colorScheme.secondary)
                                )
                            }
                        }
                        Text(
                            text = thread.time,
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Map,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = thread.courseLine.ifBlank { course.title },
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "${thread.countText} · ${thread.statusText}",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = thread.lastMessage,
                            modifier = Modifier.weight(1f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (thread.unreadCount > 0) {
                            Badge(containerColor = colorScheme.secondary) {
                                Text(
                                    text = thread.unreadCount.toString(),
                                    color = colorScheme.onSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
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

@Composable
private fun MeetingChatPreview(thread: ChatThread) {
    val course = thread.previewCourse()

    Box(
        modifier = Modifier
            .width(54.dp)
            .height(54.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        CourseScenicPanel(course = course, modifier = Modifier.fillMaxSize(), cornerRadius = 8.dp)
    }
}

@Composable
private fun EmptyMeetingChatState(tab: MeetingChatTab) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 44.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "아직 ${tab.label} 모임이 없어요",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "탐색에서 마음에 드는 여행을 찾아보세요",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun ChatThread.previewCourse(): TripCourse {
    val key = courseLine.ifBlank { title }
    val courseId = when {
        key.contains("주왕산") -> "cheongsong-juwangsan"
        key.contains("하회마을") -> "andong-hahoe"
        key.contains("도산서원") -> "andong-dosan"
        key.contains("경주") -> "gyeongju-healing"
        key.contains("문경") -> "mungyeong-saejae"
        key.contains("부석사") -> "yeongju-buseoksa"
        key.contains("포항") || key.contains("영덕") -> "pohang-sea"
        else -> "ulleung-island"
    }
    return MockTripRepository.findCourse(courseId)
}

private fun List<ChatThread>.filterFor(tab: MeetingChatTab): List<ChatThread> = when (tab) {
    MeetingChatTab.Active -> filter { !it.isReadOnly }
    MeetingChatTab.Applied -> emptyList()
    MeetingChatTab.Confirmed -> filter { !it.isReadOnly && it.statusText.contains("확정") }
    MeetingChatTab.Ended -> filter { it.isReadOnly }
}

private fun List<ChatThread>.sortedForMeetings(): List<ChatThread> {
    val order = listOf(
        "chat-gyeongju-fall",
        "chat-pohang-drive",
        "chat-ulleung-island",
        "chat-andong-hahoe",
        "chat-andong-dosan",
        "chat-mungyeong-fall",
        "chat-cheongsong-juwangsan",
        "chat-yeongju-buseoksa",
        "chat-ended-andong-spring",
        "chat-ended-ulleung"
    ).withIndex().associate { (index, id) -> id to index }

    return sortedBy { thread -> order[thread.id] ?: Int.MAX_VALUE }
}

enum class MeetingChatTab(val label: String) {
    Active("진행중"),
    Applied("신청중"),
    Confirmed("확정"),
    Ended("종료")
}
