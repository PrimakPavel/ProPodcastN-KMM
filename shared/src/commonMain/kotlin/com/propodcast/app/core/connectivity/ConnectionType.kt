package com.propodcast.app.core.connectivity

/** What the device is connected through right now. */
enum class ConnectionType {
    /** Wi-Fi or Ethernet: download without asking. */
    UNMETERED,

    /** Mobile data (or another metered network): ask before downloading. */
    METERED,

    NONE,
}
