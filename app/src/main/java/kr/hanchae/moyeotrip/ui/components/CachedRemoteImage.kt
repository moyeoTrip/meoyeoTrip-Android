package kr.hanchae.moyeotrip.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import kr.hanchae.moyeotrip.data.image.MoyeoImageRepository

/**
 * 로딩 중과 "이미지가 없음"을 구분한다.
 *
 * 이전에는 비트맵이 `null` 인 동안 로딩과 실패가 한 덩어리라 실패한 자리에 빈 판이 남았다.
 * 로딩 중에는 [placeholder](스켈레톤)를 두고, 없거나 실패한 뒤에만 마스코트를 그린다 —
 * 로딩 중에 마스코트를 먼저 보여주면 사진이 뒤늦게 갈리며 화면이 튄다.
 */
private sealed interface RemoteImageState {
    data object Loading : RemoteImageState
    data class Loaded(val bitmap: Bitmap) : RemoteImageState
    data object Missing : RemoteImageState
}

@Composable
internal fun CachedRemoteImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    /**
     * 이미지가 **필수인 자리**에서 URL 이 없거나 내려받기가 실패했을 때 대신 그릴 마스코트 에셋의 비율.
     *
     * `null` 이면 기존처럼 [placeholder] 를 그린다 — 아바타처럼 닉네임에서 유도한 대체 표시가
     * 이미 있는 자리는 그 편이 더 낫다.
     */
    fallbackShape: MoyeoPlaceholderShape? = null,
    placeholder: @Composable () -> Unit
) {
    val context = LocalContext.current.applicationContext
    // 이미 받아 둔 이미지는 **첫 프레임부터** 그린다. 코루틴이 돌 때까지 기다리면
    // 탭을 오갈 때마다 자리표시자가 한 번 깜빡이고 실제 사진으로 갈린다(정본 R5).
    val cached = remember(url) { MoyeoImageRepository.cachedOrNull(url) }
    val state by produceState<RemoteImageState>(
        initialValue = cached?.let(RemoteImageState::Loaded) ?: RemoteImageState.Loading,
        key1 = url
    ) {
        if (cached != null) return@produceState
        value = if (url == null) {
            RemoteImageState.Missing
        } else {
            MoyeoImageRepository.load(context, url)
                ?.let { RemoteImageState.Loaded(it) }
                ?: RemoteImageState.Missing
        }
    }

    when (val current = state) {
        is RemoteImageState.Loaded -> Image(
            bitmap = current.bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )

        RemoteImageState.Missing -> if (fallbackShape != null) {
            // 호출부가 넘긴 modifier·contentScale 을 그대로 적용해 자리 크기와 잘림을 맞춘다.
            Image(
                painter = painterResource(fallbackShape.drawableRes),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        } else {
            placeholder()
        }

        RemoteImageState.Loading -> placeholder()
    }
}
