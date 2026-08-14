package com.videoqa.challenge.robots

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import com.videoqa.challenge.base.BaseRobot
import com.videoqa.challenge.base.BaseVerification

fun consentRobot(composeRule: ComposeTestRule, func: ConsentRobot.() -> Unit): ConsentRobot =
    ConsentRobot(composeRule).apply(func)

class ConsentRobot(composeRule: ComposeTestRule) : BaseRobot(composeRule) {
    fun acceptAll() = click("consent_accept_button")
    fun rejectOptional() = click("consent_reject_button")
    fun verify(block: ConsentVerification.() -> Unit) =
        ConsentVerification(composeRule).apply(block)
}

class ConsentVerification(composeRule: ComposeTestRule) : BaseVerification(composeRule) {
    fun screenShown() = assertExists(hasTestTag("consent_screen"))
    fun titleShown() = assertExists(hasText("Your privacy choices"))
    fun descriptionShown() = assertExists(
        hasText(
            "We use a small amount of local data to remember your preferences and improve your experience. Choose how this demo app may handle optional features. You can change this later from the debug options.",
        )
    )

    fun acceptAllButtonShown() =
        assertExists(hasTestTag("consent_accept_button") and hasText("Accept all"))

    fun rejectOptionalButtonShown() =
        assertExists(hasTestTag("consent_reject_button") and hasText("Reject optional"))

    fun managePreferencesButtonShown() =
        assertExists(hasTestTag("consent_manage_preferences_button") and hasText("Manage preferences"))
}