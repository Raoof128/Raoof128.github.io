# Mehr Guard Icon Integration Guide

> **Complete guide for integrating app icons across all 5 platforms**

---

## 📦 Source Icons

The master icon set is located in `/mehr-guard-iconset/`:

| File | Size | Purpose |
|------|------|---------|
| `mehr-guard-icon-1024.png` | 1024×1024 | Master source file |
| `Mehr Guard.iconset/` | Various | macOS iconset bundle |

### Available Sizes in iconset

| File | Size | Retina |
|------|------|--------|
| `icon_16x16.png` | 16×16 | No |
| `icon_16x16@2x.png` | 32×32 | Yes (16pt) |
| `icon_32x32.png` | 32×32 | No |
| `icon_32x32@2x.png` | 64×64 | Yes (32pt) |
| `icon_128x128.png` | 128×128 | No |
| `icon_128x128@2x.png` | 256×256 | Yes (128pt) |
| `icon_256x256.png` | 256×256 | No |
| `icon_256x256@2x.png` | 512×512 | Yes (256pt) |
| `icon_512x512.png` | 512×512 | No |
| `icon_512x512@2x.png` | 1024×1024 | Yes (512pt) |

---

## 🤖 Android Integration

### Location
```
androidApp/src/main/res/
├── mipmap-mdpi/ic_launcher.png          (48×48)
├── mipmap-hdpi/ic_launcher.png          (72×72)
├── mipmap-xhdpi/ic_launcher.png         (96×96)
├── mipmap-xxhdpi/ic_launcher.png        (144×144)
├── mipmap-xxxhdpi/ic_launcher.png       (192×192)
└── mipmap-anydpi-v26/ic_launcher.xml    (Adaptive icon definition)
```

### Required Sizes

| Density | Size (px) | Scale |
|---------|-----------|-------|
| mdpi | 48×48 | 1x |
| hdpi | 72×72 | 1.5x |
| xhdpi | 96×96 | 2x |
| xxhdpi | 144×144 | 3x |
| xxxhdpi | 192×192 | 4x |

### Adaptive Icon (Android 8.0+)

Create `ic_launcher.xml` in `mipmap-anydpi-v26/`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background"/>
    <foreground android:drawable="@drawable/ic_launcher_foreground"/>
    <monochrome android:drawable="@drawable/ic_launcher_foreground"/>
</adaptive-icon>
```

### AndroidManifest.xml

```xml
<application
    android:icon="@mipmap/ic_launcher"
    android:roundIcon="@mipmap/ic_launcher_round"
    ...>
```

### Using Android Studio Image Asset Studio

1. Right-click `res/` → New → Image Asset
2. Select "Launcher Icons (Adaptive and Legacy)"
3. Import `mehr-guard-icon-1024.png` as foreground
4. Configure background color
5. Generate all densities automatically

---

## 🍎 iOS Integration

### Location
```
iosApp/MehrGuard/Assets.xcassets/
├── AppIcon.appiconset/
│   ├── Contents.json
│   ├── app-icon-1024.png      (1024×1024, App Store)
│   ├── app-icon-1024-dark.png (1024×1024, Dark mode)
│   └── app-icon-1024-tinted.png (1024×1024, Tinted)
└── Logo.imageset/
    ├── logo.png    (1x)
    ├── logo@2x.png (2x)
    └── logo@3x.png (3x)
```

### Required Sizes (iOS 18+)

With iOS 18, you only need:
- **1024×1024** single icon (system auto-scales)
- Optional: Dark mode variant
- Optional: Tinted variant

### Contents.json Example

```json
{
  "images" : [
    {
      "filename" : "app-icon-1024.png",
      "idiom" : "universal",
      "platform" : "ios",
      "size" : "1024x1024"
    },
    {
      "appearances" : [
        {
          "appearance" : "luminosity",
          "value" : "dark"
        }
      ],
      "filename" : "app-icon-1024-dark.png",
      "idiom" : "universal",
      "platform" : "ios",
      "size" : "1024x1024"
    }
  ],
  "info" : {
    "author" : "xcode",
    "version" : 1
  }
}
```

### Integration Steps

1. Open `iosApp/MehrGuard.xcodeproj` in Xcode
2. Navigate to Assets.xcassets → AppIcon
3. Drag `mehr-guard-icon-1024.png` to the 1024×1024 slot
4. Build to verify

---

## 🖥️ Desktop (Compose Desktop) Integration

### Location
```
desktopApp/src/desktopMain/resources/
├── icon.ico       (Windows)
├── icon.icns      (macOS)
└── icon.png       (Linux)
```

### Required Formats

| OS | Format | How to Create |
|----|--------|---------------|
| Windows | `.ico` | Use online converter or ImageMagick |
| macOS | `.icns` | Use `iconutil` or Xcode |
| Linux | `.png` | Direct from source |

### Creating .icns from iconset (macOS)

```bash
cd /path/to/mehr-guard-iconset
iconutil -c icns Mehr Guard.iconset -o icon.icns
```

### Creating .ico (ImageMagick)

```bash
convert mehr-guard-icon-1024.png -define icon:auto-resize=256,128,64,48,32,16 icon.ico
```

### build.gradle.kts Configuration

```kotlin
compose.desktop {
    application {
        mainClass = "MainKt"
        
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Mehr Guard"
            packageVersion = "1.20.30"
            
            windows {
                iconFile.set(project.file("src/desktopMain/resources/icon.ico"))
            }
            macOS {
                iconFile.set(project.file("src/desktopMain/resources/icon.icns"))
            }
            linux {
                iconFile.set(project.file("src/desktopMain/resources/icon.png"))
            }
        }
    }
}
```

### Window Icon (Runtime)

```kotlin
fun main() = application {
    val icon = painterResource("icon.png")
    
    Window(
        onCloseRequest = ::exitApplication,
        icon = icon,
        title = "Mehr Guard"
    ) {
        App()
    }
}
```

---

## 🌐 Web App Integration

### Location
```
webApp/src/jsMain/resources/
├── index.html
├── assets/
│   ├── favicon-16.png
│   ├── favicon-32.png
│   ├── icon-128.png
│   ├── icon-256.png
│   ├── icon-512.png
│   └── logo.svg
└── manifest.json
```

### Required PWA Sizes

| Size | Purpose |
|------|---------|
| 16×16 | Favicon (tab icon) |
| 32×32 | Favicon (high-DPI) |
| 128×128 | Chrome Web Store |
| 192×192 | Android home screen |
| 256×256 | PWA manifest |
| 512×512 | PWA splash screen |

### index.html

```html
<head>
    <link rel="icon" type="image/png" sizes="32x32" href="assets/favicon-32.png">
    <link rel="icon" type="image/png" sizes="16x16" href="assets/favicon-16.png">
    <link rel="apple-touch-icon" sizes="180x180" href="assets/icon-256.png">
    <link rel="manifest" href="manifest.json">
</head>
```

### manifest.json

```json
{
    "name": "Mehr Guard",
    "short_name": "Mehr Guard",
    "icons": [
        {
            "src": "assets/icon-128.png",
            "sizes": "128x128",
            "type": "image/png"
        },
        {
            "src": "assets/icon-256.png",
            "sizes": "256x256",
            "type": "image/png"
        },
        {
            "src": "assets/icon-512.png",
            "sizes": "512x512",
            "type": "image/png",
            "purpose": "any maskable"
        }
    ],
    "theme_color": "#4F8BFF",
    "background_color": "#0F172A",
    "display": "standalone"
}
```

---

## 🧩 Wasm App

Wasm uses the same web assets as the JS web app. No additional configuration needed.

---

## 🔧 Icon Generation Script

Create `scripts/generate-icons.sh`:

```bash
#!/bin/bash
# Generate all icon sizes from master 1024×1024 image

SOURCE="mehr-guard-iconset/mehr-guard-icon-1024.png"

# Android mipmap
mkdir -p androidApp/src/main/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}
sips -z 48 48 "$SOURCE" --out androidApp/src/main/res/mipmap-mdpi/ic_launcher.png
sips -z 72 72 "$SOURCE" --out androidApp/src/main/res/mipmap-hdpi/ic_launcher.png
sips -z 96 96 "$SOURCE" --out androidApp/src/main/res/mipmap-xhdpi/ic_launcher.png
sips -z 144 144 "$SOURCE" --out androidApp/src/main/res/mipmap-xxhdpi/ic_launcher.png
sips -z 192 192 "$SOURCE" --out androidApp/src/main/res/mipmap-xxxhdpi/ic_launcher.png

# Web favicons
sips -z 16 16 "$SOURCE" --out webApp/src/jsMain/resources/assets/favicon-16.png
sips -z 32 32 "$SOURCE" --out webApp/src/jsMain/resources/assets/favicon-32.png
sips -z 128 128 "$SOURCE" --out webApp/src/jsMain/resources/assets/icon-128.png
sips -z 256 256 "$SOURCE" --out webApp/src/jsMain/resources/assets/icon-256.png
sips -z 512 512 "$SOURCE" --out webApp/src/jsMain/resources/assets/icon-512.png

# Desktop
cp mehr-guard-iconset/icon_512x512.png desktopApp/src/desktopMain/resources/icon.png
# For .icns, run: iconutil -c icns mehr-guard-iconset/Mehr Guard.iconset -o desktopApp/src/desktopMain/resources/icon.icns

# iOS (1024 only needed for iOS 18+)
cp "$SOURCE" iosApp/MehrGuard/Assets.xcassets/AppIcon.appiconset/app-icon-1024.png

echo "✅ Icons generated for all platforms!"
```

---

## ✅ Verification Checklist

| Platform | Location | Status |
|----------|----------|--------|
| Android | `mipmap-*/ic_launcher.png` | ⏳ |
| Android | `mipmap-anydpi-v26/ic_launcher.xml` | ⏳ |
| iOS | `AppIcon.appiconset/app-icon-1024.png` | ✅ |
| Desktop | `resources/icon.png` | ⏳ |
| Desktop | `resources/icon.icns` | ⏳ |
| Desktop | `resources/icon.ico` | ⏳ |
| Web | `assets/favicon-*.png` | ✅ |
| Web | `assets/icon-*.png` | ✅ |
| Web | `manifest.json` | ✅ |

---

## 📋 Quick Commands

```bash
# Generate macOS .icns from iconset
iconutil -c icns mehr-guard-iconset/Mehr Guard.iconset -o desktopApp/src/desktopMain/resources/icon.icns

# Resize with sips (macOS)
sips -z 512 512 mehr-guard-icon-1024.png --out icon-512.png

# Resize with ImageMagick
convert input.png -resize 512x512 output.png

# Create .ico with ImageMagick
convert input.png -define icon:auto-resize=256,128,64,48,32,16 output.ico
```

---

*Last Updated: December 31, 2025*
*Version: 1.20.30*
