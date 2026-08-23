package kr.hanchae.moyeotrip.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.hanchae.moyeotrip.data.TripCourse
import kr.hanchae.moyeotrip.domain.RecruitmentSummary
import kr.hanchae.moyeotrip.domain.recruitmentSummary
import kr.hanchae.moyeotrip.ui.theme.MoyeoTheme
import kr.hanchae.moyeotrip.ui.theme.SunYellow

/**
 * 본문 안의 일부 어절만 굵게 강조한다 — 화면기획 본문의 `<b>` 구간을 그대로 옮기기 위한 헬퍼.
 *
 * 한 문장을 여러 Text로 쪼개면 줄바꿈 위치가 기획과 달라지므로 반드시 한 AnnotatedString으로 만든다.
 * [boldParts] 는 문장에 나타나는 순서대로 찾는다. 찾지 못한 조각은 그냥 건너뛴다.
 */
fun emphasized(
    text: String,
    vararg boldParts: String,
    boldWeight: FontWeight = FontWeight.Bold,
    boldColor: Color? = null
): AnnotatedString = buildAnnotatedString {
    var cursor = 0
    boldParts.forEach { part ->
        if (part.isEmpty()) return@forEach
        val start = text.indexOf(part, cursor)
        if (start < 0) return@forEach
        append(text.substring(cursor, start))
        withStyle(SpanStyle(fontWeight = boldWeight, color = boldColor ?: Color.Unspecified)) {
            append(part)
        }
        cursor = start + part.length
    }
    append(text.substring(cursor))
}

/**
 * 화면기획의 모집 진행바. Material3 기본값이 넣는 끝점 표시(초록 점)와 트랙 사이 여백을 없애고,
 * 라이트에서도 보이는 트랙 색(`softLine`)을 쓴다. 기본 `surfaceVariant`는 라이트 배경(#F7F8F7)과
 * 같은 값이라 미충족 구간이 아예 보이지 않는다.
 */
@Composable
fun MoyeoLinearProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
    color: Color? = null,
    trackColor: Color? = null
) {
    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(50)),
        color = color ?: MaterialTheme.colorScheme.primary,
        trackColor = trackColor ?: MoyeoTheme.tints.softLine,
        gapSize = 0.dp,
        drawStopIndicator = {}
    )
}

@Composable
fun ScreenHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (action != null) {
            Spacer(modifier = Modifier.width(12.dp))
            action()
        }
    }
}

@Composable
fun SectionHeader(title: String, trailing: String? = null, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun MascotBadge(emoji: String, label: String, modifier: Modifier = Modifier, container: Color? = null) {
    val colorScheme = MaterialTheme.colorScheme
    val resolvedContainer = container ?: colorScheme.primaryContainer

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(resolvedContainer)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = emoji, fontSize = 20.sp)
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun AnimalAvatar(emoji: String, modifier: Modifier = Modifier, container: Color? = null) {
    val resolvedContainer = container ?: MaterialTheme.colorScheme.secondaryContainer

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(resolvedContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = 24.sp)
    }
}

@Composable
fun InfoPill(text: String, modifier: Modifier = Modifier, container: Color? = null, content: Color? = null) {
    val colorScheme = MaterialTheme.colorScheme
    val resolvedContainer = container ?: colorScheme.surface
    val resolvedContent = content ?: colorScheme.onSurface

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = resolvedContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = resolvedContent
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagRow(tags: List<String>, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        tags.forEach { tag ->
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = tag,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TripCourseCard(course: TripCourse, onClick: () -> Unit, modifier: Modifier = Modifier, highlight: Boolean = false) {
    val containerColor = if (highlight) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
    val imageContainerColor = if (highlight) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val recruitment = course.recruitmentSummary()
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(imageContainerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = course.imageEmoji, fontSize = 30.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${course.region} · ${course.duration}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = course.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = course.oneLine,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnimalAvatar(
                        emoji = course.hostAvatar,
                        modifier = Modifier.size(34.dp),
                        container = if (highlight) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        }
                    )
                    Text(
                        text = course.host,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                InfoPill(
                    text = "${recruitment.displayText} · ★ ${course.rating}",
                    container = imageContainerColor,
                    content = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            RecruitmentProgress(
                summary = recruitment,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            TagRow(tags = course.tags.take(3))
        }
    }
}

@Composable
fun RecruitmentProgress(summary: RecruitmentSummary, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        MoyeoLinearProgress(
            progress = summary.progress,
            modifier = Modifier.fillMaxWidth(),
            height = 8.dp,
            color = if (summary.minimumMet) colorScheme.primary else colorScheme.secondary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = summary.minimumText,
                style = MaterialTheme.typography.labelLarge,
                color = if (summary.minimumMet) colorScheme.primary else colorScheme.secondary
            )
            Text(
                text = summary.displayText,
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatTile(label: String, value: String, modifier: Modifier = Modifier, container: Color? = null) {
    val colorScheme = MaterialTheme.colorScheme
    val resolvedContainer = container ?: colorScheme.surface

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = resolvedContainer),
        border = BorderStroke(1.dp, colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = if (container == null) colorScheme.primary else colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WarmDivider(modifier: Modifier = Modifier) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(SunYellow.copy(alpha = 0.35f))
    )
}
