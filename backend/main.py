from fastapi import FastAPI, HTTPException, Query, Request
from fastapi.middleware.cors import CORSMiddleware
from youtube_transcript_api import (
    YouTubeTranscriptApi,
    TranscriptsDisabled,
    NoTranscriptFound,
    VideoUnavailable,
    CouldNotRetrieveTranscript,
)
import uvicorn
import logging
import httpx
import os
from requests import Session
import xml.etree.ElementTree as ET
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
    version="1.1.0"
)
app.state.limiter = limiter
app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)

@app.on_event("startup")
async def startup_event():
    port = os.getenv("PORT", "8000")
    logger.info(f"### [YTSummary] Backend is starting on port {port} ###")
    logger.info(f"### [YTSummary] Origins: {ALLOWED_ORIGINS} ###")

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

def get_proxy_config() -> dict | None:
    """
    Lấy cấu hình proxy từ biến môi trường.
    Hỗ trợ 2 cách:
    - YOUTUBE_PROXY_URL: dùng chung cho HTTP/HTTPS
    - YOUTUBE_HTTP_PROXY + YOUTUBE_HTTPS_PROXY
    """
    proxy_url = os.getenv("YOUTUBE_PROXY_URL", "").strip()
    http_proxy = os.getenv("YOUTUBE_HTTP_PROXY", "").strip()
    https_proxy = os.getenv("YOUTUBE_HTTPS_PROXY", "").strip()

    if proxy_url:
        http_proxy = http_proxy or proxy_url
        https_proxy = https_proxy or proxy_url

    if not http_proxy and not https_proxy:
        return None

    return {
        "http": http_proxy or https_proxy,
        "https": https_proxy or http_proxy,
    }

def fetch_transcript_compatible(video_id: str, languages: list[str]) -> list:
    """
    Tương thích cả API cũ (get_transcript) và mới (instance.fetch).
    """
    proxy_config = get_proxy_config()

    if hasattr(YouTubeTranscriptApi, "get_transcript"):
        kwargs = {"languages": languages}
        if proxy_config:
            kwargs["proxies"] = proxy_config
        return YouTubeTranscriptApi.get_transcript(video_id, **kwargs)

    if proxy_config:
        http_client = Session()
        http_client.proxies.update(proxy_config)
        ytt_api = YouTubeTranscriptApi(http_client=http_client)
    else:
        ytt_api = YouTubeTranscriptApi()

    fetched_transcript = ytt_api.fetch(video_id, languages=languages)

    if hasattr(fetched_transcript, "to_raw_data"):
        return fetched_transcript.to_raw_data()

    return fetched_transcript

@app.get("/")
async def read_root(request: Request):
    return {"status": "online", "message": "YouTube Transcript API is running."}

@app.get("/health")
async def health_check():
    return {"status": "ok"}

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
        transcript_list = fetch_transcript_compatible(video_id, ['vi', 'en'])
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
    except CouldNotRetrieveTranscript as e:
        message = str(e)
        if "blocking requests from your IP" in message:
            raise HTTPException(
                status_code=503,
                detail="YouTube đang chặn IP backend. Hãy cấu hình proxy quay vòng để lấy transcript ổn định trên cloud."
            )
        logger.error(f"Transcript retrieve error: {message}")
        raise HTTPException(status_code=502, detail="Không thể lấy phụ đề từ YouTube ở thời điểm hiện tại.")
    except ET.ParseError:
        raise HTTPException(status_code=502, detail="Dữ liệu phụ đề YouTube không hợp lệ, vui lòng thử lại sau.")
    except Exception as e:
        logger.error(f"System Error: {str(e)}")
        # Che giấu chi tiết lỗi backend để bảo mật
        raise HTTPException(status_code=500, detail="Có lỗi xảy ra trên máy chủ, vui lòng thử lại sau.")

if __name__ == "__main__":
    # Lấy port từ env (ưu tiên Railway PORT), mặc định 8000 khi chạy local
    port = int(os.environ.get("PORT", 8000))
    logger.info(f"YTSummary Server running locally on port {port}")
    uvicorn.run(app, host="0.0.0.0", port=port)
