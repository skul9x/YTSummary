package com.skul9x.ytsummary.transcript

import android.util.Log
import com.skul9x.ytsummary.transcript.exception.TranscriptException
import com.skul9x.ytsummary.transcript.model.CaptionTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Client cho YouTube InnerTube API.
 * Port từ Python TranscriptListFetcher (_transcripts.py lines 347-454).
 *
 * Luồng:
 * 1. GET watch page → extract INNERTUBE_API_KEY
 * 2. POST InnerTube player API → get captions JSON
 * 3. Parse captions → List<CaptionTrack>
 */
class InnerTubeClient(
    baseClient: OkHttpClient
) {

    companion object {
        private const val TAG = "InnerTubeClient"

        private const val WATCH_URL = "https://www.youtube.com/watch?v=%s"
        private const val INNERTUBE_API_URL = "https://www.youtube.com/youtubei/v1/player?key=%s"

        /** InnerTube context giả lập Android client (giống Python _settings.py) */
        private const val INNERTUBE_REQUEST_BODY = """
            {
                "context": {
                    "client": {
                        "clientName": "ANDROID",
                        "clientVersion": "20.10.38"
                    }
                },
                "videoId": "%s"
            }
        """

        private val API_KEY_REGEX = Regex(""""INNERTUBE_API_KEY":\s*"([a-zA-Z0-9_-]+)"""")
        private val CONSENT_VALUE_REGEX = Regex("""name="v" value="(.*?)"""")

        // Playability status values (giống Python _PlayabilityStatus enum)
        private const val STATUS_OK = "OK"
        private const val STATUS_ERROR = "ERROR"
        private const val STATUS_LOGIN_REQUIRED = "LOGIN_REQUIRED"

        // Playability failure reasons (giống Python _PlayabilityFailedReason enum)
        private const val REASON_BOT_DETECTED = "Sign in to confirm you're not a bot"
        private const val REASON_AGE_RESTRICTED = "This video may be inappropriate for some users."
        private const val REASON_VIDEO_UNAVAILABLE = "This video is unavailable"

        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    /** CookieJar đơn giản để handle consent cookies */
    private val cookieStore = mutableListOf<Cookie>()

    private val httpClient: OkHttpClient = baseClient.newBuilder()
        .cookieJar(object : CookieJar {
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                cookieStore.addAll(cookies)
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                return cookieStore.filter { it.matches(url) }
            }
        })
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Fetch danh sách caption tracks cho video.
     * Đây là entry point chính cho InnerTubeClient.
     *
     * @throws TranscriptException nếu có lỗi
     */
    suspend fun fetchCaptionTracks(videoId: String): List<CaptionTrack> =
        withContext(Dispatchers.IO) {
            // Step 1: Fetch watch page HTML và extract API key
            val html = fetchVideoHtml(videoId)
            val apiKey = extractApiKey(html, videoId)

            // Step 2: Call InnerTube API
            val innertubeData = fetchInnerTubeData(videoId, apiKey)

            // Step 3: Check playability và extract captions
            val captionsJson = extractCaptionsJson(innertubeData, videoId)

            // Step 4: Parse caption tracks
            parseCaptionTracks(captionsJson)
        }

    // ═══════════════════════════════════════════════════════════════════
    // Step 1: Fetch watch page HTML
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Fetch YouTube watch page HTML.
     * Handle consent page (EU cookie wall) nếu gặp.
     * (Port từ Python _fetch_video_html lines 432-439)
     */
    private fun fetchVideoHtml(videoId: String): String {
        var html = fetchHtml(videoId)

        // Check consent page
        if ("action=\"https://consent.youtube.com/s\"" in html) {
            Log.d(TAG, "Consent page detected, creating cookie...")
            createConsentCookie(html, videoId)
            html = fetchHtml(videoId)

            // Nếu vẫn còn consent page → fail
            if ("action=\"https://consent.youtube.com/s\"" in html) {
                throw TranscriptException.ConsentRequired(videoId)
            }
        }

        return html
    }

    /**
     * GET watch page, unescape HTML entities.
     * (Port từ Python _fetch_html lines 441-443)
     */
    private fun fetchHtml(videoId: String): String {
        val request = Request.Builder()
            .url(String.format(WATCH_URL, videoId))
            .header("Accept-Language", "en-US")
            .get()
            .build()

        val response = httpClient.newCall(request).execute()
        raiseHttpErrors(response, videoId)

        val body = response.body?.string()
            ?: throw TranscriptException.DataUnparsable(videoId)

        // Unescape HTML entities (giống Python html.unescape)
        return unescapeHtml(body)
    }

    /**
     * Tạo consent cookie từ consent page HTML.
     * (Port từ Python _create_consent_cookie lines 424-430)
     */
    private fun createConsentCookie(html: String, videoId: String) {
        val match = CONSENT_VALUE_REGEX.find(html)
            ?: throw TranscriptException.ConsentRequired(videoId)

        val consentValue = "YES+" + match.groupValues[1]
        val cookie = Cookie.Builder()
            .domain(".youtube.com")
            .name("CONSENT")
            .value(consentValue)
            .build()
        cookieStore.add(cookie)
    }

    // ═══════════════════════════════════════════════════════════════════
    // Step 2: Extract API key & call InnerTube
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Extract INNERTUBE_API_KEY từ watch page HTML.
     * (Port từ Python _extract_innertube_api_key lines 375-382)
     */
    private fun extractApiKey(html: String, videoId: String): String {
        val match = API_KEY_REGEX.find(html)
        if (match != null && match.groupValues.size == 2) {
            return match.groupValues[1]
        }

        // Nếu gặp recaptcha → IP blocked
        if ("class=\"g-recaptcha\"" in html) {
            throw TranscriptException.IpBlocked(videoId)
        }

        throw TranscriptException.DataUnparsable(videoId)
    }

    /**
     * POST InnerTube player API để lấy video data (bao gồm captions).
     * (Port từ Python _fetch_innertube_data lines 445-454)
     */
    private fun fetchInnerTubeData(videoId: String, apiKey: String): JsonObject {
        val body = String.format(INNERTUBE_REQUEST_BODY, videoId)
            .toRequestBody(JSON_MEDIA_TYPE)

        val request = Request.Builder()
            .url(String.format(INNERTUBE_API_URL, apiKey))
            .header("Accept-Language", "en-US")
            .post(body)
            .build()

        val response = httpClient.newCall(request).execute()
        raiseHttpErrors(response, videoId)

        val responseBody = response.body?.string()
            ?: throw TranscriptException.DataUnparsable(videoId)

        return try {
            json.parseToJsonElement(responseBody).jsonObject
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse InnerTube response for $videoId", e)
            throw TranscriptException.DataUnparsable(videoId)
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Step 3: Extract & validate captions from InnerTube response
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Extract captions JSON từ InnerTube response, validate playability.
     * (Port từ Python _extract_captions_json lines 384-393)
     */
    private fun extractCaptionsJson(data: JsonObject, videoId: String): JsonObject {
        // Check playability status
        data["playabilityStatus"]?.jsonObject?.let { status ->
            assertPlayability(status, videoId)
        }

        // Extract captions
        val captions = data["captions"]
            ?.jsonObject
            ?.get("playerCaptionsTracklistRenderer")
            ?.jsonObject

        if (captions == null || !captions.containsKey("captionTracks")) {
            throw TranscriptException.TranscriptsDisabled(videoId)
        }

        return captions
    }

    /**
     * Check playability status và throw exception tương ứng.
     * (Port từ Python _assert_playability lines 395-422)
     */
    private fun assertPlayability(statusData: JsonObject, videoId: String) {
        val status = statusData["status"]?.jsonPrimitive?.content ?: return

        if (status == STATUS_OK) return

        val reason = statusData["reason"]?.jsonPrimitive?.content

        when (status) {
            STATUS_LOGIN_REQUIRED -> {
                when (reason) {
                    REASON_BOT_DETECTED -> throw TranscriptException.RequestBlocked(videoId)
                    REASON_AGE_RESTRICTED -> throw TranscriptException.AgeRestricted(videoId)
                }
            }

            STATUS_ERROR -> {
                if (reason == REASON_VIDEO_UNAVAILABLE) {
                    // Nếu videoId trông như URL → InvalidVideoId
                    if (videoId.startsWith("http://") || videoId.startsWith("https://")) {
                        throw TranscriptException.InvalidVideoId(videoId)
                    }
                    throw TranscriptException.VideoUnavailable(videoId)
                }
            }
        }

        // Fallback: VideoUnplayable với sub-reasons
        val subReasons = try {
            statusData["errorScreen"]
                ?.jsonObject?.get("playerErrorMessageRenderer")
                ?.jsonObject?.get("subreason")
                ?.jsonObject?.get("runs")
                ?.jsonArray?.map {
                    it.jsonObject["text"]?.jsonPrimitive?.content ?: ""
                } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        throw TranscriptException.VideoUnplayable(videoId, reason, subReasons)
    }

    // ═══════════════════════════════════════════════════════════════════
    // Step 4: Parse caption tracks
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Parse captions JSON thành list CaptionTrack.
     * (Port từ Python TranscriptList.build lines 206-250)
     */
    private fun parseCaptionTracks(captionsJson: JsonObject): List<CaptionTrack> {
        val tracks = mutableListOf<CaptionTrack>()

        val captionTracks = captionsJson["captionTracks"]?.jsonArray ?: return tracks

        for (caption in captionTracks) {
            val obj = caption.jsonObject

            val baseUrl = obj["baseUrl"]?.jsonPrimitive?.content ?: continue
            val languageCode = obj["languageCode"]?.jsonPrimitive?.content ?: continue
            val kind = obj["kind"]?.jsonPrimitive?.content ?: ""
            val isTranslatable = obj["isTranslatable"]?.jsonPrimitive?.booleanOrNull ?: false

            // Extract language name from nested runs
            val language = try {
                obj["name"]?.jsonObject
                    ?.get("runs")?.jsonArray
                    ?.firstOrNull()?.jsonObject
                    ?.get("text")?.jsonPrimitive?.content
                    ?: languageCode
            } catch (e: Exception) {
                languageCode
            }

            tracks.add(
                CaptionTrack(
                    // Bỏ &fmt=srv3 (giống Python line 238)
                    baseUrl = baseUrl.replace("&fmt=srv3", ""),
                    language = language,
                    languageCode = languageCode,
                    isGenerated = kind == "asr",
                    isTranslatable = isTranslatable
                )
            )
        }

        return tracks
    }

    // ═══════════════════════════════════════════════════════════════════
    // Utilities
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Check HTTP response status và throw exception tương ứng.
     * (Port từ Python _raise_http_errors lines 93-100)
     */
    private fun raiseHttpErrors(response: okhttp3.Response, videoId: String) {
        if (response.isSuccessful) return

        if (response.code == 429) {
            throw TranscriptException.IpBlocked(videoId)
        }

        throw TranscriptException.HttpError(videoId, response.code)
    }

    /**
     * Unescape common HTML entities.
     * Xử lý các entities phổ biến nhất mà YouTube sử dụng.
     */
    private fun unescapeHtml(text: String): String {
        return text
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&#x27;", "'")
            .replace("&#x2F;", "/")
    }
}
