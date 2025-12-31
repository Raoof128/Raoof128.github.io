# 🖥️ Mehr Guard Desktop App

> **Compose Desktop implementation for Windows, macOS, and Linux**

---

## ✨ Features

- **Full URL Analysis** — Same detection engine as mobile apps
- **QR Code Scanning** — From image files (PNG, JPG, GIF, BMP)
- **Drag & Drop** — Drop QR code images for instant analysis
- **URL Paste** — Ctrl/Cmd+V to paste and analyze URLs
- **History** — View past scan results
- **Dark Mode** — System-aware theming

---

## 🚀 Quick Start

### Run from Source

```bash
# Prerequisites: JDK 17+
cd mehrguard

# Run directly (recommended for development)
./gradlew :desktopApp:run

# With arguments
./gradlew :desktopApp:run --args="--url https://example.com"
```

### Build Standalone JAR

```bash
# Build fat JAR for current OS
./gradlew :desktopApp:packageUberJarForCurrentOS

# Output: desktopApp/build/compose/jars/MehrGuard-*.jar

# Run standalone
java -jar desktopApp/build/compose/jars/MehrGuard-*.jar
```

### Package Native Distribution

```bash
# Create native installer (DMG on macOS, MSI on Windows, DEB on Linux)
./gradlew :desktopApp:package

# macOS DMG
./gradlew :desktopApp:packageDmg

# Windows MSI
./gradlew :desktopApp:packageMsi

# Linux DEB
./gradlew :desktopApp:packageDeb
```

---

## 📸 Screenshots

```
┌──────────────────────────────────────────────────────────────────────────────┐
│  🛡️ Mehr Guard Desktop                                          🗕  🗗  ✕   │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌────────────────────────────────┐  ┌────────────────────────────────────┐ │
│  │                                │  │                                    │ │
│  │     📤 Drop QR Image Here      │  │   📊 ANALYSIS RESULTS              │ │
│  │                                │  │   ─────────────────────────────    │ │
│  │    or paste URL (Ctrl+V)       │  │                                    │ │
│  │                                │  │   Score: 87/100                    │ │
│  │  ┌─────────────────────────┐  │  │   Verdict: ❌ MALICIOUS            │ │
│  │  │ 🔗 Enter URL...          │  │  │   Confidence: HIGH                 │ │
│  │  └─────────────────────────┘  │  │                                    │ │
│  │                                │  │   ─────────────────────────────    │ │
│  │  [ 📂 Open File ] [ 🔍 Scan ]  │  │                                    │ │
│  │                                │  │   🚨 SIGNALS DETECTED:             │ │
│  └────────────────────────────────┘  │   • Brand Impersonation (+35)     │ │
│                                       │   • Suspicious TLD (+25)          │ │
│  📜 RECENT SCANS                      │   • Credential Path (+12)         │ │
│  ──────────────────────             │                                    │ │
│  • paypa1-secure.tk    ❌ 87      │  │   [ 🔗 Open URL ] [ 🚫 Block ]    │ │
│  • google.com          ✅ 8       │  │                                    │ │
│  • bit.ly/abc123       ⚠️ 45      │  └────────────────────────────────────┘ │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## ⌨️ Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| `Ctrl/Cmd + V` | Paste URL from clipboard |
| `Ctrl/Cmd + O` | Open file picker |
| `Enter` | Analyze current URL |
| `Escape` | Clear input |
| `Ctrl/Cmd + Q` | Quit application |

---

## 🔧 System Requirements

| Platform | Minimum | Recommended |
|----------|---------|-------------|
| **macOS** | macOS 10.13+ | macOS 12+ |
| **Windows** | Windows 10 | Windows 11 |
| **Linux** | Ubuntu 18.04+ | Ubuntu 22.04+ |
| **Java** | JDK 17 | JDK 21 |
| **RAM** | 512MB | 1GB |
| **Disk** | 50MB | 100MB |

---

## 🏗️ Architecture

```
desktopApp/
├── src/desktopMain/kotlin/com/mehrguard/desktop/
│   ├── Main.kt              ← Application entry point
│   ├── ui/
│   │   ├── MainWindow.kt    ← Main Compose window
│   │   ├── ScannerPanel.kt  ← URL input + file drop
│   │   ├── ResultPanel.kt   ← Analysis results display
│   │   └── HistoryPanel.kt  ← Scan history list
│   └── scanner/
│       └── DesktopScanner.kt ← ZXing QR decoder
└── build.gradle.kts          ← Desktop-specific config
```

### Key Dependencies

- **Compose Desktop** — Jetpack Compose for desktop UI
- **ZXing** — QR code decoding from images
- **SQLDelight** — Local history storage

---

## 🧪 Testing

```bash
# Run desktop-specific tests
./gradlew :common:desktopTest

# Run with verbose output
./gradlew :common:desktopTest --info
```

---

## 📦 Distribution

### GitHub Releases

Pre-built JARs are available on the [Releases page](https://github.com/Raoof128/Raoof128.github.io/releases).

### Manual Installation

1. Download `MehrGuard-<version>-desktop.jar`
2. Ensure JDK 17+ is installed
3. Run: `java -jar MehrGuard-<version>-desktop.jar`

---

## ⚠️ Known Limitations

1. **No live camera scanning** — Desktop version only scans from images
2. **First launch slow** — JVM cold start adds ~2-3 seconds
3. **Native packaging requires signing** — Unsigned apps may trigger OS warnings

---

## 📄 License

Apache 2.0 — See [LICENSE](../LICENSE) in root directory.
