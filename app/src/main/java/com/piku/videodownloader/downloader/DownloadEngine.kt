package com.piku.videodownloader.downloader

import android.util.Log
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DownloadEngine {

    // Now returns a List of Strings (e.g., ["1080p", "720p", "Audio Only"])
    suspend fun fetchVideoFormats(videoUrl: String): List<String> = withContext(Dispatchers.IO) {
        val uniqueResolutions = mutableSetOf<Int>()

        try {
            val request = YoutubeDLRequest(videoUrl)
            request.addOption("--flat-playlist")

            val info = YoutubeDL.getInstance().getInfo(request)

            info.formats?.forEach { format ->
                // Only grab MP4s with actual video data
                if (format.height > 0 && format.ext == "mp4") {
                    uniqueResolutions.add(format.height)
                }
            }
        } catch (e: Exception) {
            Log.e("PikuFormat", "🔴 Failed to fetch formats", e)
        }

        // Sort from highest quality to lowest, add "p", and add an Audio option
        val displayList = uniqueResolutions.sortedDescending().map { "${it}p" }.toMutableList()
        if (displayList.isNotEmpty()) {
            displayList.add("Audio Only")
        }

        return@withContext displayList
    }

    suspend fun executeDownload(videoUrl: String, selectedQuality: String, context: android.content.Context) = withContext(Dispatchers.IO) {
        try {
            val request = YoutubeDLRequest(videoUrl)

            // 1. Setup the complex format string based on the user's simple selection
            if (selectedQuality == "Audio Only") {
                request.addOption("-f", "bestaudio[ext=m4a]/bestaudio")
                request.addOption("--extract-audio")
                request.addOption("--audio-format", "m4a")
            } else {
                // Convert "720p" to the number 720
                val height = selectedQuality.replace("p", "").toIntOrNull() ?: 720

                // This exact format string forces MP4 and pairs it with the best M4A audio
                val formatString = "bestvideo[height<=$height][ext=mp4]+bestaudio[ext=m4a]/best[height<=$height][ext=mp4]/best"
                request.addOption("-f", formatString)
            }

            // 2. Set the output location to the app's hidden cache directory
            val cachePath = context.cacheDir.absolutePath
            request.addOption("-o", "$cachePath/%(title)s.%(ext)s")

            Log.d("PikuDownload", "⏳ Starting download to: $cachePath")

            // 3. Execute the download and listen to the progress!
            YoutubeDL.getInstance().execute(request, "PikuDownloadTask") { progress, etaInSeconds, _ ->
                Log.d("PikuDownload", "⬇️ Downloading: $progress% | ETA: $etaInSeconds sec")
            }

            Log.d("PikuDownload", "✅ Download and Muxing Complete!")

        } catch (e: Exception) {
            Log.e("PikuDownload", "🔴 Download Failed", e)
        }
    }
}