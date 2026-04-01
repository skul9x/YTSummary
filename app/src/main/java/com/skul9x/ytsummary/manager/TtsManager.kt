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
    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                Log.d("TtsManager", "Audio focus LOST (code: $focusChange). Stopping TTS.")
                stop()
                onTtsDone()
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                Log.d("TtsManager", "Audio focus GAINED.")
            }
        }
    }

    private data class TtsProgress(
        var totalSpokenLength: Int = 0,
        var currentIndex: Int = 0,
        var activeUtteranceId: String? = null
    )
    private val progress = TtsProgress()
    
    // id -> absolute offset (where this chunk starts in the cleaned text)
    private val chunkOffsetMap = mutableMapOf<String, Int>()

    val currentIndex: Int get() = progress.currentIndex
    
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
                override fun onStart(utteranceId: String?) {
                    val now = System.currentTimeMillis() % 100000 // Last 5 digits of ms
                    Log.d("TtsManager", "[$now] Utterance START: $utteranceId, pending=${pendingUtterances.get()}")
                    progress.activeUtteranceId = utteranceId
                }

                override fun onDone(utteranceId: String?) {
                    val now = System.currentTimeMillis() % 100000
                    Log.d("TtsManager", "[$now] Utterance DONE: $utteranceId, pending=${pendingUtterances.get()}")
                    
                    // Robust tracking: Use pre-stored offset + length
                    utteranceId?.let { id ->
                        chunkOffsetMap.remove(id)?.let { offset ->
                            // Extract length from ID: "CHUNK|length|id"
                            val parts = id.split("|")
                            if (parts.size >= 2) {
                                val length = parts[1].toIntOrNull() ?: 0
                                progress.totalSpokenLength = offset + length
                                Log.d("TtsManager", "Updated totalSpokenLength to ${progress.totalSpokenLength} using map.")
                            }
                        }
                    }
                    
                    progress.currentIndex = 0
                    if (pendingUtterances.decrementAndGet() <= 0) {
                        pendingUtterances.set(0)
                        abandonAudioFocus()
                        onTtsDone()
                    }
                }

                @Deprecated("Deprecated in Java", ReplaceWith("Unit"))
                override fun onError(utteranceId: String?) {
                    val now = System.currentTimeMillis() % 100000
                    Log.e("TtsManager", "[$now] TTS Error for utterance: $utteranceId")
                    utteranceId?.let { chunkOffsetMap.remove(it) }
                    if (pendingUtterances.decrementAndGet() <= 0) {
                        pendingUtterances.set(0)
                        abandonAudioFocus()
                        onTtsDone()
                    }
                }

                override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                    super.onRangeStart(utteranceId, start, end, frame)
                    val now = System.currentTimeMillis() % 100000
                    Log.v("TtsManager", "[$now] Utterance RANGE: $utteranceId, start=$start")
                    progress.currentIndex = start
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
                .setOnAudioFocusChangeListener(audioFocusChangeListener) // Added Listener
                .build()
            focusRequest?.let { audioManager.requestAudioFocus(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC,
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
        private const val MAX_CHUNK_SIZE = 3500 // Safe margin for Android TTS limit
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
        if (cleanedText.isEmpty() || fromIndex >= cleanedText.length) {
            onTtsDone()
            return
        }
        
        // Reset tracking
        progress.totalSpokenLength = fromIndex
        progress.currentIndex = 0
        pendingUtterances.set(0)
        
        val textToSpeak = if (fromIndex > 0) cleanedText.substring(fromIndex) else cleanedText
        speakInChunks(textToSpeak, true)
    }

    private fun speakInChunks(fullText: String, isFirstChunk: Boolean) {
        if (fullText.isEmpty()) return

        var remainingText = fullText
        var isFirst = isFirstChunk
        var cumulativeOffset = 0

        while (remainingText.isNotEmpty()) {
            var endIndex = minOf(MAX_CHUNK_SIZE, remainingText.length)
            
            // Try to cut at punctuation or newline for natural speech
            if (endIndex < remainingText.length) {
                val chunk = remainingText.substring(0, endIndex)
                val lastPeriod = chunk.lastIndexOf('.')
                val lastComma = chunk.lastIndexOf(',')
                val lastNewline = chunk.lastIndexOf('\n')
                val bestCut = maxOf(lastPeriod, lastComma, lastNewline)
                
                if (bestCut > MAX_CHUNK_SIZE * 0.7) { // Only cut if we found a point in the last 30% of the chunk
                    endIndex = bestCut + 1
                }
            }

            val chunkText = remainingText.substring(0, endIndex)
            remainingText = remainingText.substring(endIndex)

            pendingUtterances.incrementAndGet()
            val queueMode = if (isFirst) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            
            // id format: CHUNK|length|timestamp
            val utteranceId = "CHUNK|${chunkText.length}|${System.currentTimeMillis()}|${UUID.randomUUID()}"
            
            // Store absolute offset in map: initial totalSpokenLength + what we've added in this loop
            chunkOffsetMap[utteranceId] = progress.totalSpokenLength + cumulativeOffset
            
            Log.d("TtsManager", "Enqueueing chunk: length=${chunkText.length}, offset=${chunkOffsetMap[utteranceId]}, queueMode=$queueMode")
            tts?.speak(chunkText, queueMode, null, utteranceId)
            
            cumulativeOffset += chunkText.length
            isFirst = false
        }
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
        
        pendingUtterances.incrementAndGet()
        val utteranceId = "STREAM|${cleaned.length}|${UUID.randomUUID()}"
        
        // For streaming, we record current totalSpokenLength + whatever was enqueued but not finished yet?
        // Actually, streaming is harder because it's additive. 
        // For simplicity, we just use current progress.totalSpokenLength as base.
        chunkOffsetMap[utteranceId] = progress.totalSpokenLength
        
        tts?.speak(cleaned, TextToSpeech.QUEUE_ADD, null, utteranceId)
    }

    /**
     * Pause TTS và trả về vị trí tuyệt đối trong text đã clean.
     * Vị trí = tổng các chunk đã đọc xong + vị trí hiện tại trong chunk đang đọc.
     */
    fun pause(): Int {
        if (!isInitialized) return 0
        
        val isSpeaking = tts?.isSpeaking ?: false
        val posOffset = progress.totalSpokenLength
        val posIndex = progress.currentIndex
        
        // Boundary Guard: Nếu engine báo không nói nhưng còn task đợi, 
        // nghĩa là ta đang ở ranh giới giữa 2 chunk. 
        // Index lúc này nên là posOffset (đầu của chunk kế tiếp).
        val absolutePosition = if (!isSpeaking && pendingUtterances.get() > 0) {
            posOffset
        } else {
            posOffset + posIndex
        }
        
        Log.d("TtsManager", "PAUSE command: abs=$absolutePosition (offset=$posOffset, index=$posIndex, isSpeaking=$isSpeaking, pending=${pendingUtterances.get()})")
        
        tts?.stop()
        pendingUtterances.set(0)
        chunkOffsetMap.clear()
        abandonAudioFocus()
        
        return absolutePosition
    }

    fun stop() {
        if (isInitialized) {
            Log.d("TtsManager", "STOP command: clearing all state.")
            tts?.stop()
            pendingUtterances.set(0)
            chunkOffsetMap.clear()
            progress.totalSpokenLength = 0
            progress.currentIndex = 0
            progress.activeUtteranceId = null
            abandonAudioFocus()
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
