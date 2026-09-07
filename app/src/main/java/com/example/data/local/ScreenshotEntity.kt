package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "screenshots",
    indices = [
        Index(value = ["dateTaken"]),
        Index(value = ["category"]),
        Index(value = ["isFavorite"]),
        Index(value = ["isDuplicate"]),
        Index(value = ["duplicateGroupId"]),
        Index(value = ["size"])
    ]
)
data class ScreenshotEntity(
    @PrimaryKey val id: Long,
    val uri: String,
    val filePath: String,
    val displayName: String,
    val dateTaken: Long,
    val dateModified: Long,
    val size: Long,
    val width: Int = 0,
    val height: Int = 0,
    val ocrText: String = "",
    val category: String = "Other",
    val isFavorite: Boolean = false,
    val isProcessed: Boolean = false,
    val fileHash: String = "",
    val perceptualHash: Long = 0L,
    val extractedPrices: String = "",
    val extractedDates: String = "",
    val extractedEmails: String = "",
    val extractedPhones: String = "",
    val extractedUrls: String = "",
    val extractedOrderNumbers: String = "",
    val duplicateGroupId: String? = null,
    val isDuplicate: Boolean = false,
    val isPotentiallyRemovable: Boolean = false,
    val aiSummary: String? = null
)
