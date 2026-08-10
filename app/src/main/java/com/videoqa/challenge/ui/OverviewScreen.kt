package com.videoqa.challenge.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.videoqa.challenge.AppContainer
import com.videoqa.challenge.model.ContentItem
import com.videoqa.challenge.viewmodel.ContentListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    container: AppContainer,
    viewModel: ContentListViewModel,
    onOpenDetail: (String) -> Unit,
    onOpenDebug: () -> Unit,
) {
    val contentMode by container.debugConfiguration.contentMode.collectAsState()
    var loadToken by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(loadToken, contentMode) {
        viewModel.load(contentMode)
    }

    Scaffold(
        modifier = Modifier.testTag("content_overview_screen"),
        topBar = {
            TopAppBar(
                title = { Text("Video") },
                actions = {
                    IconButton(
                        onClick = { loadToken += 1 },
                        modifier = Modifier.testTag("content_refresh_button"),
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(
                        onClick = onOpenDebug,
                        modifier = Modifier.testTag("debug_options_button"),
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Debug options")
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (val state = viewModel.state) {
                is ContentListViewModel.LoadState.Loading -> LoadingState()
                is ContentListViewModel.LoadState.Loaded -> ContentList(
                    items = state.items,
                    onOpenDetail = onOpenDetail,
                )
                is ContentListViewModel.LoadState.Empty -> EmptyState(onRetry = { loadToken += 1 })
                is ContentListViewModel.LoadState.Error -> ErrorState(onRetry = { loadToken += 1 })
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.testTag("content_loading_indicator"),
        )
        Spacer(Modifier.height(16.dp))
        Text("Loading videos…", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("content_empty_state"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Default.Movie,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        Text("No videos are available", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            modifier = Modifier.testTag("content_empty_retry_button"),
        ) {
            Text("Try again")
        }
    }
}

@Composable
private fun ErrorState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("content_error_state"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = Color(0xFFE65100),
        )
        Spacer(Modifier.height(16.dp))
        Text("Something went wrong", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "We could not load the videos",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag("content_error_message"),
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            modifier = Modifier.testTag("content_error_retry_button"),
        ) {
            Text("Try again")
        }
    }
}

@Composable
private fun ContentList(
    items: List<ContentItem>,
    onOpenDetail: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("content_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(items, key = { it.id }) { item ->
            ContentCard(item = item, onClick = { onOpenDetail(item.id) })
        }
    }
}

@Composable
private fun ContentCard(item: ContentItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("content_item_${item.id}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Brush.linearGradient(categoryColors(item.category))),
        ) {
            Icon(
                Icons.Default.SmartDisplay,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier
                    .size(44.dp)
                    .align(Alignment.Center),
            )
            Text(
                text = item.formattedDuration,
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(50))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }

        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.testTag("content_title_${item.id}"),
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "  ·  ${item.formattedPublishedDate}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun categoryColors(category: String): List<Color> = when (category) {
    "Travel" -> listOf(Color(0xFF00BCD4), Color(0xFF1565C0))
    "News" -> listOf(Color(0xFF3949AB), Color(0xFF8E24AA))
    "Technology" -> listOf(Color(0xFFFB8C00), Color(0xFFD81B60))
    else -> listOf(Color(0xFF78909C), Color(0xFF1565C0))
}
