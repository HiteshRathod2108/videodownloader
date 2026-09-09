package com.piku.videodownloader.downloader

import android.util.Log
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DownloadEngine {

    // We use a 'suspend' function because network requests take time
    // and we don't want to freeze the app's user interface.
    suspend fun fetchVideoFormats(videoUrl: String) = withContext(Dispatchers.IO) {
        try {
            Log.d("PikuFormat", "🔄 Fetching formats for: $videoUrl")

            // Create a request for the specific video URL
            val request = YoutubeDLRequest(videoUrl)

            // This flag makes the extraction much faster
            request.addOption("--flat-playlist")

            // Ask yt-dlp to extract the video data (this automatically parses the JSON for us!)
            val info = YoutubeDL.getInstance().getInfo(request)

            Log.d("PikuFormat", "✅ Video Title: ${info.title}")

            // Look through the available formats and print the MP4 video options
            val formats = info.formats
            if (formats != null) {
                for (format in formats) {
                    // We only care about valid MP4 video formats for this test
                    if (format.height > 0 && format.ext == "mp4") {
                        Log.d("PikuFormat", "📺 Found Format: ${format.height}p | Codec: ${format.vcodec}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("PikuFormat", "🔴 Failed to fetch formats", e)
        }
    }
}