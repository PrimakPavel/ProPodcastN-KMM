package com.propodcast.app.core.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged

/** [ConnectivityMonitor] on ConnectivityManager's default network. */
class AndroidConnectivityMonitor(
    private val context: Context,
) : ConnectivityMonitor {

    private val manager: ConnectivityManager?
        get() = context.getSystemService(ConnectivityManager::class.java)

    override fun currentConnection(): ConnectionType {
        val manager = manager ?: return ConnectionType.NONE
        return manager.getNetworkCapabilities(manager.activeNetwork).toConnectionType()
    }

    override val connection: Flow<ConnectionType> = callbackFlow {
        val manager = manager
        if (manager == null) {
            trySend(ConnectionType.NONE)
            close()
            return@callbackFlow
        }
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                trySend(capabilities.toConnectionType())
            }

            override fun onLost(network: Network) {
                trySend(currentConnection())
            }
        }
        trySend(currentConnection())
        manager.registerDefaultNetworkCallback(callback)
        awaitClose { manager.unregisterNetworkCallback(callback) }
    }.conflate().distinctUntilChanged()

    private fun NetworkCapabilities?.toConnectionType(): ConnectionType {
        if (this == null || !hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) return ConnectionType.NONE
        val wifiOrEthernet = hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        return if (wifiOrEthernet) ConnectionType.UNMETERED else ConnectionType.METERED
    }
}
