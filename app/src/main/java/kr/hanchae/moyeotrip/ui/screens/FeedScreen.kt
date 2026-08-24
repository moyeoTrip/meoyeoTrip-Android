package kr.hanchae.moyeotrip.ui.screens

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kr.hanchae.moyeotrip.data.FeedPost
import kr.hanchae.moyeotrip.data.FeedVisibility
import kr.hanchae.moyeotrip.data.MockTripRepository
import kr.hanchae.moyeotrip.data.feed.FeedTab
import kr.hanchae.moyeotrip.data.feed.ServerFeed
import kr.hanchae.moyeotrip.ui.LocalServerData
import kr.hanchae.moyeotrip.ui.components.AnimalAvatar
import kr.hanchae.moyeotrip.ui.components.CachedRemoteImage
import kr.hanchae.moyeotrip.ui.theme.Coral
import kr.hanchae.moyeotrip.ui.theme.MoyeoTheme

@Composable
fun FeedScreen(onOpenPost: (String) -> Unit, onWritePost: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    var selectedTab by remember { mutableStateOf(FeedTimelineTab.Discover) }
    val posts = feedTimelinePosts(selectedTab)

    // 로그인 상태면 실서버 피드(GET feeds)로 대체한다 — 비어 있으면 빈 상태 UI
    val server = LocalServerData.current
    var serverFeeds by remember(server) { mutableStateOf<List<ServerFeed>?>(null) }
    val feedScope = rememberCoroutineScope()
    LaunchedEffect(server, selectedTab) {
        serverFeeds = if (server == null) {
            null
        } else {
            runCatching { server.feeds.feeds(selectedTab.serverTab).feeds }.getOrNull()
        }
    }

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
            val feeds = serverFeeds
            if (feeds != null) {
                if (feeds.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 72.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "아직 올라온 피드가 없어요",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = "여행을 다녀오면 첫 피드를 남겨보세요.",
                                fontSize = 13.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(items = feeds, key = { feed -> feed.feedId }) { feed ->
                        ServerFeedPostRow(
                            feed = feed,
                            onOpenPost = { onOpenPost("srv-${feed.feedId}") },
                            onToggleLike = {
                                feedScope.launch {
                                    runCatching { server?.feeds?.toggleLike(feed.feedId) }
                                        .onSuccess { result ->
                                            if (result != null) {
                                                serverFeeds = serverFeeds?.map {
                                                    if (it.feedId == feed.feedId) {
                                                        it.copy(
                                                            liked = result.liked,
                                                            likeCount = result.likeCount
                                                        )
                                                    } else {
                                                        it
                                                    }
                                                }
                                            }
                                        }
                                }
                            }
                        )
                    }
                }
            } else {
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
}

/** 실서버 피드 행 — 서버가 주지 않는 값(제목·지역 태그 줄)은 표시하지 않는다. */
@Composable
private fun ServerFeedPostRow(feed: ServerFeed, onOpenPost: () -> Unit, onToggleLike: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .testTag("feed-server-post-${feed.feedId}")
            .clickable(onClick = onOpenPost)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            UserAvatar(
                imageUrl = feed.author.profileImageUrl,
                nickname = feed.author.nickname,
                modifier = Modifier.size(38.dp),
                fallbackFontSize = 17.sp
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = feed.author.nickname,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = feed.createdAt.take(10).replace('-', '.'),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = feed.content,
            modifier = Modifier.padding(top = 10.dp),
            fontSize = 15.sp,
            lineHeight = 21.sp,
            color = colorScheme.onSurface,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )
        feed.trip?.courseTitle?.let { courseTitle ->
            Text(
                text = "🗺 $courseTitle",
                modifier = Modifier.padding(top = 6.dp),
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (feed.imageUrls.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .height(166.dp)
                    .clip(RoundedCornerShape(8.dp)),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                feed.imageUrls.take(2).forEach { imageUrl ->
                    CachedRemoteImage(
                        url = imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentScale = ContentScale.Crop
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .background(colorScheme.surfaceVariant)
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.clickable(onClick = onToggleLike),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = if (feed.liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "좋아요",
                    tint = if (feed.liked) Coral else colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(21.dp)
                )
                Text(
                    text = feed.likeCount.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurfaceVariant
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ChatBubbleOutline,
                    contentDescription = "댓글",
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(21.dp)
                )
                Text(
                    text = feed.commentCount.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 12.dp, bottom = 12.dp),
            color = colorScheme.outline.copy(alpha = 0.45f)
        )
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
        FeedMediaGrid(
            post = post,
            compactRoute = false,
            modifier = Modifier
                .padding(top = 12.dp)
                .height(166.dp)
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
    // 화면기획의 피드 사진은 사진처럼 채도가 낮은 풍경 팔레트다 (코스 카드 썸네일과 같은 축).
    // colorScheme.primary/tertiary 를 알파로 겹치면 다크에서도 형광 초록·파랑이 되어 라이트 자산처럼 보인다.
    val palette = post.scenicKind().scenicPalette(MoyeoTheme.isDark)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(palette.sky)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val farHill = palette.farHill
            val midHill = palette.midHill
            val nearHill = palette.nearHill
            val foreground = palette.sky.copy(alpha = 0.42f)

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(palette.sky, palette.haze, palette.ground),
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

/** 화면기획의 `Photo hue` 와 같은 축 — 피드 글의 지역으로 풍경 종류를 고른다. */
private fun FeedPost.scenicKind(): PlaceScenicKind = when (region) {
    "안동" -> PlaceScenicKind.Hanok
    "경주" -> PlaceScenicKind.Autumn
    "포항", "울릉", "영덕", "울진" -> PlaceScenicKind.Coast
    else -> PlaceScenicKind.Forest
}

@Composable
internal fun FeedRouteMapPanel(
    stops: List<String>,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 0.dp,
    compact: Boolean = false
) {
    val dark = MoyeoTheme.isDark
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
private fun feedTimelineColor(): Color = if (MoyeoTheme.isDark) {
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

/** 서버 피드 탭 매핑 — 팔로잉은 FRIENDS, 발견은 DISCOVER 다. */
private val FeedTimelineTab.serverTab: FeedTab
    get() = when (this) {
        FeedTimelineTab.Following -> FeedTab.FRIENDS
        FeedTimelineTab.Discover -> FeedTab.DISCOVER
    }

private fun FeedPost.timelineSubtitle(): String = when (id) {
    // 부제는 "장소 · #해시태그" 한 줄이다 (4개 플랫폼 공통, docs/alignment/MOCKDATA-CANON.md).
    // 별도의 태그 칩 줄은 두지 않는다 — 같은 내용을 두 줄로 반복하게 된다.
    "feed-1" -> "청송 · #주왕산 #주산지 #숲길"

    "feed-2" -> "안동 · #한옥산책 #가을여행"

    "feed-3" -> "경주 · #야경 #월정교"

    "feed-4" -> "포항 · #바다 #드라이브"

    "feed-5" -> "문경 · #단풍 #숲길"

    "feed-7" -> "울릉 · #섬여행 #해안산책"

    else -> region
}

private fun FeedPost.timeLabel(): String = when {
    id.startsWith("session-feed-") -> "방금"
    id == "feed-1" -> "2시간 전"
    id == "feed-2" -> "5시간 전"
    id == "feed-3" -> "어제"
    id == "feed-4" -> "2일 전"
    id == "feed-5" -> "3일 전"
    id == "feed-7" -> "2주 전"
    else -> "어제"
}
