package com.propodcast.app.core.ui

import androidx.compose.runtime.Composable

/** Shares a link: `share(title, url, chooserTitle)`. */
fun interface LinkSharer {
    fun share(title: String, url: String, chooserTitle: String)
}

/**
 * The system share sheet with [title] and the url as plain text: Android's chooser, iOS's activity sheet.
 * `chooserTitle` is the chooser's heading on Android; iOS shows none.
 */
@Composable
expect fun rememberLinkSharer(): LinkSharer
