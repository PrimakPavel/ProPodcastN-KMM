package com.propodcast.app.core.commonwidgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.ui.sharedPodcastArtwork

/** Compact podcast tile for horizontal rows (e.g. recommendations). */
@Composable
fun PodcastCard(
    podcast: Podcast,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(128.dp)
            .clickable(onClick = onClick)
            .padding(4.dp),
    ) {
        PodcastImage(
            url = podcast.thumbnailUrl,
            modifier = Modifier
                .sharedPodcastArtwork(podcast.id)
                .fillMaxWidth()
                .aspectRatio(1f),
        )
        Text(
            text = podcast.title,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp),
        )
        if (podcast.publisher.isNotBlank()) {
            Text(
                text = podcast.publisher,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
