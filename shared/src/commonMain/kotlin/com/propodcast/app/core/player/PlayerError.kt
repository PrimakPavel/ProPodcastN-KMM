package com.propodcast.app.core.player

/** Why playback stopped with an error. */
enum class PlayerError {
    /** No connection or a timeout while loading the audio. */
    NETWORK,

    /** The audio file is missing, broken or in an unsupported format. */
    SOURCE,
}
