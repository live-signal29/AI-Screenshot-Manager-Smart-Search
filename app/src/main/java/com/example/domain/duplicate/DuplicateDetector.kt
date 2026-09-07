package com.example.domain.duplicate

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import com.example.data.local.ScreenshotEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.security.MessageDigest
import kotlin.math.abs

data class DuplicateGroup(
    val groupId: String,
    val canonicalScreenshot: ScreenshotEntity,
    val duplicateScreenshots: List<ScreenshotEntity>,
    val totalSavingsBytes: Long
)

object DuplicateDetector {

    /**
     * Computes a 64-bit difference hash (dHash) for an image.
     * Downsamples to 9x8 grayscale and compares adjacent horizontal pixels.
     */
    suspend fun computeDHash(context: Context, uriString: String): Long = withContext(Dispatchers.IO) {
        val uri = Uri.parse(uriString)
        try {
            val originalStream: InputStream? = context.contentResolver.openInputStream(uri)
            val fullBitmap = BitmapFactory.decodeStream(originalStream)
            originalStream?.close()

            if (fullBitmap == null) return@withContext 0L

            val scaled = Bitmap.createScaledBitmap(fullBitmap, 9, 8, true)
            var hash = 0L

            for (y in 0 until 8) {
                for (x in 0 until 8) {
                    val leftPixel = scaled.getPixel(x, y)
                    val rightPixel = scaled.getPixel(x + 1, y)

                    val leftBrightness = (Color.red(leftPixel) * 299 + Color.green(leftPixel) * 587 + Color.blue(leftPixel) * 114) / 1000
                    val rightBrightness = (Color.red(rightPixel) * 299 + Color.green(rightPixel) * 587 + Color.blue(rightPixel) * 114) / 1000

                    if (leftBrightness > rightBrightness) {
                        hash = hash or (1L shl (y * 8 + x))
                    }
                }
            }
            scaled.recycle()
            fullBitmap.recycle()
            hash
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Computes SHA-256 file hash for exact duplicate detection.
     */
    suspend fun computeFileHash(context: Context, uriString: String): String = withContext(Dispatchers.IO) {
        val uri = Uri.parse(uriString)
        try {
            val stream = context.contentResolver.openInputStream(uri) ?: return@withContext ""
            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (stream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
            stream.close()
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Calculates the Hamming distance between two 64-bit perceptual hashes.
     */
    fun hammingDistance(hash1: Long, hash2: Long): Int {
        var xor = hash1 xor hash2
        var distance = 0
        while (xor != 0L) {
            distance += (xor and 1L).toInt()
            xor = xor ushr 1
        }
        return distance
    }

    /**
     * Groups a list of screenshots into duplicate clusters.
     */
    fun findDuplicateClusters(screenshots: List<ScreenshotEntity>): List<DuplicateGroup> {
        val groups = mutableListOf<DuplicateGroup>()
        val visitedIds = mutableSetOf<Long>()

        for (i in screenshots.indices) {
            val current = screenshots[i]
            if (current.id in visitedIds) continue

            val duplicates = mutableListOf<ScreenshotEntity>()

            for (j in (i + 1) until screenshots.size) {
                val candidate = screenshots[j]
                if (candidate.id in visitedIds) continue

                val isExactDuplicate = current.fileHash.isNotBlank() && current.fileHash == candidate.fileHash
                val isSizeMatch = current.size > 0 && current.size == candidate.size &&
                        current.width == candidate.width && current.height == candidate.height

                val isNearDuplicate = current.perceptualHash != 0L && candidate.perceptualHash != 0L &&
                        hammingDistance(current.perceptualHash, candidate.perceptualHash) <= 4

                if (isExactDuplicate || isSizeMatch || isNearDuplicate) {
                    duplicates.add(candidate)
                    visitedIds.add(candidate.id)
                }
            }

            if (duplicates.isNotEmpty()) {
                visitedIds.add(current.id)
                val savings = duplicates.sumOf { it.size }
                groups.add(
                    DuplicateGroup(
                        groupId = "dup_group_${current.id}",
                        canonicalScreenshot = current,
                        duplicateScreenshots = duplicates,
                        totalSavingsBytes = savings
                    )
                )
            }
        }
        return groups
    }
}
