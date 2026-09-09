package com.piku.videodownloader.browser

object AdBlocker {

    private val adDomains = listOf(
        "googleads.g.doubleclick.net",
        "pagead2.googlesyndication.com",
        "ad.youtube.com",
        "youtube.com/api/stats/ads",
        "youtube.com/pagead/",
        "doubleclick.net",
        "googleadservices.com"
    )

    fun isAdOrTracker(url: String): Boolean {
        return adDomains.any { domain -> url.contains(domain, ignoreCase = true) }
    }

    fun getAdBypassScript(): String {
        return """
            javascript:(function() {
                // Prevent injecting the same loop multiple times
                if (window.pikuAdBlockerInjected) return;
                window.pikuAdBlockerInjected = true;

                // 1. Hide mobile-specific ad containers
                var style = document.createElement('style');
                style.innerHTML = `
                    .ad-container, .ad-div, .ytp-ad-module, 
                    .ytp-ad-image-overlay, .video-ads, 
                    ytd-display-ad-renderer, .player-ads,
                    ytm-promoted-video-renderer,
                    .ytm-custom-ad-host { display: none !important; } 
                `;
                document.head.appendChild(style);

                // 2. Aggressive video fast-forward & skip loop
                setInterval(function() {
                    var video = document.querySelector('video');
                    var adContainer = document.querySelector('.ad-showing, .ad-interrupting, .ytm-custom-ad-host');
                    
                    // If an ad is playing, mute it and play it at 16x speed
                    if (adContainer && video) {
                        video.muted = true;
                        if (video.playbackRate !== 16.0) video.playbackRate = 16.0;
                    }

                    // Hunt for skip buttons
                    var skipButtons = document.querySelectorAll('.ytp-ad-skip-button, .ytp-skip-ad-button, .ytm-btn');
                    skipButtons.forEach(function(btn) {
                        // Click if the button explicitly has "Skip" text or class
                        if (btn && btn.innerText && btn.innerText.toLowerCase().includes('skip')) {
                            btn.click();
                        } else if (btn && btn.className.includes('skip')) {
                            btn.click();
                        }
                    });
                }, 250);
            })();
        """.trimIndent()
    }
}