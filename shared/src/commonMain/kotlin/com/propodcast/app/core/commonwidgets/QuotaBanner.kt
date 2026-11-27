package com.propodcast.app.core.commonwidgets

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.quota_exhausted
import com.propodcast.app.resources.quota_resets_on
import com.propodcast.app.resources.quota_running_low
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.propodcast.app.core.designsystem.theme.ProPodcastTheme
import com.propodcast.app.core.model.ApiQuota
import com.propodcast.app.core.ui.formatMediumDate
import kotlin.time.Clock

/** Warning about the Listen Notes quota; shows nothing unless the quota is running low. */
@Composable
fun QuotaBanner(
    quota: ApiQuota?,
    modifier: Modifier = Modifier,
) {
    if (quota == null || !quota.isRunningLow()) return
    val text = stringResource(
        if (quota.isExhausted) Res.string.quota_exhausted else Res.string.quota_running_low,
        quota.used,
        quota.freeQuota,
    )
    val resets = quota.nextBillingDate?.let { stringResource(Res.string.quota_resets_on, it.toEpochMilliseconds().let(::formatMediumDate)) }
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.medium,
    ) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.errorContainer),
            leadingContent = { Icon(Icons.Filled.Warning, contentDescription = null) },
            headlineContent = {
                Column {
                    Text(text)
                    if (resets != null) Text(resets, style = MaterialTheme.typography.bodySmall)
                }
            },
        )
    }
}


@Preview
@Composable
private fun QuotaBannerPreview() {
    ProPodcastTheme {
        QuotaBanner(ApiQuota(used = 2100, freeQuota = 2500, nextBillingDate = Clock.System.now(), plan = "FREE"))
    }
}
