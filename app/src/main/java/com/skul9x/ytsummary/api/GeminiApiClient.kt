package com.skul9x.ytsummary.api

import android.util.Log
import com.skul9x.ytsummary.api.gemini.GeminiPrompts
import com.skul9x.ytsummary.api.gemini.GeminiResponseHelper
import com.skul9x.ytsummary.di.NetworkModule
import com.skul9x.ytsummary.manager.ApiKeyManager
import com.skul9x.ytsummary.manager.ModelManager
import com.skul9x.ytsummary.manager.ModelQuotaManager
import com.skul9x.ytsummary.model.AiResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.UnknownHostException
import java.net.ConnectException

/**
 * Gemini API client with Model-First key rotation strategy.
 */
class GeminiApiClient(
    private val apiKeyManager: ApiKeyManager,
    private val quotaManager: ModelQuotaManager,
    private val modelManager: ModelManager,
    private val client: OkHttpClient = NetworkModule.geminiOkHttpClient,
    private val baseUrl: String = BASE_URL,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

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

        val activeModels = modelManager.getModels()

        for (model in activeModels) {
            for (apiKey in keys) {
                if (!quotaManager.isAvailable(model, apiKey)) continue

                val accumulatedText = StringBuilder()
                var hasStarted = false
                
                try {
                    com.skul9x.ytsummary.util.retryWithBackoff(
                        maxRetries = 2,
                        tag = TAG,
                        shouldRetry = { it is IOException && !hasStarted } // FIX 2: CHỈ retry nếu CHƯA bắt đầu stream text để tránh rác UI
                    ) {
                        val request = Request.Builder()
                            .url("$baseUrl/$model:streamGenerateContent?alt=sse")
                            .header("x-goog-api-key", apiKey)
                            .post(requestBodyJson.toRequestBody("application/json".toMediaType()))
                            .build()

                        client.newCall(request).execute().use { response ->
                            if (!response.isSuccessful) {
                                // Đọc body lỗi để phân tích chi tiết
                                val errorBody = response.body?.string() ?: ""
                                
                                when (response.code) {
                                    429 -> {
                                        // FIX 1: Phân biệt Rate Limit (15 RPM) và Daily Quota
                                        if (errorBody.contains("per minute", ignoreCase = true) || 
                                            errorBody.contains("Rate limit", ignoreCase = true)) {
                                            Log.w(TAG, "Rate limit (RPM) hit for $model with key ...${apiKey.takeLast(4)}")
                                            quotaManager.markCooldown(model, apiKey) // Chỉ khóa 5 phút
                                            throw ServerBusyException()
                                        } else {
                                            Log.e(TAG, "Daily Quota exhausted for $model with key ...${apiKey.takeLast(4)}")
                                            quotaManager.markExhausted(model, apiKey) // Khóa 30 tiếng
                                            throw QuotaExceededException()
                                        }
                                    }
                                    503 -> {
                                        quotaManager.markCooldown(model, apiKey)
                                        throw ServerBusyException()
                                    }
                                    400, 404 -> {
                                        // FIX 3: Prompt quá dài hoặc Model không tồn tại -> Quăng exception riêng để nhảy Model
                                        throw ModelUnavailableException("Model $model unsupported or bad request: $errorBody")
                                    }
                                    else -> throw Exception("HTTP ${response.code}: $errorBody")
                                }
                            }

                            response.body?.source()?.let { source ->
                                while (!source.exhausted()) {
                                    val line = source.readUtf8Line() ?: break
                                    if (line.startsWith("data: ")) {
                                        val data = line.substring(6)
                                        val textChunk = GeminiResponseHelper.extractText(data)
                                        if (textChunk.isNotEmpty()) {
                                            accumulatedText.append(textChunk)
                                            emit(AiResult.DeltaSuccess(textChunk, model))
                                            hasStarted = true
                                        }
                                    }
                                }
                                if (hasStarted) {
                                    emit(AiResult.Success(accumulatedText.toString(), model))
                                }
                            }
                        }
                    }

                    if (hasStarted) return@flow

                } catch (e: QuotaExceededException) {
                    continue // Thử Key tiếp theo
                } catch (e: ServerBusyException) {
                    delay(300L)
                    continue // Thử Key tiếp theo
                } catch (e: ModelUnavailableException) {
                    Log.e(TAG, "Skipping model $model due to client/model error: ${e.message}")
                    break // THOÁT VÒNG LẶP KEY, CHUYỂN NGAY SANG MODEL TIẾP THEO
                } catch (e: UnknownHostException) {
                    Log.e(TAG, "Mất mạng vật lý: ${e.message}")
                    emit(AiResult.Error("Không có kết nối mạng. Vui lòng kiểm tra lại đường truyền."))
                    return@flow
                } catch (e: ConnectException) {
                    Log.e(TAG, "Không thể kết nối đến server: ${e.message}")
                    emit(AiResult.Error("Không có kết nối mạng. Vui lòng kiểm tra lại đường truyền."))
                    return@flow
                } catch (e: Exception) {
                    Log.e(TAG, "Error with $model: ${e.message}")
                    if (hasStarted) {
                        // Nếu mạng đứt khi đang stream dở, tuyệt đối không âm thầm nhảy sang Key khác
                        // vì luồng text cũ trên UI đã được emit. Chuyển key sẽ gây lỗi ghép nối văn bản.
                        emit(AiResult.Error("Kết nối bị gián đoạn giữa chừng. Vui lòng thử lại."))
                        return@flow 
                    }
                    continue // Nếu chưa stream gì cả, an toàn để thử Key tiếp theo
                }
            }
        }

        emit(AiResult.AllQuotaExhausted)
    }.flowOn(ioDispatcher)

    // Khai báo thêm Exception mới để điều hướng luồng
    private class QuotaExceededException : Exception()
    private class ServerBusyException : Exception()
    private class ModelUnavailableException(message: String) : Exception(message)

    companion object {
        private const val TAG = "GeminiApiClient"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta"
    }
}

