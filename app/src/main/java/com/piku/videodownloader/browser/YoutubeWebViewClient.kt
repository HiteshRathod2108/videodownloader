package com.piku.videodownloader.browser

import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient

// We added a "callback" so the client can tell the UI when a video is found
class YoutubeWebViewClient(private val onVideoDetected: (String) -> Unit) : WebViewClient() {

    private val videoUrlPattern = Regex(".*youtube\\.com/watch\\?v=.*")

    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)

        if (url != null && videoUrlPattern.matches(url)) {
            Log.d("PikuBrowser", "🟢 VIDEO DETECTED: $url")

            // Trigger the callback with the URL!
            onVideoDetected(url)
        }
    }
}