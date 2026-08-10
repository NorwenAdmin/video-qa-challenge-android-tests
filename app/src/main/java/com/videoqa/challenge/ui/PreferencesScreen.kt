package com.videoqa.challenge.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    onBack: () -> Unit,
    onSave: (analytics: Boolean, personalisation: Boolean) -> Unit,
) {
    var analyticsEnabled by remember { mutableStateOf(false) }
    var personalisationEnabled by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preferences") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .testTag("preferences_screen"),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Analytics", modifier = Modifier.weight(1f))
                Switch(
                    checked = analyticsEnabled,
                    onCheckedChange = { analyticsEnabled = it },
                    modifier = Modifier.testTag("analytics_toggle"),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Personalised content", modifier = Modifier.weight(1f))
                Switch(
                    checked = personalisationEnabled,
                    onCheckedChange = { personalisationEnabled = it },
                    modifier = Modifier.testTag("personalisation_toggle"),
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "These options only change locally stored values. " +
                    "No data leaves the device in this demo app.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { onSave(analyticsEnabled, personalisationEnabled) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("preferences_save_button"),
            ) {
                Text("Save preferences")
            }
        }
    }
}
