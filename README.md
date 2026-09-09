# 🎓 StudyFlow - Ứng dụng Quản Lý Học Tập & Tập Trung Thông Minh

StudyFlow là ứng dụng Android giúp học sinh, sinh viên quản lý việc học tập hiệu quả, tối ưu hóa thời gian và nâng cao khả năng tập trung thông qua các tính năng thông minh: thời khóa biểu, danh sách công việc, chế độ tập trung (Focus Clock / Pomodoro), đồng bộ đám mây và trợ lý học tập AI.

---

## ✨ Tính Năng Nổi Bật

- 📅 **Quản Lý Thời Khóa Biểu & Lịch Học (Schedule & Courses):**
  - Quản lý danh sách môn học, phòng học, giảng viên.
  - Lập lịch biểu chi tiết và nhắc nhở tự động trước buổi học (`ClassReminderScheduler`).
  - Tự động sắp xếp lại lịch khi có xung đột (`AutoRescheduler`, `SmartScheduler`).

- ✅ **Quản Lý Công Việc & Mục Tiêu Học Tập (Tasks & Study Plans):**
  - Theo dõi deadline, độ ưu tiên công việc.
  - Phân chia kế hoạch học tập theo môn học.

- ⏱️ **Chế Độ Tập Trung (Focus Mode / Pomodoro):**
  - Đồng hồ đếm giờ tập trung trực quan (`FocusRing`, `FocusClock`).
  - Chạy ngầm liên tục với Android Foreground Service (`FocusService`).
  - Thống kê lịch sử các phiên tập trung để theo dõi tiến độ.

- 🤖 **Trợ Lý AI Học Tập (AI Study Assistant):**
  - Tích hợp API phân tích và gợi ý kế hoạch học tập (`AiApiClient`).

- ☁️ **Đồng Bộ Dữ Liệu Đám Mây (Cloud Sync):**
  - Xác thực người dùng qua Firebase Authentication / Google Sign-In.
  - Lưu trữ dữ liệu cục bộ an toàn với Room Database (`StudyFlowDatabase`).
  - Tự động đồng bộ lên Firebase Firestore (`StudyDataSync`).

- 🖥️ **Backend API Server:**
  - Hỗ trợ backend Node.js (`backend/server.js`) cho các dịch vụ mở rộng.

---

## 🛠️ Công Nghệ Sử Dụng

- **Platform:** Android (Java / Android SDK 37, Min SDK 24)
- **Kiến trúc:** MVVM (Model - View - ViewModel)
- **Database:** Room Database, SQLite
- **Cloud & Auth:** Firebase Auth, Firebase Firestore, Google Credential Manager
- **Networking:** Android Volley, REST API Client
- **Background Tasks:** Android Foreground Service, BroadcastReceiver, AlarmManager
- **Backend:** Node.js, Express

---

## 🚀 Tải Về & Cài Đặt (Releases)

Bạn có thể tải file cài đặt APK và mã nguồn tại mục [Releases](https://github.com/Ngoc-hoa06/StudyFlow/releases):
- 📱 **StudyFlow APK:** Tải file `.apk` và cài đặt trực tiếp trên thiết bị Android (Android 7.0 trở lên).
- 📦 **Mã Nguồn (ZIP):** Tải file `.zip` để mở và chạy trên Android Studio.

---

## 💻 Hướng Dẫn Chạy Mã Nguồn Trên Android Studio

1. **Clone repository:**
   ```bash
   git clone https://github.com/Ngoc-hoa06/StudyFlow.git
   ```
2. Mở thư mục dự án bằng **Android Studio**.
3. Chờ Gradle đồng bộ (Sync Project with Gradle Files).
4. Kết nối thiết bị Android hoặc máy ảo (Emulator).
5. Nhấn **Run** (`Shift + F10`) để khởi chạy ứng dụng.