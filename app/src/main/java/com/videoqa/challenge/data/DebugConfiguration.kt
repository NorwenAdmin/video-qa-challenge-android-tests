package com.videoqa.challenge.data

import com.videoqa.challenge.model.ContentMode
import com.videoqa.challenge.model.VideoMode
import com.videoqa.challenge.util.VqcLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Holds the selected content and video response modes.
 *
 * Values persist across launches. Intent extras override the persisted values
 * for the current run only (the override itself is not written back); any
 * change made afterwards in the debug screen is persisted again.
 */
class DebugConfiguration(
    private val persistence: PersistenceService,
    launchArguments: LaunchArguments,
) {
    private val _contentMode = MutableStateFlow(
        launchArguments.contentMode ?: persistence.storedContentMode ?: ContentMode.SUCCESS
    )
    val contentMode: StateFlow<ContentMode> = _contentMode

    private val _videoMode = MutableStateFlow(
        launchArguments.videoMode ?: persistence.storedVideoMode ?: VideoMode.NORMAL
    )
    val videoMode: StateFlow<VideoMode> = _videoMode

    fun setContentMode(mode: ContentMode) {
        _contentMode.value = mode
        persistence.storedContentMode = mode
        VqcLog.debug("Debug content mode changed to ${mode.rawValue}")
    }

    fun setVideoMode(mode: VideoMode) {
        _videoMode.value = mode
        persistence.storedVideoMode = mode
        VqcLog.debug("Debug video mode changed to ${mode.rawValue}")
    }

    fun restoreDefaults() {
        setContentMode(ContentMode.SUCCESS)
        setVideoMode(VideoMode.NORMAL)
    }
}
