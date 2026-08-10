package com.dpgmedia.videoqachallenge.model

enum class PlayerState(val label: String) {
    IDLE("Idle"),
    BUFFERING("Buffering"),
    PLAYING("Playing"),
    PAUSED("Paused"),
    ERROR("Error"),
    COMPLETED("Completed"),
}
