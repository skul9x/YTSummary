from fastapi import FastAPI, HTTPException, Query, Request
from fastapi.middleware.cors import CORSMiddleware
from youtube_transcript_api import YouTubeTranscriptApi, TranscriptsDisabled, NoTranscriptFound, VideoUnavailable
import uvicorn
import logging
import httpx
import os
from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.util import get_remote_address
from slowapi.errors import RateLimitExceeded

# Setup Logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("YTSummary-Backend")

# Rate Limiter setup
limiter = Limiter(key_func=get_remote_address)
app = FastAPI(
    title="YouTube Summarizer Backend Proxy",
    description="API lấy Transcript từ YouTube Video ID (v4.0 Production)",
    version="1.0.0"
)
app.state.limiter = limiter
app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)

# Cấu hình CORS chặt chẽ cho Production
# Khuyến nghị: Set ALLOWED_ORIGINS trong Dashboard Railway (ví dụ: https://your-frontend.railway.app,*)
raw_origins = os.getenv("ALLOWED_ORIGINS", "*") 
ALLOWED_ORIGINS = [origin.strip() for origin in raw_origins.split(",")]

app.add_middleware(
    CORSMiddleware,
    allow_origins=ALLOWED_ORIGINS,
    allow_credentials=True,
    allow_methods=["GET", "POST", "OPTIONS"], # Mở rộng methods nếu cần
    allow_headers=["*"], # Cho phép tất cả header trong production hoặc chỉnh sửa cụ thể
)

def clean_transcript(transcript_data: list) -> str:
    """
    Hàm làm sạch dữ liệu: Chỉ lấy text, xóa timestamp, gộp thành một đoạn văn duy nhất.
    """
    text_list = [item['text'] for item in transcript_data]
    return " ".join(text_list).replace('\n', ' ').strip()

@app.get("/")
@limiter.limit("5/minute")
async def read_root(request: Request):
    return {"status": "online", "message": "YouTube Transcript API is running."}

@app.get("/api/metadata")
@limiter.limit("10/minute")
async def get_metadata(
    request: Request,
    video_id: str = Query(..., description="YouTube Video ID")
):
    """
    Fetch YouTube video metadata (title, thumbnail) using oEmbed.
    No API Key required.
    """
    video_url = f"https://www.youtube.com/watch?v={video_id}"
    oembed_url = f"https://www.youtube.com/oembed?url={video_url}&format=json"
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(oembed_url)
            if response.status_code == 200:
                data = response.json()
                return {
                    "video_id": video_id,
                    "title": data.get("title", "Unknown Title"),
                    "thumbnail_url": data.get("thumbnail_url", ""),
                    "author_name": data.get("author_name", ""),
                    "status": "success"
                }
            else:
                return {
                    "video_id": video_id,
                    "title": f"Video {video_id}",
                    "thumbnail_url": f"https://i.ytimg.com/vi/{video_id}/hqdefault.jpg",
                    "status": "partial"
                }
        except Exception as e:
            logger.error(f"Error fetching metadata for {video_id}: {str(e)}")
            return {
                "video_id": video_id,
                "title": f"Video {video_id}",
                "thumbnail_url": f"https://i.ytimg.com/vi/{video_id}/hqdefault.jpg",
                "status": "fallback"
            }

@app.get("/api/transcript")
@limiter.limit("10/minute")
async def get_transcript(
    request: Request,
    video_id: str = Query(..., description="YouTube Video ID")
):
    """
    Endpoint chính lấy transcript. Tự động ưu tiên Tiếng Việt (vi), sau đó là Tiếng Anh (en).
    """
    logger.info(f"Fetching transcript for video: {video_id}")
    
    try:
        ytt_api = YouTubeTranscriptApi()
        fetched_transcript = ytt_api.fetch(video_id, languages=['vi', 'en'])
        transcript_list = fetched_transcript.to_raw_data()
        plain_text = clean_transcript(transcript_list)
        
        return {
            "video_id": video_id,
            "status": "success",
            "length": len(plain_text),
            "transcript": plain_text
        }

    except TranscriptsDisabled:
        raise HTTPException(status_code=400, detail="Video này đã tắt phụ đề.")
    except NoTranscriptFound:
        raise HTTPException(status_code=404, detail="Không tìm thấy phụ đề phù hợp.")
    except VideoUnavailable:
        raise HTTPException(status_code=404, detail="Video không tồn tại.")
    except Exception as e:
        logger.error(f"System Error: {str(e)}")
        # Che giấu chi tiết lỗi backend để bảo mật
        raise HTTPException(status_code=500, detail="Có lỗi xảy ra trên máy chủ, vui lòng thử lại sau.")

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
