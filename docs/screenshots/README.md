# 📸 QR-SHIELD Screenshots

> **Platform Screenshots** — Visual evidence of QR-SHIELD running across all 4 platforms.

---

## 📱 Required Screenshots

To generate screenshots for the competition submission, run the apps on each platform and capture:

### Android Screenshots

| Screenshot | Description | Filename |
|------------|-------------|----------|
| Dashboard | Main screen with scan stats | `android_dashboard.png` |
| Scanner | Camera view scanning QR | `android_scanner.png` |
| Safe Result | Green checkmark verdict | `android_safe.png` |
| Malicious Result | Red warning with flags | `android_malicious.png` |

### iOS Screenshots

| Screenshot | Description | Filename |
|------------|-------------|----------|
| Dashboard | Main screen with SwiftUI | `ios_dashboard.png` |
| Scanner | AVFoundation camera | `ios_scanner.png` |
| Safe Result | Green verdict card | `ios_safe.png` |
| Malicious Result | Red warning card | `ios_malicious.png` |

### Desktop Screenshots

| Screenshot | Description | Filename |
|------------|-------------|----------|
| Dashboard | Compose Desktop main | `desktop_dashboard.png` |
| Scanner | File/clipboard input | `desktop_scanner.png` |
| Analysis | Detailed threat breakdown | `desktop_analysis.png` |

### Web Screenshots

| Screenshot | Description | Filename |
|------------|-------------|----------|
| Landing | Main page hero | `web_landing.png` |
| Scan Input | URL input field | `web_scan.png` |
| Safe Result | Green badge result | `web_safe.png` |
| Malicious Result | Red warning result | `web_malicious.png` |
| Beat the Bot | Gamification feature | `web_beatthebot.png` |

---

## 🖼️ Generating Screenshots

### Android
```bash
# Run on emulator
./gradlew :androidApp:installDebug
adb shell screencap /sdcard/screenshot.png
adb pull /sdcard/screenshot.png android_dashboard.png
```

### iOS
```bash
# Run simulator
./scripts/run_ios_simulator.sh
# Use Cmd+S in Simulator to capture
```

### Desktop
```bash
# Run desktop app
./gradlew :desktopApp:run
# Use system screenshot (Cmd+Shift+4 on Mac)
```

### Web
```bash
# Open in browser
./gradlew :webApp:jsBrowserDevelopmentRun
# Or visit https://raoof128.github.io
# Use browser DevTools for consistent viewport
```

---

## 📐 Recommended Dimensions

| Platform | Dimensions | Format |
|----------|------------|--------|
| Android | 1080x2340 | PNG |
| iOS | 1170x2532 | PNG |
| Desktop | 1920x1080 | PNG |
| Web | 1440x900 | PNG |

---

## 🎨 Screenshot Guidelines

1. **Use real data** — Don't use placeholder text
2. **Show verdicts** — Capture SAFE, SUSPICIOUS, and MALICIOUS flows
3. **Consistent theme** — Use dark mode on all platforms if possible
4. **Clean state** — Clear cache/history before capturing
5. **Highlight features** — Show key differentiators (explainability, offline badge)

---

*Screenshots are important for judges who may not have time to build/run all platforms.*
