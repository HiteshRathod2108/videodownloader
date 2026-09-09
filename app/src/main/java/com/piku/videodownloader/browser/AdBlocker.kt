package com.piku.videodownloader.browser

object AdBlocker {

    // A lightweight DNS-style blocklist for known trackers and banner ad servers
    private val adDomains = listOf(
        "googleads.g.doubleclick.net",
        "pagead2.googlesyndication.com",
        "ad.youtube.com",
        "youtube.com/api/stats/ads",
        "youtube.com/pagead/",
        "doubleclick.net",
        "googleadservices.com"
    )

    /**
     * Checks if the outgoing network request matches our blocklist.
     */
    fun isAdOrTracker(url: String): Boolean {
        return adDomains.any { domain -> url.contains(domain, ignoreCase = true) }
    }

    /**
     * JavaScript injected into the webpage to hide empty ad boxes
     * and specific YouTube ad-player elements.
     */
    fun getHiddenCssScript(): String {
        return """
            javascript:(function() {
                var style = document.createElement('style');
                style.innerHTML = ' 
                    .ad-container, 
                    .ad-div, 
                    .ytp-ad-module, 
                    .ytp-ad-image-overlay, 
                    .video-ads, 
                    ytd-promoted-sparkles-web-renderer, 
                    ytd-display-ad-renderer 
                    { display: none !important; } 
                ';
                document.head.appendChild(style);
            })();
        """.trimIndent()
    }
}