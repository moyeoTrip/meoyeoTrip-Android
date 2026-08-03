package kr.hanchae.moyeotrip.ui.screens

import android.app.DatePickerDialog
import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
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
import java.net.URL
import java.time.LocalDate
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.hanchae.moyeotrip.R
import kr.hanchae.moyeotrip.data.MockTripRepository
import kr.hanchae.moyeotrip.data.auth.AuthDependencies
import kr.hanchae.moyeotrip.domain.auth.AuthDestination
import kr.hanchae.moyeotrip.domain.auth.AuthFlowCoordinator
import kr.hanchae.moyeotrip.domain.auth.AuthFlowState
import kr.hanchae.moyeotrip.domain.auth.AuthProvider
import kr.hanchae.moyeotrip.domain.auth.EmailAuthAction
import kr.hanchae.moyeotrip.domain.auth.EmailAuthRequest
import kr.hanchae.moyeotrip.domain.auth.Gender
import kr.hanchae.moyeotrip.domain.auth.NicknameCandidate
import kr.hanchae.moyeotrip.domain.auth.NicknameSelectionState
import kr.hanchae.moyeotrip.domain.auth.ProfileImageCandidate
import kr.hanchae.moyeotrip.ui.theme.Coral
import kr.hanchae.moyeotrip.ui.theme.ForestGreen
import kr.hanchae.moyeotrip.ui.theme.SkyBlue
import kr.hanchae.moyeotrip.ui.theme.SunYellow

@Composable
fun AuthFlowScreen(
    onComplete: () -> Unit,
    onExit: () -> Unit = {},
    providedDependencies: AuthDependencies? = null,
    allowExit: Boolean = true
) {
    val context = LocalContext.current
    val dependencies = remember(providedDependencies, context) {
        providedDependencies ?: AuthDependencies.appDefault(context)
    }
    var authState by remember { mutableStateOf(AuthFlowState()) }
    val coordinator = remember(dependencies) {
        AuthFlowCoordinator(
            identityTokenProvider = dependencies.identityTokenProvider,
            authGateway = dependencies.authGateway,
            sessionStore = dependencies.sessionStore,
            userProfileStore = dependencies.userProfileStore,
            onStateChange = { authState = it }
        )
    }
    var stepName by rememberSaveable { mutableStateOf(AuthStep.ONBOARDING_ONE.name) }
    val coroutineScope = rememberCoroutineScope()
    var lastProviderName by rememberSaveable { mutableStateOf(AuthProvider.KAKAO.name) }
    var selectedBirth by rememberSaveable { mutableStateOf("") }
    var selectedGender by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordConfirmation by rememberSaveable { mutableStateOf("") }
    var emailActionName by rememberSaveable { mutableStateOf(EmailAuthAction.SIGN_IN.name) }
    val step = AuthStep.valueOf(stepName)

    LaunchedEffect(authState.destination) {
        when (authState.destination) {
            AuthDestination.LOGIN -> Unit

            AuthDestination.NICKNAME -> stepName = AuthStep.NICKNAME.name

            AuthDestination.PROFILE_IMAGE -> stepName = AuthStep.CHARACTER.name

            AuthDestination.COMPLETE -> {
                MockTripRepository.completeAuthFlow()
                onComplete()
            }
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

            AuthStep.NICKNAME -> AuthStep.LOGIN.name

            AuthStep.CHARACTER -> {
                onExit()
                step.name
            }

            AuthStep.BASIC_INFO -> AuthStep.NICKNAME.name
        }
    }

    AuthFlowFrame(
        step = step,
        onBack = goBack,
        onClose = onExit,
        showBack = allowExit || step != AuthStep.ONBOARDING_ONE,
        showClose = allowExit
    ) {
        when (step) {
            AuthStep.SPLASH -> SplashStep(onNext = { stepName = AuthStep.ONBOARDING_ONE.name })

            AuthStep.ONBOARDING_ONE,
            AuthStep.ONBOARDING_TWO,
            AuthStep.ONBOARDING_THREE -> {
                OnboardingStep(
                    page = onboardingPageFor(step),
                    onNext = {
                        stepName = when (step) {
                            AuthStep.ONBOARDING_ONE -> AuthStep.ONBOARDING_TWO.name
                            AuthStep.ONBOARDING_TWO -> AuthStep.ONBOARDING_THREE.name
                            else -> AuthStep.LOGIN.name
                        }
                    }
                )
            }

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
                passwordConfirmation = passwordConfirmation,
                action = EmailAuthAction.valueOf(emailActionName),
                isLoading = authState.isLoading,
                errorMessage = authState.errorMessage,
                noticeMessage = authState.noticeMessage,
                onEmailChange = { email = it },
                onPasswordChange = { password = it },
                onPasswordConfirmationChange = { passwordConfirmation = it },
                onActionChange = {
                    emailActionName = it.name
                    coordinator.clearError()
                },
                onSubmit = {
                    coroutineScope.launch {
                        coordinator.loginWithEmail(
                            EmailAuthRequest(email, password, EmailAuthAction.valueOf(emailActionName))
                        )
                    }
                },
                onResetPassword = { coroutineScope.launch { coordinator.sendPasswordReset(email) } }
            )

            AuthStep.NICKNAME -> NicknameStep(
                state = authState.nickname,
                onSelectNickname = coordinator::selectNickname,
                onRefresh = { coroutineScope.launch { coordinator.refreshNicknames() } },
                onNext = { stepName = AuthStep.BASIC_INFO.name }
            )

            AuthStep.CHARACTER -> ProfileImageStep(
                state = authState,
                onSelect = coordinator::selectProfileImage,
                onGenerate = { coroutineScope.launch { coordinator.generateProfileImage() } },
                onComplete = { coroutineScope.launch { coordinator.completeProfileImage() } },
                onRetry = { coroutineScope.launch { coordinator.retryProfileAction() } }
            )

            AuthStep.BASIC_INFO -> BasicInfoStep(
                selectedBirth = selectedBirth,
                selectedGender = selectedGender,
                onSelectBirth = { selectedBirth = it },
                onSelectGender = { selectedGender = it },
                isLoading = authState.isLoading,
                errorMessage = authState.errorMessage,
                onNext = {
                    coroutineScope.launch {
                        coordinator.signup(
                            gender = Gender.valueOf(selectedGender),
                            birthDate = selectedBirth
                        )
                    }
                },
                onRetry = {
                    coroutineScope.launch {
                        coordinator.signup(
                            gender = Gender.valueOf(selectedGender),
                            birthDate = selectedBirth
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun AuthFlowFrame(
    step: AuthStep,
    onBack: () -> Unit,
    onClose: () -> Unit,
    showBack: Boolean,
    showClose: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val progress = step.progress

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .testTag("auth-flow")
            .semantics { contentDescription = "인증 플로우" }
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 30.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                if (showClose) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("auth-flow-close")
                            .semantics { contentDescription = "인증 닫기" }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null,
                            tint = colorScheme.onBackground
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(44.dp))
                }
            }
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
                if (progress != null) {
                    Text(
                        text = "${progress.current}/${progress.total}",
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
            if (progress != null) {
                LinearProgressIndicator(
                    progress = { progress.current / progress.total.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = colorScheme.primary,
                    trackColor = colorScheme.surfaceVariant
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(step.testTag)
                .semantics { contentDescription = step.screenDescription },
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SplashStep(onNext: () -> Unit) {
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
    AuthPrimaryButton(
        text = "시작하기",
        tag = "auth-splash-next",
        contentDescription = "스플래시 다음",
        onClick = onNext
    )
}

@Composable
private fun OnboardingStep(page: OnboardingPage, onNext: () -> Unit) {
    AuthHeroPanel(
        icon = page.icon,
        title = page.title,
        subtitle = page.subtitle,
        accentColor = page.accentColor
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = page.badge,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = page.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    AuthPrimaryButton(
        text = if (page.number == 3) "로그인 시작" else "다음",
        tag = "auth-onboarding-next",
        contentDescription = "온보딩 ${page.number} 다음",
        onClick = onNext
    )
}

@Composable
private fun LoginStep(
    isLoading: Boolean,
    errorMessage: String?,
    onProviderClick: (AuthProvider) -> Unit,
    onRetry: () -> Unit
) {
    StepTitle(
        title = "모여트립에 오신 걸 환영해요",
        subtitle = "30초 안에 시작할 수 있어요"
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
    val palette = googleButtonPalette(isSystemInDarkTheme())
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
    val darkTheme = isSystemInDarkTheme()
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
    iconStartPadding: androidx.compose.ui.unit.Dp = 12.dp,
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
            Image(
                painter = painterResource(icon),
                contentDescription = iconDescription,
                modifier = Modifier
                    .padding(start = iconStartPadding)
                    .size(iconSize)
                    .align(Alignment.CenterStart),
                contentScale = ContentScale.Fit,
                alpha = if (enabled) 1f else 0.5f
            )
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

@Composable
private fun EmailLoginStep(
    email: String,
    password: String,
    passwordConfirmation: String,
    action: EmailAuthAction,
    isLoading: Boolean,
    errorMessage: String?,
    noticeMessage: String?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordConfirmationChange: (String) -> Unit,
    onActionChange: (EmailAuthAction) -> Unit,
    onSubmit: () -> Unit,
    onResetPassword: () -> Unit
) {
    StepTitle(
        title = "이메일로 시작하기",
        subtitle = "Firebase 이메일 계정으로 로그인하거나 새 계정을 만들어요."
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        EmailModeButton(
            text = "로그인",
            selected = action == EmailAuthAction.SIGN_IN,
            tag = "auth-email-mode-sign-in",
            onClick = { onActionChange(EmailAuthAction.SIGN_IN) }
        )
        EmailModeButton(
            text = "새 계정 만들기",
            selected = action == EmailAuthAction.CREATE_ACCOUNT,
            tag = "auth-email-mode-create",
            onClick = { onActionChange(EmailAuthAction.CREATE_ACCOUNT) }
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            modifier = Modifier.fillMaxWidth().testTag("auth-email-address"),
            label = { Text("이메일") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            enabled = !isLoading
        )
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            modifier = Modifier.fillMaxWidth().testTag("auth-email-password"),
            label = { Text("비밀번호") },
            supportingText = { Text("6자 이상 입력해 주세요") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            enabled = !isLoading
        )
        if (action == EmailAuthAction.CREATE_ACCOUNT) {
            val passwordsMismatch = passwordConfirmation.isNotEmpty() && password != passwordConfirmation
            OutlinedTextField(
                value = passwordConfirmation,
                onValueChange = onPasswordConfirmationChange,
                modifier = Modifier.fillMaxWidth().testTag("auth-email-password-confirmation"),
                label = { Text("비밀번호 확인") },
                supportingText = {
                    if (passwordsMismatch) Text("비밀번호가 일치하지 않아요.")
                },
                isError = passwordsMismatch,
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                enabled = !isLoading
            )
        }
        AuthPrimaryButton(
            text = if (action == EmailAuthAction.SIGN_IN) "이메일로 로그인" else "새 계정 만들기",
            tag = "auth-email-submit",
            contentDescription = if (action == EmailAuthAction.SIGN_IN) "이메일 로그인" else "이메일 새 계정 만들기",
            enabled = !isLoading &&
                emailCredentialsError(email, password, passwordConfirmation, action) == null,
            onClick = onSubmit
        )
        TextButton(
            onClick = onResetPassword,
            enabled = !isLoading && email.contains('@'),
            modifier = Modifier.align(Alignment.CenterHorizontally).testTag("auth-email-reset")
        ) {
            Text("비밀번호를 잊으셨나요?")
        }
    }
    if (isLoading) AuthLoadingStatus("Firebase 계정을 확인하고 있어요...")
    noticeMessage?.let {
        Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
    }
    errorMessage?.let { AuthErrorCard(message = it, onRetry = onSubmit) }
}

@Composable
private fun RowScope.EmailModeButton(text: String, selected: Boolean, tag: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag(tag),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
            contentColor = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun NicknameStep(
    state: NicknameSelectionState,
    onSelectNickname: (String) -> Unit,
    onRefresh: () -> Unit,
    onNext: () -> Unit
) {
    StepTitle(
        title = "어떤 친구로 시작할까요?",
        subtitle = "본명 대신 동물 친구로 만나요.\n이름을 고르면 캐릭터를 그려드릴게요."
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (state.isLoading) {
            repeat(3) { index -> NicknameCandidateSkeleton(index) }
        } else {
            state.candidates.forEachIndexed { index, candidate ->
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
    AuthPrimaryButton(
        text = "다음",
        tag = "auth-nickname-next",
        contentDescription = "닉네임 선택 완료",
        enabled = state.canContinue,
        onClick = onNext
    )
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
                    text = candidate.animalEmoji(),
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
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

private fun NicknameCandidate.animalEmoji(): String = when (animal) {
    "사슴" -> "🦌"
    "거북이" -> "🐢"
    "토끼" -> "🐰"
    "여우" -> "🦊"
    "수달", "해달" -> "🦦"
    "다람쥐" -> "🐿️"
    "고양이" -> "🐱"
    "강아지" -> "🐶"
    "판다" -> "🐼"
    "펭귄" -> "🐧"
    "돌고래" -> "🐬"
    "부엉이" -> "🦉"
    "참새" -> "🐦"
    "알파카" -> "🦙"
    "코알라" -> "🐨"
    "두루미" -> "🪽"
    "고슴도치" -> "🦔"
    "너구리" -> "🦝"
    "기린" -> "🦒"
    else -> "🐾"
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
private fun ProfileImageStep(
    state: AuthFlowState,
    onSelect: (Long) -> Unit,
    onGenerate: () -> Unit,
    onComplete: () -> Unit,
    onRetry: () -> Unit
) {
    val images = state.profileImages
    val selectedNickname = state.nickname.selectedNickname
    val selectedNicknameCandidate = state.nickname.candidates.firstOrNull {
        it.nickname == selectedNickname
    }
    StepTitle(
        title = "여행 친구를 만들어볼까요?",
        subtitle = selectedNickname?.let { nickname ->
            "선택한 닉네임 ‘$nickname’을 바탕으로 후보를 만들어요."
        } ?: "서버에 저장된 닉네임을 바탕으로 후보를 만들어요."
    )
    if (state.isGeneratingProfileImage) {
        ProfileImageGeneratingCard(nickname = selectedNickname)
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "프로필 후보 ${images?.candidates?.size ?: 0}개",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${images?.remainingGenerationCount ?: 0}회 남음",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (images?.candidates.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(132.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (state.isLoading) "이미지를 만들고 있어요..." else "아직 만든 이미지가 없어요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    images.candidates.forEachIndexed { index, candidate ->
                        ProfileImageCandidateCard(
                            candidate = candidate,
                            index = index,
                            selected = state.selectedProfileImageId == candidate.profileImageId,
                            enabled = !state.isLoading,
                            nicknameColor = selectedNicknameCandidate?.colorLabel(),
                            accentColor = selectedNicknameCandidate?.swatchColor(),
                            onClick = { onSelect(candidate.profileImageId) }
                        )
                    }
                }
            }
        }
    }
    AuthPrimaryButton(
        text = if (state.isLoading) {
            "새 후보를 만들고 있어요..."
        } else {
            "새 후보 만들기 · 남은 ${images?.remainingGenerationCount ?: 0}회"
        },
        tag = "auth-profile-generate",
        contentDescription = "새 프로필 후보 만들기, 남은 ${images?.remainingGenerationCount ?: 0}회",
        enabled = !state.isLoading && (images?.remainingGenerationCount ?: 0) > 0,
        onClick = onGenerate
    )
    AuthPrimaryButton(
        text = "이 이미지로 시작",
        tag = "auth-profile-complete",
        contentDescription = "프로필 이미지 선택 완료",
        enabled = state.canSubmitProfileImage,
        onClick = onComplete
    )
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
            delay(1_000)
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
                trackColor = MaterialTheme.colorScheme.surfaceVariant
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
    nicknameColor: String?,
    accentColor: Color?,
    onClick: () -> Unit
) {
    val selectionColor = accentColor ?: MaterialTheme.colorScheme.primary
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
            .testTag("auth-profile-option-$index")
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected && accentColor != null) {
            accentColor.copy(alpha = 0.14f)
        } else if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            1.dp,
            if (selected) selectionColor else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(66.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                ProfileImageThumbnail(candidate = candidate)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "여행 친구 ${index + 1}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "서버에서 생성된 프로필 이미지",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (nicknameColor != null) {
                    Text(
                        text = "닉네임 색상 · $nicknameColor",
                        style = MaterialTheme.typography.labelSmall,
                        color = selectionColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "선택됨",
                    tint = selectionColor
                )
            }
        }
    }
}

@Composable
private fun ProfileImageThumbnail(candidate: ProfileImageCandidate) {
    val bitmap by produceState<ImageBitmap?>(initialValue = null, key1 = candidate.profileImageUrl) {
        val isRemoteImage =
            candidate.profileImageUrl.startsWith("https://") ||
                candidate.profileImageUrl.startsWith("http://")
        value =
            if (isRemoteImage) {
                withContext(Dispatchers.IO) {
                    runCatching {
                        URL(candidate.profileImageUrl).openStream().use {
                            BitmapFactory.decodeStream(it)?.asImageBitmap()
                        }
                    }.getOrNull()
                }
            } else {
                null
            }
    }
    if (bitmap != null) {
        Image(
            bitmap = requireNotNull(bitmap),
            contentDescription = "생성된 프로필 이미지",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    } else {
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
    val animal = when {
        nickname.contains("거북이") -> "🐢"
        nickname.contains("너구리") -> "🦝"
        else -> "🦌"
    }

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
private fun BasicInfoStep(
    selectedBirth: String,
    selectedGender: String,
    onSelectBirth: (String) -> Unit,
    onSelectGender: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onNext: () -> Unit,
    onRetry: () -> Unit
) {
    StepTitle(
        title = "기본 정보",
        subtitle = "생년월일과 성별만 먼저 알려주세요."
    )
    Text(
        text = "생년월일",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onBackground
    )
    val selectedBirthDate = remember(selectedBirth) {
        runCatching { LocalDate.parse(selectedBirth) }.getOrNull()
    }
    val birthDateLabel = selectedBirthDate?.let { date ->
        "${date.year}년 ${date.monthValue}월 ${date.dayOfMonth}일"
    } ?: "연도 / 월 / 일"
    val context = LocalContext.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .testTag("auth-birth-date")
            .semantics { contentDescription = "생년월일 $birthDateLabel 선택" }
            .clickable {
                val initialDate = selectedBirthDate ?: LocalDate.of(1998, 4, 12)
                val dialog = DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        onSelectBirth("%04d-%02d-%02d".format(year, month + 1, day))
                    },
                    initialDate.year,
                    initialDate.monthValue - 1,
                    initialDate.dayOfMonth
                )
                dialog.datePicker.maxDate = System.currentTimeMillis()
                dialog.setOnShowListener {
                    val headerId = context.resources.getIdentifier("date_picker_header_date", "id", "android")
                    val header = dialog.findViewById<android.widget.TextView>(headerId)
                    fun updateHeader(year: Int, month: Int, day: Int) {
                        header?.text = String.format(Locale.KOREA, "%d년 %d월 %d일", year, month + 1, day)
                    }
                    dialog.datePicker.init(
                        initialDate.year,
                        initialDate.monthValue - 1,
                        initialDate.dayOfMonth
                    ) { _, year, month, day ->
                        updateHeader(year, month, day)
                    }
                    updateHeader(initialDate.year, initialDate.monthValue - 1, initialDate.dayOfMonth)
                }
                dialog.show()
            },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                imageVector = Icons.Filled.CalendarToday,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
    Text(
        text = "성별",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onBackground
    )
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
    AuthPrimaryButton(
        text = if (isLoading) "계정을 만들고 있어요..." else "가입하고 프로필 만들기",
        tag = "auth-basic-next",
        contentDescription = "기본정보로 계정 만들기",
        enabled = isValidBirthDate(selectedBirth) && selectedGender.isNotBlank() && !isLoading,
        onClick = onNext
    )
    errorMessage?.let { AuthErrorCard(message = it, onRetry = onRetry) }
}

internal fun isValidBirthDate(value: String, today: LocalDate = LocalDate.now()): Boolean = runCatching {
    val date = LocalDate.parse(value)
    !date.isAfter(today) && date.year >= 1900
}.getOrDefault(false)

@Composable
private fun TermsStep(
    agreedAge: Boolean,
    agreedService: Boolean,
    agreedPrivacy: Boolean,
    agreedLocation: Boolean,
    agreedMarketing: Boolean,
    onToggleAll: () -> Unit,
    onToggleAge: () -> Unit,
    onToggleService: () -> Unit,
    onTogglePrivacy: () -> Unit,
    onToggleLocation: () -> Unit,
    onToggleMarketing: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onNext: () -> Unit,
    onRetry: () -> Unit
) {
    val requiredAgreed = agreedAge && agreedService && agreedPrivacy
    val allAgreed = requiredAgreed && agreedLocation && agreedMarketing

    StepTitle(
        title = "약관 동의",
        subtitle = "서비스 이용에 필요한 항목만 먼저 확인해요."
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("auth-terms-all")
            .semantics { contentDescription = "약관 모두 동의" }
            .clickable(onClick = onToggleAll),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (allAgreed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Checkbox(
                checked = allAgreed,
                onCheckedChange = { onToggleAll() },
                modifier = Modifier.testTag("auth-terms-all-checkbox"),
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "모두 동의",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "선택 항목까지 한 번에 동의",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TermsRow(
            title = "만 14세 이상",
            required = true,
            checked = agreedAge,
            tag = "auth-terms-age",
            contentDescription = "만 14세 이상 필수 약관 동의",
            onToggle = onToggleAge
        )
        TermsRow(
            title = "이용약관 동의",
            required = true,
            checked = agreedService,
            tag = "auth-terms-service",
            contentDescription = "이용약관 필수 동의",
            onToggle = onToggleService
        )
        TermsRow(
            title = "개인정보 처리방침",
            required = true,
            checked = agreedPrivacy,
            tag = "auth-terms-privacy",
            contentDescription = "개인정보 처리방침 필수 동의",
            onToggle = onTogglePrivacy
        )
        TermsRow(
            title = "위치정보 이용",
            required = false,
            checked = agreedLocation,
            tag = "auth-terms-location",
            contentDescription = "위치정보 이용 선택 동의",
            onToggle = onToggleLocation
        )
        TermsRow(
            title = "마케팅 정보 수신",
            required = false,
            checked = agreedMarketing,
            tag = "auth-terms-marketing",
            contentDescription = "마케팅 정보 수신 선택 동의",
            onToggle = onToggleMarketing
        )
    }
    AuthPrimaryButton(
        text = if (isLoading) "계정을 만들고 있어요..." else "동의하고 계정 만들기",
        tag = "auth-terms-finish",
        contentDescription = "약관 동의 후 계정 만들기",
        enabled = requiredAgreed && !isLoading,
        onClick = onNext
    )
    errorMessage?.let { AuthErrorCard(message = it, onRetry = onRetry) }
}

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
                    .testTag("auth-error-retry")
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
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag(tag)
            .semantics { this.contentDescription = contentDescription },
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            modifier = Modifier
                .padding(start = 6.dp)
                .size(20.dp)
        )
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
                    .padding(start = 12.dp)
                    .size(26.dp)
                    .background(markBackground, CircleShape)
                    .align(Alignment.CenterStart),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mark,
                    fontSize = 15.sp,
                    color = markColor,
                    fontWeight = FontWeight.SemiBold
                )
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

internal fun emailCredentialsError(
    email: String,
    password: String,
    passwordConfirmation: String,
    action: EmailAuthAction
): String? = when {
    !email.contains('@') -> "이메일 주소를 확인해 주세요."
    password.length < 6 -> "비밀번호는 6자 이상 입력해 주세요."
    action == EmailAuthAction.CREATE_ACCOUNT && password != passwordConfirmation -> "비밀번호가 일치하지 않아요."
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
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
            .semantics { this.contentDescription = contentDescription }
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = if (required) "필수" else "선택",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (required) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
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
        progress = AuthProgress(current = 1, total = 7)
    ),
    ONBOARDING_TWO(
        headerLabel = "온보딩",
        testTag = "auth-step-onboarding-2",
        screenDescription = "온보딩 두 번째 화면",
        progress = AuthProgress(current = 2, total = 7)
    ),
    ONBOARDING_THREE(
        headerLabel = "온보딩",
        testTag = "auth-step-onboarding-3",
        screenDescription = "온보딩 세 번째 화면",
        progress = AuthProgress(current = 3, total = 7)
    ),
    LOGIN(
        headerLabel = "로그인",
        testTag = "auth-step-login",
        screenDescription = "소셜 로그인 화면",
        progress = AuthProgress(current = 4, total = 7)
    ),
    EMAIL(
        headerLabel = "이메일 로그인",
        testTag = "auth-step-email",
        screenDescription = "이메일 로그인 및 계정 생성 화면",
        progress = AuthProgress(current = 4, total = 7)
    ),
    NICKNAME(
        headerLabel = "프로필 설정",
        testTag = "auth-step-nickname",
        screenDescription = "닉네임 선택 화면",
        progress = AuthProgress(current = 5, total = 7)
    ),
    CHARACTER(
        headerLabel = "프로필 설정",
        testTag = "auth-step-profile-image",
        screenDescription = "프로필 이미지 생성 선택 화면",
        progress = AuthProgress(current = 7, total = 7)
    ),
    BASIC_INFO(
        headerLabel = "프로필 설정",
        testTag = "auth-step-basic-info",
        screenDescription = "생년월일 성별 선택 화면",
        progress = AuthProgress(current = 6, total = 7)
    )
}

private data class AuthProgress(val current: Int, val total: Int)

private data class OnboardingPage(
    val number: Int,
    val title: String,
    val subtitle: String,
    val badge: String,
    val body: String,
    val icon: ImageVector,
    val accentColor: Color
)

private fun onboardingPageFor(step: AuthStep): OnboardingPage = when (step) {
    AuthStep.ONBOARDING_ONE -> OnboardingPage(
        number = 1,
        title = "고민 없이 고르는 경북 코스",
        subtitle = "날씨와 취향에 맞춰 추천해요",
        badge = "1/3",
        body = "날씨와 취향에 맞춰 오늘 떠나기 좋은 코스를 추천해요.",
        icon = Icons.Filled.Favorite,
        accentColor = Coral
    )

    AuthStep.ONBOARDING_TWO -> OnboardingPage(
        number = 2,
        title = "3명이 모이면 채팅방이 열려요",
        subtitle = "모집 확정 후 바로 대화해요",
        badge = "2/3",
        body = "모집이 확정되면 바로 대화가 시작돼요.",
        icon = Icons.Filled.CalendarToday,
        accentColor = SkyBlue
    )

    else -> OnboardingPage(
        number = 3,
        title = "여행 뒤엔 자연스럽게 친구로",
        subtitle = "경로 피드와 도감으로 남겨요",
        badge = "3/3",
        body = "경로 피드와 도감으로 함께한 순간을 남겨요.",
        icon = Icons.Filled.Star,
        accentColor = SunYellow
    )
}
