package com.propodcast.app.feature.favorites

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.favorites_tab_downloads
import com.propodcast.app.resources.favorites_tab_episodes
import com.propodcast.app.resources.favorites_tab_podcasts
import org.jetbrains.compose.resources.StringResource

enum class FavoritesTab(val labelRes: StringResource) {
    PODCASTS(Res.string.favorites_tab_podcasts),
    EPISODES(Res.string.favorites_tab_episodes),
    DOWNLOADS(Res.string.favorites_tab_downloads),
}
