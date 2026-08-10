package com.dpgmedia.videoqachallenge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dpgmedia.videoqachallenge.data.LaunchArguments
import com.dpgmedia.videoqachallenge.ui.VideoQAApp

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
