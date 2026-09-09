package com.piku.videodownloader.browser

import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.ByteArrayInputStream

class YoutubeWebViewClient(
    private val onPageChanged: (url: String, isVideo: Boolean) -> Unit
) : WebViewClient() {

    private val videoUrlPattern = Regex(".*youtube\\.com/watch\\?v=.*")

    // 1. Detect Navigation
    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)
        if (url != null) {
            val isVideo = videoUrlPattern.matches(url)
            onPageChanged(url, isVideo)
        }
    }

    // 2. Intercept and Block Network Requests
    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
        val url = request?.url?.toString() ?: return super.shouldInterceptRequest(view, request)

        // If the URL matches our AdBlocker list, return an empty, dead response
        if (AdBlocker.isAdOrTracker(url)) {
            Log.d("PikuAdBlock", "🛡️ Blocked Ad/Tracker: $url")

            // This returns an empty stream, effectively neutralizing the ad request
            val emptyStream = ByteArrayInputStream(ByteArray(0))
            return WebResourceResponse("text/plain", "UTF-8", emptyStream)
        }

        // Otherwise, let the request pass normally
        return super.shouldInterceptRequest(view, request)
    }

    // 3. Inject CSS to hide visual ad containers once the page loads
    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        view?.evaluateJavascript(AdBlocker.getHiddenCssScript(), null)
    }
}