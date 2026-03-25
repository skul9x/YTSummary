# Kế hoạch: Di chuyển thông báo Update "youtube-transcript-api"
Created: 2026-03-25T19:18
Status: 🟡 In Progress

## Mục tiêu
Di chuyển banner cảnh báo cập nhật bản mới từ `SettingsScreen` sang thẻ `MainScreen` giúp dev nhận biết khi truy cập màn hình chính

## Chi tiết các phase

### Phase 01: Cập nhật `SummaryViewModel`
Thêm State Flow đón giữ các thông tin cập nhật Python và chuyển logic từ file view về file viewModel ở đây.

### Phase 02: Cập nhật `MainScreen`
Import `PythonUpdateBanner` và gọi truyền vào State lấy ra từ viewModel. Thêm banner tại một vị trí phù hợp trong `MainActivity.kt`.

### Phase 03: Loại bỏ từ `SettingsScreen`
Xóa việc get State và hiển thị Banner ra khỏi màn hình Cài Đặt (do nó được di chuyển ra màn MainActivity rồi).

### Phase 04: Build and Test
Chạy Unit Test hoặc Test UI xem banner thể hiện đúng mục đích không và logic `checkForUpdate()` chạy trong thời gian bao lâu. 
