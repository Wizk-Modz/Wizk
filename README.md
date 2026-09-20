# Wizk Browser 🌐

[![Android CI](https://github.com/Wizk-Modz/Wizk/actions/workflows/android.yml/badge.svg)](https://github.com/Wizk-Modz/Wizk/actions/workflows/android.yml)
[![Min SDK](https://img.shields.io/badge/minSdk-30%20(Android%2011)-brightgreen.svg)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/targetSdk-34%20(Android%2014)-blue.svg)](https://developer.android.com)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Material 3](https://img.shields.io/badge/Design-Material%203-purple.svg)](https://m3.material.io)

**Wizk Browser** là ứng dụng trình duyệt web Android nhẹ nhàng, hiện đại và bảo mật, được xây dựng trên nền tảng **Android WebView** kết hợp giao diện **Material Design 3**.

---

## ✨ Tính năng nổi bật

- 🚀 **Trải nghiệm duyệt web tối ưu**: Hỗ trợ đầy đủ JavaScript, HTML5 DOM Storage, Database Storage, zoom đa điểm và viewport responsive.
- 🔍 **Thanh địa chỉ thông minh (Smart URL Bar)**:
  - Tự động nhận diện tên miền và chuẩn hóa liên kết (`https://`).
  - Tự động chuyển đổi từ khóa thành truy vấn tìm kiếm Google an toàn.
  - Tự động bôi đen toàn bộ nội dung khi nhấp để tiện nhập URL mới.
- 🧭 **Điều hướng trực quan**:
  - Các nút điều hướng Lùi (Back), Tiến (Forward), Làm mới / Dừng (Reload / Stop) và Trang chủ (Home).
  - Tự động kích hoạt/vô hiệu hóa nút bấm theo lịch sử duyệt web thực tế.
  - Tích hợp `OnBackPressedDispatcher` để lùi trang lịch sử trước khi thoát ứng dụng.
- 📊 **Thanh tiến trình Material 3**: `LinearProgressIndicator` hiển thị tiến độ tải trang từ 0 - 100% mượt mà và tự động ẩn khi hoàn tất.
- 📥 **Quản lý tải xuống linh hoạt**:
  - Bộ tải tích hợp đa luồng dùng HTTP Range, hỗ trợ tạm dừng/tiếp tục trong phiên chạy, theo dõi tiến độ và tốc độ qua Foreground Service Notification.
  - Tùy chọn tải bằng Android `DownloadManager` để lưu vào thư mục `Downloads` công khai của thiết bị.
  - Phát hiện và chuyển liên kết tải đến ứng dụng đã cài như ADM, 1DM, FDM, Gopeed, Download Navi, Aria2App và AB DM.
  - Bộ tải tích hợp lưu tệp tại thư mục riêng `Android/data/com.wizk.app/files/Download/`, tương thích Scoped Storage trên Android 11 trở lên.
- 📤 **Hỗ trợ tải tệp lên**: Tích hợp `WebChromeClient.onShowFileChooser` với hệ thống `ActivityResultLauncher` để người dùng dễ dàng tải ảnh/tệp lên trang web.
- 🔄 **Giữ nguyên trạng thái (State Preservation)**: Cấu hình `configChanges` chống reload trang khi xoay màn hình và phục hồi lịch sử duyệt web qua `saveState`/`restoreState`.

---

## 📁 Cấu trúc mã nguồn

Dự án được tổ chức theo chuẩn kiến trúc module hóa Android:

```
app/src/main/java/com/wizk/app/
├── MainActivity.java                 # UI Controller chính, kết nối ViewBinding và vòng đời Activity
├── PermissionHelper.java             # Tiện ích quản lý quyền hệ thống runtime
├── client/
│   ├── BrowserWebViewClient.java     # Xử lý điều hướng trang, scheme intent (tel:, mailto:), trang lỗi
│   └── BrowserWebChromeClient.java   # Xử lý thanh tiến trình và sự kiện chọn tệp tải lên
├── download/
│   ├── BrowserDownloadListener.java  # Thu thập download request từ WebView
│   ├── model/                        # DownloadTask, Chunk, DownloadRequest, DownloadStatus
│   ├── engine/                       # HTTP Range, tải đa luồng và tiện ích tệp
│   ├── service/                      # Foreground Service, Notification và Pause/Resume
│   ├── custom/                       # Điều hướng sang ADM, 1DM, FDM, Gopeed...
│   └── ui/                           # Hộp thoại chọn phương thức tải
└── util/
    └── UrlUtils.java                 # Tiện ích kiểm tra URL, định dạng tìm kiếm và ẩn bàn phím ảo
```

Tài nguyên giao diện:
```
app/src/main/res/
├── drawable/
│   ├── bg_url_bar.xml                # Bo góc nền thanh địa chỉ
│   ├── ic_arrow_back.xml             # Icon quay lại trang trước
│   ├── ic_arrow_forward.xml          # Icon tiến trang kế tiếp
│   ├── ic_close.xml                  # Icon dừng tải trang
│   ├── ic_home.xml                   # Icon quay về trang chủ
│   └── ic_refresh.xml                # Icon làm mới trang web
├── layout/
│   └── activity_main.xml             # Bố cục giao diện trình duyệt Material 3
└── values/
    ├── colors.xml                    # Bảng màu
    ├── strings.xml                   # Chuỗi tài nguyên và cấu hình URL mặc định
    └── themes.xml                    # Giao diện Material 3 DayNight
```

---

## 🛠️ Yêu cầu môi trường

- **Android SDK**: `compileSdk = 36`, `targetSdk = 34`, `minSdk = 30` (Android 11 trở lên).
- **Ngôn ngữ**: Java 17.
- **Hệ thống build**: Gradle với Kotlin DSL (`build.gradle.kts`).
- **Thư viện chính**:
  - `androidx.core:core:1.17.0`
  - `androidx.appcompat:appcompat:1.8.0`
  - `com.google.android.material:material:1.14.0`
  - `androidx.constraintlayout:constraintlayout:2.2.2`

---

## 🚀 Hướng dẫn biên dịch & Chạy

### 1. Build bản Debug
```bash
./gradlew assembleDebug
```
File APK sẽ được tạo tại: `app/build/outputs/apk/debug/app-debug.apk`

### 2. Build bản Release
```bash
./gradlew assembleRelease
```
File APK sẽ được tạo tại: `app/build/outputs/apk/release/app-release.apk`

### 3. Cài đặt trực tiếp lên thiết bị Android
Đảm bảo đã bật chế độ **Gỡ lỗi USB (USB Debugging)** trên thiết bị:
```bash
./gradlew installDebug
```

---

## 🤖 CI / CD

Dự án được tích hợp sẵn GitHub Actions workflow tại `.github/workflows/android.yml`:
- Tự động biên dịch `assembleRelease` trên máy ảo Ubuntu với JDK 21 khi có commit mới.
- Tự động ký số APK release với keystore cấu hình trong `app/build.gradle.kts`.
- Xuất bản file cài đặt APK dưới dạng GitHub Actions Artifact.

---

## 📄 Bản quyền (License)

Dự án thuộc bản quyền của **Wizk Bytes**. Phát triển theo chuẩn mã nguồn mở Android.
