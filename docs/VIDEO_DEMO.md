# 🎬 QR-SHIELD Video Demo

> **Quick visual demonstration of QR-SHIELD's capabilities**

---

## 📺 Watch the Demo

<!-- TODO: Replace with actual video embed when ready -->

**Video coming soon!**

The demo will showcase:
- 📷 Real-time QR code scanning
- 🧠 Instant threat analysis (<5ms)
- ✅ Clear verdict display (SAFE/SUSPICIOUS/MALICIOUS)
- 🔍 Explainable reasons for each verdict
- 🌐 Cross-platform functionality

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

---

## 📱 Platform Demos

| Platform | How to Run |
|----------|------------|
| **Web** | [raoof128.github.io](https://raoof128.github.io) |
| **Android** | [Download APK](../releases/QRShield-1.1.0-release.apk) |
| **Desktop** | `./gradlew :desktopApp:run` |
| **iOS** | `./scripts/run_ios_simulator.sh` |

---

*🛡️ Scan smart. Stay protected.*
