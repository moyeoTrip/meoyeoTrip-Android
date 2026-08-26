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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kr.hanchae.moyeotrip.data.MockTripRepository
import kr.hanchae.moyeotrip.data.TripCourse
import kr.hanchae.moyeotrip.data.rooms.ChatRoomSearchResult
import kr.hanchae.moyeotrip.data.rooms.RoomMeetingCluster
import kr.hanchae.moyeotrip.data.rooms.RoomTag
import kr.hanchae.moyeotrip.data.rooms.dayTripHoursText
import kr.hanchae.moyeotrip.data.rooms.meetingClusters
import kr.hanchae.moyeotrip.data.rooms.meetingText
import kr.hanchae.moyeotrip.data.rooms.recruitmentDeadlineText
import kr.hanchae.moyeotrip.data.rooms.statusLabel
import kr.hanchae.moyeotrip.ui.LocalServerData
import kr.hanchae.moyeotrip.ui.components.CachedRemoteImage
import kr.hanchae.moyeotrip.ui.components.KakaoMapView
import kr.hanchae.moyeotrip.ui.components.MapMarker
import kr.hanchae.moyeotrip.ui.components.MapMarkerShape
import kr.hanchae.moyeotrip.ui.components.MoyeoLatLng
import kr.hanchae.moyeotrip.ui.theme.Coral
import kr.hanchae.moyeotrip.ui.theme.MoyeoTheme

@Composable
fun ExploreScreen(
    onOpenCourse: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onCreateRecruitment: (String) -> Unit,
    startInMap: Boolean = false,
    onOpenRoom: (Long) -> Unit = {}
) {
    val filters = listOf("전체", "자연", "역사", "체험", "힐링")
    var selectedFilter by rememberSaveable { mutableStateOf(filters.first()) }
    var showingMap by rememberSaveable(startInMap) { mutableStateOf(startInMap) }
    var likedCourseIds by rememberSaveable { mutableStateOf(listOf("cheongsong-juwangsan")) }
    val courses = remember(selectedFilter) {
        webExploreCourses().filter { it.matchesExploreFilter(selectedFilter) }
    }
    // 로그인 상태면 실서버 모집 목록(GET chat-rooms/search)으로 대체한다.
    // 실패·미로그인 시에는 기존 목데이터 목록을 그대로 유지한다.
    val server = LocalServerData.current
    var serverRooms by remember(server) { mutableStateOf<List<ChatRoomSearchResult>?>(null) }
    // 테마 칩 후보는 서버 코스 태그(GET travel-courses/tags)를 쓴다 — 목데이터 분류를 서버 모드에 섞지 않는다
    var serverTags by remember(server) { mutableStateOf<List<RoomTag>>(emptyList()) }
    var selectedTagId by remember(server) { mutableStateOf<Long?>(null) }
    // 찜은 검색 응답의 favorite 이 근거다. 토글 결과도 서버 응답값으로만 갱신한다.
    var favoriteRoomIds by remember(server) { mutableStateOf<Set<Long>>(emptySet()) }
    val favoriteScope = rememberCoroutineScope()
    LaunchedEffect(server) {
        if (server == null) {
            serverRooms = null
            serverTags = emptyList()
            favoriteRoomIds = emptySet()
            return@LaunchedEffect
        }
        val rooms = runCatching { server.chatRooms.search() }.getOrNull()
        serverRooms = rooms
        favoriteRoomIds = rooms.orEmpty()
            .filter(ChatRoomSearchResult::favorite)
            .map(ChatRoomSearchResult::roomId)
            .toSet()
        serverTags = runCatching { server.courses.tags() }.getOrElse { emptyList() }
    }
    fun toggleRoomFavorite(roomId: Long) {
        val rooms = server ?: return
        favoriteScope.launch {
            runCatching { rooms.chatRooms.toggleFavorite(roomId) }.onSuccess { favorite ->
                favoriteRoomIds = if (favorite) favoriteRoomIds + roomId else favoriteRoomIds - roomId
            }
        }
    }
    fun toggleFavorite(courseId: String) {
        likedCourseIds = if (courseId in likedCourseIds) {
            likedCourseIds - courseId
        } else {
            likedCourseIds + courseId
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(explorePageColor())
    ) {
        if (showingMap) {
            ExploreMapView(
                courses = webExploreCourses(),
                likedCourseIds = likedCourseIds,
                serverRooms = serverRooms.orEmpty(),
                favoriteRoomIds = favoriteRoomIds,
                modifier = Modifier.fillMaxSize(),
                onOpenCourse = onOpenCourse,
                onToggleFavorite = ::toggleFavorite,
                onOpenRoom = onOpenRoom,
                onToggleRoomFavorite = ::toggleRoomFavorite
            )
            ExploreMapHeader(
                onBackClick = { showingMap = false },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .background(explorePageColor().copy(alpha = 0.94f))
                    .padding(horizontal = 18.dp, vertical = 18.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 22.dp, top = 26.dp, end = 22.dp, bottom = 112.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    ExploreHeader(onMenuClick = { showingMap = true })
                }
                item {
                    ExploreSearchSurface(onClick = onOpenSearch)
                }
                val allRooms = serverRooms
                val rooms = allRooms?.filter { room ->
                    selectedTagId == null || room.tags.any { it.tagId == selectedTagId }
                }
                if (rooms != null) {
                    if (serverTags.isNotEmpty()) {
                        item {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                                item {
                                    ExploreFilterChip(
                                        text = "전체",
                                        selected = selectedTagId == null,
                                        onClick = { selectedTagId = null }
                                    )
                                }
                                items(serverTags, key = { it.tagId }) { tag ->
                                    ExploreFilterChip(
                                        text = tag.name,
                                        selected = selectedTagId == tag.tagId,
                                        onClick = { selectedTagId = tag.tagId }
                                    )
                                }
                            }
                        }
                    }
                    if (rooms.isEmpty()) {
                        item {
                            Text(
                                text = "지금 모집 중인 모임이 없어요.",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(rooms, key = { it.roomId }) { room ->
                            ExploreServerRoomRow(
                                room = room,
                                favorite = room.roomId in favoriteRoomIds,
                                onClick = { onOpenRoom(room.roomId) },
                                onFavoriteClick = { toggleRoomFavorite(room.roomId) }
                            )
                        }
                    }
                } else {
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            items(filters) { filter ->
                                ExploreFilterChip(
                                    text = filter,
                                    selected = selectedFilter == filter,
                                    onClick = { selectedFilter = filter }
                                )
                            }
                        }
                    }

                    items(courses, key = { it.id }) { course ->
                        ExploreCourseRow(
                            course = course,
                            liked = course.id in likedCourseIds,
                            onClick = { onOpenCourse(course.id) },
                            onFavoriteClick = { toggleFavorite(course.id) }
                        )
                    }
                }
            }
        }
        if (!showingMap) {
            FloatingActionButton(
                onClick = { onCreateRecruitment(courses.firstOrNull()?.id ?: "cheongsong-juwangsan") },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 22.dp, bottom = 14.dp)
                    .size(50.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "모집 만들기",
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun ExploreMapHeader(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(40.dp)
                .testTag("explore-map-back")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "목록 탐색",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = "지도 탐색",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.ExtraBold
        )
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(32.dp)
                .testTag("explore-map-list")
        ) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "목록 탐색",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun ExploreHeader(onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "탐색",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.ExtraBold
        )
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "메뉴",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

@Composable
private fun ExploreSearchSurface(onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(11.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = if (MoyeoTheme.isDark) 0.dp else 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("explore-search-entry")
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "어디로 떠나고 싶나요?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ExploreFilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = if (selected) colors.primary else colors.surface,
        border = BorderStroke(1.dp, if (selected) colors.primary else colors.outline)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) colors.onPrimary else colors.onSurface,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ExploreCourseRow(course: TripCourse, liked: Boolean, onClick: () -> Unit, onFavoriteClick: () -> Unit) {
    val favoriteDescription = if (liked) "찜 해제" else "찜"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
            .testTag("explore-course-${course.id}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = if (MoyeoTheme.isDark) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CoursePreview(
                    course = course,
                    modifier = Modifier.size(width = 88.dp, height = 68.dp)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
                ) {
                    Text(
                        text = course.title,
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = course.exploreArea(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${course.participants}/${course.capacity}명",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("explore-course-favorite-${course.id}")
                    .semantics { contentDescription = favoriteDescription }
            ) {
                Icon(
                    imageVector = if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = null,
                    tint = if (liked) Coral else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(21.dp)
                )
            }
        }
    }
}

/**
 * 서버 모집(chat-rooms/search) 행 — 화면기획 10의 카드 구성을 그대로 쓴다.
 * 찜 하트·상태 배지·모집 마감 D-day 는 2026-08-24 서버 패치로 목록 응답에 근거가 생겨 되살렸다
 * (그 전까지는 근거가 없어 숨겨 뒀다). 값이 없으면 그 표기만 사라진다.
 */
@Composable
private fun ExploreServerRoomRow(
    room: ChatRoomSearchResult,
    favorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    val favoriteDescription = if (favorite) "찜 해제" else "찜"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
            .testTag("explore-room-${room.roomId}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = if (MoyeoTheme.isDark) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ServerRoomThumbnail(room = room, modifier = Modifier.size(width = 88.dp, height = 68.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
            ) {
                Text(
                    text = room.title,
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val subtitle = listOfNotNull(
                    room.courseTitle,
                    room.tags.joinToString(", ") { it.name }.takeIf(String::isNotBlank)
                ).joinToString(" · ")
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = listOfNotNull(
                        "${room.participantCount}/${room.maxParticipants}명",
                        room.recruitmentDeadlineText()
                    ).joinToString(" · "),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("explore-room-favorite-${room.roomId}")
                    .semantics { contentDescription = favoriteDescription }
            ) {
                Icon(
                    imageVector = if (favorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = null,
                    tint = if (favorite) Coral else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(21.dp)
                )
            }
        }
    }
}

/** 서버 모집 썸네일 + 상태 배지. 배지 자리는 화면기획 10의 카드 썸네일 좌하단 그대로다. */
@Composable
private fun ServerRoomThumbnail(room: ChatRoomSearchResult, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        CachedRemoteImage(
            url = room.thumbnail,
            contentDescription = room.title,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
        room.statusLabel()?.let { status ->
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 7.dp, bottom = 7.dp),
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = status,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun CoursePreview(course: TripCourse, modifier: Modifier = Modifier, showsBadge: Boolean = true) {
    Box(
        modifier = modifier
    ) {
        CourseScenicPanel(course = course, modifier = Modifier.fillMaxSize(), cornerRadius = 8.dp)
        if (!showsBadge) return@Box
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 7.dp, bottom = 7.dp),
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.primary
        ) {
            Text(
                text = course.exploreStatus(),
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun ExploreMapView(
    courses: List<TripCourse>,
    likedCourseIds: List<String>,
    serverRooms: List<ChatRoomSearchResult>,
    favoriteRoomIds: Set<Long>,
    modifier: Modifier = Modifier,
    onOpenCourse: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenRoom: (Long) -> Unit,
    onToggleRoomFavorite: (Long) -> Unit
) {
    val selectedCourse = courses.first()
    // 서버 모집의 집합 좌표(둘 다 있는 항목만)가 있으면 목데이터 코스 대신 실제 모임을 지도에 올린다.
    val roomClusters = remember(serverRooms) { serverRooms.meetingClusters() }
    val selectedRoom = remember(serverRooms, roomClusters) {
        roomClusters.firstOrNull()?.roomIds?.firstOrNull()?.let { roomId ->
            serverRooms.firstOrNull { it.roomId == roomId }
        }
    }
    Box(modifier = modifier) {
        // 카카오 실지도 + 지역 묶음 마커. 폴백(키 없음·인증 실패·QA 캡처)에서는 기존 목업 판을 그린다.
        val clusters = remember(courses, roomClusters) {
            roomClusters.takeIf { it.isNotEmpty() }?.roomClusterMarkers() ?: courses.exploreClusterMarkers()
        }
        if (clusters.isEmpty()) {
            // 좌표가 없으면 실지도를 띄울 근거가 없다 — 목업 유지
            ExploreMapMockup(selectedCourse, Modifier.matchParentSize())
        } else {
            KakaoMapView(
                center = clusters.first().position,
                modifier = Modifier.matchParentSize(),
                markers = clusters,
                zoomLevel = 9,
                fallback = { fallbackModifier -> ExploreMapMockup(selectedCourse, fallbackModifier) }
            )
        }
        // 화면기획 11의 내 위치 버튼 — 카드 위 우측
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 176.dp)
                .size(44.dp)
                .testTag("explore-map-my-location"),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.MyLocation,
                    contentDescription = "내 위치",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        val cardModifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(horizontal = 18.dp, vertical = 64.dp)
        if (selectedRoom != null) {
            SelectedMapRoom(
                room = selectedRoom,
                favorite = selectedRoom.roomId in favoriteRoomIds,
                modifier = cardModifier,
                onClick = { onOpenRoom(selectedRoom.roomId) },
                onFavoriteClick = { onToggleRoomFavorite(selectedRoom.roomId) }
            )
        } else {
            SelectedMapCourse(
                course = selectedCourse,
                liked = selectedCourse.id in likedCourseIds,
                modifier = cardModifier,
                onClick = { onOpenCourse(selectedCourse.id) },
                onFavoriteClick = { onToggleFavorite(selectedCourse.id) }
            )
        }
    }
}

/**
 * 화면기획 11의 지역 묶음 마커 — 같은 지역의 코스를 하나의 초록 원 + 흰 개수로 묶는다.
 * 좌표가 없는 코스(latitude/longitude 0.0)는 지도에 올리지 않는다.
 */
private fun List<TripCourse>.exploreClusterMarkers(): List<MapMarker> = this
    .filter { it.latitude != 0.0 && it.longitude != 0.0 }
    .groupBy { it.region }
    .map { (region, group) ->
        MapMarker(
            id = "explore-cluster-$region",
            position = MoyeoLatLng(
                latitude = group.sumOf { it.latitude } / group.size,
                longitude = group.sumOf { it.longitude } / group.size
            ),
            shape = MapMarkerShape.Cluster,
            badge = group.size.toString()
        )
    }

/** 서버 모집의 집합 좌표 묶음을 화면기획 11과 같은 초록 원 + 개수 마커로 만든다. */
private fun List<RoomMeetingCluster>.roomClusterMarkers(): List<MapMarker> = map { cluster ->
    MapMarker(
        id = "explore-rooms-${cluster.roomIds.joinToString("-")}",
        position = MoyeoLatLng(cluster.point.latitude, cluster.point.longitude),
        shape = MapMarkerShape.Cluster,
        badge = cluster.roomIds.size.toString()
    )
}

/** 실지도를 쓸 수 없을 때(키 없음·인증 실패·QA 캡처) 그리는 기존 목업 지도 판. */
@Composable
private fun ExploreMapMockup(selectedCourse: TripCourse, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        MapBackground()
        MapCluster(
            text = "1",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 74.dp, top = 230.dp)
        )
        MapCluster(
            text = "6",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 190.dp)
        )
        MapCluster(
            text = "2",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 82.dp, top = 245.dp)
        )
        MapCluster(
            text = "2",
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 80.dp)
        )
        MapLandmark(
            course = selectedCourse,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 78.dp, bottom = 56.dp)
        )
    }
}

@Composable
private fun MapBackground() {
    val darkTheme = MoyeoTheme.isDark
    val mapBase = if (darkTheme) Color(0xFF101B16) else Color(0xFFE6F1E5)
    val hill = if (darkTheme) Color(0xFF182C22) else Color(0xFFD8E8D5)
    val water = if (darkTheme) Color(0xFF17303B) else Color(0xFFC9E0E5)

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(mapBase)
        val hillPath = Path().apply {
            moveTo(0f, size.height * 0.28f)
            quadraticTo(size.width * 0.22f, size.height * 0.18f, size.width * 0.42f, size.height * 0.29f)
            quadraticTo(size.width * 0.65f, size.height * 0.42f, size.width, size.height * 0.24f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        val waterPath = Path().apply {
            moveTo(size.width * 0.67f, 0f)
            quadraticTo(size.width * 0.88f, size.height * 0.20f, size.width, size.height * 0.24f)
            lineTo(size.width, size.height)
            lineTo(size.width * 0.80f, size.height)
            quadraticTo(size.width * 0.78f, size.height * 0.66f, size.width * 0.73f, size.height * 0.48f)
            quadraticTo(size.width * 0.67f, size.height * 0.24f, size.width * 0.67f, 0f)
            close()
        }
        drawPath(hillPath, hill)
        drawPath(waterPath, water)
    }
}

@Composable
private fun MapCluster(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(32.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = if (MoyeoTheme.isDark) 0.dp else 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun MapLandmark(course: TripCourse, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(52.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = course.imageEmoji, fontSize = 25.sp)
        }
    }
}

/**
 * 화면기획 11의 지도 카드에 서버 모집을 올린 형태. 줄 구성(제목·안내·인원)은 그대로 두고
 * 목데이터의 "지역 · 테마" 자리에는 서버가 주는 **집합 안내**를, 인원 뒤에는 **당일 여행 시간**을 쓴다.
 * 집합 장소가 미정(null)이면 안내 줄이 사라지고, 숙박 방은 시간이 사라진다 — 문구를 지어내지 않는다.
 */
@Composable
private fun SelectedMapRoom(
    room: ChatRoomSearchResult,
    favorite: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    val favoriteDescription = if (favorite) "찜 해제" else "찜"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .testTag("explore-map-selected-room-${room.roomId}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (MoyeoTheme.isDark) 0.dp else 6.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CachedRemoteImage(
                url = room.thumbnail,
                contentDescription = room.title,
                modifier = Modifier
                    .size(width = 84.dp, height = 76.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 84.dp, height = 76.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = room.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                room.meetingText()?.let { meeting ->
                    Text(
                        text = meeting,
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .testTag("explore-map-room-meeting"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = listOfNotNull(
                        "${room.participantCount}/${room.maxParticipants}명",
                        room.dayTripHoursText()
                    ).joinToString(" · "),
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .testTag("explore-map-room-hours"),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("explore-map-room-favorite-${room.roomId}")
                    .semantics { contentDescription = favoriteDescription }
            ) {
                Icon(
                    imageVector = if (favorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = null,
                    tint = if (favorite) Coral else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(21.dp)
                )
            }
        }
    }
}

@Composable
private fun SelectedMapCourse(
    course: TripCourse,
    liked: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    val favoriteDescription = if (liked) "찜 해제" else "찜"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .testTag("explore-map-selected-course")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (MoyeoTheme.isDark) 0.dp else 6.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 화면기획 11의 지도 카드 썸네일에는 상태 배지가 없다
                CoursePreview(
                    course = course,
                    modifier = Modifier.size(width = 84.dp, height = 76.dp),
                    showsBadge = false
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = course.exploreArea(),
                        modifier = Modifier.padding(top = 5.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${course.participants}/${course.capacity}명",
                        modifier = Modifier.padding(top = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("explore-map-favorite-${course.id}")
                    .semantics { contentDescription = favoriteDescription }
            ) {
                Icon(
                    imageVector = if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = null,
                    tint = if (liked) Coral else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(21.dp)
                )
            }
        }
    }
}

@Composable
private fun explorePageColor(): Color = if (MoyeoTheme.isDark) {
    MaterialTheme.colorScheme.background
} else {
    MaterialTheme.colorScheme.surface
}

private fun webExploreCourses(): List<TripCourse> {
    val webPlanOrder = listOf(
        "cheongsong-juwangsan",
        "andong-hahoe",
        "ulleung-island",
        "gyeongju-healing",
        "pohang-sea",
        "mungyeong-saejae",
        "yeongju-buseoksa",
        "andong-dosan"
    )
    val byId = MockTripRepository.courses.associateBy { it.id }
    val orderedCourses = webPlanOrder.mapNotNull(byId::get)
    return orderedCourses + MockTripRepository.courses.filterNot { it.id in webPlanOrder }
}

private fun TripCourse.matchesExploreFilter(filter: String): Boolean = when (filter) {
    "자연" -> {
        region in listOf("청송", "울릉", "문경") ||
            tags.any { it in listOf("숲길", "폭포", "섬", "트레킹", "바다", "단풍") }
    }

    "역사" -> {
        region in listOf("안동", "경주", "영주") ||
            tags.any { it.contains("역사") || it.contains("고택") || it.contains("사찰") }
    }

    "체험" -> tags.any { it in listOf("로컬간식", "피크닉", "사진") } || oneLine.contains("체험")

    "힐링" -> title.contains("힐링") || oneLine.contains("천천히") || oneLine.contains("머무는")

    else -> true
}

/**
 * 화면기획 10의 카드 부제("지역 · 테마") 테마 값. 코스마다 정해진 값이라 지역에서 기계적으로
 * 뽑으면 "역사, 문화"로 뭉개지거나 두 번째 테마가 사라진다.
 */
private val exploreThemeByCourseId = mapOf(
    "cheongsong-juwangsan" to "자연, 히든명소",
    "andong-hahoe" to "역사, 문화",
    "ulleung-island" to "자연, 힐링",
    "gyeongju-healing" to "역사, 야경",
    "pohang-sea" to "바다, 드라이브",
    "mungyeong-saejae" to "자연, 단풍",
    "yeongju-buseoksa" to "역사, 사찰",
    "andong-dosan" to "역사, 그늘"
)

private fun TripCourse.exploreArea(): String {
    val theme = exploreThemeByCourseId[id] ?: tags.take(2).joinToString(", ").ifBlank { "여행" }
    return "$region · $theme"
}

private fun TripCourse.exploreStatus(): String = if (deadlineLabel.contains("확정")) "확정" else "진행중"
