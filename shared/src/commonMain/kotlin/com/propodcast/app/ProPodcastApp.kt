package com.propodcast.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.window.core.layout.WindowSizeClass
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.propodcast.app.core.model.toEpisode
import com.propodcast.app.core.ui.LocalSharedTransitionScope
import com.propodcast.app.core.ui.SnackbarEffect
import com.propodcast.app.feature.downloads.DownloadsViewModel
import com.propodcast.app.feature.downloads.CellularDownloadPrompt
import com.propodcast.app.feature.downloads.rememberEpisodeDownloadActions
import com.propodcast.app.feature.network.NetworkBannerViewModel
import com.propodcast.app.feature.network.NetworkStatusBar
import com.propodcast.app.feature.player.FullPlayerOverlay
import com.propodcast.app.feature.player.MiniPlayer
import com.propodcast.app.feature.player.PlayerViewModel
import com.propodcast.app.navigation.AppNavHost
import com.propodcast.app.navigation.TopLevelDestination

/** Root composable: adaptive navigation (bottom bar on phones, rail on wider screens), nav host and player. */
@Composable
fun ProPodcastApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    playerViewModel: PlayerViewModel = koinViewModel(),
    downloadsViewModel: DownloadsViewModel = koinViewModel(),
    networkBannerViewModel: NetworkBannerViewModel = koinViewModel(),
    /** Called with the screen's name (route class) on every navigation, for analytics. */
    onScreenView: (String) -> Unit = {},
) {
    val networkBanner by networkBannerViewModel.banner.collectAsStateWithLifecycle()
    val player by playerViewModel.uiState.collectAsStateWithLifecycle()
    val downloads = rememberEpisodeDownloadActions(downloadsViewModel)
    val playerSnackbar = remember { SnackbarHostState() }
    SnackbarEffect(downloadsViewModel.snackbarEvents, playerSnackbar)
    CellularDownloadPrompt(downloadsViewModel)
    var showFullPlayer by rememberSaveable { mutableStateOf(false) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Keeps the tab highlighted while a nested screen (e.g. podcast details) is open.
    var selectedTab by rememberSaveable { mutableStateOf(TopLevelDestination.HOME) }
    backStackEntry?.destination?.topLevelDestination()?.let { selectedTab = it }
    val screenName = backStackEntry?.destination?.route?.substringBefore('?')?.substringBefore('/')?.substringAfterLast('.')
    LaunchedEffect(backStackEntry?.id) { screenName?.let(onScreenView) }

    // One layout for the app and the full player, so the artwork can fly between mini and full player.
    SharedTransitionLayout(modifier) {
    // A phone in landscape is short: a navigation rail instead of a bottom bar leaves the height to the content.
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val tallEnough = adaptiveInfo.windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)
    val navigationType = if (tallEnough) {
        NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)
    } else {
        NavigationSuiteType.NavigationRail
    }
    NavigationSuiteScaffold(
        layoutType = navigationType,
        navigationSuiteItems = {
            TopLevelDestination.entries.forEach { destination ->
                item(
                    selected = destination == selectedTab,
                    onClick = { navController.navigateToTopLevel(destination) },
                    icon = { Icon(destination.icon, contentDescription = null) },
                    label = { Text(stringResource(destination.labelRes)) },
                )
            }
        },
    ) {
        // The bottom system inset (gesture bar) is applied once here, below the mini player and network
        // strip: with a navigation rail nothing else takes it, so they would sit under the gesture bar
        // while the screens above added a gap of their own. With a bottom bar the bar already took it (0 here).
        Column(Modifier.windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))) {
            CompositionLocalProvider(LocalSharedTransitionScope provides this@SharedTransitionLayout) {
                AppNavHost(
                    navController = navController,
                    onPlayEpisode = playerViewModel::play,
                    modifier = Modifier.weight(1f),
                )
            }
            AnimatedVisibility(visible = player.isVisible, enter = expandVertically(), exit = shrinkVertically()) {
                MiniPlayer(
                    state = player,
                    onOpen = { showFullPlayer = true },
                    onTogglePlayPause = playerViewModel::togglePlayPause,
                    onClose = playerViewModel::stop,
                    expanded = showFullPlayer,
                    sharedTransitionScope = this@SharedTransitionLayout,
                )
            }
            NetworkStatusBar(networkBanner)
        }
    }

    val playingEpisode = player.playback.nowPlaying?.toEpisode()
    FullPlayerOverlay(
        visible = showFullPlayer && player.isVisible,
        state = player,
        sharedTransitionScope = this@SharedTransitionLayout,
        onCollapse = { showFullPlayer = false },
        onTogglePlayPause = playerViewModel::togglePlayPause,
        onSeekTo = playerViewModel::seekTo,
        onSeekBack = playerViewModel::seekBack,
        onSeekForward = playerViewModel::seekForward,
        onSpeedSelected = playerViewModel::setSpeed,
        downloadState = playingEpisode?.let { downloads.stateOf(it) },
        onDownloadClick = playingEpisode?.let { episode -> { downloads.onPrimaryAction(episode) } },
        snackbarHostState = playerSnackbar,
    )
    }
}

private fun NavDestination.topLevelDestination(): TopLevelDestination? =
    TopLevelDestination.entries.firstOrNull { destination -> hierarchy.any { it.hasRoute(destination.routeClass) } }

/**
 * Opens a tab on its main screen. Each tab keeps its own state (list scroll, search query…), but anything
 * opened on top of it (e.g. podcast details) is dropped, also when the tab is tapped again while deep inside.
 */
private fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
    popBackStack(destination.routeClass, inclusive = false)
}
