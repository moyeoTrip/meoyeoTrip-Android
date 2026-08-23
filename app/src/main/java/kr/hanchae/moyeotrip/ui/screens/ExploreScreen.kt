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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.hanchae.moyeotrip.data.MockTripRepository
import kr.hanchae.moyeotrip.data.TripCourse
import kr.hanchae.moyeotrip.ui.theme.Coral
import kr.hanchae.moyeotrip.ui.theme.MoyeoTheme

@Composable
fun ExploreScreen(
    onOpenCourse: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onCreateRecruitment: (String) -> Unit,
    startInMap: Boolean = false
) {
    val filters = listOf("전체", "자연", "역사", "체험", "힐링")
    var selectedFilter by rememberSaveable { mutableStateOf(filters.first()) }
    var showingMap by rememberSaveable(startInMap) { mutableStateOf(startInMap) }
    var likedCourseIds by rememberSaveable { mutableStateOf(listOf("cheongsong-juwangsan")) }
    val courses = remember(selectedFilter) {
        webExploreCourses().filter { it.matchesExploreFilter(selectedFilter) }
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
                modifier = Modifier.fillMaxSize(),
                onOpenCourse = onOpenCourse,
                onToggleFavorite = ::toggleFavorite
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
    modifier: Modifier = Modifier,
    onOpenCourse: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    val selectedCourse = courses.first()
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
        SelectedMapCourse(
            course = selectedCourse,
            liked = selectedCourse.id in likedCourseIds,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 18.dp, vertical = 64.dp),
            onClick = { onOpenCourse(selectedCourse.id) },
            onFavoriteClick = { onToggleFavorite(selectedCourse.id) }
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
