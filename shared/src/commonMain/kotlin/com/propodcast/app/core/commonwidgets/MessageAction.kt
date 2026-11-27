package com.propodcast.app.core.commonwidgets

/** An extra button under a [MessageState], e.g. "Go to downloads". */
data class MessageAction(
    val label: String,
    val onClick: () -> Unit,
)
