package com.propodcast.app.core.commonwidgets

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.action_add_to_favorites
import com.propodcast.app.resources.action_more
import com.propodcast.app.resources.action_remove_from_favorites
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.propodcast.app.core.designsystem.theme.ProPodcastTheme
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.ui.EpisodeDownloadActions
import com.propodcast.app.core.ui.formatDuration
import com.propodcast.app.core.ui.formatPubDate

/**
 * One episode: artwork, title, date · duration with the download state mark, a favorite toggle and a menu
 * with Share and the download action that fits the state (Download / Cancel download / Delete download).
 */
@Composable
fun EpisodeListItem(
    episode: Episode,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShare: (() -> Unit)?,
    modifier: Modifier = Modifier,
    downloads: EpisodeDownloadActions = EpisodeDownloadActions(),
) {
    val downloadState = downloads.stateOf(episode)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PodcastImage(url = episode.thumbnailUrl, modifier = Modifier.size(56.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = episode.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            val meta = listOf(
                episode.pubDateMs.takeIf { it > 0 }?.let(::formatPubDate),
                formatDuration(episode.durationSec).ifEmpty { null },
            ).filterNotNull().joinToString(" · ")
            if (meta.isNotEmpty() || downloadState != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (downloadState != null) {
                        Spacer(Modifier.width(6.dp))
                        DownloadIndicator(downloadState, size = 14.dp)
                    }
                }
            }
        }
        IconButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = stringResource(
                    if (isFavorite) Res.string.action_remove_from_favorites else Res.string.action_add_to_favorites,
                ),
                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        val menuItems = episodeMenuItems(episode, downloadState, downloads, onShare)
        if (menuItems.isNotEmpty()) {
            var expanded by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = stringResource(Res.string.action_more))
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    menuItems.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(stringResource(item.labelRes)) },
                            leadingIcon = { Icon(item.icon(), contentDescription = null) },
                            onClick = {
                                expanded = false
                                item.onClick()
                            },
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EpisodeListItemPreview() {
    ProPodcastTheme {
        EpisodeListItem(
            episode = Episode(
                id = "e1",
                title = "864: Part 2 of My (New) Conversation With Bobby Roberts",
                description = "",
                audioUrl = null,
                imageUrl = null,
                thumbnailUrl = null,
                pubDateMs = 1_479_110_402_562,
                durationSec = 2447,
                listennotesUrl = "https://www.listennotes.com/e/1/",
                podcastId = "p1",
                podcastTitle = "Star Wars 7x7",
            ),
            isFavorite = false,
            onClick = {},
            onToggleFavorite = {},
            onShare = {},
        )
    }
}
