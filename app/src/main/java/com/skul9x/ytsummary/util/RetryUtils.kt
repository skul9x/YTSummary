package com.skul9x.ytsummary.util

import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.retryWhen
import kotlin.math.pow

/**
 * Utility function to retry a block of code with exponential backoff.
 * Non-blocking because it uses coroutine [delay].
 *
 * [maxRetries] is the number of ADDITIONAL attempts after the first failure.
 */
suspend fun <T> retryWithBackoff(
    maxRetries: Int = 3,
    initialDelayMillis: Long = 1000,
    maxDelayMillis: Long = 4000,
    factor: Double = 2.0,
    tag: String = "RetryUtils",
    shouldRetry: (Throwable) -> Boolean = { it is java.io.IOException },
    block: suspend () -> T
): T {
    var currentDelay = initialDelayMillis
    repeat(maxRetries) { attempt ->
        try {
            return block()
        } catch (e: Exception) {
            if (!shouldRetry(e)) {
                throw e
            }
            Log.w(tag, "Attempt ${attempt + 1} failed: ${e.message}. Retrying in ${currentDelay}ms...")
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMillis)
        }
    }
    return block() // Final attempt (maxRetries + 1)
}

/**
 * Extension function for Flow to handle retries with backoff.
 */
fun <T> kotlinx.coroutines.flow.Flow<T>.retryWithBackoff(
    maxRetries: Int = 3,
    initialDelayMillis: Long = 1000,
    shouldRetry: (Throwable) -> Boolean = { it is java.io.IOException }
): kotlinx.coroutines.flow.Flow<T> = this.retryWhen { cause, attempt ->
    if (attempt < maxRetries && shouldRetry(cause)) {
        val waitTime = (2.0.pow(attempt.toDouble()) * initialDelayMillis).toLong()
        delay(waitTime)
        true
    } else {
        false
    }
}
