package com.skul9x.ytsummary.api

import android.content.Context
import android.util.Log
import com.skul9x.ytsummary.api.gemini.GeminiPrompts
import com.skul9x.ytsummary.api.gemini.GeminiResponseHelper
import com.skul9x.ytsummary.di.NetworkModule
import com.skul9x.ytsummary.manager.ApiKeyManager
import com.skul9x.ytsummary.manager.ModelQuotaManager
import com.skul9x.ytsummary.model.AiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

/**
 * Gemini API client with Model-First key rotation strategy.
 */
class GeminiApiClient(
    private val apiKeyManager: ApiKeyManager,
    private val quotaManager: ModelQuotaManager,
    private val client: OkHttpClient = NetworkModule.okHttpClient,
    private val baseUrl: String = BASE_URL
) {

    /**
     * Tóm tắt transcript với cơ chế xoay tua Key.
     */
    /**
     * Tóm tắt transcript với cơ chế xoay tua Key và Streaming (SSE).
     */
    fun summarize(transcript: String): Flow<AiResult> = flow {
        val keys = apiKeyManager.getApiKeys()
        if (keys.isEmpty()) {
            emit(AiResult.NoApiKeys)
            return@flow
        }

        val prompt = GeminiPrompts.buildSummarizationPrompt(transcript)
        val requestBodyJson = GeminiResponseHelper.buildRequestBody(prompt)
        Log.d(TAG, "Starting streaming rotation...")

        for (model in MODELS) {
            for (apiKey in keys) {
                if (!quotaManager.isAvailable(model, apiKey)) continue

                val accumulatedText = StringBuilder()
                var hasStarted = false
                
                try {
                    val request = Request.Builder()
                        .url("$BASE_URL/$model:streamGenerateContent?alt=sse")
                        .header("x-goog-api-key", apiKey)
                        .post(requestBodyJson.toRequestBody("application/json".toMediaType()))
                        .build()

                    val response = client.newCall(request).execute()
                    
                    if (!response.isSuccessful) {
                        response.use { resp ->
                            when (resp.code) {
                                429 -> {
                                    quotaManager.markExhausted(model, apiKey)
                                    throw QuotaExceededException()
                                }
                                503 -> {
                                    quotaManager.markCooldown(model, apiKey)
                                    throw ServerBusyException()
                                }
                                else -> throw Exception("HTTP ${resp.code}")
                            }
                        }
                    }

                    response.use { resp ->
                        resp.body?.source()?.let { source ->
                            while (!source.exhausted()) {
                                val line = source.readUtf8Line() ?: break
                                if (line.startsWith("data: ")) {
                                    val data = line.substring(6)
                                    val textChunk = GeminiResponseHelper.extractText(data)
                                    if (textChunk.isNotEmpty()) {
                                        accumulatedText.append(textChunk)
                                        emit(AiResult.Success(accumulatedText.toString(), model))
                                        hasStarted = true
                                    }
                                }
                            }
                        }
                    }
                    
                    if (hasStarted) return@flow
                    
                } catch (e: QuotaExceededException) {
                    continue
                } catch (e: ServerBusyException) {
                    continue
                } catch (e: Exception) {
                    Log.e(TAG, "Error with $model: ${e.message}")
                    continue
                }
            }
        }

        emit(AiResult.AllQuotaExhausted)
    }.flowOn(Dispatchers.IO)

    private class QuotaExceededException : Exception()
    private class ServerBusyException : Exception()


    companion object {
        private const val TAG = "GeminiApiClient"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta"

        val MODELS = listOf(
            "models/gemini-2.0-flash",       // Tối ưu nhất về tốc độ (No Thinking)
            "models/gemini-2.5-flash",       // Mạnh mẽ, tóm tắt sâu (Thinking disabled)
            "models/gemini-2.5-flash-lite", 
            "models/gemini-2.0-flash-lite",
            "models/gemini-3-flash-preview"  // Dự phòng cuối cùng
        )
    }
}
