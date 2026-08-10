package com.dpgmedia.videoqachallenge.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dpgmedia.videoqachallenge.data.ContentRepository
import com.dpgmedia.videoqachallenge.model.ContentItem
import com.dpgmedia.videoqachallenge.model.ContentMode
import com.dpgmedia.videoqachallenge.util.VqcLog
import kotlinx.coroutines.CancellationException

class ContentListViewModel(private val repository: ContentRepository) {

    sealed interface LoadState {
        data object Loading : LoadState
        data class Loaded(val items: List<ContentItem>) : LoadState
        data object Empty : LoadState
        data object Error : LoadState
    }

    var state: LoadState by mutableStateOf(LoadState.Loading)
        private set

    suspend fun load(mode: ContentMode) {
        state = LoadState.Loading
        VqcLog.content("Content loading started, mode=${mode.rawValue}")
        try {
            val items = repository.fetchContent(mode)
            state = if (items.isEmpty()) {
                VqcLog.content("Content loading completed with 0 items")
                LoadState.Empty
            } else {
                VqcLog.content("Content loading completed with ${items.size} items")
                LoadState.Loaded(items)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            VqcLog.contentError("Content loading failed: ${e.message}")
            state = LoadState.Error
        }
    }
}
