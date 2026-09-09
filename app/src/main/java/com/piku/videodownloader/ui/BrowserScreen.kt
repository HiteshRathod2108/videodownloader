package com.piku.videodownloader.ui

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.piku.videodownloader.browser.YoutubeWebViewClient
import com.piku.videodownloader.downloader.DownloadEngine
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen() {
    // This allows us to launch background tasks from the UI screen safely
    val coroutineScope = rememberCoroutineScope()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true

                    // We pass a block of code to run whenever a video URL is detected
                    webViewClient = YoutubeWebViewClient { videoUrl ->
                        coroutineScope.launch {
                            // Fetch the formats in the background!
                            DownloadEngine.fetchVideoFormats(videoUrl)
                        }
                    }

                    loadUrl("https://m.youtube.com")
                }
            }
        )
    }
}