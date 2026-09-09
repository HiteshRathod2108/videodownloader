package com.piku.videodownloader.browser

import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient

class YoutubeWebViewClient : WebViewClient() {

    // Regex to match URLs that represent an actual video page
    // Matches "m.youtube.com/watch?v=" and "youtube.com/watch?v="
    private val videoUrlPattern = Regex(".*youtube\\.com/watch\\?v=.*")

    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)

        if (url != null) {
            if (videoUrlPattern.matches(url)) {
                // We are on a video page!
                Log.d("PikuBrowser", "🟢 VIDEO DETECTED: $url")
            } else {
                // We are browsing the home page or search results
                Log.d("PikuBrowser", "⚪ BROWSING: $url")
            }
        }
    }
}