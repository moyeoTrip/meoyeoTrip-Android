package kr.hanchae.moyeotrip.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.hanchae.moyeotrip.data.FeedPost
import kr.hanchae.moyeotrip.data.MockTripRepository
import kr.hanchae.moyeotrip.ui.components.AnimalAvatar

@Composable
fun FeedDetailScreen(postId: String, onBack: () -> Unit, onOpenAllComments: () -> Unit = {}) {
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
                    FeedDetailMetrics(post = post, modifier = Modifier.padding(top = 18.dp))
                }
                item {
                    TextButton(
                        onClick = onOpenAllComments,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .testTag("feed-comments-open")
                    ) {
                        Text("댓글 ${post.comments}개 모두 보기")
                    }
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
        FeedDetailTagRow(tags = post.detailTags(), modifier = Modifier.padding(top = 4.dp))
        Text(
            text = "공개 범위 · ${post.visibility.label}",
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FeedDetailTagRow(tags: List<String>, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tags.take(3).forEach { tag ->
            Text(
                text = tag,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Black,
                color = colorScheme.primary,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colorScheme.primaryContainer)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
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
private fun FeedDetailMetrics(post: FeedPost, modifier: Modifier = Modifier) {
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
                text = "댓글 ${post.comments}",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
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
    "feed-1" -> "청송 · 방금 다녀온 숲길 기록"
    "feed-2" -> "#한옥산책 #가을여행"
    "feed-3" -> "#경주 #야경 #월정교"
    "feed-4" -> "#포항 #바다 #드라이브"
    "feed-5" -> "#문경 #단풍 #숲길"
    "feed-7" -> "#울릉 #섬여행 #해안산책"
    else -> region
}

private fun FeedPost.detailTags(): List<String> = when (id) {
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
