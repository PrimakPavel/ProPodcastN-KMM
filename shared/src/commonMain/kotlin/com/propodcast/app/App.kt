package com.propodcast.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.propodcast.app.core.common.apiCall
import com.propodcast.app.core.network.ApiConfig
import com.propodcast.app.core.network.ListenNotesApi
import com.propodcast.app.core.network.createListenNotesHttpClient
import com.propodcast.app.resources.Res
import com.propodcast.app.resources.app_name
import com.propodcast.app.resources.greeting
import org.jetbrains.compose.resources.stringResource

@Composable
@Preview
fun App() {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
        Surface(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.safeDrawingPadding().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(Res.string.app_name), style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(Res.string.greeting, platformName()), style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(24.dp))
                NetworkCheck()
            }
        }
    }
}

/** Temporary check of the network layer (stage 2): first best podcasts from the configured API. */
@Composable
private fun NetworkCheck() {
    var text by remember { mutableStateOf("Loading best podcasts…") }
    LaunchedEffect(Unit) {
        val client = createListenNotesHttpClient(ApiConfig.fromBuildKonfig(), logging = BuildKonfig.HTTP_LOGGING)
        text = apiCall { ListenNotesApi(client).bestPodcasts() }.fold(
            onSuccess = { response ->
                "Best podcasts (${response.total}):\n" + response.podcasts.take(5).joinToString("\n") { it.title }
            },
            onFailure = { "Error: ${it.message}" },
        )
        client.close()
    }
    Text(text, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
}
