package kr.hanchae.moyeotrip.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kr.hanchae.moyeotrip.data.ServerDataDependencies
import kr.hanchae.moyeotrip.data.courses.TravelCourse
import kr.hanchae.moyeotrip.data.courses.TravelCoursePlace
import kr.hanchae.moyeotrip.data.rooms.ChatRoomSearchResult
import kr.hanchae.moyeotrip.ui.LocalServerData
import kr.hanchae.moyeotrip.ui.components.CachedRemoteImage
import kr.hanchae.moyeotrip.ui.components.CourseRouteMap
import kr.hanchae.moyeotrip.ui.components.CourseRoutePoint
import kr.hanchae.moyeotrip.ui.components.InfoPill
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyState
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyText
import kr.hanchae.moyeotrip.ui.components.MoyeoLatLng
import kr.hanchae.moyeotrip.ui.components.MoyeoPlaceholderShape
import kr.hanchae.moyeotrip.ui.components.SectionHeader
import kr.hanchae.moyeotrip.ui.components.ServerListState
import kr.hanchae.moyeotrip.ui.components.afterReload

/**
 * 화면기획 14 코스 상세 — 서버 코스(GET travel-courses/{id})만 그린다.
 *
 * 라우트가 들고 오는 식별자는 `srv-{courseId}` 다. 그 형태가 아니거나 미로그인이면 코스를 읽어올
 * 근거가 없으므로 빈 상태를 그린다 — 예시 코스를 대신 보여주지 않는다.
 */
@Composable
fun CourseDetailScreen(
    courseId: String,
    onBack: () -> Unit,
    onCreateRecruitment: (String) -> Unit,
    onOpenRoom: (String) -> Unit
) {
    val server = LocalServerData.current
    val serverCourseId = courseId.removePrefix("srv-").toLongOrNull()?.takeIf { courseId.startsWith("srv-") }
    if (serverCourseId == null || server == null) {
        CourseDetailShell(onBack = onBack) {
            MoyeoEmptyState(MoyeoEmptyText.SIGN_IN_EXPLORE, testTag = "course-detail-signed-out")
        }
        return
    }
    ServerCourseDetail(
        courseId = serverCourseId,
        server = server,
        onBack = onBack,
        onCreateRecruitment = onCreateRecruitment,
        onOpenRoom = onOpenRoom
    )
}

/** 헤더만 있는 껍데기 — 빈 상태·로딩·오류를 그릴 때 화면 구조가 흔들리지 않게 공유한다. */
@Composable
private fun CourseDetailShell(onBack: () -> Unit, content: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로",
                    tint = colors.onBackground
                )
            }
            Text(
                text = "코스 상세",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onBackground,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
    }
}

/**
 * 화면기획 14의 "○○ 님이 다녀온 코스" 행.
 *
 * 아바타는 서버가 주는 작성자 프로필 이미지([TravelCourse.creatorProfileImageUrl], 2026-09-02 추가)다.
 * 그 값이 `null` 일 때만(작성자 비공개·탈퇴·이미지 없음) 닉네임 동물 아바타로 떨어진다 —
 * 이미지가 있는데도 동물이 보이던 것이 이 화면의 오래된 지적이었다.
 */
@Composable
private fun CoursePublisherRow(name: String, meta: String, avatar: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            avatar()
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "$name 님이 다녀온 코스",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
                // 서버가 공개일·모임 수를 모두 주지 않으면 빈 줄을 만들지 않는다
                if (meta.isNotBlank()) {
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
            InfoPill(
                text = "여행자 코스",
                container = colors.primaryContainer,
                content = colors.primary
            )
        }
    }
}

@Composable
private fun ServerCourseDetail(
    courseId: Long,
    server: ServerDataDependencies,
    onBack: () -> Unit,
    onCreateRecruitment: (String) -> Unit,
    onOpenRoom: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var course by remember(courseId) { mutableStateOf<TravelCourse?>(null) }
    var loadFailed by remember(courseId) { mutableStateOf(false) }
    var reloadKey by remember(courseId) { mutableIntStateOf(0) }
    LaunchedEffect(courseId, server, reloadKey) {
        loadFailed = false
        val loaded = runCatching { server.courses.course(courseId) }.getOrNull()
        course = loaded
        loadFailed = loaded == null
    }

    // 14 하트 — POST travel-courses/{id}/favorite (토글). 모임 찜은 이미 서버에 저장하는데
    // 코스 찜만 서버로 안 갔다. 상세 응답에 `favorite` 필드가 없어 처음 상태는
    // GET travel-courses/me/favorites 로 판정한다 — 켜져 있는지 지어내지 않는다.
    var favorite by remember(courseId) { mutableStateOf<Boolean?>(null) }
    var favoriteBusy by remember(courseId) { mutableStateOf(false) }
    val favoriteScope = rememberCoroutineScope()
    LaunchedEffect(courseId, server) {
        favorite = runCatching { server.courses.likedCourses() }.getOrNull()
            ?.any { it.courseId == courseId }
    }

    // 14 "모집 중인 모임 보기" — GET travel-courses/{courseId}/chat-rooms (정본 §4).
    // 예전에 "모집을 찾는 API 가 없다"고 잘못 적어 버튼째 빠져 있었다. API 는 있고, 설명까지 이 버튼용이다.
    var showsRooms by remember(courseId) { mutableStateOf(false) }
    var roomsReloadKey by remember(courseId) { mutableIntStateOf(0) }
    var recruitingRooms by remember(courseId) {
        mutableStateOf<ServerListState<ChatRoomSearchResult>>(ServerListState.Loading)
    }
    // 시트를 열었을 때만 부른다 — 상세 첫 화면에 필요 없는 요청을 얹지 않는다.
    LaunchedEffect(courseId, server, showsRooms, roomsReloadKey) {
        if (!showsRooms) return@LaunchedEffect
        recruitingRooms = recruitingRooms.afterReload(runCatching { server.courses.courseChatRooms(courseId) })
    }

    val loadedCourse = course
    if (loadedCourse == null) {
        CourseDetailShell(onBack = onBack) {
            if (loadFailed) {
                MoyeoEmptyState(MoyeoEmptyText.FAILED, onRetry = { reloadKey++ })
            } else {
                MoyeoEmptyState(MoyeoEmptyText.LOADING)
            }
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로",
                        tint = colors.onBackground
                    )
                }
                Text(
                    text = "코스 상세",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onBackground,
                    fontWeight = FontWeight.ExtraBold
                )
                // 상태를 못 읽었으면 하트를 그리지 않는다 — 꺼진 것처럼 보이면 눌러서 되레 풀린다.
                favorite?.let { on ->
                    IconButton(
                        enabled = !favoriteBusy,
                        onClick = {
                            favoriteBusy = true
                            favorite = !on
                            favoriteScope.launch {
                                runCatching { server.courses.toggleCourseFavorite(courseId) }
                                    .onSuccess { saved -> favorite = saved }
                                    .onFailure { favorite = on }
                                favoriteBusy = false
                            }
                        },
                        modifier = Modifier.testTag("course-detail-favorite")
                    ) {
                        Icon(
                            imageVector = if (on) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (on) "찜 해제" else "찜하기",
                            tint = if (on) colors.primary else colors.onSurfaceVariant
                        )
                    }
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 132.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    CachedRemoteImage(
                        url = loadedCourse.thumbnail,
                        contentDescription = loadedCourse.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop,
                        fallbackShape = MoyeoPlaceholderShape.LANDSCAPE
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.surfaceVariant)
                        )
                    }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = loadedCourse.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.onSurface,
                            fontWeight = FontWeight.ExtraBold
                        )
                        loadedCourse.description?.let { description ->
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurfaceVariant
                            )
                        }
                        val meta = listOfNotNull(
                            loadedCourse.travelTime,
                            loadedCourse.distanceKm?.let { "${it}km" },
                            loadedCourse.averageRating?.let { "★ $it (${loadedCourse.ratingCount})" }
                        ).joinToString(" · ")
                        if (meta.isNotBlank()) {
                            Text(
                                text = meta,
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.onSurfaceVariant
                            )
                        }
                        if (loadedCourse.tags.isNotEmpty()) {
                            Text(
                                text = loadedCourse.tags.joinToString(" ") { "#${it.name}" },
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                loadedCourse.creatorNickname?.takeIf { it.isNotBlank() }?.let { nickname ->
                    item {
                        CoursePublisherRow(
                            name = nickname,
                            meta = serverCoursePublisherMeta(loadedCourse)
                        ) {
                            UserAvatar(
                                imageUrl = loadedCourse.creatorProfileImageUrl,
                                nickname = nickname,
                                modifier = Modifier.size(32.dp),
                                fallbackFontSize = 16.sp
                            )
                        }
                    }
                }
                if (loadedCourse.places.isNotEmpty()) {
                    item { SectionHeader(title = "코스 미리보기") }
                    // 좌표가 있는 방문지만 실제 지도에 올린다. 하나도 없으면 지도 자리가 사라지고
                    // 아래 방문지 목록만 남는다 — 손으로 그린 지도를 대신 그리지 않는다.
                    item { CourseRouteMap(points = loadedCourse.places.toRoutePoints()) }
                    items(loadedCourse.places.size) { index ->
                        val place = loadedCourse.places[index]
                        CoursePlaceRow(index = index, place = place)
                    }
                }
            }
        }

        CourseDetailBottomActions(
            onCreateRecruitment = { onCreateRecruitment("srv-$courseId") },
            onOpenRecruitingRooms = { showsRooms = true },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showsRooms) {
        CourseRecruitingRoomsSheet(
            state = recruitingRooms,
            onDismiss = { showsRooms = false },
            onRetry = { roomsReloadKey++ },
            onOpenRoom = { roomId ->
                showsRooms = false
                onOpenRoom("room-$roomId")
            }
        )
    }
}

/**
 * 14 `모집 중인 모임 보기` 결과 시트 — GET travel-courses/{courseId}/chat-rooms.
 *
 * 서버가 주는 것은 "모집 마감 전이고 내가 아직 참가하지 않은 방"뿐이다. 0건이면 정본 문구를 쓴다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseRecruitingRoomsSheet(
    state: ServerListState<ChatRoomSearchResult>,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onOpenRoom: (Long) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .testTag("course-detail-rooms-sheet"),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "모집 중인 모임",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            when (state) {
                ServerListState.Loading -> item { MoyeoEmptyState(MoyeoEmptyText.LOADING) }

                ServerListState.Failed -> item { MoyeoEmptyState(MoyeoEmptyText.FAILED, onRetry = onRetry) }

                is ServerListState.Loaded -> if (state.items.isEmpty()) {
                    item { MoyeoEmptyState(MoyeoEmptyText.NO_ROOMS, testTag = "course-detail-rooms-empty") }
                } else {
                    items(state.items, key = { it.roomId }) { room ->
                        SearchResultRoomCard(room = room, onClick = { onOpenRoom(room.roomId) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CoursePlaceRow(index: Int, place: TravelCoursePlace) {
    val colors = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(colors.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${index + 1}",
                style = MaterialTheme.typography.labelSmall,
                color = colors.primary,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = place.title,
                style = MaterialTheme.typography.labelLarge,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold
            )
            val placeMeta = listOfNotNull(
                "${place.dayNumber}일차",
                place.visitTime?.take(5)
            ).joinToString(" · ")
            Text(
                text = placeMeta,
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CourseDetailBottomActions(
    onCreateRecruitment: () -> Unit,
    onOpenRecruitingRooms: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface)
            .navigationBarsPadding()
            .padding(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 웹·iOS·화면기획에 이미 있던 버튼이다 — 안드로이드에만 빠져 있었다 (정본 §4)
        OutlinedButton(
            onClick = onOpenRecruitingRooms,
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .testTag("course-detail-open-rooms"),
            border = BorderStroke(1.dp, colors.outline),
            contentPadding = PaddingValues(horizontal = 8.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "모집 중인 모임 보기",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
        Button(
            onClick = onCreateRecruitment,
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .testTag("course-detail-create-recruitment"),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
            contentPadding = PaddingValues(horizontal = 8.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "이 코스로 모집 만들기",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

/** 서버 방문지 → 지도 순번 마커. 좌표가 없는 방문지는 지도에서 빠진다. */
internal fun List<TravelCoursePlace>.toRoutePoints(): List<CourseRoutePoint> = mapNotNull { place ->
    val latitude = place.latitude ?: return@mapNotNull null
    val longitude = place.longitude ?: return@mapNotNull null
    CourseRoutePoint(
        id = "course-place-${place.contentId}-${place.sequence}",
        title = place.title,
        position = MoyeoLatLng(latitude, longitude)
    )
}

/**
 * 화면기획 14의 "2026.05.25 여행 후 공개 · 이 코스로 떠난 모임 3" 보조 문구를 서버값으로 만든다.
 * 서버가 주지 않는 항목은 빼고 이어 붙인다 — 없는 값을 "0" 같은 자리표시자로 채우지 않는다.
 */
private fun serverCoursePublisherMeta(course: TravelCourse): String = listOfNotNull(
    course.creatorTravelStartDate?.takeIf { it.isNotBlank() }?.let { "${it.replace('-', '.')} 여행 후 공개" },
    course.chatRoomCount?.let { "이 코스로 떠난 모임 $it" }
).joinToString(" · ")
