package com.propodcast.app.core.commonwidgets

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.action_download
import com.propodcast.app.resources.download_cancel
import com.propodcast.app.resources.download_cellular_message
import com.propodcast.app.resources.download_cellular_message_size
import com.propodcast.app.resources.download_cellular_title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource

/** Asks before downloading over mobile data; [sizeLabel] is the estimated size ("≈ 60 MB"), if known. */
@Composable
fun DownloadOverCellularDialog(
    sizeLabel: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.download_cellular_title)) },
        text = {
            Text(
                if (sizeLabel != null) {
                    stringResource(Res.string.download_cellular_message_size, sizeLabel)
                } else {
                    stringResource(Res.string.download_cellular_message)
                },
            )
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(Res.string.action_download)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.download_cancel)) } },
    )
}
