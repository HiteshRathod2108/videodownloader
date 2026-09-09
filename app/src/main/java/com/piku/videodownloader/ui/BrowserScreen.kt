package com.piku.videodownloader.ui

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.piku.videodownloader.browser.YoutubeWebViewClient
import com.piku.videodownloader.downloader.DownloadEngine
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen() {
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    // UI State Memory
    var currentUrl by remember { mutableStateOf("") }
    var isVideoPage by remember { mutableStateOf(false) }

    var showBottomSheet by remember { mutableStateOf(false) }
    var isFetching by remember { mutableStateOf(false) }
    var availableQualities by remember { mutableStateOf<List<String>>(emptyList()) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        // 1. The Floating Action Button (Only shows on video pages)
        floatingActionButton = {
            if (isVideoPage) {
                ExtendedFloatingActionButton(
                    onClick = {
                        showBottomSheet = true
                        // Only fetch if we haven't fetched for this specific video yet
                        if (availableQualities.isEmpty()) {
                            isFetching = true
                            coroutineScope.launch {
                                availableQualities = DownloadEngine.fetchVideoFormats(currentUrl)
                                isFetching = false
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Text("Download")
                }
            }
        }
    ) { innerPadding ->

        // 2. The YouTube Browser
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    webViewClient = YoutubeWebViewClient { url, isVideo ->
                        // If the user clicked a NEW video, reset the qualities list
                        if (url != currentUrl) {
                            availableQualities = emptyList()
                        }
                        currentUrl = url
                        isVideoPage = isVideo
                    }
                    loadUrl("https://m.youtube.com")
                }
            }
        )
    }

    // 3. The Bottom Sheet Menu
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Choose Quality",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (isFetching) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    Text("Finding best streams...")
                } else {
                    // Display the cleaned-up list of buttons
                    availableQualities.forEach { quality ->
                        Button(
                            onClick = {
                                showBottomSheet = false
                                // Launch the actual download in the background!
                                coroutineScope.launch {
                                    DownloadEngine.executeDownload(currentUrl, quality, context)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(quality)
                        }
                    }
                }
            }
        }
    }
}