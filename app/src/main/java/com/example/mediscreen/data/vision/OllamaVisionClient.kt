package com.example.mediscreen.data.vision

import android.util.Base64
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class StrokeScreeningAnalysis(
    val concerning: Boolean,
    val uncertain: Boolean,
    val observations: List<String>
)

/** Calls a developer-run Ollama server on the same private network. */
class OllamaVisionClient(
    private val baseUrl: String,
    private val model: String
) {
    /**
     * Starts loading the model while the user is completing the camera steps.
     * Ollama keeps it resident so the real request does not pay the cold-start
     * cost after the user presses Analyze.
     */
    suspend fun warmUp() = withContext(Dispatchers.IO) {
        requireConfigured()
        executeRequest(
            JSONObject().apply {
                put("model", model)
                put("stream", false)
                put("keep_alive", KEEP_ALIVE)
                put("options", JSONObject().put("num_ctx", NUM_CONTEXT_TOKENS))
            },
            readTimeoutMillis = WARM_UP_TIMEOUT_MILLIS
        )
    }

    suspend fun analyzeStroke(
        faceImagePath: String,
        armsImagePath: String,
        speechObservation: String
    ): StrokeScreeningAnalysis = withContext(Dispatchers.IO) {
        requireConfigured()

        // Decode and compress the independent captures concurrently. CameraX
        // images are large enough that doing this serially is noticeable.
        val encodedImages = coroutineScope {
            val face = async { encodeImage(faceImagePath) }
            val arms = async { encodeImage(armsImagePath) }
            listOf(face.await(), arms.await())
        }

        val requestBody = JSONObject().apply {
            put("model", model)
            put("stream", false)
            put("keep_alive", KEEP_ALIVE)
            put("format", responseSchema())
            put("system", SYSTEM_PROMPT)
            put("prompt", "Evaluate these guided stroke-screening inputs. User observation: $speechObservation")
            put("images", JSONArray().apply {
                encodedImages.forEach(::put)
            })
            put("options", JSONObject()
                .put("temperature", 0)
                .put("num_ctx", NUM_CONTEXT_TOKENS)
                .put("num_predict", MAX_RESPONSE_TOKENS)
            )
        }

        val responseText = executeRequest(requestBody, ANALYSIS_TIMEOUT_MILLIS)
        parseResponse(JSONObject(responseText).getString("response"))
    }

    private fun requireConfigured() {
        check(baseUrl.isNotBlank()) {
            "Local AI is not configured. Build with -PollamaBaseUrl=http://YOUR_LAPTOP_IP:11434"
        }
    }

    private fun executeRequest(requestBody: JSONObject, readTimeoutMillis: Int): String {
        val connection = (URL("${baseUrl.trimEnd('/')}/api/generate").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = readTimeoutMillis
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
        }
        return try {
            connection.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(requestBody.toString()) }
            val responseText = (if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            check(connection.responseCode in 200..299) { "Local AI request failed (${connection.responseCode}): $responseText" }
            responseText
        } finally {
            connection.disconnect()
        }
    }

    /**
     * CameraX photos are much larger than the visual evidence needed for this
     * screening. Resizing keeps two images well inside the local model's context.
     */
    private fun encodeImage(path: String): String {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        check(bounds.outWidth > 0 && bounds.outHeight > 0) { "Could not read captured image" }

        var sampleSize = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sampleSize * 2) >= MAX_IMAGE_SIDE) {
            sampleSize *= 2
        }
        val source = BitmapFactory.decodeFile(
            path,
            BitmapFactory.Options().apply { inSampleSize = sampleSize }
        ) ?: error("Could not read captured image")
        val largestSide = maxOf(source.width, source.height)
        val scaled = if (largestSide > MAX_IMAGE_SIDE) {
            val scale = MAX_IMAGE_SIDE.toFloat() / largestSide
            Bitmap.createScaledBitmap(
                source,
                (source.width * scale).toInt(),
                (source.height * scale).toInt(),
                true
            ).also { if (it !== source) source.recycle() }
        } else {
            source
        }
        return ByteArrayOutputStream().use { output ->
            scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
            if (scaled !== source) scaled.recycle()
            Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
        }
    }

    private fun parseResponse(content: String): StrokeScreeningAnalysis {
        val json = JSONObject(content)
        return StrokeScreeningAnalysis(
            concerning = json.optBoolean("concerning", false),
            uncertain = json.optBoolean("uncertain", true),
            observations = json.optJSONArray("observations")?.let { values ->
                List(values.length()) { index -> values.optString(index) }.filter { it.isNotBlank() }
            }.orEmpty()
        )
    }

    private fun responseSchema() = JSONObject().apply {
        put("type", "object")
        put("properties", JSONObject().apply {
            put("concerning", JSONObject().put("type", "boolean"))
            put("uncertain", JSONObject().put("type", "boolean"))
            put("observations", JSONObject().apply {
                put("type", "array")
                put("maxItems", 3)
                put("items", JSONObject().put("type", "string"))
            })
        })
        put("required", JSONArray(listOf("concerning", "uncertain", "observations")))
    }

    private companion object {
        const val MAX_IMAGE_SIDE = 320
        const val JPEG_QUALITY = 65
        const val KEEP_ALIVE = "10m"
        const val NUM_CONTEXT_TOKENS = 2048
        const val MAX_RESPONSE_TOKENS = 72
        const val WARM_UP_TIMEOUT_MILLIS = 120_000
        const val ANALYSIS_TIMEOUT_MILLIS = 120_000
        const val SYSTEM_PROMPT = """
            You are a conservative visual screening assistant for a stroke emergency demo, not a medical diagnostician.
            Review only visible face asymmetry, the raised-arm attempt, and the user's speech observation. Do not identify a disease.
            Set concerning=true if any possible FAST warning sign is visible or described. Set uncertain=true if image quality, pose,
            lighting, or evidence prevents a confident assessment. If uncertain, err toward concern. Return at most three brief
            observations and only JSON matching the schema.
        """
    }
}
