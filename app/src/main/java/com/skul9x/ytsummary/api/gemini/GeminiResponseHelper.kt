package com.skul9x.ytsummary.api.gemini

import android.util.Log
import kotlinx.serialization.json.*

/**
 * Helper object for processing Gemini API responses and managing JSON serialization.
 */
object GeminiResponseHelper {
    private const val TAG = "GeminiResponseHelper"

    fun buildRequestBody(prompt: String, temperature: Double = 0.7, maxOutputTokens: Int = 4096): String {
        val json = buildJsonObject {
            putJsonArray("contents") {
                addJsonObject {
                    putJsonArray("parts") {
                        addJsonObject {
                            put("text", prompt)
                        }
                    }
                }
            }
            putJsonObject("generationConfig") {
                put("temperature", temperature)
                put("maxOutputTokens", maxOutputTokens)
                put("topP", 0.95)
            }
            putJsonArray("safetySettings") {
                listOf(
                    "HARM_CATEGORY_HARASSMENT",
                    "HARM_CATEGORY_HATE_SPEECH",
                    "HARM_CATEGORY_SEXUALLY_EXPLICIT",
                    "HARM_CATEGORY_DANGEROUS_CONTENT"
                ).forEach { category ->
                    addJsonObject {
                        put("category", category)
                        put("threshold", "BLOCK_ONLY_HIGH")
                    }
                }
            }
        }
        return json.toString()
    }

    fun extractText(responseBody: String): String {
        return try {
            val json = Json.parseToJsonElement(responseBody).jsonObject
            val candidates = json["candidates"]?.jsonArray ?: return ""
            if (candidates.isEmpty()) return ""
            
            val firstCandidate = candidates[0].jsonObject
            val content = firstCandidate["content"]?.jsonObject ?: return ""
            val parts = content["parts"]?.jsonArray ?: return ""
            if (parts.isEmpty()) return ""
            
            val result = StringBuilder()
            for (part in parts) {
                val partObj = part.jsonObject
                val isThought = partObj["thought"]?.jsonPrimitive?.booleanOrNull == true
                if (!isThought) {
                    val text = partObj["text"]?.jsonPrimitive?.content
                    if (!text.isNullOrEmpty()) {
                        result.append(text)
                    }
                }
            }
            result.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting text from response", e)
            ""
        }
    }
}
