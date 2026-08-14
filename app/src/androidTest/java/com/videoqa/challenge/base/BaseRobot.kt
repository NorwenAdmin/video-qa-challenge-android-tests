package com.videoqa.challenge.base

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.performClick
import com.videoqa.challenge.util.TestTimeouts.DEFAULT
import com.videoqa.challenge.util.waitUntilClickable

abstract class BaseRobot(val composeRule: ComposeTestRule) {
    fun click(tag: String, timeoutMillis: Long = DEFAULT) = click(hasTestTag(tag), timeoutMillis)

    fun click(matcher: SemanticsMatcher, timeoutMillis: Long = DEFAULT) {
        composeRule.waitUntilClickable(matcher, timeoutMillis)
        composeRule.onNode(matcher).performClick()
    }
}