package com.videoqa.challenge.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.videoqa.challenge.AppContainer
import com.videoqa.challenge.viewmodel.ContentListViewModel

sealed interface MainScreen {
    data object Overview : MainScreen
    data class Detail(val contentId: String) : MainScreen
    data object Debug : MainScreen
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun VideoQAApp(container: AppContainer) {
    val consentCompleted by container.consentCompleted.collectAsState()

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF2D6AE7),
            secondary = Color(0xFF00838F),
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                // Exposes Compose testTags as resource-id for Appium/UiAutomator.
                .semantics { testTagsAsResourceId = true }
        ) {
            if (consentCompleted) {
                MainFlow(container)
            } else {
                ConsentFlow(container)
            }
        }
    }
}

@Composable
private fun ConsentFlow(container: AppContainer) {
    var showPreferences by remember { mutableStateOf(false) }

    if (showPreferences) {
        BackHandler { showPreferences = false }
        PreferencesScreen(
            onBack = { showPreferences = false },
            onSave = { analytics, personalisation ->
                container.selectConsent(
                    com.videoqa.challenge.ConsentChoice.CUSTOM,
                    analytics = analytics,
                    personalisation = personalisation,
                )
            },
        )
    } else {
        ConsentScreen(
            onAcceptAll = {
                container.selectConsent(com.videoqa.challenge.ConsentChoice.ACCEPTED_ALL)
            },
            onRejectOptional = {
                container.selectConsent(com.videoqa.challenge.ConsentChoice.REJECTED_OPTIONAL)
            },
            onManagePreferences = { showPreferences = true },
        )
    }
}

@Composable
private fun MainFlow(container: AppContainer) {
    var screen by remember { mutableStateOf<MainScreen>(MainScreen.Overview) }
    // Shared across navigation so returning from a detail page does not reload content.
    val listViewModel = remember { ContentListViewModel(container.repository) }

    when (val current = screen) {
        is MainScreen.Overview -> OverviewScreen(
            container = container,
            viewModel = listViewModel,
            onOpenDetail = { contentId -> screen = MainScreen.Detail(contentId) },
            onOpenDebug = { screen = MainScreen.Debug },
        )

        is MainScreen.Detail -> {
            BackHandler { screen = MainScreen.Overview }
            DetailScreen(
                container = container,
                contentId = current.contentId,
                onBack = { screen = MainScreen.Overview },
            )
        }

        is MainScreen.Debug -> {
            BackHandler { screen = MainScreen.Overview }
            DebugScreen(
                container = container,
                onDone = { screen = MainScreen.Overview },
            )
        }
    }
}
