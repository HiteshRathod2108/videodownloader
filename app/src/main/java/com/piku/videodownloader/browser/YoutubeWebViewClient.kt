package com.piku.videodownloader.browser

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.ByteArrayInputStream

class YoutubeWebViewClient(
    // Added a 3rd parameter: canGoBack
    private val onPageChanged: (url: String, isVideo: Boolean, canGoBack: Boolean) -> Unit
) : WebViewClient() {

    private val videoUrlPattern = Regex(".*youtube\\.com/watch\\?v=.*")

    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)

        // THE FIX: Inject the ad-skipper on EVERY navigation (because YouTube is a Single Page App)
        view?.evaluateJavascript(AdBlocker.getAdBypassScript(), null)

        if (url != null) {
            val isVideo = videoUrlPattern.matches(url)
            // Tell the UI if the browser has history to go back to
            onPageChanged(url, isVideo, view?.canGoBack() ?: false)
        }
    }

    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
        val url = request?.url?.toString() ?: return super.shouldInterceptRequest(view, request)

        if (AdBlocker.isAdOrTracker(url)) {
            val emptyStream = ByteArrayInputStream(ByteArray(0))
            return WebResourceResponse("text/plain", "UTF-8", emptyStream)
        }
        return super.shouldInterceptRequest(view, request)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        view?.evaluateJavascript(AdBlocker.getAdBypassScript(), null)
    }
}