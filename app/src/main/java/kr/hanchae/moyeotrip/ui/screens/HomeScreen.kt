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
import androidx.compose.foundation.layout.Spacer
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
import kr.hanchae.moyeotrip.data.MockTripRepository
import kr.hanchae.moyeotrip.data.TripCourse
import kr.hanchae.moyeotrip.domain.WeatherCoursePolicy
import kr.hanchae.moyeotrip.domain.WeatherHero
import kr.hanchae.moyeotrip.domain.WeatherHeroPolicy
import kr.hanchae.moyeotrip.domain.WeatherHeroState
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
    val weatherSignal = MockTripRepository.currentWeatherSignal
    val recommendedCourses = WeatherCoursePolicy.recommendedCourses(weatherSignal, MockTripRepository.courses)
    val featuredCourse = recommendedCourses.first()
    val hero = WeatherHeroPolicy.heroFor(weatherSignal, featuredCourse)
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
                if (isOnline) {
                    item {
                        HomeHero(hero = hero)
                    }
                    item {
                        HomeSectionHeader(
                            title = "지금 떠나기 좋은 코스",
                            trailing = "더보기 ›",
                            onTrailingClick = onOpenExplore
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(recommendedCourses.take(6)) { course ->
                                HomeMiniCourseCard(
                                    course = course,
                                    selected = course.id == featuredCourse.id,
                                    onClick = { onOpenCourse(course.id) }
                                )
                            }
                        }
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            HomeSectionHeader(title = "인기 코스 TOP 3")
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                homePopularCourses().forEach { ranked ->
                                    PopularCourseRow(
                                        course = ranked,
                                        onClick = { onOpenCourse(ranked.courseId) }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    item { OfflineWeatherPlaceholder() }
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "저장해둔 코스",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Surface(
                                modifier = Modifier.padding(start = 8.dp),
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    "오프라인에서도 열려요",
                                    Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    items(MockTripRepository.courses.take(3)) { course ->
                        SavedOfflineCourseRow(course = course, onClick = { onOpenCourse(course.id) })
                    }
                    item { OfflineRecruitmentPlaceholder() }
                }
            }
            // 오프라인일 때 FAB 위에 떠 있던 안내 캡션은 두지 않는다 — 화면기획 36에는 없고,
            // 카드 밖으로 밀려 FAB과 겹쳐 보였다. 같은 안내는 아래 모집 카드 안에 이미 있다.
            FloatingActionButton(
                onClick = { if (isOnline) onCreateRecruitment(featuredCourse.id) },
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
private fun HomeMiniCourseCard(course: TripCourse, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(146.dp)
            .height(166.dp)
            .testTag("home-course-${course.id}")
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
            ) {
                CourseScenicPanel(course = course, modifier = Modifier.fillMaxSize(), cornerRadius = 0.dp)
                if (selected) {
                    Text(
                        text = "진행중",
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Column(
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = course.homeCardTitle(),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${course.participants}/${course.capacity}명",
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun TripCourse.homeCardTitle(): String = when (id) {
    "cheongsong-juwangsan" -> "주왕산 & 주산지 힐링\n트레킹"
    "andong-hahoe" -> "안동 하회마을 하루\n코스"
    "gyeongju-healing" -> "경주 감성 힐링\n코스"
    "ulleung-island" -> "울릉도 2박 3일\n섬 여행"
    "pohang-sea" -> "포항·영덕 동해\n드라이브"
    "mungyeong-saejae" -> "문경 새재 단풍\n트레킹"
    else -> title
}

@Composable
private fun PopularCourseRow(course: HomePopularCourse, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .testTag("home-popular-${course.rank}")
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
                    text = course.rank.toString(),
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
                    maxLines = 1,
                    modifier = Modifier.testTag("home-popular-${course.rank}-title")
                )
                Text(
                    text = course.subtitle,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
private fun HomeHero(hero: WeatherHero) {
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
                    text = "${hero.weatherLabel} · ${hero.landmark}",
                    state = hero.state,
                    isDark = isDark,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                )
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(hero.tags) { tag ->
                    WeatherTagBubble(
                        text = tag,
                        selected = tag == hero.weatherLabel,
                        state = hero.state,
                        isDark = isDark
                    )
                }
            }
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

@Composable
private fun WeatherTagBubble(text: String, selected: Boolean, state: WeatherHeroState, isDark: Boolean) {
    Text(
        text = text,
        modifier = Modifier
            .background(
                if (selected) state.selectedPillBackground(isDark) else Color.White.copy(alpha = 0.14f),
                RoundedCornerShape(50)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        color = if (selected) state.selectedPillForeground(isDark) else MaterialTheme.colorScheme.onPrimary,
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

private data class HomePopularCourse(val rank: Int, val title: String, val subtitle: String, val courseId: String)

private fun homePopularCourses(): List<HomePopularCourse> = listOf(
    HomePopularCourse(
        rank = 1,
        title = "주왕산 단풍 물길",
        subtitle = "청송 · 자연",
        courseId = "cheongsong-juwangsan"
    ),
    HomePopularCourse(
        rank = 2,
        title = "안동 하회마을 산책",
        subtitle = "안동 · 문화",
        courseId = "andong-hahoe"
    ),
    HomePopularCourse(
        rank = 3,
        title = "울릉도 2박 3일 섬 여행",
        subtitle = "울릉 · 힐링",
        courseId = "ulleung-island"
    )
)

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

/** 저장해둔 코스 한 줄. 저장 시점을 함께 보여 캐시된 내용임을 알린다. */
@Composable
private fun SavedOfflineCourseRow(course: TripCourse, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("home-offline-saved-${course.id}"),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CourseScenicPanel(course = course, modifier = Modifier.size(62.dp), cornerRadius = 10.dp)
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(course.title, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                Text(
                    "${course.region} · ${course.duration} ${course.distance}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "어제 저장됨",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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
