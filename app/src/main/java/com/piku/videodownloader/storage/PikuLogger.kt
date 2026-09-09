package com.piku.videodownloader.storage

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PikuLogger {

    private const val LOG_FILE_NAME = "piku_error_log.txt"

    /**
     * Appends an error message and stack trace to the public log file.
     */
    fun logError(context: Context, tag: String, message: String, throwable: Throwable? = null) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val stackTrace = throwable?.stackTraceToString() ?: "No stack trace"

        val logEntry = """
            ==================================================
            TIMESTAMP: $timestamp
            TAG:       $tag
            MESSAGE:   $message
            DETAILS:   $stackTrace
            ==================================================
            
        """.trimIndent()

        Log.e(tag, "$message: $stackTrace")

        try {
            writeToDownloads(context, logEntry)
        } catch (e: Exception) {
            Log.e("PikuLogger", "Failed to write error to disk", e)
        }
    }

    private fun writeToDownloads(context: Context, textToAppend: String) {
        val resolver = context.contentResolver
        val uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI

        // Query if our log file already exists in Downloads/Piku
        val projection = arrayOf(MediaStore.Downloads._ID)
        val selection = "${MediaStore.Downloads.DISPLAY_NAME} = ? AND ${MediaStore.Downloads.RELATIVE_PATH} = ?"
        val selectionArgs = arrayOf(LOG_FILE_NAME, Environment.DIRECTORY_DOWNLOADS + "/Piku/")

        var existingUri: android.net.Uri? = null
        resolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                existingUri = android.content.ContentUris.withAppendedId(uri, id)
            }
        }

        val targetUri = existingUri ?: run {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, LOG_FILE_NAME)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Piku")
            }
            resolver.insert(uri, contentValues)
        }

        targetUri?.let { fileUri ->
            resolver.openOutputStream(fileUri, "wa")?.use { outputStream -> // "wa" = write append
                outputStream.write(textToAppend.toByteArray())
            }
        }
    }
}