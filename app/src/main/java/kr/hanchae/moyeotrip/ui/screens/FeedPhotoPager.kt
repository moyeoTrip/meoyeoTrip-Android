package kr.hanchae.moyeotrip.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kr.hanchae.moyeotrip.ui.components.CachedRemoteImage
import kr.hanchae.moyeotrip.ui.components.MoyeoPlaceholderShape

/**
 * 23 피드 상세의 사진 영역 — 기획 `FeedDetailPhotos` 정본과 같은 규칙.
 *
 * 예전에는 `imageUrls.take(2)` 로 앞 2장만 나란히 그렸다. 사진이 4장이면 2장은 볼 방법이
 * 아예 없었다 (사용자가 발견). iOS·웹은 거기에 고정 `"1/4"` 배지까지 얹어 페이저가 있는
 * 것처럼 보였다.
 *
 * 1. **전체를 좌우로 넘긴다.** 2칸으로 잘라 보여주지 않는다.
 * 2. 배지는 **지금 몇 번째인지**를 가리킨다.
 * 3. 사진을 누르면 **전체화면**으로 크게 본다.
 */
@Composable
fun FeedPhotoPager(imageUrls: List<String>, height: Int = 190) {
    if (imageUrls.isEmpty()) return
    val colorScheme = MaterialTheme.colorScheme
    val pagerState = rememberPagerState(pageCount = { imageUrls.size })
    var zoomed by remember { mutableIntStateOf(-1) }

    Box(
        modifier = Modifier
            .padding(top = 18.dp)
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(10.dp))
            .testTag("feed-detail-photos")
    ) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            CachedRemoteImage(
                url = imageUrls[page],
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { zoomed = page },
                contentScale = ContentScale.Crop,
                fallbackShape = MoyeoPlaceholderShape.SQUARE
            ) {
                Box(modifier = Modifier.fillMaxSize().background(colorScheme.surfaceVariant))
            }
        }

        if (imageUrls.size > 1) {
            Text(
                text = "${pagerState.currentPage + 1}/${imageUrls.size}",
                color = Color.White,
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                    .padding(horizontal = 7.dp, vertical = 4.dp)
                    .testTag("feed-detail-photo-count")
            )
        }
    }

    if (zoomed >= 0) {
        FeedPhotoViewerDialog(
            imageUrls = imageUrls,
            startIndex = zoomed,
            onDismiss = { zoomed = -1 }
        )
    }
}

/** 전체화면으로 크게 본다. 좌우로 넘기고 닫기로 나간다. */
@Composable
private fun FeedPhotoViewerDialog(imageUrls: List<String>, startIndex: Int, onDismiss: () -> Unit) {
    val pagerState = rememberPagerState(initialPage = startIndex, pageCount = { imageUrls.size })
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .testTag("feed-detail-photo-viewer")
        ) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                CachedRemoteImage(
                    url = imageUrls[page],
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    // 전체화면에서는 잘라내지 않는다 — 사진 전체를 보려고 누른 것이다.
                    contentScale = ContentScale.Fit,
                    fallbackShape = MoyeoPlaceholderShape.SQUARE
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black))
                }
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(44.dp)
                    .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                    .testTag("feed-detail-photo-viewer-close")
            ) {
                Icon(Icons.Filled.Close, contentDescription = "닫기", tint = Color.White)
            }
            if (imageUrls.size > 1) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1}/${imageUrls.size}",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
