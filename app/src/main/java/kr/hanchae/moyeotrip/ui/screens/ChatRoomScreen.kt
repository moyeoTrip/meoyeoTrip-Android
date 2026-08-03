package kr.hanchae.moyeotrip.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kr.hanchae.moyeotrip.data.ChatMessage
import kr.hanchae.moyeotrip.data.ChatThread
import kr.hanchae.moyeotrip.data.MockTripRepository
import kr.hanchae.moyeotrip.ui.components.InfoPill

@Composable
fun ChatRoomScreen(threadId: String, onBack: () -> Unit) {
    val thread = MockTripRepository.findThread(threadId)
    val messages = remember(threadId) {
        mutableStateListOf<ChatMessage>().also { it.addAll(thread.messages) }
    }
    var draft by rememberSaveable { mutableStateOf("") }
    var toolbarMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val canSend = draft.isNotBlank() && !thread.isReadOnly
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        ChatRoomTopBar(
            thread = thread,
            onBack = onBack,
            onCallClick = {
                toolbarMessage = "호스트 연락 방식과 통화 가능 시간을 확인할 수 있어요."
            },
            onMoreClick = {
                toolbarMessage = "신고, 알림 끄기, 멤버 보기 메뉴를 확인할 수 있어요."
            }
        )
        LazyColumn(
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
            } else {
                item {
                    Text(
                        text = "모임 신청 후 이어지는 여행 채팅방이에요",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )
                }
            }
            items(messages) { message ->
                MessageBubble(message = message)
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .navigationBarsPadding()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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
                                messages.add(MockTripRepository.appendChatMessage(threadId, trimmed))
                                draft = ""
                            }
                        },
                        enabled = canSend,
                        modifier = Modifier.testTag("chat-message-send")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "보내기")
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

@Composable
private fun ChatRoomTopBar(thread: ChatThread, onBack: () -> Unit, onCallClick: () -> Unit, onMoreClick: () -> Unit) {
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
                Text(
                    text = thread.countText,
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onSurfaceVariant
                )
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
                            Icon(
                                imageVector = Icons.Filled.Call,
                                contentDescription = "전화",
                                tint = colors.onSurface
                            )
                        }
                        IconButton(
                            onClick = onMoreClick,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreHoriz,
                                contentDescription = "더보기",
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
    val bubbleColor = if (message.mine) colors.primaryContainer else colors.surface
    val contentColor = if (message.mine) colors.onPrimaryContainer else colors.onSurface
    val metaColor = if (message.mine) colors.onPrimaryContainer.copy(alpha = 0.72f) else colors.onSurfaceVariant

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.mine) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.78f),
            contentAlignment = if (message.mine) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Column(
                modifier = Modifier
                    .background(
                        color = bubbleColor,
                        shape = RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (message.mine) 18.dp else 4.dp,
                            bottomEnd = if (message.mine) 4.dp else 18.dp
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = contentColor
                )
                Text(
                    text = message.time,
                    style = MaterialTheme.typography.bodyMedium,
                    color = metaColor
                )
            }
        }
    }
}
