package com.videoqa.challenge.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.videoqa.challenge.AppContainer
import com.videoqa.challenge.model.ContentMode
import com.videoqa.challenge.model.VideoMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    container: AppContainer,
    onDone: () -> Unit,
) {
    val contentMode by container.debugConfiguration.contentMode.collectAsState()
    val videoMode by container.debugConfiguration.videoMode.collectAsState()

    Scaffold(
        modifier = Modifier.testTag("debug_screen"),
        topBar = {
            TopAppBar(
                title = { Text("Debug options") },
                actions = {
                    TextButton(
                        onClick = onDone,
                        modifier = Modifier.testTag("debug_done_button"),
                    ) {
                        Text("Done")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            SectionHeader("Content response")
            ContentMode.entries.forEach { mode ->
                SelectionRow(
                    title = mode.displayName,
                    selected = contentMode == mode,
                    tag = mode.testTag,
                    onClick = { container.debugConfiguration.setContentMode(mode) },
                )
            }

            Spacer(Modifier.height(24.dp))

            SectionHeader("Video response")
            VideoMode.entries.forEach { mode ->
                SelectionRow(
                    title = mode.displayName,
                    selected = videoMode == mode,
                    tag = mode.testTag,
                    onClick = { container.debugConfiguration.setVideoMode(mode) },
                )
            }

            Spacer(Modifier.height(24.dp))

            SectionHeader("State controls")
            ActionRow("Reset consent", "debug_reset_consent") {
                container.resetConsent()
            }
            ActionRow("Clear playback progress", "debug_clear_progress") {
                container.clearPlaybackProgress()
            }
            ActionRow("Restore default settings", "debug_restore_defaults") {
                container.debugConfiguration.restoreDefaults()
            }
            ActionRow("Reset all app state", "debug_reset_all", destructive = true) {
                container.resetAllState()
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun SelectionRow(
    title: String,
    selected: Boolean,
    tag: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, modifier = Modifier.weight(1f))
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
    HorizontalDivider()
}

@Composable
private fun ActionRow(
    title: String,
    tag: String,
    destructive: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
    ) {
        Text(
            text = title,
            color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        )
    }
    HorizontalDivider()
}
