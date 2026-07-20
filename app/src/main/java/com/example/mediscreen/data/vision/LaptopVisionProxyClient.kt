package com.example.mediscreen.data.vision

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

/** Sends compact camera captures to the private laptop proxy, never OpenRouter directly. */
class LaptopVisionProxyClient(
    private val baseUrl: String
) {
    suspend fun analyzeStroke(
        faceImagePath: String,
        armsImagePath: String,
        speechObservation: String
    ): StrokeScreeningAnalysis = withContext(Dispatchers.IO) {
        check(baseUrl.isNotBlank()) { "Cloud analysis is not configured." }

        val images = coroutineScope {
            val face = async { encodeImage(faceImagePath) }
            val arms = async { encodeImage(armsImagePath) }
            listOf(face.await(), arms.await())
        }
        val requestBody = JSONObject().apply {
            put("faceImageBase64", images[0])
            put("armsImageBase64", images[1])
            put("speechObservation", speechObservation)
        }
        val connection = (URL("${baseUrl.trimEnd('/')}/stroke-screen").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10_000
            readTimeout = 55_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
        }
        try {
            connection.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(requestBody.toString()) }
            val responseText = (if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            check(connection.responseCode in 200..299) { JSONObject(responseText).optString("error", "Cloud analysis failed.") }
            parseResponse(JSONObject(responseText))
        } finally {
            connection.disconnect()
        }
    }

    private fun encodeImage(path: String): String {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        check(bounds.outWidth > 0 && bounds.outHeight > 0) { "Could not read captured image." }
        var sampleSize = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sampleSize * 2) >= MAX_IMAGE_SIDE) sampleSize *= 2
        val source = BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sampleSize })
            ?: error("Could not read captured image.")
        val largestSide = maxOf(source.width, source.height)
        val scaled = if (largestSide > MAX_IMAGE_SIDE) {
            val scale = MAX_IMAGE_SIDE.toFloat() / largestSide
            Bitmap.createScaledBitmap(source, (source.width * scale).toInt(), (source.height * scale).toInt(), true)
                .also { source.recycle() }
        } else source
        return ByteArrayOutputStream().use { output ->
            scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
            scaled.recycle()
            Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
        }
    }

    private fun parseResponse(json: JSONObject) = StrokeScreeningAnalysis(
        concerning = json.optBoolean("concerning", false),
        uncertain = json.optBoolean("uncertain", true),
        observations = json.optJSONArray("observations")?.let { values ->
            List(values.length()) { index -> values.optString(index) }.filter { it.isNotBlank() }
        }.orEmpty()
    )

    private companion object {
        // Cloud inference has enough bandwidth to use more visual detail than
        // the CPU-only local model path, which improves face and arm review.
        const val MAX_IMAGE_SIDE = 512
        const val JPEG_QUALITY = 80
    }
}
