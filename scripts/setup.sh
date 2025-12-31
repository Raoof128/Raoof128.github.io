#!/bin/bash
# Mehr Guard Setup Script
# Sets up development environment

set -e

echo "🛡️ Mehr Guard Development Setup"
echo "==============================="
echo ""

# Check prerequisites
echo "📋 Checking prerequisites..."

# Check Java
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install JDK 17+."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java 17+ is required. Found version $JAVA_VERSION."
    exit 1
fi
echo "✅ Java $JAVA_VERSION"

# Check Kotlin
if command -v kotlin &> /dev/null; then
    echo "✅ Kotlin $(kotlin -version 2>&1 | head -1)"
else
    echo "⚠️  Kotlin CLI not found (optional, Gradle will use embedded version)"
fi

# Check Android SDK (optional)
if [ -n "$ANDROID_HOME" ]; then
    echo "✅ Android SDK: $ANDROID_HOME"
else
    echo "⚠️  ANDROID_HOME not set (required for Android builds)"
fi

# Check Xcode (macOS only)
if [[ "$OSTYPE" == "darwin"* ]]; then
    if command -v xcodebuild &> /dev/null; then
        echo "✅ Xcode $(xcodebuild -version | head -1)"
    else
        echo "⚠️  Xcode not found (required for iOS builds)"
    fi
fi

echo ""
echo "📦 Setting up Gradle wrapper..."
cd "$(dirname "$0")/.."

# Generate Gradle wrapper if not present
if [ ! -f "gradlew" ]; then
    echo "Generating Gradle wrapper..."
    gradle wrapper --gradle-version 8.5
fi

chmod +x gradlew

echo ""
echo "🔧 Syncing Gradle dependencies..."
./gradlew --refresh-dependencies

echo ""
echo "✅ Setup complete!"
echo ""
echo "Next steps:"
echo "  ./gradlew build              # Build all modules"
echo "  ./gradlew :androidApp:installDebug  # Install Android app"
echo "  ./gradlew :desktopApp:run    # Run desktop app"
echo "  ./gradlew allTests           # Run all tests"
