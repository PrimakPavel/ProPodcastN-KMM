package com.propodcast.app.widget

import android.content.Context
import android.content.ComponentName
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.propodcast.app.shared.R

/** Widget UI: artwork, episode and podcast title, play / pause; a tap elsewhere opens the app. */
@Composable
fun PlayerWidgetContent(state: PlayerWidgetState?, artwork: Bitmap?, strings: PlayerWidgetStrings) {
    val context = LocalContext.current
    val openApp = launcherActivity(context)
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(16.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .let { if (openApp != null) it.clickable(actionStartActivity(openApp)) else it },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (state == null) {
            Text(
                text = strings.empty,
                style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 14.sp),
                maxLines = 2,
            )
            return@Row
        }
        Image(
            provider = artwork?.let(::ImageProvider) ?: ImageProvider(R.drawable.widget_ic_podcast_placeholder),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = GlanceModifier.size(48.dp).cornerRadius(8.dp),
        )
        Spacer(GlanceModifier.width(12.dp))
        Column(GlanceModifier.defaultWeight()) {
            Text(
                text = state.episodeTitle,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
                maxLines = 1,
            )
            val subtitle = if (state.hasError) strings.sourceError else state.podcastTitle
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = TextStyle(
                        color = if (state.hasError) GlanceTheme.colors.error else GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 12.sp,
                    ),
                    maxLines = 1,
                )
            }
        }
        Spacer(GlanceModifier.width(8.dp))
        CircleIconButton(
            imageProvider = ImageProvider(if (state.isPlaying) R.drawable.widget_ic_pause else R.drawable.widget_ic_play),
            contentDescription = if (state.isPlaying) strings.pause else strings.play,
            onClick = actionRunCallback<TogglePlaybackAction>(),
            backgroundColor = GlanceTheme.colors.primary,
            contentColor = GlanceTheme.colors.onPrimary,
        )
    }
}

/** The app's launcher activity (it lives in androidApp, which this module does not see); none in unit tests. */
private fun launcherActivity(context: Context): ComponentName? =
    context.packageManager.getLaunchIntentForPackage(context.packageName)?.component
