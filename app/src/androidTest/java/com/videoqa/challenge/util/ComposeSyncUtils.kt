package com.videoqa.challenge.util

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.junit4.ComposeTestRule
import com.videoqa.challenge.model.PlayerState

fun ComposeTestRule.waitUntilExists(
    matcher: SemanticsMatcher,
    timeoutMillis: Long = TestTimeouts.DEFAULT,
) = waitUntil(timeoutMillis) { onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty() }

fun ComposeTestRule.waitUntilClickable(
    matcher: SemanticsMatcher,
    timeoutMillis: Long = TestTimeouts.DEFAULT,
) = waitUntil(timeoutMillis) {
    onAllNodes(matcher and isEnabled()).fetchSemanticsNodes().isNotEmpty()
}

fun ComposeTestRule.waitUntilState(
    state: PlayerState,
    timeoutMillis: Long = TestTimeouts.DEFAULT,
) = waitUntilExists(hasTestTag("video_state_label") and hasText(state.label), timeoutMillis)