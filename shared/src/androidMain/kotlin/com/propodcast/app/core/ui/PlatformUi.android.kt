package com.propodcast.app.core.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

actual fun formatMediumDate(epochMs: Long): String =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(epochMs))

@Composable
actual fun fileSizeLabel(bytes: Long): String = Formatter.formatShortFileSize(LocalContext.current, bytes)

@Composable
actual fun rememberLinkSharer(): LinkSharer {
    val context = LocalContext.current
    return remember(context) {
        LinkSharer { title, url, chooserTitle ->
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, url)
            }
            context.startActivity(Intent.createChooser(send, chooserTitle))
        }
    }
}

@Composable
actual fun rememberNotificationPermissionRequester(): (action: () -> Unit) -> Unit {
    val context = LocalContext.current
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var asked by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        pendingAction?.invoke()
        pendingAction = null
    }
    return remember(context, launcher) {
        { action ->
            val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            if (needsPermission && !asked) {
                asked = true
                pendingAction = action
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                action()
            }
        }
    }
}
