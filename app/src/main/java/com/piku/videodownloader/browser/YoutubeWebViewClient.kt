package com.piku.videodownloader.browser

import android.webkit.WebView
import android.webkit.WebViewClient

class YoutubeWebViewClient(
    private val onPageChanged: (url: String, isVideo: Boolean) -> Unit
) : WebViewClient() {

    private val videoUrlPattern = Regex(".*youtube\\.com/watch\\?v=.*")

    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)
        if (url != null) {
            val isVideo = videoUrlPattern.matches(url)
            onPageChanged(url, isVideo)
        }
    }
}