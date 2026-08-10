package com.videoqa.challenge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.videoqa.challenge.data.LaunchArguments
import com.videoqa.challenge.ui.VideoQAApp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val launchArguments = LaunchArguments.fromExtras(intent.extras)
        val container = AppContainer(applicationContext, launchArguments)
        setContent {
            VideoQAApp(container)
        }
    }
}
