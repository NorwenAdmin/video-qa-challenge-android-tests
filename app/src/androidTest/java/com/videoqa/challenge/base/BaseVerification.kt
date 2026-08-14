package com.videoqa.challenge.base

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.ComposeTestRule
import com.videoqa.challenge.util.TestTimeouts
import com.videoqa.challenge.util.waitUntilExists

abstract class BaseVerification(val composeRule: ComposeTestRule) {
    protected fun assertExists(
        matcher: SemanticsMatcher,
        timeoutMillis: Long = TestTimeouts.DEFAULT
    ) {
        composeRule.waitUntilExists(matcher, timeoutMillis)
        composeRule.onNode(matcher).assertExists()
    }
}
