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
            Text(text = nickname.animalEmoji(), fontSize = fallbackFontSize)
        }
    }
}

private fun String?.animalEmoji(): String {
    val value = this.orEmpty()
    return when {
        "사슴" in value || "고라니" in value -> "🦌"
        "토끼" in value -> "🐰"
        "거북" in value -> "🐢"
        "여우" in value -> "🦊"
        "고양" in value -> "🐱"
        "강아" in value -> "🐶"
        "두루미" in value || "백로" in value -> "🪽"
        "수달" in value -> "🦦"
        "부엉" in value -> "🦉"
        else -> "🐻"
    }
}
