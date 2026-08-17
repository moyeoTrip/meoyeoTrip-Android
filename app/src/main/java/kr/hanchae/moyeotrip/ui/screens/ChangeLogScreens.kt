package kr.hanchae.moyeotrip.ui.screens

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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
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
import kr.hanchae.moyeotrip.data.MockTripRepository
import kr.hanchae.moyeotrip.ui.components.AnimalAvatar

private data class MenuEntry(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val danger: Boolean = false,
    val onClick: () -> Unit
)

private data class FriendEntry(val emoji: String, val name: String, val subtitle: String)

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
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = tint)
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
    onOpenRoute: (String) -> Unit
) {
    val thread = MockTripRepository.findThread(threadId)
    val trip = thread.tripId?.let(MockTripRepository::findTrip)
    val members = listOf(
        FriendEntry("🐻", "숲속여행자", "호스트 · 매너 4.8 · 여행 8회"),
        FriendEntry("🦌", "따스한 사슴 3492", "나 · 매너 4.9 · 여행 12회"),
        FriendEntry("🐰", "엉뚱한 토끼 1457", "매너 4.7 · 여행 5회"),
        FriendEntry("🐢", "잔잔한 거북이 9032", "매너 4.8 · 여행 6회"),
        FriendEntry("🦝", "호기심 많은 너구리 9027", "매너 4.6 · 여행 3회")
    )
    ChangeLogScaffold(title = "모임 정보", onBack = onBack, modifier = Modifier.testTag("chat-menu-screen")) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                    Text(
                        trip?.title ?: thread.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        "5/25(토) 당일치기 · 08:00 – 18:00",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Text(
                        "07:50 청송 시외버스터미널 정문 앞 집합",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.padding(top = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = { trip?.id?.let(onOpenRoute) }, modifier = Modifier.weight(1f)) {
                            Text("여행 경로")
                        }
                        OutlinedButton(onClick = { trip?.id?.let(onOpenNotices) }, modifier = Modifier.weight(1f)) {
                            Text("모집 상세")
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
                    IconButton(onClick = {}, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Filled.MoreHoriz, contentDescription = "${member.name} 관리")
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatAttachmentScreen(onBack: () -> Unit, onOpenSpecialMessages: () -> Unit, isOnline: Boolean) {
    val items = listOf(
        Triple(Icons.Filled.CameraAlt, "사진", "최대 20MB · 1장씩"),
        Triple(Icons.Filled.LocationOn, "장소", "TourAPI 장소 카드"),
        Triple(Icons.Filled.Map, "지도", "만날 위치 핀 공유"),
        Triple(Icons.Filled.Poll, "투표", "2~5개 · 익명 기본"),
        Triple(Icons.Filled.Payments, "정산", "메모용 · 송금 아님"),
        Triple(Icons.AutoMirrored.Filled.StickyNote2, "메모", "상단 고정 공지")
    )
    ChangeLogScaffold(title = "첨부", onBack = onBack, modifier = Modifier.testTag("chat-attach-screen")) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
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
                            .clickable(enabled = isOnline, onClick = onOpenSpecialMessages)
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
        }
    }
}

@Composable
fun FriendsScreen(onBack: () -> Unit, onOpenDex: () -> Unit) {
    var tab by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf("내 친구 3", "받은 신청 2", "보낸 신청 1")
    val lists = listOf(
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
                items(lists[tab]) { friend ->
                    Row(
                        modifier = Modifier.fillMaxWidth().height(68.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnimalAvatar(friend.emoji, modifier = Modifier.size(44.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(friend.name, fontWeight = FontWeight.ExtraBold)
                            Text(
                                friend.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        when (tab) {
                            0 -> IconButton(onClick = {}, modifier = Modifier.size(48.dp)) {
                                Icon(Icons.Filled.MoreHoriz, contentDescription = "${friend.name} 관리")
                            }

                            1 -> Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(onClick = {}) { Text("거절") }
                                Button(onClick = {}, contentPadding = PaddingValues(horizontal = 12.dp)) { Text("수락") }
                            }

                            else -> Text("요청 중", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                item {
                    RoundedPanel(
                        modifier = Modifier.padding(top = 14.dp),
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            "함께 여행한 친구는 친구가 아니어도 도감에 남아요. 친구 신청은 피드를 구독하고 싶을 때만 하면 돼요.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        TextButton(onClick = onOpenDex) { Text("도감 열어보기") }
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
                    Button(onClick = onOpenDex, modifier = Modifier.weight(1f).height(50.dp)) {
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
                    "남긴 메시지는 상대방의 도감 카드 뒷면에 적혀요. 안 남겨도 카드는 그대로 모여요.",
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
                                OutlinedButton(onClick = { messages[index] = preset }) { Text(preset) }
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
fun ReportScreen(onBack: () -> Unit) {
    val reasons = listOf("스팸 · 도박", "성희롱 · 불쾌한 언행", "돈거래 유도", "허위 정보", "부적절한 내용", "기타")
    var selected by rememberSaveable { mutableStateOf(reasons[1]) }
    var block by rememberSaveable { mutableStateOf(true) }
    ChangeLogScaffold(title = "신고", onBack = onBack, modifier = Modifier.testTag("report-screen")) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("신고 사유를 알려주세요", style = MaterialTheme.typography.titleLarge)
                RoundedPanel(modifier = Modifier.padding(top = 12.dp)) {
                    Text("해당 메시지 · “계좌로 먼저 보내주시면…”", style = MaterialTheme.typography.bodySmall)
                }
            }
            items(reasons) { reason ->
                Surface(
                    modifier = Modifier.fillMaxWidth().height(50.dp).clickable { selected = reason },
                    shape = RoundedCornerShape(11.dp),
                    border = BorderStroke(
                        1.dp,
                        if (selected ==
                            reason
                        ) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }
                    ),
                    color = if (selected ==
                        reason
                    ) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selected == reason, onClick = { selected = reason })
                        Text(reason, fontWeight = FontWeight.Bold)
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
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(52.dp).padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("신고하기") }
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

@Composable
fun BlockedUsersScreen(onBack: () -> Unit) {
    val blocked = remember {
        mutableStateListOf(
            FriendEntry("🦝", "말많은 너구리 7791", "2026.07.28 차단 · 채팅방 신고"),
            FriendEntry("🪽", "청아한 두루미 2024", "2026.06.02 차단 · 프로필")
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
                        "차단하면 그 사람이 만들었거나 참여한 모집이 홈·탐색·코스 상세에서 모두 숨겨져요. 상대방에게는 알려지지 않아요.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
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
                    OutlinedButton(onClick = { blocked.remove(user) }) { Text("차단 해제") }
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
    val warningContainer = if (androidx.compose.foundation.isSystemInDarkTheme()) {
        Color(
            0xFF392E18
        )
    } else {
        Color(0xFFFFF3D6)
    }
    val warningContent = if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFFF4C76F) else Color(0xFF7C5511)
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
                        modifier = Modifier.weight(1f).height(50.dp).testTag("course-publish-start")
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
                    modifier = Modifier.testTag("course-publish-confirm-first")
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
                    modifier = Modifier.testTag("course-publish-confirm-final")
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
    var sharing by rememberSaveable { mutableStateOf(true) }
    val stops = listOf("청송터미널", "주왕산", "주산지", "달기약수탕")
    ChangeLogScaffold(
        title = MockTripRepository.findThread(threadId).title,
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
                        Icon(Icons.Filled.AttachFile, contentDescription = "첨부")
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
                    FilledIconButton(onClick = {}, modifier = Modifier.size(48.dp)) {
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
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Map, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                        Text(if (sharing) "3명이 위치를 공유 중이에요" else "위치 공유 꺼짐", fontWeight = FontWeight.Bold)
                        Text("여행이 끝나면 자동으로 꺼져요", style = MaterialTheme.typography.labelSmall)
                    }
                    Switch(checked = sharing, onCheckedChange = { sharing = it })
                }
                HorizontalDivider()
            }
            item {
                Text(
                    "오늘 여행이 시작됐어요 🎒",
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
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
    val modes = listOf("모든 메시지", "멘션 · 답글만", "받지 않기")
    var mode by rememberSaveable { mutableStateOf(modes.first()) }
    var dnd by rememberSaveable { mutableStateOf(true) }
    var muted by rememberSaveable { mutableStateOf(setOf("경주 단풍·야경 1박 2일")) }
    val selectedDays = remember { mutableStateListOf("월", "화", "수", "목", "금") }
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
            items(modes) { item ->
                Surface(
                    modifier = Modifier.fillMaxWidth().height(58.dp).clickable { mode = item },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        if (mode ==
                            item
                        ) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = mode == item, onClick = { mode = item })
                        Text(item, fontWeight = FontWeight.Bold)
                    }
                }
            }
            item {
                RoundedPanel {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("방해금지 시간대", fontWeight = FontWeight.ExtraBold)
                            Text("22:00 – 07:00", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(checked = dnd, onCheckedChange = { dnd = it })
                    }
                    FlowRow(
                        modifier = Modifier.padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("월", "화", "수", "목", "금", "토", "일").forEach { day ->
                            OutlinedButton(onClick = {
                                if (day in selectedDays) selectedDays.remove(day) else selectedDays.add(day)
                            }) {
                                Text(
                                    day,
                                    color = if (day in
                                        selectedDays
                                    ) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }
                Text("모임별 음소거", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 18.dp))
                listOf("주왕산 & 주산지 힐링 트레킹", "경주 단풍·야경 1박 2일").forEach { title ->
                    Row(
                        modifier = Modifier.fillMaxWidth().height(58.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Switch(
                            checked = title in muted,
                            onCheckedChange = { on -> muted = if (on) muted + title else muted - title }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AccountDeleteScreen(onBack: () -> Unit, onDelete: () -> Unit) {
    val reasons = listOf("원하는 여행을 찾기 어려워요", "알림이 너무 많아요", "서비스를 자주 쓰지 않아요", "기타")
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
                Button(
                    onClick = { confirm = true },
                    enabled = selected != null && understood,
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp).height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("탈퇴 계속하기") }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text("떠나기 전에 확인해주세요", style = MaterialTheme.typography.headlineSmall)
                RoundedPanel(
                    modifier = Modifier.padding(top = 12.dp),
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        "진행 중인 여행 2개를 먼저 정리해야 해요.",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        "신청 취소나 호스트 위임을 마치면 탈퇴할 수 있어요.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Text(
                    "탈퇴하는 이유를 알려주세요 (필수)",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 18.dp)
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
                    Text("삭제되는 정보", fontWeight = FontWeight.ExtraBold)
                    Text(
                        "프로필 · 친구 · 찜 · 피드 · 로그인 연결",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Text(
                        "탈퇴 요청 후 30일 안에는 계정을 되살릴 수 있어요. 30일이 지나면 복구할 수 없어요.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { understood = !understood }.padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = understood, onCheckedChange = { understood = it })
                    Text("30일 후 정보가 영구 삭제되는 것을 확인했어요", style = MaterialTheme.typography.bodySmall)
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
                Button(onClick = {
                    confirm = false
                    finalConfirm = true
                }) { Text("계속") }
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
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
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(28.dp)
            .testTag(if (maintenance) "system-maintenance-screen" else "system-error-screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = CircleShape,
                color = if (maintenance) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.errorContainer
                }
            ) {
                Icon(
                    if (maintenance) Icons.Filled.Settings else Icons.Filled.ErrorOutline,
                    contentDescription = null,
                    modifier = Modifier.padding(18.dp).size(38.dp)
                )
            }
            Text(
                if (maintenance) "잠시 쉬어가고 있어요" else "화면을 불러오지 못했어요",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 22.dp)
            )
            Text(
                if (maintenance) {
                    "더 안정적인 여행을 위해 서버를 점검하고 있어요.\n예상 종료 시각은 오늘 04:30이에요."
                } else {
                    "잠시 뒤 다시 시도해주세요. 같은 문제가 계속되면 고객센터로 알려주세요."
                },
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp)
            )
            Button(onClick = onRetry, modifier = Modifier.fillMaxWidth().height(52.dp).padding(top = 20.dp)) {
                Icon(Icons.Filled.Sync, contentDescription = null)
                Text(if (maintenance) "10분 뒤 자동으로 다시 시도해요" else "새로고침", modifier = Modifier.padding(start = 8.dp))
            }
            if (!maintenance) TextButton(onClick = onBack) { Text("이전 화면으로") }
            Text(
                if (maintenance) "점검 코드 · MAINT-503" else "오류 코드 · SERVER-500",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 18.dp)
            )
        }
    }
}

@Composable
fun FeedCommentsScreen(postId: String, onBack: () -> Unit) {
    val comments = remember {
        mutableStateListOf(
            Triple("🐻", "숲속여행자 · 작성자", "사진보다 실제 풍경이 더 좋았어요!"),
            Triple("🦌", "따스한 사슴 3492 · 함께 간 친구", "다음엔 주산지도 같이 가요."),
            Triple("🐰", "엉뚱한 토끼 1457", "코스 저장했어요. 고맙습니다!"),
            Triple("🐢", "잔잔한 거북이 9032", "단풍 절정은 언제였나요?")
        )
    }
    var draft by rememberSaveable { mutableStateOf("") }
    ChangeLogScaffold(
        title = "댓글 ${comments.size}",
        onBack = onBack,
        modifier = Modifier.testTag("feed-comments-screen-$postId"),
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        placeholder = { Text("댓글을 입력하세요") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    FilledIconButton(
                        onClick = {
                            if (draft.isNotBlank()) {
                                comments.add(Triple("🦌", "따스한 사슴 3492", draft.trim()))
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            items(comments) { comment ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                    AnimalAvatar(comment.first, modifier = Modifier.size(40.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            comment.second,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(comment.third, modifier = Modifier.padding(top = 4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "2시간 전",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = {}) { Text("답글") }
                        }
                    }
                    IconButton(onClick = {}, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Filled.MoreHoriz, contentDescription = "댓글 메뉴")
                    }
                }
            }
        }
    }
}
