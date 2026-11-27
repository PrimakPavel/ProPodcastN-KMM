package com.propodcast.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Temporary stand-in for screens that move in UI part 6.3 (Favorites, Settings). */
@Composable
internal fun PendingScreen(name: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text("$name moves in UI part 6.3", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
    }
}
