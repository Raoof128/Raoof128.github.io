#!/bin/bash
# Mehr Guard Build Script
# Builds all platform targets

set -e

echo "🛡️ Mehr Guard Build Script"
echo "========================="

# Check for required tools
command -v java >/dev/null 2>&1 || { echo "❌ Java is required but not installed."; exit 1; }

# Navigate to project root
cd "$(dirname "$0")/.."

echo "📦 Building common module..."
./gradlew :common:build

echo "🤖 Building Android app..."
./gradlew :androidApp:assembleDebug

echo "🖥️ Building Desktop app..."
./gradlew :desktopApp:build

echo "🌐 Building Web app..."
./gradlew :webApp:jsBrowserProductionWebpack

echo ""
echo "✅ All builds completed successfully!"
echo ""
echo "Artifacts:"
echo "  Android APK: androidApp/build/outputs/apk/debug/androidApp-debug.apk"
echo "  Desktop JAR: desktopApp/build/libs/desktopApp.jar"
echo "  Web Bundle:  webApp/build/distributions/"
