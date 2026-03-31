package com.skul9x.ytsummary.manager

import android.content.Context
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class TtsManagerTest {

    @MockK
    lateinit var mockContext: Context

    @MockK
    lateinit var mockTts: TextToSpeech

    private lateinit var ttsManager: TtsManager
    private lateinit var listener: UtteranceProgressListener

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        
        // Fix for constructor crash: mock AudioManager and TextToSpeech constructor
        val mockAudioManager = mockk<AudioManager>(relaxed = true)
        every { mockContext.getSystemService(Context.AUDIO_SERVICE) } returns mockAudioManager
        
        // Since we can't easily mock the constructor in a simple JUnit test without extra plugins,
        // we'll use reflection to bypass the 'init' block's side effects if possible, 
        // or just let it fail and then overwrite the field.
        // Actually, the 'ClassCastException' was likely due to getSystemService.
        
        try {
            // We need to mock the constructor of TextToSpeech because TtsManager calls it in 'init'
            mockkConstructor(TextToSpeech::class)
            every { anyConstructed<TextToSpeech>().setOnUtteranceProgressListener(any()) } returns 0
            
            ttsManager = TtsManager(mockContext)
        } catch (e: Exception) {
            // If it fails due to Android stub being incomplete, we'll try to create it anyway
            // and then fix fields via reflection.
            // This is a hacky way to test a class not designed for testing.
        }

        // Ensure clean mock injection
        val ttsField = TtsManager::class.java.getDeclaredField("tts")
        ttsField.isAccessible = true
        ttsField.set(ttsManager, mockTts)
        
        val initializedField = TtsManager::class.java.getDeclaredField("isInitialized")
        initializedField.isAccessible = true
        initializedField.set(ttsManager, true)

        // Capture the listener
        val listenerCaptor = slot<UtteranceProgressListener>()
        ttsManager.onInit(TextToSpeech.SUCCESS)
        verify { mockTts.setOnUtteranceProgressListener(capture(listenerCaptor)) }
        listener = listenerCaptor.captured
    }

    @Test
    fun `test pause during chunk correctly updates position`() {
        val progressField = TtsManager::class.java.getDeclaredField("progress")
        progressField.isAccessible = true
        val progress = progressField.get(ttsManager)
        
        val totalSpokenField = progress.javaClass.getDeclaredField("totalSpokenLength")
        totalSpokenField.isAccessible = true
        totalSpokenField.set(progress, 100)
        
        val currentIndexField = progress.javaClass.getDeclaredField("currentIndex")
        currentIndexField.isAccessible = true
        currentIndexField.set(progress, 15)

        every { mockTts.isSpeaking } returns true
        
        val pos = ttsManager.pause()
        assertEquals(115, pos)
    }

    @Test
    fun `test boundary guard returns offset when not speaking but pending`() {
        val progressField = TtsManager::class.java.getDeclaredField("progress")
        progressField.isAccessible = true
        val progress = progressField.get(ttsManager)
        
        val totalSpokenField = progress.javaClass.getDeclaredField("totalSpokenLength")
        totalSpokenField.isAccessible = true
        totalSpokenField.set(progress, 200)
        
        val currentIndexField = progress.javaClass.getDeclaredField("currentIndex")
        currentIndexField.isAccessible = true
        currentIndexField.set(progress, 0)

        val pendingField = TtsManager::class.java.getDeclaredField("pendingUtterances")
        pendingField.isAccessible = true
        val pending = pendingField.get(ttsManager) as AtomicInteger
        pending.set(1)

        every { mockTts.isSpeaking } returns false
        
        val pos = ttsManager.pause()
        assertEquals(200, pos)
    }

    @Test
    fun `test robust tracking updates length correctly via map`() {
        val utteranceId = "CHUNK|50|timestamp"
        
        val mapField = TtsManager::class.java.getDeclaredField("chunkOffsetMap")
        mapField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val map = mapField.get(ttsManager) as MutableMap<String, Int>
        map[utteranceId] = 100
        
        listener.onDone(utteranceId)
        
        val progressField = TtsManager::class.java.getDeclaredField("progress")
        progressField.isAccessible = true
        val progress = progressField.get(ttsManager)
        val totalField = progress.javaClass.getDeclaredField("totalSpokenLength")
        totalField.isAccessible = true
        
        assertEquals(150, totalField.get(progress))
    }
}
