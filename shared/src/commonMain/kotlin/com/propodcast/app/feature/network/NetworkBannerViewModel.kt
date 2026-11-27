package com.propodcast.app.feature.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.propodcast.app.core.connectivity.ConnectionType
import com.propodcast.app.core.connectivity.ConnectivityMonitor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest

/**
 * Network strip state, like YouTube's: red while offline, green for [BACK_ONLINE_MS] once the network is
 * back, hidden otherwise (also when the app starts online).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NetworkBannerViewModel(
    connectivity: ConnectivityMonitor,
) : ViewModel() {

    /**
     * "Back online" only follows a real outage, not the first "connected" at start.
     * Declared before [banner], which starts collecting right away.
     */
    private var wasOffline = false

    val banner: StateFlow<NetworkBanner> = connectivity.connection
        .map { it != ConnectionType.NONE }
        .distinctUntilChanged()
        .transformLatest { online ->
            when {
                !online -> {
                    wasOffline = true
                    emit(NetworkBanner.OFFLINE)
                }
                wasOffline -> {
                    emit(NetworkBanner.BACK_ONLINE)
                    delay(BACK_ONLINE_MS)
                    emit(NetworkBanner.HIDDEN)
                }
                else -> emit(NetworkBanner.HIDDEN)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, NetworkBanner.HIDDEN)

    companion object {
        const val BACK_ONLINE_MS = 3_000L
    }
}
