package com.piku.videodownloader.background

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.piku.videodownloader.downloader.DownloadEngine

class DownloadWorker(private val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Create a unique ID for each download so multiple progress bars don't overlap
    private val notificationId = inputData.getString("url")?.hashCode() ?: System.currentTimeMillis().toInt()
    private val channelId = "piku_downloads"

    override suspend fun doWork(): Result {
        val url = inputData.getString("url") ?: return Result.failure()
        val quality = inputData.getString("quality") ?: return Result.failure()

        createNotificationChannel()

        // 1. Start the Foreground Service to prevent Android from killing the app
        val initialNotification = createNotification("Starting download...", 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            setForeground(ForegroundInfo(notificationId, initialNotification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC))
        } else {
            setForeground(ForegroundInfo(notificationId, initialNotification))
        }

        return try {
            // 2. Execute the download and update the notification as it progresses
            DownloadEngine.executeDownload(url, quality, context) { progress, eta ->
                val progressNotification = createNotification(
                    "Downloading: ${progress.toInt()}% (ETA: $eta sec)",
                    progress.toInt()
                )
                notificationManager.notify(notificationId, progressNotification)
            }

            // 3. Mark as success and show final notification
            val successNotification = NotificationCompat.Builder(context, channelId)
                .setContentTitle("Piku Download Complete")
                .setContentText("Video saved to Downloads folder")
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .build()
            notificationManager.notify(notificationId, successNotification)

            Result.success()

        } catch (e: Exception) {
            val failedNotification = NotificationCompat.Builder(context, channelId)
                .setContentTitle("Piku Download Failed")
                .setContentText("Something went wrong.")
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .build()
            notificationManager.notify(notificationId, failedNotification)
            Result.failure()
        }
    }

    private fun createNotification(message: String, progress: Int): android.app.Notification {
        return NotificationCompat.Builder(context, channelId)
            .setContentTitle("Downloading Video")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            // Add the actual progress bar
            .setProgress(100, progress, progress == 0)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Video Downloads",
                NotificationManager.IMPORTANCE_LOW // LOW importance means it won't constantly beep
            )
            notificationManager.createNotificationChannel(channel)
        }
    }
}