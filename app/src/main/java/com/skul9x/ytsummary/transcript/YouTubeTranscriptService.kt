package com.skul9x.ytsummary.transcript

import android.util.Log
import com.skul9x.ytsummary.transcript.exception.TranscriptException
import com.skul9x.ytsummary.transcript.model.CaptionTrack
import com.skul9x.ytsummary.transcript.model.TranscriptResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

/**
 * Implementation chính của TranscriptService.
 * Tổ hợp InnerTubeClient (lấy caption tracks) + TranscriptParser (parse XML).
 *
 * Logic language fallback:
 * - Ưu tiên manual transcript trước generated (auto-generated)
 * - Duyệt qua languages theo thứ tự ưu tiên
 * - Giống Python: find_transcript() checks manual dict first, then generated dict
 */
class YouTubeTranscriptService(
    private val okHttpClient: OkHttpClient
) : TranscriptService {

    companion object {
        private const val TAG = "YTTranscriptService"
    }

    private val innerTubeClient = InnerTubeClient(okHttpClient)
    private val parser = TranscriptParser()

    override suspend fun fetchTranscript(
        videoId: String,
        languages: List<String>
    ): Result<String> {
        return fetchTranscriptResult(videoId, languages).map { it.toPlainText() }
    }

    override suspend fun fetchTranscriptResult(
        videoId: String,
        languages: List<String>
    ): Result<TranscriptResult> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching transcript for $videoId (languages: $languages)")

            // 1. Lấy danh sách caption tracks từ InnerTube
            val tracks = innerTubeClient.fetchCaptionTracks(videoId)
            Log.d(TAG, "Found ${tracks.size} caption tracks: ${tracks.map { "${it.languageCode}(${if (it.isGenerated) "auto" else "manual"})" }}")

            // 2. Phân loại: manual vs generated (giống Python TranscriptList)
            val manualTracks = tracks.filter { !it.isGenerated }
            val generatedTracks = tracks.filter { it.isGenerated }

            // 3. Tìm track theo language priority (manual trước, generated sau)
            val selectedTrack = findTrack(languages, manualTracks, generatedTracks)
                ?: return@withContext Result.failure(
                    TranscriptException.NoTranscriptFound(videoId, languages)
                )

            Log.d(TAG, "Selected track: ${selectedTrack.languageCode} (${selectedTrack.language}, generated=${selectedTrack.isGenerated})")

            // 4. Check PO Token requirement (giống Python Transcript.fetch() line 135)
            if ("&exp=xpe" in selectedTrack.baseUrl) {
                return@withContext Result.failure(
                    TranscriptException.PoTokenRequired(videoId)
                )
            }

            // 5. Fetch transcript XML
            val xml = fetchTranscriptXml(selectedTrack.baseUrl, videoId)

            // 6. Parse XML → snippets
            val snippets = parser.parse(xml)
            Log.d(TAG, "Parsed ${snippets.size} snippets for $videoId")

            Result.success(
                TranscriptResult(
                    snippets = snippets,
                    language = selectedTrack.language,
                    languageCode = selectedTrack.languageCode,
                    isGenerated = selectedTrack.isGenerated,
                    videoId = videoId
                )
            )
        } catch (e: TranscriptException) {
            Log.w(TAG, "TranscriptException for $videoId: ${e.message}")
            Result.failure(e)
        } catch (e: IOException) {
            Log.e(TAG, "Network error for $videoId", e)
            Result.failure(TranscriptException.NetworkError("Lỗi mạng: ${e.message}", e))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error for $videoId", e)
            Result.failure(TranscriptException.HttpError(videoId, -1, e))
        }
    }

    /**
     * Tìm caption track phù hợp theo language priority.
     * Manual trước, generated sau (giống Python TranscriptList._find_transcript).
     */
    private fun findTrack(
        languages: List<String>,
        manualTracks: List<CaptionTrack>,
        generatedTracks: List<CaptionTrack>
    ): CaptionTrack? {
        for (lang in languages) {
            // Thử manual trước
            manualTracks.find { it.languageCode == lang }?.let { return it }
            // Fallback sang generated
            generatedTracks.find { it.languageCode == lang }?.let { return it }
        }
        return null
    }

    /**
     * Fetch raw XML transcript từ timedtext URL.
     * (Tương đương Python Transcript.fetch() → self._http_client.get(self._url))
     */
    private fun fetchTranscriptXml(url: String, videoId: String): String {
        val request = Request.Builder()
            .url(url)
            .header("Accept-Language", "en-US")
            .get()
            .build()

        val response = okHttpClient.newCall(request).execute()

        // Check 429
        if (response.code == 429) {
            throw TranscriptException.IpBlocked(videoId)
        }

        if (!response.isSuccessful) {
            throw TranscriptException.HttpError(videoId, response.code)
        }

        return response.body?.string()
            ?: throw TranscriptException.DataUnparsable(videoId)
    }
}
