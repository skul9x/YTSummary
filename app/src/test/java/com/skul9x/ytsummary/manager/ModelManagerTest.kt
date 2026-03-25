package com.skul9x.ytsummary.manager

import android.content.Context
import android.content.SharedPreferences
import io.mockk.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ModelManagerTest {

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
        
        // Reset singleton for testing
        val field = ModelManager::class.java.getDeclaredField("instance")
        field.isAccessible = true
        field.set(null, null)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getModels returns DEFAULT_MODELS when prefs is empty`() {
        every { sharedPrefs.getString(any(), null) } returns null
        
        val manager = ModelManager.getInstance(context)
        assertEquals(ModelManager.DEFAULT_MODELS, manager.getModels())
    }

    @Test
    fun `addModel adds new model successfully`() {
        val capturedJson = slot<String>()
        every { sharedPrefs.getString(any(), null) } returns null
        every { editor.putString(any(), capture(capturedJson)) } returns editor
        
        val manager = ModelManager.getInstance(context)
        val result = manager.addModel("models/new-model")
        
        assertTrue(result)
        assertTrue(capturedJson.captured.contains("models/new-model"))
    }

    @Test
    fun `addModel rejects duplicate model`() {
        val existingJson = "[\"models/gemini-1.5-flash\"]"
        every { sharedPrefs.getString(any(), null) } returns existingJson
        
        val manager = ModelManager.getInstance(context)
        val result = manager.addModel("models/gemini-1.5-flash")
        
        assertFalse(result)
        verify(exactly = 0) { editor.putString(any(), any()) }
    }

    @Test
    fun `addModel rejects blank model`() {
        val manager = ModelManager.getInstance(context)
        assertFalse(manager.addModel(""))
        assertFalse(manager.addModel("   "))
    }

    @Test
    fun `removeModel deletes correct index`() {
        val existingJson = "[\"modelA\", \"modelB\", \"modelC\"]"
        every { sharedPrefs.getString(any(), null) } returns existingJson
        val capturedJson = slot<String>()
        every { editor.putString(any(), capture(capturedJson)) } returns editor
        
        val manager = ModelManager.getInstance(context)
        val result = manager.removeModel(1) // Remove modelB
        
        assertTrue(result)
        assertFalse(capturedJson.captured.contains("modelB"))
        assertTrue(capturedJson.captured.contains("modelA"))
        assertTrue(capturedJson.captured.contains("modelC"))
    }

    @Test
    fun `removeModel returns false for invalid index`() {
        val existingJson = "[\"modelA\"]"
        every { sharedPrefs.getString(any(), null) } returns existingJson
        
        val manager = ModelManager.getInstance(context)
        assertFalse(manager.removeModel(-1))
        assertFalse(manager.removeModel(1))
    }

    @Test
    fun `moveUp swaps with previous item`() {
        val existingJson = "[\"modelA\", \"modelB\", \"modelC\"]"
        every { sharedPrefs.getString(any(), null) } returns existingJson
        val capturedJson = slot<String>()
        every { editor.putString(any(), capture(capturedJson)) } returns editor
        
        val manager = ModelManager.getInstance(context)
        val result = manager.moveUp(1) // Move modelB up (to index 0)
        
        assertTrue(result)
        // Order should be B, A, C
        assertEquals("[\"modelB\",\"modelA\",\"modelC\"]", capturedJson.captured)
    }

    @Test
    fun `moveUp returns false for first item`() {
        val existingJson = "[\"modelA\", \"modelB\"]"
        every { sharedPrefs.getString(any(), null) } returns existingJson
        
        val manager = ModelManager.getInstance(context)
        assertFalse(manager.moveUp(0))
    }

    @Test
    fun `moveDown swaps with next item`() {
        val existingJson = "[\"modelA\", \"modelB\", \"modelC\"]"
        every { sharedPrefs.getString(any(), null) } returns existingJson
        val capturedJson = slot<String>()
        every { editor.putString(any(), capture(capturedJson)) } returns editor
        
        val manager = ModelManager.getInstance(context)
        val result = manager.moveDown(1) // Move modelB down (to index 2)
        
        assertTrue(result)
        // Order should be A, C, B
        assertEquals("[\"modelA\",\"modelC\",\"modelB\"]", capturedJson.captured)
    }

    @Test
    fun `moveDown returns false for last item`() {
        val existingJson = "[\"modelA\", \"modelB\"]"
        every { sharedPrefs.getString(any(), null) } returns existingJson
        
        val manager = ModelManager.getInstance(context)
        assertFalse(manager.moveDown(1))
    }

    @Test
    fun `resetToDefaults removes key from prefs`() {
        val manager = ModelManager.getInstance(context)
        manager.resetToDefaults()
        
        verify { editor.remove(any()) }
        verify { editor.apply() }
    }

    @Test
    fun `getModels returns DEFAULT_MODELS when saved list is empty`() {
        val existingJson = "[]"
        every { sharedPrefs.getString(any(), null) } returns existingJson
        
        val manager = ModelManager.getInstance(context)
        assertEquals(ModelManager.DEFAULT_MODELS, manager.getModels())
    }
}
