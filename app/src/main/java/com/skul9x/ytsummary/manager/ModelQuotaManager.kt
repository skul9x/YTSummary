package com.skul9x.ytsummary.manager

import android.content.Context
import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Manages quota and availability status for (Model + API Key) combinations.
 * Strategies:
 * - 429 (Quota Exceeded): Ban combo for 30 hours. Persisted to disk.
 * - 503 (Server Busy): Cooldown combo for 5 minutes. In-memory only.
 */
class ModelQuotaManager private constructor(context: Context) {

    companion object {
        private const val TAG = "ModelQuotaManager"
        private const val PREFS_NAME = "model_quota_ytsub"
        private const val KEY_EXHAUSTED = "exhausted_combos"

        private const val EXHAUSTED_DURATION_MS = 30 * 60 * 60 * 1000L // 30 hours
        private const val COOLDOWN_DURATION_MS = 5 * 60 * 1000L      // 5 minutes

        @Volatile
        private var instance: ModelQuotaManager? = null

        fun getInstance(context: Context): ModelQuotaManager {
            return instance ?: synchronized(this) {
                instance ?: ModelQuotaManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val mutex = Mutex()
    
    private val exhaustedMap = mutableMapOf<String, Long>()
    private val cooldownMap = mutableMapOf<String, Long>()
    private val apiKeyHashMap = mutableMapOf<String, String>()

    init {
        loadExhaustedMap()
    }

    private fun loadExhaustedMap() {
        val json = prefs.getString(KEY_EXHAUSTED, null)
        if (json != null) {
            try {
                val loaded: Map<String, Long> = Json.decodeFromString(json)
                val now = System.currentTimeMillis()
                exhaustedMap.putAll(loaded.filter { it.value > now })
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load exhausted map", e)
            }
        }
    }

    private fun saveExhaustedMap() {
        try {
            val now = System.currentTimeMillis()
            val validEntries = exhaustedMap.filter { it.value > now }
            val json = Json.encodeToString(validEntries)
            prefs.edit().putString(KEY_EXHAUSTED, json).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save exhausted map", e)
        }
    }

    private fun makeKey(model: String, apiKey: String): String {
        val hash = apiKeyHashMap.getOrPut(apiKey) {
            try {
                java.security.MessageDigest.getInstance("SHA-256")
                    .digest(apiKey.toByteArray())
                    .take(8)
                    .joinToString("") { "%02x".format(it) }
            } catch (e: Exception) {
                apiKey.hashCode().toString()
            }
        }
        return "$model::$hash"
    }

    suspend fun markExhausted(model: String, apiKey: String) {
        mutex.withLock {
            val key = makeKey(model, apiKey)
            val expiry = System.currentTimeMillis() + EXHAUSTED_DURATION_MS
            exhaustedMap[key] = expiry
            Log.w(TAG, "Marked EXHAUSTED (30h): $model | Key ...${apiKey.takeLast(4)}")
            saveExhaustedMap()
        }
    }

    suspend fun markCooldown(model: String, apiKey: String) {
        mutex.withLock {
            val key = makeKey(model, apiKey)
            val expiry = System.currentTimeMillis() + COOLDOWN_DURATION_MS
            cooldownMap[key] = expiry
            Log.w(TAG, "Marked COOLDOWN (5m): $model | Key ...${apiKey.takeLast(4)}")
        }
    }

    suspend fun isAvailable(model: String, apiKey: String): Boolean {
        mutex.withLock {
            val key = makeKey(model, apiKey)
            val now = System.currentTimeMillis()

            val exhaustedExpiry = exhaustedMap[key]
            if (exhaustedExpiry != null) {
                if (exhaustedExpiry > now) return false
                else {
                    exhaustedMap.remove(key)
                    saveExhaustedMap()
                }
            }

            val cooldownExpiry = cooldownMap[key]
            if (cooldownExpiry != null) {
                if (cooldownExpiry > now) return false
                else cooldownMap.remove(key)
            }

            return true
        }
    }

    suspend fun clearAll() {
        mutex.withLock {
            exhaustedMap.clear()
            cooldownMap.clear()
            prefs.edit().clear().apply()
        }
    }
}
