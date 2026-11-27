package com.propodcast.app.core.commonwidgets

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.launch

/** Returns a callback that slides [sheetState] down and then calls [onDismiss], like a swipe down does. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberSheetCloser(sheetState: SheetState, onDismiss: () -> Unit): () -> Unit {
    val scope = rememberCoroutineScope()
    val currentOnDismiss = rememberUpdatedState(onDismiss)
    return {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) currentOnDismiss.value()
        }
    }
}
