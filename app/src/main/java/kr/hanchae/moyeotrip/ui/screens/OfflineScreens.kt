package kr.hanchae.moyeotrip.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kr.hanchae.moyeotrip.ui.theme.MoyeoTheme

@Composable
fun OfflineNoCacheScreen(onRetry: () -> Unit, onOpenSavedTrips: () -> Unit = {}) {
    // 화면기획 35의 페이지는 흰색(bgBase)이고 "지금도 볼 수 있는 것"만 회색 채움 박스다.
    // 배너는 상태바 아래에서 시작해야 시계·시그널과 겹치지 않는다.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MoyeoTheme.pageSurface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("offline-no-cache")
    ) {
        // 연결 상태는 화면 최상단 배너로 먼저 알린다 (화면기획)
        OfflineDisconnectedBanner()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(.55f))
            Surface(
                modifier = Modifier.size(108.dp),
                shape = CircleShape,
                color = MoyeoTheme.subtleSurface
            ) {
                Icon(
                    imageVector = Icons.Filled.WifiOff,
                    contentDescription = "인터넷 연결 없음",
                    modifier = Modifier
                        .padding(30.dp)
                        .testTag("offline-connection-icon"),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "연결 상태를\n확인해주세요",
                modifier = Modifier.padding(top = 24.dp),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "와이파이나 데이터가 켜져 있는지 확인해주세요.\n연결되면 자동으로 다시 불러올게요.",
                modifier = Modifier.padding(top = 9.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 26.dp),
                shape = RoundedCornerShape(16.dp),
                color = MoyeoTheme.subtleSurface,
                border = BorderStroke(1.dp, MoyeoTheme.tints.softLine)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "지금도 볼 수 있는 것",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    OfflineCapabilityRow(Icons.Filled.Bookmark, "저장해둔 코스와 지난 여행 기록")
                    OfflineCapabilityRow(Icons.Filled.People, "이미 받아둔 채팅 내용")
                    // 오프라인에서 보낸 메시지의 운명을 알려주는 줄. 화면기획에 있는 항목이다.
                    OfflineCapabilityRow(Icons.Outlined.ChatBubbleOutline, "보낸 메시지는 연결되면 자동으로 전송돼요")
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("offline-retry"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("다시 시도")
            }
            TextButton(
                onClick = onOpenSavedTrips,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp).testTag("offline-open-saved")
            ) {
                Text(
                    "저장된 내 여행 보기",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "연결되면 이 화면은 저절로 닫혀요",
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** 연결이 끊겼음을 알리는 화면 최상단 배너. */
@Composable
private fun OfflineDisconnectedBanner() {
    val tints = MoyeoTheme.tints
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(tints.warningTint)
            .padding(vertical = 9.dp)
            .testTag("offline-disconnected-banner"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(tints.onWarningTint)
        )
        Text(
            text = "인터넷에 연결되어 있지 않아요",
            modifier = Modifier.padding(start = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = tints.onWarningTint,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun OfflineCachedBanner(modifier: Modifier = Modifier, onRetry: () -> Unit = {}) {
    // 화면기획 36 — 경고색 배너에 상태 문구와 "다시 시도"를 함께 둔다
    val tints = MoyeoTheme.tints
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(tints.warningTint)
            .padding(horizontal = 14.dp)
            .testTag("offline-cached-banner"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(tints.onWarningTint)
        )
        Text(
            text = "연결이 끊겼어요 · 저장된 내용만 보여드려요",
            modifier = Modifier.padding(start = 7.dp).weight(1f),
            style = MaterialTheme.typography.labelMedium,
            color = tints.onWarningTint,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "다시 시도",
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onRetry)
                .padding(horizontal = 4.dp, vertical = 4.dp)
                .testTag("offline-cached-retry"),
            style = MaterialTheme.typography.labelMedium,
            color = tints.onWarningTint,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun OfflineCapabilityRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            modifier = Modifier.padding(start = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
