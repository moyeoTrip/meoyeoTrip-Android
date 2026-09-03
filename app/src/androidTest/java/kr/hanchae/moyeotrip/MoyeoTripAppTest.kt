package kr.hanchae.moyeotrip

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import kr.hanchae.moyeotrip.data.auth.AuthAccountService
import kr.hanchae.moyeotrip.domain.auth.AuthGateway
import kr.hanchae.moyeotrip.domain.auth.AuthProvider
import kr.hanchae.moyeotrip.domain.auth.EmailAuthRequest
import kr.hanchae.moyeotrip.domain.auth.IdentityToken
import kr.hanchae.moyeotrip.domain.auth.IdentityTokenProvider
import kr.hanchae.moyeotrip.domain.auth.InMemoryAuthSessionStore
import kr.hanchae.moyeotrip.domain.auth.LoginResult
import kr.hanchae.moyeotrip.domain.auth.NicknameCandidateResponse
import kr.hanchae.moyeotrip.domain.auth.ProfileImageCandidates
import kr.hanchae.moyeotrip.domain.auth.ProfileImageSelectionResult
import kr.hanchae.moyeotrip.domain.auth.ServiceSession
import kr.hanchae.moyeotrip.domain.auth.SignupInput
import kr.hanchae.moyeotrip.domain.auth.SignupState
import kr.hanchae.moyeotrip.ui.navigation.MoyeoTripApp
import kr.hanchae.moyeotrip.ui.screens.AccountDeleteScreen
import kr.hanchae.moyeotrip.ui.screens.SettingsScreen
import kr.hanchae.moyeotrip.ui.theme.MoyeoTripTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * 라우팅·상호작용 시험.
 *
 * 예전에는 화면기획 목데이터(코스 이름·닉네임)를 단언했다. 목데이터를 지운 뒤로는
 * **화면이 열리는지**와 **§2 빈 상태 문구가 나오는지**만 본다 — 목 문자열 단언은
 * [NoMockDataUiTest] 가 반대 방향(보이면 실패)으로 지킨다.
 */
class MoyeoTripAppTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun bottomTabsNavigateToPrimaryScreens() {
        showApp()

        composeRule.onNodeWithTag("bottom-explore").performClick()
        composeRule.onNodeWithTag("explore-search-entry").assertIsDisplayed()

        composeRule.onNodeWithTag("bottom-meetings").performClick()
        composeRule.onNodeWithTag("meetings-tab-active").assertIsDisplayed()

        composeRule.onNodeWithTag("bottom-feed").performClick()
        composeRule.onNodeWithTag("feed-tab-discover").assertIsDisplayed()

        composeRule.onNodeWithTag("bottom-my").performClick()
        composeRule.onNodeWithTag("my-scroll").assertIsDisplayed()
        composeRule.onNodeWithText("내 여행").assertIsDisplayed()
    }

    /** R1 — 로그인 세션이 없으면 목데이터가 아니라 §2 빈 상태가 나와야 한다. */
    @Test
    fun signedOutTabsShowCanonEmptyStates() {
        showApp("explore")
        composeRule.onNodeWithTag("explore-signed-out").assertIsDisplayed()
        composeRule.onNodeWithText("로그인하면 모집 중인 모임을 볼 수 있어요.").assertIsDisplayed()

        showApp("meetings")
        composeRule.onNodeWithTag("meetings-empty").assertIsDisplayed()
        composeRule.onNodeWithText("참여 중인 모임이 없어요.").assertIsDisplayed()

        showApp("feed")
        composeRule.onNodeWithTag("feed-empty").assertIsDisplayed()
        composeRule.onNodeWithText("아직 올라온 피드가 없어요.").assertIsDisplayed()
    }

    @Test
    fun searchDropsThePopularKeywordSectionThatHasNoApi() {
        showApp("search")

        composeRule.onNodeWithTag("search-recent-section").assertIsDisplayed()
        // 인기 검색어 순위는 조회 API 가 없어 섹션째 뺐다 (§4)
        composeRule.onAllNodesWithText("인기 검색어").assertCountEquals(0)
    }

    /** 21 특수 메시지는 모임 탭 상단 진입으로 열린다 (번호별 비교 아트보드). */
    @Test
    fun meetingsSpecialMessagesOpenConcreteScreen() {
        showApp()
        composeRule.onNodeWithTag("bottom-meetings").performClick()
        composeRule.onNodeWithTag("meetings-special-messages-entry").performClick()

        composeRule.onNodeWithTag("special-messages-screen").assertIsDisplayed()
        composeRule.onNodeWithText("채팅방 · 특수 메시지").assertIsDisplayed()
    }

    /** 20-5·27-3 은 번호별 비교 캡처 대상이라 라우트가 살아 있어야 한다. */
    @Test
    fun numberedArtboardRoutesStayReachable() {
        showApp("tripDay")
        composeRule.onNodeWithTag("trip-day-screen").assertIsDisplayed()

        showApp("coursePublish")
        composeRule.onNodeWithTag("course-publish-screen").assertIsDisplayed()
        composeRule.onNodeWithText("한 번 공개한 코스는 다시 내릴 수 없어요.", substring = true).assertIsDisplayed()

        showApp("msgs")
        composeRule.onNodeWithTag("special-messages-screen").assertIsDisplayed()
    }

    @Test
    fun exploreMapToggleKeepsBottomBarVisible() {
        showApp()
        composeRule.onNodeWithTag("bottom-explore").performClick()
        composeRule.onNodeWithContentDescription("메뉴").performClick()

        composeRule.onNodeWithText("지도 탐색").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom-explore").assertIsDisplayed()

        composeRule.onNodeWithTag("explore-map-list").performClick()
        composeRule.onNodeWithText("어디로 떠나고 싶나요?").assertIsDisplayed()
    }

    @Test
    fun myShortcutsOpenDedicatedScreens() {
        showApp()
        composeRule.onNodeWithTag("bottom-my").performClick()
        composeRule
            .onNode(hasScrollAction())
            .performScrollToNode(hasTestTag("my-customer-center-shortcut"))
        composeRule.onNodeWithTag("my-customer-center-shortcut").performClick()
        composeRule.onNodeWithText("고객센터").assertIsDisplayed()
        composeRule.onNodeWithText("문의 접수").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("뒤로").performClick()

        composeRule
            .onNode(hasScrollAction())
            .performScrollToNode(hasTestTag("my-feed-shortcut"))
        composeRule.onNodeWithTag("my-feed-shortcut").performClick()
        composeRule.onNodeWithTag("screen-my-feed").assertIsDisplayed()
    }

    @Test
    fun myProfileSummaryOpensProfileEditor() {
        showApp()
        composeRule.onNodeWithTag("bottom-my").performClick()
        composeRule.onNodeWithTag("my-profile-summary").performClick()

        composeRule.onNodeWithTag("screen-profile-edit").assertIsDisplayed()
    }

    /** §3 — 화면기획 고정값(`1.0.4 (최신)`)이 아니라 실제 빌드 버전을 보여준다. */
    @Test
    fun settingsVersionRowShowsTheRealBuildVersion() {
        showApp("settings")

        composeRule
            .onNode(hasScrollAction())
            .performScrollToNode(hasText("버전"))
        composeRule.onNodeWithText(BuildConfig.VERSION_NAME).assertIsDisplayed()
        composeRule.onAllNodesWithText("(최신)", substring = true).assertCountEquals(0)
    }

    @Test
    fun accountDeleteRequiresExplicitConfirmations() {
        var deleteRequested = false

        composeRule.activity.setContent {
            MoyeoTripTheme {
                AccountDeleteScreen(
                    onBack = {},
                    onDelete = { deleteRequested = true }
                )
            }
        }

        composeRule.onNodeWithText("알림이 너무 많아요").performClick()
        composeRule
            .onNode(hasScrollAction())
            .performScrollToNode(hasText("삭제 범위와 30일 대기 정책을 확인했어요"))
        composeRule.onNodeWithText("삭제 범위와 30일 대기 정책을 확인했어요").performClick()
        composeRule.onNodeWithTag("account-delete-submit").performClick()
        composeRule.onNodeWithText("계속").performClick()
        composeRule.onNode(hasText("계정 탈퇴") and hasClickAction()).performClick()

        assertTrue(deleteRequested)
    }

    @Test
    fun providerManagementUsesStableOrderAndRevealsNewEmailFormOnDemand() {
        // 게이트웨이·토큰 제공자 모두 **시험이 직접 만든 것**이다 — 앱에는 데모 인증 경로가 없다(R2).
        val gateway = FakeAuthGateway(linked = setOf(AuthProvider.KAKAO))
        val sessionStore = InMemoryAuthSessionStore().apply {
            saveSignup(
                AuthProvider.KAKAO,
                ServiceSession("access", "refresh", SignupState.SIGNUP_COMPLETE)
            )
        }
        val accountService = AuthAccountService(gateway, sessionStore, FakeIdentityTokenProvider())

        composeRule.activity.setContent {
            MoyeoTripTheme {
                SettingsScreen(onBack = {}, accountService = accountService, onAuthenticationCleared = {})
            }
        }
        composeRule.onNodeWithText("로그인 방식").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000) {
            composeRule.onAllNodesWithTag("providers-apple-link").fetchSemanticsNodes().isNotEmpty()
        }

        val tags = listOf(
            "providers-kakao-link",
            "providers-google-link",
            "providers-email-link",
            "providers-apple-link"
        )
        val topPositions = tags.map { composeRule.onNodeWithTag(it).getUnclippedBoundsInRoot().top }
        assertTrue(topPositions.zipWithNext().all { (first, second) -> first < second })

        composeRule.onAllNodesWithTag("providers-email-email").assertCountEquals(0)
        composeRule.onNodeWithTag("providers-email-link").performClick()
        composeRule.onNodeWithTag("providers-email-email").assertIsDisplayed()
        composeRule.onNodeWithTag("providers-email-password").assertIsDisplayed()
    }

    @Test
    fun onboardingBackAndSkipControlsMatchVisibleFlow() {
        showApp("auth")

        composeRule.onNodeWithTag("auth-flow-back").assertIsDisplayed()
        composeRule.onNodeWithTag("auth-flow-skip").assertIsDisplayed()
        composeRule.onNodeWithTag("auth-onboarding-dots").assertIsDisplayed()
        composeRule.onNodeWithTag("auth-onboarding-next").performClick()
        composeRule.onNodeWithTag("auth-onboarding-dots").assertIsDisplayed()
        composeRule.onNodeWithTag("auth-flow-back").performClick()
        composeRule.onNodeWithTag("auth-onboarding-next").assertIsDisplayed()
    }

    /**
     * 세션 없이 05 로 들어가면 서버 후보를 받을 수 없다 —
     * 지어낸 후보가 뜨는 대신 §2 실패 문구 + 다시 시도가 나와야 한다.
     */
    @Test
    fun nicknameStepShowsCanonicalEmptyStateWithoutServerCandidates() {
        showApp("nickname")

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("auth-nickname-empty").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("불러오지 못했어요.").assertIsDisplayed()
        composeRule.onNodeWithText("다시 시도").assertIsDisplayed()
    }

    /** 후보를 못 받은 05 에서 **어떤 후보 카드도** 그려지면 안 된다. */
    @Test
    fun nicknameStepNeverShowsFabricatedCandidates() {
        showApp("nickname")

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("auth-nickname-empty").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodesWithTag("auth-nickname-option-0").assertCountEquals(0)
        FABRICATED_NICKNAMES.forEach { nickname ->
            composeRule.onAllNodesWithText(nickname, substring = true).assertCountEquals(0)
        }
        // 후보가 없으면 다음으로 넘어갈 수 없다
        composeRule.onNodeWithTag("auth-nickname-next").assertIsNotEnabled()
    }

    /** 후보를 못 받으면 가입은 05 에서 멈춘다 — 다음 단계로 흘러가지 않는다. */
    @Test
    fun authFlowStopsAtNicknameWithoutServerCandidates() {
        showApp("nickname")

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("auth-nickname-empty").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("auth-nickname-refresh").assertIsDisplayed()
        // 06 기본 정보로 넘어가 있지 않다
        composeRule.onAllNodesWithTag("auth-birth-date").assertCountEquals(0)
        composeRule.onAllNodesWithTag("auth-terms-all").assertCountEquals(0)
    }

    private fun showApp(screen: String = "home") {
        composeRule.activity.setContent {
            MoyeoTripApp(startScreen = screen, skipStartupSplash = true, skipAuthentication = true)
        }
        composeRule.waitForIdle()
    }

    private companion object {
        /** 지웠던 목 후보들. 하나라도 화면에 나오면 목 게이트웨이가 되살아난 것이다. */
        val FABRICATED_NICKNAMES = listOf("따스한 사슴", "잔잔한 거북이", "호기심 많은 너구리", "포근한 수달")
    }
}

/** 로그인 방식 관리 시험용 게이트웨이. 앱에는 이런 구현이 없다 — 시험 안에서만 산다. */
private class FakeAuthGateway(linked: Set<AuthProvider>) : AuthGateway {
    private val linkedProviders = linked.toMutableSet()

    override suspend fun login(identity: IdentityToken): LoginResult = error("시험에서 쓰지 않아요.")

    override suspend fun nicknameCandidates(): NicknameCandidateResponse = error("시험에서 쓰지 않아요.")

    override suspend fun signup(identity: IdentityToken, input: SignupInput): ServiceSession = error("시험에서 쓰지 않아요.")

    override suspend fun profileImages(accessToken: String): ProfileImageCandidates = error("시험에서 쓰지 않아요.")

    override suspend fun generateProfileImage(accessToken: String): ProfileImageCandidates = error("시험에서 쓰지 않아요.")

    override suspend fun selectProfileImage(accessToken: String, profileImageId: Long): ProfileImageSelectionResult =
        error("시험에서 쓰지 않아요.")

    override suspend fun linkedProviders(accessToken: String): Set<AuthProvider> = linkedProviders.toSet()

    override suspend fun linkProvider(accessToken: String, identity: IdentityToken): Set<AuthProvider> {
        linkedProviders += identity.provider
        return linkedProviders.toSet()
    }
}

private class FakeIdentityTokenProvider : IdentityTokenProvider {
    override suspend fun acquire(provider: AuthProvider): IdentityToken =
        IdentityToken(provider = provider, idToken = "test-id-token", fcmToken = null)

    override suspend fun acquireEmail(request: EmailAuthRequest): IdentityToken =
        IdentityToken(provider = AuthProvider.EMAIL, idToken = "test-id-token", fcmToken = null)
}
