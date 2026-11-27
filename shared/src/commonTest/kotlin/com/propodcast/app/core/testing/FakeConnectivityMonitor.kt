package com.propodcast.app.core.testing

import com.propodcast.app.core.connectivity.ConnectionType
import com.propodcast.app.core.connectivity.ConnectivityMonitor
import kotlinx.coroutines.flow.MutableStateFlow

class FakeConnectivityMonitor(initial: ConnectionType = ConnectionType.UNMETERED) : ConnectivityMonitor {
    override val connection = MutableStateFlow(initial)
    override fun currentConnection(): ConnectionType = connection.value
}
