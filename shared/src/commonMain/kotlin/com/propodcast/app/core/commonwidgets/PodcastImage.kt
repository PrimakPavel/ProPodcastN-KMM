package com.propodcast.app.core.commonwidgets

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.ic_podcast_placeholder
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

/**
 * Square podcast / episode artwork. Until it loads (or when there is none) a light microphone at half size
 * sits on a translucent background; the artwork, once loaded, covers it. Decorative.
 */
@Composable
fun PodcastImage(
    url: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = PLACEHOLDER_BACKGROUND_ALPHA)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_podcast_placeholder),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = PLACEHOLDER_ICON_ALPHA),
            modifier = Modifier.fillMaxSize(PLACEHOLDER_ICON_FRACTION),
        )
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

private const val PLACEHOLDER_BACKGROUND_ALPHA = 0.5f
private const val PLACEHOLDER_ICON_ALPHA = 0.38f
private const val PLACEHOLDER_ICON_FRACTION = 0.5f
