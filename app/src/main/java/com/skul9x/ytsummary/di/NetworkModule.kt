package com.skul9x.ytsummary.di

import android.content.Context
import com.skul9x.ytsummary.BuildConfig
import com.skul9x.ytsummary.network.RetryInterceptor
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * Singleton object cung cấp Retrofit client và API instance.
 */
object NetworkModule {

    private const val TIMEOUT_SECONDS = 60L
    private var context: Context? = null

    /**
     * Khởi tạo context cho NetworkModule để bật các tính năng như Cache. (Phase 04)
     */
    fun initialize(ctx: Context) {
        context = ctx.applicationContext
    }

    val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .addInterceptor(RetryInterceptor()) // Phase 04: Thử lại đường truyền chập chờn
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            })
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .connectionPool(ConnectionPool(10, 5, TimeUnit.MINUTES)) // Phase 04: Pool tối ưu

        // Bật cache nếu có context
        context?.let {
            try {
                val cacheSize = 10 * 1024 * 1024L // 10 MiB
                builder.cache(Cache(it.cacheDir, cacheSize))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        builder.build()
    }
}
