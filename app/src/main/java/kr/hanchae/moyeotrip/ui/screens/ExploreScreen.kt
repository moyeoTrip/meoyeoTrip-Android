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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kr.hanchae.moyeotrip.data.api.MoyeoApiException
import kr.hanchae.moyeotrip.data.rooms.ChatRoomSearchResult
import kr.hanchae.moyeotrip.data.rooms.RoomMeetingCluster
import kr.hanchae.moyeotrip.data.rooms.dayTripHoursText
import kr.hanchae.moyeotrip.data.rooms.meetingClusters
import kr.hanchae.moyeotrip.data.rooms.meetingText
import kr.hanchae.moyeotrip.data.rooms.statusLabel
import kr.hanchae.moyeotrip.ui.LocalServerData
import kr.hanchae.moyeotrip.ui.components.CachedRemoteImage
import kr.hanchae.moyeotrip.ui.components.KakaoMapView
import kr.hanchae.moyeotrip.ui.components.MapMarker
import kr.hanchae.moyeotrip.ui.components.MapMarkerShape
import kr.hanchae.moyeotrip.ui.components.MapUnavailablePlaceholder
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyState
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyText
import kr.hanchae.moyeotrip.ui.components.MoyeoLatLng
import kr.hanchae.moyeotrip.ui.components.MoyeoPlaceholderShape
import kr.hanchae.moyeotrip.ui.components.ServerListState
import kr.hanchae.moyeotrip.ui.state.LocalTabDataStore
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
    var showingMap by rememberSaveable(startInMap) { mutableStateOf(startInMap) }
    val server = LocalServerData.current
    // 탐색은 "보던 상태 유지" 탭이다 — 목록·고른 필터·찜 상태를 탭 바깥 보관소에 두고
    // 재진입할 때 다시 부르지 않는다(정본 R1·R3).
    val explore = LocalTabDataStore.current.explore
    val rooms = explore.rooms
    val serverTags = explore.tags
    val selectedTagId = explore.selectedTagId
    val favoriteRoomIds = explore.favoriteRoomIds
    val favoriteScope = rememberCoroutineScope()
    LaunchedEffect(server, explore.reloadKey) {
        if (server == null) {
            explore.rooms = ServerListState.Loaded(emptyList())
            explore.mapRooms = emptyList()
            explore.mapAreaError = null
            explore.tags = emptyList()
            explore.favoriteRoomIds = emptySet()
            return@LaunchedEffect
        }
        // 성공해서 보여줄 목록이 있을 때만 그대로 그린다 — 재진입에 다시 부르지 않는다(정본 R3).
        // 직전 조회가 실패했으면 보여줄 게 없으므로 다시 부른다(R3-1).
        if (explore.loaded) return@LaunchedEffect
        // 여기까지 왔다는 것은 캐시가 없다는 뜻이라 로딩 문구가 맞다(정본 R2).
        explore.rooms = ServerListState.Loading
        val loaded = runCatching { server.chatRooms.search() }
        explore.rooms = loaded.fold({ ServerListState.Loaded(it) }, { ServerListState.Failed })
        // 지도에 찍을 모집. 좌표가 오는 것은 이 목록뿐이다 — 검색 응답에는 집합 좌표가 없다.
        val mapLoaded = runCatching {
            server.chatRooms.mapRooms(
                latitude = GYEONGBUK_CENTER.latitude,
                longitude = GYEONGBUK_CENTER.longitude,
                radiusKm = GYEONGBUK_RADIUS_KM
            )
        }
        // 400 40040 은 "모임이 없다"가 아니라 "검색 영역이 유효 범위를 벗어났다"다.
        // 지금 보내는 값(120km)은 상한 200km 안이라 걸리지 않지만, 걸리면 조용히 빈 지도가 되면 안 된다.
        explore.mapAreaError = (mapLoaded.exceptionOrNull() as? MoyeoApiException)
            ?.takeIf(MoyeoApiException::invalidMapSearchArea)
            ?.message
        explore.mapRooms = mapLoaded.getOrElse { explore.mapRooms }
        // 찜은 검색 응답의 favorite 이 근거다. 토글 결과도 서버 응답값으로만 갱신한다.
        loaded.onSuccess { results ->
            explore.favoriteRoomIds = results
                .filter(ChatRoomSearchResult::favorite)
                .map(ChatRoomSearchResult::roomId)
                .toSet()
        }
        explore.tags = runCatching { server.courses.tags() }.getOrElse { explore.tags }
        if (loaded.isSuccess) explore.markLoaded()
    }
    fun toggleRoomFavorite(roomId: Long) {
        val dependencies = server ?: return
        favoriteScope.launch {
            runCatching { dependencies.chatRooms.toggleFavorite(roomId) }.onSuccess { favorite ->
                // 이 화면이 바꾼 값이라 해당 항목만 갱신한다 — 목록을 통째로 다시 부르지 않는다(정본 R4).
                explore.favoriteRoomIds = if (favorite) {
                    explore.favoriteRoomIds + roomId
                } else {
                    explore.favoriteRoomIds - roomId
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(explorePageColor())
    ) {
        if (showingMap) {
            ExploreMapView(
                mapRooms = explore.mapRooms,
                mapAreaError = explore.mapAreaError,
                searchRooms = (rooms as? ServerListState.Loaded)?.items.orEmpty(),
                favoriteRoomIds = favoriteRoomIds,
                modifier = Modifier.fillMaxSize(),
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
                item { ExploreHeader(onMenuClick = { showingMap = true }) }
                item { ExploreSearchSurface(onClick = onOpenSearch) }
                if (serverTags.isNotEmpty()) {
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            item {
                                ExploreFilterChip(
                                    text = "전체",
                                    selected = selectedTagId == null,
                                    onClick = { explore.selectedTagId = null }
                                )
                            }
                            items(serverTags, key = { it.tagId }) { tag ->
                                ExploreFilterChip(
                                    text = tag.name,
                                    selected = selectedTagId == tag.tagId,
                                    onClick = { explore.selectedTagId = tag.tagId }
                                )
                            }
                        }
                    }
                }
                val state = rooms
                val visibleRooms = (state as? ServerListState.Loaded)?.items?.filter { room ->
                    selectedTagId == null || room.tags.any { it.tagId == selectedTagId }
                }
                when {
                    server == null -> item {
                        MoyeoEmptyState(MoyeoEmptyText.SIGN_IN_EXPLORE, testTag = "explore-signed-out")
                    }

                    state is ServerListState.Loading -> item { MoyeoEmptyState(MoyeoEmptyText.LOADING) }

                    state is ServerListState.Failed -> item {
                        MoyeoEmptyState(MoyeoEmptyText.FAILED, onRetry = explore::reload)
                    }

                    visibleRooms.isNullOrEmpty() -> item {
                        MoyeoEmptyState(MoyeoEmptyText.NO_ROOMS, testTag = "explore-empty")
                    }

                    else -> items(visibleRooms, key = { it.roomId }) { room ->
                        ExploreServerRoomRow(
                            room = room,
                            favorite = room.roomId in favoriteRoomIds,
                            onClick = { onOpenRoom(room.roomId) },
                            onFavoriteClick = { toggleRoomFavorite(room.roomId) }
                        )
                    }
                }
            }
        }
        if (!showingMap) {
            FloatingActionButton(
                onClick = { onCreateRecruitment(NEW_RECRUITMENT_COURSE_KEY) },
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

/**
 * 서버 모집(chat-rooms/search) 행 — 화면기획 10의 카드 구성을 그대로 쓴다.
 * 찜 하트·상태 배지는 목록 응답에 근거가 있다. 모집 마감 D-day 는 쓰지 않는다 —
 * 2026-08-26 검색 응답 축소로 빠졌고, 탐색 카드에 표시하지 않는 것이 기획상 정상이다.
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
                // 모집 마감 D-day 는 탐색 카드에 쓰지 않는다 — 기획상 정상이고 서버 검색 응답에도 없다
                // (2026-08-26 확정). 마감 확인이 필요하면 모집 상세로 들어간다.
                Text(
                    text = "${room.participantCount}/${room.maxParticipants}명",
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
            contentScale = ContentScale.Crop,
            fallbackShape = MoyeoPlaceholderShape.SQUARE
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

/**
 * 지도 반경 조회의 기준점 — 경상북도 중심 근처.
 *
 * 화면 대각선으로 반경을 계산하는 방식은 지도 이동이 붙은 뒤에 쓴다. 지금은 경북 전역을 덮는
 * 고정 반경으로 받는다(서버는 상한이 없지만 과도한 값을 보내지 않는다 — BE 요청 §4-2).
 */
private val GYEONGBUK_CENTER = MoyeoLatLng(36.4, 128.9)
private const val GYEONGBUK_RADIUS_KM = 120.0

@Composable
private fun ExploreMapView(
    /** 지도 핀 전용 목록 — `GET /chat-rooms/map`. 검색 응답에는 좌표가 없다. */
    mapRooms: List<ChatRoomSearchResult>,
    /** `400 40040 INVALID_MAP_SEARCH_AREA` 일 때 서버 문구. 빈 지도와 구분해서 보여준다. */
    mapAreaError: String?,
    searchRooms: List<ChatRoomSearchResult>,
    favoriteRoomIds: Set<Long>,
    modifier: Modifier = Modifier,
    onOpenRoom: (Long) -> Unit,
    onToggleRoomFavorite: (Long) -> Unit
) {
    val roomClusters = remember(mapRooms) { mapRooms.meetingClusters() }
    // 화면기획 11: 핀을 누르면 아래 카드가 그 모집으로 바뀌고, **카드를 눌러야** 모집 상세로 간다.
    // 누른 적이 없으면 첫 묶음을 보여준다(웹 11과 같다).
    var selectedClusterId by rememberSaveable(mapRooms) { mutableStateOf<String?>(null) }
    val selectedRoom = remember(mapRooms, searchRooms, roomClusters, selectedClusterId) {
        val cluster = roomClusters.firstOrNull { it.markerId() == selectedClusterId }
            ?: roomClusters.firstOrNull()
        // 묶음의 **첫 모집**을 카드에 올린다 — 웹·iOS 와 같은 선택 규칙이다.
        cluster?.roomIds?.firstOrNull()?.let { roomId ->
            // 지도 응답이 검색 응답의 상위집합이다 — 2026-08-26 응답 축소로 검색에서 집합 좌표와
            // meetingDetails 가 빠졌다. 검색 쪽을 먼저 고르면 집합 안내 줄이 조용히 사라진다.
            mapRooms.firstOrNull { it.roomId == roomId } ?: searchRooms.firstOrNull { it.roomId == roomId }
        }
    }
    Box(modifier = modifier) {
        val clusters = remember(roomClusters) { roomClusters.roomClusterMarkers() }
        if (mapAreaError != null) {
            // 검색 영역이 유효 범위를 벗어난 것이지 모임이 없는 게 아니다 — 서버 문구를 그대로 보여준다.
            MoyeoEmptyState(
                mapAreaError,
                modifier = Modifier.align(Alignment.Center),
                testTag = "explore-map-area-error"
            )
        } else if (clusters.isEmpty()) {
            // 좌표가 없으면 지도를 그릴 근거가 없다 — 가짜 지도를 대신 그리지 않는다
            MoyeoEmptyState(
                MoyeoEmptyText.NO_ROOMS,
                modifier = Modifier.align(Alignment.Center),
                testTag = "explore-map-empty"
            )
        } else {
            KakaoMapView(
                center = clusters.first().position,
                modifier = Modifier.matchParentSize(),
                markers = clusters,
                zoomLevel = 9,
                onMarkerClick = { markerId -> selectedClusterId = markerId },
                fallback = { fallbackModifier -> MapUnavailablePlaceholder(fallbackModifier) }
            )
        }
        // 화면기획 11의 내 위치 버튼 — 카드 위 우측
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                // 화면기획 11에서 버튼은 카드 위로 한 칸 띄운 자리다(카드 상단에서 ≈100dp).
                // 카드를 기획대로 내리면서(64 → 2) 같은 양만큼 함께 내린다 — 카드와의 간격은 그대로다.
                .padding(end = 18.dp, bottom = 186.dp)
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
        if (selectedRoom != null) {
            SelectedMapRoom(
                room = selectedRoom,
                favorite = selectedRoom.roomId in favoriteRoomIds,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    // 화면기획 11은 카드가 하단 탭바에 **붙어** 있다(카드 bottom 94 · 탭바 96 = 틈 2).
                    // 이 화면은 스캐폴드 `innerPadding` 으로 탭바를 이미 비켜 있으므로 이 값이 곧 그 틈이다.
                    .padding(horizontal = 18.dp, vertical = 2.dp),
                onClick = { onOpenRoom(selectedRoom.roomId) },
                onFavoriteClick = { onToggleRoomFavorite(selectedRoom.roomId) }
            )
        }
    }
}

/**
 * 마커 id. **핀 탭이 이 값으로 묶음을 되찾으므로** 마커를 만드는 쪽과 찾는 쪽이 같은 함수를 쓴다 —
 * 두 곳에서 따로 조립하면 규칙이 어긋나는 순간 탭이 조용히 아무 것도 못 찾는다.
 */
private fun RoomMeetingCluster.markerId(): String = "explore-rooms-${roomIds.joinToString("-")}"

/** 서버 모집의 집합 좌표 묶음을 화면기획 11과 같은 초록 원 + 개수 마커로 만든다. */
private fun List<RoomMeetingCluster>.roomClusterMarkers(): List<MapMarker> = map { cluster ->
    MapMarker(
        id = cluster.markerId(),
        position = MoyeoLatLng(cluster.point.latitude, cluster.point.longitude),
        shape = MapMarkerShape.Cluster,
        badge = cluster.roomIds.size.toString()
    )
}

/**
 * 화면기획 11의 지도 카드에 서버 모집을 올린 형태. 줄 구성(제목·안내·인원)은 그대로 두고
 * 부제 자리에는 서버가 주는 **집합 안내**를, 인원 뒤에는 **당일 여행 시간**을 쓴다.
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
                contentScale = ContentScale.Crop,
                fallbackShape = MoyeoPlaceholderShape.SQUARE
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
private fun explorePageColor(): Color = if (MoyeoTheme.isDark) {
    MaterialTheme.colorScheme.background
} else {
    MaterialTheme.colorScheme.surface
}
