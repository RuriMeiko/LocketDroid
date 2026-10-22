# LocketDroid Camera & Logic Implementation Notes

- `app/src/main/java/com/locket/droid/camera/CameraController.kt`: CameraX pipeline hoàn chỉnh hỗ trợ đa camera (enumerate availableCameraInfos, switch/flip front↔back bảo lưu zoom state), ImageCapture quality 95 MINIMIZE_LATENCY lưu Pictures/Locket MediaStore, flash OFF/ON/AUTO, pinch zoom, tap-to-focus qua MeteringPoint, và CameraExtensions Night/Portrait an toàn không crash.
- `app/src/main/java/com/locket/droid/camera/CameraViewModel.kt`: StateFlow quản lý currentCameraIndex, availableCameras, availableCameraInfos, flashMode, zoomRatio, isCapturing, lastPhotoUri và các action flip, toggle/toggleFlash, zoom/setZoom, capture/takePicture tích hợp cập nhật Widget và Repository.
- `app/src/main/java/com/locket/droid/data/Photo.kt`: Data class Photo và Friend hỗ trợ id, uri, senderName, timestamp, isMine, reactions, caption, colorSeed và timeAgoFormatted.
- `app/src/main/java/com/locket/droid/data/LocketRepository.kt`: Repository in-memory lưu trữ feed kết hợp tự động scan MediaStore Pictures/Locket, fake friends (Ava, Jessica, Kyle, Alexandra, Dustin, Bobby, Sabrina, Laurel, Greg) và fake feed entries.
- `app/src/main/java/com/locket/droid/data/ChatRepository.kt`: Repository tin nhắn chứa conversation với Bobby chuẩn screenshot shot_3 ("Ya I think that's good", "That's not for a bit right?", photo, "What a style icon 😍", "Come hang ✨", "I'll send you my location!") cùng sendMessage, sendPhoto, react và markRead.
- `app/src/main/java/com/locket/droid/widget/PhotoWidgetProvider.kt`: AppWidgetProvider 4x4 hiển thị ảnh mới nhất từ Pictures/Locket hoặc placeholder, click mở app qua PendingIntent, kèm hàm tiện ích PhotoWidgetProvider.requestUpdate.
- `app/src/main/res/xml/locket_widget_info.xml`: Metadata AppWidget 4x4 (minWidth/minHeight 250dp, targetCell 4x4, updatePeriodMillis, initialLayout).
- `app/src/main/res/layout/widget_locket_photo.xml`: RemoteViews layout 4x4 bo tròn góc 28dp với ImageView fit-center, placeholder trái tim vàng và badge sender.
- `app/src/main/res/drawable/widget_background.xml`: Shape drawable bo góc 28dp màu dark navy (#141D24) cho widget card.
- `app/src/main/res/drawable/widget_badge_background.xml`: Shape drawable bo góc 14dp nền mờ bán trong suốt cho nhãn Locket trên widget.
- `app/src/main/res/drawable/ic_widget_locket_heart.xml`: Vector drawable icon trái tim vàng (#F5B800) đặc trưng Locket dùng trong placeholder widget.
- `app/src/main/res/values/strings.xml`: Định nghĩa chuỗi app_name và widget_description.
- `app/src/main/java/com/locket/droid/LocketApp.kt`: Application class lưu trữ và khởi tạo instance LocketRepository và ChatRepository phục vụ toàn bộ app.
- `app/src/main/AndroidManifest.xml`: Khai báo Application name .LocketApp và receiver PhotoWidgetProvider liên kết với @xml/locket_widget_info.
