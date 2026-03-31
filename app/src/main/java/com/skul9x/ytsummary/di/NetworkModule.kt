package com.skul9x.ytsummary.di

import android.content.Context
import com.skul9x.ytsummary.BuildConfig
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * Singleton object cung cấp Retrofit client và API instance.
 */
object NetworkModule {

    private const val CONNECT_TIMEOUT = 15L
    private const val DEFAULT_READ_TIMEOUT = 15L
    private const val GEMINI_READ_TIMEOUT = 90L // Stream long timeout

    private var context: Context? = null
    fun initialize(ctx: Context) { context = ctx.applicationContext }

    // Client chung chia sẻ tài nguyên (ConnectionPool, Cache, Interceptors)
    private val baseOkHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            })
            .connectionPool(ConnectionPool(10, 5, TimeUnit.MINUTES))
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)

        context?.let {
            try {
                val cacheSize = 10 * 1024 * 1024L // 10 MiB
                builder.cache(Cache(it.cacheDir, cacheSize))
            } catch (e: Exception) { e.printStackTrace() }
        }
        builder.build()
    }

    /**
     * OkHttpClient mặc định với timeout ngắn (15s) cho Metadata và Transcript.
     */
    val okHttpClient: OkHttpClient by lazy {
        baseOkHttpClient.newBuilder()
            .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    /**
     * OkHttpClient riêng với timeout dài (90s) cho Gemini Stream.
     */
    val geminiOkHttpClient: OkHttpClient by lazy {
        baseOkHttpClient.newBuilder()
            .readTimeout(GEMINI_READ_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }
}
