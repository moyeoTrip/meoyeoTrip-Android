package kr.hanchae.moyeotrip.ui.screens

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.hanchae.moyeotrip.data.FeedPost
import kr.hanchae.moyeotrip.data.FeedVisibility
import kr.hanchae.moyeotrip.data.MockTripRepository
import kr.hanchae.moyeotrip.ui.components.AnimalAvatar

@Composable
fun FeedScreen(onOpenPost: (String) -> Unit, onWritePost: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    var selectedTab by remember { mutableStateOf(FeedTimelineTab.Discover) }
    val posts = feedTimelinePosts(selectedTab)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        FeedTabBar(
            selectedTab = selectedTab,
            onSelectTab = { selectedTab = it }
        )
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(feedTimelineColor()),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 8.dp,
                end = 16.dp,
                bottom = 128.dp
            )
        ) {
            items(
                items = posts,
                key = { post -> post.id }
            ) { post ->
                FeedTimelinePost(
                    post = post,
                    onOpenPost = { onOpenPost(post.id) },
                    onWritePost = onWritePost
                )
            }
        }
    }
}

@Composable
private fun FeedTabBar(selectedTab: FeedTimelineTab, onSelectTab: (FeedTimelineTab) -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val tabs = FeedTimelineTab.entries

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.spacedBy(36.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val selected = tab == selectedTab
                Column(
                    modifier = Modifier
                        .width(76.dp)
                        .height(48.dp)
                        .testTag("feed-tab-${tab.key}")
                        .clickable { onSelectTab(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.label,
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = if (selected) colorScheme.onBackground else colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(if (selected) colorScheme.primary else Color.Transparent)
                    )
                }
            }
        }
        HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.45f))
    }
}

@Composable
private fun FeedTimelinePost(post: FeedPost, onOpenPost: () -> Unit, onWritePost: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .testTag("feed-post-${post.id}")
            .clickable(onClick = onOpenPost)
    ) {
        FeedPostAuthorRow(post = post)
        Text(
            text = post.title,
            modifier = Modifier
                .padding(top = 10.dp)
                .testTag("feed-post-${post.id}-title"),
            fontSize = 17.sp,
            lineHeight = 23.sp,
            fontWeight = FontWeight.Black,
            color = colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = post.timelineSubtitle(),
            modifier = Modifier
                .padding(top = 6.dp)
                .testTag("feed-post-${post.id}-subtitle"),
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        FeedTagRow(
            tags = post.timelineTags(),
            testTagPrefix = "feed-post-${post.id}",
            modifier = Modifier.padding(top = 10.dp)
        )
        FeedMediaGrid(
            post = post,
            compactRoute = false,
            modifier = Modifier
                .padding(top = 12.dp)
                .height(150.dp)
        )
        FeedActionRow(
            post = post,
            modifier = Modifier.padding(top = 12.dp),
            onWritePost = onWritePost
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = 12.dp, bottom = 12.dp),
            color = colorScheme.outline.copy(alpha = 0.45f)
        )
    }
}

@Composable
private fun FeedPostAuthorRow(post: FeedPost) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AnimalAvatar(
            emoji = post.avatar,
            modifier = Modifier.size(38.dp),
            container = colorScheme.primaryContainer
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = post.author,
                modifier = Modifier.testTag("feed-post-${post.id}-author"),
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Black,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = post.timeLabel(),
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Filled.MoreHoriz,
            contentDescription = "더보기",
            tint = colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun FeedTagRow(tags: List<String>, modifier: Modifier = Modifier, testTagPrefix: String? = null) {
    val colorScheme = MaterialTheme.colorScheme
    val rowTag = testTagPrefix?.let { "$it-tags" } ?: "feed-tags"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag(rowTag),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tags.take(3).forEachIndexed { index, tag ->
            Text(
                text = tag,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.Black,
                color = colorScheme.primary,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colorScheme.primaryContainer)
                    .testTag(testTagPrefix?.let { "$it-tag-$index" } ?: "feed-tag-$index")
                    .padding(horizontal = 9.dp, vertical = 5.dp)
            )
        }
    }
}

@Composable
private fun FeedActionRow(post: FeedPost, modifier: Modifier = Modifier, onWritePost: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FeedMetric(
            icon = {
                Icon(
                    imageVector = Icons.Filled.FavoriteBorder,
                    contentDescription = "좋아요",
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(21.dp)
                )
            },
            value = post.likes.toString()
        )
        FeedMetric(
            icon = {
                Icon(
                    imageVector = Icons.Filled.ChatBubbleOutline,
                    contentDescription = "댓글",
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(21.dp)
                )
            },
            value = post.comments.toString(),
            modifier = Modifier.testTag("feed-post-${post.id}-comments")
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = onWritePost,
            modifier = Modifier
                .size(44.dp)
                .testTag("feed-write-action-${post.id}")
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .border(1.dp, colorScheme.outline.copy(alpha = 0.55f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "피드 작성",
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun FeedMetric(icon: @Composable () -> Unit, value: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        icon()
        Text(
            text = value,
            modifier = modifier,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun FeedMediaGrid(post: FeedPost, modifier: Modifier = Modifier, compactRoute: Boolean = false) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        FeedPhotoPanel(
            post = post,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
        FeedRouteMapPanel(
            stops = post.routeStops(),
            compact = compactRoute,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
    }
}

@Composable
internal fun FeedPhotoPanel(post: FeedPost, modifier: Modifier = Modifier, cornerRadius: Dp = 0.dp) {
    val colorScheme = MaterialTheme.colorScheme
    val scenicTint = when (post.region) {
        "경주" -> colorScheme.secondary
        "안동" -> colorScheme.tertiary
        else -> colorScheme.primary
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(colorScheme.surfaceVariant)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sky = colorScheme.primaryContainer.copy(alpha = 0.82f)
            val water = scenicTint.copy(alpha = 0.24f)
            val farHill = scenicTint.copy(alpha = 0.30f)
            val midHill = colorScheme.primary.copy(alpha = 0.44f)
            val nearHill = colorScheme.primary.copy(alpha = 0.58f)
            val foreground = colorScheme.onSurface.copy(alpha = 0.28f)

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(sky, water),
                    startY = 0f,
                    endY = size.height
                )
            )

            val farPath = Path().apply {
                moveTo(0f, size.height * 0.58f)
                lineTo(size.width * 0.18f, size.height * 0.38f)
                lineTo(size.width * 0.36f, size.height * 0.49f)
                lineTo(size.width * 0.58f, size.height * 0.33f)
                lineTo(size.width * 0.82f, size.height * 0.51f)
                lineTo(size.width, size.height * 0.40f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(farPath, farHill)

            val midPath = Path().apply {
                moveTo(0f, size.height * 0.70f)
                lineTo(size.width * 0.18f, size.height * 0.54f)
                lineTo(size.width * 0.39f, size.height * 0.65f)
                lineTo(size.width * 0.64f, size.height * 0.49f)
                lineTo(size.width * 0.88f, size.height * 0.64f)
                lineTo(size.width, size.height * 0.54f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(midPath, midHill)

            val nearPath = Path().apply {
                moveTo(0f, size.height * 0.77f)
                quadraticTo(size.width * 0.22f, size.height * 0.64f, size.width * 0.44f, size.height * 0.75f)
                quadraticTo(size.width * 0.68f, size.height * 0.88f, size.width, size.height * 0.68f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(nearPath, nearHill)

            val treeXs = listOf(0.12f, 0.25f, 0.38f, 0.56f, 0.72f, 0.88f)
            treeXs.forEachIndexed { index, xRatio ->
                val treeHeight = size.height * (0.10f + (index % 3) * 0.02f)
                val x = size.width * xRatio
                val y = size.height * (0.78f + (index % 2) * 0.04f)
                val tree = Path().apply {
                    moveTo(x, y - treeHeight)
                    lineTo(x - treeHeight * 0.28f, y)
                    lineTo(x + treeHeight * 0.28f, y)
                    close()
                }
                drawPath(tree, foreground)
            }
        }
    }
}

@Composable
internal fun FeedRouteMapPanel(
    stops: List<String>,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 0.dp,
    compact: Boolean = false
) {
    val dark = isSystemInDarkTheme()
    val mapBase = if (dark) Color(0xFF14231A) else Color(0xFFEAF3E6)
    val landLine = if (dark) Color(0xFF2C3C34) else Color(0xFFD7E1D8)
    val routeColor = Color(0xFF2D8F5A)
    val pinColor = Color(0xFF2D8F5A)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(mapBase)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(mapBase)

            repeat(5) { index ->
                val y = size.height * (0.18f + index * 0.16f)
                drawLine(
                    color = landLine,
                    start = Offset(size.width * 0.08f, y),
                    end = Offset(size.width * 0.92f, y + (if (index % 2 == 0) 12f else -8f)),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val routePath = Path().apply {
                moveTo(size.width * 0.18f, size.height * 0.70f)
                cubicTo(
                    size.width * 0.26f,
                    size.height * 0.30f,
                    size.width * 0.54f,
                    size.height * 0.86f,
                    size.width * 0.64f,
                    size.height * 0.42f
                )
                cubicTo(
                    size.width * 0.70f,
                    size.height * 0.20f,
                    size.width * 0.88f,
                    size.height * 0.38f,
                    size.width * 0.82f,
                    size.height * 0.22f
                )
            }
            drawPath(
                path = routePath,
                color = routeColor,
                style = Stroke(width = if (compact) 3.dp.toPx() else 4.dp.toPx(), cap = StrokeCap.Round)
            )

            val points = listOf(
                Offset(size.width * 0.18f, size.height * 0.70f),
                Offset(size.width * 0.44f, size.height * 0.58f),
                Offset(size.width * 0.64f, size.height * 0.42f),
                Offset(size.width * 0.82f, size.height * 0.22f)
            )
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.White.toArgb()
                textAlign = Paint.Align.CENTER
                textSize = if (compact) 7.dp.toPx() else 9.dp.toPx()
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            points.take(if (compact) 3 else minOf(4, stops.size.coerceAtLeast(1))).forEachIndexed { index, point ->
                val radius = if (compact) 6.dp.toPx() else 8.dp.toPx()
                drawCircle(
                    color = pinColor,
                    radius = radius,
                    center = point
                )
                drawCircle(
                    color = mapBase,
                    radius = radius,
                    center = point,
                    style = Stroke(width = 1.6.dp.toPx())
                )
                drawContext.canvas.nativeCanvas.drawText(
                    (index + 1).toString(),
                    point.x,
                    point.y + textPaint.textSize * 0.36f,
                    textPaint
                )
            }
        }
    }
}

internal fun FeedPost.routeStops(): List<String> = routeSummary.split(" · ").filter { it.isNotBlank() }

@Composable
private fun feedTimelineColor(): Color = if (isSystemInDarkTheme()) {
    MaterialTheme.colorScheme.background
} else {
    MaterialTheme.colorScheme.surface
}

private fun feedTimelinePosts(tab: FeedTimelineTab): List<FeedPost> = when (tab) {
    FeedTimelineTab.Following ->
        MockTripRepository.feedPosts
            .filter { it.visibility == FeedVisibility.Friends }

    FeedTimelineTab.Discover ->
        MockTripRepository.feedPosts
            .filter { it.visibility != FeedVisibility.Private }
}

private enum class FeedTimelineTab(val label: String, val key: String) {
    Following("팔로잉", "following"),
    Discover("발견", "discover")
}

private fun FeedPost.timelineSubtitle(): String = when (id) {
    "feed-1" -> "청송 · 방금 다녀온 숲길 기록"
    "feed-2" -> "#한옥산책 #가을여행"
    "feed-3" -> "#경주 #야경 #월정교"
    "feed-4" -> "#포항 #바다 #드라이브"
    "feed-5" -> "#문경 #단풍 #숲길"
    "feed-7" -> "#울릉 #섬여행 #해안산책"
    else -> region
}

private fun FeedPost.timelineTags(): List<String> = when (id) {
    "feed-1" -> listOf("경로지도", "주왕산", "청송")

    "feed-2" -> listOf("하회마을", "경로지도", "안동")

    "feed-3" -> listOf("경주", "야경", "월정교")

    "feed-4" -> listOf("포항", "바다", "드라이브")

    "feed-5" -> listOf("문경", "단풍", "숲길")

    "feed-7" -> listOf("울릉", "섬여행", "해안산책")

    else -> buildList {
        add(region)
        routeStops().take(2).forEach { stop ->
            if (stop !in this) {
                add(stop)
            }
        }
    }
}

private fun FeedPost.timeLabel(): String = when {
    id.startsWith("session-feed-") -> "방금"
    id == "feed-1" -> "2시간 전"
    id == "feed-2" -> "5시간 전"
    else -> "어제"
}
