package com.example.domain.ai

import com.example.data.local.ScreenshotEntity
import com.example.domain.ocr.EntityExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class AiInsight(
    val summary: String,
    val description: String,
    val keyInformation: List<String>,
    val suggestedActions: List<String>,
    val isGeneratedLocally: Boolean
)

class AiAssistantService {

    /**
     * Generates a comprehensive summary of a screenshot.
     * Prioritizes local on-device entity synthesis when cloud AI is disabled or offline.
     */
    suspend fun generateInsight(
        screenshot: ScreenshotEntity,
        isCloudAiEnabled: Boolean,
        apiKey: String,
        endpointUrl: String
    ): AiInsight = withContext(Dispatchers.IO) {
        if (isCloudAiEnabled && apiKey.isNotBlank()) {
            try {
                return@withContext requestCloudInsight(screenshot, apiKey, endpointUrl)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // Local On-Device synthesis
        generateLocalInsight(screenshot)
    }

    suspend fun answerQuestion(
        screenshot: ScreenshotEntity,
        question: String,
        isCloudAiEnabled: Boolean,
        apiKey: String,
        endpointUrl: String
    ): String = withContext(Dispatchers.IO) {
        if (isCloudAiEnabled && apiKey.isNotBlank()) {
            try {
                return@withContext requestCloudAnswer(screenshot, question, apiKey, endpointUrl)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // Local question answering based on OCR text & entities
        answerQuestionLocally(screenshot, question)
    }

    private fun generateLocalInsight(screenshot: ScreenshotEntity): AiInsight {
        val prices = EntityExtractor.deserializeList(screenshot.extractedPrices)
        val dates = EntityExtractor.deserializeList(screenshot.extractedDates)
        val emails = EntityExtractor.deserializeList(screenshot.extractedEmails)
        val phones = EntityExtractor.deserializeList(screenshot.extractedPhones)
        val urls = EntityExtractor.deserializeList(screenshot.extractedUrls)
        val orders = EntityExtractor.deserializeList(screenshot.extractedOrderNumbers)

        val keyInfo = mutableListOf<String>()
        if (prices.isNotEmpty()) keyInfo.add("Price: ${prices.joinToString(", ")}")
        if (dates.isNotEmpty()) keyInfo.add("Date: ${dates.joinToString(", ")}")
        if (orders.isNotEmpty()) keyInfo.add("Ref/Order: ${orders.joinToString(", ")}")
        if (emails.isNotEmpty()) keyInfo.add("Email: ${emails.joinToString(", ")}")
        if (phones.isNotEmpty()) keyInfo.add("Phone: ${phones.joinToString(", ")}")
        if (urls.isNotEmpty()) keyInfo.add("Link: ${urls.first()}")

        val summary = buildString {
            append("${screenshot.category} screenshot")
            if (prices.isNotEmpty()) {
                append(" involving ${prices.first()}")
            }
            if (dates.isNotEmpty()) {
                append(" recorded on ${dates.first()}")
            }
            if (orders.isNotEmpty()) {
                append(" with reference ${orders.first()}")
            }
            append(".")
        }

        val description = if (screenshot.ocrText.isNotBlank()) {
            screenshot.ocrText.lines().filter { it.isNotBlank() }.take(3).joinToString(" • ")
        } else {
            "Captured image in ${screenshot.category} category."
        }

        val actions = mutableListOf<String>()
        if (prices.isNotEmpty()) actions.add("Track Expense")
        if (emails.isNotEmpty()) actions.add("Send Email")
        if (phones.isNotEmpty()) actions.add("Call Number")
        if (urls.isNotEmpty()) actions.add("Open Website")
        actions.add("Share Text")

        return AiInsight(
            summary = summary,
            description = description,
            keyInformation = keyInfo,
            suggestedActions = actions,
            isGeneratedLocally = true
        )
    }

    private fun answerQuestionLocally(screenshot: ScreenshotEntity, question: String): String {
        val qLower = question.lowercase()
        val prices = EntityExtractor.deserializeList(screenshot.extractedPrices)
        val dates = EntityExtractor.deserializeList(screenshot.extractedDates)
        val phones = EntityExtractor.deserializeList(screenshot.extractedPhones)
        val emails = EntityExtractor.deserializeList(screenshot.extractedEmails)

        return when {
            qLower.contains("price") || qLower.contains("how much") || qLower.contains("cost") || qLower.contains("amount") -> {
                if (prices.isNotEmpty()) "The detected amount in this screenshot is ${prices.joinToString(", ")}."
                else "No specific price or currency amount was found in this screenshot."
            }
            qLower.contains("date") || qLower.contains("when") -> {
                if (dates.isNotEmpty()) "The detected date is ${dates.joinToString(", ")}."
                else "No specific calendar date was detected."
            }
            qLower.contains("phone") || qLower.contains("number") || qLower.contains("contact") -> {
                if (phones.isNotEmpty()) "The detected phone number is ${phones.joinToString(", ")}."
                else "No contact phone number was detected."
            }
            qLower.contains("email") || qLower.contains("mail") -> {
                if (emails.isNotEmpty()) "The detected email address is ${emails.joinToString(", ")}."
                else "No email address was found."
            }
            qLower.contains("category") || qLower.contains("type") -> {
                "This screenshot is classified as '${screenshot.category}' based on on-device OCR analysis."
            }
            else -> {
                // Search OCR text for relevant line
                val matchedLine = screenshot.ocrText.lines().firstOrNull { line ->
                    val lineLower = line.lowercase()
                    qLower.split(" ").filter { it.length > 3 }.any { lineLower.contains(it) }
                }
                if (matchedLine != null) {
                    "Found relevant information: \"$matchedLine\""
                } else {
                    "Based on on-device analysis, this is a ${screenshot.category} screenshot. Extracted text snippet: \"${screenshot.ocrText.take(120)}...\""
                }
            }
        }
    }

    private fun requestCloudInsight(screenshot: ScreenshotEntity, apiKey: String, endpointUrl: String): AiInsight {
        // Standard secure REST proxy call to cloud AI endpoint
        val targetUrl = if (endpointUrl.isNotBlank()) endpointUrl else "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
        val url = URL(targetUrl)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true

        val prompt = "Analyze this screenshot OCR text and metadata. Category: ${screenshot.category}. Text: ${screenshot.ocrText.take(1500)}. Provide a concise 1-sentence summary and 3 key facts."
        val jsonBody = JSONObject().apply {
            put("contents", org.json.JSONArray().put(
                JSONObject().put("parts", org.json.JSONArray().put(
                    JSONObject().put("text", prompt)
                ))
            ))
        }

        OutputStreamWriter(conn.outputStream).use { it.write(jsonBody.toString()) }

        if (conn.responseCode in 200..299) {
            val response = conn.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(response)
            val candidates = root.optJSONArray("candidates")
            val text = candidates?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text") ?: ""
            return AiInsight(
                summary = text.lines().firstOrNull { it.isNotBlank() } ?: "Analyzed by AI Assistant",
                description = text,
                keyInformation = listOf("Cloud AI Verified", "Category: ${screenshot.category}"),
                suggestedActions = listOf("Copy AI Analysis", "Share"),
                isGeneratedLocally = false
            )
        }
        return generateLocalInsight(screenshot)
    }

    private fun requestCloudAnswer(screenshot: ScreenshotEntity, question: String, apiKey: String, endpointUrl: String): String {
        val targetUrl = if (endpointUrl.isNotBlank()) endpointUrl else "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
        val url = URL(targetUrl)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true

        val prompt = "Context from screenshot (Category: ${screenshot.category}, Text: ${screenshot.ocrText.take(1500)}). User Question: $question. Answer accurately and concisely in 2 sentences."
        val jsonBody = JSONObject().apply {
            put("contents", org.json.JSONArray().put(
                JSONObject().put("parts", org.json.JSONArray().put(
                    JSONObject().put("text", prompt)
                ))
            ))
        }

        OutputStreamWriter(conn.outputStream).use { it.write(jsonBody.toString()) }

        if (conn.responseCode in 200..299) {
            val response = conn.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(response)
            val candidates = root.optJSONArray("candidates")
            return candidates?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text")
                ?: answerQuestionLocally(screenshot, question)
        }
        return answerQuestionLocally(screenshot, question)
    }
}
