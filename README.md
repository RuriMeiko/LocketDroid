# LocketDroid

Native Android clone of [Locket Widget](https://apps.apple.com/us/app/locket-widget/id1600525061) (iOS 2.69.0) — Kotlin + Jetpack Compose + CameraX.

## Features
- Camera flow: live preview, shutter (yellow ring), flash, zoom 0.5x/1x/2x, multi-camera flip
- Capture → MediaStore (Pictures/Locket) → History thumbnail + Feed + home-screen widget update
- Feed/History, Chat (bubbles + emoji shortcuts), Friends sheet, Onboarding — UI matched to iOS screenshots
- Release build: R8 + shrinkResources, baseline profiles

## Build
```
export ANDROID_HOME=$HOME/Android/Sdk
./gradlew assembleRelease
```

APK: `app/build/outputs/apk/release/app-release.apk`

## Status
- Emulator (Pixel 7a API 36): E2E PASS (capture, feed, chat, friends, onboarding)
- Real device (OPPO Find X8 / ColorOS 15, Android 16): camera preview + install verified
