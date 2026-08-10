package com.dpgmedia.videoqachallenge.util

import android.util.Log

/**
 * Consistent logging with one tag per category, mirroring the iOS app's
 * unified logging categories. Filter with e.g. `adb logcat -s VQC.player`.
 */
object VqcLog {
    fun app(message: String) = Log.i("VQC.app", message)
    fun content(message: String) = Log.i("VQC.content", message)
    fun consent(message: String) = Log.i("VQC.consent", message)
    fun player(message: String) = Log.i("VQC.player", message)
    fun debug(message: String) = Log.i("VQC.debug", message)
    fun contentError(message: String) = Log.e("VQC.content", message)
    fun playerError(message: String) = Log.e("VQC.player", message)
}
