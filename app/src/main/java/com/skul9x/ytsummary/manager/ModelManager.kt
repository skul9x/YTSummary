package com.skul9x.ytsummary.manager

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Manager for storing and retrieving Gemini model priority list.
 */
class ModelManager private constructor(context: Context) {

    companion object {
        private const val PREFS_NAME = "model_config_ytsub"
        private const val KEY_MODELS = "gemini_models_priority"

        val DEFAULT_MODELS = listOf(
            "models/gemini-3.1-flash-lite-preview",
            "models/gemini-3-flash-preview",
            "models/gemini-2.5-flash-lite",
            "models/gemini-2.5-flash"
        )

        @Volatile
        private var instance: ModelManager? = null

        fun getInstance(context: Context): ModelManager {
            return instance ?: synchronized(this) {
                instance ?: ModelManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Trả về danh sách model. Nếu trống hoặc chưa có data -> trả DEFAULT_MODELS.
     */
    fun getModels(): List<String> {
        val json = prefs.getString(KEY_MODELS, null) ?: return DEFAULT_MODELS
        return try {
            val models = Json.decodeFromString<List<String>>(json)
            if (models.isEmpty()) DEFAULT_MODELS else models
        } catch (e: Exception) {
            DEFAULT_MODELS
        }
    }

    /**
     * Lưu danh sách model.
     */
    fun saveModels(models: List<String>) {
        val json = Json.encodeToString(models)
        prefs.edit().putString(KEY_MODELS, json).apply()
    }

    /**
     * Thêm model mới vào cuối danh sách.
     */
    fun addModel(modelName: String): Boolean {
        val trimmed = modelName.trim()
        if (trimmed.isEmpty()) return false
        
        val currentModels = getModels().toMutableList()
        if (currentModels.contains(trimmed)) return false
        
        currentModels.add(trimmed)
        saveModels(currentModels)
        return true
    }

    /**
     * Xóa model tại vị trí index.
     */
    fun removeModel(index: Int): Boolean {
        val currentModels = getModels().toMutableList()
        if (index < 0 || index >= currentModels.size) return false
        
        currentModels.removeAt(index)
        saveModels(currentModels)
        return true
    }

    /**
     * Di chuyển model lên trên (giảm index).
     */
    fun moveUp(index: Int): Boolean {
        if (index <= 0) return false
        val currentModels = getModels().toMutableList()
        if (index >= currentModels.size) return false
        
        val item = currentModels.removeAt(index)
        currentModels.add(index - 1, item)
        saveModels(currentModels)
        return true
    }

    /**
     * Di chuyển model xuống dưới (tăng index).
     */
    fun moveDown(index: Int): Boolean {
        val currentModels = getModels().toMutableList()
        if (index < 0 || index >= currentModels.size - 1) return false
        
        val item = currentModels.removeAt(index)
        currentModels.add(index + 1, item)
        saveModels(currentModels)
        return true
    }

    /**
     * Khôi phục cài đặt gốc.
     */
    fun resetToDefaults() {
        prefs.edit().remove(KEY_MODELS).apply()
    }

    /**
     * Kiểm tra xem đã có cấu hình tùy chỉnh chưa.
     */
    fun isEmpty(): Boolean {
        return !prefs.contains(KEY_MODELS)
    }
}
