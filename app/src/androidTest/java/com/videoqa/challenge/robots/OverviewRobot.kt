package com.videoqa.challenge.robots

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToNode
import com.videoqa.challenge.base.BaseRobot
import com.videoqa.challenge.base.BaseVerification
import com.videoqa.challenge.util.TestTimeouts.CONTENT_LOAD_TIMEOUT
import com.videoqa.challenge.util.VideoCard
import com.videoqa.challenge.util.waitUntilExists

fun overviewRobot(composeRule: ComposeTestRule, func: OverviewRobot.() -> Unit): OverviewRobot =
    OverviewRobot(composeRule).apply(func)

class OverviewRobot(composeRule: ComposeTestRule) : BaseRobot(composeRule) {

    fun openVideo(video: VideoCard) {
        composeRule.waitUntilExists(hasTestTag("content_list"), CONTENT_LOAD_TIMEOUT)
        composeRule.onNodeWithTag("content_list")
            .performScrollToNode(hasTestTag("content_item_${video.id}"))
        click(hasTestTag("content_item_${video.id}"), CONTENT_LOAD_TIMEOUT)
    }

    fun verify(block: OverviewVerification.() -> Unit) =
        OverviewVerification(composeRule).apply(block)
}

class OverviewVerification(composeRule: ComposeTestRule) : BaseVerification(composeRule) {
    fun assertDisplayed() {
        assertExists(hasTestTag("content_overview_screen"))
    }

    fun videoCardShown(video: VideoCard) {
        val card = hasTestTag("content_item_${video.id}")
        assertExists(card, CONTENT_LOAD_TIMEOUT)
        assertExists(card and hasText(video.title, substring = true))
        assertExists(card and hasText(video.category, substring = true))
        assertExists(card and hasText(video.publishedDate, substring = true))
    }

    fun errorStateShown() {
        assertExists(hasTestTag("content_error_state"), CONTENT_LOAD_TIMEOUT)
        assertExists(hasText("Something went wrong"))
        assertExists(
            hasTestTag("content_error_message") and hasText(
                "We could not load the videos", substring = true
            )
        )
        assertExists(hasTestTag("content_error_retry_button"))
    }
}