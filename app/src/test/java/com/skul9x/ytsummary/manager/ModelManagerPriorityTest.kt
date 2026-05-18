package com.skul9x.ytsummary.manager

import android.content.Context
import android.content.SharedPreferences
import io.mockk.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ModelManagerPriorityTest {

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
    fun testDefaultModelsPriorityMatchSpecification() {
        val expectedDefaults = listOf(
            "models/gemini-3.1-flash-lite",
            "models/gemini-2.5-flash-lite",
            "models/gemini-3-flash-preview",
            "models/gemini-2.5-flash"
        )
        
        assertEquals(expectedDefaults, ModelManager.DEFAULT_MODELS)
    }

    @Test
    fun testGetModelsReturnsSpecDefaultsWhenPrefsEmpty() {
        every { sharedPrefs.getString(any(), null) } returns null
        
        val manager = ModelManager.getInstance(context)
        val actualModels = manager.getModels()
        
        assertEquals(4, actualModels.size)
        assertEquals("models/gemini-3.1-flash-lite", actualModels[0])
        assertEquals("models/gemini-2.5-flash-lite", actualModels[1])
        assertEquals("models/gemini-3-flash-preview", actualModels[2])
        assertEquals("models/gemini-2.5-flash", actualModels[3])
    }
}
