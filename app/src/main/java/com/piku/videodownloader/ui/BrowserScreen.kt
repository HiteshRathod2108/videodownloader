package com.piku.videodownloader.ui

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen() {
    // Scaffold provides the structural layout and handles system bar padding
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        AndroidView(
            // We apply the innerPadding here so it doesn't draw under the notch/status bar
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    webViewClient = com.piku.videodownloader.browser.YoutubeWebViewClient()
                    loadUrl("https://m.youtube.com")
                }
            }
        )
    }
}