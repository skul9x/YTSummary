import logging
import json
import traceback
import urllib.request

# Setup Logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("YT-Python-Helper")

# Safe import with detailed error reporting
_IMPORT_ERROR = None
try:
    from youtube_transcript_api import (
        YouTubeTranscriptApi,
        TranscriptsDisabled,
        NoTranscriptFound,
        VideoUnavailable,
        CouldNotRetrieveTranscript,
    )
except ImportError as e:
    _IMPORT_ERROR = f"Thiếu thư viện: {getattr(e, 'name', str(e))}"
    logger.error(f"Import failed: {_IMPORT_ERROR}")
except Exception as e:
    _IMPORT_ERROR = f"{type(e).__name__}: {str(e)}"
    logger.error(f"Import failed: {_IMPORT_ERROR}")


def clean_transcript(transcript_data) -> str:
    """
    Clean transcript data: join text, remove timestamps.
    Supports both FetchedTranscriptSnippet dataclass objects and raw dicts.
    """
    text_list = []
    for item in transcript_data:
        if hasattr(item, 'text'):
            text_list.append(item.text)
        elif isinstance(item, dict):
            text_list.append(item.get('text', ''))
        else:
            text_list.append(str(item))
    return " ".join(text_list).replace('\n', ' ').strip()


def get_transcript(video_id: str) -> dict:
    """
    Fetch and clean transcript for a given video_id.
    Returns dict with keys: status, transcript/message, length
    """
    if _IMPORT_ERROR is not None:
        return {"status": "error", "message": _IMPORT_ERROR}

    try:
        tx = YouTubeTranscriptApi()
        transcript_result = tx.fetch(video_id, languages=['vi', 'en'])

        if hasattr(transcript_result, "to_raw_data"):
            data = transcript_result.to_raw_data()
        elif hasattr(transcript_result, "snippets"):
            data = transcript_result.snippets
        else:
            data = transcript_result

        plain_text = clean_transcript(data)

        return {
            "status": "success",
            "transcript": plain_text,
            "length": len(plain_text)
        }

    except Exception as e:
        error_type = type(e).__name__
        error_msg = str(e)

        if 'TranscriptsDisabled' in error_type:
            msg = "Video này đã tắt phụ đề."
        elif 'NoTranscriptFound' in error_type:
            msg = "Không tìm thấy phụ đề phù hợp (vi/en)."
        elif 'VideoUnavailable' in error_type:
            msg = "Video không tồn tại hoặc bị ẩn."
        elif 'blocked' in error_msg.lower() or 'IpBlocked' in error_type or 'RequestBlocked' in error_type:
            msg = "YouTube đang chặn request. Thử lại sau."
        else:
            msg = f"[{error_type}] {error_msg[:500]}"

        logger.error(f"get_transcript error: {msg}")
        return {"status": "error", "message": msg}


def get_metadata(video_id: str) -> dict:
    """
    Fetch YouTube video metadata using oEmbed (no API key required).
    """
    video_url = f"https://www.youtube.com/watch?v={video_id}"
    oembed_url = f"https://www.youtube.com/oembed?url={video_url}&format=json"

    try:
        with urllib.request.urlopen(oembed_url) as response:
            data = json.loads(response.read().decode())
            return {
                "status": "success",
                "video_id": video_id,
                "title": data.get("title", "Unknown Title"),
                "thumbnail_url": data.get("thumbnail_url", ""),
                "author_name": data.get("author_name", ""),
            }
    except Exception as e:
        logger.error(f"Metadata error for {video_id}: {str(e)}")
        return {
            "status": "fallback",
            "video_id": video_id,
            "title": f"Video {video_id}",
            "thumbnail_url": f"https://i.ytimg.com/vi/{video_id}/hqdefault.jpg",
        }
