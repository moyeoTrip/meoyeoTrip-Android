package kr.hanchae.moyeotrip.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.hanchae.moyeotrip.R
import kr.hanchae.moyeotrip.data.courses.TravelCourse
import kr.hanchae.moyeotrip.domain.WeatherHero
import kr.hanchae.moyeotrip.domain.WeatherHeroPolicy
import kr.hanchae.moyeotrip.domain.WeatherHeroState
import kr.hanchae.moyeotrip.ui.LocalServerData
import kr.hanchae.moyeotrip.ui.components.CachedRemoteImage
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyState
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyText
import kr.hanchae.moyeotrip.ui.components.MoyeoPlaceholderShape
import kr.hanchae.moyeotrip.ui.components.ServerListState
import kr.hanchae.moyeotrip.ui.components.afterReload
import kr.hanchae.moyeotrip.ui.state.LocalTabDataStore
import kr.hanchae.moyeotrip.ui.theme.ForestGreen
import kr.hanchae.moyeotrip.ui.theme.MoyeoTheme

@Composable
fun HomeScreen(
    onOpenCourse: (String) -> Unit,
    onOpenExplore: () -> Unit,
    onOpenNotifications: () -> Unit,
    onCreateRecruitment: (String) -> Unit,
    isOnline: Boolean = true
) {
    val server = LocalServerData.current
    // 히어로 날씨는 서버가 정한다(GET weather/gyeongbuk). 값이 없으면 히어로를 그리지 않는다 —
    // "맑음" 같은 기본값을 앱이 정해 버리면 화면이 오늘 날씨를 아는 척하게 된다.
    //
    // 데이터는 탭 바깥 보관소에 둔다. 화면 안에 들면 탭을 떠날 때 사라져 돌아올 때마다
    // 로딩 문구와 기본 썸네일이 다시 보인다(정본 R1).
    val home = LocalTabDataStore.current.home
    val weather = home.weather
    val recommended = home.recommended
    val popular = home.popular
    LaunchedEffect(server, home.reloadKey) {
        if (server == null) {
            home.weather = null
            home.recommended = ServerListState.Loaded(emptyList())
            home.popular = ServerListState.Loaded(emptyList())
            return@LaunchedEffect
        }
        // 로딩 문구는 아직 아무것도 못 받아 봤을 때만 띄운다(정본 R2).
        if (!home.loaded) {
            home.recommended = ServerListState.Loading
            home.popular = ServerListState.Loading
        }
        // 홈은 재진입할 때마다 갱신하되 **가진 것을 보여주며** 뒤에서 바꿔 끼운다 —
        // 갱신 중 로딩으로 되돌리거나 실패로 덮지 않는다(정본 R2·R3).
        home.weather = runCatching { server.weather.gyeongbuk() }.getOrNull() ?: home.weather
        home.recommended = home.recommended.afterReload(runCatching { server.courses.publicCourses() })
        home.popular = home.popular.afterReload(runCatching { server.courses.popularCourses() })
        // 성공해서 보여줄 게 생겼을 때만 기록한다 — 실패하면 다음 진입에서 다시 로딩부터 시작한다(정본 R3-1).
        if (home.recommended is ServerListState.Loaded || home.popular is ServerListState.Loaded) {
            home.markLoaded()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HomeHeader(
            onOpenNotifications = onOpenNotifications,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 18.dp)
        )
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("home.scroll"),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 18.dp,
                    top = 6.dp,
                    end = 18.dp,
                    bottom = 92.dp
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                if (!isOnline) {
                    item { OfflineWeatherPlaceholder() }
                    // 화면기획 36 · iOS 와 같은 순서로 "저장해둔 코스" 섹션이 날씨와 모집 카드 사이에 온다.
                    // 안드로이드에는 아직 코스를 기기에 담아 두는 저장소가 없다 —
                    // 없는 코스를 지어내지 않고 비어 있다는 사실을 그대로 그린다(NO-MOCK-CANON).
                    item { OfflineSavedCoursesSection() }
                    item { OfflineRecruitmentPlaceholder() }
                    return@LazyColumn
                }
                weather?.let { current ->
                    item {
                        HomeHero(
                            hero = WeatherHeroPolicy.heroFor(current.signal),
                            locationName = current.locationName
                        )
                    }
                }
                item {
                    HomeSectionHeader(
                        title = "지금 떠나기 좋은 코스",
                        trailing = "더보기 ›",
                        onTrailingClick = onOpenExplore
                    )
                }
                item {
                    HomeCourseStrip(
                        state = recommended,
                        signedIn = server != null,
                        onOpenCourse = onOpenCourse,
                        onRetry = home::reload
                    )
                }
                item { HomeSectionHeader(title = "인기 코스 TOP 3") }
                item {
                    HomePopularCourses(
                        state = popular,
                        signedIn = server != null,
                        onOpenCourse = onOpenCourse,
                        onRetry = home::reload
                    )
                }
            }
            FloatingActionButton(
                onClick = { if (isOnline) onCreateRecruitment(NEW_RECRUITMENT_COURSE_KEY) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 14.dp)
                    .size(54.dp)
                    .alpha(if (isOnline) 1f else .45f)
                    .semantics { if (!isOnline) disabled() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = if (isOnline) "모집 만들기" else "연결되면 모집을 만들 수 있어요",
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

/** 코스를 고르지 않고 모집 만들기로 들어갈 때 쓰는 초안 키. 코스는 17-1 에서 서버 목록으로 고른다. */
const val NEW_RECRUITMENT_COURSE_KEY = "new"

@Composable
private fun HomeCourseStrip(
    state: ServerListState<TravelCourse>,
    signedIn: Boolean,
    onOpenCourse: (String) -> Unit,
    onRetry: () -> Unit
) {
    when {
        !signedIn -> MoyeoEmptyState(MoyeoEmptyText.SIGN_IN_EXPLORE, testTag = "home-courses-signed-out")

        state is ServerListState.Loading -> MoyeoEmptyState(MoyeoEmptyText.LOADING)

        state is ServerListState.Failed -> MoyeoEmptyState(MoyeoEmptyText.FAILED, onRetry = onRetry)

        state is ServerListState.Loaded && state.items.isEmpty() ->
            MoyeoEmptyState("아직 공개된 코스가 없어요.", testTag = "home-courses-empty")

        state is ServerListState.Loaded -> LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.items.take(6), key = { it.courseId }) { course ->
                HomeMiniCourseCard(course = course, onClick = { onOpenCourse("srv-${course.courseId}") })
            }
        }
    }
}

@Composable
private fun HomePopularCourses(
    state: ServerListState<TravelCourse>,
    signedIn: Boolean,
    onOpenCourse: (String) -> Unit,
    onRetry: () -> Unit
) {
    when {
        !signedIn -> MoyeoEmptyState(MoyeoEmptyText.SIGN_IN_EXPLORE, testTag = "home-popular-signed-out")

        state is ServerListState.Loading -> MoyeoEmptyState(MoyeoEmptyText.LOADING)

        state is ServerListState.Failed -> MoyeoEmptyState(MoyeoEmptyText.FAILED, onRetry = onRetry)

        state is ServerListState.Loaded && state.items.isEmpty() ->
            MoyeoEmptyState("아직 인기 코스가 없어요.", testTag = "home-popular-empty")

        state is ServerListState.Loaded -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            state.items.take(3).forEachIndexed { index, course ->
                ServerPopularCourseRow(
                    rank = index + 1,
                    course = course,
                    onClick = { onOpenCourse("srv-${course.courseId}") }
                )
            }
        }
    }
}

@Composable
private fun HomeHeader(onOpenNotifications: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "모여트립 in 경북",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(
                onClick = onOpenNotifications,
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.NotificationsNone,
                    contentDescription = "알림",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun HomeSectionHeader(title: String, trailing: String? = null, onTrailingClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.ExtraBold
        )
        if (trailing != null) {
            Text(
                text = trailing,
                modifier = if (onTrailingClick != null) {
                    Modifier.clickable(onClick = onTrailingClick)
                } else {
                    Modifier
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HomeMiniCourseCard(course: TravelCourse, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(146.dp)
            .height(166.dp)
            .testTag("home-course-${course.courseId}")
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            CachedRemoteImage(
                url = course.thumbnail,
                contentDescription = course.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp),
                contentScale = ContentScale.Crop,
                fallbackShape = MoyeoPlaceholderShape.LANDSCAPE
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
            Column(
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = course.title,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "방문지 ${course.places.size}곳",
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/** 실서버 인기 코스 행 — 부제는 코스 소개(없으면 소요 시간·거리)다. */
@Composable
private fun ServerPopularCourseRow(rank: Int, course: TravelCourse, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .testTag("home-server-course-${course.courseId}")
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp, top = 14.dp, end = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rank.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = ForestGreen,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = course.title,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                val subtitle = course.description ?: listOfNotNull(
                    course.travelTime,
                    course.distanceKm?.let { "${it}km" }
                ).joinToString(" · ")
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HomeHero(hero: WeatherHero, locationName: String?) {
    val isDark = MoyeoTheme.isDark
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = hero.state.cardColor(isDark)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(13.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f, fill = true),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = hero.title.replace("\n", " "),
                        fontSize = 17.sp,
                        lineHeight = 23.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = hero.subtitle,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                    )
                }
                TextBubble(
                    text = hero.stateLabel,
                    state = hero.state,
                    isDark = isDark,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = hero.imageResId(isDark = isDark)),
                    contentDescription = "${hero.landmark} 날씨 히어로 이미지",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(144.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    // 화면기획·웹과 같이 아래쪽 기준으로 자른다
                    alignment = Alignment.BottomCenter,
                    contentScale = ContentScale.Crop
                )
                TextBubble(
                    // 지역명은 서버가 준 조회 지점이다. 없으면 날씨만 남긴다 — 지명을 지어내지 않는다.
                    text = listOfNotNull(hero.weatherLabel, locationName).joinToString(" · "),
                    state = hero.state,
                    isDark = isDark,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                )
            }
            // 9가지 날씨를 늘어놓던 칩 줄은 뺐다. 화면기획의 그 줄은 시안을 넘겨보기 위한
            // 프로토타입 조작 장치이고 제품 기능이 아니다.
            // 현재 날씨는 히어로 이미지 위의 라벨이 이미 보여준다.
        }
    }
}

@Composable
private fun TextBubble(text: String, state: WeatherHeroState, isDark: Boolean, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier
            .background(state.selectedPillBackground(isDark), RoundedCornerShape(50))
            .padding(horizontal = 9.dp, vertical = 5.dp),
        color = state.selectedPillForeground(isDark),
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
}

private fun WeatherHeroState.cardColor(isDark: Boolean): Color = when (this) {
    WeatherHeroState.Good -> if (isDark) Color(0xFF174C37) else ForestGreen
    WeatherHeroState.Caution -> if (isDark) Color(0xFF65411B) else Color(0xFFB87726)
    WeatherHeroState.Blocked -> if (isDark) Color(0xFF243E43) else Color(0xFF355D5C)
}

private fun WeatherHeroState.selectedPillBackground(isDark: Boolean): Color = when (this) {
    WeatherHeroState.Good -> if (isDark) Color(0xC707110E) else Color.White.copy(alpha = 0.92f)
    WeatherHeroState.Caution -> if (isDark) Color(0xD1261A0B) else Color(0xFFFFF7E8)
    WeatherHeroState.Blocked -> if (isDark) Color(0xD1081618) else Color(0xFFEEF6F4)
}

private fun WeatherHeroState.selectedPillForeground(isDark: Boolean): Color = when (this) {
    WeatherHeroState.Good -> if (isDark) Color(0xFFDCEFE3) else Color(0xFF155735)
    WeatherHeroState.Caution -> if (isDark) Color(0xFFFFE3B2) else Color(0xFF87530D)
    WeatherHeroState.Blocked -> if (isDark) Color(0xFFD7EFEB) else Color(0xFF254C4B)
}

private fun WeatherHero.imageResId(isDark: Boolean): Int = when (imageResourceName(isDark)) {
    "weather_sunny_cheomseongdae" -> R.drawable.weather_sunny_cheomseongdae
    "weather_sunny_cheomseongdae_night" -> R.drawable.weather_sunny_cheomseongdae_night
    "weather_cloudy_bulguksa" -> R.drawable.weather_cloudy_bulguksa
    "weather_cloudy_bulguksa_night" -> R.drawable.weather_cloudy_bulguksa_night
    "weather_rain_hahoe" -> R.drawable.weather_rain_hahoe
    "weather_rain_hahoe_night" -> R.drawable.weather_rain_hahoe_night
    "weather_snow_buseoksa" -> R.drawable.weather_snow_buseoksa
    "weather_snow_buseoksa_night" -> R.drawable.weather_snow_buseoksa_night
    "weather_fog_seokguram" -> R.drawable.weather_fog_seokguram
    "weather_fog_seokguram_night" -> R.drawable.weather_fog_seokguram_night
    "weather_wind_homigot" -> R.drawable.weather_wind_homigot
    "weather_wind_homigot_night" -> R.drawable.weather_wind_homigot_night
    "weather_heavy_rain_woljeonggyo" -> R.drawable.weather_heavy_rain_woljeonggyo
    "weather_heavy_rain_woljeonggyo_night" -> R.drawable.weather_heavy_rain_woljeonggyo_night
    "weather_heatwave_dosan" -> R.drawable.weather_heatwave_dosan
    "weather_heatwave_dosan_night" -> R.drawable.weather_heatwave_dosan_night
    "weather_dust_donggung_wolji" -> R.drawable.weather_dust_donggung_wolji
    "weather_dust_donggung_wolji_night" -> R.drawable.weather_dust_donggung_wolji_night
    else -> R.drawable.weather_heavy_rain_woljeonggyo
}

/** 오프라인에서는 오늘의 날씨와 추천을 만들 수 없다 — 자리만 알려준다 (화면기획). */
@Composable
private fun OfflineWeatherPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(vertical = 26.dp, horizontal = 18.dp)
            .testTag("home-offline-weather-placeholder"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.WbSunny,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "오늘의 날씨와 추천 코스",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            "연결되면 오늘 경북 날씨에 맞는 코스를 보여드릴게요",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 36 오프라인(캐시 있음) 홈의 "저장해둔 코스" 섹션 (화면기획 · iOS 와 같은 자리).
 *
 * 안드로이드에는 코스를 기기에 담아 두는 저장소가 아직 없다. 그래서 목록은 늘 비어 있고,
 * 그 사실을 빈 상태로 그대로 그린다 — 예시 코스를 끼워 넣지 않는다(NO-MOCK-CANON).
 */
@Composable
private fun OfflineSavedCoursesSection() {
    Column(
        modifier = Modifier.fillMaxWidth().testTag("home-offline-saved-courses"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "저장해둔 코스",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold
            )
            Surface(
                modifier = Modifier.padding(start = 8.dp),
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    "오프라인에서도 열려요",
                    Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        MoyeoEmptyState(
            text = MoyeoEmptyText.NO_SAVED_COURSES,
            testTag = "home-offline-saved-courses-empty"
        )
    }
}

/** 연결이 필요한 동작은 눌릴 수 없다는 것을 카드 안에서 알려준다 (화면기획). */
@Composable
private fun OfflineRecruitmentPlaceholder() {
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("home-offline-recruitment-placeholder"),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.People,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "모집 신청 · 새 모집 만들기",
                    Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Text(
                "연결된 뒤에 할 수 있어요. 지금 누르면 저장해뒀다가 연결되면 이어서 진행해요.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    "연결되면 신청할 수 있어요",
                    Modifier.fillMaxWidth().padding(vertical = 14.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
