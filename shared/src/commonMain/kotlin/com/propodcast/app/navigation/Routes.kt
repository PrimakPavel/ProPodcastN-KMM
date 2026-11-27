package com.propodcast.app.navigation

import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

/** Favorites; [initialTab] (a FavoritesTab name) opens a given tab, e.g. Downloaded from the home screen. */
@Serializable
data class FavoritesRoute(val initialTab: String? = null)

@Serializable
data object SearchRoute

@Serializable
data object SettingsRoute
