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
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
    suspend fun summarize(transcript: String): AiResult = withContext(Dispatchers.IO) {
        val keys = apiKeyManager.getApiKeys()
        if (keys.isEmpty()) return@withContext AiResult.NoApiKeys

        // ✅ TỐI ƯU: Build Prompt và JSON Body một lần duy nhất trước khi lặp (O(1) optimization)
        val prompt = GeminiPrompts.buildSummarizationPrompt(transcript)
        val requestBodyJson = GeminiResponseHelper.buildRequestBody(prompt)
        Log.d(TAG, "Pre-built JSON ready (Size: ${requestBodyJson.length} chars). Starting rotation...")

        // MODEL-FIRST Strategy: Thử cùng 1 model trên tất cả các Key trước khi đổi model
        for (model in MODELS) {
            for (apiKey in keys) {
                if (!quotaManager.isAvailable(model, apiKey)) continue

                Log.d(TAG, "🔄 Trying Frontier model: $model | Key: ...${apiKey.takeLast(4)}")
                val result = tryGenerateContent(model, apiKey, requestBodyJson)

                when (result) {
                    is AiResult.Success -> {
                        Log.i(TAG, "✅ Success! Summarized by $model")
                        return@withContext result
                    }
                    is AiResult.QuotaExceeded -> {
                        Log.w(TAG, "⚠️ Quota Exceeded for $model (Key: ...${apiKey.takeLast(4)})")
                        quotaManager.markExhausted(model, apiKey)
                        continue
                    }
                    is AiResult.ServerBusy -> {
                        Log.w(TAG, "⏳ Server Busy (503) for $model. Cooling down...")
                        quotaManager.markCooldown(model, apiKey)
                        continue
                    }
                    else -> {
                        if (result is AiResult.Error) {
                            Log.e("GeminiApiClient", "Error with $model: ${result.message}")
                        }
                        continue
                    }
                }
            }
        }

        AiResult.AllQuotaExhausted
    }

    private suspend fun tryGenerateContent(model: String, apiKey: String, requestBodyJson: String): AiResult {
        val request = Request.Builder()
            .url("$BASE_URL/$model:generateContent")
            .header("x-goog-api-key", apiKey)
            .post(requestBodyJson.toRequestBody("application/json".toMediaType()))
            .build()

        return try {
            val response = executeRequest(request)
            response.use { resp ->
                val body = resp.body?.string() ?: ""
                when (resp.code) {
                    200 -> {
                        val text = GeminiResponseHelper.extractText(body)
                        if (text.isNotBlank()) AiResult.Success(text, model)
                        else AiResult.Error("Phản hồi rỗng từ Gemini")
                    }
                    429 -> AiResult.QuotaExceeded
                    503 -> AiResult.ServerBusy
                    else -> AiResult.Error("HTTP ${resp.code}: $body")
                }
            }
        } catch (e: Exception) {
            AiResult.Error(e.message ?: "Lỗi kết nối")
        }
    }

    private suspend fun executeRequest(request: Request): Response = suspendCancellableCoroutine { cont ->
        val call = client.newCall(request)
        cont.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (!cont.isCompleted) cont.resumeWithException(e)
            }
            override fun onResponse(call: Call, response: Response) {
                if (!cont.isCompleted) cont.resume(response)
            }
        })
    }

    companion object {
        private const val TAG = "GeminiApiClient"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta"

        val MODELS = listOf(
            "models/gemini-2.5-flash",       // Sáng giá nhất cho tóm tắt nội dung dài
            "models/gemini-2.5-flash-lite",
            "models/gemini-2.0-flash",
            "models/gemini-2.0-flash-lite",
            "models/gemini-3-flash-preview"  // Dự phòng cuối cùng
        )
    }
}
