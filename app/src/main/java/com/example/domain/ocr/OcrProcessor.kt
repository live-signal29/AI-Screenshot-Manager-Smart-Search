package com.example.domain.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.coroutines.resume

class OcrProcessor(private val context: Context) {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    suspend fun processImageUri(uriString: String): String = withContext(Dispatchers.IO) {
        val uri = Uri.parse(uriString)
        try {
            val inputImage = try {
                InputImage.fromFilePath(context, uri)
            } catch (e: Exception) {
                // Fallback to loading decoded downsampled bitmap if direct fromFilePath fails
                val bitmap = decodeSampledBitmapFromUri(uri, 1200, 1200) ?: return@withContext ""
                InputImage.fromBitmap(bitmap, 0)
            }

            suspendCancellableCoroutine { continuation ->
                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        continuation.resume(visionText.text)
                    }
                    .addOnFailureListener { exception ->
                        // Graceful fallback on OCR failure
                        continuation.resume("")
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun decodeSampledBitmapFromUri(uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            var stream: InputStream? = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(stream, null, options)
            stream?.close()

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false

            stream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(stream, null, options)
            stream?.close()
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
