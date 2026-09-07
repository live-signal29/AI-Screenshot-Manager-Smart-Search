package com.example.domain.scanner

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.data.local.ScreenshotEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ScreenshotScanner(private val context: Context) {

    suspend fun scanScreenshots(lastScanTime: Long = 0L): List<ScreenshotEntity> = withContext(Dispatchers.IO) {
        val screenshots = mutableListOf<ScreenshotEntity>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )

        val selectionList = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()

        // Look for screenshots based on bucket name, filename, or folder
        val screenshotFilter = "(LOWER(${MediaStore.Images.Media.BUCKET_DISPLAY_NAME}) LIKE ? " +
                "OR LOWER(${MediaStore.Images.Media.DISPLAY_NAME}) LIKE ? " +
                "OR LOWER(${MediaStore.Images.Media.DATA}) LIKE ?)"
        selectionList.add(screenshotFilter)
        selectionArgs.add("%screenshot%")
        selectionArgs.add("%screenshot%")
        selectionArgs.add("%screenshot%")

        if (lastScanTime > 0) {
            selectionList.add("${MediaStore.Images.Media.DATE_MODIFIED} > ?")
            selectionArgs.add((lastScanTime / 1000).toString())
        }

        val selection = selectionList.joinToString(" AND ")
        val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"

        try {
            val cursor: Cursor? = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs.toTypedArray(),
                sortOrder
            )

            cursor?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val dataCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                val dateAddedCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                val dateModifiedCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
                val sizeCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val widthCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                val heightCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)

                while (c.moveToNext()) {
                    val id = c.getLong(idCol)
                    val name = c.getString(nameCol) ?: "Screenshot_$id"
                    val path = c.getString(dataCol) ?: ""
                    val dateAdded = c.getLong(dateAddedCol) * 1000
                    val dateModified = c.getLong(dateModifiedCol) * 1000
                    val size = c.getLong(sizeCol)
                    val width = c.getInt(widthCol)
                    val height = c.getInt(heightCol)

                    val contentUri: Uri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    screenshots.add(
                        ScreenshotEntity(
                            id = id,
                            uri = contentUri.toString(),
                            filePath = path,
                            displayName = name,
                            dateTaken = if (dateAdded > 0) dateAdded else System.currentTimeMillis(),
                            dateModified = if (dateModified > 0) dateModified else System.currentTimeMillis(),
                            size = size,
                            width = width,
                            height = height,
                            ocrText = "",
                            category = "Other",
                            isFavorite = false,
                            isProcessed = false,
                            fileHash = "",
                            perceptualHash = 0L
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        screenshots
    }
}
