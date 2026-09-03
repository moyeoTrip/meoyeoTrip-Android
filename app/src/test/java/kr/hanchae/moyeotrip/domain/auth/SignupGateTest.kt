package kr.hanchae.moyeotrip.domain.auth

import kotlinx.coroutines.runBlocking
import kr.hanchae.moyeotrip.data.auth.AuthApiException
import kr.hanchae.moyeotrip.data.auth.RefreshingAuthGateway
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * 가입 게이트 계약 (`SIGNUP-GATE-CANON.md` R1·R2).
 *
 * 409 하나에 세 코드(40902·40918·40919)가 몰려 있어 상태 코드만으로는 갈라지지 않는다.
 * 잘못 갈라지면 무한 재발급(40902·40918)이나 "이미 끝난 일"에 대한 오류 화면(40919)이 된다.
 */
class SignupGateTest {
    @Test
    fun gateStagesAreRecognisedByErrorCodeNotByStatus() {
        // 서버가 401 로 감싸 보내더라도 재발급 경로로 새면 안 되므로 코드만으로 판정한다.
        assertEquals(SignupGateStage.USER_INFO, SignupGateStage.ofCode(40902))
        assertEquals(SignupGateStage.PROFILE_IMAGE, SignupGateStage.ofCode(40918))
        // 40919 는 "이미 끝났다" 라 되돌릴 단계가 없다 — 게이트가 아니다.
        assertNull(SignupGateStage.ofCode(40919))
        assertNull(SignupGateStage.ofCode(null))
    }

    @Test
    fun refreshingGatewayNeverRefreshesOnGateCodes() = runBlocking {
        listOf(40902, 40918, SignupGateStage.PROFILE_IMAGE_ALREADY_SET_CODE).forEach { code ->
            val delegate = FailingProfileImageGateway(AuthApiException(409, "가입 미완료", code))
            val gateway = RefreshingAuthGateway(delegate, storeAtProfileImageStep())

            runCatching { gateway.profileImages("access") }

            assertEquals("코드 $code 는 토큰 문제가 아니다", 0, delegate.refreshRequests)
            assertEquals("원래 요청을 되풀이하지 않는다", 1, delegate.profileListRequests)
        }
    }

    @Test
    fun generatingWhenTheServerSaysAlreadyDoneFinishesSignupSilently() = runBlocking {
        val store = storeAtProfileImageStep()
        val coordinator = coordinator(FailingProfileImageGateway(alreadySet()), store)

        coordinator.generateProfileImage()

        assertEquals(AuthDestination.COMPLETE, coordinator.state.destination)
        assertNull(coordinator.state.errorMessage)
        assertNull(coordinator.state.profileRetryAction)
        assertEquals(SignupState.SIGNUP_COMPLETE, store.current.signupState)
    }

    @Test
    fun selectingWhenTheServerSaysAlreadyDoneFinishesSignupSilently() = runBlocking {
        val store = storeAtProfileImageStep()
        // 후보 조회는 되고 선택에서만 40919 가 오는 경우 — 다른 기기에서 먼저 끝낸 상황이다.
        val gateway = FailingProfileImageGateway(alreadySet(), listSucceeds = true)
        val coordinator = coordinator(gateway, store)
        coordinator.restoreSession()
        coordinator.selectProfileImage(41L)

        coordinator.completeProfileImage()

        assertEquals(AuthDestination.COMPLETE, coordinator.state.destination)
        assertNull(coordinator.state.errorMessage)
        assertEquals(SignupState.SIGNUP_COMPLETE, store.current.signupState)
    }

    @Test
    fun restoringASessionTheServerConsidersFinishedGoesStraightHome() = runBlocking {
        val store = storeAtProfileImageStep()
        val coordinator = coordinator(FailingProfileImageGateway(alreadySet()), store)

        coordinator.restoreSession()

        assertEquals(AuthDestination.COMPLETE, coordinator.state.destination)
        assertNull(coordinator.state.errorMessage)
    }

    /**
     * 화면기획 07 로 바로 들어온 경로. 로그인·세션 복원을 안 거쳤으니 여기서 후보를 물어야 하고,
     * 이미 끝난 계정이면 오류 없이 홈으로 가야 한다 — 안 그러면 가입 단계에 갇힌다.
     */
    @Test
    fun openingTheProfileImageStepDirectlyGoesHomeWhenTheServerSaysAlreadyDone() = runBlocking {
        val store = storeAtProfileImageStep()
        val gateway = FailingProfileImageGateway(alreadySet())
        val coordinator = coordinator(gateway, store)

        coordinator.loadProfileImageCandidates()

        assertEquals("후보를 서버에 물어야 한다", 1, gateway.profileListRequests)
        assertEquals(AuthDestination.COMPLETE, coordinator.state.destination)
        assertNull(coordinator.state.errorMessage)
        assertEquals(SignupState.SIGNUP_COMPLETE, store.current.signupState)
    }

    /** 아직 프로필 이미지가 필요한 계정이면 후보와 남은 생성 횟수가 서버 값으로 채워진다. */
    @Test
    fun openingTheProfileImageStepDirectlyLoadsServerCandidates() = runBlocking {
        val store = storeAtProfileImageStep()
        val coordinator = coordinator(FailingProfileImageGateway(alreadySet(), listSucceeds = true), store)

        coordinator.loadProfileImageCandidates()

        assertEquals(AuthDestination.PROFILE_IMAGE, coordinator.state.destination)
        assertEquals(2, coordinator.state.profileImages?.remainingGenerationCount)
        assertNull(coordinator.state.errorMessage)
    }

    @Test
    fun ordinaryProfileImageFailuresStillSurfaceAsErrors() = runBlocking {
        val store = storeAtProfileImageStep()
        val coordinator = coordinator(FailingProfileImageGateway(AuthApiException(500, "서버에러입니다.")), store)

        coordinator.generateProfileImage()

        assertEquals("서버에러입니다.", coordinator.state.errorMessage)
        assertEquals(ProfileRetryAction.GENERATE, coordinator.state.profileRetryAction)
        assertNotEquals(AuthDestination.COMPLETE, coordinator.state.destination)
    }

    private fun alreadySet() = AuthApiException(
        409,
        "이미 프로필 이미지 설정을 완료했습니다.",
        SignupGateStage.PROFILE_IMAGE_ALREADY_SET_CODE
    )

    private fun storeAtProfileImageStep() = InMemoryAuthSessionStore().apply {
        saveSignup(AuthProvider.KAKAO, ServiceSession("access", "refresh", SignupState.PROFILE_IMAGE_REQUIRED))
    }

    private fun coordinator(gateway: AuthGateway, store: AuthSessionStore) = AuthFlowCoordinator(
        identityTokenProvider = { provider -> IdentityToken(provider, "firebase-id-token") },
        authGateway = gateway,
        sessionStore = store
    )
}

private class FailingProfileImageGateway(
    private val profileImageError: AuthApiException,
    private val listSucceeds: Boolean = false
) : AuthGateway {
    var refreshRequests = 0
    var profileListRequests = 0

    override suspend fun login(identity: IdentityToken): LoginResult = error("이 테스트에서는 쓰이지 않는다")

    override suspend fun nicknameCandidates(): NicknameCandidateResponse = error("이 테스트에서는 쓰이지 않는다")

    override suspend fun signup(identity: IdentityToken, input: SignupInput) = error("이 테스트에서는 쓰이지 않는다")

    override suspend fun profileImages(accessToken: String): ProfileImageCandidates {
        profileListRequests += 1
        if (!listSucceeds) throw profileImageError
        return ProfileImageCandidates(
            candidates = listOf(ProfileImageCandidate(41L, "https://cdn.example/41.png", false)),
            generationCount = 1,
            remainingGenerationCount = 2,
            signupState = SignupState.PROFILE_IMAGE_REQUIRED
        )
    }

    override suspend fun generateProfileImage(accessToken: String): ProfileImageCandidates = throw profileImageError

    override suspend fun selectProfileImage(accessToken: String, profileImageId: Long): ProfileImageSelectionResult =
        throw profileImageError

    override suspend fun refresh(refreshToken: String): ServiceSession {
        refreshRequests += 1
        return ServiceSession("refreshed-access", "refreshed-refresh", SignupState.PROFILE_IMAGE_REQUIRED)
    }
}
