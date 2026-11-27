package com.propodcast.app.feature.network

/** What the network strip at the bottom of the app shows. */
enum class NetworkBanner {
    HIDDEN,

    /** Red "No connection" while there is no network. */
    OFFLINE,

    /** Green "Back online" for a few seconds after the network returns. */
    BACK_ONLINE,
}
