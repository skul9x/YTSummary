package com.skul9x.ytsummary.manager

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * TtsManager handles Text-to-Speech logic and system audio volume management.
 * 
 * Responsibilities:
 * - Initialize TTS engine with fallback support.
 * - Manage system music volume (auto-set to 80% on demand).
 * - Filter Markdown syntax to provide clean text for speech.
 */
class TtsManager(
    private val context: Context,
    private val onInitSuccess: () -> Unit = {},
    private val onTtsDone: () -> Unit = {}
) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var focusRequest: AudioFocusRequest? = null

    var currentIndex = 0
        private set
    private var totalSpokenLength = 0
    private var currentChunkLength = 0
    private val pendingUtterances = AtomicInteger(0)

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
            
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}

                override fun onDone(utteranceId: String?) {
                    // Cộng dồn độ dài chunk vừa đọc xong vào tổng
                    totalSpokenLength += currentChunkLength
                    currentIndex = 0
                    currentChunkLength = 0
                    // Giảm counter và chỉ gọi callback khi tất cả chunk đã đọc xong
                    if (pendingUtterances.decrementAndGet() <= 0) {
                        pendingUtterances.set(0) // Safety: không cho âm
                        abandonAudioFocus()
                        onTtsDone()
                    }
                }

                @Deprecated("Deprecated in Java", ReplaceWith("Unit"))
                override fun onError(utteranceId: String?) {}

                override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                    super.onRangeStart(utteranceId, start, end, frame)
                    currentIndex = start
                }
            })
            
            isInitialized = true
            onInitSuccess()
        } else {
            Log.e("TtsManager", "Initialization failed with status: $status")
        }
    }

    /**
     * Request Audio Focus before TTS starts.
     * AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK -> system automatically lowers other apps' volume.
     */
    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANT)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .build()
            focusRequest?.let { audioManager.requestAudioFocus(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        }
    }

    /**
     * Relinquish Audio Focus when TTS stops.
     */
    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
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

    fun speak(text: String, fromIndex: Int = 0) {
        if (!isInitialized) {
            Log.w("TtsManager", "TTS not initialized yet")
            return
        }
        
        requestAudioFocus()
        val cleanedText = cleanMarkdown(text)
        if (cleanedText.isEmpty() || fromIndex >= cleanedText.length) return
        
        // Reset tracking khi bắt đầu speak mới
        totalSpokenLength = fromIndex
        val textToSpeak = if (fromIndex > 0) cleanedText.substring(fromIndex) else cleanedText
        currentChunkLength = textToSpeak.length
        pendingUtterances.set(1)
        // Use QUEUE_FLUSH to interrupt any ongoing speech
        tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "SummaryTTS_ID")
    }

    /**
     * Add a chunk of text to the speech queue. 
     * Use this for SSE streaming to start reading as soon as the first sentence is ready.
     */
    fun speakChunk(textChunk: String) {
        if (!isInitialized || textChunk.isBlank()) return
        requestAudioFocus()
        val cleaned = cleanMarkdown(textChunk)
        if (cleaned.isBlank()) return
        currentChunkLength = cleaned.length
        pendingUtterances.incrementAndGet()
        tts?.speak(cleaned, TextToSpeech.QUEUE_ADD, null, UUID.randomUUID().toString())
    }

    /**
     * Pause TTS và trả về vị trí tuyệt đối trong text đã clean.
     * Vị trí = tổng các chunk đã đọc xong + vị trí hiện tại trong chunk đang đọc.
     */
    fun pause(): Int {
        val absolutePosition = totalSpokenLength + currentIndex
        if (isInitialized) {
            tts?.stop()
            pendingUtterances.set(0)
            abandonAudioFocus()
        }
        return absolutePosition
    }

    fun stop() {
        if (isInitialized) {
            tts?.stop()
            pendingUtterances.set(0) // Clear counter khi user chủ động dừng
            totalSpokenLength = 0
            currentIndex = 0
            currentChunkLength = 0
            abandonAudioFocus()
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
