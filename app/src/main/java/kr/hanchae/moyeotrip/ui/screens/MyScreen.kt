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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import kr.hanchae.moyeotrip.data.courses.LikedTravelCourse
import kr.hanchae.moyeotrip.data.profile.ServerPublicProfile
import kr.hanchae.moyeotrip.data.profile.ServerUserProfile
import kr.hanchae.moyeotrip.data.rooms.ChatRoomSearchResult
import kr.hanchae.moyeotrip.data.rooms.MyChatRoom
import kr.hanchae.moyeotrip.data.rooms.recruitmentDDayText
import kr.hanchae.moyeotrip.domain.auth.UserDisplayProfile
import kr.hanchae.moyeotrip.ui.LocalServerData
import kr.hanchae.moyeotrip.ui.components.CachedRemoteImage
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyState
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyText
import kr.hanchae.moyeotrip.ui.components.MoyeoLinearProgress
import kr.hanchae.moyeotrip.ui.components.MoyeoPlaceholderShape
import kr.hanchae.moyeotrip.ui.components.ServerListState
import kr.hanchae.moyeotrip.ui.components.afterReload
import kr.hanchae.moyeotrip.ui.state.LocalTabDataStore
import kr.hanchae.moyeotrip.ui.theme.Coral
import kr.hanchae.moyeotrip.ui.theme.ForestGreen
import kr.hanchae.moyeotrip.ui.theme.MoyeoTheme

@Composable
fun MyScreen(
    userProfile: UserDisplayProfile,
    onOpenTrip: (String) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenMyFeed: () -> Unit,
    onOpenFriendDex: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCustomerCenter: () -> Unit,
    onOpenFriends: () -> Unit = {},
    /** 26 `찜한 코스` 카드 → 14 코스 상세. */
    onOpenCourse: (String) -> Unit = {}
) {
    val server = LocalServerData.current
    // 마이도 홈처럼 가진 것을 그리며 뒤에서 갱신한다 — 탭 바깥 보관소에 둔다(정본 R1·R3).
    val my = LocalTabDataStore.current.my
    val selectedTab = my.selectedTab
    val serverProfile = my.profile
    val myPublicProfile = my.publicProfile
    val rooms = my.rooms
    val dexCount = my.dexCount
    // 찜 목록 둘 다 **세그먼트를 열었을 때만** 부른다 — 안 보는 목록을 미리 받아오지 않는다.
    var favorites by remember(server) { mutableStateOf<List<ChatRoomSearchResult>?>(null) }
    val favoriteRooms = favorites
    LaunchedEffect(server, selectedTab, my.reloadKey) {
        if (server == null || selectedTab != MyTripTab.Favorites) return@LaunchedEffect
        favorites = runCatching { server.chatRooms.favoriteRooms() }.getOrNull().orEmpty()
    }
    var likedCourses by remember(server) { mutableStateOf<List<LikedTravelCourse>?>(null) }
    val favoriteCourses = likedCourses
    LaunchedEffect(server, selectedTab, my.reloadKey) {
        if (server == null || selectedTab != MyTripTab.FavoriteCourses) return@LaunchedEffect
        likedCourses = runCatching { server.courses.likedCourses() }.getOrNull().orEmpty()
    }
    LaunchedEffect(server, my.reloadKey) {
        if (server == null) {
            my.profile = null
            my.publicProfile = null
            my.rooms = ServerListState.Loaded(emptyList())
            my.dexCount = null
            return@LaunchedEffect
        }
        // 로딩 문구는 아직 아무것도 못 받아 봤을 때만 띄운다(정본 R2).
        if (!my.loaded) my.rooms = ServerListState.Loading
        // 갱신에 실패하면 가진 값을 그대로 둔다 — 보고 있던 내용을 오류로 덮지 않는다(정본 R2).
        my.profile = runCatching { server.userProfile.profile() }.getOrNull() ?: my.profile
        // 3칸 지표(여행·매너·피드)는 **공개 프로필** 응답에만 있다 — `users/me/profile` 에는 없다.
        // id 는 액세스 토큰에서 동기로 읽으므로 따로 조회하지 않는다(정본 R6).
        server.signedInUserId()?.let { myUserId ->
            my.publicProfile = runCatching { server.userProfile.publicProfile(myUserId) }.getOrNull()
                ?: my.publicProfile
        }
        my.rooms = my.rooms.afterReload(runCatching { server.chatRooms.myRooms() })
        my.dexCount = runCatching { server.social.travelDex().size }.getOrNull() ?: my.dexCount
        // 성공해서 보여줄 게 생겼을 때만 기록한다 — 실패하면 다음 진입에서 다시 로딩부터 시작한다(정본 R3-1).
        if (my.rooms is ServerListState.Loaded) my.markLoaded()
    }
    val loadedRooms = (rooms as? ServerListState.Loaded)?.items.orEmpty()
    val ongoing = loadedRooms.filterNot(MyChatRoom::ended)
    val past = loadedRooms.filter(MyChatRoom::ended)

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
        item { MyPageHeader(onOpenSettings = onOpenSettings) }
        item {
            MyProfileSummaryCard(
                userProfile = userProfile,
                serverProfile = serverProfile,
                publicProfile = myPublicProfile,
                onClick = onOpenProfile
            )
        }
        item { MySectionHeader(title = "내 여행", countText = "${ongoing.size}개") }
        item {
            MySegmentedControl(
                selectedTab = selectedTab,
                onSelect = { my.selectedTab = it }
            )
        }
        val state = rooms
        when {
            state is ServerListState.Loading -> item { MoyeoEmptyState(MoyeoEmptyText.LOADING) }

            state is ServerListState.Failed -> item {
                MoyeoEmptyState(MoyeoEmptyText.FAILED, onRetry = my::reload)
            }

            selectedTab == MyTripTab.Ongoing -> if (ongoing.isEmpty()) {
                item { MoyeoEmptyState(MoyeoEmptyText.NO_JOINED_ROOMS, testTag = "my-trips-empty") }
            } else {
                ongoing.forEach { room ->
                    item {
                        MyServerRoomCard(room = room, onClick = { onOpenTrip("room-${room.roomId}") })
                    }
                }
            }

            selectedTab == MyTripTab.Past -> if (past.isEmpty()) {
                item { MoyeoEmptyState("아직 다녀온 여행 기록이 없어요.", testTag = "my-past-empty") }
            } else {
                past.forEach { room ->
                    item {
                        MyServerRoomCard(room = room, onClick = { onOpenTrip("room-${room.roomId}") })
                    }
                }
            }

            // 26 찜한 코스 — 응답이 목록 카드보다 좁다(소요 시간·거리·평점이 없다).
            // 없는 값을 카드에 두지 않는다.
            selectedTab == MyTripTab.FavoriteCourses -> when {
                favoriteCourses == null -> item { MoyeoEmptyState(MoyeoEmptyText.LOADING) }

                favoriteCourses.isEmpty() -> item {
                    MoyeoEmptyState("아직 찜한 코스가 없어요.", testTag = "my-favorite-courses-empty")
                }

                else -> favoriteCourses.forEach { course ->
                    item(key = "liked-course-${course.courseId}") {
                        MyLikedCourseCard(
                            course = course,
                            onClick = { onOpenCourse("srv-${course.courseId}") }
                        )
                    }
                }
            }

            // 26-1 찜한 모집 (정본 §6-2) — 전체 화면으로도 열 수 있다(`favorite_rooms` 라우트)
            selectedTab == MyTripTab.Favorites -> when {
                favoriteRooms == null -> item { MoyeoEmptyState(MoyeoEmptyText.LOADING) }

                favoriteRooms.isEmpty() -> item {
                    MoyeoEmptyState(MoyeoEmptyText.NO_ROOMS, testTag = "my-favorites-empty")
                }

                else -> {
                    favoriteRooms.forEach { room ->
                        item(key = "favorite-${room.roomId}") {
                            SearchResultRoomCard(
                                room = room,
                                onClick = { onOpenTrip("room-${room.roomId}") }
                            )
                        }
                    }
                    item(key = "favorite-note") {
                        Text(
                            "찜한 모집이 마감되거나 여행이 끝나도 목록에는 남아요. 하트를 다시 누르면 빠져요.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        // 「지금까지 만난 친구」 카드를 두지 않는다 — 아래 「메뉴」의 「친구 도감」 줄과
        // **같은 곳으로 가는 진입점**이라 한 화면에 두 번 있었다. 기획·웹·iOS 에는 없다
        // (26, 사용자 지적 2026-09-09).
        item {
            MyHubMenuPanel(
                onOpenMyFeed = onOpenMyFeed,
                onOpenFriendDex = onOpenFriendDex,
                onOpenProfileEdit = onOpenProfile,
                onOpenFriends = onOpenFriends,
                onOpenCustomerCenter = onOpenCustomerCenter,
                dexCount = dexCount
            )
        }
    }
}

/**
 * 26 `찜한 코스` 카드 — `LikedTravelCourseResponse` 가 주는 값만 그린다.
 * 소요 시간·거리·평점은 이 응답에 **없다**. 0 이나 `평가 없음` 으로 채우지 않고 줄 자체를 두지 않는다.
 */
@Composable
private fun MyLikedCourseCard(course: LikedTravelCourse, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("my-liked-course-${course.courseId}"),
        shape = RoundedCornerShape(14.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            CachedRemoteImage(
                url = course.thumbnail,
                contentDescription = course.title,
                modifier = Modifier
                    .size(width = MyCardMetrics.thumbWidth, height = MyCardMetrics.thumbHeight)
                    .clip(RoundedCornerShape(MyCardMetrics.thumbRadius)),
                contentScale = ContentScale.Crop,
                fallbackShape = MoyeoPlaceholderShape.LANDSCAPE
            ) {
                Box(
                    Modifier
                        .size(width = MyCardMetrics.thumbWidth, height = MyCardMetrics.thumbHeight)
                        .clip(RoundedCornerShape(MyCardMetrics.thumbRadius))
                        .background(colors.surfaceVariant)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    course.title,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                course.description?.takeIf(String::isNotBlank)?.let { description ->
                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                course.tags.joinToString(" ") { "#${it.name}" }.takeIf(String::isNotBlank)?.let { tags ->
                    Text(
                        tags,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.primary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
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
                contentScale = ContentScale.Crop,
                fallbackShape = MoyeoPlaceholderShape.SQUARE
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
                    recruitmentDDayText(room.recruitmentDDay)?.let { dday ->
                        DDayChip(text = dday)
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
private fun MyDogamShortcut(onClick: () -> Unit, dexCount: Int?) {
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
                // 마릿수는 서버 도감이 근거다 — 못 받아오면 줄째로 뺀다
                dexCount?.let { count ->
                    Text(
                        text = "${count}마리 · 최근 동행 순",
                        modifier = Modifier.testTag("my-friend-dex-preview-count"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
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
    serverProfile: ServerUserProfile? = null,
    /** 26 상단 3칸 지표의 근거. 못 받았으면 지표 줄을 그리지 않는다 — 0 으로 채우지 않는다. */
    publicProfile: ServerPublicProfile? = null
) {
    val displayName = serverProfile?.nickname ?: userProfile.nickname
    val profileImageUrl = serverProfile?.profileImageUrl ?: userProfile.profileImageUrl

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
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 아바타는 프로필 사진이 있으면 사진, 없으면 서버 닉네임의 동물이다 (R5 정본)
                UserAvatar(
                    imageUrl = profileImageUrl,
                    nickname = displayName,
                    modifier = Modifier.size(50.dp),
                    fallbackFontSize = 23.sp
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    if (!displayName.isNullOrBlank()) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    serverProfile?.introduction?.takeIf(String::isNotBlank)?.let { bio ->
                        Text(
                            text = bio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
            // 화면기획 26 상단 3칸 지표. 근거는 공개 프로필 응답뿐이라 못 받으면 줄째로 빠진다.
            val metrics = buildList {
                publicProfile?.completedTripCount?.let { add("$it" to "여행") }
                // 매너는 소수 한 자리다 — `5.0점` 처럼 적는다.
                publicProfile?.mannerRating?.let { add("%.1f".format(it) to "매너") }
                publicProfile?.feedCount?.let { add("$it" to "피드") }
            }
            if (metrics.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .5f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .testTag("my-profile-metrics"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    metrics.forEachIndexed { index, (value, label) ->
                        if (index > 0) {
                            Box(
                                Modifier
                                    .height(28.dp)
                                    .width(1.dp)
                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = .5f))
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = value,
                                style = MaterialTheme.typography.titleMedium,
                                color = ForestGreen,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
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
    /** 28 프로필 수정 — 25 가 프로필 카드로 바뀌면서 이 진입점이 메뉴로 왔다. */
    onOpenProfileEdit: () -> Unit,
    onOpenFriends: () -> Unit,
    onOpenCustomerCenter: () -> Unit,
    dexCount: Int?
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
            // 네 표면이 **같은 다섯 줄·같은 순서**를 쓴다 (26, 사용자 지적 2026-09-09).
            // 예전에는 「내 정보 수정」이 빠져 있었고 순서도 달랐다.
            MyHubMenuRow(
                title = "내 피드",
                subtitle = "내가 기록한 경북 여행",
                onClick = onOpenMyFeed,
                modifier = Modifier.testTag("my-feed-shortcut")
            )
            MyHubMenuDivider()
            MyHubMenuRow(
                title = "친구 도감",
                subtitle = dexCount?.let { "${it}마리 · 최근 동행 순" } ?: "여행에서 만난 친구들",
                onClick = onOpenFriendDex,
                modifier = Modifier.testTag("my-friend-dex-shortcut")
            )
            MyHubMenuDivider()
            MyHubMenuRow(
                title = "내 정보 수정",
                subtitle = "프로필과 여행 취향을 관리해요",
                onClick = onOpenProfileEdit,
                modifier = Modifier.testTag("my-profile-edit-shortcut")
            )
            MyHubMenuDivider()
            MyHubMenuRow(
                title = "친구 관리",
                subtitle = "친구 신청과 수락을 관리해요",
                onClick = onOpenFriends,
                modifier = Modifier.testTag("my-friends-shortcut")
            )
            MyHubMenuDivider()
            MyHubMenuRow(
                title = "고객센터",
                // 신고 내역 화면은 없다 — 갈 수 있는 두 창구를 그대로 적는다 (정본 REPORT-CANON §1).
                subtitle = "GitHub 이슈 · 이메일로 문의해요",
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

private object MyCardMetrics {
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

/**
 * 찜은 **코스**와 **모집** 두 가지다. 한 탭에 섞지 않는다 — 서로 다른 것이다.
 *
 * - `찜한 코스` — `GET travel-courses/me/favorites`. 오래 "조회 API 가 없다"고 **틀리게** 적어 두고
 *   탭째 빼 놓았는데, 실서버는 200 을 준다.
 * - `찜한 모집` — `GET chat-rooms/my/favorites` (26-1). 모집 상세 15·탐색 10 카드에 하트가 있는데
 *   모아 보는 곳이 없었다(정본 §6-2).
 */
internal enum class MyTripTab(val label: String) {
    Ongoing("진행중"),
    Past("지난여행"),
    FavoriteCourses("찜한 코스"),
    Favorites("찜한 모집")
}

@Composable
private fun myPageColor(): Color = if (MoyeoTheme.isDark) {
    MaterialTheme.colorScheme.background
} else {
    MaterialTheme.colorScheme.surface
}
