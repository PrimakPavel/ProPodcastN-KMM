package com.propodcast.app.core.connectivity

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** App-wide view of the network: Wi-Fi / mobile data / none. */
interface ConnectivityMonitor {
    /** The connection right now. */
    fun currentConnection(): ConnectionType

    /** The connection, updated as it changes. */
    val connection: Flow<ConnectionType>
}

/** Temporary [ConnectivityMonitor] until the platform monitors (stage 7): always on Wi-Fi. */
class StubConnectivityMonitor : ConnectivityMonitor {
    override fun currentConnection(): ConnectionType = ConnectionType.UNMETERED
    override val connection: Flow<ConnectionType> = MutableStateFlow(ConnectionType.UNMETERED)
}
