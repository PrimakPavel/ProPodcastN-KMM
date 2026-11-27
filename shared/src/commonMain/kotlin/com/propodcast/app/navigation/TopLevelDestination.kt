package com.propodcast.app.navigation

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.favorites_title
import com.propodcast.app.resources.nav_best
import com.propodcast.app.resources.search_title
import org.jetbrains.compose.resources.StringResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.reflect.KClass

/** Destinations shown in the bottom bar / navigation rail. */
enum class TopLevelDestination(
    val route: Any,
    val routeClass: KClass<*>,
    val icon: ImageVector,
    val labelRes: StringResource,
) {
    HOME(HomeRoute, HomeRoute::class, Icons.Filled.Home, Res.string.nav_best),
    FAVORITES(FavoritesRoute(), FavoritesRoute::class, Icons.Filled.Favorite, Res.string.favorites_title),
    SEARCH(SearchRoute, SearchRoute::class, Icons.Filled.Search, Res.string.search_title),
}
