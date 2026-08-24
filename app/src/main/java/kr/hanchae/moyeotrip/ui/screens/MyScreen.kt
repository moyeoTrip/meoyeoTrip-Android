package kr.hanchae.moyeotrip.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.hanchae.moyeotrip.data.MockTripRepository
import kr.hanchae.moyeotrip.data.TripCourse
import kr.hanchae.moyeotrip.data.TripRecruitment
import kr.hanchae.moyeotrip.data.profile.ServerUserProfile
import kr.hanchae.moyeotrip.data.rooms.MyChatRoom
import kr.hanchae.moyeotrip.domain.auth.UserDisplayProfile
import kr.hanchae.moyeotrip.ui.LocalServerData
import kr.hanchae.moyeotrip.ui.components.AnimalAvatar
import kr.hanchae.moyeotrip.ui.components.CachedRemoteImage
import kr.hanchae.moyeotrip.ui.components.MoyeoLinearProgress
import kr.hanchae.moyeotrip.ui.theme.Coral
import kr.hanchae.moyeotrip.ui.theme.ForestGreen
import kr.hanchae.moyeotrip.ui.theme.MoyeoTheme

@Composable
fun MyScreen(
    userProfile: UserDisplayProfile,
    onOpenTrip: (String) -> Unit,
    onOpenCourse: (String) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenMyFeed: () -> Unit,
    onOpenFriendDex: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCustomerCenter: () -> Unit,
    onOpenFriends: () -> Unit = {},
    onOpenCoursePublish: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(MyTripTab.Ongoing) }
    val courses = MockTripRepository.courses
    // 화면기획 26의 "내 여행"은 4개다
    val ongoingTrips = MockTripRepository.trips.take(4)
    val pastTrips = listOf(courses[2], courses[1], courses[4], courses[5], courses[3])
    val savedCourses = listOf(courses[4], courses[2], courses[3], courses[5], courses[6], courses[7])

    // 로그인 상태면 실서버 데이터(users/me/profile · chat-rooms/my · travel-dex)로 대체한다.
    // 서버가 주지 않는 값(매너 점수·피드 수·찜한 코스 목록)은 서버 모드에서 표시하지 않는다.
    val server = LocalServerData.current
    var serverProfile by remember(server) { mutableStateOf<ServerUserProfile?>(null) }
    var serverRooms by remember(server) { mutableStateOf<List<MyChatRoom>?>(null) }
    var serverDexCount by remember(server) { mutableStateOf<Int?>(null) }
    LaunchedEffect(server) {
        if (server == null) {
            serverProfile = null
            serverRooms = null
            serverDexCount = null
            return@LaunchedEffect
        }
        serverProfile = runCatching { server.userProfile.profile() }.getOrNull()
        serverRooms = runCatching { server.chatRooms.myRooms() }.getOrNull()
        serverDexCount = runCatching { server.social.travelDex().size }.getOrNull()
    }
    val serverOngoing = serverRooms?.filterNot(MyChatRoom::ended)
    val serverPast = serverRooms?.filter(MyChatRoom::ended)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(myPageColor())
            .testTag("my-scroll"),
        contentPadding = PaddingValues(
            start = 18.dp,
            end = 18.dp,
            top = 12.dp,
            bottom = 128.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            MyPageHeader(onOpenSettings = onOpenSettings)
        }
        item {
            MyProfileSummaryCard(userProfile = userProfile, serverProfile = serverProfile, onClick = onOpenProfile)
        }
        if (serverProfile == null) {
            // 매너·피드 통계는 서버가 내려주지 않는다 — 로그인 상태에서는 숨긴다
            item {
                MyProfileStatPills()
            }
        }
        item {
            MySectionHeader(title = "내 여행", countText = "${(serverOngoing ?: ongoingTrips).size}개")
        }
        item {
            MySegmentedControl(
                selectedTab = selectedTab,
                onSelect = { selectedTab = it }
            )
        }
        when (selectedTab) {
            MyTripTab.Ongoing -> {
                if (serverOngoing != null) {
                    if (serverOngoing.isEmpty()) {
                        item { MyServerEmptyState(text = "아직 참여 중인 여행이 없어요.") }
                    }
                    serverOngoing.forEach { room ->
                        item {
                            MyServerRoomCard(
                                room = room,
                                onClick = { onOpenTrip("room-${room.roomId}") }
                            )
                        }
                    }
                } else {
                    ongoingTrips.forEach { trip ->
                        val course = MockTripRepository.findCourseForTrip(trip)
                        item {
                            MyTripCard(
                                course = course,
                                title = trip.title,
                                // 화면기획 26은 집합 시간이 있는 모집만 "날짜 시간"으로 적는다
                                date = listOfNotNull(trip.scheduleDate, trip.assemblyTimeLabel).joinToString(" "),
                                place = trip.meetingPoint,
                                dday = trip.ddayLabel,
                                peopleText = trip.myPeopleText(),
                                progress = trip.myProgress(),
                                joined = trip.joined,
                                testTagPrefix = "my-active-trip-${trip.id}",
                                modifier = Modifier.testTag("my-active-trip-${trip.id}"),
                                onClick = { onOpenTrip(trip.id) }
                            )
                        }
                    }
                }
            }

            MyTripTab.Past -> {
                if (serverPast != null) {
                    if (serverPast.isEmpty()) {
                        item { MyServerEmptyState(text = "아직 다녀온 여행 기록이 없어요.") }
                    }
                    serverPast.forEach { room ->
                        item {
                            MyServerRoomCard(
                                room = room,
                                onClick = { onOpenTrip("room-${room.roomId}") }
                            )
                        }
                    }
                } else {
                    pastTrips.forEachIndexed { index, course ->
                        item {
                            Column {
                                MySummaryCourseCard(
                                    course = course,
                                    title = pastTripTitle(course),
                                    summary = pastTripSummary(course),
                                    meta = "${pastTripDate(index)} · 여행 기록",
                                    testTagPrefix = "my-past-trip-${course.id}",
                                    modifier = Modifier.testTag("my-past-trip-${course.id}"),
                                    onClick = { onOpenCourse(course.id) }
                                )
                                if (index == 0) {
                                    TextButton(
                                        onClick = onOpenCoursePublish,
                                        modifier = Modifier
                                            .align(Alignment.End)
                                            .testTag("my-course-publish")
                                    ) {
                                        Text("코스 공개하기")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            MyTripTab.Saved -> {
                if (serverRooms != null) {
                    // 찜한 코스 목록 API 가 없어 서버 모드에서는 비어 있는 상태만 보여준다 (보고서 C)
                    item { MyServerEmptyState(text = "찜한 코스를 불러올 수 없어요.") }
                } else {
                    savedCourses.forEach { course ->
                        item {
                            MySummaryCourseCard(
                                course = course,
                                title = savedCourseTitle(course),
                                summary = course.oneLine,
                                meta = "${course.duration} · ${courseDistance(course.id)}",
                                testTagPrefix = "my-saved-course-${course.id}",
                                modifier = Modifier.testTag("my-saved-course-${course.id}"),
                                onClick = { onOpenCourse(course.id) }
                            )
                        }
                    }
                }
            }
        }
        item {
            MyDogamShortcut(onClick = onOpenFriendDex, serverDexCount = serverDexCount)
        }
        item {
            MyHubMenuPanel(
                onOpenMyFeed = onOpenMyFeed,
                onOpenFriendDex = onOpenFriendDex,
                onOpenFriends = onOpenFriends,
                onOpenCustomerCenter = onOpenCustomerCenter,
                serverDexCount = serverDexCount
            )
        }
    }
}

@Composable
private fun MyServerEmptyState(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** 실서버 내 모임 카드 (GET chat-rooms/my) — 서버가 주지 않는 집합 장소는 표시하지 않는다. */
@Composable
private fun MyServerRoomCard(room: MyChatRoom, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("my-server-room-${room.roomId}")
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MyCardMetrics.gap)
        ) {
            CachedRemoteImage(
                url = room.thumbnail,
                contentDescription = room.title,
                modifier = Modifier
                    .width(MyCardMetrics.thumbWidth)
                    .height(MyCardMetrics.thumbHeight)
                    .clip(RoundedCornerShape(MyCardMetrics.thumbRadius)),
                contentScale = ContentScale.Crop
            ) {
                Box(
                    modifier = Modifier
                        .width(MyCardMetrics.thumbWidth)
                        .height(MyCardMetrics.thumbHeight)
                        .clip(RoundedCornerShape(MyCardMetrics.thumbRadius))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = room.title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    room.recruitmentDDay?.let { dday ->
                        DDayChip(text = "D-$dday")
                    }
                }
                Text(
                    text = if (room.endDate != null) "${room.startDate} ~ ${room.endDate}" else room.startDate,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                room.description?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                val joined = room.participantCount
                val capacity = room.maxParticipants
                if (joined != null && capacity != null && capacity > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MoyeoLinearProgress(
                            progress = joined.toFloat() / capacity,
                            modifier = Modifier.weight(1f),
                            height = 4.dp,
                            color = ForestGreen
                        )
                        Text(
                            text = "$joined/${capacity}명",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MyDogamShortcut(onClick: () -> Unit, serverDexCount: Int? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag("my-friend-dex-preview")
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Groups,
                    contentDescription = null,
                    tint = ForestGreen,
                    modifier = Modifier.size(19.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "지금까지 만난 친구",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${serverDexCount ?: MockTripRepository.dogamFriends.size}마리 · 최근 동행 순",
                    modifier = Modifier.testTag("my-friend-dex-preview-count"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            if (serverDexCount == null) {
                MemberStack(joined = 3)
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(21.dp)
            )
        }
    }
}

@Composable
private fun MyPageHeader(onOpenSettings: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "마이",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.ExtraBold
        )
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "설정",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun MyProfileSummaryCard(
    userProfile: UserDisplayProfile,
    onClick: () -> Unit,
    serverProfile: ServerUserProfile? = null
) {
    val profile = MockTripRepository.profile
    val displayName = serverProfile?.nickname ?: userProfile.nickname ?: profile.name

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("my-profile-summary")
            .semantics { contentDescription = "프로필 메뉴" }
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (serverProfile?.profileImageUrl != null || userProfile.profileImageUrl != null) {
                UserAvatar(
                    imageUrl = serverProfile?.profileImageUrl ?: userProfile.profileImageUrl,
                    nickname = serverProfile?.nickname ?: userProfile.nickname,
                    modifier = Modifier.size(50.dp),
                    fallbackFontSize = 23.sp
                )
            } else {
                // 화면기획 26은 곰 캐릭터 아바타를 보여준다
                // 화면기획 26의 프로필 아바타 배경은 연초록(primary50)이다 — 기본 코랄이 아니다
                AnimalAvatar(
                    profileAvatarEmoji(profile.animalBuddy),
                    modifier = Modifier.size(50.dp),
                    container = MoyeoTheme.tints.primaryTint
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val bio = if (serverProfile != null) serverProfile.introduction else profile.bio
                if (!bio.isNullOrBlank()) {
                    Text(
                        text = bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // 여행 수·매너 점수는 서버가 내려주지 않는다 — 로그인 상태에서는 숨긴다
                if (serverProfile == null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MyMiniChip("여행 ${profile.joinedTrips}", ForestGreen)
                        MyMiniChip("매너 4.7", Coral)
                    }
                }
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun MyProfileStatPills() {
    val profile = MockTripRepository.profile

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MyProfileStatPill(value = profile.joinedTrips.toString(), label = "여행", modifier = Modifier.weight(1f))
        MyProfileStatPill(value = "4.7", label = "매너", modifier = Modifier.weight(1f))
        MyProfileStatPill(value = profile.feedCount.toString(), label = "피드", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun MyProfileStatPill(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(50.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun MySectionHeader(title: String, countText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = countText,
            style = MaterialTheme.typography.labelMedium,
            color = ForestGreen,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun MyMiniChip(text: String, tint: Color) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(tint.copy(alpha = 0.14f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelSmall,
        color = tint,
        fontWeight = FontWeight.ExtraBold,
        maxLines = 1
    )
}

@Composable
private fun MyHubMenuPanel(
    onOpenMyFeed: () -> Unit,
    onOpenFriendDex: () -> Unit,
    onOpenFriends: () -> Unit,
    onOpenCustomerCenter: () -> Unit,
    serverDexCount: Int? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "메뉴",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.ExtraBold
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            MyHubMenuRow(
                title = "내 피드",
                subtitle = "내가 기록한 경북 여행",
                onClick = onOpenMyFeed,
                modifier = Modifier.testTag("my-feed-shortcut")
            )
            MyHubMenuDivider()
            MyHubMenuRow(
                title = "친구 관리",
                subtitle = "친구 신청 · 수락 · 내 친구",
                onClick = onOpenFriends,
                modifier = Modifier.testTag("my-friends-shortcut")
            )
            MyHubMenuDivider()
            MyHubMenuRow(
                title = "친구 도감",
                subtitle = "${serverDexCount ?: MockTripRepository.dogamFriends.size}마리 · 최근 동행 순",
                onClick = onOpenFriendDex,
                modifier = Modifier.testTag("my-friend-dex-shortcut")
            )
            MyHubMenuDivider()
            MyHubMenuRow(
                title = "고객센터",
                subtitle = "문의와 신고 내역",
                onClick = onOpenCustomerCenter,
                modifier = Modifier.testTag("my-customer-center-shortcut")
            )
        }
    }
}

@Composable
private fun MyHubMenuRow(title: String, subtitle: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun MyHubMenuDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(start = 16.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
    )
}

@Composable
private fun MySegmentedControl(selectedTab: MyTripTab, onSelect: (MyTripTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        MyTripTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .testTag("my-tab-${tab.name.lowercase()}")
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (selected) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                    .clickable { onSelect(tab) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) ForestGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun MyTripCard(
    course: TripCourse,
    title: String,
    date: String,
    place: String,
    dday: String,
    peopleText: String,
    progress: Float,
    joined: Int,
    testTagPrefix: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(MyCardMetrics.activeHeight)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MyCardMetrics.gap)
        ) {
            TripThumb(course = course)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = title,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("$testTagPrefix-title"),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    DDayChip(
                        text = dday,
                        modifier = Modifier.testTag("$testTagPrefix-dday")
                    )
                }
                Text(
                    text = date,
                    modifier = Modifier.testTag("$testTagPrefix-date"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = place,
                    modifier = Modifier.testTag("$testTagPrefix-place"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MoyeoLinearProgress(
                        progress = progress,
                        modifier = Modifier.weight(1f),
                        height = 4.dp,
                        color = ForestGreen
                    )
                    Text(
                        text = peopleText,
                        modifier = Modifier.testTag("$testTagPrefix-people"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    MemberStack(joined = joined)
                }
            }
        }
    }
}

@Composable
private fun MySummaryCourseCard(
    course: TripCourse,
    title: String,
    summary: String,
    meta: String,
    testTagPrefix: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(MyCardMetrics.summaryHeight)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MyCardMetrics.gap)
        ) {
            TripThumb(course = course)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    modifier = Modifier.testTag("$testTagPrefix-title"),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = summary,
                    modifier = Modifier.testTag("$testTagPrefix-summary"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = meta,
                    modifier = Modifier.testTag("$testTagPrefix-meta"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = course.region,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .testTag("$testTagPrefix-region")
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                style = MaterialTheme.typography.labelMedium,
                color = ForestGreen,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun TripThumb(course: TripCourse) {
    Box(
        modifier = Modifier
            .width(MyCardMetrics.thumbWidth)
            .height(MyCardMetrics.thumbHeight)
            .clip(RoundedCornerShape(MyCardMetrics.thumbRadius))
    ) {
        CourseScenicPanel(
            course = course,
            modifier = Modifier.fillMaxSize(),
            cornerRadius = MyCardMetrics.thumbRadius
        )
    }
}

private object MyCardMetrics {
    val activeHeight = 120.dp
    val summaryHeight = 112.dp
    val thumbWidth = 78.dp
    val thumbHeight = 68.dp
    val thumbRadius = 9.dp
    val gap = 10.dp
}

@Composable
private fun DDayChip(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        style = MaterialTheme.typography.labelSmall,
        color = Coral,
        fontWeight = FontWeight.ExtraBold,
        maxLines = 1
    )
}

@Composable
private fun MemberStack(joined: Int) {
    val visibleMembers = listOf("🐻", "🦌", "🐢").take(joined.coerceIn(1, 3))
    val hiddenCount = (joined - visibleMembers.size).coerceAtLeast(0)
    Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
        visibleMembers.forEach { emoji ->
            Box(
                modifier = Modifier
                    .size(21.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 11.sp)
            }
        }
        if (hiddenCount > 0) {
            Box(
                modifier = Modifier
                    .size(21.dp)
                    .clip(CircleShape)
                    .background(ForestGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+$hiddenCount",
                    fontSize = 9.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun TripRecruitment.myPeopleText(): String = "$joined/${capacity}명"

private fun TripRecruitment.myProgress(): Float = if (capacity == 0) 0f else joined.toFloat() / capacity.toFloat()

private fun pastTripDate(index: Int): String = when (index) {
    0 -> "2024.04.12 (금)"
    1 -> "2024.03.22 (토)"
    2 -> "2024.02.18 (일)"
    3 -> "2023.11.04 (토)"
    else -> "2023.09.16 (토)"
}

private fun pastTripTitle(course: TripCourse): String = when (course.id) {
    "gyeongju-healing" -> "경주 역사 감성 여행"
    else -> course.title
}

private fun pastTripSummary(course: TripCourse): String = when (course.id) {
    "gyeongju-healing" -> "월정교 야경과 첨성대 단풍길을 함께 걸었어요."
    "andong-hahoe" -> "하회마을 골목과 부용대 전망을 천천히 둘러봤어요."
    "ulleung-island" -> "해안 산책로와 섬마을 풍경을 여유롭게 남겼어요."
    "mungyeong-saejae" -> "완만한 고갯길과 단풍 숲길을 함께 걸었어요."
    "pohang-sea" -> "바다 전망 카페와 시장 먹거리를 가볍게 이었어요."
    else -> course.oneLine
}

private fun savedCourseTitle(course: TripCourse): String = when (course.id) {
    "gyeongju-healing" -> "경주 역사 감성 여행"
    else -> course.title
}

private fun courseDistance(courseId: String): String = when (courseId) {
    "ulleung-island" -> "12.4km"
    "gyeongju-healing" -> "7.3km"
    "pohang-sea" -> "9.1km"
    "mungyeong-saejae" -> "5.6km"
    "yeongju-buseoksa" -> "4.2km"
    "andong-dosan" -> "3.8km"
    else -> "6.2km"
}

private enum class MyTripTab(val label: String) {
    Ongoing("진행중"),
    Past("지난여행"),
    Saved("찜한 코스")
}

@Composable
private fun myPageColor(): Color = if (MoyeoTheme.isDark) {
    MaterialTheme.colorScheme.background
} else {
    MaterialTheme.colorScheme.surface
}
