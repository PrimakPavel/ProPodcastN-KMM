package com.propodcast.app.feature.downloads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.propodcast.app.core.commonwidgets.DownloadOverCellularDialog
import com.propodcast.app.core.ui.estimatedDownloadSizeLabel

/** "Not on Wi-Fi" dialog with the estimated size, while [DownloadsViewModel] waits for the answer. */
@Composable
fun CellularDownloadPrompt(viewModel: DownloadsViewModel) {
    val episode by viewModel.confirmation.collectAsStateWithLifecycle()
    episode?.let {
        DownloadOverCellularDialog(
            sizeLabel = estimatedDownloadSizeLabel(it.durationSec),
            onConfirm = viewModel::confirmDownload,
            onDismiss = viewModel::dismissConfirmation,
        )
    }
}
