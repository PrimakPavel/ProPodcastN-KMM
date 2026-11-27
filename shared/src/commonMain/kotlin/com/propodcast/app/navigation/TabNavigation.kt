package com.propodcast.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

/**
 * How back works across tabs. Android: back from any tab returns to the start tab (Best podcasts), as the system
 * back button does in Android apps. iOS: tabs are separate stacks, as in UITabBarController; a tab's root has
 * nothing to go back to, so the edge swipe does nothing there.
 */
internal expect val tabsReturnToStartOnBack: Boolean

/**
 * Opens a tab on [route]. Each tab keeps its own state (list scroll, search query, screens opened in it): it is
 * saved when the user leaves the tab and restored when [restoreState] and the user comes back.
 */
internal fun NavHostController.navigateToTab(route: Any, restoreState: Boolean = true) {
    navigate(route) {
        if (tabsReturnToStartOnBack) {
            popUpTo(graph.findStartDestination().id) { saveState = true }
        } else {
            // The whole current tab (its root included) leaves the back stack, so the new tab becomes the root.
            val currentTabRoot = currentBackStack.value.firstOrNull { it.destination !is NavGraph }?.destination?.id
            if (currentTabRoot != null) {
                popUpTo(currentTabRoot) {
                    inclusive = true
                    saveState = true
                }
            }
        }
        launchSingleTop = true
        this.restoreState = restoreState
    }
}

/**
 * iOS: at a tab's root the edge swipe is taken and does nothing, as in UIKit. Without it the swipe reached the
 * content and opened (or swiped away) the list row under the finger. Call before the NavHost and the player,
 * whose back handlers then come first.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun TabRootBackHandler(currentEntry: NavBackStackEntry?, hasPreviousEntry: Boolean) {
    BackHandler(enabled = !tabsReturnToStartOnBack && currentEntry != null && !hasPreviousEntry) {}
}
