package kr.hanchae.moyeotrip

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import kr.hanchae.moyeotrip.data.MockTripRepository
import kr.hanchae.moyeotrip.ui.navigation.MoyeoTripApp
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RecruitmentChangeLogUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun reset() = MockTripRepository.resetSessionFeedPostsForTests()

    @After
    fun cleanUp() = MockTripRepository.resetSessionFeedPostsForTests()

    @Test
    fun creationSourceAndScheduleStatesHaveDirectQaRoutes() {
        show("create")
        composeRule.onNodeWithText("모집 만들기 (1/5)").assertIsDisplayed()
        composeRule.onNodeWithTag("course-source-linked").assertIsDisplayed()
        composeRule.onNodeWithTag("course-source-custom").performClick()
        composeRule.onNodeWithTag("create-source-next").performClick()
        composeRule.onNodeWithText("코스 직접 만들기").assertIsDisplayed()
        composeRule.onNodeWithTag("custom-course-add-stop").assertIsDisplayed()

        show("createSchedule")
        composeRule.onNodeWithText("모집 만들기 (2/5)").assertIsDisplayed()
        composeRule.onNodeWithTag("schedule-day-trip").assertIsDisplayed()
        composeRule.onNodeWithTag("schedule-overnight").performClick()
        composeRule.onNodeWithText("여행 종료 날짜 *").assertIsDisplayed()

        show("createPeople")
        composeRule.onNodeWithText("모집 만들기 (3/5)").assertIsDisplayed()
        composeRule.onNodeWithTag("create-people-next").assertIsDisplayed()

        show("createMeet")
        composeRule.onNodeWithText("모집 만들기 (4/5)").assertIsDisplayed()
        composeRule.onNodeWithTag("meeting-point-save").assertIsDisplayed()
    }

    @Test
    fun recruitmentCreationVisitsEveryStepInOrder() {
        show("create")
        composeRule.onNodeWithText("모집 만들기 (1/5)").assertIsDisplayed()

        composeRule.onNodeWithTag("create-source-next").performClick()
        composeRule.onNodeWithText("모집 만들기 (2/5)").assertIsDisplayed()
        composeRule.onNodeWithText("일정 정하기").assertIsDisplayed()

        composeRule.onNodeWithTag("create-schedule-next").performClick()
        composeRule.onNodeWithText("모집 만들기 (3/5)").assertIsDisplayed()
        composeRule.onNodeWithText("인원 정하기").assertIsDisplayed()

        composeRule.onNodeWithTag("create-people-next").performClick()
        composeRule.onNodeWithText("모집 만들기 (4/5)").assertIsDisplayed()
        composeRule.onNodeWithText("집합 장소 정하기").assertIsDisplayed()
        composeRule.onNodeWithText("이전").assertIsDisplayed()

        composeRule.onNodeWithTag("meeting-point-save").performClick()
        composeRule.onNodeWithText("모집 만들기 (5/5)").assertIsDisplayed()
        composeRule.onNodeWithText("이대로 모집을 열까요?").assertIsDisplayed()
        composeRule.onNodeWithTag("create-summary-submit").assertIsDisplayed()
    }

    @Test
    fun routeScreensExposeEditableLinkedAndConfirmedPolicies() {
        show("courseEdit")
        composeRule.onNodeWithTag("course-route-save").assertIsDisplayed()
        composeRule.onNodeWithText("저장하고 멤버에게 알리기").assertIsDisplayed()

        show("courseEditLinked")
        composeRule.onNodeWithText("등록된 코스의 경로는 고정돼요. 집합 정보와 모집 조건만 바꿀 수 있어요.").assertIsDisplayed()
        composeRule.onAllNodesWithText("저장하고 멤버에게 알리기").assertCountEquals(0)

        show("courseEditLocked")
        composeRule.onNodeWithText("여행이 확정돼 경로가 잠겼어요. 변경이 필요하면 채팅방 공지로 알려주세요.").assertIsDisplayed()
        composeRule.onNodeWithText("경로 수정").assertIsDisplayed()
    }

    @Test
    fun appliedTabShowsStatusActionsWithoutChatEntry() {
        MockTripRepository.applyToTrip("trip-andong-dosan")
        show("chatListApplied")

        composeRule.onNodeWithTag("meetings-tab-applied").assertIsDisplayed()
        composeRule.onNodeWithTag("meeting-application-trip-andong-dosan").assertIsDisplayed()
        composeRule.onNodeWithText("승인 대기").assertIsDisplayed()
        composeRule.onNodeWithText("신청 취소").assertIsDisplayed()
        composeRule.onAllNodesWithText("모임 채팅으로 이동").assertCountEquals(0)
    }

    @Test
    fun offlineStatesKeepCachedContentAndQueueChatWithoutExposingQaAuth() {
        show("offline")
        composeRule.onNodeWithTag("offline-no-cache").assertIsDisplayed()
        composeRule.onNodeWithTag("offline-connection-icon").assertIsDisplayed()
        composeRule.onNodeWithText("연결 상태를 확인해주세요").assertIsDisplayed()
        composeRule.onNodeWithText("다시 시도").assertIsDisplayed()

        show("offlineCached")
        composeRule.onNodeWithTag("offline-cached-banner").assertIsDisplayed()
        composeRule.onNodeWithText("모여트립 in 경북").assertIsDisplayed()
        composeRule.onAllNodesWithText("회원가입 · 로그인 체험").assertCountEquals(0)

        show("offlineChat")
        composeRule.onNodeWithTag("offline-chat-banner").assertIsDisplayed()
        composeRule.onNodeWithTag("chat-message-input").performTextInput("연결되면 보내주세요")
        composeRule.onNodeWithTag("chat-message-send").performClick()
        composeRule.onNodeWithTag("chat-message-pending").assertIsDisplayed()
        composeRule.onNodeWithText("전송 대기").assertIsDisplayed()
    }

    @Test
    fun confirmedTripUsesFinalMascotHeroAndSingleCelebrationMoment() {
        show("tripConfirmed")
        composeRule.onNodeWithTag("trip-confirmed-screen").assertIsDisplayed()
        composeRule.onNodeWithText("여행이 확정됐어요!").assertIsDisplayed()
        composeRule.onNodeWithText("확정된 여행").assertIsDisplayed()
        composeRule.onNodeWithText("채팅방으로 가기").assertIsDisplayed()
    }

    private fun show(screen: String) {
        composeRule.activity.setContent {
            MoyeoTripApp(startScreen = screen, skipStartupSplash = true, skipAuthentication = true)
        }
        composeRule.waitForIdle()
    }
}
