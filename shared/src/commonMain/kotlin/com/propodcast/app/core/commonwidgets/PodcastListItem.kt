package com.propodcast.app.core.commonwidgets

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.episodes_count
import com.propodcast.app.resources.action_add_to_favorites
import com.propodcast.app.resources.action_more
import com.propodcast.app.resources.action_remove_from_favorites
import com.propodcast.app.resources.action_share
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
import androidx.compose.material.icons.filled.Share
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
import com.propodcast.app.core.ui.sharedPodcastArtwork
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.ui.englishPluralStringResource

/**
 * One podcast in a list: artwork, title, publisher, episode count,
 * a favorite toggle and an overflow menu with Share (shown only when [onShare] is not null).
 */
@Composable
fun PodcastListItem(
    podcast: Podcast,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShare: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PodcastImage(
            url = podcast.thumbnailUrl,
            modifier = Modifier.sharedPodcastArtwork(podcast.id).size(72.dp),
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = podcast.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (podcast.publisher.isNotBlank()) {
                Text(
                    text = podcast.publisher,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (podcast.totalEpisodes > 0) {
                Text(
                    text = englishPluralStringResource(Res.plurals.episodes_count, podcast.totalEpisodes, podcast.totalEpisodes),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        FavoriteButton(isFavorite = isFavorite, onClick = onToggleFavorite)
        if (onShare != null) {
            MoreMenu(onShare = onShare)
        }
    }
}

@Composable
private fun FavoriteButton(isFavorite: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = stringResource(
                if (isFavorite) Res.string.action_remove_from_favorites else Res.string.action_add_to_favorites,
            ),
            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MoreMenu(onShare: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Filled.MoreVert, contentDescription = stringResource(Res.string.action_more))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.action_share)) },
                leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null) },
                onClick = {
                    expanded = false
                    onShare()
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PodcastListItemPreview() {
    ProPodcastTheme {
        PodcastListItem(
            podcast = Podcast(
                id = "1",
                title = "Star Wars 7x7: A Daily Bite-Sized Dose of Star Wars Joy",
                publisher = "Allen Voivod",
                imageUrl = null,
                thumbnailUrl = null,
                description = "",
                totalEpisodes = 1234,
                listennotesUrl = "https://www.listennotes.com/c/1/",
            ),
            isFavorite = true,
            onClick = {},
            onToggleFavorite = {},
            onShare = {},
        )
    }
}
