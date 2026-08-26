package kr.hanchae.moyeotrip.ui.screens

import androidx.compose.animation.core.CubicBezierEasing
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.hanchae.moyeotrip.data.profile.ServerPublicProfile
import kr.hanchae.moyeotrip.data.profile.ServerReceivedTravelReview
import kr.hanchae.moyeotrip.data.social.DexCompanion
import kr.hanchae.moyeotrip.data.social.DexMemory
import kr.hanchae.moyeotrip.ui.LocalServerData
import kr.hanchae.moyeotrip.ui.components.CachedRemoteImage
import kr.hanchae.moyeotrip.ui.theme.MoyeoUserCardPalette
import kr.hanchae.moyeotrip.ui.theme.rememberUserCardPalette
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

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
    var profile by remember(server, userId) { mutableStateOf<ServerPublicProfile?>(null) }
    var reviews by remember(server, userId) { mutableStateOf<List<ServerReceivedTravelReview>>(emptyList()) }
    LaunchedEffect(server, userId) {
        if (server == null || userId == null) {
            profile = null
            reviews = emptyList()
            return@LaunchedEffect
        }
        profile = runCatching { server.userProfile.publicProfile(userId) }.getOrNull()
        reviews = runCatching { server.userProfile.receivedTravelReviews(userId) }.getOrEmpty()
    }

    // 목데이터로 떨어지는 것은 서버가 없을 때(캡처·목킹 모드)뿐이다.
    // 로그인 상태에서 조회가 실패했는데 목데이터를 보여주면 남의 프로필을 지어내는 셈이 된다.
    val captureMode = server == null || userId == null
    val subject = profile?.let { loaded ->
        ProfileCardSubject(
            nickname = loaded.nickname,
            nicknameColor = loaded.nicknameColor,
            profileImageUrl = loaded.profileImageUrl,
            introduction = loaded.introduction,
            travelStyles = loaded.travelStyles.map { it.label },
            mannerRating = loaded.mannerRating,
            // 서버가 주지 않는 값은 null 로 둔다 → 칸이 만들어지지 않는다
            completedTripCount = null,
            hostedTripCount = null,
            feedCount = null,
            withMeTripCount = dexCompanion?.tripCount,
            latestTripTitle = dexCompanion?.latestTripTitle,
            latestTripDate = dexCompanion?.latestTripDate,
            memories = dexCompanion?.memories.orEmpty(),
            receivedReviews = reviews.map { ReceivedReview(it.reviewerNickname, it.reviewerNicknameColor, it.content) }
        )
    } ?: if (captureMode) ProfileCardSubject.capturePreview() else null

    if (subject == null) {
        // 라이브에서 아직 못 받았거나 실패한 상태. 값을 지어내지 않고 비워 둔다.
        ProfileCardPlaceholder(onBack = onBack)
        return
    }

    ProfileCardBody(subject = subject, onBack = onBack, initialFlipped = initialFlipped)
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
    val hostedTripCount: Int?,
    val feedCount: Int?,
    val withMeTripCount: Int?,
    val latestTripTitle: String?,
    val latestTripDate: String?,
    val memories: List<DexMemory>,
    val receivedReviews: List<ReceivedReview>
) {
    companion object {
        /**
         * 캡처(로그인하지 않은 결정적 모드)에서 쓰는 값. 화면기획 25 와 같다.
         *
         * 서버에 없는 지표(여행·호스트·피드 횟수)는 여기서도 null 이다.
         * 기획(25)에는 정의가 남아 있어 그 세 칸에서 기획 캡처와 다른 것은 정상이다.
         */
        fun capturePreview() = ProfileCardSubject(
            nickname = "우직한 곰 7821",
            nicknameColor = "ORANGE",
            profileImageUrl = null,
            introduction = "사진 찍는 걸 좋아해요. 천천히 걷는 여행을 좋아합니다.",
            travelStyles = listOf("사진", "자연"),
            mannerRating = 4.8,
            // 여행·호스트·피드 횟수는 어떤 서버 응답에도 없다. 캡처 모드에서도 만들지 않는다.
            completedTripCount = null,
            hostedTripCount = null,
            feedCount = null,
            withMeTripCount = 2,
            latestTripTitle = "경주 단풍·야경 모임",
            latestTripDate = "2026.08.23",
            memories = listOf(
                DexMemory(0, "경주 단풍·야경 모임", "2026.08.23", "사진 정말 잘 찍어주셨어요!"),
                DexMemory(0, "주왕산 힐링 트레킹", "2026.06.14", null)
            ),
            receivedReviews = listOf(
                ReceivedReview("고요한 두루미 1130", "SKY_BLUE", "약속 시간을 정확히 지키고 사진도 많이 남겨주셨어요."),
                ReceivedReview("잔잔한 거북이 9032", "MINT", "걷는 속도를 계속 맞춰줘서 편했습니다.")
            )
        )
    }
}

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

@Composable
private fun ProfileCardBody(subject: ProfileCardSubject, onBack: () -> Unit, initialFlipped: Boolean) {
    val palette = rememberUserCardPalette(subject.nicknameColor)
    var phase by remember { mutableStateOf(CardPhase.REST) }
    var pointer by remember { mutableStateOf(Offset(0.5f, 0.5f)) }
    var pressed by remember { mutableStateOf(false) }
    var flipped by remember { mutableStateOf(initialFlipped) }

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
        targetValue = if (flipped) 180f else 0f,
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
                                flipped = !flipped
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
                    // Compose 에는 backface-visibility 가 없다. 90도를 넘어가면 뒷면을 직접 그린다.
                    if (flipRotation <= 90f) {
                        CardFront(subject = subject, palette = palette, shine = shine, angle = holoAngle, shift = holoShift)
                    } else {
                        Box(modifier = Modifier.graphicsLayer { rotationY = 180f }) {
                            CardBack(subject = subject, palette = palette)
                        }
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
            TextButton(onClick = { flipped = !flipped }) {
                Text(text = if (flipped) "앞면" else "뒤집기", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            // DM 기획이 없다 — 여기서 할 수 있는 행동은 친구 신청뿐이다.
            Button(
                onClick = {},
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = "친구 신청", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
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
                    Text(text = "회 동행", fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold, color = palette.chipContent)
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
                Text(text = subject.nickname.profileCardEmoji(), fontSize = 120.sp)
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
                subject.hostedTripCount?.let { add("호스트" to "$it") }
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
                            Text(text = stat.first, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = stat.second, fontSize = 13.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
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

            if (subject.memories.isNotEmpty() || subject.receivedReviews.isNotEmpty()) {
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

@Composable
private fun CardBack(subject: ProfileCardSubject, palette: MoyeoUserCardPalette) {
    Column(
        modifier = Modifier
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
            Text(
                text = "카드 뒷면",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(
            modifier = Modifier
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
                    Text(text = formatRating(rating), fontSize = 19.sp, fontWeight = FontWeight.Black, color = palette.chipContent)
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

            // 받은 게 없으면 섹션을 만들지 않는다.
            // 도감의 oneLineReview 는 "내가 남긴" 값이라 여기 섞으면 안 된다.
            if (subject.receivedReviews.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "다른 여행자들이 남긴 평가",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
        }
    }
}

/** 4.8 처럼 소수 첫째 자리까지. 5.0 을 "5"로 줄이지 않는다(평균값임이 드러나야 한다). */
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

private fun formatRating(value: Double): String = String.format("%.1f", value)

private fun String.profileCardEmoji(): String = when {
    "곰" in this -> "🐻"
    "토끼" in this -> "🐰"
    "고양이" in this -> "🐱"
    "여우" in this -> "🦊"
    "사슴" in this || "고라니" in this -> "🦌"
    "두루미" in this || "두루" in this -> "🕊️"
    "거북" in this -> "🐢"
    "기린" in this -> "🦒"
    "너구리" in this -> "🦝"
    "다람쥐" in this -> "🐿️"
    else -> "🐾"
}

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
