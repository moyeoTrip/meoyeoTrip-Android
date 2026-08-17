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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.hanchae.moyeotrip.data.ChatThread
import kr.hanchae.moyeotrip.data.MockTripRepository
import kr.hanchae.moyeotrip.data.TripApplication
import kr.hanchae.moyeotrip.data.TripApplicationStatus
import kr.hanchae.moyeotrip.data.TripCourse

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
    val counts = MeetingChatTab.entries.associateWith { tab ->
        when (tab) {
            MeetingChatTab.Active -> allThreads.count {
                !it.isReadOnly
            }

            MeetingChatTab.Applied -> applications.size

            MeetingChatTab.Confirmed -> allThreads.count { !it.isReadOnly && it.statusText.contains("확정") }

            MeetingChatTab.Ended -> allThreads.count { it.isReadOnly }
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
            if (selectedTab == MeetingChatTab.Applied && applications.isNotEmpty()) {
                items(applications, key = { it.id }) { application ->
                    ApplicationStatusCard(
                        application = application,
                        onCancel = {
                            MockTripRepository.cancelApplication(application.tripId)
                            applications = MockTripRepository.applications.toList()
                        },
                        onOpenDetail = { onOpenTrip(application.tripId) }
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

@Composable
private fun ApplicationStatusCard(application: TripApplication, onCancel: () -> Unit, onOpenDetail: () -> Unit) {
    val trip = MockTripRepository.findTrip(application.tripId)
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp).testTag("meeting-application-${application.tripId}"),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(trip.title, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        "${trip.scheduleDate} · ${trip.scheduleType.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(
                        if (application.status ==
                            TripApplicationStatus.Waitlisted
                        ) {
                            "대기 ${application.waitlistPosition ?: 1}번"
                        } else {
                            "승인 대기"
                        },
                        Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            Text(
                if (application.status ==
                    TripApplicationStatus.Waitlisted
                ) {
                    "자리가 나면 신청 순서대로 알려드려요. 아직 채팅방에는 입장할 수 없어요."
                } else {
                    "호스트가 신청을 확인하고 있어요. 승인되면 모임 채팅방이 열려요."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f).height(42.dp).testTag("application-cancel-${trip.id}")
                ) {
                    Text("신청 취소")
                }
                Button(onClick = onOpenDetail, modifier = Modifier.weight(1f).height(42.dp)) { Text("상세 보기") }
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
                    title = "우직한 곰 7821님이 결제했어요",
                    subtitle = "한옥스테이 1박",
                    body = "120,000원 · 4명 · 1인 30,000원"
                )
            }
            item {
                SpecialMessageCard(
                    eyebrow = "공지 · 호스트",
                    title = "집합: 경주역 2번 출구",
                    subtitle = "시간: 11/8 (토) 14:00",
                    body = "함께 출발하면 좋아요"
                )
            }
            item {
                SpecialMessageCard(
                    eyebrow = "확정",
                    title = "여행이 확정됐어요!",
                    subtitle = "좋은 여행 되세요",
                    body = "모임 채팅방에서 준비물을 확인해요",
                    onClick = onOpenTripConfirmed
                )
            }
            item {
                SpecialMessageCard(
                    eyebrow = "종료",
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
    onClick: (() -> Unit)? = null,
    preview: (@Composable () -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        color = colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = eyebrow,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                color = colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )
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
            Text(
                text = body,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                color = colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
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

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
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
    val courseId = when {
        title.contains("주왕산") -> "cheongsong-juwangsan"
        title.contains("하회마을") -> "andong-hahoe"
        title.contains("경주") -> "gyeongju-healing"
        title.contains("포항") || title.contains("영덕") -> "pohang-sea"
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
