package com.videoqa.challenge.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.videoqa.challenge.data.PersistenceService
import com.videoqa.challenge.model.ContentItem
import com.videoqa.challenge.model.PlayerState
import com.videoqa.challenge.model.VideoMode
import com.videoqa.challenge.util.VqcLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Drives the video player and exposes an explicit, testable state machine:
 * Idle -> Buffering -> Playing -> Paused/Completed/Error.
 *
 * Buffering and playback errors are simulated deterministically based on the
 * selected video mode so tests can rely on the behaviour.
 */
class PlayerViewModel(
    context: Context,
    private val content: ContentItem,
    private val videoMode: VideoMode,
    private val persistence: PersistenceService,
    private val bufferingMsOverride: Long?,
    private val scope: CoroutineScope,
) {
    var state: PlayerState by mutableStateOf(PlayerState.IDLE)
        private set
    var currentPositionMs: Long by mutableLongStateOf(0L)
        private set
    var durationMs: Long by mutableLongStateOf(0L)
        private set

    val progressFraction: Float
        get() = if (durationMs > 0) {
            (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        } else 0f

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()

    private val videoUri: Uri = Uri.parse(
        "android.resource://${context.packageName}/raw/${content.videoRawResourceName}"
    )
    private var bufferingJob: Job? = null
    private var mediaPrepared = false
    private var pendingStart = false
    private var released = false

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        durationMs = exoPlayer.duration.coerceAtLeast(0L)
                        if (pendingStart) {
                            pendingStart = false
                            applyStartPositionAndPlay()
                        }
                    }
                    Player.STATE_ENDED -> {
                        state = PlayerState.COMPLETED
                        currentPositionMs = durationMs
                        persistence.setPlaybackProgressMs(content.id, 0L)
                        VqcLog.player("Playback completed")
                    }
                    else -> Unit
                }
            }
        })

        scope.launch {
            while (isActive) {
                if (!released && state == PlayerState.PLAYING) {
                    currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
                }
                delay(250)
            }
        }
    }

    // Controls

    /** Handles both the initial play action and resume after pause. */
    fun play() {
        when (state) {
            PlayerState.PAUSED -> resume()
            PlayerState.PLAYING, PlayerState.BUFFERING -> Unit
            PlayerState.IDLE, PlayerState.ERROR, PlayerState.COMPLETED -> startPlayback()
        }
    }

    fun pause() {
        if (state != PlayerState.PLAYING) return
        exoPlayer.pause()
        currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
        state = PlayerState.PAUSED
        savePlaybackProgress()
        VqcLog.player("Player paused at ${currentPositionMs}ms")
    }

    fun retry() {
        VqcLog.player("Retry selected")
        startPlayback()
    }

    /** Saves progress and releases the player. Called when the detail screen leaves composition. */
    fun teardown() {
        if (state == PlayerState.PLAYING || state == PlayerState.PAUSED) {
            currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            savePlaybackProgress()
        }
        bufferingJob?.cancel()
        released = true
        exoPlayer.release()
    }

    // Playback flow

    private fun startPlayback() {
        state = PlayerState.BUFFERING
        VqcLog.player("Playback requested for ${content.id}")
        VqcLog.player("Player entered buffering")

        bufferingJob?.cancel()
        bufferingJob = scope.launch {
            delay(bufferingDelayMs())
            if (videoMode == VideoMode.ERROR) {
                state = PlayerState.ERROR
                VqcLog.playerError("Player failed (simulated playback error)")
                return@launch
            }
            beginPlayingAsset()
        }
    }

    private fun resume() {
        exoPlayer.play()
        state = PlayerState.PLAYING
        VqcLog.player("Player resumed")
    }

    private fun beginPlayingAsset() {
        if (!mediaPrepared) {
            exoPlayer.setMediaItem(MediaItem.fromUri(videoUri))
            exoPlayer.prepare()
            mediaPrepared = true
            pendingStart = true
        } else if (exoPlayer.playbackState == Player.STATE_READY ||
            exoPlayer.playbackState == Player.STATE_ENDED
        ) {
            applyStartPositionAndPlay()
        } else {
            pendingStart = true
        }
    }

    private fun applyStartPositionAndPlay() {
        var startMs = persistence.playbackProgressMs(content.id)
        if (videoMode == VideoMode.COMPLETE_QUICKLY) {
            startMs = (durationMs - 3_000L).coerceAtLeast(0L)
        }
        if (startMs > 0 && startMs < durationMs - 1_000L) {
            exoPlayer.seekTo(startMs)
        } else if (state == PlayerState.COMPLETED || exoPlayer.playbackState == Player.STATE_ENDED) {
            exoPlayer.seekTo(0L)
        }
        exoPlayer.play()
        state = PlayerState.PLAYING
        VqcLog.player("Player started")
    }

    private fun bufferingDelayMs(): Long {
        bufferingMsOverride?.let { return it }
        return when (videoMode) {
            VideoMode.NORMAL, VideoMode.COMPLETE_QUICKLY -> 800L
            VideoMode.BUFFERING -> 6_000L
            VideoMode.ERROR -> 1_200L
        }
    }

    private fun savePlaybackProgress() {
        persistence.setPlaybackProgressMs(content.id, currentPositionMs)
    }
}
