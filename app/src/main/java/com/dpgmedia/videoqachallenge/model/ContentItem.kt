package com.dpgmedia.videoqachallenge.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ContentItem(
    val id: String,
    val title: String,
    val category: String,
    val durationSeconds: Int,
    val publishedDate: String,
    val description: String,
    val videoAsset: String,
) {
    val formattedDuration: String
        get() = "%02d:%02d".format(durationSeconds / 60, durationSeconds % 60)

    val formattedPublishedDate: String
        get() = try {
            LocalDate.parse(publishedDate)
                .format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH))
        } catch (_: Exception) {
            publishedDate
        }

    /** Raw resource name of the bundled video, without the file extension. */
    val videoRawResourceName: String
        get() = videoAsset.substringBeforeLast('.')
}
