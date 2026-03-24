package com.skul9x.ytsummary.api.gemini

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import kotlinx.serialization.json.*

class GeminiResponseHelperTest {

    @Test
    fun testBuildRequestBody_includesThinkingConfigAtRoot() {
        val prompt = "Test prompt"
        val jsonString = GeminiResponseHelper.buildRequestBody(prompt)
        val json = Json.parseToJsonElement(jsonString).jsonObject

        // 1. Kiểm tra thinkingConfig nằm ở root
        val thinkingConfig = json["thinkingConfig"]?.jsonObject
        assertNotNull("thinkingConfig should exist at root", thinkingConfig)
        
        // 2. Kiểm tra thinkingBudget có giá trị là 0
        val thinkingBudget = thinkingConfig?.get("thinkingBudget")?.jsonPrimitive?.int
        assertEquals("thinkingBudget should be 0", 0, thinkingBudget)

        // 3. Kiểm tra generationConfig không còn chứa thinking_config cũ
        val generationConfig = json["generationConfig"]?.jsonObject
        assertNotNull("generationConfig should exist", generationConfig)
        assertEquals("generationConfig should NOT contain thinking_config", null, generationConfig?.get("thinking_config"))
    }
}
