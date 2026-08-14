package com.videoqa.challenge.tests

import com.videoqa.challenge.base.BaseComposeTest
import com.videoqa.challenge.model.PlayerState
import com.videoqa.challenge.robots.consentRobot
import com.videoqa.challenge.robots.detailRobot
import com.videoqa.challenge.robots.overviewRobot
import com.videoqa.challenge.util.TestFixtures.Videos.AmsterdamFromAbove
import com.videoqa.challenge.util.TestFixtures.Videos.WeekendTravelGuide
import org.junit.Test

class HappyPathTest : BaseComposeTest() {
    @Test
    fun happyPathFlow_fullStateMachine() {
        launchApp()

        consentRobot(composeRule) {
            verify {
                screenShown()
                titleShown()
                descriptionShown()
                acceptAllButtonShown()
                rejectOptionalButtonShown()
                managePreferencesButtonShown()
            }
            acceptAll()
        }
        overviewRobot(composeRule) {
            verify {
                assertDisplayed()
                videoCardShown(AmsterdamFromAbove)
            }
            openVideo(AmsterdamFromAbove)
        }
        detailRobot(composeRule) {
            verify {
                titleShown(AmsterdamFromAbove)
                categoryShown(AmsterdamFromAbove)
                descriptionShown(AmsterdamFromAbove)
                publishedDateShown(AmsterdamFromAbove)
                durationShown(AmsterdamFromAbove)
            }
            startPlayback()
            waitForState(PlayerState.PLAYING)
            verify {
                stateIs(PlayerState.PLAYING)
                positionAdvancedFromZero()
            }
            pause()
            waitForState(PlayerState.PAUSED)
            verify {
                stateIs(PlayerState.PAUSED)
            }
            back()
        }
        overviewRobot(composeRule) {
            verify {
                assertDisplayed()
            }
            openVideo(WeekendTravelGuide)
        }

        detailRobot(composeRule) {
            verify {
                assertOpened(WeekendTravelGuide)
                titleShown(WeekendTravelGuide)
                categoryShown(WeekendTravelGuide)
                descriptionShown(WeekendTravelGuide)
                publishedDateShown(WeekendTravelGuide)
            }
        }
    }
}