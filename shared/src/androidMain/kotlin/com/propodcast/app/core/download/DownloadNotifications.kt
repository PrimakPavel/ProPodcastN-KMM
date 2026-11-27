package com.propodcast.app.core.download

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import com.propodcast.app.resources.Res
import com.propodcast.app.resources.download_cancel
import com.propodcast.app.resources.download_channel_name
import com.propodcast.app.resources.download_notification_title
import com.propodcast.app.shared.R
import org.jetbrains.compose.resources.getString

/** The progress notification of the foreground download work, with a Cancel action. */
class DownloadNotifications(
    private val context: Context,
) {
    suspend fun foregroundInfo(episodeId: String, title: String, progress: Float?, cancelIntent: PendingIntent): ForegroundInfo {
        ensureChannel()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_download_notification)
            .setContentTitle(getString(Res.string.download_notification_title, title))
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setSilent(true)
            .setProgress(PROGRESS_MAX, ((progress ?: 0f) * PROGRESS_MAX).toInt(), progress == null)
            .addAction(0, getString(Res.string.download_cancel), cancelIntent)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
        return foregroundInfo(notificationId(episodeId), notification)
    }

    private fun foregroundInfo(id: Int, notification: Notification): ForegroundInfo =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(id, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(id, notification)
        }

    private suspend fun ensureChannel() {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                getString(Res.string.download_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            ),
        )
    }

    /** One notification per episode, so parallel downloads don't overwrite each other. */
    private fun notificationId(episodeId: String) = NOTIFICATION_ID_BASE + (episodeId.hashCode() and 0xFFFF)

    private companion object {
        const val CHANNEL_ID = "downloads"
        const val NOTIFICATION_ID_BASE = 1_000
        const val PROGRESS_MAX = 100
    }
}
