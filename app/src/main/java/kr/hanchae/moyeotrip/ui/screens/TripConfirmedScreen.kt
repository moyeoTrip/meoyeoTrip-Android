package kr.hanchae.moyeotrip.ui.screens

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.hanchae.moyeotrip.R
import kr.hanchae.moyeotrip.data.rooms.ChatRoomDetail
import kr.hanchae.moyeotrip.data.rooms.RoomMembers
import kr.hanchae.moyeotrip.ui.LocalServerData
import kr.hanchae.moyeotrip.ui.components.emphasized
import kr.hanchae.moyeotrip.ui.components.moyeoTripDateText
import kr.hanchae.moyeotrip.ui.theme.MoyeoTheme

/**
 * 20-4 여행 확정 모먼트.
 *
 * `확정된 여행` 카드는 **상세 응답**(`GET chat-rooms/{id}`)이 근거다 — 일정·집합·최소 인원은
 * 목록 응답(`GET chat-rooms/my`)에 없다. 웹도 같은 방식이다(`screens-additions3.jsx` `confirmedRows`).
 *
 * 대상 방은 [tripId] 로 받는다. 없으면 내 모임에서 `CONFIRMED` 방을 찾는다 —
 * 예전에는 `!ended` 까지 걸어서 실서버의 확정된 방(101 · 61) 둘 다 걸러졌고,
 * 그 결과 제목·인원 문구·카드가 통째로 빈 화면이 찍혔다. 확정 카드는 여행이 끝난 뒤에도 성립한다.
 */
@Composable
fun TripConfirmedScreen(tripId: String? = null, onBack: () -> Unit, onOpenChat: (Long) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()
    val server = LocalServerData.current
    val requestedRoomId = tripId?.serverRoomIdOrNull()
    var room by remember(server, requestedRoomId) { mutableStateOf<ChatRoomDetail?>(null) }
    var members by remember(server, requestedRoomId) { mutableStateOf<RoomMembers?>(null) }
    LaunchedEffect(server, requestedRoomId) {
        if (server == null) return@LaunchedEffect
        // 지정된 방이 없으면 내 모임에서 확정된 방을 찾는다. 진행 중인 방을 먼저 본다.
        val roomId = requestedRoomId ?: runCatching {
            val mine = server.chatRooms.myRooms().filter { it.status == "CONFIRMED" }
            (mine.firstOrNull { !it.ended } ?: mine.firstOrNull())?.roomId
        }.getOrNull()
        // 확정 모먼트 화면이라 `CONFIRMED` 인 방만 그린다 — 모집 중인 방의 값으로
        // "여행이 확정됐어요!" 를 그리면 화면이 거짓말을 한다.
        val detail = roomId?.let { runCatching { server.chatRooms.room(it) }.getOrNull() }
        room = detail?.takeIf { it.status == "CONFIRMED" }
        members = room?.roomId?.let { runCatching { server.chatRooms.members(it) }.getOrNull() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .testTag("trip-confirmed-screen")
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                }
                Text(
                    text = room?.title.orEmpty(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.size(48.dp))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 92.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 18.dp)
                        .width(252.dp)
                        .height(142.dp)
                        .clip(RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.trip_confirmed_mascots),
                        contentDescription = "여행 확정을 함께 축하하는 곰, 토끼, 너구리 캐릭터",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(scaleX = 1.14f, scaleY = 1.14f),
                        contentScale = ContentScale.Crop
                    )
                }
                Text(
                    text = "여행이 확정됐어요!",
                    modifier = Modifier.padding(top = 18.dp),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                members?.let { loadedMembers ->
                    // 마감일을 함께 적는다 — 기획(「5월 22일 마감까지」)·웹·iOS 셋 다 적는데
                    // 안드로이드만 인원만 말해서 「언제까지 모은 결과인지」가 빠졌다 (20-4, 2026-09-09).
                    // 서버가 마감일을 안 주면 그 구절 없이 인원만 적는다 (NO-MOCK R1).
                    val deadline = room?.recruitmentDeadlineDate
                        ?.takeIf(String::isNotBlank)
                        ?.let { moyeoTripDateText(it) }
                    val sentence = if (deadline == null) {
                        "${loadedMembers.participantCount}명이 모였어요.\n이제 함께 떠나기만 하면 돼요."
                    } else {
                        "$deadline 마감까지 ${loadedMembers.participantCount}명이 모였어요.\n" +
                            "이제 함께 떠나기만 하면 돼요."
                    }
                    Text(
                        // 화면기획 20-4는 모인 인원만 굵은 초록으로 강조한다
                        text = emphasized(
                            sentence,
                            "${loadedMembers.participantCount}명",
                            boldWeight = FontWeight.ExtraBold,
                            boldColor = MoyeoTheme.tints.primaryEmphasis
                        ),
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                room?.let { confirmed -> ConfirmedTripCard(room = confirmed, members = members) }
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.outline.copy(alpha = .6f))
                ) {
                    Row(
                        modifier = Modifier.padding(13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = RoundedCornerShape(11.dp), color = colors.surfaceVariant) {
                            Icon(
                                Icons.Filled.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.padding(10.dp).size(20.dp)
                            )
                        }
                        Text(
                            text = "확정 카드를 이미지로 저장해 공유할 수 있어요",
                            modifier = Modifier.weight(1f).padding(horizontal = 11.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                        Icon(Icons.Filled.Share, contentDescription = "확정 카드 공유", modifier = Modifier.size(19.dp))
                    }
                }
                Text(
                    text = "확정 이후에는 경로가 잠겨요. 변경이 필요하면 채팅방 공지로 알려주세요.",
                    modifier = Modifier.padding(top = 14.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        ConfettiBurst(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 108.dp)
                .height(202.dp)
        )
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            color = colors.surface,
            shadowElevation = 8.dp
        ) {
            Button(
                // 확정된 방을 못 읽었으면 갈 채팅방이 없다
                onClick = { room?.roomId?.let(onOpenChat) },
                enabled = room != null,
                modifier = Modifier.fillMaxWidth().padding(
                    start = 20.dp,
                    top = 10.dp,
                    end = 20.dp,
                    bottom = 24.dp
                ).height(54.dp),
                // 다른 플랫폼의 CTA는 꼭지점만 둥근 사각형이다. Material 기본 알약형은 혼자 튄다.
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("채팅방으로 가기")
            }
        }
    }
}

@Composable
private fun ConfirmedTripCard(room: ChatRoomDetail, members: RoomMembers?) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        shape = RoundedCornerShape(16.dp),
        color = colors.primaryContainer,
        border = BorderStroke(1.dp, colors.primary.copy(alpha = .25f))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(colors.primary, CircleShape))
                Text(
                    "확정된 여행",
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.primary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            // 서버가 주는 값만 적는다 — 집합 안내처럼 없는 값은 줄째로 빠진다
            ConfirmedInfoRow(
                Icons.Filled.CalendarMonth,
                listOfNotNull(room.scheduleText(), room.travelHoursText()).joinToString(" · ")
            )
            room.meetingText()?.let { meeting -> ConfirmedInfoRow(Icons.Filled.LocationOn, meeting) }
            // 최소 인원은 서버 상세가 줄 때만 적는다. `충족` 은 실제로 넘겼을 때만 붙인다 —
            // 미달인 방에 붙이면 화면이 사실과 다른 말을 한다.
            ConfirmedInfoRow(
                Icons.Filled.Groups,
                buildString {
                    append("${room.participantCount}명")
                    room.minimumParticipants?.let { minimum ->
                        append(" · 최소 ${minimum}명")
                        if (room.participantCount >= minimum) append(" 충족")
                    }
                }
            )
            members?.let { loadedMembers ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    loadedMembers.members.take(5).forEachIndexed { index, member ->
                        Box(modifier = Modifier.graphicsLayer { translationX = (-index * 4).dp.toPx() }) {
                            UserAvatar(
                                imageUrl = member.profileImageUrl,
                                nickname = member.nickname,
                                modifier = Modifier.size(30.dp),
                                fallbackFontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfirmedInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(17.dp), tint = MaterialTheme.colorScheme.primary)
        Text(
            text,
            modifier = Modifier.padding(start = 9.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ConfettiBurst(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val motionEnabled = remember(context) {
        runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
        }.getOrDefault(true)
    }
    val progress = remember { Animatable(if (motionEnabled) 0f else .35f) }
    val palette = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        Color(0xFF6CB08B),
        Color(0xFFE4A43A)
    )

    // 조각마다 다른 출발 위치·낙하 속도·좌우 흔들림·회전 속도를 **한 번만** 뽑아 둔다.
    // 예전에는 `index * 37 % 96` 같은 규칙으로 만들어서 눈에 패턴이 보였고,
    // `rotate(...)` 로 조각의 **위치까지** 돌려 원을 그리는 것처럼 움직였다
    // (2026-08-31 사용자 지적: "정해진 패턴으로 원으로 돌기만하네").
    //
    // 매 프레임 난수를 뽑으면 조각이 튀므로 `remember` 로 고정한다.
    val pieces = remember {
        val random = java.util.Random(20260831)
        List(26) {
            ConfettiPiece(
                startXRatio = random.nextFloat(),
                startYRatio = -0.15f - random.nextFloat() * 0.35f, // 화면 위에서 시작
                fallRatio = 1.25f + random.nextFloat() * 0.5f, // 화면 아래까지 지나간다
                swayDp = -26f + random.nextFloat() * 52f,
                swayCycles = 1.2f + random.nextFloat() * 1.6f,
                spinTurns = -1.5f + random.nextFloat() * 3f,
                widthDp = 5f + random.nextFloat() * 5f,
                delay = random.nextFloat() * 0.35f
            )
        }
    }

    LaunchedEffect(motionEnabled) {
        if (motionEnabled) progress.animateTo(1f, tween(2200, easing = LinearEasing))
    }

    Canvas(modifier = modifier) {
        pieces.forEachIndexed { index, piece ->
            // 조각마다 조금 늦게 출발해 한꺼번에 쏟아지지 않는다.
            val local = ((progress.value - piece.delay) / (1f - piece.delay)).coerceIn(0f, 1f)
            if (local <= 0f) return@forEachIndexed

            val x = size.width * piece.startXRatio +
                piece.swayDp.dp.toPx() * kotlin.math.sin(local * piece.swayCycles * 2f * Math.PI.toFloat())
            val y = size.height * (piece.startYRatio + piece.fallRatio * local)
            val width = piece.widthDp.dp.toPx()
            // 끝에서 서서히 사라진다 — 바닥에 쌓인 것처럼 남지 않는다.
            val alpha = if (!motionEnabled) .7f else (1f - ((local - .7f) / .3f)).coerceIn(0f, 1f)
            if (alpha <= 0f) return@forEachIndexed

            rotate(
                degrees = piece.spinTurns * 360f * local,
                pivot = androidx.compose.ui.geometry.Offset(x + width / 2f, y + width * .25f)
            ) {
                drawRoundRect(
                    color = palette[index % palette.size].copy(alpha = alpha),
                    topLeft = androidx.compose.ui.geometry.Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(width, width * .5f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
                )
            }
        }
    }
}

/** 조각 하나의 고정된 성질. 매 프레임 난수를 뽑으면 조각이 튄다. */
private data class ConfettiPiece(
    val startXRatio: Float,
    val startYRatio: Float,
    val fallRatio: Float,
    val swayDp: Float,
    val swayCycles: Float,
    val spinTurns: Float,
    val widthDp: Float,
    val delay: Float
)
