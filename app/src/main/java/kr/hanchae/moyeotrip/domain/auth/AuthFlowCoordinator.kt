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
                            .onFailure(::showError)
                    }

                    SignupState.USER_INFO_REQUIRED -> {
                        sessionStore.clear()
                        userProfileStore.clear()
                        update(state.copy(destination = AuthDestination.LOGIN, isLoading = false))
                    }
                }
            }
            .onFailure { error ->
                if (error is AuthApiException && error.statusCode in setOf(400, 401, 404)) {
                    sessionStore.clear()
                    userProfileStore.clear()
                }
                showError(error)
            }
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
                val images = authGateway.profileImages(accessToken)
                state.copy(
                    destination = AuthDestination.PROFILE_IMAGE,
                    identity = identity,
                    profileImages = images,
                    selectedProfileImageId = images.selectedCandidateId,
                    isLoading = false
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

    suspend fun signup(gender: Gender, birthDate: String) {
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
                    birthDate = birthDate
                )
            )
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
                    val images = authGateway.profileImages(session.accessToken)
                    state.copy(
                        destination = AuthDestination.PROFILE_IMAGE,
                        profileImages = images,
                        selectedProfileImageId = images.selectedCandidateId,
                        isLoading = false
                    )
                }

                SignupState.USER_INFO_REQUIRED -> error("회원가입 정보가 저장되지 않았어요. 다시 시도해 주세요.")
            }
        }.onSuccess(::update).onFailure(::showError)
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
                update(
                    state.copy(
                        isLoading = false,
                        errorMessage = error.userMessage(),
                        profileRetryAction = ProfileRetryAction.SELECT
                    )
                )
            }
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
        update(state.copy(isLoading = false, errorMessage = error.userMessage()))
    }

    private fun update(next: AuthFlowState) {
        state = next
        onStateChange(next)
    }
}

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
