package com.skul9x.ytsummary.util

object SummaryUtils {
    /**
     * Chunks a long text into smaller pieces based on paragraphs (\n\n) 
     * and a maximum character limit per slice for more efficient rendering 
     * in LazyColumn.
     */
    fun chunkText(text: String, chunkSize: Int = 1500): List<String> {
        if (text.isBlank()) return emptyList()
        
        val paragraphs = text.split("\n\n").filter { it.isNotBlank() }
        val result = mutableListOf<String>()
        
        paragraphs.forEach { p ->
            if (p.length > chunkSize) {
                var currentPos = 0
                while (currentPos < p.length) {
                    val end = minOf(currentPos + chunkSize, p.length)
                    result.add(p.substring(currentPos, end))
                    currentPos = end
                }
            } else {
                result.add(p)
            }
        }
        return result
    }

    private val HTML_TAG_REGEX = Regex("<[^>]*>")

    /**
     * Clean transcript text content:
     * 1. Unescape common HTML entities (&amp; → &, &lt; → <, &#39; → ')
     * 2. Strip HTML tags (<i>text</i> → text)
     */
    fun cleanTranscriptText(rawText: String): String {
        // Step 1: Unescape HTML entities for performance
        val unescaped = if (rawText.contains('&')) {
            rawText.replace("&amp;", "&")
                   .replace("&lt;", "<")
                   .replace("&gt;", ">")
                   .replace("&quot;", "\"")
                   .replace("&#39;", "'")
                   .replace("&nbsp;", " ")
        } else {
            rawText
        }

        // Step 2: Strip any remaining HTML tags
        return if (unescaped.contains('<')) {
            HTML_TAG_REGEX.replace(unescaped, "").trim()
        } else {
            unescaped.trim()
        }
    }
}
