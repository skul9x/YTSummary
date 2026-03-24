# Sử dụng Python 3.12 slim để tiết kiệm dung lượng
FROM python:3.12-slim

# Thiết lập thư mục làm việc
WORKDIR /app

# Copy các file cần thiết
COPY requirements.txt .
COPY main.py .

# Cài đặt folder thư viện local (Phải đảm bảo bundle folder này khi gửi lên Railway)
# Ở đây ta giả định folder youtube-transcript-api-master được copy vào backend/
COPY youtube-transcript-api-master /app/youtube-transcript-api-master

# Cài đặt dependencies
RUN pip install --no-cache-dir -r requirements.txt
RUN pip install /app/youtube-transcript-api-master

# Port Railway thường dùng là 8080 hoặc động qua biên môi trường
EXPOSE 8000

# Lệnh chạy server
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
