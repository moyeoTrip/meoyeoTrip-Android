package kr.hanchae.moyeotrip.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneOffset
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.hanchae.moyeotrip.R
import kr.hanchae.moyeotrip.data.auth.AuthDependencies
import kr.hanchae.moyeotrip.data.profile.ProfileOption
import kr.hanchae.moyeotrip.data.terms.TermSummary
import kr.hanchae.moyeotrip.domain.auth.AuthDestination
import kr.hanchae.moyeotrip.domain.auth.AuthFlowCoordinator
import kr.hanchae.moyeotrip.domain.auth.AuthFlowState
import kr.hanchae.moyeotrip.domain.auth.AuthProvider
import kr.hanchae.moyeotrip.domain.auth.EmailAuthRequest
import kr.hanchae.moyeotrip.domain.auth.Gender
import kr.hanchae.moyeotrip.domain.auth.NicknameCandidate
import kr.hanchae.moyeotrip.domain.auth.NicknameSelectionState
import kr.hanchae.moyeotrip.domain.auth.ProfileImageCandidate
import kr.hanchae.moyeotrip.notifications.MoyeoPushTokenStore
import kr.hanchae.moyeotrip.ui.components.CachedRemoteImage
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyState
import kr.hanchae.moyeotrip.ui.components.MoyeoEmptyText
import kr.hanchae.moyeotrip.ui.components.MoyeoLinearProgress
import kr.hanchae.moyeotrip.ui.components.MoyeoNicknameAnimal
import kr.hanchae.moyeotrip.ui.theme.ForestGreen
import kr.hanchae.moyeotrip.ui.theme.MoyeoTheme

@Composable
fun AuthFlowScreen(
    onComplete: () -> Unit,
    onExit: () -> Unit = {},
    providedDependencies: AuthDependencies? = null,
    allowExit: Boolean = true,
    initialStepKey: String? = null
) {
    val context = LocalContext.current
    val dependencies = remember(providedDependencies, context) {
        providedDependencies ?: AuthDependencies.appDefault(context)
    }
    // 가입 상태는 서버 응답이 채운다 — 캡처 진입이라고 예시 닉네임·후보를 미리 심지 않는다
    var authState by remember(initialStepKey) { mutableStateOf(AuthFlowState()) }
    val coordinator = remember(dependencies) {
        AuthFlowCoordinator(
            identityTokenProvider = dependencies.identityTokenProvider,
            authGateway = dependencies.authGateway,
            sessionStore = dependencies.sessionStore,
            userProfileStore = dependencies.userProfileStore,
            onFcmTokenRegistered = { MoyeoPushTokenStore.markRegistered(context, it) },
            onStateChange = { authState = it }
        )
    }
    var stepName by rememberSaveable(initialStepKey) {
        mutableStateOf(authStepForKey(initialStepKey).name)
    }
    val coroutineScope = rememberCoroutineScope()
    var lastProviderName by rememberSaveable { mutableStateOf(AuthProvider.KAKAO.name) }
    // 생년월일은 사용자가 직접 고르는 값이다. 캡처 진입에서도 미리 채우지 않는다 —
    // 채워두면 캡처가 실제 첫 화면이 아니라 값이 들어간 화면을 찍는다.
    var selectedBirth by rememberSaveable(initialStepKey) { mutableStateOf("") }
    // 성별은 사용자가 직접 고르는 값이다. 화면기획·웹과 같이 진입 시점에는 아무것도 고르지 않는다.
    var selectedGender by rememberSaveable(initialStepKey) { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    // 약관은 **서버가 주는 목록**을 그대로 그린다(GET /api/v1/terms · 인증 불필요).
    // 클라가 약관을 갖지 않는다 — 서버에서 늘거나 줄면 화면도 따라 바뀐다.
    // 동의한 termId 는 회원가입 요청의 agreedTermIds 로 보낸다(없으면 서버가 400 40012).
    var serverTerms by remember { mutableStateOf<List<TermSummary>>(emptyList()) }
    var termsLoading by remember { mutableStateOf(false) }
    var termsLoadFailed by remember { mutableStateOf(false) }
    // Set 은 rememberSaveable 로 저장할 수 없어 취향과 같은 방식으로 문자열에 인코딩한다.
    var agreedTermIdsRaw by rememberSaveable { mutableStateOf("") }
    val agreedTermIds = decodeTermIds(agreedTermIdsRaw)
    // "만 18세 이상"은 서버 약관이 아니다 — 서버는 birthDate 로 나이를 검증하고 약관 목록에 연령 항목이 없다.
    // 화면기획이 이 확인을 요구하므로 목록과 분리해 맨 위에 둔다. agreedTermIds 에는 넣지 않는다.
    var agreedAge by rememberSaveable { mutableStateOf(false) }
    var termsDetailId by rememberSaveable { mutableStateOf<Long?>(null) }
    var reloadTerms by remember { mutableStateOf(0) }

    // 06-1 취향 후보도 **서버가 주는 목록**을 그대로 그린다(GET /api/v1/users/me/profile/options · 인증 불필요).
    // 클라가 스타일·지역 표를 갖지 않는다 — 서버 시드가 바뀌면 화면도 따라 바뀐다(정본 2-2).
    var styleOptions by remember { mutableStateOf<List<ProfileOption>>(emptyList()) }
    var regionOptions by remember { mutableStateOf<List<ProfileOption>>(emptyList()) }
    var tasteLoading by remember { mutableStateOf(false) }
    var tasteLoadFailed by remember { mutableStateOf(false) }
    var reloadTaste by remember { mutableStateOf(0) }
    // 취향은 아무것도 고르지 않은 상태로 시작한다 — 고르지 않은 취향을 본인 것으로 저장하지 않는다.
    // 담는 값은 라벨이 아니라 서버 id 다. 가입 요청이 id 를 그대로 실어 보낸다(정본 R4).
    var selectedTravelStyles by rememberSaveable(initialStepKey) { mutableStateOf("") }
    var selectedInterestRegions by rememberSaveable(initialStepKey) { mutableStateOf("") }
    val travelStyleSelection = decodeTasteIds(selectedTravelStyles)
    val interestRegionSelection = decodeTasteIds(selectedInterestRegions)
    val step = AuthStep.valueOf(stepName)

    LaunchedEffect(coordinator, initialStepKey) {
        if (initialStepKey == null) coordinator.restoreSession()
    }

    // 07 프로필 이미지 단계는 화면이 열릴 때 서버 후보를 직접 읽는다.
    // 로그인·세션 복원을 거치지 않고 이 단계로 들어오면 후보도 남은 생성 횟수도 서버에 물은 적이 없어
    // 빈 화면 + "0회 남음" 이 그려졌다. 서버가 40919 를 주면 조용히 홈으로 넘어간다(가입 게이트 정본 R2).
    //
    // 단, 단계를 **직접 지정해서** 연 진입(`initialStepKey`, QA·번호별 비교 캡처)에서는 넘기지 않는다.
    // 가입을 마친 계정으로 열면 서버가 늘 40919 를 주므로, 넘기면 07 자리에 홈 화면이 찍힌다.
    LaunchedEffect(coordinator, step, initialStepKey) {
        if (step == AuthStep.CHARACTER) {
            coordinator.loadProfileImageCandidates(advanceWhenAlreadyComplete = initialStepKey == null)
        }
    }

    LaunchedEffect(authState.destination) {
        when (authState.destination) {
            AuthDestination.LOGIN -> Unit
            AuthDestination.NICKNAME -> stepName = AuthStep.NICKNAME.name
            AuthDestination.PROFILE_IMAGE -> stepName = AuthStep.CHARACTER.name
            AuthDestination.COMPLETE -> onComplete()
        }
    }

    val goBack: () -> Unit = {
        stepName = when (step) {
            AuthStep.SPLASH,
            AuthStep.ONBOARDING_ONE -> {
                onExit()
                step.name
            }

            AuthStep.ONBOARDING_TWO -> AuthStep.ONBOARDING_ONE.name

            AuthStep.ONBOARDING_THREE -> AuthStep.ONBOARDING_TWO.name

            AuthStep.LOGIN -> AuthStep.ONBOARDING_THREE.name

            AuthStep.EMAIL -> AuthStep.LOGIN.name

            AuthStep.PASSWORD_RESET -> AuthStep.EMAIL.name

            AuthStep.NICKNAME -> AuthStep.LOGIN.name

            AuthStep.CHARACTER -> {
                onExit()
                step.name
            }

            AuthStep.BASIC_INFO -> AuthStep.NICKNAME.name

            AuthStep.TASTE -> AuthStep.BASIC_INFO.name

            AuthStep.TERMS -> AuthStep.TASTE.name
        }
    }

    termsDetailId?.let { openedTermId ->
        TermsDetailScreen(
            documentKey = "",
            serverTermId = openedTermId,
            source = "signup",
            onBack = { termsDetailId = null },
            onAgree = {
                agreedTermIdsRaw = encodeTermIds(agreedTermIds + openedTermId)
                termsDetailId = null
            }
        )
        return
    }

    val onboardingSteps = setOf(AuthStep.ONBOARDING_ONE, AuthStep.ONBOARDING_TWO, AuthStep.ONBOARDING_THREE)
    val advanceOnboarding: () -> Unit = {
        stepName = when (step) {
            AuthStep.ONBOARDING_ONE -> AuthStep.ONBOARDING_TWO.name
            AuthStep.ONBOARDING_TWO -> AuthStep.ONBOARDING_THREE.name
            else -> AuthStep.LOGIN.name
        }
    }
    val submitSignup: () -> Unit = {
        coroutineScope.launch {
            coordinator.signup(
                gender = Gender.valueOf(selectedGender),
                birthDate = selectedBirth,
                agreedTermIds = agreedTermIds.sorted(),
                // 07 에서 고른 취향은 여기가 유일한 전송 지점이다 — 빠지면 그대로 유실된다(정본 R4).
                travelStyleIds = travelStyleSelection.sorted(),
                interestedRegionIds = interestRegionSelection.sorted()
            )
        }
        Unit
    }

    // 약관 단계에 들어갈 때 서버 목록을 받아온다.
    // 실패하면 목록을 비운 채 다시 불러오기를 띄운다 — 임의의 약관을 지어내지 않는다.
    // 약관을 모르는 채로 동의시키면 동의 기록이 사실과 달라진다.
    LaunchedEffect(step, reloadTerms) {
        if (step != AuthStep.TERMS || termsLoading) return@LaunchedEffect
        if (serverTerms.isNotEmpty() && reloadTerms == 0) return@LaunchedEffect
        termsLoading = true
        termsLoadFailed = false
        runCatching { dependencies.terms.terms() }
            .onSuccess {
                serverTerms = it
                // 목록이 비어 오는 것과 실패는 화면에서 구분되지 않는다 — 로그로 남긴다.
                Log.i("MoyeoAuth", "약관 ${it.size}건 로드")
            }
            .onFailure { error ->
                serverTerms = emptyList()
                termsLoadFailed = true
                Log.w("MoyeoAuth", "약관을 불러오지 못했습니다.", error)
            }
        termsLoading = false
    }

    // 취향 단계에 들어갈 때 서버 후보를 받아온다.
    // 실패하면 후보를 비운 채 다시 불러오기를 띄운다 — 표를 지어내지 않는다(정본 R1).
    LaunchedEffect(step, reloadTaste) {
        if (step != AuthStep.TASTE || tasteLoading) return@LaunchedEffect
        if (styleOptions.isNotEmpty() && reloadTaste == 0) return@LaunchedEffect
        tasteLoading = true
        tasteLoadFailed = false
        runCatching { dependencies.profileOptions.options() }
            .onSuccess { options ->
                styleOptions = options.travelStyles
                regionOptions = options.interestedRegions
                Log.i("MoyeoAuth", "취향 후보 스타일 ${options.travelStyles.size} · 지역 ${options.interestedRegions.size} 로드")
            }
            .onFailure { error ->
                styleOptions = emptyList()
                regionOptions = emptyList()
                tasteLoadFailed = true
                Log.w("MoyeoAuth", "취향 후보를 불러오지 못했습니다.", error)
            }
        tasteLoading = false
    }

    AuthFlowFrame(
        step = step,
        onBack = goBack,
        // 화면기획·웹의 온보딩 1단계에는 뒤로가기가 없다. 시작 화면에서 되돌아갈 곳이 없기 때문이다.
        showBack = step != AuthStep.ONBOARDING_ONE,
        centerContent = step in onboardingSteps,
        onSkip = if (step in onboardingSteps) {
            { stepName = AuthStep.LOGIN.name }
        } else {
            null
        },
        bottomBar = {
            // CTA는 단계마다 화면 바닥에 고정한다. 로그인(제공자 버튼)과 이메일 화면은
            // 본문 안에 동작 버튼이 있어 하단 바를 두지 않는다.
            when (step) {
                AuthStep.SPLASH -> AuthPrimaryButton(
                    text = "시작하기",
                    tag = "auth-splash-next",
                    contentDescription = "스플래시 다음",
                    onClick = { stepName = AuthStep.ONBOARDING_ONE.name }
                )

                AuthStep.ONBOARDING_ONE,
                AuthStep.ONBOARDING_TWO,
                AuthStep.ONBOARDING_THREE -> {
                    val page = onboardingPageFor(step)
                    OnboardingStepDots(current = page.number)
                    AuthPrimaryButton(
                        text = if (page.number == 3) "로그인 시작" else "다음",
                        tag = "auth-onboarding-next",
                        contentDescription = "온보딩 ${page.number} 다음",
                        onClick = advanceOnboarding
                    )
                }

                AuthStep.LOGIN, AuthStep.EMAIL -> Unit

                // 메일을 보낸 뒤에도 화면을 닫지 않는다 — 메일이 안 오면 다시 보낼 곳이 필요하다
                AuthStep.PASSWORD_RESET -> if (authState.noticeMessage == null) {
                    AuthPrimaryButton(
                        text = "재설정 링크 보내기",
                        tag = "auth-password-reset-send",
                        contentDescription = "비밀번호 재설정 링크 보내기",
                        enabled = !authState.isLoading && email.contains('@'),
                        onClick = { coroutineScope.launch { coordinator.sendPasswordReset(email) } }
                    )
                } else {
                    AuthSecondaryButton(
                        text = "로그인으로 돌아가기",
                        tag = "auth-password-reset-back",
                        contentDescription = "로그인 화면으로 돌아가기",
                        onClick = {
                            coordinator.clearError()
                            stepName = AuthStep.EMAIL.name
                        }
                    )
                }

                AuthStep.NICKNAME -> AuthPrimaryButton(
                    text = "다음",
                    tag = "auth-nickname-next",
                    contentDescription = "닉네임 선택 완료",
                    enabled = authState.nickname.canContinue,
                    onClick = { stepName = AuthStep.BASIC_INFO.name }
                )

                AuthStep.CHARACTER -> {
                    val remaining = authState.profileImages?.remainingGenerationCount ?: 0
                    AuthSecondaryButton(
                        text = if (authState.isLoading) {
                            "새 후보를 만들고 있어요..."
                        } else {
                            // 0 이면 만들 수 없다 — 비활성 버튼에 "0회 남음" 은 읽기 어렵다.
                            // 세 플랫폼이 글자 그대로 같아야 한다 (iOS 문구로 통일).
                            if (remaining == 0) {
                                "새 후보 생성 기회를 모두 사용했어요"
                            } else {
                                "새 후보 만들기 · ${remaining}회 남음"
                            }
                        },
                        tag = "auth-profile-generate",
                        contentDescription = "새 프로필 후보 만들기, 남은 ${remaining}회",
                        enabled = !authState.isLoading && remaining > 0,
                        onClick = { coroutineScope.launch { coordinator.generateProfileImage() } }
                    )
                    AuthPrimaryButton(
                        text = "이 친구로 시작하기",
                        tag = "auth-profile-complete",
                        contentDescription = "프로필 이미지 선택 완료",
                        enabled = authState.canSubmitProfileImage,
                        onClick = { coroutineScope.launch { coordinator.completeProfileImage() } }
                    )
                }

                AuthStep.BASIC_INFO -> Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 화면기획(changeLog13)은 이전 / 저장하고 다음 두 버튼이다 — 다음은 여행 취향(7/8)
                    AuthGhostButton(
                        text = "이전",
                        tag = "auth-basic-back",
                        contentDescription = "기본 정보 이전 단계",
                        modifier = Modifier.width(96.dp),
                        onClick = goBack
                    )
                    AuthPrimaryButton(
                        text = if (authState.isLoading) "계정을 만들고 있어요..." else "저장하고 다음",
                        tag = "auth-basic-next",
                        contentDescription = "기본정보 저장하고 다음",
                        enabled = isValidBirthDate(selectedBirth) &&
                            selectedGender.isNotBlank() &&
                            !authState.isLoading,
                        modifier = Modifier.weight(1f),
                        onClick = { stepName = AuthStep.TASTE.name }
                    )
                }

                AuthStep.TASTE -> {
                    // 선택 개수 요약은 CTA 바로 위에 붙는다 (화면기획 06-1)
                    TasteSelectionCaption(
                        styleCount = travelStyleSelection.size,
                        regionCount = interestRegionSelection.size
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AuthGhostButton(
                            text = "이전",
                            tag = "auth-taste-back",
                            contentDescription = "여행 취향 이전 단계",
                            modifier = Modifier.width(96.dp),
                            onClick = goBack
                        )
                        AuthPrimaryButton(
                            text = "저장하고 프로필 만들기",
                            tag = "auth-taste-next",
                            contentDescription = "여행 취향 저장하고 프로필 만들기",
                            enabled = travelStyleSelection.isNotEmpty() && interestRegionSelection.isNotEmpty(),
                            modifier = Modifier.weight(1f),
                            onClick = { stepName = AuthStep.TERMS.name }
                        )
                    }
                }

                AuthStep.TERMS -> AuthPrimaryButton(
                    text = if (authState.isLoading) "계정을 만들고 있어요..." else "동의하고 시작",
                    tag = "auth-terms-finish",
                    contentDescription = "약관 동의 후 계정 만들기",
                    enabled = serverTerms.isNotEmpty() &&
                        agreedAge &&
                        serverTerms.filter { it.required }.all { it.termId in agreedTermIds } &&
                        !authState.isLoading,
                    onClick = submitSignup
                )
            }
        }
    ) {
        when (step) {
            AuthStep.SPLASH -> SplashStep()

            AuthStep.ONBOARDING_ONE,
            AuthStep.ONBOARDING_TWO,
            AuthStep.ONBOARDING_THREE -> OnboardingStep(page = onboardingPageFor(step))

            AuthStep.LOGIN -> LoginStep(
                isLoading = authState.isLoading,
                errorMessage = authState.errorMessage,
                onProviderClick = { provider ->
                    if (provider == AuthProvider.EMAIL) {
                        coordinator.clearError()
                        stepName = AuthStep.EMAIL.name
                    } else {
                        lastProviderName = provider.name
                        coroutineScope.launch { coordinator.login(provider) }
                    }
                },
                onRetry = {
                    coroutineScope.launch { coordinator.login(AuthProvider.valueOf(lastProviderName)) }
                }
            )

            AuthStep.EMAIL -> EmailLoginStep(
                email = email,
                password = password,
                isLoading = authState.isLoading,
                errorMessage = authState.errorMessage,
                noticeMessage = authState.noticeMessage,
                onEmailChange = { email = it },
                onPasswordChange = { password = it },
                onSubmit = {
                    coroutineScope.launch {
                        coordinator.loginWithEmail(EmailAuthRequest(email, password))
                    }
                },
                // 08-B 로 간다. 예전에는 이 자리에서 메일을 바로 쏘고 화면은 그대로였다 —
                // 어떤 주소로 갔는지도, 안 오면 어떻게 하는지도 알려줄 곳이 없었다.
                onResetPassword = {
                    coordinator.clearError()
                    stepName = AuthStep.PASSWORD_RESET.name
                }
            )

            AuthStep.PASSWORD_RESET -> PasswordResetStep(
                email = email,
                isLoading = authState.isLoading,
                sent = authState.noticeMessage != null,
                errorMessage = authState.errorMessage,
                onEmailChange = { email = it },
                onResend = { coroutineScope.launch { coordinator.sendPasswordReset(email) } }
            )

            AuthStep.NICKNAME -> NicknameStep(
                state = authState.nickname,
                onSelectNickname = coordinator::selectNickname,
                onRefresh = { coroutineScope.launch { coordinator.refreshNicknames() } }
            )

            AuthStep.CHARACTER -> ProfileImageStep(
                state = authState,
                onSelect = coordinator::selectProfileImage,
                onRetry = { coroutineScope.launch { coordinator.retryProfileAction() } }
            )

            AuthStep.BASIC_INFO -> BasicInfoStep(
                nickname = authState.nickname.selectedNickname,
                selectedBirth = selectedBirth,
                selectedGender = selectedGender,
                onSelectBirth = { selectedBirth = it },
                onSelectGender = { selectedGender = it },
                errorMessage = authState.errorMessage,
                onRetry = submitSignup
            )

            AuthStep.TASTE -> TasteStep(
                styleOptions = styleOptions,
                regionOptions = regionOptions,
                selectedStyleIds = travelStyleSelection,
                selectedRegionIds = interestRegionSelection,
                isLoading = tasteLoading,
                loadFailed = tasteLoadFailed,
                onRetry = { reloadTaste++ },
                onToggleStyle = { id ->
                    selectedTravelStyles = encodeToggledTasteId(styleOptions, travelStyleSelection, id)
                },
                onToggleRegion = { id ->
                    selectedInterestRegions =
                        encodeToggledTasteId(regionOptions, interestRegionSelection, id)
                }
            )

            AuthStep.TERMS -> TermsStep(
                terms = serverTerms,
                agreedTermIds = agreedTermIds,
                agreedAge = agreedAge,
                isLoading = termsLoading,
                loadFailed = termsLoadFailed,
                onToggleAll = {
                    val turningOff = agreedAge && serverTerms.all { it.termId in agreedTermIds }
                    agreedAge = !turningOff
                    agreedTermIdsRaw = if (turningOff) "" else encodeTermIds(serverTerms.map { it.termId }.toSet())
                },
                onToggleAge = { agreedAge = !agreedAge },
                onToggleTerm = { termId ->
                    agreedTermIdsRaw = encodeTermIds(
                        if (termId in agreedTermIds) agreedTermIds - termId else agreedTermIds + termId
                    )
                },
                onOpenTerm = { termsDetailId = it },
                onReloadTerms = { reloadTerms = reloadTerms + 1 },
                errorMessage = authState.errorMessage,
                onRetry = submitSignup
            )
        }
    }
}

private fun authStepForKey(key: String?): AuthStep = when (key) {
    "onb-1" -> AuthStep.ONBOARDING_ONE
    "onb-2" -> AuthStep.ONBOARDING_TWO
    "onb-3" -> AuthStep.ONBOARDING_THREE
    "login" -> AuthStep.LOGIN
    "email" -> AuthStep.EMAIL
    "password-reset" -> AuthStep.PASSWORD_RESET
    "nickname" -> AuthStep.NICKNAME
    "profile-basic" -> AuthStep.BASIC_INFO
    "profile-taste" -> AuthStep.TASTE
    "profile-image" -> AuthStep.CHARACTER
    "terms" -> AuthStep.TERMS
    else -> AuthStep.ONBOARDING_ONE
}

@Composable
private fun AuthFlowFrame(
    step: AuthStep,
    onBack: () -> Unit,
    showBack: Boolean,
    onSkip: (() -> Unit)? = null,
    // 웹 온보딩은 본문 블록을 화면 세로 중앙에 둔다. 콘텐츠가 짧은 단계에서만 켠다.
    centerContent: Boolean = false,
    bottomBar: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val progress = step.progress

    // 헤더는 고정, 본문만 스크롤, CTA는 화면 바닥에 붙는다.
    // 한 Column을 통째로 스크롤시키면 CTA가 콘텐츠 길이에 따라 화면 중앙에 떠버린다.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .testTag("auth-flow")
            .semantics { contentDescription = "인증 플로우" }
    ) {
        Column(
            modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 30.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBack) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("auth-flow-back")
                            .semantics { contentDescription = "인증 뒤로" }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = colorScheme.onBackground
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(44.dp))
                }
                // 화면기획·웹의 온보딩/가입 헤더에는 닫기(X)가 없다.
                // 대신 온보딩에서는 건너뛰기를 오른쪽에 둔다.
                if (onSkip != null) {
                    TextButton(
                        onClick = onSkip,
                        modifier = Modifier
                            .heightIn(min = 44.dp)
                            .testTag("auth-flow-skip")
                            .semantics { contentDescription = "온보딩 건너뛰기" }
                    ) {
                        Text(
                            text = "건너뛰기",
                            style = MaterialTheme.typography.labelLarge,
                            color = colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(44.dp))
                }
            }
            // 단계 라벨은 8단계 프로그레스와 한 쌍이다. 프로그레스가 없는 보조 화면
            // (스플래시·약관)에서는 본문 제목과 같은 말이 두 번 나와서 그리지 않는다.
            if (progress != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = step.headerLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${progress.current}/${progress.total}",
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                }
                MoyeoLinearProgress(
                    progress = progress.current / progress.total.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .then(if (centerContent) Modifier else Modifier.verticalScroll(rememberScrollState()))
                .padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 22.dp)
                .testTag(step.testTag)
                .semantics { contentDescription = step.screenDescription },
            verticalArrangement = if (centerContent) {
                Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
            } else {
                Arrangement.spacedBy(20.dp)
            }
        ) {
            content()
        }
        if (bottomBar != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth-flow-bottom-bar")
                    .padding(start = 22.dp, end = 22.dp, top = 14.dp, bottom = 30.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                bottomBar()
            }
        }
    }
}

@Composable
private fun SplashStep() {
    Spacer(modifier = Modifier.height(28.dp))
    AuthHeroPanel(
        icon = Icons.Filled.Place,
        title = "모여트립",
        subtitle = "함께 떠날 경북 여행을 준비해요",
        accentColor = ForestGreen
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "스플래시",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "경북 여행 친구를 만나기 위한 가입 여정을 빠르게 시작해보세요.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun OnboardingStep(page: OnboardingPage) {
    AuthOnboardingHeroPanel(page = page)
}

@Composable
private fun LoginStep(
    isLoading: Boolean,
    errorMessage: String?,
    onProviderClick: (AuthProvider) -> Unit,
    onRetry: () -> Unit
) {
    val welcomeImage = if (MoyeoTheme.isDark) {
        R.drawable.login_welcome_night
    } else {
        R.drawable.login_welcome
    }
    Image(
        painter = painterResource(welcomeImage),
        contentDescription = "첨성대 앞에서 여행을 시작하는 모여트립 친구들",
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(12.dp))
            .testTag("auth-login-welcome-image"),
        contentScale = ContentScale.Crop
    )
    StepTitle(
        title = "모여트립에 오신 걸 환영해요",
        subtitle = "마음에 맞는 경북 여행 친구를 만나보세요"
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        KakaoLoginButton(
            enabled = !isLoading,
            onClick = { onProviderClick(AuthProvider.KAKAO) }
        )
        GoogleLoginButton(
            enabled = !isLoading,
            onClick = { onProviderClick(AuthProvider.GOOGLE) }
        )
        ProviderButton(
            text = "이메일로 시작하기",
            mark = "@",
            markColor = MaterialTheme.colorScheme.primary,
            markBackground = Color.Transparent,
            tag = "auth-login-email",
            contentDescription = "이메일 로그인",
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            enabled = !isLoading,
            onClick = { onProviderClick(AuthProvider.EMAIL) }
        )
        AppleLoginButton(
            enabled = !isLoading,
            onClick = { onProviderClick(AuthProvider.APPLE) }
        )
    }
    if (isLoading) {
        AuthLoadingStatus("로그인 정보를 확인하고 있어요...")
    }
    errorMessage?.let { AuthErrorCard(message = it, onRetry = onRetry) }
    // 화면기획의 바닥 안내문 — 로그인 이후 흐름을 미리 알려준다
    Text(
        text = "로그인 후 서버가 알려주는 가입 단계부터 이어서 진행해요.",
        modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun KakaoLoginButton(enabled: Boolean, onClick: () -> Unit) {
    BrandedLoginButton(
        text = "카카오 로그인",
        icon = R.drawable.kakao_login_official,
        iconDescription = "Kakao 로고",
        iconSize = 25.dp,
        tag = "auth-login-kakao",
        containerColor = Color(0xFFFEE500),
        contentColor = Color.Black.copy(alpha = 0.85f),
        enabled = enabled,
        onClick = onClick
    )
}

@Composable
private fun GoogleLoginButton(enabled: Boolean, onClick: () -> Unit) {
    val palette = googleButtonPalette(MoyeoTheme.isDark)
    BrandedLoginButton(
        text = "Google로 계속하기",
        icon = R.drawable.google_g_official,
        iconDescription = "Google G",
        iconSize = 19.dp,
        tag = "auth-login-google",
        containerColor = palette.container,
        contentColor = palette.label,
        border = BorderStroke(1.dp, palette.border),
        enabled = enabled,
        onClick = onClick
    )
}

@Composable
private fun AppleLoginButton(enabled: Boolean, onClick: () -> Unit) {
    val darkTheme = MoyeoTheme.isDark
    BrandedLoginButton(
        text = "Apple로 계속하기",
        icon = R.drawable.apple_continue_official,
        iconDescription = "Apple 로고",
        iconSize = 22.dp,
        tag = "auth-login-apple",
        containerColor = if (darkTheme) Color.White else Color.Black,
        contentColor = if (darkTheme) Color.Black else Color.White,
        enabled = enabled,
        onClick = onClick
    )
}

@Composable
private fun BrandedLoginButton(
    text: String,
    icon: Int,
    iconDescription: String,
    iconSize: androidx.compose.ui.unit.Dp,
    tag: String,
    containerColor: Color,
    contentColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    border: BorderStroke? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag(tag)
            .semantics { contentDescription = text }
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        contentColor = contentColor,
        border = border
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .size(width = 58.dp, height = 54.dp)
                    .testTag("$tag-icon-slot")
                    .align(Alignment.CenterStart),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(icon),
                    contentDescription = iconDescription,
                    modifier = Modifier.size(iconSize),
                    contentScale = ContentScale.Fit,
                    alpha = if (enabled) 1f else 0.5f
                )
            }
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) contentColor else contentColor.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

internal data class GoogleButtonPalette(val container: Color, val border: Color, val label: Color)

internal fun googleButtonPalette(darkTheme: Boolean): GoogleButtonPalette = if (darkTheme) {
    GoogleButtonPalette(
        container = Color(0xFF131314),
        border = Color(0xFF8E918F),
        label = Color(0xFFE3E3E3)
    )
} else {
    GoogleButtonPalette(
        container = Color.White,
        border = Color(0xFF747775),
        label = Color(0xFF1F1F1F)
    )
}

/**
 * 08-B 비밀번호 재설정 — Firebase `sendPasswordResetEmail`.
 *
 * 08-A 의 `비밀번호를 잊으셨나요?` 는 주석에 "비밀번호 재설정 진입" 이라고까지 적혀 있었는데
 * **갈 곳이 없었다.** 로그인하지 못하는 사람이 스스로 풀 수 있는 유일한 길이라, 없으면
 * 문의 말고는 방법이 없다.
 *
 * 보낸 뒤에도 화면을 닫지 않는다 — 메일이 안 오면 다시 보낼 곳이 필요하다.
 */
@Composable
private fun PasswordResetStep(
    email: String,
    isLoading: Boolean,
    sent: Boolean,
    errorMessage: String?,
    onEmailChange: (String) -> Unit,
    onResend: () -> Unit
) {
    if (!sent) {
        StepTitle(
            title = "가입하신 이메일을 알려주세요",
            subtitle = "비밀번호를 새로 정할 수 있는 링크를 보내드려요."
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            AuthFieldLabel("이메일")
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth().testTag("auth-password-reset-email"),
                placeholder = { Text("name@example.com") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                enabled = !isLoading
            )
        }
        AuthNoteBox(
            listOf("카카오로 가입하셨다면 비밀번호가 없어요. 로그인 화면에서 카카오로 다시 들어와 주세요.")
        )
    } else {
        StepTitle(
            title = "메일을 보냈어요",
            subtitle = "$email 으로 재설정 링크를 보냈어요.\n메일함을 확인해 주세요."
        )
        AuthNoteBox(
            listOf(
                "링크는 1시간 동안만 쓸 수 있어요.",
                "메일이 안 보이면 스팸함도 확인해 주세요."
            )
        )
        TextButton(
            onClick = onResend,
            enabled = !isLoading,
            modifier = Modifier.testTag("auth-password-reset-resend")
        ) {
            Text("다시 보내기", fontWeight = FontWeight.ExtraBold)
        }
    }
    if (isLoading) AuthLoadingStatus("재설정 메일을 보내고 있어요...")
    errorMessage?.let { AuthErrorCard(message = it, onRetry = onResend) }
}

/** 08-B 안내 상자 — 20-2a~f · §6 화면의 `NoteBox` 와 같은 생김새다. */
@Composable
private fun AuthNoteBox(lines: List<String>) {
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
private fun EmailLoginStep(
    email: String,
    password: String,
    isLoading: Boolean,
    errorMessage: String?,
    noticeMessage: String?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onResetPassword: () -> Unit
) {
    // 로그인/새 계정 만들기를 사용자가 먼저 고르지 않는다 — 이메일이 이미 있는 계정인지
    // 사용자가 알아야 할 이유가 없다. 소셜 로그인과 같이 한 번 시도하고, 계정이 없으면
    // 그대로 새 계정을 만들어 서버가 알려주는 가입 단계로 이어간다.
    StepTitle(
        title = "이메일로 시작하기",
        subtitle = "이메일과 비밀번호를 입력하면 로그인하거나 새 계정을 만들어요."
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            AuthFieldLabel("이메일")
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth().testTag("auth-email-address"),
                placeholder = { Text("name@example.com") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                enabled = !isLoading
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            AuthFieldLabel("비밀번호")
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth().testTag("auth-email-password"),
                placeholder = { Text("6자 이상 입력") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                enabled = !isLoading
            )
        }
        // 화면기획은 재설정 링크가 CTA 위(입력 바로 아래)에 온다
        TextButton(
            onClick = onResetPassword,
            enabled = !isLoading && email.contains('@'),
            modifier = Modifier.align(Alignment.End).testTag("auth-email-reset")
        ) {
            Text("비밀번호를 잊으셨나요?")
        }
        AuthPrimaryButton(
            text = "계속하기",
            tag = "auth-email-submit",
            contentDescription = "이메일로 계속하기",
            enabled = !isLoading && emailCredentialsError(email, password) == null,
            onClick = onSubmit
        )
    }
    if (isLoading) AuthLoadingStatus("Firebase 계정을 확인하고 있어요...")
    noticeMessage?.let {
        Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
    }
    errorMessage?.let { AuthErrorCard(message = it, onRetry = onSubmit) }
    Text(
        text = "처음 쓰는 이메일이면 새 계정을 만들고, 가입 진행 단계는 서버 응답에 따라 이어집니다.",
        modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun RowScope.EmailModeButton(text: String, selected: Boolean, tag: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
            )
            .clickable(onClick = onClick)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

/** 인풋 위에 붙는 필드 라벨. Material 플로팅 라벨은 값이 비면 플레이스홀더처럼 보인다. */
@Composable
private fun AuthFieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

/**
 * 05 이름 고르기.
 *
 * 후보는 서버(`POST /api/v1/auth/nickname-candidates`)만 준다. 화면 상태는 셋뿐이고
 * 웹·iOS 와 **같은 §2 문구**를 쓴다 — 로딩 `불러오는 중이에요…`,
 * 후보를 못 받음 `불러오지 못했어요.` + 다시 시도, 후보 있음 → 서버 값.
 */
@Composable
private fun NicknameStep(state: NicknameSelectionState, onSelectNickname: (String) -> Unit, onRefresh: () -> Unit) {
    // 화면이 열릴 때 후보가 없으면 서버에서 받아온다 — 앱이 후보를 심어 두지 않는다
    LaunchedEffect(Unit) {
        if (state.candidates.isEmpty() && !state.isLoading) onRefresh()
    }
    StepTitle(
        title = "어떤 친구로\n시작할까요?",
        subtitle = "본명 대신 동물 친구로 만나요.\n이름을 고르면 캐릭터를 그려드릴게요."
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when {
            state.isLoading -> {
                repeat(3) { index -> NicknameCandidateSkeleton(index) }
                Text(
                    text = MoyeoEmptyText.LOADING,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth-nickname-loading"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            state.candidates.isEmpty() -> MoyeoEmptyState(
                text = MoyeoEmptyText.FAILED,
                testTag = "auth-nickname-empty",
                onRetry = onRefresh
            )

            else -> state.candidates.forEachIndexed { index, candidate ->
                NicknameCandidateCard(
                    candidate = candidate,
                    selected = state.selectedNickname == candidate.nickname,
                    index = index,
                    onClick = { onSelectNickname(candidate.nickname) }
                )
            }
        }
        OutlinedButton(
            onClick = onRefresh,
            enabled = state.canRefresh,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("auth-nickname-refresh")
                .semantics { contentDescription = "서버에서 다른 이름 세 개 추천받기" },
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(
                1.dp,
                if (state.isLoading) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = if (state.isLoading) "새 이름을 받고 있어요..." else "다른 이름 추천받기",
                style = MaterialTheme.typography.labelLarge
            )
        }
        Text(
            text = state.statusMessage,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auth-nickname-refresh-status"),
            style = MaterialTheme.typography.labelSmall,
            color = if (state.errorMessage != null) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NicknameCandidateCard(candidate: NicknameCandidate, selected: Boolean, index: Int, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 92.dp)
            .testTag("auth-nickname-option-$index")
            .semantics {
                contentDescription =
                    "닉네임 ${candidate.nickname}, ${candidate.colorLabel()}, ${candidate.description} 선택"
            }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(candidate.swatchColor().copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = MoyeoNicknameAnimal.emojiForAnimal(candidate.animal),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        text = candidate.nickname,
                        modifier = Modifier.weight(1f, fill = false),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(candidate.swatchColor())
                    )
                    Text(
                        text = candidate.colorLabel(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = candidate.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun NicknameCandidate.colorLabel(): String = when (color) {
    "RED" -> "빨강"
    "ORANGE" -> "주황"
    "YELLOW" -> "노랑"
    "GREEN" -> "초록"
    "BLUE" -> "파랑"
    "NAVY" -> "남색"
    "PURPLE" -> "보라"
    "PINK" -> "분홍"
    "SKY_BLUE" -> "하늘"
    else -> "민트"
}

private fun NicknameCandidate.swatchColor(): Color = when (color) {
    "RED" -> Color(0xFFE65B58)
    "ORANGE" -> Color(0xFFED8A3D)
    "YELLOW" -> Color(0xFFD6A928)
    "GREEN" -> Color(0xFF3D9B63)
    "BLUE" -> Color(0xFF4B7EDB)
    "NAVY" -> Color(0xFF40547A)
    "PURPLE" -> Color(0xFF8A63B8)
    "PINK" -> Color(0xFFD96F98)
    "SKY_BLUE" -> Color(0xFF58A9D6)
    else -> Color(0xFF55B99A)
}

@Composable
private fun NicknameCandidateSkeleton(index: Int) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .testTag("auth-nickname-skeleton-$index"),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(width = 132.dp, height = 16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Box(
                    modifier = Modifier
                        .size(width = 52.dp, height = 12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }
    }
}

@Composable
private fun ProfileImageStep(state: AuthFlowState, onSelect: (Long) -> Unit, onRetry: () -> Unit) {
    val images = state.profileImages
    val selectedNickname = state.nickname.selectedNickname
    val selectedNicknameCandidate = state.nickname.candidates.firstOrNull {
        it.nickname == selectedNickname
    }
    val candidates = images?.candidates.orEmpty()
    StepTitle(
        title = "여행에서 만날 내 친구를 골라주세요",
        subtitle = selectedNickname?.let { nickname ->
            "$nickname 닉네임을 바탕으로 후보를 하나씩 추가해요. 이전에 만든 후보는 사라지지 않아요."
        } ?: "서버에 저장된 닉네임을 바탕으로 후보를 하나씩 추가해요."
    )
    if (state.isGeneratingProfileImage) {
        ProfileImageGeneratingCard(nickname = selectedNickname)
    }
    // 후보는 항상 수평 배치 — 1개는 가운데(폭 1/3), 2개는 좌우 절반, 3개는 3등분.
    // 빈 자리 유령 카드와 부가 설명은 두지 않는다 (화면기획 기준).
    if (candidates.isEmpty()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .testTag("auth-profile-empty"),
            shape = RoundedCornerShape(16.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = if (state.isLoading) {
                        "이미지를 만들고 있어요..."
                    } else {
                        "아래 버튼을 눌러 첫 프로필 이미지를 만들어보세요."
                    },
                    modifier = Modifier.padding(horizontal = 24.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            candidates.forEachIndexed { index, candidate ->
                ProfileImageCandidateCard(
                    candidate = candidate,
                    index = index,
                    selected = state.selectedProfileImageId == candidate.profileImageId,
                    enabled = !state.isLoading,
                    accentColor = selectedNicknameCandidate?.swatchColor(),
                    modifier = if (candidates.size == 1) {
                        Modifier.fillMaxWidth(1f / 3f)
                    } else {
                        Modifier.weight(1f)
                    }
                ) { onSelect(candidate.profileImageId) }
            }
        }
    }
    state.errorMessage?.let { AuthErrorCard(message = it, onRetry = onRetry) }
}

@Composable
private fun ProfileImageGeneratingCard(nickname: String?) {
    val messages = remember {
        listOf(
            "닉네임에서 여행 친구의 분위기를 찾고 있어요",
            "어울리는 표정과 성격을 떠올리고 있어요",
            "여행 친구의 옷과 색을 고르고 있어요",
            "경북 여행에 어울리는 소품을 더하고 있어요",
            "마지막 색을 입히고 있어요"
        )
    }
    var elapsedSeconds by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000.milliseconds)
            elapsedSeconds += 1
        }
    }
    val message = messages[(elapsedSeconds / 4).coerceAtMost(messages.lastIndex)]

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("auth-profile-generating")
            .semantics { contentDescription = "프로필 이미지 생성 중, $message" },
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(34.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = nickname?.let { "‘$it’만의 경북 여행 친구를 만들고 있어요" }
                    ?: "나만의 경북 여행 친구를 만들고 있어요",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(99.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MoyeoTheme.tints.softLine
            )
            Text(
                text = "조금 오래 걸릴 수 있어요. 다른 화면으로 이동해도 완성된 후보는 서버에 보관돼요.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ProfileImageCandidateCard(
    candidate: ProfileImageCandidate,
    index: Int,
    selected: Boolean,
    enabled: Boolean,
    accentColor: Color?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val selectionColor = accentColor ?: MaterialTheme.colorScheme.primary
    Surface(
        modifier = modifier
            // 후보 이미지는 정사각이다(안쪽 Box 가 aspectRatio(1f)). 겉 카드가 1:1.16 이면
            // 이미지 위아래로 여백이 남아 테두리가 세로로 길쭉해 보인다.
            .aspectRatio(1f)
            .testTag("auth-profile-option-$index")
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) {
            selectionColor.copy(alpha = 0.14f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) selectionColor else MaterialTheme.colorScheme.outline
        )
    ) {
        Box(
            modifier = Modifier.padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                ProfileImageThumbnail(candidate = candidate)
            }
        }
    }
}

@Composable
private fun ProfileImageThumbnail(candidate: ProfileImageCandidate) {
    CachedRemoteImage(
        url = candidate.profileImageUrl,
        contentDescription = "생성된 프로필 이미지",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    ) {
        Text(text = "🧭", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
private fun CharacterStep(nickname: String, characterCreated: Boolean, onCreate: () -> Unit, onNext: () -> Unit) {
    StepTitle(
        title = "만나서 반가워요!",
        subtitle = "한 번 정한 친구는 바꿀 수 없어요"
    )
    CharacterFriendCard(nickname = nickname, characterCreated = characterCreated)
    if (characterCreated) {
        AuthPrimaryButton(
            text = "기본정보 입력",
            tag = "auth-character-next",
            contentDescription = "캐릭터 생성 완료",
            onClick = onNext
        )
    } else {
        AuthPrimaryButton(
            text = "캐릭터 생성",
            tag = "auth-character-create",
            contentDescription = "캐릭터 생성",
            onClick = onCreate
        )
    }
}

@Composable
private fun CharacterFriendCard(nickname: String, characterCreated: Boolean) {
    val colorScheme = MaterialTheme.colorScheme
    // 아바타 표는 한 곳뿐이다 (R5 정본 = MoyeoNicknameAnimal)
    val animal = MoyeoNicknameAnimal.emojiForNickname(nickname)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("auth-character-preview")
            .semantics { contentDescription = "$nickname 캐릭터 카드" },
        shape = RoundedCornerShape(18.dp),
        color = if (characterCreated) colorScheme.primaryContainer else colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (characterCreated) colorScheme.primary else colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(138.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = animal, style = MaterialTheme.typography.headlineLarge)
                    Text(
                        text = if (characterCreated) "여행 친구 생성 완료" else "여행 친구 생성 대기",
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = nickname,
                    style = MaterialTheme.typography.titleLarge,
                    color = colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "같이 갈 사람을 고를 때 성향을 부드럽게 보여주는 친구예요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CharacterTraitChip(text = "초기 캐릭터")
                CharacterTraitChip(text = "변경 불가")
            }
        }
    }
}

@Composable
private fun CharacterTraitChip(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun RequiredFieldLabel(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = " *",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}

// / 06 단계 상단의 닉네임 카드 — 앞 단계에서 고른 친구를 다시 보여준다.
@Composable
private fun SelectedNicknameCard(nickname: String?) {
    // 앞 단계에서 아직 고르지 않았으면 카드를 그리지 않는다 — 예시 닉네임을 대신 세우지 않는다
    val label = nickname?.takeIf { it.isNotBlank() } ?: return
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("auth-basic-nickname"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = MoyeoTheme.tints.primaryTint
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = MoyeoNicknameAnimal.emojiForNickname(label),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "새 친구가 옆에 앉았어요",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// / 생년월일 아래 안내 — 공개되는 것은 나이대뿐임을 알려준다.
@Composable
private fun AgeBandNote(birthDate: LocalDate?) {
    val band = birthDate?.let { ageBandLabel(it) }
    Text(
        text = if (band == null) "나이대만 공개됩니다" else "나이대만 공개됩니다 ($band)",
        modifier = Modifier.testTag("auth-basic-age-band"),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

private fun ageBandLabel(birthDate: LocalDate): String? {
    val age = Period.between(birthDate, LocalDate.now()).years
    if (age < 10) return null
    val decade = (age / 10) * 10
    val phase = when (age % 10) {
        in 0..3 -> "초반"
        in 4..6 -> "중반"
        else -> "후반"
    }
    return "${decade}대 $phase"
}

@Composable
private fun BasicInfoStep(
    nickname: String?,
    selectedBirth: String,
    selectedGender: String,
    onSelectBirth: (String) -> Unit,
    onSelectGender: (String) -> Unit,
    errorMessage: String?,
    onRetry: () -> Unit
) {
    var showBirthDateSheet by rememberSaveable { mutableStateOf(false) }
    // 화면기획·웹은 이 단계에서 별도 제목 없이 앞 단계에서 고른 닉네임 카드를 먼저 보여준다.
    SelectedNicknameCard(nickname = nickname)
    RequiredFieldLabel("생년월일")
    val selectedBirthDate = remember(selectedBirth) {
        runCatching { LocalDate.parse(selectedBirth) }.getOrNull()
    }
    val birthDateLabel = selectedBirthDate?.let { date ->
        "${date.year}년 ${date.monthValue}월 ${date.dayOfMonth}일"
    } ?: "연도 / 월 / 일"
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .testTag("auth-birth-date")
            .semantics { contentDescription = "생년월일 $birthDateLabel 선택" }
            .clickable { showBirthDateSheet = true },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        // 화면기획은 달력 아이콘이 왼쪽, 진입 화살표가 오른쪽이다
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CalendarToday,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = birthDateLabel,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                color = if (selectedBirthDate == null) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    AgeBandNote(selectedBirthDate)
    RequiredFieldLabel("성별")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        CompactChoice(
            title = "여성",
            selected = selectedGender == Gender.FEMALE.name,
            tag = "auth-gender-female",
            contentDescription = "성별 여성 선택",
            onClick = { onSelectGender(Gender.FEMALE.name) }
        )
        CompactChoice(
            title = "남성",
            selected = selectedGender == Gender.MALE.name,
            tag = "auth-gender-male",
            contentDescription = "성별 남성 선택",
            onClick = { onSelectGender(Gender.MALE.name) }
        )
        CompactChoice(
            title = "선택 안 함",
            selected = selectedGender == Gender.UNDISCLOSED.name,
            tag = "auth-gender-undisclosed",
            contentDescription = "성별 선택 안 함",
            onClick = { onSelectGender(Gender.UNDISCLOSED.name) }
        )
    }
    errorMessage?.let { AuthErrorCard(message = it, onRetry = onRetry) }
    if (showBirthDateSheet) {
        BirthDateBottomSheet(
            initialDate = selectedBirthDate ?: LocalDate.of(1998, 4, 12),
            onDismiss = { showBirthDateSheet = false },
            onConfirm = { date ->
                onSelectBirth("%04d-%02d-%02d".format(date.year, date.monthValue, date.dayOfMonth))
                showBirthDateSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthDateBottomSheet(initialDate: LocalDate, onDismiss: () -> Unit, onConfirm: (LocalDate) -> Unit) {
    val today = LocalDate.now()
    val initialMillis = initialDate
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()
    val state = androidx.compose.material3.rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        yearRange = 1900..today.year
    )
    val selectedDate = state.selectedDateMillis?.let { millis ->
        Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
    }
    val selectedLabel = selectedDate?.let { date ->
        "${date.year}년 ${date.monthValue}월 ${date.dayOfMonth}일"
    } ?: "날짜를 선택해주세요"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MoyeoTheme.sheetSurface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "생년월일 선택",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = selectedLabel,
                modifier = Modifier.testTag("birth-date-selected-label"),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "연도부터 확인한 뒤 월과 일을 골라주세요.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            DatePicker(
                state = state,
                modifier = Modifier.fillMaxWidth(),
                showModeToggle = false,
                title = null,
                headline = null
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("취소")
                }
                Button(
                    onClick = { selectedDate?.let(onConfirm) },
                    enabled = selectedDate != null && !selectedDate.isAfter(today),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("birth-date-confirm"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("선택 완료")
                }
            }
        }
    }
}

internal fun isValidBirthDate(value: String, today: LocalDate = LocalDate.now()): Boolean = runCatching {
    val date = LocalDate.parse(value)
    !date.isAfter(today) && date.year >= 1900
}.getOrDefault(false)

/**
 * 취향 선택. 라벨이 아니라 **서버 id** 를 담는다 — 가입 요청이 id 를 보내기 때문이다(정본 R4).
 * Set 은 rememberSaveable 로 저장할 수 없어 CSV 문자열에 담는다.
 */
internal fun decodeTasteIds(encoded: String): Set<Long> =
    encoded.split(",").mapNotNull { it.trim().toLongOrNull() }.toSet()

/** 동의한 약관 ID. Set 은 rememberSaveable 로 저장할 수 없어 취향과 같은 방식으로 문자열에 담는다. */
internal fun decodeTermIds(encoded: String): Set<Long> =
    encoded.split(",").mapNotNull { it.trim().toLongOrNull() }.toSet()

internal fun encodeTermIds(ids: Set<Long>): String = ids.sorted().joinToString(",")

/** 토글 결과를 후보 순서대로 다시 인코딩한다 — 표시 순서가 탭 순서에 흔들리지 않는다. */
internal fun encodeToggledTasteId(options: List<ProfileOption>, current: Set<Long>, id: Long): String {
    val next = if (id in current) current - id else current + id
    return options.filter { it.id in next }.joinToString(",") { it.id.toString() }
}

/**
 * 06-1 · 여행 취향 — 스타일 & 관심 지역 (Step 7/8).
 *
 * 후보는 `GET /api/v1/users/me/profile/options` 가 준다 — 토큰 없이 200 이라 가입 전에도 부를 수 있다.
 * 못 받으면 칩을 하나도 그리지 않고 다시 불러오기를 띄운다. 고를 수 없는 후보를 지어내지 않는다(정본 R1).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TasteStep(
    styleOptions: List<ProfileOption>,
    regionOptions: List<ProfileOption>,
    selectedStyleIds: Set<Long>,
    selectedRegionIds: Set<Long>,
    isLoading: Boolean,
    loadFailed: Boolean,
    onRetry: () -> Unit,
    onToggleStyle: (Long) -> Unit,
    onToggleRegion: (Long) -> Unit
) {
    StepTitle(
        title = "어떤 여행을 좋아하세요?",
        subtitle = "여행 스타일과 관심 지역을 알려주시면\n딱 맞는 모집을 먼저 보여드려요."
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TasteRequirementLabel(title = "여행 스타일", requirement = "1개 이상")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            styleOptions.forEach { option ->
                TasteChip(
                    label = option.label,
                    selected = option.id in selectedStyleIds,
                    tag = "auth-taste-style-${option.label}",
                    contentDescription = "여행 스타일 ${option.label} 선택",
                    onClick = { onToggleStyle(option.id) }
                )
            }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TasteRequirementLabel(title = "관심 지역", requirement = "경북 안에서 1곳 이상")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            regionOptions.forEach { option ->
                TasteChip(
                    label = option.label,
                    selected = option.id in selectedRegionIds,
                    tag = "auth-taste-region-${option.label}",
                    contentDescription = "관심 지역 ${option.label} 선택",
                    onClick = { onToggleRegion(option.id) }
                )
            }
        }
    }
    if (isLoading) {
        AuthLoadingStatus(message = "취향 후보를 불러오는 중이에요")
    }
    if (loadFailed) {
        TextButton(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auth-taste-reload")
        ) {
            Text(text = "취향 후보 다시 불러오기", fontWeight = FontWeight.Bold)
        }
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("auth-taste-info"),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = "여행 스타일과 관심 지역은 마이 > 프로필 수정에서 언제든 함께 바꿀 수 있어요.",
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** "여행 스타일 * · 1개 이상" 같은 필수 라벨 줄. 가입(06-1)과 편집 시트(28-1)가 함께 쓴다. */
@Composable
internal fun TasteRequirementLabel(title: String, requirement: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = " *",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = " · $requirement",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 여행 취향 선택 칩 — changeLog12의 단일 강조 원칙대로 배경 틴트 + 외곽선으로만
 * 선택을 표시하고 체크 배지를 두지 않는다. 가입(06-1)과 편집 시트(28-1)가 함께 쓴다.
 */
@Composable
internal fun TasteChip(label: String, selected: Boolean, tag: String, contentDescription: String, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .height(38.dp)
            .testTag(tag)
            .semantics { this.contentDescription = contentDescription }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = if (selected) MoyeoTheme.tints.primaryTint else colors.surface,
        border = BorderStroke(1.dp, if (selected) colors.primary else colors.outline)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) colors.primary else colors.onSurface,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
            )
        }
    }
}

/** "여행 스타일 N개 · 관심 지역 N곳 선택" 요약 캡션. */
@Composable
internal fun TasteSelectionCaption(styleCount: Int, regionCount: Int, modifier: Modifier = Modifier) {
    Text(
        text = "여행 스타일 ${styleCount}개 · 관심 지역 ${regionCount}곳 선택",
        modifier = modifier
            .fillMaxWidth()
            .testTag("taste-selection-caption"),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun TermsStep(
    terms: List<TermSummary>,
    agreedTermIds: Set<Long>,
    agreedAge: Boolean,
    isLoading: Boolean,
    loadFailed: Boolean,
    onToggleAll: () -> Unit,
    onToggleAge: () -> Unit,
    onToggleTerm: (Long) -> Unit,
    onOpenTerm: (Long) -> Unit,
    onReloadTerms: () -> Unit,
    errorMessage: String?,
    onRetry: () -> Unit
) {
    val requiredAgreed = agreedAge && terms.filter { it.required }.all { it.termId in agreedTermIds }
    val allAgreed = agreedAge && terms.isNotEmpty() && terms.all { it.termId in agreedTermIds }

    StepTitle(
        title = "약관 동의",
        subtitle = "모여트립 이용을 위해 동의가 필요해요"
    )
    // 약관은 항목마다 카드를 두지 않고 한 줄씩 수직으로 쌓는다 (화면기획 기준).
    // 카드가 겹치면 필수/선택 위계가 읽히지 않는다.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("auth-terms-all")
            .semantics { contentDescription = "약관 모두 동의" }
            .clickable(enabled = terms.isNotEmpty(), onClick = onToggleAll)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Checkbox(
            checked = allAgreed,
            onCheckedChange = { onToggleAll() },
            enabled = terms.isNotEmpty(),
            modifier = Modifier.testTag("auth-terms-all-checkbox"),
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
        )
        Text(
            text = "모두 동의",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        // 서버 약관이 아니라 별도 확인이다 — 서버는 birthDate 로 나이를 검증한다.
        TermsRow(
            title = "만 18세 이상",
            required = true,
            checked = agreedAge,
            tag = "auth-terms-age",
            contentDescription = "만 18세 이상 필수 확인",
            onToggle = onToggleAge
        )
        terms.forEach { term ->
            TermsRow(
                title = term.displayTitle(),
                required = term.required,
                checked = term.termId in agreedTermIds,
                tag = "auth-terms-${term.termId}",
                contentDescription = "${term.displayTitle()} ${if (term.required) "필수" else "선택"} 동의",
                onToggle = { onToggleTerm(term.termId) },
                onOpenDetails = { onOpenTerm(term.termId) }
            )
        }
    }
    if (isLoading) {
        AuthLoadingStatus(message = "약관을 불러오는 중이에요")
    }
    errorMessage?.let { AuthErrorCard(message = it, onRetry = onRetry) }
    if (loadFailed) {
        TextButton(
            onClick = onReloadTerms,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auth-terms-reload")
        ) {
            Text(text = "약관 다시 불러오기", fontWeight = FontWeight.Bold)
        }
    }
    // requiredAgreed 는 CTA 활성 조건과 같은 규칙이다 — 화면에서 한 번 더 계산해 두면
    // 조건이 갈릴 때 바로 드러난다.
    check(!requiredAgreed || terms.isNotEmpty())
}

/** 서버 제목은 "[필수] 모여트립 이용약관" 형태다. 필수/선택 배지를 따로 그리므로 접두사는 뗀다. */
internal fun TermSummary.displayTitle(): String = title.removePrefix("[필수]").removePrefix("[선택]").trim()

@Composable
private fun AuthLoadingStatus(message: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("auth-loading"),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AuthErrorCard(message: String, onRetry: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("auth-error"),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth-error-retry"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("다시 시도")
            }
        }
    }
}

@Composable
private fun AuthHeroPanel(icon: ImageVector, title: String, subtitle: String, accentColor: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(48.dp)
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AuthOnboardingHeroPanel(page: OnboardingPage) {
    // 화면기획의 온보딩은 카드에 담기지 않는다 — 배경 위에 이미지와 문구만 중앙 정렬한다
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Image(
            painter = painterResource(page.imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(220.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("auth-onboarding-illustration-${page.number}")
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = page.body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** 온보딩 3단계 위치를 점으로 알려준다. 화면기획과 웹에 있는 요소다. */
@Composable
private fun OnboardingStepDots(current: Int, total: Int = 3) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("auth-onboarding-dots"),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { index ->
            val active = index + 1 == current
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(width = if (active) 18.dp else 6.dp, height = 6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (active) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }
                    )
            )
        }
    }
}

@Composable
private fun StepTitle(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AuthPrimaryButton(
    text: String,
    tag: String,
    contentDescription: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .then(if (modifier == Modifier) Modifier.fillMaxWidth() else Modifier)
            .height(54.dp)
            .testTag(tag)
            .semantics { this.contentDescription = contentDescription },
        shape = RoundedCornerShape(12.dp)
    ) {
        // 화면기획의 CTA는 글자만 있다 — 방향 아이콘을 덧붙이지 않는다
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

/** 보조 CTA — 외곽선 + 브랜드 색 글자. 웹·화면기획의 secondary 버튼과 같은 위계. */
@Composable
private fun AuthSecondaryButton(
    text: String,
    tag: String,
    contentDescription: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .then(if (modifier == Modifier) Modifier.fillMaxWidth() else Modifier)
            .height(54.dp)
            .testTag(tag)
            .semantics { this.contentDescription = contentDescription },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

/** 중립 CTA — 되돌아가기처럼 강조하지 않는 동작. */
@Composable
private fun AuthGhostButton(
    text: String,
    tag: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(54.dp)
            .testTag(tag)
            .semantics { this.contentDescription = contentDescription },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun ProviderButton(
    text: String,
    mark: String,
    markColor: Color,
    markBackground: Color,
    tag: String,
    contentDescription: String,
    containerColor: Color,
    contentColor: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag(tag)
            .semantics { this.contentDescription = contentDescription },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .size(width = 58.dp, height = 54.dp)
                    .testTag("$tag-icon-slot")
                    .align(Alignment.CenterStart),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(markBackground, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mark,
                        fontSize = 15.sp,
                        color = markColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

/**
 * 이메일 입력 검증.
 *
 * 로그인/가입을 따로 고르지 않으므로 "비밀번호 확인" 입력이 없다 —
 * 로그인일 수도 있는 입력에 확인란을 요구할 수 없다.
 */
internal fun emailCredentialsError(email: String, password: String): String? = when {
    !email.contains('@') -> "이메일 주소를 확인해 주세요."
    password.length < 6 -> "비밀번호는 6자 이상 입력해 주세요."
    else -> null
}

@Composable
private fun ChoiceCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    tag: String,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
            .semantics { this.contentDescription = contentDescription }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RowScope.CompactChoice(
    title: String,
    selected: Boolean,
    tag: String,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag(tag)
            .semantics { this.contentDescription = contentDescription }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TermsRow(
    title: String,
    required: Boolean,
    checked: Boolean,
    tag: String,
    contentDescription: String,
    onToggle: () -> Unit,
    onOpenDetails: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(tag)
                .semantics { this.contentDescription = contentDescription }
                .clickable(onClick = onToggle)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = if (required) "(필수)" else "(선택)",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (onOpenDetails != null) {
                IconButton(
                    onClick = onOpenDetails,
                    modifier = Modifier.size(36.dp).testTag("$tag-details")
                ) {
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "$title 내용 보기",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

private enum class AuthStep(
    val headerLabel: String,
    val testTag: String,
    val screenDescription: String,
    val progress: AuthProgress?
) {
    SPLASH(
        headerLabel = "스플래시",
        testTag = "auth-step-splash",
        screenDescription = "스플래시 화면",
        progress = null
    ),
    ONBOARDING_ONE(
        headerLabel = "온보딩",
        testTag = "auth-step-onboarding-1",
        screenDescription = "온보딩 첫 번째 화면",
        progress = AuthProgress(current = 1, total = 8)
    ),
    ONBOARDING_TWO(
        headerLabel = "온보딩",
        testTag = "auth-step-onboarding-2",
        screenDescription = "온보딩 두 번째 화면",
        progress = AuthProgress(current = 2, total = 8)
    ),
    ONBOARDING_THREE(
        headerLabel = "온보딩",
        testTag = "auth-step-onboarding-3",
        screenDescription = "온보딩 세 번째 화면",
        progress = AuthProgress(current = 3, total = 8)
    ),
    LOGIN(
        headerLabel = "로그인",
        testTag = "auth-step-login",
        screenDescription = "소셜 로그인 화면",
        progress = AuthProgress(current = 4, total = 8)
    ),
    EMAIL(
        // 이메일 로그인은 로그인 방식 선택에서 갈라지는 보조 화면이다 — 8단계 프로그레스를 다시 그리지 않는다
        headerLabel = "이메일 로그인",
        testTag = "auth-step-email",
        screenDescription = "이메일 로그인 및 계정 생성 화면",
        progress = null
    ),

    // 08-B 비밀번호 재설정 — 08-A 이메일 로그인에서 갈라지는 보조 화면이다.
    // 로그인하지 못하는 사람이 스스로 풀 수 있는 유일한 길이라, 없으면 문의 말고는 방법이 없었다.
    PASSWORD_RESET(
        headerLabel = "비밀번호 재설정",
        testTag = "auth-step-password-reset",
        screenDescription = "비밀번호 재설정 메일 요청 화면",
        progress = null
    ),
    NICKNAME(
        headerLabel = "프로필 설정",
        testTag = "auth-step-nickname",
        screenDescription = "닉네임 선택 화면",
        progress = AuthProgress(current = 5, total = 8)
    ),
    CHARACTER(
        headerLabel = "프로필 설정",
        testTag = "auth-step-profile-image",
        screenDescription = "프로필 이미지 생성 선택 화면",
        progress = AuthProgress(current = 8, total = 8)
    ),
    BASIC_INFO(
        headerLabel = "프로필 설정",
        testTag = "auth-step-basic-info",
        screenDescription = "생년월일 성별 선택 화면",
        progress = AuthProgress(current = 6, total = 8)
    ),

    // changeLog13 — 기본 정보(6/8) 다음의 여행 취향 단계. 스타일과 지역을 한 화면에서 함께 받는다.
    TASTE(
        headerLabel = "프로필 설정",
        testTag = "auth-step-taste",
        screenDescription = "여행 취향 선택 화면",
        progress = AuthProgress(current = 7, total = 8)
    ),
    TERMS(
        // 약관 동의는 프로필 8단계 밖의 보조 화면이다 — 8/8 프로그레스를 다시 그리지 않는다
        headerLabel = "약관 동의",
        testTag = "auth-step-terms",
        screenDescription = "약관 동의 화면",
        progress = null
    )
}

private data class AuthProgress(val current: Int, val total: Int)

private data class OnboardingPage(
    val number: Int,
    val title: String,
    val subtitle: String,
    val badge: String,
    val body: String,
    val imageRes: Int
)

private fun onboardingPageFor(step: AuthStep): OnboardingPage = when (step) {
    AuthStep.ONBOARDING_ONE -> OnboardingPage(
        number = 1,
        title = "고민 없이 고르는\n경북 코스",
        subtitle = "날씨와 취향에 맞춰 추천해요",
        badge = "1/3",
        body = "날씨와 취향에 맞춰\n오늘 떠나기 좋은 코스를 추천해요.",
        imageRes = R.drawable.onboarding_1
    )

    AuthStep.ONBOARDING_TWO -> OnboardingPage(
        number = 2,
        title = "3명이 모이면\n채팅방이 열려요",
        subtitle = "모집 확정 후 바로 대화해요",
        badge = "2/3",
        body = "모집이 확정되면\n바로 대화가 시작돼요.",
        imageRes = R.drawable.onboarding_2
    )

    else -> OnboardingPage(
        number = 3,
        title = "여행 뒤엔\n자연스럽게 친구로",
        subtitle = "경로 피드와 도감으로 남겨요",
        badge = "3/3",
        body = "경로 피드와 도감으로\n함께한 순간을 남겨요.",
        imageRes = R.drawable.onboarding_3
    )
}
