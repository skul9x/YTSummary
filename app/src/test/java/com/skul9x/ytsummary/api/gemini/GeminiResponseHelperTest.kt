package com.skul9x.ytsummary.api.gemini

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import kotlinx.serialization.json.*

class GeminiResponseHelperTest {

/*
    @Test
    fun testBuildRequestBody_basicStructure() {
        val prompt = "Test prompt"
        val jsonString = GeminiResponseHelper.buildRequestBody(prompt)
        val json = Json.parseToJsonElement(jsonString).jsonObject

        // 1. Kiểm tra cấu trúc contents
        val contents = json["contents"]?.jsonArray
        assertNotNull("contents should exist", contents)
        val firstPart = contents?.get(0)?.jsonObject?.get("parts")?.jsonArray?.get(0)?.jsonObject
        assertEquals("Prompt should match", prompt, firstPart?.get("text")?.jsonPrimitive?.content)
    }
*/

    @Test
    fun testExtractText_PreservesSpacesAtBoundaries() {
        val json = """
            {
                "candidates": [{
                    "content": {
                        "parts": [{"text": "Hello "}]
                    }
                }]
            }
        """.trimIndent()
        
        val result = GeminiResponseHelper.extractText(json)
        assertEquals("Should preserve trailing space", "Hello ", result)
    }

    @Test
    fun testExtractText_IncludesOnlyWhitespaceChunks() {
        val json = """
            {
                "candidates": [{
                    "content": {
                        "parts": [{"text": " "}]
                    }
                }]
            }
        """.trimIndent()
        
        val result = GeminiResponseHelper.extractText(json)
        assertEquals("Should include chunk with only whitespace", " ", result)
    }

    @Test
    fun testExtractText_ConcatenatesMultiplePartsCorrectly() {
        val json = """
            {
                "candidates": [{
                    "content": {
                        "parts": [
                            {"text": "A"},
                            {"text": " B"}
                        ]
                            }
                }]
            }
        """.trimIndent()
        
        val result = GeminiResponseHelper.extractText(json)
        assertEquals("Should concatenate parts accurately", "A B", result)
    }
}
