package com.videoqa.challenge.robots

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.hasStateDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import com.videoqa.challenge.base.BaseRobot
import com.videoqa.challenge.base.BaseVerification
import com.videoqa.challenge.model.PlayerState
import com.videoqa.challenge.util.TestTimeouts.CONTENT_LOAD_TIMEOUT
import com.videoqa.challenge.util.TestTimeouts.PLAYER_STATE_TRANSITION
import com.videoqa.challenge.util.VideoCard
import com.videoqa.challenge.util.waitUntilState

fun detailRobot(composeRule: ComposeTestRule, func: DetailRobot.() -> Unit): DetailRobot =
    DetailRobot(composeRule).apply(func)

class DetailRobot(composeRule: ComposeTestRule) : BaseRobot(composeRule) {
    fun back() = click("detail_back_button")
    fun startPlayback() = click("video_play_button")
    fun pause() = click("video_pause_button")

    fun waitForState(
        state: PlayerState,
        timeoutMillis: Long = PLAYER_STATE_TRANSITION
    ) = composeRule.waitUntilState(state, timeoutMillis)

    fun verify(block: DetailVerification.() -> Unit) =
        DetailVerification(composeRule).apply(block)
}

class DetailVerification(composeRule: ComposeTestRule) : BaseVerification(composeRule) {

    fun assertOpened(card: VideoCard) =
        assertExists(hasTestTag("detail_title") and hasStateDescription(card.id))

    fun titleShown(card: VideoCard) =
        assertExists(hasTestTag("detail_title") and hasText(card.title))

    fun categoryShown(card: VideoCard) =
        assertExists(hasTestTag("detail_category") and hasText(card.category))

    fun descriptionShown(card: VideoCard) =
        assertExists(hasTestTag("detail_description") and hasText(card.description))

    fun publishedDateShown(card: VideoCard) =
        assertExists(hasText(card.publishedDate, substring = true))

    fun durationShown(card: VideoCard) =
        assertExists(hasText(card.duration, substring = true))

    fun stateIs(state: PlayerState) =
        assertExists(hasTestTag("video_state_label") and hasText(state.label))

    fun errorMessageShown() = assertExists(hasTestTag("video_error_message"))
    fun retryButtonShown() = assertExists(hasTestTag("video_retry_button"))
    fun playButtonNotClickable() =
        composeRule.onNode(hasTestTag("video_play_button") and isEnabled()).assertDoesNotExist()

    fun positionAdvancedFromZero(timeoutMillis: Long = CONTENT_LOAD_TIMEOUT) {
        composeRule.waitUntil(timeoutMillis) {
            val text = composeRule.onNodeWithTag("video_current_position")
                .fetchSemanticsNode().config[SemanticsProperties.Text].joinToString { it.text }
            text != "00:00"
        }
    }
}