package com.piku.videodownloader.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the yt-dlp engine in a background thread so it doesn't freeze the app
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                YoutubeDL.getInstance().init(applicationContext)
                val version = YoutubeDL.getInstance().version(applicationContext)
                Log.d("PikuEngine", "🟢 YT-DLP ENGINE READY! Version: $version")
            } catch (e: Exception) {
                Log.e("PikuEngine", "🔴 ENGINE FAILED TO INITIALIZE", e)
            }
        }

        setContent {
            BrowserScreen()
        }
    }
}