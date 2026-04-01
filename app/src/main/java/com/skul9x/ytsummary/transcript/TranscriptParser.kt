package com.skul9x.ytsummary.transcript

import com.skul9x.ytsummary.transcript.model.TranscriptSnippet
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

/**
 * Parser cho YouTube transcript XML (timedtext format).
 * Tương đương _TranscriptParser trong Python.
 *
 * Input format:
 * ```xml
 * <transcript>
 *   <text start="0" dur="1.54">Hey, this is just a test</text>
 *   <text start="1.54" dur="4.16">this is &lt;i&gt;not&lt;/i&gt; the original</text>
 *   <text start="5" dur="0.5"></text>
 * </transcript>
 * ```
 */
class TranscriptParser {

    companion object {
        private const val TAG = "TranscriptParser"
    }

    /**
     * Parse XML transcript thành list of snippets.
     * - Bỏ qua element có text null hoặc empty (giống Python line 491).
     * - Unescape HTML entities (e.g. &amp; → &, &lt; → <).
     * - Strip HTML tags (e.g. <i>not</i> → not).
     *
     * @param xmlData Raw XML string từ YouTube timedtext API
     * @return List<TranscriptSnippet> đã clean
     */
    fun parse(xmlData: String): List<TranscriptSnippet> {
        val snippets = mutableListOf<TranscriptSnippet>()
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xmlData))

        var eventType = parser.eventType
        var currentStart: Float? = null
        var currentDuration: Float? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "text") {
                        currentStart = parser.getAttributeValue(null, "start")?.toFloatOrNull()
                        currentDuration = parser.getAttributeValue(null, "dur")?.toFloatOrNull() ?: 0.0f
                    }
                }

                XmlPullParser.TEXT -> {
                    if (currentStart != null) {
                        val rawText = parser.text
                        if (!rawText.isNullOrBlank()) {
                            val cleanedText = com.skul9x.ytsummary.util.SummaryUtils.cleanTranscriptText(rawText)
                            if (cleanedText.isNotEmpty()) {
                                snippets.add(
                                    TranscriptSnippet(
                                        text = cleanedText,
                                        start = currentStart,
                                        duration = currentDuration ?: 0.0f
                                    )
                                )
                            }
                        }
                    }
                }

                XmlPullParser.END_TAG -> {
                    if (parser.name == "text") {
                        currentStart = null
                        currentDuration = null
                    }
                }
            }
            eventType = parser.next()
        }

        return snippets
    }
}
