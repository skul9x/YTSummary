package com.skul9x.ytsummary.manager

import android.content.Context
import android.content.SharedPreferences
import com.skul9x.ytsummary.di.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject
import com.chaquo.python.Python

/**
 * Kiểm tra cập nhật thư viện Python youtube-transcript-api.
 * - Đọc version local từ Chaquopy runtime (không hardcode).
 * - So sánh với version mới nhất trên PyPI.
 * - Cache kết quả 24h để tránh gọi API quá nhiều.
 */
object PythonUpdateChecker {

    internal var PYPI_URL = "https://pypi.org/pypi/youtube-transcript-api/json"
    private const val PACKAGE_NAME = "youtube-transcript-api"
    private const val PREFS_NAME = "python_update_prefs"
    private const val KEY_CACHED_LATEST = "cached_latest_version"
    private const val KEY_CACHE_TIME = "cache_timestamp"
    private const val CACHE_DURATION_MS = 24 * 60 * 60 * 1000L // 24 giờ

    data class UpdateInfo(
        val currentVersion: String,
        val latestVersion: String
    )

    /**
     * @return UpdateInfo nếu có bản mới, null nếu đã là bản mới nhất hoặc lỗi.
     */
    suspend fun checkForUpdate(context: Context): UpdateInfo? {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Đọc version đang cài trong APK
                val currentVersion = getInstalledVersion() ?: return@withContext null

                // 2. Kiểm tra cache trước
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val cachedVersion = getCachedVersion(prefs)
                if (cachedVersion != null) {
                    return@withContext if (cachedVersion != currentVersion) {
                        UpdateInfo(currentVersion, cachedVersion)
                    } else null
                }

                // 3. Gọi PyPI API (dùng OkHttpClient singleton từ NetworkModule)
                val latestVersion = fetchLatestFromPyPI() ?: return@withContext null

                // 4. Lưu cache
                saveCachedVersion(prefs, latestVersion)

                // 5. So sánh
                if (latestVersion != currentVersion) {
                    UpdateInfo(currentVersion, latestVersion)
                } else null

            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    internal var testVersion: String? = null

    /**
     * Đọc version thực sự từ Python runtime (Chaquopy).
     * Dùng importlib.metadata.version() - standard library Python 3.8+.
     */
    internal fun getInstalledVersion(): String? {
        testVersion?.let { return it }
        return try {
            if (!Python.isStarted()) return null
            val py = Python.getInstance()
            val metadata = py.getModule("importlib.metadata")
            metadata.callAttr("version", PACKAGE_NAME).toString()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Gọi PyPI JSON API.
     * Dùng NetworkModule.okHttpClient (singleton, đã có timeout 30s).
     *
     * Response format: { "info": { "version": "0.6.3", ... }, ... }
     */
    private fun fetchLatestFromPyPI(): String? {
        val request = Request.Builder()
            .url(PYPI_URL)
            .build()

        NetworkModule.okHttpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return null
                return JSONObject(body)
                    .getJSONObject("info")
                    .getString("version")
            }
        }
        return null
    }

    // --- Cache helpers ---

    private fun getCachedVersion(prefs: SharedPreferences): String? {
        val cacheTime = prefs.getLong(KEY_CACHE_TIME, 0)
        if (System.currentTimeMillis() - cacheTime > CACHE_DURATION_MS) return null
        return prefs.getString(KEY_CACHED_LATEST, null)
    }

    private fun saveCachedVersion(prefs: SharedPreferences, version: String) {
        prefs.edit()
            .putString(KEY_CACHED_LATEST, version)
            .putLong(KEY_CACHE_TIME, System.currentTimeMillis())
            .apply()
    }

    /**
     * Xóa cache (dùng khi user muốn force refresh).
     */
    fun clearCache(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().clear().apply()
    }
}
