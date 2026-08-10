package com.videoqa.challenge.data

import android.os.Bundle
import com.videoqa.challenge.model.ContentMode
import com.videoqa.challenge.model.VideoMode

/**
 * Test-setup configuration passed through intent extras.
 *
 * Example:
 *   adb shell am start -S -n com.videoqa.challenge/.MainActivity \
 *     --ez resetAllState true --es contentMode error --ei contentDelayMs 1000
 *
 * Extras override persisted debug settings for that application run only.
 */
data class LaunchArguments(
    val resetAllState: Boolean = false,
    val resetConsent: Boolean = false,
    val contentMode: ContentMode? = null,
    val videoMode: VideoMode? = null,
    val contentDelayMs: Long? = null,
    val videoBufferingMs: Long? = null,
) {
    companion object {
        fun fromExtras(extras: Bundle?): LaunchArguments {
            if (extras == null) return LaunchArguments()

            fun flag(key: String): Boolean = when (val value = extras.get(key)) {
                is Boolean -> value
                is String -> value.equals("true", ignoreCase = true)
                else -> false
            }

            fun long(key: String): Long? = extras.get(key)?.toString()?.toLongOrNull()

            return LaunchArguments(
                resetAllState = flag("resetAllState"),
                resetConsent = flag("resetConsent"),
                contentMode = ContentMode.fromRawValue(extras.getString("contentMode")),
                videoMode = VideoMode.fromRawValue(extras.getString("videoMode")),
                contentDelayMs = long("contentDelayMs"),
                videoBufferingMs = long("videoBufferingMs"),
            )
        }
    }
}
