package com.skul9x.ytsummary.util

import org.junit.Assert.assertEquals
import org.junit.Test

class SummaryUtilsTest {

    @Test
    fun `chunkText split by paragraphs correctly`() {
        val input = "Paragraph 1\n\nParagraph 2\n\nParagraph 3"
        val result = SummaryUtils.chunkText(input)
        
        assertEquals(3, result.size)
        assertEquals("Paragraph 1", result[0])
        assertEquals("Paragraph 2", result[1])
        assertEquals("Paragraph 3", result[2])
    }

    @Test
    fun `chunkText further chunks large paragraphs`() {
        val largeParagraph = "A".repeat(2500)
        val input = largeParagraph
        
        // Chunk size is 1500, so 2500 -> 1500 + 1000 (2 chunks)
        val result = SummaryUtils.chunkText(input, chunkSize = 1500)
        
        assertEquals(2, result.size)
        assertEquals(1500, result[0].length)
        assertEquals(1000, result[1].length)
        assertEquals("A".repeat(1500), result[0])
        assertEquals("A".repeat(1000), result[1])
    }

    @Test
    fun `chunkText handles blank input`() {
        assertEquals(0, SummaryUtils.chunkText("").size)
        assertEquals(0, SummaryUtils.chunkText("   ").size)
    }

    @Test
    fun `chunkText ignores empty paragraphs`() {
        val input = "Paragraph 1\n\n\n\nParagraph 2"
        val result = SummaryUtils.chunkText(input)
        
        assertEquals(2, result.size)
        assertEquals("Paragraph 1", result[0])
        assertEquals("Paragraph 2", result[1])
    }

    @Test
    fun `cleanTranscriptText unescapes HTML entities`() {
        assertEquals("&", SummaryUtils.cleanTranscriptText("&amp;"))
        assertEquals("<", SummaryUtils.cleanTranscriptText("&lt;"))
        assertEquals(">", SummaryUtils.cleanTranscriptText("&gt;"))
        assertEquals("\"", SummaryUtils.cleanTranscriptText("&quot;"))
        assertEquals("'", SummaryUtils.cleanTranscriptText("&#39;"))
    }

    @Test
    fun `cleanTranscriptText handles nbsp`() {
        val input = "Text&nbsp;Space"
        val result = SummaryUtils.cleanTranscriptText(input)
        assertEquals("Text Space", result)
    }

    @Test
    fun `cleanTranscriptText strips HTML tags`() {
        val input = "This is <i>italics</i> and <b>bold</b>"
        val result = SummaryUtils.cleanTranscriptText(input)
        assertEquals("This is italics and bold", result)
    }

    @Test
    fun `cleanTranscriptText handles complex mixed input`() {
        val input = "Click &lt;a href=&quot;link&quot;&gt;here&lt;/a&gt; for <b>more</b> info &amp; help"
        val result = SummaryUtils.cleanTranscriptText(input)
        assertEquals("Click here for more info & help", result)
    }
}
