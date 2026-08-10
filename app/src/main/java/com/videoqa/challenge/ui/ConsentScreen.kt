package com.videoqa.challenge.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ConsentScreen(
    onAcceptAll: () -> Unit,
    onRejectOptional: () -> Unit,
    onManagePreferences: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("consent_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))

        Icon(
            imageVector = Icons.Default.PrivacyTip,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp),
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Your privacy choices",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "We use a small amount of local data to remember your preferences and improve " +
                "your experience. Choose how this demo app may handle optional features. " +
                "You can change this later from the debug options.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onAcceptAll,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("consent_accept_button"),
            ) {
                Text("Accept all")
            }

            OutlinedButton(
                onClick = onRejectOptional,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("consent_reject_button"),
            ) {
                Text("Reject optional")
            }

            TextButton(
                onClick = onManagePreferences,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("consent_manage_preferences_button"),
            ) {
                Text("Manage preferences")
            }
        }
    }
}
