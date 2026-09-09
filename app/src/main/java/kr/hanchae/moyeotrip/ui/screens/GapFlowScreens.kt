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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import kr.hanchae.moyeotrip.data.courses.TravelCourse
import kr.hanchae.moyeotrip.data.rooms.ChatRoomDetail
import kr.hanchae.moyeotrip.data.rooms.ChatRoomSearchResult
import kr.hanchae.moyeotrip.data.rooms.MeetingInfoUpdate
import kr.hanchae.moyeotrip.data.rooms.MyWaitingRoom
import kr.hanchae.moyeotrip.data.rooms.RoomKickHistory
import kr.hanchae.moyeotrip.data.rooms.RoomNotice
import kr.hanchae.moyeotrip.data.rooms.RoomStatusChange
import kr.hanchae.moyeotrip.data.social.Friend
import kr.hanchae.moyeotrip.ui.LocalServerData
import kr.hanchae.moyeotrip.ui.components.CachedRemoteImage
import kr.hanchae.moyeotrip.ui.components.KakaoMapView
import kr.hanchae.moyeotrip.ui.components.MapMarker
import kr.hanchae.moyeotrip.ui.components.MapMarkerShape
import kr.hanchae.moyeotrip.ui.components.MapUnavailablePlaceholder
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyState
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyText
import kr.hanchae.moyeotrip.ui.components.MoyeoLatLng
import kr.hanchae.moyeotrip.ui.components.MoyeoPlaceholderShape
import kr.hanchae.moyeotrip.ui.components.OverlayBackdrop
import kr.hanchae.moyeotrip.ui.components.ServerListState
import kr.hanchae.moyeotrip.ui.components.afterReload

// 버튼은 있는데 **이어지는 화면이 없던** 자리들 — 정본 `docs/alignment/ATTACH-COMPOSER-CANON.md` §6.
//
// 기획 원본: `모여트립 in 경북/screens-gaps.jsx` · `screens-gaps2.jsx` · `screens-gaps3.jsx`
//
// 열두 화면이 20-2a~f 첨부 작성과 같은 골격(헤더 + 스크롤 본문 + 하단 고정 CTA 한 개)을 쓴다.
// 되돌릴 수 없는 동작(불발·친구 끊기·공지 삭제·신청 취소·차단 해제)은 반드시 확인 단계를 지난다.
//
// 목데이터를 그리지 않는다(NO-MOCK-CANON R1). 서버가 대상을 주지 않으면 화면은 그대로 열리되
// 실행 버튼만 잠그고 이유를 적는다 — 예시 값을 대신 그리지 않는다.

/** 대상 방을 못 찾았을 때 CTA 아래에 적는 이유. 20-2a~f 의 `NO_ROOM_HINT` 와 같은 자리다. */
// `internal` 이다 — 수정·삭제 화면 7종(`EditDeleteScreens.kt`)이 같은 골격을 쓴다.
// 화면마다 다른 껍데기를 만들면 같은 종류로 읽히지 않는다.
internal const val GAP_NO_ROOM_HINT = "실제 모임에서 열어야 보낼 수 있어요."

/** §6 화면 공통 골격. [cta] 가 null 이면 하단 바 없이 스크롤 본문만 그린다. */
@Composable
internal fun GapScaffold(
    title: String,
    onBack: () -> Unit,
    testTag: String,
    cta: String? = null,
    ctaEnabled: Boolean = true,
    ctaDanger: Boolean = false,
    error: String? = null,
    onCta: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로", tint = colors.onBackground)
            }
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                color = colors.onBackground,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.size(48.dp))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            content()
        }
        if (cta != null) {
            Surface(color = colors.surface, shadowElevation = 8.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    error?.let {
                        Text(
                            text = it,
                            modifier = Modifier.testTag("$testTag-error"),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.error
                        )
                    }
                    Button(
                        onClick = onCta,
                        enabled = ctaEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("$testTag-cta"),
                        shape = RoundedCornerShape(12.dp),
                        colors = if (ctaDanger) {
                            ButtonDefaults.buttonColors(containerColor = colors.error)
                        } else {
                            ButtonDefaults.buttonColors()
                        }
                    ) {
                        Text(cta, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        } else if (error != null) {
            Text(
                text = error,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .testTag("$testTag-error"),
                style = MaterialTheme.typography.labelSmall,
                color = colors.error
            )
        }
    }
}

/** "이건 이렇게 동작해요" 안내 상자 — 20-2a~f 의 `NoteBox` 와 같은 생김새다. */
@Composable
private fun GapNoteBox(lines: List<String>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            lines.forEach { line ->
                Text(
                    text = "· $line",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
internal fun GapFieldLabel(text: String, required: Boolean = false) {
    Text(
        text = if (required) "$text *" else text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.ExtraBold
    )
}

/**
 * 별 1~5 입력. 서버가 받는 값이 **정수 1~5** 라 반 개는 두지 않는다.
 *
 * 27-4 코스 평가와 27-1 매너 점수가 같은 것을 쓴다 — 두 자리에서 별이 다르게 생기면
 * 같은 뜻(1~5점)이라는 것이 읽히지 않는다.
 */
@Composable
internal fun MoyeoStarRating(
    score: Int,
    onScoreChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    starSize: Int = 40,
    enabled: Boolean = true,
    testTag: String? = null
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier.then(if (testTag == null) Modifier else Modifier.testTag(testTag)),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        (1..5).forEach { n ->
            Icon(
                // 안 고른 별은 **외곽선**이다. 채운 회색 별을 쓰면 진입 상태가 「5점 줬다」로
                // 읽힌다 — 기획·웹·iOS 셋 다 외곽선을 쓴다 (27-4, 사용자 지적 2026-09-09).
                imageVector = if (n <= score) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = "${n}점",
                modifier = Modifier
                    .size(starSize.dp)
                    .then(
                        if (testTag == null) Modifier else Modifier.testTag("$testTag-$n")
                    )
                    .clickable(enabled = enabled) { onScoreChange(n) },
                tint = if (n <= score) MoyeoStarOnTint else colors.outline
            )
        }
    }
}

/** 채워진 별 색. 디자인 토큰의 warning 에 해당한다. */
private val MoyeoStarOnTint = androidx.compose.ui.graphics.Color(0xFFF5A623)

/** 별점을 고르기 전에는 아무 말도 하지 않는다 — 기본 문구를 띄우면 이미 고른 것처럼 보인다. */
private val COURSE_RATING_WORDS =
    listOf("", "아쉬웠어요", "그저 그랬어요", "괜찮았어요", "좋았어요", "정말 좋았어요")

// ───────── 27-4 · 코스 평가 ─────────

/**
 * 27-4 코스 평가 — `POST /api/v1/travel-courses/chat-rooms/{roomId}/rating` `{ score: 1~5 }`.
 *
 * **이 화면이 없어서 14 코스 상세의 평점이 영원히 비어 있었다.** 코스 상세는
 * `averageRating`·`ratingCount` 를 그리는데, 그 값을 만드는 곳이 어디에도 없었다.
 *
 * 서버 조건은 "완료한 여행의 참가자" 다(그 외에는 400 40006). 조건을 클라이언트가 미리
 * 판정하지 않고, 서버가 준 메시지를 그대로 보여준다.
 */
@Composable
fun CourseRatingScreen(threadId: String?, onBack: () -> Unit) {
    val server = LocalServerData.current
    val roomId = threadId?.serverRoomIdOrNull()
    val scope = rememberCoroutineScope()
    var score by remember { mutableIntStateOf(0) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var room by remember(roomId, server) { mutableStateOf<ChatRoomDetail?>(null) }
    // 코스 사진·방문지 수·거리는 **코스 응답**에 있다 (방 상세에는 없다).
    // 예전에는 방 상세만 받아서 안드로이드 카드만 사진이 없고 「2026.08.28 · 1일」로 찍혔다
    // (27-4, 사용자 지적 2026-09-09).
    var course by remember(roomId, server) { mutableStateOf<TravelCourse?>(null) }

    LaunchedEffect(roomId, server) {
        if (server == null || roomId == null) return@LaunchedEffect
        room = runCatching { server.chatRooms.room(roomId) }.getOrNull()
        course = runCatching { server.courses.roomCourse(roomId) }.getOrNull()
    }

    GapScaffold(
        title = "코스 평가",
        onBack = onBack,
        testTag = "course-rating",
        cta = when {
            busy -> "보내는 중..."
            score == 0 -> "별점을 골라주세요"
            else -> "평가 남기기"
        },
        ctaEnabled = server != null && roomId != null && score > 0 && !busy,
        error = error ?: GAP_NO_ROOM_HINT.takeIf { roomId == null || server == null },
        onCta = {
            if (server == null || roomId == null) return@GapScaffold
            busy = true
            error = null
            scope.launch {
                runCatching { server.courses.rateRoomCourse(roomId, score) }
                    .onSuccess { onBack() }
                    .onFailure { error = it.message ?: "평가를 남기지 못했어요." }
                busy = false
            }
        }
    ) {
        Text(
            "이번 코스, 어떠셨어요?",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            "남겨주신 점수는 이 코스의 평균 별점에 반영돼요.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        // 무엇을 평가하는지 — 코스를 먼저 보여준다. 서버가 아직 안 주면 카드를 아예 그리지 않는다.
        room?.let { detail ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CachedRemoteImage(
                        url = course?.thumbnail ?: detail.thumbnail,
                        contentDescription = detail.courseTitle ?: detail.title,
                        modifier = Modifier.size(54.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop,
                        fallbackShape = MoyeoPlaceholderShape.SQUARE
                    ) {
                        Box(
                            Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            detail.courseTitle ?: detail.title,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.testTag("course-rating-course")
                        )
                        // 「2026.08.28 다녀옴 · 3곳 · 19.9km」 — 기획·iOS 와 같은 줄이다.
                        Text(
                            listOfNotNull(
                                detail.startDate.takeIf(String::isNotBlank)
                                    ?.replace('-', '.')
                                    ?.let { "$it 다녀옴" },
                                course?.places?.size?.takeIf { it > 0 }?.let { "${it}곳" },
                                course?.distanceKm?.let { "%.1fkm".format(it) }
                            ).joinToString(" · "),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        Box(Modifier.fillMaxWidth().padding(top = 14.dp), contentAlignment = Alignment.Center) {
            MoyeoStarRating(
                score = score,
                onScoreChange = { score = it },
                enabled = !busy,
                testTag = "course-rating-star"
            )
        }
        Text(
            text = COURSE_RATING_WORDS[score],
            modifier = Modifier.fillMaxWidth().testTag("course-rating-word"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        GapNoteBox(
            listOf(
                "점수만 남겨요. 글은 피드에 써주세요.",
                "누가 몇 점을 줬는지는 아무에게도 보이지 않아요.",
                "함께 간 사람들의 점수가 모여 코스 평균이 돼요."
            )
        )
    }
}

// ───────── 18-1 · 여행 확정 / 불발 (호스트) ─────────

/**
 * 18-1 여행 확정 / 불발 — `POST /api/v1/chat-rooms/{roomId}/status` `{ status: CONFIRMED | CANCELLED }`.
 *
 * 참가자가 결과를 보는 20-4 확정 모먼트는 있는데 **호스트가 그 버튼을 누르는 화면이 없었다** —
 * 확정이 사용자 조작 없이 저절로 일어나는 것처럼 그려져 있었다.
 *
 * 최소 인원(`minimumParticipants`)에 못 미치면 확정 CTA 를 잠근다. 서버가 그 값을 주지 않으면
 * 잠그지 않는다 — 임의의 하한을 지어내지 않는다.
 */
@Composable
fun TripStatusScreen(tripId: String, onBack: () -> Unit, onConfirmed: () -> Unit, onCancelled: () -> Unit) {
    val server = LocalServerData.current
    val roomId = tripId.serverRoomIdOrNull()
    val scope = rememberCoroutineScope()
    var room by remember(roomId, server) { mutableStateOf<ChatRoomDetail?>(null) }
    var pick by remember { mutableStateOf(RoomStatusChange.CONFIRMED) }
    var confirmCancel by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(roomId, server) {
        if (server == null || roomId == null) return@LaunchedEffect
        room = runCatching { server.chatRooms.room(roomId) }.getOrNull()
    }

    val approved = room?.participantCount
    val minimum = room?.minimumParticipants
    // 최소 인원을 서버가 주지 않으면 잠글 근거가 없다 — 그때는 잠그지 않는다.
    val enough = minimum == null || (approved != null && approved >= minimum)

    fun changeStatus(status: RoomStatusChange) {
        if (server == null || roomId == null) return
        busy = true
        error = null
        scope.launch {
            runCatching { server.chatRooms.changeStatus(roomId, status) }
                .onSuccess { if (status == RoomStatusChange.CONFIRMED) onConfirmed() else onCancelled() }
                .onFailure { error = it.message ?: "상태를 바꾸지 못했어요." }
            busy = false
        }
    }

    GapScaffold(
        title = "여행 확정하기",
        onBack = onBack,
        testTag = "trip-status",
        cta = when {
            busy -> "처리 중..."
            pick == RoomStatusChange.CANCELLED -> "모집 불발 처리"
            enough -> "여행 확정하기"
            else -> "최소 ${minimum}명이 필요해요"
        },
        ctaEnabled = server != null && roomId != null && !busy &&
            (pick == RoomStatusChange.CANCELLED || enough),
        ctaDanger = pick == RoomStatusChange.CANCELLED,
        error = error ?: GAP_NO_ROOM_HINT.takeIf { roomId == null || server == null },
        onCta = {
            // 불발은 되돌릴 수 없다 — 확인 단계를 한 번 더 지난다.
            if (pick == RoomStatusChange.CANCELLED) confirmCancel = true else changeStatus(pick)
        }
    ) {
        // 지금 몇 명인지 먼저 — 확정할지 접을지를 정하는 유일한 근거다
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Filled.Groups, null, tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        approved?.let { "승인된 동행자 ${it}명" } ?: MoyeoEmptyText.LOADING,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.testTag("trip-status-approved")
                    )
                    Text(
                        listOfNotNull(
                            minimum?.let { "최소 인원 ${it}명" },
                            room?.title
                        ).joinToString(" · "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (approved != null) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            if (enough) "확정 가능" else "인원 부족",
                            Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
        GapFieldLabel("어떻게 할까요?")
        TripStatusOption(
            selected = pick == RoomStatusChange.CONFIRMED,
            icon = Icons.Filled.Check,
            title = "여행 확정하기",
            description = "더 이상 신청을 받지 않고 이 인원으로 떠나요. 동행자 모두에게 알림이 가요.",
            danger = false,
            testTag = "trip-status-confirm"
        ) { pick = RoomStatusChange.CONFIRMED }
        TripStatusOption(
            selected = pick == RoomStatusChange.CANCELLED,
            icon = Icons.Filled.Close,
            title = "모집 불발 처리",
            description = "이번 여행을 접어요. 승인된 동행자와 대기 중인 신청자 모두에게 알림이 가요.",
            danger = true,
            testTag = "trip-status-cancel"
        ) { pick = RoomStatusChange.CANCELLED }
        GapNoteBox(
            if (pick == RoomStatusChange.CONFIRMED) {
                listOf(
                    "확정하면 모집이 닫혀요. 다시 열 수 없어요.",
                    "대기 중인 신청자에게는 마감 알림이 가요.",
                    "여행 날이 되면 채팅방에 진행 위젯이 열려요."
                )
            } else {
                listOf(
                    "불발 처리하면 채팅방이 닫혀요. 되돌릴 수 없어요.",
                    "같은 코스로 다시 모집을 열 수는 있어요.",
                    "이미 나눈 대화는 사라져요."
                )
            }
        )
    }

    if (confirmCancel) {
        GapConfirmSheet(
            title = "모집을 불발 처리할까요?",
            description = room?.title.orEmpty(),
            lines = listOf(
                "불발 처리하면 채팅방이 닫혀요. 되돌릴 수 없어요.",
                "같은 코스로 다시 모집을 열 수는 있어요.",
                "이미 나눈 대화는 사라져요."
            ),
            cancel = "그대로 둘게요",
            confirm = "모집 불발 처리",
            danger = true,
            testTag = "trip-status-cancel-confirm",
            onDismiss = { confirmCancel = false },
            onConfirm = {
                confirmCancel = false
                changeStatus(RoomStatusChange.CANCELLED)
            }
        )
    }
}

@Composable
private fun TripStatusOption(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    danger: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val accent = if (danger) colors.error else colors.primary
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) colors.surfaceVariant else colors.surface,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) accent else colors.outline)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, Modifier.size(22.dp), tint = accent)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    title,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (danger) colors.error else colors.onSurface
                )
                Text(
                    description,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant
                )
            }
            if (selected) Icon(Icons.Filled.Check, null, Modifier.size(18.dp), tint = accent)
        }
    }
}

// ───────── 18-2 · 집합 정보 수정 (호스트) ─────────

/**
 * 18-2 집합 정보 수정 — `PUT /api/v1/chat-rooms/{roomId}/meeting-info`
 * `{ meetingLatitude, meetingLongitude, meetingDetails, meetingDateTime }`.
 *
 * 17-3 집합 장소 지정은 모집 **만들기** 단계라 이미 만든 모집에는 쓸 수 없었다.
 * 좌표는 지금 방에 등록된 값을 그대로 다시 보낸다 — 지도에서 새 핀을 찍는 것은
 * 17-3 과 같은 장소 선택 화면이 필요해 이번 범위 밖이다(집합 안내·일시만 고친다).
 *
 * 상단에는 등록된 집합 좌표를 **실제 카카오 지도**에 핀으로 올린다(웹·iOS·기획과 같다) —
 * 좌표 문자열만 보여주면 호스트가 그 위치가 맞는지 확인할 길이 없다.
 *
 * 집합 일시는 **날짜·시간 피커**로 고른다. 이전에는 `2026-09-20T08:30:00` 을 그대로 입력창에 넣고
 * `서버가 받는 형식 그대로 적어요 (yyyy-MM-dd'T'HH:mm:ss)` 라고 안내했다 — 일반 사용자는 그 형식을
 * 모른다. ISO 문자열은 서버로 보낼 때만 만든다.
 */
@Composable
fun MeetingEditScreen(tripId: String, onBack: () -> Unit) {
    val server = LocalServerData.current
    val roomId = tripId.serverRoomIdOrNull()
    val scope = rememberCoroutineScope()
    var room by remember(roomId, server) { mutableStateOf<ChatRoomDetail?>(null) }
    var details by remember(roomId) { mutableStateOf("") }
    // 화면은 날짜·시간 값을 들고 있고, 서버로 보낼 때만 ISO 문자열로 바꾼다.
    var meetingAt by remember(roomId) { mutableStateOf<LocalDateTime?>(null) }
    var picker by remember(roomId) { mutableStateOf(MeetingPicker.None) }
    var loaded by remember(roomId) { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(roomId, server) {
        if (server == null || roomId == null) return@LaunchedEffect
        val detail = runCatching { server.chatRooms.room(roomId) }.getOrNull()
        room = detail
        details = detail?.meetingDetails.orEmpty()
        meetingAt = detail?.meetingDateTime?.let(::parseServerDateTimeOrNull)
        loaded = detail != null
    }

    GapScaffold(
        title = "집합 정보 수정",
        onBack = onBack,
        testTag = "meeting-edit",
        cta = if (busy) "저장 중..." else "집합 정보 저장",
        ctaEnabled = loaded && meetingAt != null && !busy,
        error = error ?: GAP_NO_ROOM_HINT.takeIf { roomId == null || server == null },
        onCta = {
            val chosen = meetingAt
            if (server == null || roomId == null || chosen == null) return@GapScaffold
            busy = true
            error = null
            scope.launch {
                runCatching {
                    server.chatRooms.updateMeetingInfo(
                        roomId,
                        MeetingInfoUpdate(
                            // 서버가 받는 형식은 여기서만 만든다 — 사용자는 이 문자열을 보지 않는다.
                            meetingDateTime = chosen.format(SERVER_DATE_TIME),
                            meetingLatitude = room?.meetingLatitude,
                            meetingLongitude = room?.meetingLongitude,
                            meetingDetails = details.trim().takeIf(String::isNotBlank)
                        )
                    )
                }
                    .onSuccess { onBack() }
                    .onFailure { error = it.message ?: "집합 정보를 저장하지 못했어요." }
                busy = false
            }
        }
    ) {
        // 등록된 집합 좌표를 실제 지도에 핀으로 올린다. 좌표가 없으면 지도 자리를 아예 만들지 않는다
        // (좌표를 지어내 지도를 그리면 "여기가 집합 장소"라고 거짓말하는 셈이다).
        val meetingPoint = room?.let { detail ->
            val latitude = detail.meetingLatitude
            val longitude = detail.meetingLongitude
            if (latitude != null && longitude != null) MoyeoLatLng(latitude, longitude) else null
        }
        if (meetingPoint != null) {
            KakaoMapView(
                center = meetingPoint,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("meeting-edit-map"),
                markers = listOf(
                    MapMarker(id = "meeting-point", position = meetingPoint, shape = MapMarkerShape.Pin)
                ),
                zoomLevel = 16,
                fallback = { fallbackModifier -> MapUnavailablePlaceholder(fallbackModifier) }
            )
        }
        GapFieldLabel("집합 장소")
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Filled.LocationOn,
                    null,
                    Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // 좌표가 없으면 좌표를 지어내지 않는다 — 등록된 적이 없다고 적는다.
                val point = room?.let { detail ->
                    val latitude = detail.meetingLatitude
                    val longitude = detail.meetingLongitude
                    if (latitude != null && longitude != null) "%.5f, %.5f".format(latitude, longitude) else null
                }
                Text(
                    point ?: "등록된 집합 좌표가 없어요.",
                    modifier = Modifier.weight(1f).testTag("meeting-edit-point"),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        GapFieldLabel("안내 문구")
        OutlinedTextField(
            value = details,
            onValueChange = { details = it.take(100) },
            modifier = Modifier.fillMaxWidth().testTag("meeting-edit-details"),
            placeholder = { Text("시외버스터미널 정문 앞") },
            singleLine = true,
            enabled = loaded && !busy
        )
        GapFieldLabel("집합 일시", required = true)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MeetingPickerField(
                icon = Icons.Filled.Event,
                text = meetingAt?.let(::formatMeetingDate) ?: "날짜 선택",
                filled = meetingAt != null,
                enabled = loaded && !busy,
                modifier = Modifier.weight(1f).testTag("meeting-edit-date"),
                onClick = { picker = MeetingPicker.Date }
            )
            MeetingPickerField(
                icon = Icons.Filled.Schedule,
                text = meetingAt?.let(::formatMeetingTime) ?: "시간 선택",
                filled = meetingAt != null,
                enabled = loaded && !busy,
                modifier = Modifier.weight(1f).testTag("meeting-edit-time"),
                onClick = { picker = MeetingPicker.Time }
            )
        }
        GapNoteBox(
            listOf(
                "고치면 동행자 모두에게 알림이 가요.",
                "고정 공지에 적어둔 집합 안내는 따로 고쳐주세요."
            )
        )
    }

    when (picker) {
        MeetingPicker.None -> Unit

        MeetingPicker.Date -> MeetingDateSheet(
            initial = meetingAt?.toLocalDate() ?: LocalDate.now(),
            onDismiss = { picker = MeetingPicker.None },
            onConfirm = { date ->
                meetingAt = LocalDateTime.of(date, meetingAt?.toLocalTime() ?: LocalTime.of(9, 0))
                picker = MeetingPicker.None
            }
        )

        MeetingPicker.Time -> MeetingTimeSheet(
            initial = meetingAt?.toLocalTime() ?: LocalTime.of(9, 0),
            onDismiss = { picker = MeetingPicker.None },
            onConfirm = { time ->
                meetingAt = LocalDateTime.of(meetingAt?.toLocalDate() ?: LocalDate.now(), time)
                picker = MeetingPicker.None
            }
        )
    }
}

/** 18-5 집합 일시 — 어느 피커를 띄웠는지. */
private enum class MeetingPicker { None, Date, Time }

/** 서버가 받는 집합 일시 형식. **사용자에게는 보여주지 않는다.** */
private val SERVER_DATE_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

/**
 * 서버가 준 집합 일시를 화면 값으로 읽는다.
 *
 * 서버는 `2026-09-20T08:30:00` 을 주지만 오프셋이 붙어 오는 경우도 있어 두 갈래로 읽는다.
 * 못 읽으면 값을 지어내지 않고 `null` 이다 — 피커가 "날짜 선택"으로 열린다.
 */
private fun parseServerDateTimeOrNull(raw: String): LocalDateTime? {
    val text = raw.trim().takeIf(String::isNotBlank) ?: return null
    return runCatching { LocalDateTime.parse(text) }.getOrNull()
        ?: runCatching { java.time.OffsetDateTime.parse(text).atZoneSameInstant(ZoneId.systemDefault()) }
            .getOrNull()
            ?.toLocalDateTime()
}

private fun formatMeetingDate(value: LocalDateTime) = "${value.year}. ${value.monthValue}. ${value.dayOfMonth}."

private fun formatMeetingTime(value: LocalDateTime): String {
    val morning = value.hour < 12
    val hour = when (value.hour % 12) {
        0 -> 12
        else -> value.hour % 12
    }
    return "%s %d:%02d".format(if (morning) "오전" else "오후", hour, value.minute)
}

/** 18-5 날짜·시간 칩. 누르면 피커가 열린다 — 사용자가 글자를 적어 넣는 칸이 아니다. */
@Composable
private fun MeetingPickerField(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    filled: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (filled) FontWeight.Bold else FontWeight.Normal,
                color = if (filled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeetingDateSheet(initial: LocalDate, onDismiss: () -> Unit, onConfirm: (LocalDate) -> Unit) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial.atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
    )
    val picked = state.selectedDateMillis?.let { millis ->
        java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneOffset.UTC).toLocalDate()
    }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("집합 날짜", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            DatePicker(state = state, modifier = Modifier.fillMaxWidth(), showModeToggle = false, title = null)
            MeetingSheetButtons(
                confirmEnabled = picked != null,
                onDismiss = onDismiss,
                onConfirm = { picked?.let(onConfirm) },
                confirmTag = "meeting-edit-date-confirm"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeetingTimeSheet(initial: LocalTime, onDismiss: () -> Unit, onConfirm: (LocalTime) -> Unit) {
    val state = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = false)
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("집합 시간", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            TimePicker(state = state)
            MeetingSheetButtons(
                confirmEnabled = true,
                onDismiss = onDismiss,
                onConfirm = { onConfirm(LocalTime.of(state.hour, state.minute)) },
                confirmTag = "meeting-edit-time-confirm"
            )
        }
    }
}

@Composable
private fun MeetingSheetButtons(
    confirmEnabled: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmTag: String
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f).height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) { Text("취소") }
        Button(
            onClick = onConfirm,
            enabled = confirmEnabled,
            modifier = Modifier.weight(1f).height(50.dp).testTag(confirmTag),
            shape = RoundedCornerShape(12.dp)
        ) { Text("선택 완료") }
    }
}

// ───────── 20-1c · 이 모임 알림 ─────────

/**
 * 20-1c 이 모임 알림 — `GET/PUT /api/v1/notifications/settings/chat-rooms/{roomId}`.
 *
 * 20-1 사이드 메뉴의 `알림 설정 · 이 모임의 알림만 끄기` 가 **전역 방해금지 화면(29-2)** 으로 갔다.
 * "이 모임만" 이라고 써놓고 계정 전체 설정을 열어주면 방 하나 끄려던 사람이 모든 알림을 끄게 된다.
 */
@Composable
fun RoomNotificationScreen(threadId: String?, onBack: () -> Unit, onOpenAppNotificationSettings: () -> Unit) {
    val server = LocalServerData.current
    val roomId = threadId?.serverRoomIdOrNull()
    val scope = rememberCoroutineScope()
    var room by remember(roomId, server) { mutableStateOf<ChatRoomDetail?>(null) }
    var enabled by remember(roomId, server) { mutableStateOf<Boolean?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(roomId, server) {
        if (server == null || roomId == null) return@LaunchedEffect
        room = runCatching { server.chatRooms.room(roomId) }.getOrNull()
        enabled = runCatching { server.notifications.roomSetting(roomId) }.getOrNull()?.enabled
    }

    GapScaffold(
        title = "이 모임 알림",
        onBack = onBack,
        testTag = "room-notif",
        error = error ?: GAP_NO_ROOM_HINT.takeIf { roomId == null || server == null }
    ) {
        room?.let { detail ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(detail.title, fontWeight = FontWeight.ExtraBold)
                    Text(
                        "동행자 ${detail.participantCount}명 · ${detail.startDate.replace('-', '.')} 출발",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        val current = enabled
        if (current == null) {
            MoyeoEmptyState(MoyeoEmptyText.LOADING)
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("이 모임 알림 받기", fontWeight = FontWeight.ExtraBold)
                    Text(
                        if (current) {
                            "새 메시지와 공지 알림을 받아요."
                        } else {
                            "이 모임의 알림만 꺼져요. 다른 모임과 앱 전체 알림은 그대로예요."
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = current,
                    onCheckedChange = { next ->
                        if (server == null || roomId == null) return@Switch
                        enabled = next
                        error = null
                        scope.launch {
                            runCatching { server.notifications.updateRoomSetting(roomId, next) }
                                .onSuccess { saved -> enabled = saved.enabled }
                                .onFailure {
                                    enabled = !next
                                    error = it.message ?: "알림 설정을 바꾸지 못했어요."
                                }
                        }
                    },
                    modifier = Modifier.testTag("room-notif-toggle")
                )
            }
        }
        GapNoteBox(
            listOf(
                "꺼도 채팅은 그대로 쌓여요. 들어가면 다 볼 수 있어요.",
                "앱 전체 알림과 방해 금지 시간대는 설정에서 따로 정해요."
            )
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenAppNotificationSettings)
                .testTag("room-notif-app-settings"),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Filled.Notifications, null, Modifier.size(16.dp))
                Text("앱 전체 알림 설정", Modifier.weight(1f), fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ───────── 20-3a · 공지 수정 · 삭제 ─────────

/**
 * 20-3a 공지 수정 · 삭제 — `PUT`/`DELETE /api/v1/chat-rooms/{roomId}/notices/{noticeId}`.
 *
 * 20-3 공지 이력의 카드마다 `수정` 이 있는데 갈 곳이 없었다.
 * 제목 칸은 없다 — 서버 모델이 `notice` 하나뿐이라 공지는 **본문만**이다(정본 §2).
 */
@Composable
fun NoticeEditScreen(tripId: String, noticeId: Long, onBack: () -> Unit) {
    val server = LocalServerData.current
    val roomId = tripId.serverRoomIdOrNull()
    val scope = rememberCoroutineScope()
    var notice by remember(roomId, noticeId) { mutableStateOf<RoomNotice?>(null) }
    var content by remember(roomId, noticeId) { mutableStateOf("") }
    var pinned by remember(roomId, noticeId) { mutableStateOf(false) }
    var loaded by remember(roomId, noticeId) { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(roomId, noticeId, server) {
        if (server == null || roomId == null) return@LaunchedEffect
        val found = runCatching { server.chatRooms.notices(roomId) }.getOrNull()
            ?.all
            ?.firstOrNull { it.noticeId == noticeId }
        notice = found
        content = found?.content.orEmpty()
        pinned = found?.pinned == true
        loaded = found != null
    }

    GapScaffold(
        title = "공지 수정",
        onBack = onBack,
        testTag = "notice-edit",
        cta = if (busy) "저장 중..." else "수정 저장",
        ctaEnabled = loaded && content.isNotBlank() && !busy,
        error = error ?: GAP_NO_ROOM_HINT.takeIf { roomId == null || server == null },
        onCta = {
            if (server == null || roomId == null) return@GapScaffold
            busy = true
            error = null
            scope.launch {
                runCatching {
                    server.chatRooms.updateNotice(roomId, noticeId, notice = content.trim(), pinned = pinned)
                }
                    .onSuccess { onBack() }
                    .onFailure { error = it.message ?: "공지를 저장하지 못했어요." }
                busy = false
            }
        }
    ) {
        GapFieldLabel("공지 내용", required = true)
        OutlinedTextField(
            value = content,
            onValueChange = { content = it.take(1000) },
            modifier = Modifier.fillMaxWidth().height(150.dp).testTag("notice-edit-content"),
            enabled = loaded && !busy,
            supportingText = { Text("${content.length}/1000") }
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("상단에 고정하기", fontWeight = FontWeight.ExtraBold)
                Text(
                    "채팅방 맨 위에 계속 보여요.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = pinned,
                onCheckedChange = { pinned = it },
                enabled = loaded && !busy,
                modifier = Modifier.testTag("notice-edit-pin")
            )
        }
        notice?.let { loadedNotice ->
            Text(
                "${loadedNotice.authorNickname} · ${loadedNotice.createdAt.take(10).replace('-', '.')} 작성",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // 삭제는 되돌릴 수 없다 — 저장 CTA 와 멀리 떼어 놓는다
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { confirmDelete = true },
            enabled = loaded && !busy,
            modifier = Modifier.fillMaxWidth().height(46.dp).testTag("notice-edit-delete"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text("이 공지 삭제하기", fontWeight = FontWeight.ExtraBold)
        }
        Text(
            "삭제하면 공지 이력에서도 사라져요. 되돌릴 수 없어요.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (confirmDelete) {
        GapConfirmSheet(
            title = "이 공지를 삭제할까요?",
            description = content.take(40),
            lines = listOf(
                "삭제하면 공지 이력에서도 사라져요. 되돌릴 수 없어요.",
                "동행자에게 따로 알리지 않아요."
            ),
            cancel = "그대로 둘게요",
            confirm = "공지 삭제",
            danger = true,
            testTag = "notice-delete-confirm",
            onDismiss = { confirmDelete = false },
            onConfirm = {
                confirmDelete = false
                if (server == null || roomId == null) return@GapConfirmSheet
                busy = true
                scope.launch {
                    runCatching { server.chatRooms.deleteNotice(roomId, noticeId) }
                        .onSuccess { onBack() }
                        .onFailure { error = it.message ?: "공지를 삭제하지 못했어요." }
                    busy = false
                }
            }
        )
    }
}

// ───────── 26-1 · 찜한 모집 ─────────

/**
 * 26-1 찜한 모집 — `GET /api/v1/chat-rooms/my/favorites`.
 *
 * 모집 상세 15와 탐색 10 카드에 하트가 있는데 **모아 보는 곳이 없었다.**
 * 마이 26 의 `찜한 코스` 는 **코스**이지 모집이 아니다 — 서로 다른 것이다.
 */
@Composable
fun FavoriteRoomsScreen(onBack: () -> Unit, onOpenRoom: (Long) -> Unit) {
    val server = LocalServerData.current
    var reloadKey by remember { mutableIntStateOf(0) }
    var rooms by remember(server) {
        mutableStateOf<ServerListState<ChatRoomSearchResult>>(ServerListState.Loading)
    }

    LaunchedEffect(server, reloadKey) {
        if (server == null) return@LaunchedEffect
        rooms = rooms.afterReload(runCatching { server.chatRooms.favoriteRooms() })
    }

    GapScaffold(title = "찜한 모집", onBack = onBack, testTag = "favorite-rooms") {
        when {
            server == null -> MoyeoEmptyState(
                MoyeoEmptyText.SIGN_IN_EXPLORE,
                testTag = "favorite-rooms-signed-out"
            )

            rooms is ServerListState.Loading -> MoyeoEmptyState(MoyeoEmptyText.LOADING)

            rooms is ServerListState.Failed -> MoyeoEmptyState(
                MoyeoEmptyText.FAILED,
                onRetry = { reloadKey++ }
            )

            else -> {
                val items = (rooms as ServerListState.Loaded).items
                if (items.isEmpty()) {
                    MoyeoEmptyState(MoyeoEmptyText.NO_ROOMS, testTag = "favorite-rooms-empty")
                } else {
                    items.forEach { room ->
                        SearchResultRoomCard(room = room, onClick = { onOpenRoom(room.roomId) })
                    }
                    Text(
                        "찜한 모집이 마감되거나 여행이 끝나도 목록에는 남아요. 하트를 다시 누르면 빠져요.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ───────── 13-2 · 내 강퇴 이력 ─────────

/**
 * 13-2 내보내진 기록 — `GET /api/v1/chat-rooms/my-kick-histories`.
 *
 * 13-1 내보내기 안내는 **알림 한 건**을 여는 화면이라, 알림이 사라지면 사유를 다시 볼 길이 없었다.
 */
@Composable
fun KickHistoryScreen(onBack: () -> Unit) {
    val server = LocalServerData.current
    var reloadKey by remember { mutableIntStateOf(0) }
    var rows by remember(server) {
        mutableStateOf<ServerListState<RoomKickHistory>>(ServerListState.Loading)
    }

    LaunchedEffect(server, reloadKey) {
        if (server == null) return@LaunchedEffect
        rows = rows.afterReload(runCatching { server.chatRooms.myKickHistories() })
    }

    GapScaffold(title = "내보내진 기록", onBack = onBack, testTag = "kick-history") {
        when {
            server == null -> MoyeoEmptyState(
                MoyeoEmptyText.SIGN_IN_EXPLORE,
                testTag = "kick-history-signed-out"
            )

            rows is ServerListState.Loading -> MoyeoEmptyState(MoyeoEmptyText.LOADING)

            rows is ServerListState.Failed -> MoyeoEmptyState(
                MoyeoEmptyText.FAILED,
                onRetry = { reloadKey++ }
            )

            else -> {
                val items = (rows as ServerListState.Loaded).items
                if (items.isEmpty()) {
                    MoyeoEmptyState(MoyeoEmptyText.NO_KICK_HISTORIES, testTag = "kick-history-empty")
                } else {
                    items.forEach { row ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("kick-history-${row.kickHistoryId}"),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(
                                Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(9.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.WarningAmber,
                                        null,
                                        Modifier.size(15.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Text(row.roomTitle, Modifier.weight(1f), fontWeight = FontWeight.Bold)
                                    Text(
                                        row.kickedAt.take(10).replace('-', '.'),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(row.reason, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                Text(
                    "내보낸 사유는 호스트가 직접 적은 글이에요. 부당하다고 느끼시면 고객센터로 알려주세요.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ───────── 되돌리기 어려운 행동을 묻는 시트 ─────────

/**
 * 19-2 · 29-1a · 27-2a 친구 끊기 · 20-3a 공지 삭제 · 18-1 불발이 **같은 골격**을 쓴다.
 * 각자 다른 모양이면 사용자가 "이건 아까 그거랑 다른 건가" 하고 멈칫한다.
 */
@Composable
internal fun GapConfirmSheet(
    title: String,
    description: String,
    lines: List<String>,
    cancel: String,
    confirm: String,
    danger: Boolean,
    testTag: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    busy: Boolean = false,
    background: @Composable () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    OverlayBackdrop(
        modifier = Modifier.testTag(testTag),
        scrimAlpha = .48f,
        onScrimClick = onDismiss,
        background = background
    ) {
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = colors.surface,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 10.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .background(colors.outline, RoundedCornerShape(50))
                )
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                if (description.isNotBlank()) {
                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
                if (lines.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    GapNoteBox(lines)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp).testTag("$testTag-cancel"),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text(cancel, fontWeight = FontWeight.Bold) }
                    Button(
                        onClick = onConfirm,
                        enabled = !busy,
                        modifier = Modifier.weight(1f).height(48.dp).testTag("$testTag-confirm"),
                        shape = RoundedCornerShape(12.dp),
                        colors = if (danger) {
                            ButtonDefaults.buttonColors(containerColor = colors.error)
                        } else {
                            ButtonDefaults.buttonColors()
                        }
                    ) { Text(confirm, fontWeight = FontWeight.ExtraBold) }
                }
            }
        }
    }
}

// ───────── 19-2 · 참가 신청 취소 확인 ─────────

/**
 * 19-2 참가 신청 취소 확인 — `DELETE /api/v1/chat-rooms/{roomId}/applications/me`.
 *
 * 19-1 참가 신청한 모임의 `신청 취소` 는 지금까지 **확인 없이 바로** 눌리는 자리였다.
 * 대기열 순번을 잃는 행동이라 되돌릴 수 없다.
 *
 * 대기 순번은 서버(`GET chat-rooms/my-waiting`)가 준 값만 적는다 — 못 받으면 그 줄이 사라진다.
 */
@Composable
fun ApplyCancelScreen(tripId: String, onBack: () -> Unit, onCancelled: () -> Unit) {
    val server = LocalServerData.current
    val roomId = tripId.serverRoomIdOrNull()
    val scope = rememberCoroutineScope()
    var waiting by remember(roomId, server) { mutableStateOf<MyWaitingRoom?>(null) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(roomId, server) {
        if (server == null || roomId == null) return@LaunchedEffect
        waiting = runCatching { server.chatRooms.myWaitingRooms() }.getOrNull()
            ?.firstOrNull { it.roomId == roomId }
    }

    GapConfirmSheet(
        title = "참가 신청을 취소할까요?",
        description = waiting?.title.orEmpty(),
        lines = listOfNotNull(
            waiting?.waitlistPosition?.let { "대기 순번 ${it}번이 사라져요. 다시 신청하면 맨 뒤부터예요." }
                ?: "다시 신청하면 맨 뒤부터예요.",
            "호스트에게는 따로 알리지 않아요.",
            error
        ),
        cancel = "그대로 둘게요",
        confirm = if (busy) "취소하는 중..." else "신청 취소",
        danger = true,
        testTag = "apply-cancel",
        busy = busy || server == null || roomId == null,
        // 시트 뒤에는 19-1 신청한 모임 목록이 딤 아래 깔려 있어야 한다 — 뒤가 검으면 어느 화면에서
        // 열린 시트인지 알 수 없다(기획·웹·iOS 모두 이 구조다).
        background = { MeetingsScreen(onOpenRoom = {}, initialTab = MeetingChatTab.Applied) },
        onDismiss = onBack,
        onConfirm = {
            if (server == null || roomId == null) return@GapConfirmSheet
            busy = true
            error = null
            scope.launch {
                runCatching { server.chatRooms.cancelApplication(roomId) }
                    .onSuccess { onCancelled() }
                    .onFailure { error = it.message ?: "신청을 취소하지 못했어요." }
                busy = false
            }
        }
    )
}

// ───────── 29-1a · 차단 해제 확인 ─────────

/** 29-1a 차단 해제 확인 — `DELETE /api/v1/users/me/blocks/{userId}`. 29-1 의 `차단 해제` 에서 온다. */
@Composable
fun UnblockConfirmScreen(userId: Long, nickname: String, onBack: () -> Unit, onUnblocked: () -> Unit) {
    val server = LocalServerData.current
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    GapConfirmSheet(
        title = "차단을 풀까요?",
        description = nickname,
        lines = listOfNotNull(
            "차단을 풀면 이 사람이 올린 모집과 피드가 다시 보여요.",
            "이 사람도 회원님의 모집과 피드를 볼 수 있어요.",
            "언제든 다시 차단할 수 있어요.",
            error
        ),
        cancel = "그대로 둘게요",
        confirm = if (busy) "푸는 중..." else "차단 해제",
        danger = false,
        testTag = "unblock-confirm",
        busy = busy || server == null,
        // 시트 뒤에는 29-1 차단한 사용자 목록이 깔려 있다
        background = { BlockedUsersScreen(onBack = {}) },
        onDismiss = onBack,
        onConfirm = {
            if (server == null) return@GapConfirmSheet
            busy = true
            error = null
            scope.launch {
                runCatching { server.social.unblock(userId) }
                    .onSuccess { onUnblocked() }
                    .onFailure { error = it.message ?: "차단을 풀지 못했어요." }
                busy = false
            }
        }
    )
}

// ───────── 27-2a · 친구 정리 (친구 끊기) ─────────

/**
 * 27-2a 친구 정리 — `DELETE /api/v1/users/me/friends/{friendUserId}`.
 *
 * 27-2 친구 관리 '내 친구' 행 우측의 ⋯ 는 눌러도 아무 일이 없었다.
 * 친구를 끊는 것은 되돌리기 어렵다 — 도감 카드까지 함께 사라진다.
 */
@Composable
fun FriendManageScreen(
    userId: Long,
    nickname: String,
    subtitle: String,
    onBack: () -> Unit,
    onOpenProfile: (Long) -> Unit,
    onRemoved: () -> Unit
) {
    val server = LocalServerData.current
    val scope = rememberCoroutineScope()
    var confirmRemove by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val colors = MaterialTheme.colorScheme

    // 헤더는 **서버 친구 목록**이 근거다 — 라우트 인자만 믿으면(캡처 진입은 인자가 비어 있다)
    // 이미지·닉네임·부제가 통째로 빈 시트가 그려진다. 프로필 이미지가 있으면 이모지를 쓰지 않는다(R5).
    var friend by remember(server, userId) { mutableStateOf<Friend?>(null) }
    LaunchedEffect(server, userId) {
        if (server == null) return@LaunchedEffect
        val friends = runCatching { server.social.friends() }.getOrNull().orEmpty()
        // 대상을 지정하지 않고 들어오는 진입(캡처 라우트)에서는 목록의 첫 친구를 쓴다 —
        // 값을 지어내는 게 아니라 실제 친구 목록에서 고르는 것이다. 친구가 없으면 그대로 비어 있다.
        friend = friends.firstOrNull { it.user.userId == userId }
            ?: friends.firstOrNull().takeIf { userId <= 0L }
    }
    val shownNickname = friend?.user?.nickname?.takeIf(String::isNotBlank) ?: nickname
    // 부제는 iOS 와 같은 근거다 — 친구 목록의 `lastActive`(`5일 전 접속`). 없으면 소개글, 그다음 라우트 인자.
    val shownSubtitle = friend?.lastActive?.takeIf(String::isNotBlank)?.let { "$it 접속" }
        ?: friend?.user?.introduction?.takeIf(String::isNotBlank)
        ?: subtitle

    OverlayBackdrop(
        modifier = Modifier.testTag("friend-manage"),
        scrimAlpha = .48f,
        onScrimClick = onBack,
        // 시트 뒤에는 27-2 친구 관리 목록이 딤 아래 깔려 있어야 한다
        background = { FriendsScreen(onBack = {}, onOpenDex = {}) }
    ) {
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = colors.surface,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 24.dp)
            ) {
                Box(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 12.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .background(colors.outline, RoundedCornerShape(50))
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(11.dp)
                ) {
                    UserAvatar(
                        imageUrl = friend?.user?.profileImageUrl,
                        nickname = shownNickname,
                        modifier = Modifier.size(44.dp),
                        fallbackFontSize = 20.sp
                    )
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(shownNickname, fontWeight = FontWeight.Bold)
                        if (shownSubtitle.isNotBlank()) {
                            Text(
                                shownSubtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                }
                HorizontalDivider(color = colors.outline.copy(alpha = .45f))
                TextButton(
                    onClick = { onOpenProfile(userId) },
                    modifier = Modifier.fillMaxWidth().height(54.dp).testTag("friend-manage-profile")
                ) {
                    // 아이콘은 기획·iOS 와 같은 자리·같은 뜻이다 (카드 · 오른쪽 화살표).
                    // 예전에는 글자만 있어서 안드로이드만 줄이 비어 보였다.
                    Icon(
                        Icons.Filled.ContactPage,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = colors.onSurfaceVariant
                    )
                    Text(
                        "프로필 카드 보기",
                        Modifier.weight(1f).padding(start = 10.dp),
                        color = colors.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = colors.onSurfaceVariant
                    )
                }
                HorizontalDivider(color = colors.outline.copy(alpha = .45f))
                TextButton(
                    onClick = { confirmRemove = true },
                    enabled = server != null && !busy,
                    modifier = Modifier.fillMaxWidth().height(54.dp).testTag("friend-manage-remove")
                ) {
                    Icon(
                        Icons.Filled.PersonRemove,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = colors.error
                    )
                    Text(
                        "친구 끊기",
                        Modifier.weight(1f).padding(start = 10.dp),
                        color = colors.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    "친구를 끊으면 서로의 도감 카드에서도 빠져요. 다시 친구가 되면 카드도 돌아와요.",
                    modifier = Modifier.padding(top = 6.dp, bottom = 14.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant
                )
                error?.let {
                    Text(
                        it,
                        modifier = Modifier.padding(bottom = 10.dp).testTag("friend-manage-error"),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.error
                    )
                }
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("닫기", fontWeight = FontWeight.Bold) }
            }
        }
    }

    if (confirmRemove) {
        GapConfirmSheet(
            title = "친구를 끊을까요?",
            description = nickname,
            lines = listOf(
                "친구를 끊으면 서로의 도감 카드에서도 빠져요. 다시 친구가 되면 카드도 돌아와요.",
                "상대방에게는 따로 알리지 않아요."
            ),
            cancel = "그대로 둘게요",
            confirm = if (busy) "끊는 중..." else "친구 끊기",
            danger = true,
            testTag = "friend-remove-confirm",
            busy = busy,
            onDismiss = { confirmRemove = false },
            onConfirm = {
                confirmRemove = false
                if (server == null) return@GapConfirmSheet
                busy = true
                error = null
                scope.launch {
                    runCatching { server.social.removeFriend(userId) }
                        .onSuccess { onRemoved() }
                        .onFailure { error = it.message ?: "친구를 끊지 못했어요." }
                    busy = false
                }
            }
        )
    }
}

// ───────── 31 · 채팅방 나가기 (호스트 / 참가자) ─────────

/**
 * 31 채팅방 나가기 — `DELETE /api/v1/chat-rooms/{roomId}/members/me`.
 *
 * **호스트와 참가자는 결과가 전혀 다르다.** 호스트가 나가면 모임이 없어지고,
 * 참가자가 나가면 자리가 하나 비어 대기자가 들어온다. 문구가 호스트 기준으로 고정돼 있어
 * 참가자가 이 화면을 보면 **자기가 모임을 없애는 것**으로 읽혔다(정본 §6-5).
 */
internal data class LeaveCopy(val title: String, val description: String, val confirm: String) {
    companion object {
        fun of(host: Boolean): LeaveCopy = if (host) {
            LeaveCopy(
                title = "호스트가 나가면\n이 모임은 종료돼요",
                description = "승인된 동행자 모두에게 알림이 가고, 채팅방은 14일 동안 읽기 전용으로 유지된 후 사라져요.",
                confirm = "모임 종료"
            )
        } else {
            LeaveCopy(
                title = "이 모임에서\n나갈까요?",
                description = "내 자리가 비면서 대기 중인 다음 신청자가 자동으로 합류해요. 다시 신청하면 맨 뒤부터예요.",
                confirm = "나가기"
            )
        }
    }
}

/** 접근성 낭독에서 줄바꿈이 끊겨 읽히지 않도록 한 줄로 합친다. */
internal fun LeaveCopy.spokenTitle(): String = title.replace("\n", " ")

// ───────── 27-1 · 여행 마무리 매너 점수 (별 1~5) ─────────

/**
 * 27-1 여행 마무리의 매너 점수 한 줄 — `PUT /api/v1/chat-rooms/{id}/companions/{userId}/review`
 * 의 `mannerScore` (1~5, 필수).
 *
 * **이 입력이 없어서 앱 곳곳의 `매너 4.7` 을 만드는 사람이 아무도 없었다.**
 * 점수를 고르기 전에는 `눌러서 매겨요` 라고만 적는다 — 기본 점수를 채워두면 이미 매긴 것처럼 보인다.
 */
@Composable
internal fun MannerScoreRow(score: Int, enabled: Boolean, onScoreChange: (Int) -> Unit, testTag: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "매너 점수",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        MoyeoStarRating(
            score = score,
            onScoreChange = onScoreChange,
            starSize = 22,
            enabled = enabled,
            testTag = testTag
        )
        Spacer(Modifier.weight(1f))
        Text(
            if (score > 0) "${score}점" else "눌러서 매겨요",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
