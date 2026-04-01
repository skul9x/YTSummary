package com.skul9x.ytsummary.manager

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Manager for storing and retrieving Gemini API keys.
 * Uses EncryptedSharedPreferences for secure storage.
 */
class ApiKeyManager private constructor(context: Context) {

    companion object {
        private const val PREFS_NAME = "api_keys_secure_ytsub"
        private const val KEY_API_KEYS = "gemini_api_keys"
        
        @Volatile
        private var instance: ApiKeyManager? = null
        
        fun getInstance(context: Context): ApiKeyManager {
            return instance ?: synchronized(this) {
                instance ?: ApiKeyManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val prefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            context.deleteSharedPreferences(PREFS_NAME)
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
                
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }

    fun getApiKeys(): List<String> {
        val json = prefs.getString(KEY_API_KEYS, null) ?: return emptyList()
        return try {
            Json.decodeFromString<List<String>>(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addApiKey(apiKey: String): Boolean {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.length < 30 || !trimmedKey.startsWith("AIza")) {
            return false
        }
        val currentKeys = getApiKeys().toMutableList()
        if (currentKeys.contains(trimmedKey)) {
            return false
        }
        currentKeys.add(trimmedKey)
        saveKeys(currentKeys)
        return true
    }

    fun removeApiKey(index: Int): Boolean {
        val currentKeys = getApiKeys().toMutableList()
        if (index < 0 || index >= currentKeys.size) {
            return false
        }
        currentKeys.removeAt(index)
        saveKeys(currentKeys)
        return true
    }

    fun hasApiKeys(): Boolean = getApiKeys().isNotEmpty()

    private fun saveKeys(keys: List<String>) {
        val json = Json.encodeToString(keys)
        prefs.edit().putString(KEY_API_KEYS, json).apply()
    }

    fun maskApiKey(apiKey: String): String {
        if (apiKey.length <= 12) return "****"
        return "${apiKey.take(8)}...${apiKey.takeLast(4)}"
    }
}
