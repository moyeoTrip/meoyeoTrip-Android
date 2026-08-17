package kr.hanchae.moyeotrip.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.hanchae.moyeotrip.R
import kr.hanchae.moyeotrip.data.MockTripRepository
import kr.hanchae.moyeotrip.data.TripCourse
import kr.hanchae.moyeotrip.ui.components.InfoPill
import kr.hanchae.moyeotrip.ui.components.SectionHeader
import kr.hanchae.moyeotrip.ui.components.TagRow

@Composable
fun CourseDetailScreen(
    courseId: String,
    onBack: () -> Unit,
    onOpenTrip: (String) -> Unit,
    onCreateRecruitment: (String) -> Unit
) {
    val course = remember(courseId) { MockTripRepository.findCourse(courseId) }
    val colors = MaterialTheme.colorScheme
    var isFavorite by rememberSaveable(courseId) { mutableStateOf(false) }
    var actionMessage by rememberSaveable(courseId) { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 132.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                CourseDetailHeader(
                    isFavorite = isFavorite,
                    onBack = onBack,
                    onShare = {
                        actionMessage = "코스 공유 링크를 복사했어요."
                    },
                    onToggleFavorite = {
                        isFavorite = !isFavorite
                        actionMessage = if (isFavorite) {
                            "찜한 코스에 담았어요."
                        } else {
                            "찜한 코스에서 제외했어요."
                        }
                    }
                )
            }
            actionMessage?.let { message ->
                item {
                    CourseActionBanner(message = message)
                }
            }
            item {
                CourseDetailHero(course = course)
            }
            item {
                SectionHeader(title = "코스 미리보기")
                Spacer(modifier = Modifier.height(10.dp))
                CourseRouteMapPreview(course.stops)
            }
            item {
                SectionHeader(title = "코스 태그")
                Spacer(modifier = Modifier.height(10.dp))
                DetailPanel {
                    TagRow(tags = course.tags)
                }
            }
        }

        CourseDetailBottomActions(
            onCreateRecruitment = { onCreateRecruitment(course.id) },
            onOpenTrip = { onOpenTrip(MockTripRepository.tripIdForCourse(course.id)) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun CourseDetailBottomActions(
    onCreateRecruitment: () -> Unit,
    onOpenTrip: () -> Unit,
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
        OutlinedButton(
            onClick = onCreateRecruitment,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            border = BorderStroke(1.dp, colors.primary),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary),
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
        Button(
            onClick = onOpenTrip,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
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
    }
}

@Composable
private fun CourseDetailHeader(
    isFavorite: Boolean,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
        }
        Text(
            text = "코스 상세",
            style = MaterialTheme.typography.titleLarge,
            color = colors.onBackground
        )
        Row {
            IconButton(onClick = onShare) {
                Icon(imageVector = Icons.Filled.Share, contentDescription = "공유")
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isFavorite) "찜 해제" else "찜"
                )
            }
        }
    }
}

@Composable
private fun CourseActionBanner(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun CourseDetailHero(course: TripCourse) {
    val colors = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()

    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, colors.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(218.dp)
            ) {
                Image(
                    painter = painterResource(id = course.heroImageResId(isDark)),
                    contentDescription = "${course.title} 대표 이미지",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(218.dp),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.48f))
                            )
                        )
                )
                InfoPill(
                    text = course.courseStatusLabel(),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp),
                    container = colors.secondaryContainer,
                    content = colors.secondary
                )
            }
            Column(
                modifier = Modifier.padding(start = 18.dp, end = 18.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    course.tags.take(3).forEach { tag ->
                        InfoPill(text = tag, container = colors.primaryContainer, content = colors.primary)
                    }
                }
                Text(
                    text = course.oneLine,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
                CourseMetricGrid(course = course)
            }
        }
    }
}

@Composable
private fun CourseMetricGrid(course: TripCourse) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CourseMetric(
            info = CourseMetricInfo(
                icon = Icons.Filled.Schedule,
                label = "소요시간",
                value = course.courseTime
            ),
            modifier = Modifier.weight(1f)
        )
        CourseMetric(
            info = CourseMetricInfo(
                icon = Icons.Filled.Route,
                label = "이동거리",
                value = course.distance
            ),
            modifier = Modifier.weight(1f)
        )
        CourseMetric(
            info = CourseMetricInfo(
                icon = Icons.Filled.Star,
                label = "평점",
                value = course.rating.toString()
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CourseMetric(info: CourseMetricInfo, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = info.icon,
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = info.label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant
        )
        Text(
            text = info.value,
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurface,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

private data class CourseMetricInfo(val icon: ImageVector, val label: String, val value: String)

@Composable
private fun DetailPanel(content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme

    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            content = content
        )
    }
}

@Composable
private fun CourseRouteMapPreview(stops: List<String>) {
    val isDark = isSystemInDarkTheme()
    val mapBackground = if (isDark) Color(0xFF16251F) else Color(0xFFDCECE4)
    val roadColor = if (isDark) Color(0xFF31423A) else Color.White.copy(alpha = 0.50f)
    val ridgeColor = if (isDark) Color(0xFF23362E) else Color(0xFFBED8C4)
    val routeColor = if (isDark) Color(0xFF58C98C) else Color(0xFF2D8F5A)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(146.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(mapBackground)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val route = listOf(
                Offset(size.width * 0.16f, size.height * 0.75f),
                Offset(size.width * 0.40f, size.height * 0.52f),
                Offset(size.width * 0.66f, size.height * 0.32f),
                Offset(size.width * 0.86f, size.height * 0.16f)
            )
            drawLine(
                color = roadColor,
                start = Offset(0f, size.height * 0.36f),
                end = Offset(size.width, size.height * 0.22f),
                strokeWidth = 30.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = ridgeColor,
                start = Offset(0f, size.height * 0.62f),
                end = Offset(size.width, size.height * 0.50f),
                strokeWidth = 26.dp.toPx(),
                cap = StrokeCap.Round
            )
            route.zipWithNext().forEach { (start, end) ->
                drawLine(
                    color = routeColor,
                    start = start,
                    end = end,
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
        stops.take(4).forEachIndexed { index, stop ->
            RoutePoint(
                number = index + 1,
                isStart = index == 0,
                label = stop,
                isDark = isDark,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(
                        x = when (index) {
                            0 -> 24.dp
                            1 -> 104.dp
                            2 -> 210.dp
                            else -> 208.dp
                        },
                        y = when (index) {
                            0 -> 98.dp
                            1 -> 70.dp
                            2 -> 58.dp
                            else -> 8.dp
                        }
                    )
            )
        }
    }
}

@Composable
private fun RoutePoint(number: Int, isStart: Boolean, label: String, isDark: Boolean, modifier: Modifier = Modifier) {
    val labelBackground = if (isDark) Color(0xE61D2C26) else Color.White.copy(alpha = 0.88f)
    val labelText = if (isDark) Color(0xFFE8F5ED) else Color(0xFF0F1714)

    Row(
        modifier = modifier
            .background(labelBackground, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isStart) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = labelText,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

private fun TripCourse.heroImageResId(isDark: Boolean): Int = when (id) {
    "cheongsong-juwangsan" -> weatherRes(
        isDark = isDark,
        light = R.drawable.weather_fog_seokguram,
        dark = R.drawable.weather_fog_seokguram_night
    )

    "andong-hahoe" -> weatherRes(
        isDark = isDark,
        light = R.drawable.weather_rain_hahoe,
        dark = R.drawable.weather_rain_hahoe_night
    )

    "gyeongju-healing" -> weatherRes(
        isDark = isDark,
        light = R.drawable.weather_sunny_cheomseongdae,
        dark = R.drawable.weather_sunny_cheomseongdae_night
    )

    "pohang-sea",
    "ulleung-island" -> weatherRes(
        isDark = isDark,
        light = R.drawable.weather_wind_homigot,
        dark = R.drawable.weather_wind_homigot_night
    )

    "mungyeong-saejae" -> weatherRes(
        isDark = isDark,
        light = R.drawable.weather_cloudy_bulguksa,
        dark = R.drawable.weather_cloudy_bulguksa_night
    )

    "yeongju-buseoksa" -> weatherRes(
        isDark = isDark,
        light = R.drawable.weather_snow_buseoksa,
        dark = R.drawable.weather_snow_buseoksa_night
    )

    "andong-dosan" -> weatherRes(
        isDark = isDark,
        light = R.drawable.weather_heatwave_dosan,
        dark = R.drawable.weather_heatwave_dosan_night
    )

    else -> weatherRes(
        isDark = isDark,
        light = R.drawable.weather_sunny_cheomseongdae,
        dark = R.drawable.weather_sunny_cheomseongdae_night
    )
}

private fun weatherRes(isDark: Boolean, light: Int, dark: Int): Int = if (isDark) dark else light

private fun TripCourse.courseStatusLabel(): String {
    val distanceValue = distance.removeSuffix("km").toDoubleOrNull() ?: 0.0
    return when {
        distanceValue < 5.5 -> "느긋한 코스"
        distanceValue < 7.5 -> "알찬 코스"
        else -> "활동적인 코스"
    }
}
