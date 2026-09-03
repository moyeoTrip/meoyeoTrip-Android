package kr.hanchae.moyeotrip.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import kr.hanchae.moyeotrip.R

/**
 * 이미지가 필수인 자리에 실제 이미지가 없을 때 쓰는 공용 플레이스홀더.
 *
 * 관광 데이터는 이미지가 간헐적으로 비어 있는데(한국관광공사 원본에 없는 콘텐츠가 있다),
 * 화면기획은 이미지가 항상 있다고 보고 그려져 있다. 빈 자리를 그대로 두면 카드가 깨져 보이므로
 * 마스코트 일러스트를 대신 넣는다. 관광지가 아니어도 이미지가 필수인 뷰에 같이 쓴다.
 *
 * 에셋은 WEBP 다(앱·웹은 HEIC/WEBP, 원본은 화면기획에 둔다). DPI 별 드로어블로 번들한다.
 */
enum class MoyeoPlaceholderShape(@DrawableRes val drawableRes: Int) {
    /** 1:1 자리. 목록 썸네일·정사각 프레임. */
    SQUARE(R.drawable.placeholder_square),

    /** 16:9 자리. 상세 상단 히어로·카드형 커버. */
    LANDSCAPE(R.drawable.placeholder_landscape);

    companion object {
        /** 자리의 가로세로 비에서 가까운 쪽을 고른다. ContentScale.Crop 으로 잘리는 양을 줄인다. */
        fun nearest(width: Float, height: Float): MoyeoPlaceholderShape {
            if (height <= 0f) return LANDSCAPE
            val ratio = width / height
            val toSquare = kotlin.math.abs(ratio - 1f)
            val toLandscape = kotlin.math.abs(ratio - 16f / 9f)
            return if (toSquare <= toLandscape) SQUARE else LANDSCAPE
        }
    }
}

/**
 * URL 이 아예 없는 자리에 바로 그리는 플레이스홀더.
 *
 * [CachedRemoteImage] 의 `fallbackShape` 는 "받아봤더니 실패" 를 덮는다. 이 컴포저블은 URL 자체가
 * 없어 요청을 보낼 것도 없는 자리(서버가 썸네일을 안 준 모임·코스·피드)에 쓴다.
 */
@Composable
internal fun MoyeoPlaceholderImage(
    shape: MoyeoPlaceholderShape,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    Image(
        painter = painterResource(shape.drawableRes),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}
