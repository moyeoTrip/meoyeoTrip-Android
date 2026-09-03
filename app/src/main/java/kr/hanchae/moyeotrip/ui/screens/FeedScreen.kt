package kr.hanchae.moyeotrip.ui.screens

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kr.hanchae.moyeotrip.data.feed.FeedTab
import kr.hanchae.moyeotrip.data.feed.ServerFeed
import kr.hanchae.moyeotrip.ui.LocalServerData
import kr.hanchae.moyeotrip.ui.components.CachedRemoteImage
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyState
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyText
import kr.hanchae.moyeotrip.ui.components.MoyeoPlaceholderShape
import kr.hanchae.moyeotrip.ui.components.ServerListState
import kr.hanchae.moyeotrip.ui.state.LocalTabDataStore
import kr.hanchae.moyeotrip.ui.theme.Coral
import kr.hanchae.moyeotrip.ui.theme.MoyeoTheme

@Composable
fun FeedScreen(onOpenPost: (String) -> Unit, onWritePost: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val server = LocalServerData.current
    // 피드도 "보던 상태 유지" 탭이다 — 탭별 목록을 탭 바깥 보관소에 담고 재진입 시 다시 부르지 않는다
    // (정본 R1·R3).
    val feedTab = LocalTabDataStore.current.feed
    val selectedTab = feedTab.selectedTab
    val feeds = feedTab.state(selectedTab)
    val feedScope = rememberCoroutineScope()
    LaunchedEffect(server, selectedTab, feedTab.reloadKey) {
        if (server == null) {
            feedTab.show(selectedTab, ServerListState.Loaded(emptyList()))
            return@LaunchedEffect
        }
        // 성공해서 보여줄 목록이 있는 탭이면 그대로 그린다 — 탭을 오가도 다시 부르지 않는다(정본 R3).
        // 직전 조회가 실패했으면 보여줄 게 없으므로 다시 부른다(R3-1).
        if (feedTab.isLoaded(selectedTab)) return@LaunchedEffect
        // 여기까지 왔다는 것은 캐시가 없다는 뜻이라 로딩 문구가 맞다(정본 R2).
        feedTab.show(selectedTab, ServerListState.Loading)
        feedTab.putFromServer(
            selectedTab,
            runCatching { server.feeds.feeds(selectedTab.serverTab).feeds }
                .fold({ ServerListState.Loaded(it) }, { ServerListState.Failed })
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        FeedTabBar(
            selectedTab = selectedTab,
            onSelectTab = { feedTab.selectedTab = it }
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
            val state = feeds
            when {
                state is ServerListState.Loading -> item { MoyeoEmptyState(MoyeoEmptyText.LOADING) }

                state is ServerListState.Failed -> item {
                    MoyeoEmptyState(MoyeoEmptyText.FAILED, onRetry = { feedTab.reload(selectedTab) })
                }

                state is ServerListState.Loaded && state.items.isEmpty() -> item {
                    MoyeoEmptyState(MoyeoEmptyText.NO_FEEDS, testTag = "feed-empty")
                }

                state is ServerListState.Loaded -> items(
                    items = state.items,
                    key = { feed -> feed.feedId }
                ) { feed ->
                    ServerFeedPostRow(
                        feed = feed,
                        onOpenPost = { onOpenPost("srv-${feed.feedId}") },
                        onToggleLike = {
                            feedScope.launch {
                                val result = runCatching { server?.feeds?.toggleLike(feed.feedId) }.getOrNull()
                                    ?: return@launch
                                // 이 화면이 바꾼 값이라 해당 항목만 갈아 끼운다 — 목록을 다시 부르지 않는다(정본 R4).
                                val current = feedTab.state(selectedTab)
                                if (current is ServerListState.Loaded) {
                                    feedTab.putFromServer(
                                        selectedTab,
                                        ServerListState.Loaded(
                                            current.items.map {
                                                if (it.feedId == feed.feedId) {
                                                    it.copy(liked = result.liked, likeCount = result.likeCount)
                                                } else {
                                                    it
                                                }
                                            }
                                        )
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
        // 피드 작성 진입점은 목록 카드가 아니라 화면 하단 동작이다 (모든 카드에 + 버튼을 달지 않는다)
        FeedWriteEntry(onWritePost = onWritePost)
    }
}

@Composable
private fun FeedWriteEntry(onWritePost: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.End
    ) {
        IconButton(
            onClick = onWritePost,
            modifier = Modifier
                .size(44.dp)
                .testTag("feed-write-action")
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
private fun feedTimelineColor(): Color = if (MoyeoTheme.isDark) {
    MaterialTheme.colorScheme.background
} else {
    MaterialTheme.colorScheme.surface
}

internal enum class FeedTimelineTab(val label: String, val key: String) {
    Following("팔로잉", "following"),
    Discover("발견", "discover")
}

/** 서버 피드 탭 매핑 — 팔로잉은 FRIENDS, 발견은 DISCOVER 다. */
internal val FeedTimelineTab.serverTab: FeedTab
    get() = when (this) {
        FeedTimelineTab.Following -> FeedTab.FRIENDS
        FeedTimelineTab.Discover -> FeedTab.DISCOVER
    }
