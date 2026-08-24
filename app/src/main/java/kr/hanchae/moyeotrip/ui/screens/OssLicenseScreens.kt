package kr.hanchae.moyeotrip.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.hanchae.moyeotrip.data.oss.OssLicense
import kr.hanchae.moyeotrip.data.oss.OssLicenseCatalog
import kr.hanchae.moyeotrip.ui.theme.MoyeoTheme

/**
 * 화면기획 29-4 오픈소스 라이선스 목록.
 *
 * 목록은 앱에 내장한 정본(`resources/oss/oss-licenses.json` — 워크스페이스 `docs/oss` 의 android 배열)이라
 * 서버 호출도 로그인도 필요 없고 캡처 모드에서도 같은 내용을 보여준다(changeLog17).
 */
@Composable
fun OssLicensesScreen(onBack: () -> Unit, onOpenLicense: (String) -> Unit) {
    val items = remember { OssLicenseCatalog.items }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MoyeoTheme.pageSurface)
    ) {
        CompactMenuHeader(title = "오픈소스 라이선스", onBack = onBack)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("oss-licenses-list"),
            contentPadding = menuContentPadding(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MoyeoTheme.subtleSurface,
                    border = BorderStroke(1.dp, MoyeoTheme.tints.softLine)
                ) {
                    Text(
                        text = "모여트립은 아래 오픈소스 소프트웨어의 도움을 받아 만들었어요. " +
                            "항목을 누르면 라이선스 전문을 볼 수 있어요. " +
                            "항목 구성은 플랫폼(iOS · Android · 웹)에 따라 달라요.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(13.dp),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp, lineHeight = 19.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "사용 중인 오픈소스",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${items.size}개",
                        modifier = Modifier.testTag("oss-licenses-count"),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MoyeoTheme.cardSurface,
                    border = BorderStroke(1.dp, MoyeoTheme.tints.softLine)
                ) {
                    Column {
                        items.forEachIndexed { index, license ->
                            if (index > 0) {
                                HorizontalDivider(color = MoyeoTheme.tints.softLine)
                            }
                            OssLicenseRow(license = license, onClick = { onOpenLicense(license.slug) })
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    HorizontalDivider(color = MoyeoTheme.tints.softLine)
                    Text(
                        text = "오픈소스 라이선스가 아닌 자체 배포 SDK는 해당 사업자의 약관을 따라요. " +
                            "새 라이브러리를 추가하면 이 목록도 함께 갱신돼요.",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp, lineHeight = 18.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun OssLicenseRow(license: OssLicense, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 62.dp)
            .clickable(onClick = onClick)
            .testTag("oss-license-row-${license.slug}")
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = license.name,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.5.sp),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "${license.version} · ${license.license}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * 화면기획 29-4a 라이선스 전문.
 *
 * 전문이 없는 자체 배포 SDK(카카오 지도)는 라이선스 이름과 원문 URL만 보여준다 —
 * 저작권 줄이나 전문을 지어내지 않는다.
 */
@Composable
fun OssLicenseDetailScreen(slug: String, onBack: () -> Unit) {
    val license = remember(slug) { OssLicenseCatalog.find(slug) }
    val licenseText = remember(license) { license?.let(OssLicenseCatalog::licenseText) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MoyeoTheme.pageSurface)
    ) {
        CompactMenuHeader(title = "라이선스 전문", onBack = onBack)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("oss-license-detail"),
            contentPadding = menuContentPadding(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (license == null) {
                return@LazyColumn
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text(
                        text = license.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Black
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        OssLicenseBadge(
                            text = license.version,
                            container = MoyeoTheme.subtleSurface,
                            content = MaterialTheme.colorScheme.onSurfaceVariant,
                            border = MoyeoTheme.tints.softLine
                        )
                        OssLicenseBadge(
                            text = license.license,
                            container = MoyeoTheme.tints.primaryTint,
                            content = MoyeoTheme.tints.onPrimaryTint,
                            border = MoyeoTheme.tints.primaryTintStrong
                        )
                    }
                }
            }

            item {
                Text(
                    text = "원문 확인 · ${license.url}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp, lineHeight = 18.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            license.note?.let { note ->
                item {
                    Text(
                        text = note,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp, lineHeight = 18.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (licenseText != null) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MoyeoTheme.subtleSurface,
                        border = BorderStroke(1.dp, MoyeoTheme.tints.softLine)
                    ) {
                        Text(
                            text = licenseText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                                .testTag("oss-license-text"),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                lineHeight = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OssLicenseBadge(
    text: String,
    container: androidx.compose.ui.graphics.Color,
    content: androidx.compose.ui.graphics.Color,
    border: androidx.compose.ui.graphics.Color
) {
    Surface(
        modifier = Modifier.height(24.dp),
        shape = CircleShape,
        color = container,
        border = BorderStroke(1.dp, border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = content,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
