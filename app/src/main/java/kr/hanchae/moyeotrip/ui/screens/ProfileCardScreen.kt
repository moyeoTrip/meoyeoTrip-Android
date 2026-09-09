package kr.hanchae.moyeotrip.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.launch
import kr.hanchae.moyeotrip.data.profile.ServerPublicProfile
import kr.hanchae.moyeotrip.data.profile.ServerReceivedTravelReview
import kr.hanchae.moyeotrip.data.social.DexCompanion
import kr.hanchae.moyeotrip.data.social.DexMemory
import kr.hanchae.moyeotrip.ui.LocalServerData
import kr.hanchae.moyeotrip.ui.components.CachedRemoteImage
import kr.hanchae.moyeotrip.ui.components.LocalCaptureMode
import kr.hanchae.moyeotrip.ui.components.MOYEO_CTA_HEIGHT
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyText
import kr.hanchae.moyeotrip.ui.components.MoyeoNicknameAnimal
import kr.hanchae.moyeotrip.ui.theme.MoyeoUserCardPalette
import kr.hanchae.moyeotrip.ui.theme.rememberUserCardPalette

/**
 * 25 · 프로필 카드
 *
 * 카드 자체가 그 유저의 프로필이다. 도감·피드 작성자·멤버 시트·친구 목록 등
 * "유저를 눌러 자세히 보는" 모든 진입점이 이 화면으로 온다.
 *
 * 진입점마다 서버가 주는 필드가 다르다. 카드는 받은 값만 채우고 없는 슬롯은 지운다.
 *   GET /users/{userId}/profile        닉네임·이미지·색상·소개·취향·평균 매너
 *   GET /users/{userId}/travel-reviews 다른 여행자들이 남긴 평가 (여행 제목·시각 없음)
 *   도감 응답                          나와 동행한 횟수·함께한 여행·내가 남긴 메시지
 * 전체 여행/호스트/피드 횟수는 어떤 응답에도 없다 — 칸을 지운다(지어내지 않는다).
 */
@Composable
fun ProfileCardScreen(
    userId: Long?,
    dexCompanion: DexCompanion?,
    onBack: () -> Unit,
    // 25-1 캡처는 뒤집힌 상태로 진입한다. 실사용 기본값은 앞면이다.
    initialFlipped: Boolean = false
) {
    val server = LocalServerData.current
    val friendScope = rememberCoroutineScope()
    var friendRequestState by remember(server, userId) {
        mutableStateOf(
            if (server == null || userId == null) {
                ProfileFriendRequestState.Unavailable
            } else {
                ProfileFriendRequestState.Idle
            }
        )
    }
    var friendRequestError by remember(server, userId) { mutableStateOf<String?>(null) }
    // 자기에게 친구 신청을 걸 수는 없다 — 내 카드에서는 그 버튼을 그리지 않는다.
    val isMe = remember(server, userId) {
        val mine = server?.signedInUserId?.invoke()
        userId != null && mine != null && userId == mine
    }
    var profile by remember(server, userId) { mutableStateOf<ServerPublicProfile?>(null) }
    var reviews by remember(server, userId) { mutableStateOf<List<ServerReceivedTravelReview>>(emptyList()) }
    // 도감(27)에서 들어오면 동행 정보가 `dexCompanion` 으로 함께 온다. 그런데 **유저 id 만 아는
    // 진입점**(피드 작성자 · 멤버 · 캡처 라우트)에서는 그 값이 없어 「N회 동행」 배지와
    // 「최근 동행」 줄, 뒷면의 한 줄 메시지가 통째로 사라졌다 — 같은 화면이 들어온 길에 따라
    // 달라 보였다 (25, 사용자 지적 2026-09-09). 도감 응답에 그 값이 다 있으니 찾아 쓴다.
    var foundDexCompanion by remember(server, userId) { mutableStateOf<DexCompanion?>(null) }
    LaunchedEffect(server, userId) {
        if (server == null || userId == null) {
            profile = null
            reviews = emptyList()
            return@LaunchedEffect
        }
        profile = runCatching { server.userProfile.publicProfile(userId) }.getOrNull()
        reviews = runCatching { server.userProfile.receivedTravelReviews(userId) }.getOrEmpty()
        if (dexCompanion == null) {
            // 도감에 없으면 나와 동행한 적이 없는 사람이다 — 그 칸은 비워 둔다.
            foundDexCompanion = runCatching { server.social.travelDex() }
                .getOrNull()
                ?.firstOrNull { it.userId == userId }
        }
    }
    val companion = dexCompanion ?: foundDexCompanion

    val subject = profile?.let { loaded ->
        ProfileCardSubject(
            nickname = loaded.nickname,
            nicknameColor = loaded.nicknameColor,
            profileImageUrl = loaded.profileImageUrl,
            introduction = loaded.introduction,
            travelStyles = loaded.travelStyles.map { it.label },
            mannerRating = loaded.mannerRating,
            // 여행·피드 횟수는 서버가 준다 (`completedTripCount` · `feedCount`).
            completedTripCount = loaded.completedTripCount,
            feedCount = loaded.feedCount,
            withMeTripCount = companion?.tripCount,
            latestTripTitle = companion?.latestTripTitle,
            latestTripDate = companion?.latestTripDate,
            memories = companion?.memories.orEmpty(),
            receivedReviews = reviews.map { ReceivedReview(it.reviewerNickname, it.reviewerNicknameColor, it.content) }
        )
    }

    if (subject == null) {
        // 라이브에서 아직 못 받았거나 실패한 상태. 값을 지어내지 않고 비워 둔다.
        ProfileCardPlaceholder(onBack = onBack)
        return
    }

    ProfileCardBody(
        subject = subject,
        onBack = onBack,
        initialFlipped = initialFlipped,
        friendRequestState = friendRequestState,
        friendRequestError = friendRequestError,
        isMe = isMe,
        onSendFriendRequest = {
            // userId 를 모르면 보낼 곳이 없다 — 버튼은 이미 잠겨 있고, 여기서도 아무 일도 하지 않는다.
            if (server != null && userId != null && friendRequestState == ProfileFriendRequestState.Idle) {
                friendRequestState = ProfileFriendRequestState.Sending
                friendRequestError = null
                friendScope.launch {
                    runCatching { server.social.sendRequest(userId) }
                        .onSuccess { friendRequestState = ProfileFriendRequestState.Sent }
                        .onFailure { error ->
                            friendRequestState = ProfileFriendRequestState.Idle
                            // 문구는 웹 20-1a 와 같은 것을 쓴다
                            friendRequestError = error.message ?: "친구 요청을 보내지 못했어요."
                        }
                }
            }
        }
    )
}

internal data class ReceivedReview(val nickname: String, val nicknameColor: String?, val content: String)

internal data class ProfileCardSubject(
    val nickname: String,
    val nicknameColor: String?,
    val profileImageUrl: String?,
    val introduction: String?,
    val travelStyles: List<String>,
    val mannerRating: Double?,
    val completedTripCount: Int?,
    // 호스트 횟수는 기획에 없다 — 서버도 주지 않고 칸도 만들지 않는다(2026-08-26 확정).
    val feedCount: Int?,
    val withMeTripCount: Int?,
    val latestTripTitle: String?,
    val latestTripDate: String?,
    val memories: List<DexMemory>,
    val receivedReviews: List<ReceivedReview>
)

private fun <T> Result<List<T>>.getOrEmpty(): List<T> = getOrNull().orEmpty()

/**
 * 카드의 세 단계.
 *
 *   REST   손을 뗀 뒤 제자리로 — 천천히(900ms)
 *   ENTER  평평한 상태에서 누른 자리까지 — 짧게(260ms). 없으면 처음 누를 때 각도가 튄다.
 *   FOLLOW 손가락을 따라가는 중 — 애니메이션 없음. 걸어두면 계속 뒤따라온다.
 */
private enum class CardPhase { REST, ENTER, FOLLOW }

private val RestEasing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
private val EnterEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

/** 25 "친구 신청" 버튼의 상태. `Unavailable` 은 userId 없이 열린 카드(캡처 진입)다. */
internal enum class ProfileFriendRequestState { Idle, Sending, Sent, Unavailable }

@Composable
private fun ProfileCardBody(
    subject: ProfileCardSubject,
    onBack: () -> Unit,
    initialFlipped: Boolean,
    friendRequestState: ProfileFriendRequestState,
    friendRequestError: String?,
    onSendFriendRequest: () -> Unit,
    // / 내 카드인지 — 자기에게 친구 신청을 걸 수는 없어 그 버튼을 그리지 않는다.
    isMe: Boolean
) {
    val palette = rememberUserCardPalette(subject.nicknameColor)
    var phase by remember { mutableStateOf(CardPhase.REST) }
    var pointer by remember { mutableStateOf(Offset(0.5f, 0.5f)) }
    var pressed by remember { mutableStateOf(false) }
    // 뒤집힘을 **부호 있는 반회전 수**로 센다 — 부호가 방향이다. `Boolean` 이던 동안에는
    // 어느 쪽으로 밀어도 늘 같은 방향으로 돌았다.
    var flipTurns by remember { mutableIntStateOf(if (initialFlipped) 1 else 0) }

    val spec = when (phase) {
        CardPhase.FOLLOW -> snap<Float>()
        CardPhase.ENTER -> tween(durationMillis = 260, easing = EnterEasing)
        CardPhase.REST -> tween(durationMillis = 900, easing = RestEasing)
    }
    // 중앙 기준 ±10도. 값이 커지면 글씨가 읽기 어려워진다.
    val rotateX by animateFloatAsState(if (pressed) (0.5f - pointer.y) * 20f else 0f, spec, label = "cardRotateX")
    val rotateY by animateFloatAsState(if (pressed) (pointer.x - 0.5f) * 20f else 0f, spec, label = "cardRotateY")
    val scale by animateFloatAsState(if (pressed) 1.02f else 1f, spec, label = "cardScale")
    val shine by animateFloatAsState(if (pressed) 0.9f else 0.62f, spec, label = "cardShine")
    val holoAngle by animateFloatAsState(105f + (pointer.x - 0.5f) * 90f, spec, label = "holoAngle")
    val holoShift by animateFloatAsState(if (pressed) pointer.x else 0.5f, spec, label = "holoShift")
    val flipRotation by animateFloatAsState(
        targetValue = flipTurns * 180f,
        animationSpec = tween(durationMillis = 700, easing = RestEasing),
        label = "cardFlip"
    )

    val density = LocalDensity.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CompactMenuHeader(title = "프로필", onBack = onBack)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(318.dp)
                    .graphicsLayer {
                        rotationX = rotateX
                        rotationY = rotateY
                        scaleX = scale
                        scaleY = scale
                        cameraDistance = 14f * density.density
                    }
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            val start = down.position
                            // 평평한 상태에서 처음 누른 순간은 짧게 기울여 들어간다.
                            phase = CardPhase.ENTER
                            pressed = true
                            pointer = Offset(
                                (start.x / size.width).coerceIn(0f, 1f),
                                (start.y / size.height).coerceIn(0f, 1f)
                            )
                            var moved = Offset.Zero
                            var settled = false
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (change.pressed) {
                                    moved = change.position - start
                                    // 진입 애니메이션이 끝난 뒤부터는 손가락을 즉시 따라간다.
                                    if (!settled && (abs(moved.x) > 6f || abs(moved.y) > 6f)) {
                                        settled = true
                                        phase = CardPhase.FOLLOW
                                    }
                                    pointer = Offset(
                                        (change.position.x / size.width).coerceIn(0f, 1f),
                                        (change.position.y / size.height).coerceIn(0f, 1f)
                                    )
                                    change.consume()
                                } else {
                                    break
                                }
                            }
                            // 가로로 크게 그었으면 뒤집는다. 세로 스크롤과 충돌하지 않게 가로가 더 커야 한다.
                            val flipThreshold = with(density) { 40.dp.toPx() }
                            if (abs(moved.x) > flipThreshold && abs(moved.x) > abs(moved.y)) {
                                // 스와이프한 방향으로 돈다 — 왼쪽으로 밀면 왼쪽으로 넘어간다.
                                flipTurns += if (moved.x < 0f) -1 else 1
                            }
                            phase = CardPhase.REST
                            pressed = false
                        }
                    }
            ) {
                // 카드 뒤 번짐 — 지정색을 흐리게 깔아 프로필 이미지 배경과 층을 벌린다.
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(top = 6.dp)
                        .blur(22.dp)
                        .background(palette.glow.copy(alpha = 0.28f + shine * 0.2f), RoundedCornerShape(26.dp))
                )

                Box(
                    modifier = Modifier.graphicsLayer {
                        rotationY = flipRotation
                        cameraDistance = 14f * density.density
                    }
                ) {
                    // 두 면을 항상 함께 컴포즈해 **같은 크기**를 갖게 한다 — 웹이 뒷면을
                    // `position:absolute; inset:0` 로 앞면 위에 겹치는 것과 같은 구조다.
                    // 한 면씩만 그리면 뒷면이 내용만큼만 작아져 뒤집을 때 카드 크기가 튄다.
                    // Compose 에는 backface-visibility 가 없어 보이지 않는 면은 alpha 로 감춘다.
                    // 90~270도 구간에서는 뒷면이 앞을 향한다. 음수 회전(왼쪽으로 넘김)과
                    // 두 바퀴 이상도 같게 다루려면 각도를 0~360 으로 정규화해야 한다.
                    val facingBack = ((flipRotation % 360f) + 360f) % 360f in 90f..270f
                    Box(
                        modifier = Modifier.graphicsLayer { alpha = if (facingBack) 0f else 1f }
                    ) {
                        CardFront(
                            subject = subject,
                            palette = palette,
                            shine = shine,
                            angle = holoAngle,
                            shift = holoShift
                        )
                    }
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .graphicsLayer {
                                rotationY = 180f
                                alpha = if (facingBack) 1f else 0f
                            }
                    ) {
                        CardBack(subject = subject, palette = palette)
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 스와이프로도 뒤집히지만, 뒤집을 수 있다는 걸 알 방법이 필요해 버튼도 둔다.
            // 버튼에는 방향이 없다 — 오른쪽으로 돈다.
            TextButton(onClick = { flipTurns += 1 }) {
                // 문구를 고정한다 — 누를 때마다 이름이 바뀌면 무엇을 누르는 버튼인지 매번 다시 읽어야 한다.
                Text(text = "카드 뒤집기", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            // DM 기획이 없다 — 여기서 할 수 있는 행동은 친구 신청뿐이다.
            // POST users/me/friend-requests/{userId}. 라벨은 20-1a 멤버 액션과 같은 것을 쓴다.
            // 모서리를 **직접 준다**. Material3 `Button` 의 기본 모양은 완전한 알약이라
            // 기획·웹·iOS 의 12dp 둥근 사각형과 달라 안드로이드만 좌우가 다 둥글게 찍혔다
            // (25-1, 사용자 지적 2026-09-09).
            if (!isMe) {
                Button(
                    onClick = onSendFriendRequest,
                    modifier = Modifier.weight(1f).height(MOYEO_CTA_HEIGHT).testTag("profile-card-friend-request"),
                    enabled = friendRequestState == ProfileFriendRequestState.Idle,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = when (friendRequestState) {
                            ProfileFriendRequestState.Idle -> "친구 신청"

                            ProfileFriendRequestState.Sending -> "보내는 중..."

                            ProfileFriendRequestState.Sent -> "친구 요청을 보냈어요"

                            // 누구인지 모르면(userId 없이 열린 카드) 보낼 곳이 없다 — 왜 못 누르는지 적는다
                            ProfileFriendRequestState.Unavailable -> "친구 신청"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        friendRequestError?.let { message ->
            Text(
                text = message,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
                    .testTag("profile-card-friend-request-error"),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * 홀로그램 배경 브러시.
 *
 * 무지개 띠는 프로필 이미지 "뒤"에 깐다. 이미지 위에 얹으면 두 테마를 동시에 만족시킬 수 없다 —
 * 다크에서는 그림을 덮고, 라이트에서는 반투명 색조가 거의 보이지 않는다.
 * 실제 홀로 카드도 그림이 홀로그램 판 위에 올라가 있다.
 */
private fun holoBrush(angleDegrees: Float, shift: Float, sizePx: Float): Brush {
    val radians = Math.toRadians(angleDegrees.toDouble())
    // 띠 하나의 길이. 카드 폭의 0.9배 정도가 화면기획의 320% 배경과 비슷하게 보인다.
    val span = sizePx * 0.9f
    val dx = (cos(radians) * span).toFloat()
    val dy = (sin(radians) * span).toFloat()
    val origin = sizePx * (shift - 0.5f) * 0.6f
    return Brush.linearGradient(
        colorStops = arrayOf(
            0.04f to Color(0x57FF7773),
            0.12f to Color(0x4DFFED8C),
            0.20f to Color(0x47A8FF96),
            0.28f to Color(0x4D83F0F7),
            0.36f to Color(0x528CA0FF),
            0.44f to Color(0x4DD88CFF),
            0.50f to Color(0x38FFFFFF),
            0.56f to Color(0x57FF7773)
        ),
        start = Offset(origin, origin),
        end = Offset(origin + dx, origin + dy),
        tileMode = TileMode.Repeated
    )
}

@Composable
private fun CardFront(
    subject: ProfileCardSubject,
    palette: MoyeoUserCardPalette,
    shine: Float,
    angle: Float,
    shift: Float
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    0f to palette.background,
                    0.46f to MaterialTheme.colorScheme.surface,
                    1f to MaterialTheme.colorScheme.surface
                )
            )
            .border(2.dp, palette.border, RoundedCornerShape(18.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 9.dp, bottom = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = subject.nickname,
                modifier = Modifier.weight(1f),
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            subject.withMeTripCount?.let { count ->
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(palette.chipContainer)
                        .border(1.dp, palette.frame, RoundedCornerShape(999.dp))
                        .padding(horizontal = 9.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(text = "$count", fontSize = 14.sp, fontWeight = FontWeight.Black, color = palette.chipContent)
                    Text(
                        text = "회 동행",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = palette.chipContent
                    )
                }
            }
        }

        // 프로필 이미지는 1:1 이므로 정사각형 프레임에 꽉 채운다.
        Box(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(palette.plate)
                .border(2.dp, palette.frame, RoundedCornerShape(12.dp))
                // 홀로그램은 프로필 이미지 "위"에 얹는다. 뒤에 깔면 실제 이미지가
                // 정사각형을 꽉 채워 완전히 가려진다(실서버 이미지로 확인).
                .drawWithContent {
                    drawContent()
                    // 그림 위에 얹으므로 알파를 낮춘다. 0.62·0.9 에 0.65 를 곱한 값이다.
                    drawRect(brush = holoBrush(angle, shift, size.width), alpha = shine * 0.65f)
                },
            contentAlignment = Alignment.Center
        ) {
            CachedRemoteImage(
                url = subject.profileImageUrl,
                contentDescription = "${subject.nickname} 프로필 이미지",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            ) {
                // 이미지가 없으면 닉네임에서 뽑은 동물 이모지를 크게 보여준다.
                Text(text = MoyeoNicknameAnimal.emojiForNickname(subject.nickname), fontSize = 120.sp)
            }
        }

        Column(
            modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 13.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            // 횟수 지표 — 서버가 준 값만 칸을 만든다. 지어낸 숫자를 채우지 않는다.
            // 매너 점수는 스트립에 넣지 않는다. 단위가 다르고(점 vs 회), 매너만 내려오는
            // 진입점에서는 한 칸짜리 전체폭 스트립이 남아 어색했다.
            subject.mannerRating?.let {
                ProfileCardMetaRow(label = "매너 점수", value = "${formatRating(it)}점")
            }
            val stats = buildList {
                subject.completedTripCount?.let { add("여행" to "$it") }
                subject.feedCount?.let { add("피드" to "$it") }
            }
            if (stats.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.plate)
                        .border(1.dp, palette.frame, RoundedCornerShape(10.dp))
                ) {
                    stats.forEachIndexed { index, stat ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stat.first,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stat.second,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (index < stats.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(34.dp)
                                    .background(palette.frame)
                            )
                        }
                    }
                }
            }

            if (subject.latestTripTitle != null) {
                ProfileCardMetaRow(
                    label = "최근 동행",
                    value = listOfNotNull(subject.latestTripTitle, subject.latestTripDate).joinToString(" · ")
                )
            }

            subject.introduction?.let { introduction ->
                Text(
                    text = introduction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(9.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (subject.travelStyles.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    subject.travelStyles.forEach { style ->
                        Text(
                            text = style,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(palette.chipContainer)
                                .border(1.dp, palette.frame, RoundedCornerShape(999.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = palette.chipContent
                        )
                    }
                }
            }

            // 뒷면에 볼 것이 하나라도 있을 때만 안내한다 — 매너 점수만 있어도 볼 것이 있다.
            if (subject.mannerRating != null ||
                subject.memories.isNotEmpty() ||
                subject.receivedReviews.isNotEmpty()
            ) {
                // iOS·웹에는 화살표가 있는데 안드로이드에만 없었다 — 같은 안내는 같게 보여야 한다.
                // 화살표를 3dp 폭으로 두 번 왕복시켜 "옆으로 밀 수 있다"를 알린다. 들여다보는
                // 화면이라 계속 움직이지 않고, 캡처에서는 아예 움직이지 않는다.
                val captureMode = LocalCaptureMode.current
                val nudge = remember { Animatable(0f) }
                LaunchedEffect(captureMode) {
                    if (captureMode) return@LaunchedEffect
                    repeat(2) {
                        nudge.animateTo(3f, tween(620, easing = LinearOutSlowInEasing))
                        nudge.animateTo(0f, tween(620, easing = LinearOutSlowInEasing))
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(12.dp)
                            .offset(x = nudge.value.dp)
                    )
                    Text(
                        text = "옆으로 밀면 함께한 여행과 평가를 볼 수 있어요",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CardBack(subject: ProfileCardSubject, palette: MoyeoUserCardPalette) {
    Column(
        modifier = Modifier
            // 앞면과 같은 크기를 채운다. 내용이 넘치면 아래 스크롤 영역이 받는다.
            .fillMaxSize()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    0f to palette.background,
                    0.5f to MaterialTheme.colorScheme.surface,
                    1f to MaterialTheme.colorScheme.surface
                )
            )
            .border(2.dp, palette.border, RoundedCornerShape(18.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 9.dp, bottom = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = subject.nickname,
                modifier = Modifier.weight(1f),
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            // 「카드 뒷면」 문구를 두지 않는다 — 뒤집힌 것은 화면이 이미 보여준다.
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            subject.mannerRating?.let { rating ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.chipContainer)
                        .border(1.dp, palette.frame, RoundedCornerShape(10.dp))
                        .padding(horizontal = 11.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = formatRating(rating),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        color = palette.chipContent
                    )
                    Text(text = "점", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = palette.chipContent)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "동행자들이 준 평균",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (subject.memories.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "함께한 여행",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    subject.memories.forEach { memory ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(9.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "${memory.tripTitle} · ${memory.tripDate}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = memory.oneLineReview?.let { "내 메시지 \"$it\"" } ?: "메시지를 남기지 않았어요",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 평가 칸은 **0건이어도 그린다** — 제목만 남고 아래가 비면 고장으로 읽힌다.
            // 뒷면에 다른 내용이 하나라도 있을 때만이다.
            // 도감의 oneLineReview 는 "내가 남긴" 값이라 여기 섞으면 안 된다.
            if (subject.receivedReviews.isNotEmpty() ||
                subject.mannerRating != null ||
                subject.memories.isNotEmpty()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "다른 여행자들이 남긴 평가",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (subject.receivedReviews.isEmpty()) {
                        Text(
                            text = MoyeoEmptyText.NO_RECEIVED_REVIEWS,
                            fontSize = 10.5.sp,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.testTag("profile-back-no-reviews")
                        )
                    }
                    subject.receivedReviews.forEach { review ->
                        // 강조선은 남긴 사람의 색 — 카드 주인 색과 헷갈리지 않게.
                        val reviewerPalette = rememberUserCardPalette(review.nicknameColor)
                        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(reviewerPalette.border)
                            )
                            Column {
                                Text(
                                    text = "\"${review.content}\"",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = review.nickname,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            // 매너 점수도 함께한 여행도 평가도 없으면 뒷면이 통째로 비어 있었다 —
            // 뒤집어 봤는데 아무것도 없으면 고장으로 읽힌다.
            if (subject.mannerRating == null &&
                subject.memories.isEmpty() &&
                subject.receivedReviews.isEmpty()
            ) {
                Text(
                    text = MoyeoEmptyText.NO_COMPANION_HISTORY,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.chipContent.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        // 한 줄이 위에 붙어 있으면 카드가 여전히 비어 보인다 — 남은 자리 가운데 놓는다.
                        .heightIn(min = 190.dp)
                        .wrapContentHeight(Alignment.CenterVertically)
                        .testTag("profile-back-empty")
                )
            }
        }
    }
}

/** 라벨 + 값 한 줄. 앞면의 `매너 점수` · `최근 동행` 이 같은 정렬을 쓴다. */
@Composable
private fun ProfileCardMetaRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = label,
            modifier = Modifier.width(52.dp),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/** 4.8 처럼 소수 첫째 자리까지. 5.0 을 "5"로 줄이지 않는다(평균값임이 드러나야 한다). */
private fun formatRating(value: Double): String = String.format("%.1f", value)

@Composable
private fun ProfileCardPlaceholder(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CompactMenuHeader(title = "프로필", onBack = onBack)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "프로필을 불러오지 못했어요",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
