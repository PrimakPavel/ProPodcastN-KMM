package com.propodcast.app

import androidx.compose.ui.window.ComposeUIViewController

/** Entry point for iOS: the Swift side hosts this view controller. Koin is started by the Swift app first. */
@Suppress("FunctionName", "unused")
fun MainViewController() = ComposeUIViewController { App() }
