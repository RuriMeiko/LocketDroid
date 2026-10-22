# LocketDroid — Clone Locket Widget iOS cho Android (native)

## Objective
App Android native (Kotlin + Jetpack Compose + CameraX) clone 1:1 UI của Locket Widget iOS (v2.69.0), mượt như native thật, hỗ trợ đa camera, cài Oppo Find X8.

## Phân công agent
- **agy** = UI: Compose screens, theme, animation, pixel-perfect từ screenshots
- **claude** = logic khó: CameraX multi-camera pipeline, state management, widget, data layer
- **codex** = review/test/build-fix

## Reference materials (ĐỌC TRƯỚC KHI CODE)
- **Screenshots UI chính thức (iPhone 15 Pro Max, 1290×2796):** `/home/rurimeiko/locket-android/ui-ref/shot_0.jpg` … `shot_7.jpg`
  - shot_0: onboarding (logo vàng #F5B800 + headline trắng)
  - shot_1: marketing "Send" (không phải app UI)
  - shot_2: **Rollcall feed** — header dark #1A1A1A, close X #3A3A3A circle, megaphone + "Rollcall" + date "JAN 19–25" #8E8E8E, avatar stack + badge vàng #F5C518, photo card full-width, emoji reactions 😍🔥💖 overlay bottom-left, add-friend smiley+ circle bottom-right, caption bar (avatar + name + "+" button)
  - shot_3: **Chat screen** — back circle #2E332F, avatar center + name pill "Bobby ›" #2A2F2B, timestamp #8A938C, bubbles: sender #E8E8E8 text #1A1A1A, receiver #2A2F2B text white, photo bubble radius 40, reaction badge, input bar: shutter ring vàng + pill field #2E332F "Message..." #8A938C + emoji shortcuts 🔥💖😊
  - shot_4: marketing "Receive"
  - shot_5: **Main camera screen** — top row: megaphone circle + "12 Friends" pill + avatar (bg #2A343B translucent), viewfinder rounded 28px, flash icon top-left, "1×" zoom top-right; control row: gallery icon | shutter trắng + ring vàng #F5B800 | flip icon; history row: thumbnail 48px + "History" + chevron; bottom tab bar capsule #2A343B: keypad | home (active, highlight #4A545C) | chat + badge vàng "2"
  - shot_6: **Friends sheet** — grabber #48484A, title "🔥 20 Friends" trắng bold, subtitle "Add your best friends" (best friends = #FFC107), search pill #2C2C2E "Add a new friend", section "Your friends" #98989F, avatar grid 3 cột ~110px + names trắng, "Add" tile: dashed vàng + "+", "Show 15 more" pill #2C2C2E + squiggle lines, "Suggestions" section, rows: avatar + name + X + Add button vàng #FFC107 text đen
  - shot_7: **History feed** — top: megaphone circle + "Everyone ▾" pill #2C2C2E + avatar; photo card radius 40; timestamp pill gradient #A78BFA→#F0ABFC với icon + "9:41 AM" trắng; sender row: avatar 44px + "Jessica" trắng bold + "13m" #8E8E93; reply bar #2C2C2E "Reply to Jessica..." + 🔥💖😊; bottom toolbar: grid circle | capsule(keyboard + shutter vàng-ring + chat) | share circle
- **App icon:** `/home/rurimeiko/locket-android/ui-ref/appicon_1024.png` (1024×1024)
- **IPA extracted:** `/home/rurimeiko/locket-android/ipa-extracted/Payload/Locket.app/` — toàn bộ UI strings (Localizable.strings), asset names
- **REA evidence:** `/home/rurimeiko/locket-android/rea-analyze.json` (Mach-O arm64, Swift+TCA)

## Design tokens (TỪ SCREENSHOTS — dùng đúng, không tự chế)
- Background chính: #141D24 → #0F171D (gradient dark navy) HOẶC #1C1C1E (pure dark) tùy màn
- Surface/pill: #2C2C2E (opaque) hoặc #2A343B (navy tint)
- Active highlight: #4A545C
- Brand yellow (accent): #F5B800 (shutter ring, badge) / #FFC107 (buttons, links) / #F5C518 (badge)
- Text primary: #FFFFFF / #F2F2F7
- Text secondary: #8E8E93 / #98989F / #8A938C
- Sender bubble: #E8E8E8 với text #1A1A1A
- Receiver bubble: #2A2F2B (hoặc #2C2C2E) với text trắng
- Gradient timestamp pill: #A78BFA → #F0ABFC
- Corner radii: photo card 40px, viewfinder 28px, pills = height/2 (capsule), avatar circle
- Font: SF Pro Rounded → Android dùng Nunito (Google Fonts, rounded geometry gần nhất). Headers ExtraBold, UI text Semibold/Medium.

## Architecture rules
- Kotlin + Jetpack Compose, KHÔNG dùng WebView/cross-platform framework
- Material3 nhưng custom theme (dark only, màu theo tokens trên)
- Camera: CameraX (camera-core 1.4.1), bind UseCase theo cameraId, hỗ trợ đa camera (front/back + extra back lenses qua CameraSelector + availableCameras)
- Photo capture: ImageCapture UseCase, JPEG quality 95+, lưu MediaStore
- Preview: PreviewView trong AndroidView composable
- Animation: Compose animate*AsState với spring physics (giống iOS spring feel)
- Navigation: navigation-compose, 3 tab chính: Feed(History) | Camera | Chat
- State: ViewModel + StateFlow, KHÔNG dùng global singletons

## Feature list (đủ theo iOS)
1. Main camera screen (shot_5): top bar (megaphone, friends pill, avatar), viewfinder, flash/zoom controls, gallery/shutter/flip, history row, bottom tab bar
2. Feed/History (shot_7): filter pill "Everyone ▾", photo cards với timestamp gradient pill, sender row, reply bar với reactions
3. Rollcall weekly feed (shot_2): header với date range, stacked photo feed, emoji reactions, add-friend
4. Chat (shot_3): conversations, text bubbles, photo messages, reactions, input bar với emoji shortcuts
5. Friends sheet (shot_6): search, avatar grid, add tile, suggestions
6. Onboarding (shot_0): logo + headline
7. Home-screen widget (phase 2): photo widget cập nhật — Glance hoặc RemoteViews
8. Camera: multi-camera (front/back flip animation, zoom 0.5×/1×/2× nếu lens có), flash modes, tap-to-focus, pinch zoom, portrait/night nếu CameraExtensions available

## Verification
- Build: `./gradlew assembleDebug` phải pass KHÔNG warning nghiêm trọng
- Install + screenshot compare từng màn với shot tương ứng (user sẽ check)
- Camera phải chạy trên emulator (virtual scene) VÀ Find X8 thật
