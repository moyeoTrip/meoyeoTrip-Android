package kr.hanchae.moyeotrip.domain.auth

import kr.hanchae.moyeotrip.data.auth.AuthApiException

enum class AuthDestination {
    LOGIN,
    NICKNAME,
    PROFILE_IMAGE,
    COMPLETE
}

enum class ProfileRetryAction {
    GENERATE,
    SELECT
}

data class AuthFlowState(
    val destination: AuthDestination = AuthDestination.LOGIN,
    val identity: IdentityToken? = null,
    val nickname: NicknameSelectionState = NicknameSelectionState.empty(),
    val profileImages: ProfileImageCandidates? = null,
    val selectedProfileImageId: Long? = null,
    val profileRetryAction: ProfileRetryAction? = null,
    val isLoading: Boolean = false,
    val isGeneratingProfileImage: Boolean = false,
    val errorMessage: String? = null,
    val noticeMessage: String? = null
) {
    val canSubmitProfileImage: Boolean
        get() = !isLoading && selectedProfileImageId != null
}

class AuthFlowCoordinator(
    private val identityTokenProvider: IdentityTokenProvider,
    private val authGateway: AuthGateway,
    private val sessionStore: AuthSessionStore,
    private val userProfileStore: UserProfileStore = kr.hanchae.moyeotrip.data.auth.InMemoryUserProfileStore(),
    private val onFcmTokenRegistered: (String) -> Unit = {},
    private val onStateChange: (AuthFlowState) -> Unit = {}
) {
    var state: AuthFlowState = AuthFlowState()
        private set

    suspend fun restoreSession() {
        val stored = sessionStore.current
        val refreshToken = stored.refreshToken
        val provider = stored.provider
        if (refreshToken.isNullOrBlank() || provider == null) {
            if (stored.accessToken != null || stored.signupState != null || stored.provider != null) {
                sessionStore.clear()
            }
            return
        }

        update(state.copy(isLoading = true, errorMessage = null, noticeMessage = null))
        runCatching { authGateway.refresh(refreshToken) }
            .onSuccess { refreshed ->
                sessionStore.saveSignup(provider, refreshed)
                userProfileStore.updateFromAccessToken(refreshed.accessToken)
                when (refreshed.signupState) {
                    SignupState.SIGNUP_COMPLETE -> {
                        runCatching { authGateway.profileImages(refreshed.accessToken) }
                            .getOrNull()
                            ?.candidates
                            ?.firstOrNull(ProfileImageCandidate::selected)
                            ?.profileImageUrl
                            ?.let(userProfileStore::saveProfileImage)
                        update(state.copy(destination = AuthDestination.COMPLETE, isLoading = false))
                    }

                    SignupState.PROFILE_IMAGE_REQUIRED -> {
                        runCatching { authGateway.profileImages(refreshed.accessToken) }
                            .onSuccess { images ->
                                update(
                                    state.copy(
                                        destination = AuthDestination.PROFILE_IMAGE,
                                        profileImages = images,
                                        selectedProfileImageId = images.selectedCandidateId,
                                        isLoading = false
                                    )
                                )
                            }
                            .onFailure { error ->
                                // 40919 는 서버가 이미 이미지 설정을 끝냈다는 뜻이다 — 사용자 잘못이 아니므로
                                // 오류를 띄우지 않고 그대로 홈으로 보낸다(정본 R2).
                                if (error.isProfileImageAlreadySet()) {
                                    update(signupFinishedByServer())
                                } else {
                                    showError(error)
                                }
                            }
                    }

                    SignupState.USER_INFO_REQUIRED -> resumeSignupWithoutReLogin(provider)
                }
            }
            .onFailure { error ->
                if (error is AuthApiException && error.statusCode in setOf(400, 401, 404)) {
                    // 리프레시 토큰이 만료·무효라는 뜻이다(`400 40001`). 시간이 지나면 당연히 일어나는 일이라
                    // 로그인 화면으로 보내는 것 자체가 안내다 — 그 위에 오류를 얹으면 사용자가 뭘 잘못한 것처럼
                    // 보인다(정본 R3). 서버 errorMessage 도 사용자 문구로 쓰지 않는다(R4).
                    sessionStore.clear()
                    userProfileStore.clear()
                    update(
                        state.copy(
                            destination = AuthDestination.LOGIN,
                            isLoading = false,
                            errorMessage = null
                        )
                    )
                } else {
                    // 네트워크 실패·5xx 는 사용자가 조치할 수 있으므로 계속 오류로 보여준다.
                    showError(error)
                }
            }
    }

    /**
     * 세션은 살아 있는데 가입이 안 끝난(`USER_INFO_REQUIRED`) 상태로 복귀했을 때.
     *
     * `POST /auth/signup` 은 Firebase idToken 을 요구하는데 복원 시점에는 그 값이 앱에 없다.
     * Firebase SDK 가 로그인 상태를 기기에 유지하므로 **재로그인 없이** 새로 받아 회원 정보 입력으로
     * 이어간다. 기기에 Firebase 로그인이 남아 있지 않을 때만 로그인 화면으로 보낸다(정본 R2-1).
     */
    private suspend fun resumeSignupWithoutReLogin(provider: AuthProvider) {
        val identity = runCatching { identityTokenProvider.currentIdentity(provider) }.getOrNull()
        if (identity == null) {
            sessionStore.clear()
            userProfileStore.clear()
            update(state.copy(destination = AuthDestination.LOGIN, isLoading = false, errorMessage = null))
            return
        }
        runCatching { authGateway.nicknameCandidates() }
            .onSuccess { candidates ->
                update(
                    state.copy(
                        destination = AuthDestination.NICKNAME,
                        identity = identity,
                        nickname = NicknameSelectionState.fromInitial(candidates),
                        isLoading = false
                    )
                )
            }
            .onFailure(::showError)
    }

    suspend fun login(provider: AuthProvider) {
        require(provider != AuthProvider.EMAIL) { "이메일은 이메일 로그인 화면에서 진행해 주세요." }
        update(state.copy(isLoading = true, errorMessage = null, noticeMessage = null))
        runCatching {
            val identity = identityTokenProvider.acquire(provider)
            finishLogin(identity)
        }.onSuccess(::update).onFailure(::showError)
    }

    suspend fun loginWithEmail(request: EmailAuthRequest) {
        update(state.copy(isLoading = true, errorMessage = null, noticeMessage = null))
        runCatching {
            val identity = identityTokenProvider.acquireEmail(request)
            finishLogin(identity)
        }.onSuccess(::update).onFailure(::showError)
    }

    suspend fun sendPasswordReset(email: String) {
        update(state.copy(isLoading = true, errorMessage = null, noticeMessage = null))
        runCatching { identityTokenProvider.sendPasswordReset(email) }
            .onSuccess {
                update(
                    state.copy(
                        isLoading = false,
                        noticeMessage = "비밀번호 재설정 메일을 보냈어요."
                    )
                )
            }
            .onFailure(::showError)
    }

    private suspend fun finishLogin(identity: IdentityToken): AuthFlowState {
        val result = authGateway.login(identity)
        identity.fcmToken?.let(onFcmTokenRegistered)
        require(result.providerType == identity.provider) { "로그인 제공자 정보가 일치하지 않아요." }
        sessionStore.saveLogin(identity.provider, result)
        result.accessToken?.let(userProfileStore::updateFromAccessToken)
        return when (result.signupState) {
            SignupState.SIGNUP_COMPLETE -> {
                require(!result.accessToken.isNullOrBlank() && !result.refreshToken.isNullOrBlank()) {
                    "로그인 토큰을 받지 못했어요."
                }
                runCatching { authGateway.profileImages(requireNotNull(result.accessToken)) }
                    .getOrNull()
                    ?.candidates
                    ?.firstOrNull(ProfileImageCandidate::selected)
                    ?.profileImageUrl
                    ?.let(userProfileStore::saveProfileImage)
                state.copy(
                    destination = AuthDestination.COMPLETE,
                    identity = identity,
                    isLoading = false
                )
            }

            SignupState.USER_INFO_REQUIRED -> {
                val candidates = authGateway.nicknameCandidates()
                state.copy(
                    destination = AuthDestination.NICKNAME,
                    identity = identity,
                    nickname = NicknameSelectionState.fromInitial(candidates),
                    isLoading = false
                )
            }

            SignupState.PROFILE_IMAGE_REQUIRED -> {
                val accessToken = requireNotNull(result.accessToken) { "프로필 설정 토큰을 받지 못했어요." }
                runCatching { authGateway.profileImages(accessToken) }
                    .fold(
                        onSuccess = { images ->
                            state.copy(
                                destination = AuthDestination.PROFILE_IMAGE,
                                identity = identity,
                                profileImages = images,
                                selectedProfileImageId = images.selectedCandidateId,
                                isLoading = false
                            )
                        },
                        onFailure = { error ->
                            // 로그인 응답의 signupState 와 실제 이미지 보유가 어긋난 경우다.
                            // 서버가 "이미 끝났다"(40919)고 하면 그 말이 맞다 — 오류 없이 홈으로 간다(정본 R2).
                            if (error.isProfileImageAlreadySet()) {
                                signupFinishedByServer().copy(identity = identity)
                            } else {
                                throw error
                            }
                        }
                    )
            }
        }
    }

    fun selectNickname(nickname: String) {
        update(state.copy(nickname = state.nickname.select(nickname), errorMessage = null))
    }

    suspend fun refreshNicknames() {
        if (!state.nickname.canRefresh) return
        update(
            state.copy(
                nickname = state.nickname.beginRefresh(),
                isLoading = true,
                errorMessage = null
            )
        )
        runCatching { authGateway.nicknameCandidates() }
            .onSuccess { response ->
                update(
                    state.copy(
                        nickname = state.nickname.completeRefresh(response),
                        isLoading = false
                    )
                )
            }
            .onFailure { error ->
                val message = error.userMessage()
                update(
                    state.copy(
                        nickname = state.nickname.failRefresh(message),
                        isLoading = false,
                        errorMessage = message
                    )
                )
            }
    }

    suspend fun signup(
        gender: Gender,
        birthDate: String,
        agreedTermIds: List<Long>,
        // 07 취향 단계에서 고른 서버 id. 지금까지는 가입 요청에 실리지 않아 조용히 유실됐다(정본 R4).
        travelStyleIds: List<Long> = emptyList(),
        interestedRegionIds: List<Long> = emptyList()
    ) {
        val identity = state.identity ?: return showError(IllegalStateException("로그인 정보를 다시 확인해 주세요."))
        val nickname = state.nickname.selectedNickname
            ?: return showError(IllegalStateException("닉네임을 선택해 주세요."))
        update(state.copy(isLoading = true, errorMessage = null))
        runCatching {
            val session = authGateway.signup(
                identity = identity,
                input = SignupInput(
                    nicknameSelectionToken = state.nickname.selectionToken,
                    nickname = nickname,
                    gender = gender,
                    birthDate = birthDate,
                    agreedTermIds = agreedTermIds,
                    travelStyleIds = travelStyleIds,
                    interestedRegionIds = interestedRegionIds
                )
            )
            identity.fcmToken?.let(onFcmTokenRegistered)
            sessionStore.saveSignup(identity.provider, session)
            userProfileStore.updateFromAccessToken(session.accessToken)
            val selectedCandidate = state.nickname.candidates.firstOrNull { it.nickname == nickname }
            userProfileStore.saveNickname(nickname, selectedCandidate?.color)
            when (session.signupState) {
                SignupState.SIGNUP_COMPLETE -> state.copy(
                    destination = AuthDestination.COMPLETE,
                    isLoading = false
                )

                SignupState.PROFILE_IMAGE_REQUIRED -> {
                    runCatching { authGateway.profileImages(session.accessToken) }
                        .fold(
                            onSuccess = { images ->
                                state.copy(
                                    destination = AuthDestination.PROFILE_IMAGE,
                                    profileImages = images,
                                    selectedProfileImageId = images.selectedCandidateId,
                                    isLoading = false
                                )
                            },
                            onFailure = { error ->
                                if (error.isProfileImageAlreadySet()) signupFinishedByServer() else throw error
                            }
                        )
                }

                SignupState.USER_INFO_REQUIRED -> error("회원가입 정보가 저장되지 않았어요. 다시 시도해 주세요.")
            }
        }.onSuccess(::update).onFailure(::showError)
    }

    /**
     * 프로필 이미지 단계(화면기획 07)로 **바로 들어온** 경로에서 후보를 읽어온다.
     *
     * 로그인·세션 복원을 거쳐 들어오면 그쪽에서 이미 후보를 채우지만, 그 두 경로를 타지 않고
     * 이 단계 화면이 먼저 열리면 지금까지 서버에 아무것도 묻지 않았다 — 후보가 비고
     * "남은 생성 횟수"가 서버 값이 아니라 기본값 0 으로 그려졌다.
     *
     * 여기서 `GET /users/me/profile-images` 가 `40919` 를 주면 서버가 이미 끝났다는 뜻이므로
     * 오류를 띄우지 않고 조용히 홈으로 넘긴다(정본 R2). 세션의 가입 상태도 함께 맞춘다.
     *
     * @param advanceWhenAlreadyComplete `40919` 에서 홈으로 넘길지. 이 단계 화면을 **직접 지정해서**
     * 연 진입(QA·번호별 비교 캡처의 `moyeo_screen=profile-image`)에서는 false 다 — 가입을 이미 마친
     * 계정으로 열면 서버가 늘 `40919` 를 주므로, 넘겨 버리면 07 자리에 홈 화면이 찍힌다.
     * 오류로 띄우지 않는다는 정본 R2 는 그대로 지키고 후보만 빈 상태로 그린다(iOS 와 같은 화면).
     */
    suspend fun loadProfileImageCandidates(advanceWhenAlreadyComplete: Boolean = true) {
        if (state.profileImages != null || state.isLoading) return
        // 세션이 없으면 물어볼 토큰이 없다 — 미로그인 진입은 화면을 그대로 둔다.
        val accessToken = sessionStore.current.accessToken?.takeIf(String::isNotBlank) ?: return
        update(state.copy(isLoading = true, errorMessage = null))
        runCatching { authGateway.profileImages(accessToken) }
            .onSuccess { images ->
                update(
                    state.copy(
                        destination = AuthDestination.PROFILE_IMAGE,
                        profileImages = images,
                        selectedProfileImageId = images.selectedCandidateId,
                        isLoading = false
                    )
                )
            }
            .onFailure { error ->
                if (error.isProfileImageAlreadySet()) {
                    if (advanceWhenAlreadyComplete) {
                        update(signupFinishedByServer())
                    } else {
                        // 서버가 후보를 내려주지 않는다. 없는 후보를 지어내지 않고 빈 상태로 둔다 —
                        // 남은 생성 횟수도 서버가 알려준 적이 없으므로 0 이다(더 만들 수 없는 게 사실이다).
                        update(
                            state.copy(
                                destination = AuthDestination.PROFILE_IMAGE,
                                profileImages = ProfileImageCandidates(
                                    candidates = emptyList(),
                                    generationCount = 0,
                                    remainingGenerationCount = 0,
                                    signupState = SignupState.SIGNUP_COMPLETE
                                ),
                                selectedProfileImageId = null,
                                isLoading = false,
                                errorMessage = null
                            )
                        )
                    }
                } else {
                    showError(error)
                }
            }
    }

    fun selectProfileImage(profileImageId: Long) {
        if (state.isLoading ||
            state.profileImages?.candidates?.none { it.profileImageId == profileImageId } != false
        ) {
            return
        }
        update(state.copy(selectedProfileImageId = profileImageId, errorMessage = null))
    }

    suspend fun generateProfileImage() {
        val accessToken = sessionStore.current.accessToken
            ?: return showError(IllegalStateException("로그인 세션이 만료됐어요. 다시 로그인해 주세요."))
        if (state.profileImages?.remainingGenerationCount == 0) return
        update(state.copy(isLoading = true, isGeneratingProfileImage = true, errorMessage = null))
        runCatching { authGateway.generateProfileImage(accessToken) }
            .onSuccess { images ->
                val previousIds = state.profileImages?.candidates.orEmpty().mapTo(mutableSetOf()) {
                    it.profileImageId
                }
                val accumulated = images.accumulateAfter(state.profileImages)
                val newCandidateId = accumulated.candidates
                    .lastOrNull { it.profileImageId !in previousIds }
                    ?.profileImageId
                update(
                    state.copy(
                        profileImages = accumulated,
                        selectedProfileImageId = newCandidateId
                            ?: accumulated.selectedCandidateId
                            ?: state.selectedProfileImageId,
                        isLoading = false,
                        isGeneratingProfileImage = false,
                        profileRetryAction = null
                    )
                )
            }
            .onFailure { error ->
                // 후보 생성은 PROFILE_IMAGE_REQUIRED 전용 API 다. 40919 면 다른 경로에서 이미 설정이 끝난 것이라
                // "새 후보를 못 만들었다"는 오류가 아니라 다음 화면으로 넘어갈 신호다(정본 R2).
                if (error.isProfileImageAlreadySet()) {
                    update(signupFinishedByServer())
                    return@onFailure
                }
                update(
                    state.copy(
                        isLoading = false,
                        isGeneratingProfileImage = false,
                        errorMessage = error.userMessage(),
                        profileRetryAction = ProfileRetryAction.GENERATE
                    )
                )
            }
    }

    suspend fun completeProfileImage() {
        val accessToken = sessionStore.current.accessToken
            ?: return showError(IllegalStateException("로그인 세션이 만료됐어요. 다시 로그인해 주세요."))
        val imageId = state.selectedProfileImageId
            ?: return showError(IllegalStateException("프로필 이미지를 선택해 주세요."))
        update(state.copy(isLoading = true, errorMessage = null))
        runCatching { authGateway.selectProfileImage(accessToken, imageId) }
            .mapCatching { result ->
                require(result.signupState == SignupState.SIGNUP_COMPLETE) {
                    "프로필 설정을 완료하지 못했어요."
                }
                sessionStore.completeProfile()
                userProfileStore.saveProfileImage(result.selectedImage.profileImageUrl)
                state.copy(
                    destination = AuthDestination.COMPLETE,
                    isLoading = false,
                    profileRetryAction = null
                )
            }
            .onSuccess(::update)
            .onFailure { error ->
                // 선택도 PROFILE_IMAGE_REQUIRED 전용이다. 40919 는 이미 원하는 결과에 도달했다는 뜻이므로
                // 다시 시도를 권하지 않고 조용히 완료 처리한다(정본 R2).
                if (error.isProfileImageAlreadySet()) {
                    update(signupFinishedByServer())
                    return@onFailure
                }
                update(
                    state.copy(
                        isLoading = false,
                        errorMessage = error.userMessage(),
                        profileRetryAction = ProfileRetryAction.SELECT
                    )
                )
            }
    }

    /**
     * 서버가 "가입은 이미 끝났다"(`40919`)고 알려온 경우의 상태.
     * 세션의 가입 상태도 함께 맞춘다 — 안 맞추면 다음 진입에서 또 프로필 이미지 단계로 돌아간다.
     */
    private fun signupFinishedByServer(): AuthFlowState {
        sessionStore.completeProfile()
        return state.copy(
            destination = AuthDestination.COMPLETE,
            isLoading = false,
            isGeneratingProfileImage = false,
            errorMessage = null,
            profileRetryAction = null
        )
    }

    suspend fun retryProfileAction() {
        when (state.profileRetryAction) {
            ProfileRetryAction.GENERATE -> generateProfileImage()
            ProfileRetryAction.SELECT -> completeProfileImage()
            null -> Unit
        }
    }

    fun clearError() {
        update(state.copy(errorMessage = null, noticeMessage = null))
    }

    private fun showError(error: Throwable) {
        // 사용자가 스스로 그만둔 것은 "문제가 생겼다"가 아니다 — 로딩만 걷고 화면은 그대로 둔다(정본 R1·R2).
        if (error is SocialLoginCancelledException) {
            update(state.copy(isLoading = false, isGeneratingProfileImage = false, errorMessage = null))
            return
        }
        update(state.copy(isLoading = false, errorMessage = error.userMessage()))
    }

    private fun update(next: AuthFlowState) {
        state = next
        onStateChange(next)
    }
}

/**
 * 프로필 이미지 API 3종이 돌려주는 `409 40919`.
 * "이미 프로필 이미지 설정을 완료했습니다" 는 실패가 아니라 이미 도달한 결과다(정본 R2).
 */
private fun Throwable.isProfileImageAlreadySet(): Boolean = this is AuthApiException && profileImageAlreadySet

private fun Throwable.userMessage(): String = message
    ?.takeIf(String::isNotBlank)
    ?: "요청을 완료하지 못했어요. 잠시 후 다시 시도해 주세요."

private val ProfileImageCandidates.selectedCandidateId: Long?
    get() = candidates.firstOrNull(ProfileImageCandidate::selected)?.profileImageId

private fun ProfileImageCandidates.accumulateAfter(previous: ProfileImageCandidates?): ProfileImageCandidates {
    val byId = linkedMapOf<Long, ProfileImageCandidate>()
    previous?.candidates.orEmpty().forEach { byId[it.profileImageId] = it }
    candidates.forEach { byId[it.profileImageId] = it }
    return copy(candidates = byId.values.toList())
}
