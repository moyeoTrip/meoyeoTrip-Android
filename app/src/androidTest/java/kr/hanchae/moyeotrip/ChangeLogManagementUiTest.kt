package kr.hanchae.moyeotrip

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import kr.hanchae.moyeotrip.ui.navigation.MoyeoTripApp
import org.junit.Rule
import org.junit.Test

class ChangeLogManagementUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun chatFriendAndSafetyScreensHaveReachableQaRoutes() {
        show("chatMenu")
        composeRule.onNodeWithTag("chat-menu-screen").assertIsDisplayed()
        show("chatAttach")
        composeRule.onNodeWithTag("chat-attach-screen").assertIsDisplayed()
        show("friends")
        composeRule.onNodeWithTag("friends-screen").assertIsDisplayed()
        show("tripMessage")
        composeRule.onNodeWithTag("trip-message-screen").assertIsDisplayed()
        show("report")
        composeRule.onNodeWithTag("report-screen").assertIsDisplayed()
        show("blockedUsers")
        composeRule.onNodeWithTag("blocked-users-screen").assertIsDisplayed()
    }

    @Test
    fun tripNotificationDeleteAndSystemScreensUseFinalPolicies() {
        show("tripDay")
        composeRule.onNodeWithTag("trip-day-screen").assertIsDisplayed()
        show("coursePublish")
        composeRule.onNodeWithTag("course-publish-screen").assertIsDisplayed()
        show("notifDetail")
        composeRule.onNodeWithTag("notification-detail-screen").assertIsDisplayed()
        show("accountDelete")
        composeRule.onNodeWithTag("account-delete-screen").assertIsDisplayed()
        composeRule.onAllNodesWithText("30일", substring = true)[0].assertIsDisplayed()
        show("maintenance")
        composeRule.onNodeWithTag("system-maintenance-screen").assertIsDisplayed()
        show("error500")
        composeRule.onNodeWithTag("system-error-screen").assertIsDisplayed()
    }

    @Test
    fun commentsAreReachableAndHomeDoesNotExposeAuthDemo() {
        show("feedComments:1")
        composeRule.onNodeWithTag("feed-comments-screen-srv-1").assertIsDisplayed()
        show("home")
        composeRule.onAllNodesWithText("회원가입 · 로그인 체험").assertCountEquals(0)
    }

    private fun show(screen: String) {
        composeRule.activity.setContent {
            MoyeoTripApp(startScreen = screen, skipStartupSplash = true, skipAuthentication = true)
        }
        composeRule.waitForIdle()
    }
}
