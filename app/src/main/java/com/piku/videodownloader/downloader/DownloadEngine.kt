package com.piku.videodownloader.downloader

import android.content.Context
import android.util.Log
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DownloadEngine {

    suspend fun fetchVideoFormats(videoUrl: String): List<String> = withContext(Dispatchers.IO) {
        val uniqueResolutions = mutableSetOf<Int>()

        try {
            val request = YoutubeDLRequest(videoUrl)
            request.addOption("--flat-playlist")

            // THE FIX: Tell yt-dlp to ignore playlist metadata and focus ONLY on the video
            request.addOption("--no-playlist")

            val info = YoutubeDL.getInstance().getInfo(request)

            info.formats?.forEach { format ->
                if (format.height > 0 && format.ext == "mp4") {
                    uniqueResolutions.add(format.height)
                }
            }
        } catch (e: Exception) {
            Log.e("PikuFormat", "🔴 Failed to fetch formats", e)
        }

        val displayList = uniqueResolutions.sortedDescending().map { "${it}p" }.toMutableList()
        if (displayList.isNotEmpty()) {
            displayList.add("Audio Only")
        }

        return@withContext displayList
    }

    suspend fun executeDownload(videoUrl: String, selectedQuality: String, context: Context) = withContext(Dispatchers.IO) {
        try {
            val request = YoutubeDLRequest(videoUrl)

            // THE FIX: Ignore playlists during the actual download too
            request.addOption("--no-playlist")

            if (selectedQuality == "Audio Only") {
                request.addOption("-f", "bestaudio[ext=m4a]/bestaudio")
                request.addOption("--extract-audio")
                request.addOption("--audio-format", "m4a")
            } else {
                val height = selectedQuality.replace("p", "").toIntOrNull() ?: 720
                val formatString = "bestvideo[height<=$height][ext=mp4]+bestaudio[ext=m4a]/best[height<=$height][ext=mp4]/best"
                request.addOption("-f", formatString)
            }

            val cachePath = context.cacheDir.absolutePath
            request.addOption("-o", "$cachePath/%(title)s.%(ext)s")

            Log.d("PikuDownload", "⏳ Starting download to: $cachePath")

            YoutubeDL.getInstance().execute(request, "PikuDownloadTask") { progress, etaInSeconds, _ ->
                Log.d("PikuDownload", "⬇️ Downloading: $progress% | ETA: $etaInSeconds sec")
            }

            Log.d("PikuDownload", "✅ Download and Muxing Complete!")

            com.piku.videodownloader.storage.MediaStoreExporter.exportLatestVideo(context)

        } catch (e: Exception) {
            Log.e("PikuDownload", "🔴 Download Failed", e)
        }
    }
}