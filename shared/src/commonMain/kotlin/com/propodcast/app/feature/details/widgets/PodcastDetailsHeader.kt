package com.propodcast.app.feature.details.widgets

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.episodes_count
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.propodcast.app.core.ui.englishPluralStringResource
import androidx.compose.ui.unit.dp
import com.propodcast.app.core.commonwidgets.ExpandableHtmlText
import com.propodcast.app.core.commonwidgets.PodcastImage
import com.propodcast.app.core.model.Podcast

/**
 * Artwork, title, publisher, episode count and the (HTML) description. The artwork is the list thumbnail
 * (300 px, enough for 120 dp), so it is already cached when the details open; [artworkModifier] makes it
 * the end of the artwork flight from the list.
 */
@Composable
fun PodcastDetailsHeader(
    podcast: Podcast,
    modifier: Modifier = Modifier,
    artworkModifier: Modifier = Modifier,
) {
    Column(modifier.padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PodcastImage(url = podcast.thumbnailUrl ?: podcast.imageUrl, modifier = artworkModifier.size(120.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(podcast.title, style = MaterialTheme.typography.titleLarge)
                if (podcast.publisher.isNotBlank()) {
                    Text(
                        text = podcast.publisher,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (podcast.totalEpisodes > 0) {
                    Text(
                        text = englishPluralStringResource(
                            Res.plurals.episodes_count,
                            podcast.totalEpisodes,
                            podcast.totalEpisodes,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (podcast.description.isNotBlank()) {
            Spacer(Modifier.size(12.dp))
            ExpandableHtmlText(html = podcast.description)
        }
    }
}
