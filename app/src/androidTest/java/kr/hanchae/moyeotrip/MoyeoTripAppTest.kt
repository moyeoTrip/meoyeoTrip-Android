package kr.hanchae.moyeotrip

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.getUnclippedBoundsInRoot
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
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import kr.hanchae.moyeotrip.data.MockTripRepository
import kr.hanchae.moyeotrip.data.auth.AuthAccountService
import kr.hanchae.moyeotrip.domain.auth.AuthProvider
import kr.hanchae.moyeotrip.domain.auth.DemoAuthGateway
import kr.hanchae.moyeotrip.domain.auth.InMemoryAuthSessionStore
import kr.hanchae.moyeotrip.domain.auth.MockIdentityTokenProvider
import kr.hanchae.moyeotrip.domain.auth.ServiceSession
import kr.hanchae.moyeotrip.domain.auth.SignupState
import kr.hanchae.moyeotrip.ui.navigation.MoyeoTripApp
import kr.hanchae.moyeotrip.ui.screens.SettingsScreen
import kr.hanchae.moyeotrip.ui.theme.MoyeoTripTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MoyeoTripAppTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun waitForStartupSplash() {
        MockTripRepository.resetSessionFeedPostsForTests()
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(1_500)
        composeRule.waitUntil(timeoutMillis = 8_000) {
            composeRule.onAllNodesWithTag("screen.splash").fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun homeShowsHeroAndOpensFeaturedTripDetail() {
        composeRule.onNodeWithText("모여트립 in 경북").assertIsDisplayed()
        composeRule.onNodeWithText("추천").assertIsDisplayed()
        composeRule.onNodeWithText("햇살 좋은 날, 걷기 좋은 코스를 추천해드려요").assertIsDisplayed()
        composeRule.onNodeWithText("맑음 · 경주 첨성대").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("경주 첨성대 날씨 히어로 이미지").assertIsDisplayed()

        composeRule.onNodeWithTag("home-course-gyeongju-healing").performClick()

        composeRule.onNodeWithText("코스 상세").assertIsDisplayed()
        composeRule.onNodeWithText("경주 감성 힐링 코스").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("찜").performClick()
        composeRule.onNodeWithText("찜한 코스에 담았어요.").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("공유").performClick()
        composeRule.onNodeWithText("코스 공유 링크를 복사했어요.").assertIsDisplayed()
        openRecruitmentListFromCourseDetail()

        composeRule.onNodeWithText("모집 상세").assertIsDisplayed()
        composeRule.onNodeWithText("경주 단풍·야경 1박 2일").assertIsDisplayed()
        composeRule.onNodeWithText("달빛 토끼 6142").assertIsDisplayed()
        composeRule.onNodeWithTag("trip-favorite-button").performClick()
        composeRule.onNodeWithText("찜한 모임에 담았어요.").assertIsDisplayed()
        composeRule.onNodeWithTag("trip-favorite-button").performClick()
        composeRule.onNodeWithText("찜한 모임에서 제외했어요.").assertIsDisplayed()
    }

    @Test
    fun notificationWeatherRecommendationOpensCourseDetail() {
        composeRule.onNodeWithContentDescription("알림").performClick()
        composeRule.onNodeWithTag("notification-course-gyeongju-healing").performClick()

        composeRule.onNodeWithText("코스 상세").assertIsDisplayed()
        composeRule.onNodeWithText("경주 감성 힐링 코스").assertIsDisplayed()
        composeRule.onNodeWithText("첨성대").assertIsDisplayed()
    }

    @Test
    fun homeBottomRankingAndSupportActionsRemainReachable() {
        val initialHostedCount = MockTripRepository.profile.hostedTrips

        composeRule
            .onNodeWithTag("home.scroll")
            .performScrollToIndex(3)
        composeRule.onNodeWithText("울릉도 2박 3일 섬 여행").assertIsDisplayed()
        val thirdRankingBounds = composeRule
            .onNodeWithTag("home-popular-3")
            .getUnclippedBoundsInRoot()
        val thirdRankingTitleBounds = composeRule
            .onNodeWithTag("home-popular-3-title", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        val bottomTabTop = composeRule
            .onNodeWithTag("bottom-bar")
            .getUnclippedBoundsInRoot()
            .top
        assertTrue(
            "The third home ranking row should sit above the bottom navigation.",
            thirdRankingBounds.bottom <= bottomTabTop - 12.dp
        )
        assertTrue(
            "Home ranking rows should keep enough vertical breathing room.",
            thirdRankingBounds.bottom - thirdRankingBounds.top >= 80.dp &&
                thirdRankingTitleBounds.top >= thirdRankingBounds.top + 14.dp
        )

        composeRule.onNodeWithTag("home.scroll").performScrollToIndex(0)
        composeRule.onNodeWithContentDescription("알림").performClick()
        composeRule.onNodeWithText("새 댓글이 달렸어요").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("뒤로").performClick()
        composeRule.onNodeWithContentDescription("모집 만들기").performClick()
        composeRule.onNodeWithText("모집 만들기 (1/5)").assertIsDisplayed()
        completeCreateRecruitment()
        composeRule.onNodeWithText("모집 관리").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("뒤로").performClick()

        composeRule.waitUntil(timeoutMillis = 8_000) {
            composeRule.onAllNodesWithTag("bottom-my").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("bottom-my").performClick()
        composeRule.waitUntil(timeoutMillis = 8_000) {
            composeRule.onAllNodesWithText("내 여행").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodesWithText("주왕산 & 주산지 힐링 트레킹")[0].assertIsDisplayed()
        composeRule.onNodeWithText("방금 생성").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("프로필 메뉴").performClick()
        composeRule
            .onNodeWithTag("profile-stat-hosted-value")
            .assertTextContains((initialHostedCount + 1).toString())
    }

    @Test
    fun hostManageApprovesRejectsCancelsAndOpensChatAfterRecruitmentCreation() {
        composeRule.onNodeWithContentDescription("모집 만들기").performClick()
        completeCreateRecruitment()

        composeRule.onNodeWithText("모집 관리").assertIsDisplayed()
        composeRule.onNodeWithText("승인 대기").assertIsDisplayed()
        composeRule.onNodeWithText("따스한 사슴 3492").assertIsDisplayed()
        composeRule.onNodeWithTag("host-applicant-applicant-deer-approve").performClick()
        composeRule.onNodeWithText("승인된 동행자").assertIsDisplayed()
        composeRule.onAllNodesWithText("따스한 사슴 3492").assertCountEquals(1)

        composeRule.onNodeWithTag("host-applicant-applicant-turtle-reject").performClick()
        composeRule.onNodeWithText("거절 기록").assertIsDisplayed()
        composeRule
            .onNodeWithTag("support-list")
            .performScrollToNode(hasText("거절 사유: 일정과 동선 조건이 맞지 않아요."))
        composeRule.onNodeWithText("거절 사유: 일정과 동선 조건이 맞지 않아요.").assertIsDisplayed()

        composeRule.onNodeWithTag("support-list").performScrollToNode(hasTestTag("host-manage-toggle-close"))
        composeRule.onNodeWithTag("host-manage-toggle-close").performClick()
        composeRule.onNodeWithTag("host-manage-close-state").assertTextContains("모집 취소됨")
        composeRule.onNodeWithTag("host-manage-open-chat").performClick()
        composeRule.onNodeWithText("모집이 만들어졌어요. 함께 갈 사람을 기다려요.").assertIsDisplayed()
    }

    @Test
    fun bottomTabsNavigateToPrimaryScreens() {
        composeRule.onNodeWithTag("bottom-meetings").performClick()
        composeRule.onNodeWithText("4/8명 · 마감 D-3").assertIsDisplayed()
        composeRule.onNodeWithText("경주 단풍·야경").assertIsDisplayed()

        composeRule.onNodeWithTag("bottom-feed").performClick()
        composeRule.onNodeWithText("팔로잉").assertIsDisplayed()
        composeRule.onNodeWithText("발견").assertIsDisplayed()
        composeRule.onNodeWithText("주왕산 & 주산지 힐링 트레킹").assertIsDisplayed()

        composeRule.onNodeWithTag("bottom-my").performClick()
        composeRule.onNodeWithText("내 여행").assertIsDisplayed()
        composeRule.onNodeWithText("진행중").assertIsDisplayed()
        composeRule.onNodeWithText("지난여행").assertIsDisplayed()
        composeRule.onNodeWithText("찜한 코스").assertIsDisplayed()
        composeRule.onAllNodesWithText("지난 여행").assertCountEquals(0)
        composeRule.onNodeWithText("청송 시외버스터미널").assertIsDisplayed()
        composeRule.onNodeWithText("2/5명").assertIsDisplayed()
        composeRule.onNodeWithText("안동 하회마을 하루여행").assertIsDisplayed()
        composeRule.onAllNodesWithText("안동 하회마을 하루 코스").assertCountEquals(0)
        composeRule.onNodeWithText("지난여행").performClick()
        composeRule.onNodeWithText("경주 역사 감성 여행").assertIsDisplayed()
        composeRule.onNodeWithText("월정교 야경과 첨성대 단풍길을 함께 걸었어요.").assertIsDisplayed()
        composeRule.onNodeWithText("찜한 코스").performClick()
        composeRule.onNodeWithText("울릉도 2박 3일 섬 여행").assertIsDisplayed()
        composeRule.onNodeWithText("바다 전망과 짧은 트레킹, 섬마을 산책을 묶은 여유로운 일정이에요.").assertIsDisplayed()
        composeRule.onNodeWithText("2박 3일 · 12.4km").assertIsDisplayed()
        composeRule
            .onNodeWithTag("my-scroll")
            .performScrollToNode(hasText("지금까지 만난 친구"))
        composeRule.onNodeWithText("지금까지 만난 친구").assertIsDisplayed()
        composeRule
            .onNodeWithTag("my-friend-dex-preview-count", useUnmergedTree = true)
            .assertTextContains("12마리 · 최근 동행 순")
    }

    @Test
    fun mockAuthFlowCompletesFromQaRoute() {
        composeRule.activity.setContent {
            MoyeoTripApp(startScreen = "auth", skipStartupSplash = true, skipAuthentication = true)
        }

        composeRule.onNodeWithText("고민 없이 고르는 경북 코스").assertIsDisplayed()
        clickAuthNode("auth-onboarding-next")
        composeRule.onNodeWithText("3명이 모이면 채팅방이 열려요").assertIsDisplayed()
        clickAuthNode("auth-onboarding-next")
        composeRule.onNodeWithText("여행 뒤엔 자연스럽게 친구로").assertIsDisplayed()
        clickAuthNode("auth-onboarding-next")
        composeRule.onNodeWithText("모여트립에 오신 걸 환영해요").assertIsDisplayed()
        composeRule.onNodeWithText("30초 안에 시작할 수 있어요").assertIsDisplayed()
        clickAuthNode("auth-login-kakao")
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("auth-nickname-option-0").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule
            .onNode(hasScrollAction())
            .performScrollToNode(hasTestTag("auth-nickname-option-0"))
        composeRule.onNodeWithTag("auth-nickname-option-0").assertIsDisplayed()
        clickAuthNode("auth-nickname-option-0")
        clickAuthNode("auth-nickname-next")
        composeRule.onNodeWithText("기본 정보").assertIsDisplayed()
        composeRule.onNodeWithTag("auth-birth-date").performClick()
        composeRule.onNodeWithText("1998년 4월 12일").assertIsDisplayed()
        clickAuthNode("birth-date-confirm")
        clickAuthNode("auth-gender-female")
        clickAuthNode("auth-basic-next")
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("auth-profile-generate").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("여행 친구를 만들어볼까요?").assertIsDisplayed()
        composeRule
            .onNodeWithText("선택한 닉네임 ‘따스한 사슴 3492’을 바탕으로 후보를 만들어요.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("새 후보 만들기 · 남은 3회").assertIsDisplayed()
        clickAuthNode("auth-profile-generate")
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("auth-profile-option-0").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("auth-profile-option-0").performClick()
        clickAuthNode("auth-profile-complete")

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("모여트립 in 경북").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("모여트립 in 경북").assertIsDisplayed()
    }

    @Test
    fun settingsWithdrawalDeletesSessionAndReturnsToAuthentication() {
        val gateway = DemoAuthGateway()
        val sessionStore = InMemoryAuthSessionStore().apply {
            saveSignup(
                AuthProvider.KAKAO,
                ServiceSession(
                    accessToken = "withdraw-access",
                    refreshToken = "withdraw-refresh",
                    signupState = SignupState.SIGNUP_COMPLETE
                )
            )
        }
        val accountService = AuthAccountService(gateway, sessionStore)
        var authenticationCleared = false

        composeRule.activity.setContent {
            MoyeoTripTheme {
                SettingsScreen(
                    onBack = {},
                    accountService = accountService,
                    onAuthenticationCleared = { authenticationCleared = true }
                )
            }
        }

        composeRule
            .onNode(hasScrollAction())
            .performScrollToNode(hasText("계정 탈퇴"))
        composeRule.onNodeWithText("계정 탈퇴").performClick()
        composeRule.onNodeWithText("즉시 영구 삭제").assertIsDisplayed()
        composeRule.onNodeWithText("영구 탈퇴").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) { authenticationCleared }
        assertTrue(authenticationCleared)
        assertEquals(null, sessionStore.current.accessToken)
        assertEquals(null, sessionStore.current.refreshToken)
    }

    @Test
    fun googleLoginJoinsTheServerDrivenSignupFlow() {
        openAuthLogin()

        composeRule.onNodeWithTag("auth-login-welcome-image").assertIsDisplayed()
        val kakaoTop = composeRule.onNodeWithTag("auth-login-kakao").getUnclippedBoundsInRoot().top
        val googleTop = composeRule.onNodeWithTag("auth-login-google").getUnclippedBoundsInRoot().top
        val emailTop = composeRule.onNodeWithTag("auth-login-email").getUnclippedBoundsInRoot().top
        val appleTop = composeRule.onNodeWithTag("auth-login-apple").getUnclippedBoundsInRoot().top
        assertTrue(kakaoTop < googleTop && googleTop < emailTop && emailTop < appleTop)
        listOf("auth-login-kakao", "auth-login-google", "auth-login-email", "auth-login-apple").forEach { tag ->
            val bounds = composeRule.onNodeWithTag(tag).getUnclippedBoundsInRoot()
            assertEquals(54.dp, bounds.bottom - bounds.top)
        }
        val iconSlotLefts = listOf("kakao", "google", "email", "apple").map { provider ->
            composeRule
                .onNodeWithTag("auth-login-$provider-icon-slot", useUnmergedTree = true)
                .getUnclippedBoundsInRoot()
                .left
        }
        iconSlotLefts.drop(1).forEach { left -> assertEquals(iconSlotLefts.first(), left) }
        composeRule.onNodeWithContentDescription("Google G", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("Google로 계속하기").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Apple로 계속하기").assertIsDisplayed()
        composeRule.onNodeWithTag("auth-login-google").assertIsDisplayed().performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("auth-nickname-option-0").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("어떤 친구로 시작할까요?").assertIsDisplayed()
    }

    @Test
    fun emailAccountCreationAndPasswordResetAreAvailable() {
        openAuthLogin()
        composeRule.onNodeWithTag("auth-login-email").performClick()

        composeRule.onNodeWithTag("auth-step-email").assertIsDisplayed()
        composeRule.onNodeWithTag("auth-email-address").performTextInput("trip@example.com")
        composeRule.onNodeWithTag("auth-email-reset").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("비밀번호 재설정 메일을 보냈어요.").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("auth-email-password").performTextInput("password123")
        composeRule.onNodeWithTag("auth-email-mode-create").performClick()
        composeRule.onNodeWithTag("auth-email-submit").assertIsNotEnabled()
        composeRule.onNodeWithTag("auth-email-password-confirmation").performTextInput("different")
        composeRule.onNodeWithText("비밀번호가 일치하지 않아요.").assertIsDisplayed()
        composeRule.onNodeWithTag("auth-email-submit").assertIsNotEnabled()
        composeRule.onNodeWithTag("auth-email-password-confirmation").performTextClearance()
        composeRule.onNodeWithTag("auth-email-password-confirmation").performTextInput("password123")
        composeRule.onNodeWithTag("auth-email-submit").assertIsEnabled().performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("auth-nickname-option-0").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("auth-step-nickname").assertIsDisplayed()
    }

    @Test
    fun nicknameRefreshReplacesCandidatesAndClearsSelection() {
        openNicknameSelection()

        composeRule.onNodeWithTag("auth-nickname-next").assertIsNotEnabled()
        clickAuthNode("auth-nickname-option-0")
        composeRule.onNodeWithTag("auth-nickname-next").assertIsEnabled()

        composeRule.mainClock.autoAdvance = false
        clickAuthNode("auth-nickname-refresh")
        composeRule.mainClock.advanceTimeByFrame()
        assertEquals(
            1,
            composeRule.onAllNodesWithTag("auth-nickname-skeleton-0").fetchSemanticsNodes().size
        )
        assertEquals(
            1,
            composeRule.onAllNodesWithTag("auth-nickname-skeleton-2").fetchSemanticsNodes().size
        )
        composeRule.onNodeWithText("새 이름을 받고 있어요...").assertIsDisplayed()
        composeRule.onNodeWithTag("auth-nickname-refresh").assertIsNotEnabled()
        composeRule.onNodeWithTag("auth-nickname-next").assertIsNotEnabled()

        composeRule.mainClock.advanceTimeBy(600)
        composeRule.mainClock.autoAdvance = true
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("포근한 수달", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule
            .onNode(hasScrollAction())
            .performScrollToNode(hasText("포근한 수달", substring = true))
        composeRule.onNodeWithText("포근한 수달", substring = true).assertIsDisplayed()
        composeRule.onAllNodesWithText("따스한 사슴", substring = true).assertCountEquals(0)
        composeRule
            .onNode(hasScrollAction())
            .performScrollToNode(hasTestTag("auth-nickname-next"))
        composeRule.onNodeWithTag("auth-nickname-next").assertIsNotEnabled()
        composeRule.onNodeWithText("마음에 들 때까지 새 후보를 받아보세요").assertIsDisplayed()
    }

    @Test
    fun mockAuthFlowBackAndCloseControlsMatchVisibleFlow() {
        composeRule.activity.setContent {
            MoyeoTripApp(startScreen = "auth", skipStartupSplash = true, skipAuthentication = true)
        }
        composeRule.onNodeWithTag("auth-flow-back").assertIsDisplayed()
        composeRule.onNodeWithTag("auth-flow-close").assertIsDisplayed()
        clickAuthNode("auth-onboarding-next")
        composeRule.onNodeWithText("3명이 모이면 채팅방이 열려요").assertIsDisplayed()
        composeRule.onNodeWithTag("auth-flow-back").performClick()
        composeRule.onNodeWithText("고민 없이 고르는 경북 코스").assertIsDisplayed()
        composeRule.onNodeWithTag("auth-flow-close").performClick()

        composeRule.onNodeWithText("모여트립 in 경북").assertIsDisplayed()
    }

    @Test
    fun meetingsTabOpensChatRoom() {
        composeRule.onNodeWithTag("bottom-meetings").performClick()
        composeRule.onNodeWithText("경주 단풍·야경").performClick()

        composeRule.onNodeWithText("모임 신청 후 이어지는 여행 채팅방이에요").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("전화").performClick()
        composeRule.onNodeWithText("채팅방 도구").assertIsDisplayed()
        composeRule.onNodeWithText("호스트 연락 방식과 통화 가능 시간을 확인할 수 있어요.").assertIsDisplayed()
        composeRule.onNodeWithText("확인").performClick()
        composeRule.onNodeWithContentDescription("더보기").performClick()
        composeRule.onNodeWithText("신고, 알림 끄기, 멤버 보기 메뉴를 확인할 수 있어요.").assertIsDisplayed()
    }

    @Test
    fun meetingsSpecialMessagesOpenConcreteScreen() {
        composeRule.onNodeWithTag("bottom-meetings").performClick()
        composeRule.onNodeWithText("친구 도감 메시지").assertIsDisplayed()
        composeRule.onNodeWithText("친구 도감 메시지").performClick()

        composeRule.onNodeWithText("채팅방 · 특수 메시지 6종").assertIsDisplayed()
        composeRule.onNodeWithText("동궁과 월지").assertIsDisplayed()
        composeRule.onNodeWithText("여행이 확정됐어요!").assertIsDisplayed()
    }

    @Test
    fun exploreCourseListOpensTripDetail() {
        composeRule.onNodeWithTag("bottom-explore").performClick()
        composeRule.onNodeWithText("어디로 떠나고 싶나요?").assertIsDisplayed()

        composeRule.onNodeWithTag("explore-course-gyeongju-healing").performClick()

        composeRule.onNodeWithText("코스 상세").assertIsDisplayed()
        openRecruitmentListFromCourseDetail()

        composeRule.onNodeWithText("모집 상세").assertIsDisplayed()
        composeRule.onNodeWithText("경주 단풍·야경 1박 2일").assertIsDisplayed()
    }

    @Test
    fun searchShowsHelpfulEmptyStateAndRecoveryKeywords() {
        composeRule.onNodeWithTag("bottom-explore").performClick()
        composeRule.onNodeWithTag("explore-search-entry").performClick()

        composeRule.onNodeWithText("검색").assertIsDisplayed()
        composeRule.onNodeWithTag("search-query-field").performTextInput("없는장소")
        composeRule.onNodeWithTag("search-empty-state").assertIsDisplayed()
        composeRule.onNodeWithText("검색 결과가 없어요").assertIsDisplayed()
        composeRule.onNodeWithTag("search-recovery-경주").performClick()

        composeRule.onNodeWithText("경주 감성 힐링 코스").assertIsDisplayed()
    }

    @Test
    fun exploreFavoriteButtonsToggleWithoutOpeningDetail() {
        composeRule.onNodeWithTag("bottom-explore").performClick()
        composeRule.onNodeWithText("어디로 떠나고 싶나요?").assertIsDisplayed()

        composeRule
            .onNodeWithTag("explore-course-favorite-andong-hahoe")
            .assertContentDescriptionEquals("찜")
        composeRule.onNodeWithTag("explore-course-favorite-andong-hahoe").performClick()
        composeRule
            .onNodeWithTag("explore-course-favorite-andong-hahoe")
            .assertContentDescriptionEquals("찜 해제")
        composeRule.onNodeWithText("어디로 떠나고 싶나요?").assertIsDisplayed()
        composeRule.onAllNodesWithText("코스 상세").assertCountEquals(0)
        composeRule.onNodeWithTag("explore-course-favorite-andong-hahoe").performClick()
        composeRule
            .onNodeWithTag("explore-course-favorite-andong-hahoe")
            .assertContentDescriptionEquals("찜")

        composeRule.onNodeWithContentDescription("메뉴").performClick()
        composeRule.onNodeWithText("지도 탐색").assertIsDisplayed()
        composeRule
            .onNodeWithTag("explore-map-favorite-cheongsong-juwangsan")
            .assertContentDescriptionEquals("찜 해제")
        composeRule.onNodeWithTag("explore-map-favorite-cheongsong-juwangsan").performClick()
        composeRule
            .onNodeWithTag("explore-map-favorite-cheongsong-juwangsan")
            .assertContentDescriptionEquals("찜")
        composeRule.onNodeWithText("지도 탐색").assertIsDisplayed()
        composeRule.onAllNodesWithText("코스 상세").assertCountEquals(0)

        composeRule.onNodeWithTag("explore-map-selected-course").performClick()
        composeRule.onNodeWithText("코스 상세").assertIsDisplayed()
    }

    @Test
    fun exploreMapToggleKeepsBottomBarVisible() {
        composeRule.onNodeWithTag("bottom-explore").performClick()
        composeRule.onNodeWithContentDescription("메뉴").performClick()

        composeRule.onNodeWithText("지도 탐색").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom-explore").assertIsDisplayed()
        composeRule.onNodeWithTag("explore-map-selected-course").performClick()

        composeRule.onNodeWithText("코스 상세").assertIsDisplayed()
        composeRule.onAllNodesWithTag("bottom-explore").assertCountEquals(0)
        composeRule.onNodeWithContentDescription("뒤로").performClick()
        composeRule.onNodeWithText("지도 탐색").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom-explore").assertIsDisplayed()
        composeRule.onNodeWithTag("explore-map-list").performClick()
        composeRule.onNodeWithText("어디로 떠나고 싶나요?").assertIsDisplayed()
    }

    @Test
    fun exploreInitialCourseOrderMatchesWebPlan() {
        composeRule.onNodeWithTag("bottom-explore").performClick()
        composeRule.onNodeWithText("어디로 떠나고 싶나요?").assertIsDisplayed()

        val juwangsanTop = composeRule
            .onNodeWithTag("explore-course-cheongsong-juwangsan")
            .getUnclippedBoundsInRoot()
            .top
        val hahoeTop = composeRule
            .onNodeWithTag("explore-course-andong-hahoe")
            .getUnclippedBoundsInRoot()
            .top
        val ulleungTop = composeRule
            .onNodeWithTag("explore-course-ulleung-island")
            .getUnclippedBoundsInRoot()
            .top
        val gyeongjuTop = composeRule
            .onNodeWithTag("explore-course-gyeongju-healing")
            .getUnclippedBoundsInRoot()
            .top

        assertTrue("주왕산 should appear before 안동 하회마을.", juwangsanTop < hahoeTop)
        assertTrue("안동 하회마을 should appear before 울릉도.", hahoeTop < ulleungTop)
        assertTrue("울릉도 should appear before 경주.", ulleungTop < gyeongjuTop)
    }

    @Test
    fun tripApplicationShowsPendingApprovalInAppliedTab() {
        val initialJoinedCount = MockTripRepository.profile.joinedTrips

        composeRule.onNodeWithTag("home-course-gyeongju-healing").performClick()
        openRecruitmentListFromCourseDetail()

        composeRule.onNodeWithText("모집 상세").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("채팅").performClick()
        composeRule.onNodeWithText("모임 신청 후 이어지는 여행 채팅방이에요").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("뒤로").performClick()
        composeRule.onNodeWithTag("trip-apply-button").performClick()
        composeRule.onNodeWithText("한마디를 남겨주세요!").assertIsDisplayed()
        composeRule.onNodeWithText("신청하기").performClick()
        composeRule.onNodeWithText("신청을 보냈어요").assertIsDisplayed()
        composeRule.onNodeWithTag("application-done").performClick()
        composeRule.onNodeWithText("신청을 보냈어요. 호스트 승인 후 채팅방이 열려요.").assertIsDisplayed()
        repeat(2) {
            composeRule.onNodeWithContentDescription("뒤로").performClick()
        }
        composeRule.waitUntil(timeoutMillis = 8_000) {
            composeRule.onAllNodesWithTag("bottom-my").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("bottom-meetings").performClick()
        composeRule.onNodeWithTag("meetings-tab-applied").performClick()
        composeRule.onNodeWithText("경주 단풍·야경 1박 2일").assertIsDisplayed()
        composeRule.onNodeWithText("승인 대기").assertIsDisplayed()
        composeRule.onAllNodesWithText("모임 채팅으로 이동").assertCountEquals(0)
        composeRule.onNodeWithTag("bottom-my").performClick()
        composeRule.onNodeWithContentDescription("프로필 메뉴").performClick()
        composeRule
            .onNodeWithTag("profile-stat-trips-value")
            .assertTextContains(initialJoinedCount.toString())
    }

    @Test
    fun feedCardOpensFeedDetail() {
        composeRule.onNodeWithTag("bottom-feed").performClick()
        composeRule.onNodeWithText("주왕산 & 주산지 힐링 트레킹").performClick()

        composeRule.onNodeWithText("이동 거리").assertIsDisplayed()
        composeRule.onNodeWithText("1/10").assertIsDisplayed()
        composeRule.onNodeWithText("경로지도").assertIsDisplayed()
        val commentInputBounds = composeRule
            .onNodeWithTag("feed-comment-input")
            .getUnclippedBoundsInRoot()
        val commentSendBounds = composeRule
            .onNodeWithTag("feed-comment-send")
            .getUnclippedBoundsInRoot()
        assertTrue(
            "Feed comment input should meet the 48dp touch target.",
            commentInputBounds.bottom - commentInputBounds.top >= 48.dp
        )
        assertTrue(
            "Feed comment send button should meet the 48dp touch target.",
            commentSendBounds.bottom - commentSendBounds.top >= 48.dp
        )
        composeRule.onNodeWithContentDescription("더보기").performClick()
        composeRule.onNodeWithText("피드 저장, 공유, 신고 옵션을 확인할 수 있어요.").assertIsDisplayed()
        composeRule.onNodeWithText("댓글을 입력하세요...").assertIsDisplayed()
        composeRule.onNodeWithTag("feed-comment-input").performTextInput("다음에 저도 가보고 싶어요")
        composeRule.onNodeWithTag("feed-comment-send").performClick()
        composeRule
            .onNode(hasScrollAction())
            .performScrollToNode(hasText("나: 다음에 저도 가보고 싶어요"))
        composeRule.onNodeWithText("나: 다음에 저도 가보고 싶어요").assertIsDisplayed()
        composeRule.onNodeWithText("댓글 19").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("뒤로").performClick()
        composeRule
            .onNodeWithTag("feed-post-feed-1-comments", useUnmergedTree = true)
            .assertTextContains("19")
    }

    @Test
    fun chatMessageUpdatesMeetingPreview() {
        composeRule.onNodeWithTag("bottom-meetings").performClick()
        composeRule
            .onNode(hasScrollAction())
            .performScrollToNode(hasText("주왕산 & 주산지 힐링 트레킹"))
        composeRule.onNodeWithText("주왕산 & 주산지 힐링 트레킹").performClick()

        composeRule.onNodeWithTag("chat-message-input").performTextInput("날씨 확인하고 갈게요")
        composeRule.onNodeWithTag("chat-message-send").performClick()
        composeRule.onNodeWithText("날씨 확인하고 갈게요").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("뒤로").performClick()
        composeRule.onNodeWithText("나: 날씨 확인하고 갈게요").assertIsDisplayed()
    }

    @Test
    fun endedMeetingChatShowsArchivePolicyAndBlocksInput() {
        composeRule.onNodeWithTag("bottom-meetings").performClick()
        composeRule.onNodeWithText("종료").performClick()
        composeRule.onNodeWithText("안동 봄날 고택 산책").performClick()

        composeRule.onNodeWithText("여행이 종료됐어요").assertIsDisplayed()
        composeRule.onAllNodesWithText("보관 D-14")[0].assertIsDisplayed()
        composeRule.onNodeWithText("채팅은 14일 동안 읽기 전용으로 보관되고 이후 친구 도감 기록만 남아요.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("종료된 모임이라 새 메시지를 보낼 수 없어요.").assertIsDisplayed()
        composeRule.onAllNodesWithTag("chat-message-input").assertCountEquals(0)
    }

    @Test
    fun feedWriteCreatesPostThenListAndDetailUseTheSameData() {
        val initialFeedCount = MockTripRepository.profile.feedCount

        composeRule.onNodeWithTag("bottom-feed").performClick()
        composeRule.onNodeWithTag("feed-write-action-feed-1").performClick()

        composeRule.onNodeWithTag("feed-write-screen").assertIsDisplayed()
        composeRule.onNodeWithTag("feed-write-course-andong-hahoe").performClick()
        composeRule.onAllNodesWithText("안동 하회마을 하루 코스").assertCountEquals(2)
        composeRule.onNodeWithText("하회마을 입구").assertIsDisplayed()
        repeat(3) {
            composeRule.onNodeWithTag("feed-write-next").performClick()
        }
        composeRule.onNodeWithTag("feed-write-visibility-public").performClick()
        composeRule.onNodeWithText("발견 탭에서도 보이고, 경북 여행자 누구나 볼 수 있어요.").assertIsDisplayed()
        composeRule.onNodeWithTag("feed-write-next").performClick()
        composeRule.onNodeWithText("전체공개").assertIsDisplayed()
        composeRule.onNodeWithTag("feed-write-next").performClick()

        composeRule.onNodeWithText("첫 반패키지 단풍 여행").assertIsDisplayed()
        composeRule.onNodeWithText("공개 범위 · 전체공개").assertIsDisplayed()
        composeRule.onNodeWithText("하회마을 입구").assertIsDisplayed()
        composeRule.onNodeWithText("부용대 전망").assertIsDisplayed()
        composeRule.onNodeWithText("방문지").assertIsDisplayed()
        composeRule.onNodeWithText("3곳").assertIsDisplayed()
        composeRule.onNodeWithText("1/3").assertIsDisplayed()
        composeRule.onNode(hasText("처음 반패키지 여행이었는데", substring = true)).assertIsDisplayed()
        composeRule.onNodeWithText("좋아요 0개").assertIsDisplayed()
        composeRule.onNodeWithText("댓글 0").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("뒤로").performClick()
        composeRule.onNodeWithTag("bottom-my").performClick()
        composeRule.onNodeWithContentDescription("프로필 메뉴").performClick()
        composeRule
            .onNodeWithTag("profile-stat-feed-value")
            .assertTextContains((initialFeedCount + 1).toString())
    }

    @Test
    fun feedTabsSwitchBetweenDiscoverAndFollowingTimelines() {
        composeRule.onNodeWithTag("bottom-feed").performClick()

        composeRule.onNodeWithTag("feed-tab-discover").performClick()
        composeRule.onNodeWithTag("feed-post-feed-1").assertIsDisplayed()
        composeRule.onNodeWithText("주왕산 & 주산지 힐링 트레킹").assertIsDisplayed()

        composeRule.onNodeWithTag("feed-tab-following").performClick()

        composeRule.onNodeWithTag("feed-post-feed-1").assertIsDisplayed()
        composeRule.onNodeWithText("주왕산 & 주산지 힐링 트레킹").assertIsDisplayed()
        composeRule.onAllNodesWithTag("feed-post-feed-3").assertCountEquals(0)
        composeRule.onAllNodesWithText("경주 역사 감성 여행은 월정교에서 동궁과 월지로 이어지는 밤 동선이 제일 좋았어요.").assertCountEquals(0)
    }

    @Test
    fun meetingsAndFeedKeepComfortableTouchAndReadingSpace() {
        composeRule.onNodeWithTag("bottom-meetings").performClick()

        val activeTabBounds = composeRule
            .onNodeWithTag("meetings-tab-active")
            .getUnclippedBoundsInRoot()
        assertTrue(
            "Meeting segments should keep a comfortable tap target.",
            activeTabBounds.bottom - activeTabBounds.top in 36.dp..44.dp
        )

        val firstThreadBounds = composeRule
            .onNodeWithTag("meeting-thread-chat-gyeongju-fall")
            .getUnclippedBoundsInRoot()
        assertTrue(
            "Meeting rows should stay compact while keeping three readable text lines.",
            firstThreadBounds.bottom - firstThreadBounds.top in 74.dp..86.dp
        )

        composeRule.onNodeWithTag("bottom-feed").performClick()
        val firstPostBounds = composeRule
            .onNodeWithTag("feed-post-feed-1")
            .getUnclippedBoundsInRoot()
        assertTrue(
            "Feed cards should stay compact while preserving reading space.",
            firstPostBounds.bottom - firstPostBounds.top in 300.dp..380.dp
        )
        val firstPostAuthorBounds = composeRule
            .onNodeWithTag("feed-post-feed-1-author", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        val firstPostTitleBounds = composeRule
            .onNodeWithTag("feed-post-feed-1-title", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        val firstPostSubtitleBounds = composeRule
            .onNodeWithTag("feed-post-feed-1-subtitle", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        val firstPostTagsBounds = composeRule
            .onNodeWithTag("feed-post-feed-1-tags", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        assertTrue(
            "Feed title should not crowd the author row.",
            firstPostTitleBounds.top >= firstPostAuthorBounds.bottom + 8.dp
        )
        assertTrue(
            "Feed subtitle should keep a readable gap below the title.",
            firstPostSubtitleBounds.top >= firstPostTitleBounds.bottom + 4.dp
        )
        assertTrue(
            "Feed tags should sit clearly below the subtitle.",
            firstPostTagsBounds.top >= firstPostSubtitleBounds.bottom + 8.dp
        )
        assertTrue(
            "Feed tags should stay inside the post body.",
            firstPostTagsBounds.left >= firstPostBounds.left &&
                firstPostTagsBounds.right <= firstPostBounds.right
        )

        val writeActionBounds = composeRule
            .onNodeWithTag("feed-write-action-feed-1")
            .assertIsDisplayed()
            .getUnclippedBoundsInRoot()
        assertTrue(
            "Feed write action should remain a 44dp touch target.",
            writeActionBounds.bottom - writeActionBounds.top >= 44.dp &&
                writeActionBounds.right - writeActionBounds.left >= 44.dp
        )
    }

    @Test
    fun myTripTabsKeepOngoingPastAndSavedContentSeparate() {
        composeRule.onNodeWithTag("bottom-my").performClick()

        composeRule.onNodeWithTag("my-profile-summary").assertIsDisplayed()
        composeRule.onNodeWithText("혼자 떠나도 같이 웃을 수 있는 작은 여행을 좋아해요.").assertIsDisplayed()
        composeRule.onNodeWithText("매너").assertIsDisplayed()
        composeRule.onNodeWithText("내 여행").assertIsDisplayed()
        composeRule.onNodeWithTag("my-active-trip-trip-cheongsong-juwangsan").assertIsDisplayed()
        val ongoingCardBounds = composeRule
            .onNodeWithTag("my-active-trip-trip-cheongsong-juwangsan")
            .getUnclippedBoundsInRoot()
        val ongoingTitleBounds = composeRule
            .onNodeWithTag("my-active-trip-trip-cheongsong-juwangsan-title", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        val ongoingDateBounds = composeRule
            .onNodeWithTag("my-active-trip-trip-cheongsong-juwangsan-date", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        val ongoingPlaceBounds = composeRule
            .onNodeWithTag("my-active-trip-trip-cheongsong-juwangsan-place", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        val ongoingPeopleBounds = composeRule
            .onNodeWithTag("my-active-trip-trip-cheongsong-juwangsan-people", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        assertTrue(
            "Ongoing trip rows may be taller for progress, but should keep a consistent card size.",
            ongoingCardBounds.bottom - ongoingCardBounds.top in 116.dp..128.dp
        )
        assertTrue(
            "Ongoing trip text should keep title, date, place, and people in readable order.",
            ongoingDateBounds.top >= ongoingTitleBounds.bottom + 4.dp &&
                ongoingPlaceBounds.top >= ongoingDateBounds.bottom + 4.dp &&
                ongoingPeopleBounds.top >= ongoingPlaceBounds.bottom + 4.dp &&
                ongoingPeopleBounds.bottom <= ongoingCardBounds.bottom - 8.dp
        )
        composeRule.onAllNodesWithTag("my-past-trip-gyeongju-healing").assertCountEquals(0)
        composeRule.onAllNodesWithTag("my-saved-course-ulleung-island").assertCountEquals(0)

        composeRule.onNodeWithTag("my-tab-past").performClick()
        composeRule.onNodeWithTag("my-past-trip-gyeongju-healing").assertIsDisplayed()
        val pastCardBounds = composeRule
            .onNodeWithTag("my-past-trip-gyeongju-healing")
            .getUnclippedBoundsInRoot()
        val pastTitleBounds = composeRule
            .onNodeWithTag("my-past-trip-gyeongju-healing-title", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        val pastSummaryBounds = composeRule
            .onNodeWithTag("my-past-trip-gyeongju-healing-summary", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        val pastMetaBounds = composeRule
            .onNodeWithTag("my-past-trip-gyeongju-healing-meta", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        assertTrue(
            "Past trip rows should align with saved course rows instead of collapsing.",
            pastCardBounds.bottom - pastCardBounds.top in 108.dp..120.dp
        )
        assertTrue(
            "Past trip card copy should keep a readable vertical rhythm.",
            pastSummaryBounds.top >= pastTitleBounds.bottom + 4.dp &&
                pastMetaBounds.top >= pastSummaryBounds.bottom + 3.dp &&
                pastMetaBounds.bottom <= pastCardBounds.bottom - 8.dp
        )
        composeRule.onAllNodesWithTag("my-active-trip-trip-cheongsong-juwangsan").assertCountEquals(0)
        composeRule.onAllNodesWithTag("my-saved-course-ulleung-island").assertCountEquals(0)

        composeRule.onNodeWithTag("my-tab-saved").performClick()
        composeRule.onNodeWithTag("my-saved-course-ulleung-island").assertIsDisplayed()
        val savedCardBounds = composeRule
            .onNodeWithTag("my-saved-course-ulleung-island")
            .getUnclippedBoundsInRoot()
        val savedTitleBounds = composeRule
            .onNodeWithTag("my-saved-course-ulleung-island-title", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        val savedSummaryBounds = composeRule
            .onNodeWithTag("my-saved-course-ulleung-island-summary", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        val savedMetaBounds = composeRule
            .onNodeWithTag("my-saved-course-ulleung-island-meta", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        assertTrue(
            "Saved course rows should share the past trip row height.",
            savedCardBounds.bottom - savedCardBounds.top in 108.dp..120.dp
        )
        assertTrue(
            "Saved course card copy should keep a readable vertical rhythm.",
            savedSummaryBounds.top >= savedTitleBounds.bottom + 4.dp &&
                savedMetaBounds.top >= savedSummaryBounds.bottom + 3.dp &&
                savedMetaBounds.bottom <= savedCardBounds.bottom - 8.dp
        )
        composeRule.onAllNodesWithTag("my-active-trip-trip-cheongsong-juwangsan").assertCountEquals(0)
        composeRule.onAllNodesWithTag("my-past-trip-gyeongju-healing").assertCountEquals(0)
    }

    @Test
    fun myCustomerCenterShortcutOpensDedicatedSupportScreen() {
        composeRule.onNodeWithTag("bottom-my").performClick()
        composeRule
            .onNode(hasScrollAction())
            .performScrollToNode(hasTestTag("my-customer-center-shortcut"))
        composeRule.onAllNodesWithText("알림과 화면 옵션").assertCountEquals(0)
        composeRule.onNodeWithTag("my-customer-center-shortcut").performClick()

        composeRule.onNodeWithText("고객센터").assertIsDisplayed()
        composeRule.onNodeWithText("문의 접수").assertIsDisplayed()
        composeRule.onNodeWithText("자주 묻는 질문").assertIsDisplayed()
    }

    @Test
    fun myFeedShortcutOpensOwnFeedAndPostDetail() {
        composeRule.onNodeWithTag("bottom-my").performClick()
        composeRule
            .onNode(hasScrollAction())
            .performScrollToNode(hasTestTag("my-feed-shortcut"))
        composeRule.onNodeWithTag("my-feed-shortcut").performClick()

        composeRule.onNodeWithTag("screen-my-feed").assertIsDisplayed()
        composeRule.onNodeWithText("내가 남긴 경북 여행 기록").assertIsDisplayed()
        composeRule.onNodeWithTag("my-feed-post-feed-1").performClick()

        composeRule.onNodeWithTag("feed-comment-input").assertIsDisplayed()
        composeRule.onNodeWithText("좋아요 128개").assertIsDisplayed()
    }

    @Test
    fun profileEditShortcutOpensDedicatedEditor() {
        composeRule.onNodeWithTag("bottom-my").performClick()
        composeRule.onNodeWithTag("my-profile-summary").performClick()
        composeRule.onAllNodesWithTag("profile-menu-settings").assertCountEquals(0)

        composeRule.onNodeWithTag("profile-menu-edit").performClick()
        composeRule.onNodeWithTag("screen-profile-edit").assertIsDisplayed()
        composeRule.onNodeWithTag("profile-edit-name").assertIsDisplayed()
        composeRule.onNodeWithText("선호 지역").assertIsDisplayed()
        composeRule.onNodeWithTag("profile-edit-save").performClick()
        composeRule.onNodeWithText("저장 완료").assertIsDisplayed()
    }

    @Test
    fun profileDogamPreviewOpensFriendDex() {
        composeRule.onNodeWithTag("bottom-my").performClick()
        composeRule.onNodeWithTag("my-profile-summary").performClick()
        composeRule
            .onNode(hasScrollAction())
            .performScrollToNode(hasTestTag("profile-dogam-preview-dogam-01"))
        composeRule.onNodeWithTag("profile-dogam-preview-dogam-01").performClick()

        composeRule.onNodeWithText("지금까지 만난 친구").assertIsDisplayed()
        composeRule.onNodeWithText("12").assertIsDisplayed()
    }

    @Test
    fun friendDexSearchAndFiltersUpdateResults() {
        composeRule.onNodeWithTag("bottom-my").performClick()
        composeRule
            .onNode(hasScrollAction())
            .performScrollToNode(hasTestTag("my-friend-dex-shortcut"))
        composeRule.onNodeWithTag("my-friend-dex-shortcut").performClick()

        composeRule.onNodeWithText("지금까지 만난 친구").assertIsDisplayed()
        composeRule.onNodeWithText("12").assertIsDisplayed()

        composeRule.onNodeWithTag("friend-dex-filter-Repeated").performClick()
        composeRule.onNodeWithText("4").assertIsDisplayed()

        composeRule.onNodeWithTag("friend-dex-filter-All").performClick()
        composeRule.onNodeWithContentDescription("검색").performClick()
        composeRule.onNodeWithTag("friend-dex-search-field").performTextInput("1130")

        composeRule.onNodeWithText("1").assertIsDisplayed()
        composeRule.onNodeWithText("고요한 두루미 1130").assertIsDisplayed()
        composeRule.onAllNodesWithText("따스한 사슴 3492").assertCountEquals(0)
    }

    @Test
    fun settingsRowsOpenMockDetailDialogs() {
        composeRule.onNodeWithTag("bottom-my").performClick()
        composeRule.onNodeWithContentDescription("프로필 메뉴").performClick()
        composeRule.onNodeWithContentDescription("설정").performClick()

        composeRule.onNodeWithText("테마").performClick()
        composeRule.onNodeWithText("테마 설정").assertIsDisplayed()
        composeRule.onNodeWithText("닫기").performClick()

        composeRule
            .onNode(hasScrollAction())
            .performScrollToNode(hasText("로그아웃"))
        composeRule.onNodeWithText("로그아웃").performClick()
        composeRule.onNodeWithText("로그아웃 안내").assertIsDisplayed()
        composeRule.onNodeWithText("확인").performClick()
    }

    @Test
    fun providerManagementUsesStableOrderAndRevealsNewEmailFormOnDemand() {
        val gateway = DemoAuthGateway()
        val sessionStore = InMemoryAuthSessionStore().apply {
            saveSignup(
                AuthProvider.KAKAO,
                ServiceSession("access", "refresh", SignupState.SIGNUP_COMPLETE)
            )
        }
        val accountService = AuthAccountService(gateway, sessionStore, MockIdentityTokenProvider())

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
        composeRule.onAllNodesWithTag("providers-email-password").assertCountEquals(0)
        composeRule.onNodeWithTag("providers-email-link").performClick()
        composeRule.onNodeWithTag("providers-email-email").assertIsDisplayed()
        composeRule.onNodeWithTag("providers-email-password").assertIsDisplayed()
        composeRule.onAllNodesWithText("기존 이메일").assertCountEquals(0)
    }

    @Test
    fun courseChatMappingsStayCourseSpecific() {
        val ulleungThreadId = MockTripRepository.chatThreadIdForCourse("ulleung-island")
        val dosanThreadId = MockTripRepository.chatThreadIdForCourse("andong-dosan")

        assertEquals("chat-ulleung-island", ulleungThreadId)
        assertEquals("울릉도 2박 3일 섬 여행", MockTripRepository.findThread(ulleungThreadId).title)
        assertEquals("chat-andong-dosan", dosanThreadId)
        assertEquals("안동 도산서원 그늘 코스", MockTripRepository.findThread(dosanThreadId).title)
    }

    @Test
    fun ulleungTripApplicationStaysPendingWithoutOpeningChat() {
        composeRule.onNodeWithTag("bottom-my").performClick()
        composeRule.onNodeWithText("찜한 코스").performClick()
        composeRule
            .onNode(hasScrollAction())
            .performScrollToNode(hasText("울릉도 2박 3일 섬 여행"))
        composeRule.onNodeWithText("울릉도 2박 3일 섬 여행").performClick()
        openRecruitmentListFromCourseDetail()

        composeRule.onNodeWithTag("trip-apply-button").performClick()
        composeRule.onNodeWithText("신청하기").performClick()
        composeRule.onNodeWithText("신청을 보냈어요").assertIsDisplayed()
        composeRule.onNodeWithTag("application-done").performClick()
        composeRule.onNodeWithContentDescription("뒤로").performClick()
        composeRule.onNodeWithContentDescription("뒤로").performClick()
        composeRule.onNodeWithTag("bottom-meetings").performClick()
        composeRule.onNodeWithTag("meetings-tab-applied").performClick()
        composeRule.onNodeWithText("울릉도 2박 3일 섬 여행").assertIsDisplayed()
        composeRule.onNodeWithText("승인 대기").assertIsDisplayed()
        composeRule.onAllNodesWithTag("chat-message-input").assertCountEquals(0)
    }

    private fun openRecruitmentListFromCourseDetail() {
        composeRule.onNodeWithText("모집 중인 모임 보기").performClick()
    }

    private fun completeCreateRecruitment() {
        composeRule.onNodeWithTag("create-source-next").performClick()
        composeRule.onNodeWithText("모집 만들기 (2/5)").assertIsDisplayed()
        composeRule.onNodeWithTag("create-schedule-next").performClick()
        composeRule.onNodeWithText("모집 만들기 (3/5)").assertIsDisplayed()
        composeRule.onNodeWithTag("create-people-next").performClick()
        composeRule.onNodeWithText("모집 만들기 (4/5)").assertIsDisplayed()
        composeRule.onNodeWithTag("meeting-point-save").performClick()
        composeRule.onNodeWithText("모집 만들기 (5/5)").assertIsDisplayed()
        composeRule.onNodeWithTag("create-summary-submit").performClick()
    }

    private fun clickAuthNode(tag: String) {
        composeRule
            .onNode(hasScrollAction())
            .performScrollToNode(hasTestTag(tag))
        composeRule.onNodeWithTag(tag).performClick()
    }

    private fun openNicknameSelection() {
        openAuthLogin()
        clickAuthNode("auth-login-kakao")
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("auth-nickname-option-0").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun openAuthLogin() {
        composeRule.onNodeWithTag("home-mock-auth-entry").performClick()
        clickAuthNode("auth-onboarding-next")
        clickAuthNode("auth-onboarding-next")
        clickAuthNode("auth-onboarding-next")
    }
}
