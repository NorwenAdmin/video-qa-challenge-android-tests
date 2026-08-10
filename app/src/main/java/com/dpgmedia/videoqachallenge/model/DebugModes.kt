package com.dpgmedia.videoqachallenge.model

enum class ContentMode(val rawValue: String, val displayName: String, val testTag: String) {
    SUCCESS("success", "Success", "debug_content_success"),
    EMPTY("empty", "Empty", "debug_content_empty"),
    ERROR("error", "Server error", "debug_content_error"),
    SLOW("slow", "Slow response", "debug_content_slow");

    companion object {
        fun fromRawValue(value: String?): ContentMode? =
            entries.firstOrNull { it.rawValue == value }
    }
}

enum class VideoMode(val rawValue: String, val displayName: String, val testTag: String) {
    NORMAL("normal", "Normal playback", "debug_video_normal"),
    BUFFERING("buffering", "Long buffering", "debug_video_buffering"),
    ERROR("error", "Playback error", "debug_video_error"),
    COMPLETE_QUICKLY("completeQuickly", "Playback completes quickly", "debug_video_complete_quickly");

    companion object {
        fun fromRawValue(value: String?): VideoMode? = when (value) {
            null -> null
            "complete_quickly" -> COMPLETE_QUICKLY
            else -> entries.firstOrNull { it.rawValue == value }
        }
    }
}
