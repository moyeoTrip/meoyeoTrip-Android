package kr.hanchae.moyeotrip.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kr.hanchae.moyeotrip.data.FeedPost
import kr.hanchae.moyeotrip.data.FeedVisibility
import kr.hanchae.moyeotrip.data.MockTripRepository
import kr.hanchae.moyeotrip.data.ServerDataDependencies
import kr.hanchae.moyeotrip.data.feed.FeedComment
import kr.hanchae.moyeotrip.data.feed.ServerFeed
import kr.hanchae.moyeotrip.ui.LocalServerData
import kr.hanchae.moyeotrip.ui.components.AnimalAvatar
import kr.hanchae.moyeotrip.ui.components.CachedRemoteImage
import kr.hanchae.moyeotrip.ui.theme.MoyeoTheme

@Composable
fun FeedDetailScreen(postId: String, onBack: () -> Unit, onOpenAllComments: () -> Unit = {}) {
    // "srv-{id}" 는 실서버 피드다 (서버 피드 목록·좋아요 알림에서만 이 형태로 진입한다)
    val server = LocalServerData.current
    val serverFeedId = postId.removePrefix("srv-").toLongOrNull()?.takeIf { postId.startsWith("srv-") }
    if (serverFeedId != null && server != null) {
        ServerFeedDetail(feedId = serverFeedId, server = server, onBack = onBack)
        return
    }
    val post = MockTripRepository.findFeedPost(postId)
    var comment by rememberSaveable { mutableStateOf("") }
    var actionMessage by rememberSaveable(postId) { mutableStateOf<String?>(null) }
    val submittedComments = remember(postId) { mutableStateListOf<String>() }
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            FeedDetailHeader(
                onBack = onBack,
                onOpenMore = {
                    actionMessage = "피드 저장, 공유, 신고 옵션을 확인할 수 있어요."
                }
            )
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("feed-detail-scroll"),
                contentPadding = PaddingValues(start = 18.dp, top = 22.dp, end = 18.dp, bottom = 108.dp)
            ) {
                actionMessage?.let { message ->
                    item {
                        FeedActionBanner(message = message)
                    }
                }
                item {
                    FeedDetailAuthorRow(post = post)
                }
                item {
                    FeedDetailTitleBlock(post = post)
                }
                item {
                    FeedDetailMedia(post = post, modifier = Modifier.padding(top = 22.dp))
                }
                item {
                    Text(
                        text = post.body,
                        modifier = Modifier.padding(top = 22.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onBackground,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                    )
                }
                item {
                    FeedDetailStats(
                        stats = post.detailStats(),
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                item {
                    FeedDetailMetrics(
                        post = post,
                        modifier = Modifier.padding(top = 18.dp),
                        onOpenAllComments = onOpenAllComments
                    )
                }
                if (submittedComments.isNotEmpty()) {
                    items(submittedComments.size) { index ->
                        FeedSubmittedComment(text = submittedComments[index])
                    }
                }
            }
        }
        FeedCommentBar(
            comment = comment,
            onCommentChange = { comment = it },
            onSend = {
                val trimmed = comment.trim()
                if (trimmed.isNotEmpty()) {
                    MockTripRepository.addFeedComment(post.id)
                    submittedComments.add(trimmed)
                    comment = ""
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun FeedDetailHeader(onBack: () -> Unit, onOpenMore: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(colorScheme.background)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로",
                tint = colorScheme.onBackground
            )
        }
        IconButton(onClick = onOpenMore) {
            Icon(
                imageVector = Icons.Filled.MoreHoriz,
                contentDescription = "더보기",
                tint = colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun FeedActionBanner(message: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
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
private fun FeedDetailAuthorRow(post: FeedPost) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        AnimalAvatar(
            emoji = post.avatar,
            modifier = Modifier.size(42.dp),
            container = colorScheme.primaryContainer
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = post.author,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Black,
                color = colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = post.detailTimeLabel(),
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant
            )
        }
        // 공개범위 필은 작성자 행 오른쪽에 둔다 (화면기획 23)
        FeedVisibilityPill(post)
    }
}

@Composable
private fun FeedVisibilityPill(post: FeedPost) {
    FeedVisibilityPill(label = post.visibility.label)
}

/**
 * 화면기획 23의 공개범위 필. 실서버 피드는 `visibility`(PUBLIC·FRIENDS·PRIVATE)를 주는데
 * 라이브에서 이 자리가 비어 있었다 — 같은 필을 서버값으로도 그린다.
 */
@Composable
private fun FeedVisibilityPill(label: String) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(50),
        color = colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MoyeoTheme.tints.softLine)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.People,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FeedDetailTitleBlock(post: FeedPost) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.padding(top = 22.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = post.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = post.detailSubtitle(),
            style = MaterialTheme.typography.labelMedium,
            color = colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FeedDetailMedia(post: FeedPost, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(198.dp)
            .clip(RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
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
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
        Text(
            text = post.photoCountText,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .background(colorScheme.scrim.copy(alpha = 0.55f), CircleShape)
                .padding(horizontal = 7.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = colorScheme.onPrimary
        )
    }
}

@Composable
private fun FeedDetailStats(stats: List<Pair<String, String>>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        stats.forEach { stat ->
            FeedStatTile(
                label = stat.first,
                value = stat.second,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FeedStatTile(label: String, value: String, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                modifier = Modifier.padding(top = 5.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun FeedDetailMetrics(post: FeedPost, modifier: Modifier = Modifier, onOpenAllComments: () -> Unit = {}) {
    val colorScheme = MaterialTheme.colorScheme

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "좋아요 ${post.likes}개",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )
            Text(
                text = "댓글 ${post.comments}개 모두 보기 →",
                modifier = Modifier
                    .clickable(onClick = onOpenAllComments)
                    .testTag("feed-comments-open"),
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 14.dp),
            color = colorScheme.outline.copy(alpha = 0.45f)
        )
    }
}

@Composable
private fun FeedSubmittedComment(text: String) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        shape = RoundedCornerShape(10.dp),
        color = colorScheme.surfaceVariant
    ) {
        Text(
            text = "나: $text",
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurface
        )
    }
}

@Composable
private fun FeedCommentBar(
    comment: String,
    onCommentChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val sendEnabled = comment.isNotBlank()
    val textStyle = MaterialTheme.typography.labelMedium.merge(
        TextStyle(color = colorScheme.onSurface)
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding(),
        color = colorScheme.surface
    ) {
        Column {
            HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.45f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BasicTextField(
                    value = comment,
                    onValueChange = onCommentChange,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("feed-comment-input")
                        .clip(CircleShape)
                        .border(1.dp, colorScheme.outline.copy(alpha = 0.55f), CircleShape)
                        .padding(horizontal = 16.dp),
                    singleLine = true,
                    textStyle = textStyle,
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (comment.isEmpty()) {
                                Text(
                                    text = "댓글을 입력하세요...",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                IconButton(
                    onClick = onSend,
                    enabled = sendEnabled,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("feed-comment-send")
                        .clip(CircleShape)
                        .background(
                            if (sendEnabled) {
                                colorScheme.tertiary
                            } else {
                                colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                            }
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "댓글 보내기",
                        tint = if (sendEnabled) colorScheme.onTertiary else colorScheme.surface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun FeedPost.detailTimeLabel(): String = when {
    id.startsWith("session-feed-") -> "방금"
    id == "feed-1" -> "2시간 전"
    id == "feed-2" -> "5시간 전"
    else -> "어제"
}

private fun FeedPost.detailSubtitle(): String = when (id) {
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

private fun FeedPost.detailStats(): List<Pair<String, String>> {
    val routeStopCount = routeStops().size.coerceAtLeast(1)
    val travelStat = when (id) {
        "feed-1" -> "12.4km" to "4시간 30분"
        "feed-2" -> "8.8km" to "3시간 10분"
        else -> "6.5km" to "2시간 40분"
    }
    return listOf(
        "이동 거리" to travelStat.first,
        "소요 시간" to travelStat.second,
        "방문지" to "${routeStopCount}곳"
    )
}

/**
 * 실서버 피드 상세 (GET feeds/{id} + comments). 댓글 등록은 POST comments,
 * 좋아요는 POST like 다. 서버가 주지 않는 값(이동 거리 통계 등)은 표시하지 않는다.
 */
@Composable
private fun ServerFeedDetail(feedId: Long, server: ServerDataDependencies, onBack: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    var feed by remember(feedId) { mutableStateOf<ServerFeed?>(null) }
    var comments by remember(feedId) { mutableStateOf<List<FeedComment>>(emptyList()) }
    var loadFailed by remember(feedId) { mutableStateOf(false) }
    var comment by rememberSaveable(feedId) { mutableStateOf("") }
    val detailScope = rememberCoroutineScope()

    LaunchedEffect(feedId, server) {
        val loaded = runCatching { server.feeds.feed(feedId) }.getOrNull()
        feed = loaded
        loadFailed = loaded == null
        if (loaded != null) {
            comments = runCatching { server.feeds.comments(feedId) }.getOrNull().orEmpty()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            FeedDetailHeader(onBack = onBack, onOpenMore = {})
            val loadedFeed = feed
            if (loadedFeed == null) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (loadFailed) "피드를 불러오지 못했어요." else "불러오는 중…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("feed-detail-scroll"),
                    contentPadding = PaddingValues(start = 18.dp, top = 22.dp, end = 18.dp, bottom = 108.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            UserAvatar(
                                imageUrl = loadedFeed.author.profileImageUrl,
                                nickname = loadedFeed.author.nickname,
                                modifier = Modifier.size(42.dp),
                                fallbackFontSize = 19.sp
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = loadedFeed.author.nickname,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    text = loadedFeed.createdAt.take(10).replace('-', '.'),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                            // 서버 visibility 를 화면기획 23의 공개범위 필로 그린다
                            serverFeedVisibilityLabel(loadedFeed.visibility)?.let { label ->
                                FeedVisibilityPill(label = label)
                            }
                        }
                    }
                    if (loadedFeed.imageUrls.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .padding(top = 18.dp)
                                    .fillMaxWidth()
                                    .height(190.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                loadedFeed.imageUrls.take(2).forEach { imageUrl ->
                                    CachedRemoteImage(
                                        url = imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier.weight(1f).fillMaxHeight(),
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
                    }
                    item {
                        Text(
                            text = loadedFeed.content,
                            modifier = Modifier.padding(top = 18.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onBackground
                        )
                    }
                    loadedFeed.trip?.courseTitle?.let { courseTitle ->
                        item {
                            Text(
                                text = "🗺 $courseTitle",
                                modifier = Modifier.padding(top = 10.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier.padding(top = 18.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "좋아요 ${loadedFeed.likeCount}",
                                modifier = Modifier
                                    .clickable {
                                        detailScope.launch {
                                            runCatching { server.feeds.toggleLike(feedId) }
                                                .onSuccess { result ->
                                                    feed = feed?.copy(
                                                        liked = result.liked,
                                                        likeCount = result.likeCount
                                                    )
                                                }
                                        }
                                    }
                                    .testTag("feed-server-like"),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (loadedFeed.liked) colorScheme.primary else colorScheme.onSurface,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "댓글 ${loadedFeed.commentCount}",
                                style = MaterialTheme.typography.labelLarge,
                                color = colorScheme.onSurface,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                    items(comments.size) { index ->
                        val entry = comments[index]
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                UserAvatar(
                                    imageUrl = entry.author.profileImageUrl,
                                    nickname = entry.author.nickname,
                                    modifier = Modifier.size(30.dp),
                                    fallbackFontSize = 14.sp
                                )
                                Text(
                                    text = entry.author.nickname,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colorScheme.onSurface
                                )
                            }
                            Text(
                                text = entry.content,
                                modifier = Modifier.padding(top = 6.dp, start = 38.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
        FeedCommentBar(
            comment = comment,
            onCommentChange = { comment = it },
            onSend = {
                val trimmed = comment.trim()
                if (trimmed.isNotEmpty()) {
                    detailScope.launch {
                        runCatching { server.feeds.addComment(feedId, trimmed) }
                            .onSuccess { created ->
                                comments = comments + created
                                feed = feed?.copy(commentCount = (feed?.commentCount ?: 0) + 1)
                                comment = ""
                            }
                    }
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * 서버 피드 공개범위(PUBLIC·FRIENDS·PRIVATE)를 화면기획 문구로 바꾼다.
 * 모르는 값은 null 이다 — 지어낸 문구를 필에 넣지 않는다.
 */
private fun serverFeedVisibilityLabel(visibility: String): String? = when (visibility) {
    "PUBLIC" -> FeedVisibility.Public.label
    "FRIENDS" -> FeedVisibility.Friends.label
    "PRIVATE" -> FeedVisibility.Private.label
    else -> null
}
