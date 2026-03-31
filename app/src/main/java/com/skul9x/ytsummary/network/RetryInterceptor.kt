package com.skul9x.ytsummary.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import kotlin.math.pow

/**
 * Interceptor tự động retry network khi gặp lỗi đường truyền (IOException)
 * hoặc lỗi server tạm thời (5xx) với thuật toán Exponential Backoff.
 * Tối đa 3 lần thử lại (1s -> 2s -> 4s).
 *
 * Nếu là lỗi client (4xx, bao gồm 429), interceptor sẽ trả về ngay để layer cao hơn xử lý
 * (ví dụ GeminiApiClient đổi model/key).
 */
class RetryInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var retryCount = 0
        var exception: IOException? = null
        
        while (retryCount <= MAX_RETRY) {
            try {
                if (retryCount > 0) {
                    val waitTime = (2.0.pow(retryCount - 1) * 1000).toLong()
                    Log.d(TAG, "Retry #$retryCount for ${request.url} after ${waitTime}ms...")
                    Thread.sleep(waitTime)
                }
                
                val response = chain.proceed(request)
                
                // Nếu thành công hoặc là lỗi Client (4xx) -> trả về ngay
                if (response.isSuccessful || (response.code in 400..499)) {
                    return response
                }
                
                // Nếu là lỗi Server (5xx) -> Thử lại
                if (response.code in 500..599) {
                    Log.w(TAG, "Server error ${response.code}, retrying...")
                    response.close()
                    retryCount++
                    continue
                }
                
                return response
                
            } catch (e: IOException) {
                Log.e(TAG, "Network error: ${e.message}, retrying...")
                exception = e
                retryCount++
            } catch (e: Exception) {
                // Các lỗi không phục hồi được (InterruptedException, v.v)
                throw e
            }
        }
        
        // Nếu đã hết số lần retry mà vẫn lỗi đường truyền
        if (exception != null) throw exception
        
        // Fallback (thường không rơi vào đây)
        throw IOException("Failed after $MAX_RETRY retries")
    }

    companion object {
        private const val TAG = "RetryInterceptor"
        private const val MAX_RETRY = 3
    }
}
