package kr.hanchae.moyeotrip.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kr.hanchae.moyeotrip.ui.components.OverlayBackdrop
import kr.hanchae.moyeotrip.ui.theme.MoyeoTheme

@Composable
fun QaDesignSystemOverviewScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).testTag("qa-design-system"),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Text("모여트립 디자인 시스템", style = MaterialTheme.typography.headlineMedium) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.error
                ).forEach { color ->
                    Surface(Modifier.size(64.dp), color = color, shape = RoundedCornerShape(12.dp)) {}
                }
            }
        }
        item { Text("화면 제목", style = MaterialTheme.typography.headlineSmall) }
        item { Text("카드 제목", style = MaterialTheme.typography.titleMedium) }
        item { Text("본문과 보조 정보가 읽기 편한 간격을 유지해요.", style = MaterialTheme.typography.bodyMedium) }
        item { Button(onClick = {}, shape = RoundedCornerShape(12.dp)) { Text("주요 행동") } }
        item { OutlinedButton(onClick = {}, shape = RoundedCornerShape(12.dp)) { Text("보조 행동") } }
    }
}

@Composable
fun QaComponentStatesScreen() {
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(20.dp)
            .testTag("qa-component-states"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 화면기획은 상태마다 생김새가 다르다 — 빈 상태·에러에는 행동 버튼, 로딩은 스켈레톤,
        // 오프라인은 경고 색 배너. 네 장을 같은 모양으로 그리면 상태를 구분할 수 없다.
        Text("화면 상태", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        StateCard(
            label = "EMPTY",
            text = "아직 참여한 모임이 없어요",
            description = "첫 모임을 열어보세요",
            icon = Icons.Filled.Groups,
            actionLabel = "+ 만들기",
            actionIsPrimary = true
        )
        LoadingSkeletonCard()
        StateCard(
            label = "ERROR",
            text = "문제가 생겼어요",
            description = "잠시 후 다시 시도해주세요 E-503",
            icon = Icons.Filled.WarningAmber,
            isError = true,
            actionLabel = "새로고침"
        )
        OfflineStateBanner()
    }
}

@Composable
private fun StateCard(
    label: String,
    text: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isError: Boolean = false,
    actionLabel: String? = null,
    actionIsPrimary: Boolean = false
) {
    val tints = MoyeoTheme.tints
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(60.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (isError) tints.dangerTint else tints.primaryTint
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        null,
                        Modifier.size(28.dp),
                        tint = if (isError) tints.onDangerTint else MaterialTheme.colorScheme.primary
                    )
                }
            }
            Column(Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    description,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (actionLabel != null) {
                if (actionIsPrimary) {
                    Button(onClick = {}, shape = RoundedCornerShape(10.dp)) { Text(actionLabel) }
                } else {
                    OutlinedButton(onClick = {}, shape = RoundedCornerShape(10.dp)) { Text(actionLabel) }
                }
            }
        }
    }
}

@Composable
private fun LoadingSkeletonCard() {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "LOADING (Skeleton)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                ) {}
                Column(
                    Modifier.weight(1f).padding(top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SkeletonBar(0.7f, 12.dp)
                    SkeletonBar(0.5f, 10.dp)
                    SkeletonBar(0.85f, 10.dp)
                }
            }
        }
    }
}

@Composable
private fun SkeletonBar(widthFraction: Float, height: Dp) {
    Surface(
        modifier = Modifier.fillMaxWidth(widthFraction).height(height),
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    ) {}
}

@Composable
private fun OfflineStateBanner() {
    val tints = MoyeoTheme.tints
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = tints.warningTint) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "OFFLINE",
                style = MaterialTheme.typography.labelMedium,
                color = tints.onWarningTint,
                fontWeight = FontWeight.Bold
            )
            Text(
                "인터넷에 연결되어 있지 않아요",
                Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = tints.onWarningTint
            )
            Text(
                "재시도",
                style = MaterialTheme.typography.labelMedium,
                color = tints.onWarningTint,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun QaLeaveAlertScreen(
    onDismiss: () -> Unit,
    backdropThreadId: String = OVERLAY_BACKDROP_THREAD_ID,
    // 31 호스트 / 31-1 참가자. **결과가 전혀 다른데 문구가 호스트 기준으로 고정**돼 있어서
    // 참가자가 이 화면을 보면 자기가 모임을 없애는 것으로 읽혔다 (정본 §6-5).
    host: Boolean = true
) {
    // 화면기획의 경고 팝업은 좌측 정렬 카드에 같은 너비의 두 버튼이다.
    // Material AlertDialog 는 제목을 가운데 두고 버튼을 우측에 몰아 다른 플랫폼과 어긋난다.
    // changeLog14 "오버레이 배경 일괄" — 빈 딤 대신 20-1 채팅방 사이드 메뉴 본문을 깐다.
    val colors = MaterialTheme.colorScheme
    val copy = LeaveCopy.of(host)
    OverlayBackdrop(
        modifier = Modifier.testTag(if (host) "leave-alert-screen" else "leave-member-alert-screen"),
        scrimAlpha = .48f,
        onScrimClick = onDismiss,
        background = { ChatMenuBody(threadId = backdropThreadId) }
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = 372.dp)
                .padding(horizontal = 18.dp),
            shape = RoundedCornerShape(20.dp),
            // 시트·팝업 표면은 화면 배경(background)과 구분되는 카드 표면(surface)이다
            color = colors.surface,
            border = BorderStroke(1.dp, colors.outline),
            shadowElevation = 16.dp
        ) {
            Column(Modifier.padding(24.dp)) {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(colors.error.copy(alpha = .16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.WarningAmber, null, tint = colors.error)
                }
                Text(
                    copy.title,
                    modifier = Modifier.padding(top = 16.dp).testTag("leave-alert-title"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    copy.description,
                    modifier = Modifier.padding(top = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, colors.outline),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.onSurface)
                    ) { Text("취소") }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(44.dp).testTag("leave-alert-confirm"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.error)
                    ) { Text(copy.confirm) }
                }
            }
        }
    }
}
