package kr.hanchae.moyeotrip.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledIconButton
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kr.hanchae.moyeotrip.data.ChatMessage
import kr.hanchae.moyeotrip.data.ChatThread
import kr.hanchae.moyeotrip.data.MockTripRepository
import kr.hanchae.moyeotrip.data.chat.ChatOutbox
import kr.hanchae.moyeotrip.data.chat.QueuedChatMessage
import kr.hanchae.moyeotrip.ui.components.InfoPill
import kr.hanchae.moyeotrip.ui.theme.MoyeoTheme

@Composable
fun ChatRoomScreen(
    threadId: String,
    isOnline: Boolean = true,
    onBack: () -> Unit,
    onOpenNotices: (String) -> Unit = {},
    onOpenRoute: (String) -> Unit = {},
    onOpenMenu: () -> Unit = {},
    onOpenAttachment: () -> Unit = {}
) {
    val thread = MockTripRepository.findThread(threadId)
    val trip = thread.tripId?.let(MockTripRepository::findTrip)
    val messages = remember(threadId, isOnline) {
        mutableStateListOf<ChatMessage>().also {
            if (isOnline) {
                it.addAll(thread.messages)
            } else {
                // 화면기획 37 — 연결이 끊긴 시점의 대화 스냅숏
                it.add(ChatMessage("숲속여행자", "주차장 도착하면 알려주세요~", ""))
                it.add(ChatMessage("나", "네 곧 도착해요!", "09:31", mine = true))
            }
        }
    }
    var draft by rememberSaveable { mutableStateOf("") }
    var toolbarMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val outbox = remember(threadId) {
        ChatOutbox().apply {
            // 화면기획 37 — 전송 대기 중인 메시지 한 건
            if (!isOnline) enqueue("주차장 입구에서 만나요", "")
        }
    }
    var queuedMessages by remember(threadId) { mutableStateOf(outbox.pending) }
    val messageListState = rememberLazyListState()
    val canSend = draft.isNotBlank() && !thread.isReadOnly
    val colors = MaterialTheme.colorScheme

    LaunchedEffect(isOnline) {
        if (isOnline && outbox.pending.isNotEmpty()) {
            outbox.drainInOrder().forEach { queued ->
                messages.add(MockTripRepository.appendChatMessage(threadId, queued.text))
                delay(80)
            }
            queuedMessages = outbox.pending
        }
    }

    LaunchedEffect(messages.size, queuedMessages.size) {
        delay(50)
        val lastItemIndex = messageListState.layoutInfo.totalItemsCount - 1
        if (lastItemIndex >= 0) messageListState.animateScrollToItem(lastItemIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        ChatRoomTopBar(
            thread = thread,
            showCount = trip == null || !isOnline,
            onBack = onBack,
            onCallClick = {
                toolbarMessage = "호스트 연락 방식과 통화 가능 시간을 확인할 수 있어요."
            },
            onMoreClick = {
                onOpenMenu()
            }
        )
        if (!isOnline) OfflineChatWarning()
        if (trip != null && isOnline) {
            val notices = MockTripRepository.noticesForTrip(trip.id)
            val pinnedCount = notices.count { it.isPinned }
            // 화면기획 20 — "2/5명 · 5/25(토) 08:00–18:00 · 당일치기 · 마감 D-3", 마감만 강조색
            Text(
                text = buildAnnotatedString {
                    append(
                        "${trip.joined}/${trip.capacity}명 · ${chatCompactDate(trip.scheduleDate)} " +
                            "${trip.scheduleTime.replace(" - ", "–")} · ${trip.scheduleType.label} · "
                    )
                    withStyle(SpanStyle(color = colors.secondary, fontWeight = FontWeight.Bold)) {
                        append("마감 ${trip.ddayLabel}")
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    Icons.Filled.Payments to "1인 ${"%,d".format(trip.estimatedCostPerPerson)}원",
                    Icons.Filled.People to "${trip.minimumAge}~${trip.maximumAge}세",
                    Icons.Filled.Person to trip.genderCondition
                ).forEach { (icon, condition) -> ConditionChip(icon = icon, text = condition) }
            }
            HorizontalDivider(color = colors.outline.copy(alpha = .45f), modifier = Modifier.padding(top = 6.dp))
            if (pinnedCount > 0) {
                // 화면기획 20 — 고정 공지 바: "07:50 … 집합" + "공지 n개 · 고정 n · 이력 보기"
                ChatUtilityBar(
                    icon = Icons.Filled.Description,
                    title = "${trip.meetingLocation.meetingTime} ${trip.meetingLocation.name} " +
                        "${trip.meetingLocation.detail} 집합",
                    subtitle = "공지 ${notices.size}개 · 고정 $pinnedCount · 이력 보기",
                    tinted = true,
                    tag = "chat-pinned-notice",
                    onClick = { onOpenNotices(trip.id) },
                    trailing = {
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = colors.onSurfaceVariant
                        )
                    }
                )
            }
            ChatUtilityBar(
                icon = Icons.Filled.Map,
                title = MockTripRepository.findCourseForTrip(trip).title,
                subtitle = "방문지 ${trip.routeStops.size.takeIf {
                    it > 0
                } ?: MockTripRepository.findCourseForTrip(trip).stops.size}곳 · ${trip.courseSource.label}",
                tag = "chat-route-summary",
                onClick = { onOpenRoute(trip.id) },
                trailing = {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, colors.primary.copy(alpha = .65f)),
                        modifier = Modifier.clickable { onOpenRoute(trip.id) }.testTag("chat-route-edit")
                    ) {
                        Text(
                            text = "경로 수정",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
        LazyColumn(
            state = messageListState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (thread.isReadOnly) {
                item {
                    ChatArchiveNotice(thread = thread)
                }
            }
            items(messages) { message ->
                when {
                    message.sender == "시스템" && message.text.contains("경로를 수정") ->
                        RouteChangeMessage(message)

                    // 입장·개설 같은 시스템 안내는 가운데 필 형태다 (화면기획 20)
                    message.sender == "시스템" -> SystemPillMessage(message.text)

                    else -> MessageBubble(message = message)
                }
            }
            items(queuedMessages, key = { "queued-${it.id}" }) { message ->
                QueuedMessageBubble(message)
            }
            if (queuedMessages.isNotEmpty()) {
                item {
                    Text(
                        text = "연결되면 순서대로 보내드려요",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }
        Surface(color = colors.surface, shadowElevation = 8.dp) {
            if (thread.isReadOnly) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = thread.archiveStatus ?: "읽기 전용 보관",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.primary
                    )
                    Text(
                        text = "종료된 모임이라 새 메시지를 보낼 수 없어요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(
                            onClick = onOpenAttachment,
                            enabled = isOnline,
                            modifier = Modifier.testTag("chat-attachment")
                        ) {
                            // 화면기획의 첨부는 클립이 아니라 + 원형 버튼이다
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = if (isOnline) "첨부" else "오프라인에서는 첨부할 수 없어요"
                            )
                        }
                        OutlinedTextField(
                            value = draft,
                            onValueChange = { draft = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat-message-input"),
                            placeholder = { Text("메시지 입력") },
                            singleLine = true
                        )
                        FilledIconButton(
                            onClick = {
                                val trimmed = draft.trim()
                                if (trimmed.isNotEmpty()) {
                                    if (isOnline) {
                                        messages.add(MockTripRepository.appendChatMessage(threadId, trimmed))
                                    } else {
                                        val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                                        outbox.enqueue(trimmed, time)
                                        queuedMessages = outbox.pending
                                    }
                                    draft = ""
                                }
                            },
                            enabled = canSend,
                            modifier = Modifier.testTag("chat-message-send")
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "보내기")
                        }
                    }
                    if (!isOnline) {
                        Text(
                            text = "사진·장소 공유는 연결된 뒤에 보낼 수 있어요",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, bottom = 8.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
    if (toolbarMessage != null) {
        AlertDialog(
            onDismissRequest = { toolbarMessage = null },
            title = { Text("채팅방 도구") },
            text = { Text(toolbarMessage.orEmpty()) },
            confirmButton = {
                TextButton(onClick = { toolbarMessage = null }) {
                    Text("확인")
                }
            }
        )
    }
}

/**
 * 오버레이 배경으로 쓰는 기본 채팅방 — 캡처 도구가 쓰는 기본 chatId와 같다.
 */
internal const val OVERLAY_BACKDROP_THREAD_ID = "chat-cheongsong-juwangsan"

/**
 * 오버레이(20-2 첨부 시트 · 32 신고 시트) 배경으로 쓰는 채팅방 본문 — changeLog14 `ChatRoomBody`.
 *
 * 화면과 오버레이 배경이 같은 코드를 쓰도록 [ChatRoomScreen]을 그대로 재사용한다.
 * 상호작용은 위에 얹히는 스크림이 차단하므로 콜백은 비워 둔다.
 */
@Composable
internal fun ChatRoomBody(threadId: String = OVERLAY_BACKDROP_THREAD_ID, isOnline: Boolean = true) {
    ChatRoomScreen(threadId = threadId, isOnline = isOnline, onBack = {})
}

@Composable
private fun OfflineChatWarning() {
    val tints = MoyeoTheme.tints
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(tints.warningTint)
            .padding(horizontal = 14.dp)
            .testTag("offline-chat-banner"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(tints.onWarningTint)
        )
        Text(
            text = "연결이 끊겼어요. 보낸 메시지는 연결되면 자동으로 전송돼요.",
            modifier = Modifier.padding(start = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = tints.onWarningTint,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}

@Composable
private fun QueuedMessageBubble(message: QueuedChatMessage) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Column(
            modifier = Modifier
                .fillMaxWidth(.78f)
                .dashedRoundedBorder(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = .72f),
                    radius = 18.dp
                )
                .background(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = .55f),
                    RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .testTag("chat-message-pending")
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .7f)
                )
                Text(
                    text = "전송 대기",
                    modifier = Modifier.padding(start = 7.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
        }
    }
}

private fun Modifier.dashedRoundedBorder(color: Color, radius: androidx.compose.ui.unit.Dp): Modifier = drawBehind {
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(radius.toPx()),
        style = Stroke(
            width = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx()))
        )
    )
}

/** 화면기획 20 조건 칩 — 아이콘 + 텍스트의 필 형태. */
@Composable
private fun ConditionChip(icon: ImageVector, text: String) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(50),
        color = if (MoyeoTheme.isDark) colors.surfaceVariant else colors.surface,
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(14.dp))
            Text(text = text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * 화면기획 20의 고정 공지/코스 바 — 전체 폭 띠에 아이콘 + 굵은 제목 + 회색 부제,
 * 우측에는 화면별 트레일링(공지: 셰브런, 코스: "경로 수정" 버튼)이 붙는다.
 */
@Composable
private fun ChatUtilityBar(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tag: String,
    onClick: () -> Unit,
    tinted: Boolean = false,
    trailing: (@Composable () -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    val container = when {
        tinted -> MoyeoTheme.tints.primaryTint
        MoyeoTheme.isDark -> colors.surfaceVariant
        else -> colors.surface
    }
    Surface(
        modifier = Modifier.fillMaxWidth().testTag(tag).clickable(onClick = onClick),
        color = container
    ) {
        Column {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = colors.onSurfaceVariant
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold)
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                trailing?.invoke()
            }
            HorizontalDivider(color = colors.outline.copy(alpha = .45f))
        }
    }
}

@Composable
private fun SystemPillMessage(text: String) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MoyeoTheme.tints.systemMessage
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

/**
 * 화면기획 20의 경로 수정 카드 — 아이콘+굵은 제목, 본문(바뀐 방문지만 볼드), "바뀐 경로 보기 →" 링크.
 */
@Composable
private fun RouteChangeMessage(message: ChatMessage) {
    val tints = MoyeoTheme.tints
    val lines = message.text.split("\n", limit = 2)
    val title = lines.first()
    val body = lines.getOrElse(1) { "" }
    val changedRange = Regex("""\S+ → .+?(?=(으|이|가|은|는|을|를)?로\s|$)""").find(body)?.range

    Surface(
        modifier = Modifier.fillMaxWidth().testTag("chat-route-change-message"),
        shape = RoundedCornerShape(14.dp),
        color = tints.primaryTint
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(
                    imageVector = Icons.Filled.Autorenew,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = tints.onPrimaryTint
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = tints.onPrimaryTint,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            if (body.isNotEmpty()) {
                Text(
                    text = buildAnnotatedString {
                        append(body)
                        if (changedRange != null) {
                            addStyle(
                                SpanStyle(fontWeight = FontWeight.ExtraBold),
                                changedRange.first,
                                changedRange.last + 1
                            )
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )
            }
            Text(
                text = "바뀐 경로 보기 →",
                style = MaterialTheme.typography.labelMedium,
                color = tints.onPrimaryTint,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun ChatRoomTopBar(
    thread: ChatThread,
    onBack: () -> Unit,
    onCallClick: () -> Unit,
    onMoreClick: () -> Unit,
    showCount: Boolean = true
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.width(112.dp), contentAlignment = Alignment.CenterStart) {
                Surface(
                    color = colors.surface,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.55f))
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(58.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로",
                            tint = colors.onSurface
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = thread.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onSurface,
                    textAlign = TextAlign.Center
                )
                if (showCount) {
                    // 화면기획 20은 헤더 아래 메타 줄이 인원을 보여줘 헤더에는 중복 표기하지 않는다
                    Text(
                        text = thread.countText,
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            Box(modifier = Modifier.width(112.dp), contentAlignment = Alignment.CenterEnd) {
                Surface(
                    modifier = Modifier.height(58.dp),
                    color = colors.surface,
                    shape = RoundedCornerShape(30.dp),
                    border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.55f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onCallClick,
                            modifier = Modifier.size(42.dp)
                        ) {
                            // 화면기획·웹의 채팅 헤더는 대화 검색이다 (연락처를 받지 않아 통화 진입은 없다)
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "대화 검색",
                                tint = colors.onSurface
                            )
                        }
                        IconButton(
                            onClick = onMoreClick,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "모임 정보",
                                tint = colors.onSurface
                            )
                        }
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.outline.copy(alpha = 0.55f))
        )
    }
}

@Composable
private fun ChatArchiveNotice(thread: ChatThread) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.surface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = thread.closureReason ?: "여행이 종료됐어요",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onSurface
                )
                InfoPill(
                    text = thread.archiveStatus ?: "읽기 전용",
                    container = colors.primaryContainer,
                    content = colors.primary
                )
            }
            Text(
                text = thread.archiveNotice ?: "채팅은 14일 동안 읽기 전용으로 보관돼요.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val colors = MaterialTheme.colorScheme
    // 디자인 시스템의 chat-mine 토큰. primaryContainer 는 선택 상태 색이라 "내 메시지"로 읽히지 않는다.
    // 상대 버블은 화면기획 20 기준 다크에서 한 단 밝은 bgRaised(#18231E)다.
    val bubbleColor = when {
        message.mine -> MoyeoTheme.tints.chatMine
        MoyeoTheme.isDark -> colors.surfaceVariant
        else -> colors.surface
    }
    val contentColor = if (message.mine) colors.onPrimaryContainer else colors.onSurface
    val bubbleShape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = if (message.mine) 18.dp else 4.dp,
        bottomEnd = if (message.mine) 4.dp else 18.dp
    )
    val bubble: @Composable () -> Unit = {
        Text(
            text = message.text,
            modifier = Modifier
                .widthIn(max = 264.dp)
                .background(color = bubbleColor, shape = bubbleShape)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = contentColor
        )
    }

    if (message.mine) {
        // 화면기획 20 — 내 메시지는 시간 라벨이 버블 왼쪽에 붙는다
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = message.time,
                modifier = Modifier.padding(end = 6.dp, bottom = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant
            )
            bubble()
        }
    } else {
        // 화면기획 20 — 상대 메시지는 아바타 + 이름 + 버블, 시간은 버블 오른쪽
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            UserAvatar(
                imageUrl = null,
                nickname = message.sender,
                modifier = Modifier.size(36.dp),
                fallbackFontSize = 18.sp
            )
            Column(
                modifier = Modifier.padding(start = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = message.sender,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    bubble()
                    Text(
                        text = message.time,
                        modifier = Modifier.padding(start = 6.dp, bottom = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** 화면기획 20의 날짜 표기 — "2026.05.25 (토)" → "5/25(토)". */
private fun chatCompactDate(value: String): String {
    val match = Regex("""\d{4}\.\s*(\d{1,2})\.\s*(\d{1,2})\s*\(([^)]+)\)""").find(value) ?: return value
    return "${match.groupValues[1].toInt()}/${match.groupValues[2].toInt()}(${match.groupValues[3]})"
}
