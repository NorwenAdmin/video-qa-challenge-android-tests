package com.dpgmedia.videoqachallenge.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import com.dpgmedia.videoqachallenge.AppContainer
import com.dpgmedia.videoqachallenge.model.ContentItem
import com.dpgmedia.videoqachallenge.model.PlayerState
import com.dpgmedia.videoqachallenge.viewmodel.PlayerViewModel

@Composable
fun PlayerSection(container: AppContainer, item: ContentItem) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val player = remember(item.id) {
        PlayerViewModel(
            context = context,
            content = item,
            videoMode = container.debugConfiguration.videoMode.value,
            persistence = container.persistence,
            bufferingMsOverride = container.launchArguments.videoBufferingMs,
            scope = scope,
        ).also { it.play() }
    }

    DisposableEffect(item.id) {
        onDispose { player.teardown() }
    }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Color.Black),
        ) {
            AndroidView(
                factory = { viewContext ->
                    PlayerView(viewContext).apply {
                        useController = false
                        this.player = player.exoPlayer
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("video_player"),
            )

            if (player.state == PlayerState.BUFFERING) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.testTag("video_buffering_indicator"),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("Buffering…", color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            if (player.state == PlayerState.ERROR) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFFEB3B),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Video could not be played",
                            color = Color.White,
                            modifier = Modifier.testTag("video_error_message"),
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { player.retry() },
                            modifier = Modifier.testTag("video_retry_button"),
                        ) {
                            Text("Try again")
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (player.state == PlayerState.PLAYING) {
                IconButton(
                    onClick = { player.pause() },
                    modifier = Modifier.testTag("video_pause_button"),
                ) {
                    Icon(Icons.Default.Pause, contentDescription = "Pause")
                }
            } else {
                IconButton(
                    onClick = { player.play() },
                    enabled = player.state != PlayerState.BUFFERING && player.state != PlayerState.ERROR,
                    modifier = Modifier.testTag("video_play_button"),
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                }
            }

            Text(
                text = formatMs(player.currentPositionMs),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.testTag("video_current_position"),
            )

            Spacer(Modifier.width(8.dp))

            LinearProgressIndicator(
                progress = { player.progressFraction },
                modifier = Modifier
                    .weight(1f)
                    .testTag("video_progress"),
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = formatMs(player.durationMs),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.testTag("video_duration"),
            )
        }

        Text(
            text = player.state.label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(50),
                )
                .padding(horizontal = 10.dp, vertical = 4.dp)
                .testTag("video_state_label"),
        )
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    return "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}
