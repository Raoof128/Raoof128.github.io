# 🎬 Mehr Guard Video Demo

> **Quick visual demonstration of Mehr Guard's capabilities**

---

## 📺 Watch the Demo

<!-- TODO: Replace with actual video embed when ready -->

**Video coming soon!**

The demo will showcase:
- 📷 Real-time QR code scanning on all 5 platforms
- 🧠 Instant threat analysis (<5ms) with explainable verdicts
- ✅ Clear verdict display (SAFE/SUSPICIOUS/MALICIOUS)
- 🔍 Dynamic analysis breakdowns (16 flag types)
- 🌐 Cross-platform functionality with full parity
- 🎮 Beat the Bot gamification feature
- 🔴 Red Team developer mode
- 🌍 16 language support

---

## 🧪 Try It Yourself

While waiting for the video, try the **live demo** at:

**[👉 raoof128.github.io](https://raoof128.github.io)**

### Test URLs

| URL | Expected Result |
|-----|-----------------|
| `https://paypa1-secure.tk/login` | 🔴 **MALICIOUS** — Brand impersonation + risky TLD |
| `https://google.com` | 🟢 **SAFE** — No threats detected |
| `https://gооgle.com` (Cyrillic 'о') | 🔴 **MALICIOUS** — Homograph attack |
| `https://bit.ly/xyz` | ⚠️ **SUSPICIOUS** — URL shortener hides destination |
| `http://192.168.1.1/login` | ⚠️ **SUSPICIOUS** — IP address instead of domain |

---

## 📱 Platform Demos

| Platform | How to Run | Version |
|----------|------------|---------|
| **Web** | [raoof128.github.io](https://raoof128.github.io) | v1.20.30 |
| **Android** | [Download APK](../releases/MehrGuard-1.1.0-release.apk) | v1.20.30 |
| **Desktop** | `./gradlew :desktopApp:run` | v1.20.30 |
| **iOS** | `./scripts/run_ios_simulator.sh` | v1.20.30 |

---

## 🎯 Demo Script (For Recording)

### 1. Opening (10 seconds)
- Show app icon and name
- "Mehr Guard: Offline QR Phishing Detection"

### 2. Problem Statement (20 seconds)
- "QRishing attacks increased 587 since 2023"
- "Existing solutions require cloud = privacy nightmare"
- "Mehr Guard is 100% offline, zero data collection"

### 3. Web Demo (60 seconds)
- Open raoof128.github.io
- Paste `https://paypa1-secure.tk/login` → Show MALICIOUS verdict
- Paste `https://google.com` → Show SAFE verdict
- Show analysis breakdown with 16 flag types
- Show "Analyzed offline • No data leaves device"

### 4. Android Demo (45 seconds)
- Open app on phone/emulator
- Scan a QR code with camera
- Show instant verdict (<5ms)
- Show history screen
- **Red Team Mode Demo:**
  - Go to Settings → 7-tap version number
  - Toggle "Red Team Mode" → Developer Mode unlocks
  - Show Red Team scenarios panel in Scanner
  - Tap attack scenarios (Homograph, IP Obfuscation, Brand Impersonation)
  - Each instantly analyzed with detailed breakdown

### 5. iOS Demo (45 seconds)
- Open app on iPhone simulator
- Paste URL and analyze
- Show full parity with Android
- **Red Team Mode Demo (iOS):**
  - Settings → 7-tap version → Developer Mode
  - Toggle Red Team Mode
  - Demonstrate same 14 attack scenarios as Android
- Show VoiceOver accessibility

### 6. Desktop Demo (30 seconds)
- Run via `./gradlew :desktopApp:run`
- Import image with QR code
- Show Red Team chip bar in scanner (always visible)
- Show keyboard shortcuts

### 7. Beat the Bot Game (30 seconds)
- Show gamification training
- Demonstrate URL detection challenge
- Show reset button

### 8. Closing (10 seconds)
- "5 platforms, 1 codebase, 100% offline"
- "Red Team tested across Android, iOS, Desktop, Web"
- "Scan smart. Stay protected."

---

## 🎨 Recording Guidelines

- **Resolution:** 1920x1080 minimum
- **Duration:** 3-5 minutes
- **Audio:** Clear narration or captions
- **Theme:** Dark mode preferred
- **Test URLs:** Use provided examples

---

*🛡️ Scan smart. Stay protected.*
