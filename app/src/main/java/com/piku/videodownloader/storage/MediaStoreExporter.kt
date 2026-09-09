package com.piku.videodownloader.storage

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.FileInputStream

object MediaStoreExporter {

    fun exportLatestVideo(context: Context) {
        val cacheDir = context.cacheDir

        // Find the newly downloaded .mp4 file in our hidden cache
        val videoFile = cacheDir.listFiles()?.firstOrNull { it.extension == "mp4" }

        if (videoFile == null || !videoFile.exists()) {
            Log.e("PikuStorage", "🔴 No MP4 file found in cache to export.")
            return
        }

        Log.d("PikuStorage", "📦 Moving ${videoFile.name} to public Downloads...")

        val resolver = context.contentResolver

        // Tell Android we want to create a new MP4 file in the public Downloads folder
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, videoFile.name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            // We will put it in a "Piku" subfolder inside Downloads to keep it tidy
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Piku")
        }

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

        if (uri != null) {
            try {
                // Copy the raw bytes from our hidden cache to the public folder
                resolver.openOutputStream(uri)?.use { outputStream ->
                    FileInputStream(videoFile).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                Log.d("PikuStorage", "✅ Successfully saved to Downloads/Piku!")

                // Delete the hidden cached file so we don't waste the user's storage space
                videoFile.delete()

            } catch (e: Exception) {
                Log.e("PikuStorage", "🔴 Failed to copy file to MediaStore", e)
            }
        }
    }
}