from youtube_transcript_api import (
    YouTubeTranscriptApi,
    TranscriptsDisabled,
    NoTranscriptFound,
    VideoUnavailable,
    CouldNotRetrieveTranscript,
)
import logging
import json
import urllib.request

# Setup Logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("YT-Python-Helper")

def clean_transcript(transcript_data: list) -> str:
    """
    Clean transcript data: join text, remove timestamps.
    """
    text_list = [item['text'] for item in transcript_data]
    return " ".join(text_list).replace('\n', ' ').strip()

def get_transcript(video_id: str) -> dict:
    """
    Fetch and clean transcript for a given video_id.
    Returns:
        dict: {
            "status": "success"/"error",
            "transcript": str,
            "message": str (error details if status is error)
        }
    """
    logger.info(f"Fetching transcript locally for video: {video_id}")
    
    try:
        # Replicate existing backend priority: vi, then en
        # Use instantiation for v1.2.4+ (get_transcript is deprecated/removed in some envs)
        tx = YouTubeTranscriptApi()
        transcript_list = tx.fetch(video_id, languages=['vi', 'en'])
        
        # In newer versions, fetch might return an object or direct list. 
        # Checking for to_raw_data() like in the original backend main.py.
        if hasattr(transcript_list, "to_raw_data"):
            data = transcript_list.to_raw_data()
        else:
            data = transcript_list

        plain_text = clean_transcript(data)
        
        return {
            "status": "success",
            "transcript": plain_text,
            "length": len(plain_text)
        }

    except TranscriptsDisabled:
        return {"status": "error", "message": "Video này đã tắt phụ đề."}
    except NoTranscriptFound:
        return {"status": "error", "message": "Không tìm thấy phụ đề phù hợp."}
    except VideoUnavailable:
        return {"status": "error", "message": "Video không tồn tại."}
    except CouldNotRetrieveTranscript as e:
        message = str(e)
        if "blocking requests from your IP" in message:
            return {"status": "error", "message": "YouTube đang chặn request từ IP này."}
        return {"status": "error", "message": f"Không thể lấy phụ đề: {message}"}
    except Exception as e:
        logger.error(f"Internal Python Error: {str(e)}")
        return {"status": "error", "message": f"Lỗi nội bộ: {str(e)}"}

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
        logger.error(f"Error fetching metadata for {video_id}: {str(e)}")
        return {
            "status": "fallback",
            "video_id": video_id,
            "title": f"Video {video_id}",
            "thumbnail_url": f"https://i.ytimg.com/vi/{video_id}/hqdefault.jpg",
        }
