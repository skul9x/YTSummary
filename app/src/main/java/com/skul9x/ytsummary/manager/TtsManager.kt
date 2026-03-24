package com.skul9x.ytsummary.manager

import android.content.Context
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

/**
 * TtsManager handles Text-to-Speech logic and system audio volume management.
 * 
 * Responsibilities:
 * - Initialize TTS engine with fallback support.
 * - Manage system music volume (auto-set to 80% on demand).
 * - Filter Markdown syntax to provide clean text for speech.
 */
class TtsManager(private val context: Context, private val onInitSuccess: () -> Unit = {}) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Priority: Vietnamese -> System Default -> US English
            val vnLocale = Locale("vi", "VN")
            val result = tts?.setLanguage(vnLocale)
            
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w("TtsManager", "Vietnamese not supported, falling back to system default.")
                tts?.language = Locale.getDefault()
            }
            
            isInitialized = true
            onInitSuccess()
        } else {
            Log.e("TtsManager", "Initialization failed with status: $status")
        }
    }

    /**
     * Set system music volume to a specific percentage.
     * Default is 80% as per requirements.
     */
    fun setVolume(percentage: Int = 80) {
        try {
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val targetVolume = (maxVolume * percentage) / 100
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0)
            Log.d("TtsManager", "Volume set to $percentage% ($targetVolume/$maxVolume)")
        } catch (e: Exception) {
            Log.e("TtsManager", "Failed to set volume", e)
        }
    }

    companion object {
        private val REGEX_MARKDOWN_SYMBOLS = Regex("[#*`~>_-]")
        private val REGEX_LINKS = Regex("\\[(.*?)\\]\\(.*?\\)")
        private val REGEX_NUMBERED_LISTS = Regex("\\d+\\.\\s+")
        private val REGEX_WHITESPACE = Regex("\\s+")
    }

    /**
     * Strips Markdown characters to prevent TTS from reading raw symbols.
     * E.g., "**Important**" -> "Important"
     */
    fun cleanMarkdown(text: String): String {
        return text
            .replace(REGEX_MARKDOWN_SYMBOLS, " ") // Remove basic Markdown symbols
            .replace(REGEX_LINKS, "$1") // Simplify links: [Text](URL) -> Text
            .replace(REGEX_NUMBERED_LISTS, "") // Remove numbered list prefixes
            .replace(REGEX_WHITESPACE, " ") // Clean up whitespace
            .trim()
    }

    fun speak(text: String) {
        if (!isInitialized) {
            Log.w("TtsManager", "TTS not initialized yet")
            return
        }
        
        val cleanedText = cleanMarkdown(text)
        if (cleanedText.isEmpty()) return
        
        // Use QUEUE_FLUSH to interrupt any ongoing speech
        tts?.speak(cleanedText, TextToSpeech.QUEUE_FLUSH, null, "SummaryTTS_ID")
    }

    fun stop() {
        if (isInitialized) {
            tts?.stop()
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
