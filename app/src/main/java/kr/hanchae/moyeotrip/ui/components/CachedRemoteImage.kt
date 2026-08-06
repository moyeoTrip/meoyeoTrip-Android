package kr.hanchae.moyeotrip.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kr.hanchae.moyeotrip.data.image.MoyeoImageRepository

@Composable
internal fun CachedRemoteImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    placeholder: @Composable () -> Unit
) {
    val context = LocalContext.current.applicationContext
    val bitmap by produceState<Bitmap?>(initialValue = null, key1 = url) {
        value = url?.let { MoyeoImageRepository.load(context, it) }
    }

    if (bitmap != null) {
        Image(
            bitmap = requireNotNull(bitmap).asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        placeholder()
    }
}
