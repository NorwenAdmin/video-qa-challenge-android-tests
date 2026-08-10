package com.videoqa.challenge

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke tests used to verify the app build. They are not part of the
 * candidate assignment, but they double as executable documentation of the
 * test tags (exposed as resource-id to Appium/UiAutomator).
 */
@RunWith(AndroidJUnit4::class)
class SmokeTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private fun launch(vararg extras: Pair<String, Any>): ActivityScenario<MainActivity> {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        extras.forEach { (key, value) ->
            when (value) {
                is Boolean -> intent.putExtra(key, value)
                is String -> intent.putExtra(key, value)
                is Long -> intent.putExtra(key, value)
                is Int -> intent.putExtra(key, value)
            }
        }
        return ActivityScenario.launch(intent)
    }

    private fun waitForTag(tag: String, timeoutMs: Long = 10_000) {
        compose.waitUntil(timeoutMs) {
            compose.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForStateLabel(text: String, timeoutMs: Long = 15_000) {
        compose.waitUntil(timeoutMs) {
            compose.onAllNodes(hasTestTag("video_state_label"))
                .fetchSemanticsNodes()
                .any { node ->
                    node.config.getOrNull(SemanticsProperties.Text)
                        ?.any { annotated -> annotated.text == text } == true
                }
        }
    }

    @Test
    fun consentContentDetailAndPlaybackFlow() {
        val scenario = launch("resetAllState" to true)

        // Consent appears on first launch
        waitForTag("consent_accept_button", 5_000)
        compose.onNodeWithTag("consent_reject_button").assertIsDisplayed()
        compose.onNodeWithTag("consent_manage_preferences_button").assertIsDisplayed()
        compose.onNodeWithTag("consent_accept_button").performClick()

        // Overview loads after a variable delay
        waitForTag("content_item_amsterdam")
        compose.onNodeWithTag("content_title_amsterdam", useUnmergedTree = true)
            .assertTextEquals("Amsterdam from above")

        // Scrollable content: last item starts outside the viewport
        compose.onNodeWithTag("content_list")
            .performScrollToNode(hasTestTag("content_item_interview"))
        compose.onNodeWithTag("content_item_interview").assertIsDisplayed()
        compose.onNodeWithTag("content_list")
            .performScrollToNode(hasTestTag("content_item_amsterdam"))

        // Detail
        compose.onNodeWithTag("content_item_amsterdam").performClick()
        waitForTag("detail_title", 5_000)
        compose.onNodeWithTag("detail_title").assertTextEquals("Amsterdam from above")
        compose.onNodeWithTag("detail_category").assertTextEquals("Travel")

        // Playback: Idle -> Buffering -> Playing
        compose.onNodeWithTag("video_play_button").performClick()
        waitForTag("video_state_label", 5_000)
        waitForStateLabel("Playing")

        // Pause and resume
        compose.onNodeWithTag("video_pause_button").performClick()
        waitForStateLabel("Paused", 5_000)
        compose.onNodeWithTag("video_play_button").performClick()
        waitForStateLabel("Playing")

        // Back
        compose.onNodeWithTag("detail_back_button").performClick()
        waitForTag("content_item_amsterdam", 5_000)

        scenario.close()
    }

    @Test
    fun contentErrorAndEmptyModesViaLaunchExtras() {
        val scenario = launch("resetAllState" to true, "contentMode" to "error")
        waitForTag("consent_accept_button", 5_000)
        compose.onNodeWithTag("consent_accept_button").performClick()

        waitForTag("content_error_message")
        compose.onNodeWithTag("content_error_retry_button").assertIsDisplayed()
        scenario.close()

        // Consent must persist after relaunch, and empty state should show
        val scenario2 = launch("contentMode" to "empty")
        waitForTag("content_empty_retry_button")
        scenario2.close()
    }

    @Test
    fun videoErrorModeShowsRetry() {
        val scenario = launch("resetAllState" to true, "videoMode" to "error")
        waitForTag("consent_accept_button", 5_000)
        compose.onNodeWithTag("consent_accept_button").performClick()

        waitForTag("content_item_amsterdam")
        compose.onNodeWithTag("content_item_amsterdam").performClick()

        compose.onNodeWithTag("video_play_button").performClick()
        waitForTag("video_error_message")
        compose.onNodeWithTag("video_retry_button").assertIsDisplayed()
        waitForStateLabel("Error", 5_000)
        scenario.close()
    }

    @Test
    fun managePreferencesFlow() {
        val scenario = launch("resetAllState" to true)
        waitForTag("consent_manage_preferences_button", 5_000)
        compose.onNodeWithTag("consent_manage_preferences_button").performClick()

        waitForTag("analytics_toggle", 5_000)
        compose.onNodeWithTag("analytics_toggle").performClick()
        compose.onNodeWithTag("preferences_save_button").performClick()

        waitForTag("content_item_amsterdam")
        scenario.close()
    }

    @Test
    fun debugOptionsSwitchToEmptyAndResetConsent() {
        val scenario = launch("resetAllState" to true)
        waitForTag("consent_accept_button", 5_000)
        compose.onNodeWithTag("consent_accept_button").performClick()

        waitForTag("content_item_amsterdam")
        compose.onNodeWithTag("debug_options_button").performClick()

        waitForTag("debug_content_empty", 5_000)
        compose.onNodeWithTag("debug_content_empty").performClick()
        compose.onNodeWithTag("debug_done_button").performClick()

        waitForTag("content_empty_retry_button")

        compose.onNodeWithTag("debug_options_button").performClick()
        waitForTag("debug_reset_consent", 5_000)
        compose.onNodeWithTag("debug_reset_consent").performClick()

        waitForTag("consent_accept_button", 5_000)
        scenario.close()
    }
}
