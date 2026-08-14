package com.videoqa.challenge.base

import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.videoqa.challenge.MainActivity
import org.junit.Rule

abstract class BaseComposeTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    protected fun launchApp(extras: Intent.() -> Unit = {}) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("resetAllState", true)
            extras()
        }
        ActivityScenario.launch<MainActivity>(intent)
    }
}