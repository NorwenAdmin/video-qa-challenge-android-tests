package com.dpgmedia.videoqachallenge.data

import android.content.Context
import com.dpgmedia.videoqachallenge.model.ContentItem
import com.dpgmedia.videoqachallenge.model.ContentMode
import kotlinx.coroutines.delay
import org.json.JSONArray
import kotlin.random.Random

class ContentRepositoryException : Exception("We could not load the videos")

/**
 * Simulated content service. Content comes from a bundled JSON asset and the
 * response behaviour (delay, empty, error) is driven by the debug configuration.
 */
class ContentRepository(
    private val context: Context,
    /** Fixed delay override from the contentDelayMs launch extra. */
    private val fixedDelayMs: Long? = null,
) {
    private val cache: List<ContentItem> by lazy { parseBundledContent() }

    suspend fun fetchContent(mode: ContentMode): List<ContentItem> {
        delay(delayMs(mode))
        return when (mode) {
            ContentMode.SUCCESS, ContentMode.SLOW -> cache
            ContentMode.EMPTY -> emptyList()
            ContentMode.ERROR -> throw ContentRepositoryException()
        }
    }

    /** Synchronous deterministic lookup used by the detail screen. */
    fun findItem(contentId: String): ContentItem? = cache.firstOrNull { it.id == contentId }

    private fun delayMs(mode: ContentMode): Long {
        fixedDelayMs?.let { return it }
        return when (mode) {
            ContentMode.SLOW -> 5_000L
            else -> Random.nextLong(500L, 1_501L)
        }
    }

    private fun parseBundledContent(): List<ContentItem> {
        val json = context.assets.open("content.json").bufferedReader().use { it.readText() }
        val array = JSONArray(json)
        return (0 until array.length()).map { index ->
            val obj = array.getJSONObject(index)
            ContentItem(
                id = obj.getString("id"),
                title = obj.getString("title"),
                category = obj.getString("category"),
                durationSeconds = obj.getInt("durationSeconds"),
                publishedDate = obj.getString("publishedDate"),
                description = obj.getString("description"),
                videoAsset = obj.getString("videoAsset"),
            )
        }
    }
}
