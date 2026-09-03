package kr.hanchae.moyeotrip.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.TextUnit
import kr.hanchae.moyeotrip.ui.components.CachedRemoteImage
import kr.hanchae.moyeotrip.ui.components.MoyeoNicknameAnimal

@Composable
internal fun UserAvatar(imageUrl: String?, nickname: String?, modifier: Modifier, fallbackFontSize: TextUnit) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        CachedRemoteImage(
            url = imageUrl,
            contentDescription = "내 프로필 이미지",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        ) {
            Text(text = MoyeoNicknameAnimal.emojiForNickname(nickname), fontSize = fallbackFontSize)
        }
    }
}
