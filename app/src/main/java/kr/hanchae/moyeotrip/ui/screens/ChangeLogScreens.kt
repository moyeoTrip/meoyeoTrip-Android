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
import androidx.compose.ui.draw.clip
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
import kr.hanchae.moyeotrip.ui.theme.MoyeoTheme

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
        // 화면기획 20-1은 전원 "매너 4.8 · 여행 8회"로 표기한다
        FriendEntry("🐻", "숲속여행자", "호스트 · 매너 4.8 · 여행 8회"),
        FriendEntry("🦌", "따스한 사슴 3492", "나 · 매너 4.8 · 여행 8회"),
        FriendEntry("🐰", "엉뚱한 토끼 1457", "매너 4.8 · 여행 8회"),
        FriendEntry("🐢", "잔잔한 거북이 9032", "매너 4.8 · 여행 8회"),
        FriendEntry("🦝", "호기심 많은 너구리 9027", "매너 4.8 · 여행 8회")
    )
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("chat-attach-screen")
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.72f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 96.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.55f).height(40.dp),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.48f)
            ) {}
            Surface(
                modifier = Modifier.align(Alignment.End).fillMaxWidth(0.42f).height(40.dp),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.48f)
            ) {}
            Surface(
                modifier = Modifier.fillMaxWidth(0.62f).height(40.dp),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.48f)
            ) {}
        }

        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface
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
                                Button(
                                    onClick = {
                                    },
                                    contentPadding = PaddingValues(
                                        horizontal = 12.dp
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("수락")
                                }
                            }

                            else -> Text("요청 중", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                    "함께 여행한 친구는 친구가 아니어도 도감에 남아요. " +
                                        "친구 신청은 피드를 구독하고 싶을 때만 하면 돼요.",
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
fun ReportScreen(onBack: () -> Unit) {
    val reasons = listOf("스팸 · 도박", "성희롱 · 불쾌한 언행", "돈거래 유도", "허위 정보", "부적절한 내용", "기타")
    var selected by rememberSaveable { mutableStateOf(reasons[1]) }
    var block by rememberSaveable { mutableStateOf(true) }
    // 화면기획 32는 전체 화면이 아니라 어두운 채팅 위로 올라오는 바텀시트다
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = .45f))
            .testTag("report-screen"),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.background
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
                                if (chosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
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
                                    color = if (chosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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
                        Text("차단 해제", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
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
                        Switch(checked = dnd, onCheckedChange = { dnd = it })
                    }
                    if (dnd) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            QuietHourField("시작", "22:30", Modifier.weight(1f))
                            QuietHourField("종료", "07:00", Modifier.weight(1f))
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
                            "집합 30분 전 알림처럼 여행 당일 안내는 방해금지 시간에도 전달돼요.",
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
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(28.dp)
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
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .6f))
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
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MoyeoTheme.tints.softLine)
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
