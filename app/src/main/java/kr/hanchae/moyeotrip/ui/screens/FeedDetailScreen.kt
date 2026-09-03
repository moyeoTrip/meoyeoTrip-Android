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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kr.hanchae.moyeotrip.data.FeedVisibility
import kr.hanchae.moyeotrip.data.ServerDataDependencies
import kr.hanchae.moyeotrip.data.feed.FeedComment
import kr.hanchae.moyeotrip.data.feed.ServerFeed
import kr.hanchae.moyeotrip.ui.LocalServerData
import kr.hanchae.moyeotrip.ui.components.CachedRemoteImage
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyState
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyText
import kr.hanchae.moyeotrip.ui.components.MoyeoPlaceholderShape
import kr.hanchae.moyeotrip.ui.theme.MoyeoTheme

/**
 * 화면기획 23 피드 상세 — 서버 피드(GET feeds/{id})만 그린다.
 * 라우트 식별자는 `srv-{feedId}` 다. 그 형태가 아니거나 미로그인이면 빈 상태다.
 *
 * [onOpenReport] 는 30-2 피드 신고 시트로 가는 **유일한 진입점**이다 — 서버가 접수하는 신고는
 * 피드뿐인데(`POST feeds/{id}/reports`) 여기 진입점이 없으면 기능이 죽는다(정본 §2).
 * 자기 피드는 서버가 신고를 거절하므로(실서버 400 `40039`) 버튼을 아예 두지 않는다.
 */
@Composable
fun FeedDetailScreen(
    postId: String,
    onBack: () -> Unit,
    onOpenAllComments: () -> Unit = {},
    onOpenReport: (Long) -> Unit = {}
) {
    val server = LocalServerData.current
    val serverFeedId = postId.removePrefix("srv-").toLongOrNull()?.takeIf { postId.startsWith("srv-") }
    if (serverFeedId == null || server == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            FeedDetailHeader(onBack = onBack, onOpenReport = null)
            MoyeoEmptyState(MoyeoEmptyText.NO_FEEDS, testTag = "feed-detail-empty")
        }
        return
    }
    ServerFeedDetail(
        feedId = serverFeedId,
        server = server,
        onBack = onBack,
        onOpenAllComments = onOpenAllComments,
        onOpenReport = onOpenReport
    )
}

@Composable
private fun FeedDetailHeader(onBack: () -> Unit, onOpenReport: (() -> Unit)?) {
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
        // 화면기획 23 의 ⋯ 자리. 예전에는 빈 onClick 이라 눌러도 아무 일도 없었다 —
        // 30-2 피드 신고로 잇는다. 신고할 수 없는 피드(자기 피드·빈 상태)에는 두지 않는다.
        if (onOpenReport != null) {
            IconButton(onClick = onOpenReport, modifier = Modifier.testTag("feed-detail-report")) {
                Icon(
                    imageVector = Icons.Filled.MoreHoriz,
                    contentDescription = "신고",
                    tint = colorScheme.onBackground
                )
            }
        }
    }
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

/**
 * 23 피드 상세가 미리보기로 보여주는 댓글 수. 세 플랫폼이 3 으로 맞춘 값이다
 * (PDF-REVIEW-2026-08-31 §F3 · 웹 `FEED_DETAIL_COMMENT_PREVIEW`).
 * 나머지는 `댓글 … 모두 보기` 로 들어가는 23-1 이 무한 스크롤로 받는다.
 */
private const val FEED_DETAIL_COMMENT_PREVIEW = 3

/**
 * 실서버 피드 상세 (GET feeds/{id} + comments). 댓글 등록은 POST comments,
 * 좋아요는 POST like 다. 서버가 주지 않는 값(이동 거리 통계 등)은 표시하지 않는다.
 */
@Composable
private fun ServerFeedDetail(
    feedId: Long,
    server: ServerDataDependencies,
    onBack: () -> Unit,
    onOpenAllComments: () -> Unit,
    onOpenReport: (Long) -> Unit
) {
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
            // 23 은 미리보기 3건만 그린다 — 자르는 것은 서버 커서 `limit` 이고 클라가 잘라내지
            // 않는다(웹 `FEED_DETAIL_COMMENT_PREVIEW` 와 같은 값·같은 방식).
            // 뒤 묶음은 `댓글 … 모두 보기`로 들어가는 23-1 이 무한 스크롤로 받는다.
            comments = runCatching {
                server.feeds.comments(feedId, limit = FEED_DETAIL_COMMENT_PREVIEW).comments
            }.getOrNull().orEmpty()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            val myUserId = remember(server) { server.signedInUserId() }
            val reportable = feed?.author?.userId?.let { it != myUserId } == true
            FeedDetailHeader(
                onBack = onBack,
                onOpenReport = if (reportable) ({ onOpenReport(feedId) }) else null
            )
            val loadedFeed = feed
            if (loadedFeed == null) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    MoyeoEmptyState(if (loadFailed) MoyeoEmptyText.FAILED else MoyeoEmptyText.LOADING)
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
                    // 화면기획 23 은 작성자 바로 아래에 **코스 제목**이 굵게 온다.
                    // 이전에는 제목이 본문 아래에 작은 글씨로 있어 웹·iOS 와 위계가 어긋났다.
                    loadedFeed.trip?.courseTitle?.let { courseTitle ->
                        item {
                            Text(
                                text = courseTitle,
                                modifier = Modifier.padding(top = 14.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = colorScheme.onSurface
                            )
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
                                        contentScale = ContentScale.Crop,
                                        fallbackShape = MoyeoPlaceholderShape.SQUARE
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
                    // 화면기획 23 의 지표 칸. 서버는 이동 거리·소요 시간을 주지 않으므로
                    // 방문지 수만 그린다 — 없는 값을 지어내지 않는다.
                    loadedFeed.trip?.placeTitles?.size?.takeIf { it > 0 }?.let { placeCount ->
                        item {
                            Surface(
                                modifier = Modifier
                                    .padding(top = 18.dp)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = colorScheme.surfaceVariant
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "방문지",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${placeCount}곳",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = colorScheme.onSurface
                                    )
                                }
                            }
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
                                text = "댓글 ${loadedFeed.commentCount}개 모두 보기 →",
                                modifier = Modifier
                                    .clickable(onClick = onOpenAllComments)
                                    .testTag("feed-comments-open"),
                                style = MaterialTheme.typography.labelLarge,
                                color = colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                    if (comments.isEmpty()) {
                        item { MoyeoEmptyState(MoyeoEmptyText.NO_COMMENTS, testTag = "feed-detail-no-comments") }
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
                            .onSuccess {
                                // 댓글을 달면 **미리보기 첫 묶음을 다시 읽는다**. 손으로 끼워 넣지
                                // 않는 이유는 웹과 같다 — 최상위 댓글은 최신 ID 부터 오므로 3건
                                // 미리보기에 새 댓글이 들어가면 마지막 1건은 밀려나야 하고,
                                // 그 자리를 클라가 계산하면 서버 순서와 어긋난다.
                                comment = ""
                                feed = feed?.copy(commentCount = (feed?.commentCount ?: 0) + 1)
                                comments = runCatching {
                                    server.feeds.comments(feedId, limit = FEED_DETAIL_COMMENT_PREVIEW).comments
                                }.getOrNull() ?: comments
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
