package com.videoqa.challenge.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.videoqa.challenge.model.ContentMode
import com.videoqa.challenge.model.VideoMode

/** Thin wrapper around SharedPreferences so every persisted key lives in one place. */
class PersistenceService(context: Context) {

    private object Key {
        const val CONSENT_CHOICE = "consent.choice"
        const val ANALYTICS = "consent.analytics"
        const val PERSONALISATION = "consent.personalisation"
        const val CONTENT_MODE = "debug.contentMode"
        const val VIDEO_MODE = "debug.videoMode"
        const val PROGRESS_PREFIX = "playback.progress."
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences("videoqachallenge_prefs", Context.MODE_PRIVATE)

    // Consent

    var consentChoice: String?
        get() = prefs.getString(Key.CONSENT_CHOICE, null)
        set(value) = prefs.edit { putString(Key.CONSENT_CHOICE, value) }

    var analyticsEnabled: Boolean
        get() = prefs.getBoolean(Key.ANALYTICS, false)
        set(value) = prefs.edit { putBoolean(Key.ANALYTICS, value) }

    var personalisationEnabled: Boolean
        get() = prefs.getBoolean(Key.PERSONALISATION, false)
        set(value) = prefs.edit { putBoolean(Key.PERSONALISATION, value) }

    fun resetConsent() = prefs.edit {
        remove(Key.CONSENT_CHOICE)
        remove(Key.ANALYTICS)
        remove(Key.PERSONALISATION)
    }

    // Debug configuration

    var storedContentMode: ContentMode?
        get() = ContentMode.fromRawValue(prefs.getString(Key.CONTENT_MODE, null))
        set(value) = prefs.edit { putString(Key.CONTENT_MODE, value?.rawValue) }

    var storedVideoMode: VideoMode?
        get() = VideoMode.fromRawValue(prefs.getString(Key.VIDEO_MODE, null))
        set(value) = prefs.edit { putString(Key.VIDEO_MODE, value?.rawValue) }

    // Playback progress

    fun playbackProgressMs(contentId: String): Long =
        prefs.getLong(Key.PROGRESS_PREFIX + contentId, 0L)

    fun setPlaybackProgressMs(contentId: String, positionMs: Long) =
        prefs.edit { putLong(Key.PROGRESS_PREFIX + contentId, positionMs) }

    fun clearPlaybackProgress() = prefs.edit {
        prefs.all.keys.filter { it.startsWith(Key.PROGRESS_PREFIX) }.forEach { remove(it) }
    }

    fun resetAll() = prefs.edit { clear() }
}
