package com.videoqa.challenge.tests

import com.videoqa.challenge.base.BaseComposeTest
import com.videoqa.challenge.model.PlayerState
import com.videoqa.challenge.robots.consentRobot
import com.videoqa.challenge.robots.detailRobot
import com.videoqa.challenge.robots.overviewRobot
import com.videoqa.challenge.util.TestFixtures.Videos.AmsterdamFromAbove
import com.videoqa.challenge.util.TestTimeouts
import org.junit.Test

class RiskScenariosTest : BaseComposeTest() {

    @Test
    fun consentRejectOptional_stillReachesOverview() {
        launchApp()
        consentRobot(composeRule) {
            rejectOptional()
        }
        overviewRobot(composeRule) {
            verify {
                assertDisplayed()
            }
        }
    }

    @Test
    fun videoError_showsErrorAndRetryButton() {
        launchApp { putExtra("videoMode", "error") }

        consentRobot(composeRule) {
            acceptAll()
        }
        overviewRobot(composeRule) {
            verify {
                assertDisplayed()
            }
            openVideo(AmsterdamFromAbove)
        }

        detailRobot(composeRule) {

            startPlayback()
            waitForState(PlayerState.ERROR)
            verify {
                stateIs(PlayerState.ERROR)
                errorMessageShown()
                retryButtonShown()
            }
        }
    }

    @Test
    fun playButton_disabledWhileBuffering() {
        launchApp { putExtra("videoMode", "buffering") }

        consentRobot(composeRule) {
            acceptAll()
        }
        overviewRobot(composeRule) {
            verify {
                assertDisplayed()
            }
            openVideo(AmsterdamFromAbove)
        }

        detailRobot(composeRule) {
            verify { assertOpened(AmsterdamFromAbove) }
            startPlayback()
            verify {
                stateIs(PlayerState.BUFFERING)
                playButtonNotClickable()
            }
        }
    }

    @Test
    fun videoCompleteQuickly_reachesCompletedState() {
        launchApp { putExtra("videoMode", "completeQuickly") }

        consentRobot(composeRule) {
            acceptAll()
        }
        overviewRobot(composeRule) {
            verify {
                assertDisplayed()
            }
            openVideo(AmsterdamFromAbove)
        }

        detailRobot(composeRule) {
            verify { assertOpened(AmsterdamFromAbove) }
            startPlayback()
            waitForState(
                PlayerState.COMPLETED,
                TestTimeouts.PLAYER_STATE_COMPLETED
            )
            verify { stateIs(PlayerState.COMPLETED) }
        }
    }

    @Test
    fun contentError_showsRetryOnOverview() {
        launchApp { putExtra("contentMode", "error") }

        consentRobot(composeRule) {
            acceptAll()
        }
        overviewRobot(composeRule) {
            verify {
                errorStateShown()
            }
        }
    }
}