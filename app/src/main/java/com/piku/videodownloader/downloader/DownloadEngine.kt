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
}