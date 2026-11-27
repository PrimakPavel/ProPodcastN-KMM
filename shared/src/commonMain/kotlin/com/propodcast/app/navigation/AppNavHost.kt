package com.propodcast.app.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.ui.LocalArtworkOrigin
import com.propodcast.app.core.ui.LocalNavAnimatedVisibilityScope
import com.propodcast.app.feature.details.DetailsPreview
import com.propodcast.app.feature.details.PodcastDetailsScreen
import com.propodcast.app.feature.favorites.FavoritesScreen
import com.propodcast.app.feature.favorites.FavoritesTab
import com.propodcast.app.feature.home.HomeScreen
import com.propodcast.app.feature.search.SearchScreen
import com.propodcast.app.feature.settings.SettingsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    /** Starts playing an episode; false when it has no audio. */
    onPlayEpisode: (Episode) -> Boolean,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
    ) {
        composable<HomeRoute> {
            Destination(this, origin = "home") {
                HomeScreen(
                    onPodcastClick = { navController.navigate(it.detailsRoute("home")) },
                    onOpenSettings = { navController.navigate(SettingsRoute) },
                    onOpenDownloads = {
                        // Like the bottom bar's Favorites, but straight to the Downloaded tab.
                        navController.navigateToTab(
                            FavoritesRoute(initialTab = FavoritesTab.DOWNLOADS.name),
                            restoreState = false,
                        )
                    },
                )
            }
        }
        composable<FavoritesRoute> { entry ->
            val initialTab = entry.toRoute<FavoritesRoute>().initialTab
                ?.let { name -> FavoritesTab.entries.firstOrNull { it.name == name } }
                ?: FavoritesTab.PODCASTS
            Destination(this, origin = "favorites") {
                FavoritesScreen(
                    initialTab = initialTab,
                    onPodcastClick = { navController.navigate(it.detailsRoute("favorites")) },
                    onPlayEpisode = onPlayEpisode,
                )
            }
        }
        composable<SearchRoute> {
            Destination(this, origin = "search") {
                SearchScreen(onPodcastClick = { navController.navigate(it.detailsRoute("search")) })
            }
        }
        composable<SettingsRoute> {
            Destination(this, origin = "settings") {
                SettingsScreen(onBack = navController::navigateUp)
            }
        }
        composable<PodcastDetailsRoute> { entry ->
            val route = entry.toRoute<PodcastDetailsRoute>()
            // Recommendations get their own origin, so they fly only between this screen and their details.
            val origin = "details/${route.podcastId}"
            Destination(this, origin = origin) {
                PodcastDetailsScreen(
                    onBack = navController::navigateUp,
                    // At most two details on the back stack: the one opened from a list and the latest
                    // recommendation. From the first, a recommendation opens on top; from a recommendation,
                    // the next one replaces it. Back: latest recommendation → first details → the list.
                    onPodcastClick = {
                        navController.navigate(it.detailsRoute(origin, fromRecommendation = true)) {
                            if (route.fromRecommendation) popUpTo<PodcastDetailsRoute> { inclusive = true }
                        }
                    },
                    onPlayEpisode = onPlayEpisode,
                    preview = DetailsPreview(route.title, route.artworkUrl, route.artworkKey),
                )
            }
        }
    }
}

/** Gives the screen its transition scope and artwork origin for shared element ("hero") transitions. */
@Composable
private fun Destination(
    animatedVisibilityScope: AnimatedVisibilityScope,
    origin: String,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalNavAnimatedVisibilityScope provides animatedVisibilityScope,
        LocalArtworkOrigin provides origin,
        content = content,
    )
}
