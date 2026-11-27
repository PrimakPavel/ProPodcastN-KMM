package com.propodcast.app.core.connectivity

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import platform.Network.nw_path_get_status
import platform.Network.nw_path_is_constrained
import platform.Network.nw_path_is_expensive
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.Network.nw_path_t
import platform.darwin.dispatch_get_main_queue

/**
 * [ConnectivityMonitor] on Network.framework's `NWPathMonitor`. One monitor for the app's lifetime; its updates
 * arrive on the main queue. Cellular and Low Data Mode count as metered (iOS calls them expensive / constrained),
 * so downloads ask first there, like on Android's mobile data.
 *
 * Unlike Android's ConnectivityManager, the monitor has no current state until its first (asynchronous) update.
 * Until then nothing is emitted: starting from NONE showed "No connection" and then "Back online" at app start.
 */
class IosConnectivityMonitor : ConnectivityMonitor {

    /** Null until the monitor's first update. */
    private val state = MutableStateFlow<ConnectionType?>(null)

    private val monitor = nw_path_monitor_create().also { monitor ->
        nw_path_monitor_set_update_handler(monitor) { path -> state.value = path.toConnectionType() }
        nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
        nw_path_monitor_start(monitor)
    }

    /** Before the first update the type is unknown: metered, so a download asks instead of using mobile data. */
    override fun currentConnection(): ConnectionType = state.value ?: ConnectionType.METERED

    override val connection: Flow<ConnectionType> = state.filterNotNull()

    private fun nw_path_t.toConnectionType(): ConnectionType = when {
        nw_path_get_status(this) != nw_path_status_satisfied -> ConnectionType.NONE
        nw_path_is_expensive(this) || nw_path_is_constrained(this) -> ConnectionType.METERED
        else -> ConnectionType.UNMETERED
    }
}
