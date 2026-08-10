package com.videoqa.challenge

import android.content.Context
import com.videoqa.challenge.data.ContentRepository
import com.videoqa.challenge.data.DebugConfiguration
import com.videoqa.challenge.data.LaunchArguments
import com.videoqa.challenge.data.PersistenceService
import com.videoqa.challenge.util.VqcLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class ConsentChoice(val rawValue: String) {
    ACCEPTED_ALL("accepted_all"),
    REJECTED_OPTIONAL("rejected_optional"),
    CUSTOM("custom"),
}

/** Root application state and shared services, mirroring the iOS AppModel. */
class AppContainer(context: Context, val launchArguments: LaunchArguments) {

    val persistence = PersistenceService(context)

    init {
        if (launchArguments.resetAllState) {
            persistence.resetAll()
            VqcLog.app("Launch extra resetAllState applied")
        }
        if (launchArguments.resetConsent) {
            persistence.resetConsent()
            VqcLog.app("Launch extra resetConsent applied")
        }
    }

    val debugConfiguration = DebugConfiguration(persistence, launchArguments)
    val repository = ContentRepository(context, launchArguments.contentDelayMs)

    private val _consentCompleted = MutableStateFlow(persistence.consentChoice != null)
    val consentCompleted: StateFlow<Boolean> = _consentCompleted

    init {
        VqcLog.app("App launched, consentCompleted=${_consentCompleted.value}")
    }

    // Consent

    fun selectConsent(
        choice: ConsentChoice,
        analytics: Boolean? = null,
        personalisation: Boolean? = null,
    ) {
        persistence.consentChoice = choice.rawValue
        when (choice) {
            ConsentChoice.ACCEPTED_ALL -> {
                persistence.analyticsEnabled = true
                persistence.personalisationEnabled = true
            }
            ConsentChoice.REJECTED_OPTIONAL -> {
                persistence.analyticsEnabled = false
                persistence.personalisationEnabled = false
            }
            ConsentChoice.CUSTOM -> {
                persistence.analyticsEnabled = analytics ?: false
                persistence.personalisationEnabled = personalisation ?: false
            }
        }
        _consentCompleted.value = true
        VqcLog.consent("Consent selected: ${choice.rawValue}")
    }

    // State reset

    fun resetConsent() {
        persistence.resetConsent()
        _consentCompleted.value = false
        VqcLog.debug("Consent reset")
    }

    fun clearPlaybackProgress() {
        persistence.clearPlaybackProgress()
        VqcLog.debug("Playback progress cleared")
    }

    fun resetAllState() {
        persistence.resetAll()
        debugConfiguration.restoreDefaults()
        _consentCompleted.value = false
        VqcLog.debug("All app state reset")
    }
}
