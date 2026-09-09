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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kr.hanchae.moyeotrip.data.courses.TravelCourse
import kr.hanchae.moyeotrip.data.rooms.ChatRoomSearchResult
import kr.hanchae.moyeotrip.ui.LocalServerData
import kr.hanchae.moyeotrip.ui.components.CachedRemoteImage
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyState
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyText
import kr.hanchae.moyeotrip.ui.components.MoyeoPlaceholderShape
import kr.hanchae.moyeotrip.ui.components.ServerListState
import kr.hanchae.moyeotrip.ui.components.afterReload

/**
 * 화면기획 12-1 검색 결과 — 정본 `ATTACH-COMPOSER-CANON.md` §3.
 *
 * 결과의 주인공은 **코스**다(R6): `GET travel-courses/search?keyword=` 는 제목에 포함되거나
 * 태그명이 일치하는 공개 코스를 준다. 소개글은 검색 대상이 아니다 — 클라이언트가 다시 거르지 않는다.
 * 두 번째 탭 `모집` 은 기존 `GET chat-rooms/search?keyword=` 로 11 탐색과 같은 카드를 그린다.
 */
@Composable
fun SearchResultsScreen(keyword: String, onBack: () -> Unit, onOpenCourse: (Long) -> Unit, onOpenRoom: (Long) -> Unit) {
    val server = LocalServerData.current
    var tab by rememberSaveable { mutableStateOf(0) }
    var reloadKey by remember { mutableIntStateOf(0) }
    var courses by remember(server, keyword) {
        mutableStateOf<ServerListState<TravelCourse>>(ServerListState.Loading)
    }
    var rooms by remember(server, keyword) {
        mutableStateOf<ServerListState<ChatRoomSearchResult>>(ServerListState.Loading)
    }
    val trimmed = keyword.trim()

    LaunchedEffect(server, trimmed, reloadKey) {
        if (server == null || trimmed.isBlank()) return@LaunchedEffect
        // 두 목록은 각각 갱신한다 — 한쪽이 실패해도 다른 쪽은 보여준다 (TAB-STATE R2)
        courses = courses.afterReload(runCatching { server.courses.searchCourses(trimmed) })
        rooms = rooms.afterReload(runCatching { server.chatRooms.search(keyword = trimmed) })
    }

    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .testTag("search-results-screen")
    ) {
        // 검색어를 계속 보여준다 — 무엇으로 찾은 결과인지 화면에 남아 있어야 한다
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로")
            }
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clickable(onClick = onBack),
                shape = RoundedCornerShape(50),
                color = colors.surfaceVariant,
                border = BorderStroke(1.dp, colors.outline)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Search, null, Modifier.size(16.dp), tint = colors.onSurfaceVariant)
                    Text(
                        text = trimmed,
                        modifier = Modifier.weight(1f).testTag("search-results-keyword"),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            SearchResultTab(
                label = "코스",
                count = (courses as? ServerListState.Loaded)?.items?.size,
                selected = tab == 0,
                testTag = "search-results-tab-course",
                modifier = Modifier.weight(1f)
            ) { tab = 0 }
            SearchResultTab(
                label = "모집",
                count = (rooms as? ServerListState.Loaded)?.items?.size,
                selected = tab == 1,
                testTag = "search-results-tab-room",
                modifier = Modifier.weight(1f)
            ) { tab = 1 }
        }
        HorizontalDivider(color = colors.outline.copy(alpha = .45f))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (server == null) {
                item { MoyeoEmptyState(MoyeoEmptyText.SIGN_IN_SEARCH, testTag = "search-results-signed-out") }
                return@LazyColumn
            }
            if (tab == 0) {
                when (val state = courses) {
                    ServerListState.Loading -> item { MoyeoEmptyState(MoyeoEmptyText.LOADING) }

                    ServerListState.Failed -> item {
                        MoyeoEmptyState(MoyeoEmptyText.FAILED, onRetry = { reloadKey++ })
                    }

                    is ServerListState.Loaded -> if (state.items.isEmpty()) {
                        item {
                            MoyeoEmptyState(
                                MoyeoEmptyText.NO_SEARCH_RESULTS,
                                testTag = "search-results-course-empty"
                            )
                        }
                    } else {
                        items(state.items, key = { it.courseId }) { course ->
                            SearchCourseCard(
                                course = course,
                                onClick = { onOpenCourse(course.courseId) }
                            )
                        }
                    }
                }
            } else {
                when (val state = rooms) {
                    ServerListState.Loading -> item { MoyeoEmptyState(MoyeoEmptyText.LOADING) }

                    ServerListState.Failed -> item {
                        MoyeoEmptyState(MoyeoEmptyText.FAILED, onRetry = { reloadKey++ })
                    }

                    is ServerListState.Loaded -> if (state.items.isEmpty()) {
                        item {
                            MoyeoEmptyState(MoyeoEmptyText.NO_ROOMS, testTag = "search-results-room-empty")
                        }
                    } else {
                        items(state.items, key = { it.roomId }) { room ->
                            SearchResultRoomCard(room = room, onClick = { onOpenRoom(room.roomId) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultTab(
    label: String,
    count: Int?,
    selected: Boolean,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .height(44.dp)
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        // 개수는 응답이 온 뒤에만 붙인다 — 아직 모르는 숫자를 0 으로 적지 않는다
        Text(
            text = listOfNotNull(label, count?.toString()).joinToString(" "),
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) colors.primary else colors.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
        )
        if (selected) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(colors.primary)
            )
        }
    }
}

/**
 * 코스 결과 카드 — `TravelCourseInformationResponse` 가 주는 값만 그린다(R7).
 * 평가가 없으면 별점을 지어내지 않고 `평가 없음` 으로 적는다(R8).
 */
@Composable
private fun SearchCourseCard(course: TravelCourse, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("search-results-course-${course.courseId}"),
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
                modifier = Modifier.size(74.dp).clip(RoundedCornerShape(11.dp)),
                contentScale = ContentScale.Crop,
                fallbackShape = MoyeoPlaceholderShape.SQUARE
            ) {
                Box(Modifier.size(74.dp).clip(RoundedCornerShape(11.dp)).background(colors.surfaceVariant))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = course.title,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val meta = listOfNotNull(
                    course.travelTime,
                    course.distanceKm?.let { "${it}km" },
                    course.places.size.takeIf { it > 0 }?.let { "${it}곳" }
                ).joinToString(" · ")
                if (meta.isNotBlank()) {
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (course.tags.isNotEmpty()) {
                        // 태그는 **표시 전용**이다 — 태그로 모임을 찾는 기능은 없다(2026-08-31 확인)
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // 태그는 **칩**이다 — 기획·웹·iOS 가 외곽선 칩으로 그린다.
                            // 예전에는 `#섬 #자연` 해시태그 글자였고, 작아서 카드가 비어 보였다
                            // (사용자 지적, 2026-09-09).
                            course.tags.take(3).forEach { tag ->
                                Surface(
                                    modifier = Modifier.testTag("search-results-tag-${tag.tagId}"),
                                    shape = RoundedCornerShape(999.dp),
                                    color = colors.surface,
                                    border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = tag.name,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = colors.onSurface,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    } else {
                        Box(Modifier.weight(1f))
                    }
                    Text(
                        text = course.averageRating?.let { "★ $it (${course.ratingCount})" } ?: "평가 없음",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * 모집 결과 카드. 14 코스 상세의 `모집 중인 모임 보기` 목록도 같은 응답(`SearchChatRoomResponse`)이라
 * 같은 카드를 쓴다 — 11 탐색과 같은 값만 그린다.
 */
@Composable
internal fun SearchResultRoomCard(room: ChatRoomSearchResult, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("search-result-room-${room.roomId}"),
        shape = RoundedCornerShape(14.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            CachedRemoteImage(
                url = room.thumbnail,
                contentDescription = room.title,
                modifier = Modifier.size(width = 88.dp, height = 68.dp).clip(RoundedCornerShape(11.dp)),
                contentScale = ContentScale.Crop,
                fallbackShape = MoyeoPlaceholderShape.LANDSCAPE
            ) {
                Box(
                    Modifier
                        .size(width = 88.dp, height = 68.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(colors.surfaceVariant)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = room.title,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                room.tags.joinToString(", ") { it.name }.takeIf(String::isNotBlank)?.let { tags ->
                    Text(
                        text = tags,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${room.participantCount}/${room.maxParticipants}명",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
