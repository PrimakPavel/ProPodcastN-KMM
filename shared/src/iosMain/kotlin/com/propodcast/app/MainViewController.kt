package com.propodcast.app

import androidx.compose.ui.window.ComposeUIViewController
import com.propodcast.app.core.data.appStorage

/** Entry point for iOS: the Swift side hosts this view controller. */
@Suppress("FunctionName", "unused")
fun MainViewController() = ComposeUIViewController { App(appStorage) }
