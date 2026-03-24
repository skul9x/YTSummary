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

# Port Railway sẽ tự động cung cấp biến môi trường $PORT
# Chúng ta không cần EXPOSE nữa, Railway sẽ tự quản lý
# ENV PORT=8000 - Bạn có thể set default nếu chạy local

# Lệnh chạy server: Chạy trực tiếp qua script để hỗ trợ port động tốt hơn
CMD ["python", "main.py"]
