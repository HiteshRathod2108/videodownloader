package com.piku.videodownloader.ui

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.piku.videodownloader.background.DownloadWorker
import com.piku.videodownloader.browser.YoutubeWebViewClient
import com.piku.videodownloader.downloader.DownloadEngine
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen() {
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // NEW: Compose state for the back button
    var canGoBack by remember { mutableStateOf(false) }

    var currentUrl by remember { mutableStateOf("") }
    var isVideoPage by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var isFetching by remember { mutableStateOf(false) }
    var availableQualities by remember { mutableStateOf<List<String>>(emptyList()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // THE FIX: Only trap the back button if the WebView actually has history
    BackHandler(enabled = canGoBack) {
        webViewRef?.goBack()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            if (isVideoPage) {
                ExtendedFloatingActionButton(
                    onClick = {
                        showBottomSheet = true
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

        AndroidView(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            factory = { ctx ->
                WebView(ctx).apply {
                    webViewRef = this

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = false
                        mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                    }

                    // Pass the backAvailable state up to Compose!
                    webViewClient = YoutubeWebViewClient { url, isVideo, backAvailable ->
                        if (url != currentUrl) { availableQualities = emptyList() }
                        currentUrl = url
                        isVideoPage = isVideo
                        canGoBack = backAvailable // This triggers the BackHandler to enable/disable
                    }
                    loadUrl("https://m.youtube.com")
                }
            }
        )
    }

    if (showBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showBottomSheet = false }) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Choose Quality", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))

                if (isFetching) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    Text("Finding best streams...")
                } else {
                    availableQualities.forEach { quality ->
                        Button(
                            onClick = {
                                showBottomSheet = false
                                val downloadData = Data.Builder()
                                    .putString("url", currentUrl)
                                    .putString("quality", quality)
                                    .build()

                                val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                                    .setInputData(downloadData)
                                    .build()

                                WorkManager.getInstance(context).enqueue(downloadRequest)
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(quality)
                        }
                    }
                }
            }
        }
    }
}