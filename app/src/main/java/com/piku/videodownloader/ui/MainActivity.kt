package com.piku.videodownloader.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Global Crash Handler
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            com.piku.videodownloader.storage.PikuLogger.logError(
                applicationContext,
                "FATAL_CRASH",
                "Uncaught application crash occurred",
                throwable
            )
        }

        // Initialize the yt-dlp engine in a background thread so it doesn't freeze the app
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 1. Init Python yt-dlp
                YoutubeDL.getInstance().init(applicationContext)

                // 2. Init FFmpeg for merging audio and video
                FFmpeg.getInstance().init(applicationContext)

                // 3. NEW: Force an update to bypass YouTube's 403 Forbidden error!
                Log.d("PikuEngine", "🔄 Downloading latest yt-dlp bypass signatures...")
                YoutubeDL.getInstance().updateYoutubeDL(applicationContext, YoutubeDL.UpdateChannel.STABLE)

                val version = YoutubeDL.getInstance().version(applicationContext)
                Log.d("PikuEngine", "🟢 YT-DLP & FFMPEG READY! Version: $version")
            } catch (e: Exception) {
                Log.e("PikuEngine", "🔴 ENGINE FAILED TO INITIALIZE", e)
            }
        }

        setContent {
            BrowserScreen()
        }
    }
}