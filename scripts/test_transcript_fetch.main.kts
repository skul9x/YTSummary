#!/usr/bin/env kotlinc-jvm -script
/**
 * ====================================================
 * TEST SCRIPT: YouTube Transcript Fetcher (Standalone)
 * ====================================================
 * Chạy bằng: kotlinc -script scripts/test_transcript_fetch.main.kts
 *
 * Script này test toàn bộ luồng lấy phụ đề từ YouTube:
 *   1. Gọi YouTube watch page → extract INNERTUBE_API_KEY
 *   2. POST InnerTube player API → lấy caption tracks
 *   3. Download XML transcript từ track URL
 *   4. Parse XML → plain text
 *
 * Không cần Android SDK, Gradle hay device — chỉ cần JVM + internet.
 * ====================================================
 */

@file:DependsOn("com.squareup.okhttp3:okhttp:4.12.0")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.6.3")

import kotlinx.serialization.json.*
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.xml.sax.InputSource
import java.io.StringReader
import java.util.concurrent.TimeUnit
import javax.xml.parsers.DocumentBuilderFactory

// ─────────────────────────────────────────
// CONFIG
// ─────────────────────────────────────────

val VIDEO_URL = "https://youtu.be/MbOah3Fjkhc?si=6kP2BtZwM8HJfJdY"
val VIDEO_ID = "MbOah3Fjkhc"  // extracted from URL above
val PRIORITY_LANGUAGES = listOf("vi", "en")

// ─────────────────────────────────────────
// CONSTANTS
// ─────────────────────────────────────────

val WATCH_URL = "https://www.youtube.com/watch?v=%s"
val INNERTUBE_API_URL = "https://www.youtube.com/youtubei/v1/player?key=%s"
val INNERTUBE_BODY = """
    {
        "context": {
            "client": {
                "clientName": "ANDROID",
                "clientVersion": "20.10.38"
            }
        },
        "videoId": "%s"
    }
""".trimIndent()

val API_KEY_REGEX = Regex(""""INNERTUBE_API_KEY":\s*"([a-zA-Z0-9_-]+)"""")
val CONSENT_VALUE_REGEX = Regex("""name="v" value="(.*?)"""")
val HTML_TAG_REGEX = Regex("<[^>]*>")
val HTML_ENTITY_MAP = mapOf(
    "&amp;" to "&", "&lt;" to "<", "&gt;" to ">",
    "&quot;" to "\"", "&#39;" to "'", "&apos;" to "'",
    "&#x27;" to "'", "&#x2F;" to "/"
)

val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

// ─────────────────────────────────────────
// DATA CLASSES
// ─────────────────────────────────────────

data class CaptionTrack(
    val baseUrl: String,
    val language: String,
    val languageCode: String,
    val isGenerated: Boolean
)

data class TranscriptSnippet(
    val text: String,
    val start: Float,
    val duration: Float
)

// ─────────────────────────────────────────
// HTTP CLIENT
// ─────────────────────────────────────────

val cookieStore = mutableListOf<Cookie>()
val httpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .cookieJar(object : CookieJar {
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookieStore.addAll(cookies)
        }
        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return cookieStore.filter { it.matches(url) }
        }
    })
    .build()

val json = Json { ignoreUnknownKeys = true }

// ─────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────

fun unescapeHtml(text: String): String {
    var result = text
    HTML_ENTITY_MAP.forEach { (entity, char) -> result = result.replace(entity, char) }
    return result
}

fun cleanSnippetText(rawText: String): String {
    // 1. Strip HTML tags
    val stripped = HTML_TAG_REGEX.replace(rawText, "")
    // 2. Unescape HTML entities (pure Kotlin, no Android)
    return unescapeHtml(stripped).trim()
}

fun printSection(title: String) {
    println("\n" + "═".repeat(60))
    println("  $title")
    println("═".repeat(60))
}

fun printStep(step: Int, msg: String) = println("\n[$step] $msg")
fun printOk(msg: String) = println("  ✅ $msg")
fun printErr(msg: String) = println("  ❌ $msg")
fun printInfo(msg: String) = println("  ℹ️  $msg")

// ─────────────────────────────────────────
// STEP 1: Fetch watch page HTML
// ─────────────────────────────────────────

fun fetchWatchPageHtml(videoId: String): String {
    val request = Request.Builder()
        .url(String.format(WATCH_URL, videoId))
        .header("Accept-Language", "en-US")
        .header("User-Agent", "Mozilla/5.0 (Linux; Android 11; Pixel 5) AppleWebKit/537.36")
        .get()
        .build()

    val response = httpClient.newCall(request).execute()
    if (!response.isSuccessful) error("HTTP ${response.code} when fetching watch page")
    val body = response.body?.string() ?: error("Empty watch page response")

    // Handle EU consent page
    if ("action=\"https://consent.youtube.com/s\"" in body) {
        printInfo("Consent page detected, setting cookie...")
        val match = CONSENT_VALUE_REGEX.find(body) ?: error("Cannot parse consent value")
        val consentValue = "YES+" + match.groupValues[1]
        val cookie = Cookie.Builder()
            .domain(".youtube.com")
            .name("CONSENT")
            .value(consentValue)
            .build()
        cookieStore.add(cookie)

        // Refetch after setting cookie
        val response2 = httpClient.newCall(request).execute()
        return response2.body?.string()?.let { unescapeHtml(it) } ?: error("Empty response after consent")
    }

    return unescapeHtml(body)
}

// ─────────────────────────────────────────
// STEP 2: Extract API key
// ─────────────────────────────────────────

fun extractApiKey(html: String): String {
    if ("class=\"g-recaptcha\"" in html) error("IP blocked by YouTube (reCAPTCHA detected)")
    val match = API_KEY_REGEX.find(html) ?: error("INNERTUBE_API_KEY not found in page HTML")
    return match.groupValues[1]
}

// ─────────────────────────────────────────
// STEP 3: Call InnerTube API
// ─────────────────────────────────────────

fun fetchInnerTubeData(videoId: String, apiKey: String): JsonObject {
    val body = String.format(INNERTUBE_BODY, videoId).toRequestBody(JSON_MEDIA_TYPE)
    val request = Request.Builder()
        .url(String.format(INNERTUBE_API_URL, apiKey))
        .header("Accept-Language", "en-US")
        .post(body)
        .build()

    val response = httpClient.newCall(request).execute()
    if (!response.isSuccessful) error("HTTP ${response.code} from InnerTube API")
    val responseBody = response.body?.string() ?: error("Empty InnerTube response")
    return json.parseToJsonElement(responseBody).jsonObject
}

// ─────────────────────────────────────────
// STEP 4: Parse caption tracks
// ─────────────────────────────────────────

fun parseCaptionTracks(data: JsonObject, videoId: String): List<CaptionTrack> {
    // Check playability
    data["playabilityStatus"]?.jsonObject?.let { status ->
        val statusStr = status["status"]?.jsonPrimitive?.content
        if (statusStr != "OK") {
            val reason = status["reason"]?.jsonPrimitive?.content
            error("Video not playable: $statusStr - $reason")
        }
    }

    val captions = data["captions"]
        ?.jsonObject
        ?.get("playerCaptionsTracklistRenderer")
        ?.jsonObject
        ?: error("No captions found in InnerTube response (transcripts may be disabled)")

    val captionTracks = captions["captionTracks"]?.jsonArray
        ?: error("captionTracks array not found (transcripts may be disabled)")

    return captionTracks.mapNotNull { element ->
        val obj = element.jsonObject
        val baseUrl = obj["baseUrl"]?.jsonPrimitive?.content ?: return@mapNotNull null
        val languageCode = obj["languageCode"]?.jsonPrimitive?.content ?: return@mapNotNull null
        val kind = obj["kind"]?.jsonPrimitive?.content ?: ""
        val language = try {
            obj["name"]?.jsonObject
                ?.get("runs")?.jsonArray
                ?.firstOrNull()?.jsonObject
                ?.get("text")?.jsonPrimitive?.content ?: languageCode
        } catch (e: Exception) { languageCode }

        CaptionTrack(
            baseUrl = baseUrl.replace("&fmt=srv3", ""),
            language = language,
            languageCode = languageCode,
            isGenerated = kind == "asr"
        )
    }
}

// ─────────────────────────────────────────
// STEP 5: Select best track
// ─────────────────────────────────────────

fun selectBestTrack(tracks: List<CaptionTrack>, languages: List<String>): CaptionTrack? {
    val manual = tracks.filter { !it.isGenerated }
    val generated = tracks.filter { it.isGenerated }
    for (lang in languages) {
        manual.find { it.languageCode == lang }?.let { return it }
        generated.find { it.languageCode == lang }?.let { return it }
    }
    return null
}

// ─────────────────────────────────────────
// STEP 6: Download + parse XML transcript
// ─────────────────────────────────────────

fun fetchTranscriptXml(url: String): String {
    val request = Request.Builder()
        .url(url)
        .header("Accept-Language", "en-US")
        .get()
        .build()
    val response = httpClient.newCall(request).execute()
    if (response.code == 429) error("IP blocked (HTTP 429)")
    if (!response.isSuccessful) error("HTTP ${response.code} when fetching transcript XML")
    return response.body?.string() ?: error("Empty transcript XML response")
}

fun parseTranscriptXml(xml: String): List<TranscriptSnippet> {
    val snippets = mutableListOf<TranscriptSnippet>()
    val factory = DocumentBuilderFactory.newInstance()
    val builder = factory.newDocumentBuilder()
    val doc = builder.parse(InputSource(StringReader(xml)))
    val nodes = doc.getElementsByTagName("text")

    for (i in 0 until nodes.length) {
        val node = nodes.item(i)
        val start = node.attributes.getNamedItem("start")?.nodeValue?.toFloatOrNull() ?: continue
        val duration = node.attributes.getNamedItem("dur")?.nodeValue?.toFloatOrNull() ?: 0f
        val rawText = node.textContent
        if (rawText.isNullOrBlank()) continue
        val cleanText = cleanSnippetText(rawText)
        if (cleanText.isNotEmpty()) {
            snippets.add(TranscriptSnippet(text = cleanText, start = start, duration = duration))
        }
    }

    return snippets
}

// ─────────────────────────────────────────
// MAIN: Run the test
// ─────────────────────────────────────────

printSection("YouTube Transcript Fetch Test")
println("  Video URL: $VIDEO_URL")
println("  Video ID : $VIDEO_ID")
println("  Languages: $PRIORITY_LANGUAGES")

try {
    // STEP 1
    printStep(1, "Fetching YouTube watch page HTML...")
    val html = fetchWatchPageHtml(VIDEO_ID)
    printOk("Watch page fetched (${html.length} chars)")

    // STEP 2
    printStep(2, "Extracting INNERTUBE_API_KEY...")
    val apiKey = extractApiKey(html)
    printOk("API Key found: ${apiKey.take(10)}...")

    // STEP 3
    printStep(3, "Calling InnerTube player API...")
    val innerTubeData = fetchInnerTubeData(VIDEO_ID, apiKey)
    printOk("InnerTube response received")

    // STEP 4
    printStep(4, "Parsing caption tracks...")
    val tracks = parseCaptionTracks(innerTubeData, VIDEO_ID)
    printOk("Found ${tracks.size} caption track(s):")
    tracks.forEach { track ->
        val type = if (track.isGenerated) "🤖 auto-generated" else "✍️  manual"
        println("     • [${track.languageCode}] ${track.language} — $type")
    }

    // STEP 5
    printStep(5, "Selecting best track for languages: $PRIORITY_LANGUAGES...")
    val selectedTrack = selectBestTrack(tracks, PRIORITY_LANGUAGES)
        ?: error("No matching track found for languages: $PRIORITY_LANGUAGES")
    printOk("Selected: [${selectedTrack.languageCode}] ${selectedTrack.language} (generated=${selectedTrack.isGenerated})")

    // Check PO Token requirement
    if ("&exp=xpe" in selectedTrack.baseUrl) {
        error("PO Token required — cannot fetch this transcript without authentication")
    }

    // STEP 6
    printStep(6, "Downloading transcript XML...")
    val xml = fetchTranscriptXml(selectedTrack.baseUrl)
    printOk("XML downloaded (${xml.length} chars)")

    printStep(7, "Parsing transcript XML → plain text...")
    val snippets = parseTranscriptXml(xml)
    printOk("Parsed ${snippets.size} snippets")

    val plainText = snippets.joinToString(" ") { it.text }
    val wordCount = plainText.trim().split("\\s+".toRegex()).size

    printSection("✅ TRANSCRIPT FETCHED SUCCESSFULLY!")
    println("  Language   : [${selectedTrack.languageCode}] ${selectedTrack.language}")
    println("  Type       : ${if (selectedTrack.isGenerated) "Auto-generated" else "Manual"}")
    println("  Snippets   : ${snippets.size}")
    println("  Characters : ${plainText.length}")
    println("  Words      : ~$wordCount")
    println()
    println("─── First 500 characters ───────────────────────────────")
    println(plainText.take(500))
    if (plainText.length > 500) println("... [${plainText.length - 500} more chars]")
    println("────────────────────────────────────────────────────────")
    println()
    println("─── First 5 snippets ───────────────────────────────────")
    snippets.take(5).forEachIndexed { i, s ->
        println("  [${i+1}] t=${s.start}s  \"${s.text}\"")
    }
    println("────────────────────────────────────────────────────────")

} catch (e: Exception) {
    printSection("❌ FETCH FAILED")
    println("  Error type : ${e.javaClass.simpleName}")
    println("  Message    : ${e.message}")
    println()
    println("Stack trace:")
    e.printStackTrace()
}
