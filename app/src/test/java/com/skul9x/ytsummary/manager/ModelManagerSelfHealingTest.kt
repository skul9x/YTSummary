package com.skul9x.ytsummary.manager

import android.content.Context
import android.content.SharedPreferences
import io.mockk.*
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ModelManagerSelfHealingTest {

    private val context = mockk<Context>(relaxed = true)
    private val sharedPrefs = mockk<SharedPreferences>(relaxed = true)
    private val editor = mockk<SharedPreferences.Editor>(relaxed = true)

    @Before
    fun setup() {
        every { context.applicationContext } returns context
        every { context.getSharedPreferences(any(), any()) } returns sharedPrefs
        every { sharedPrefs.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.remove(any()) } returns editor
        
        // Reset singleton
        val field = ModelManager::class.java.getDeclaredField("instance")
        field.isAccessible = true
        field.set(null, null)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testSaveModelsSelfHealsEmptyList() {
        val capturedJson = slot<String>()
        every { editor.putString(any(), capture(capturedJson)) } returns editor
        
        val manager = ModelManager.getInstance(context)
        
        // Act: Save an empty list
        manager.saveModels(emptyList())
        
        // Assert: It should save the DEFAULT_MODELS instead of []
        val savedList: List<String> = Json.decodeFromString(capturedJson.captured)
        assertEquals(ModelManager.DEFAULT_MODELS, savedList)
    }

    @Test
    fun testRemoveAllModelsTriggersSelfHealing() {
        // Mocking an initial list with 1 item
        var mockSavedData = "[\"models/gemini-2.5-flash\"]"
        every { sharedPrefs.getString(any(), null) } answers { mockSavedData }
        
        val capturedJson = slot<String>()
        every { editor.putString(any(), capture(capturedJson)) } answers {
            mockSavedData = capturedJson.captured
            editor
        }

        val manager = ModelManager.getInstance(context)
        
        // Act: Remove the last item (index 0)
        val result = manager.removeModel(0)
        
        // Assert: Success should be true
        assertEquals(true, result)
        
        // The saved JSON must now be the DEFAULT_MODELS JSON because of self-healing
        val savedList: List<String> = Json.decodeFromString(mockSavedData)
        assertEquals(ModelManager.DEFAULT_MODELS, savedList)
        
        // getModels() must return DEFAULT_MODELS
        assertEquals(ModelManager.DEFAULT_MODELS, manager.getModels())
    }
}
