#!/bin/bash
# ==============================================================================
# iOS Simulator Runner
# ==============================================================================
# One-command iOS app launch for judges and developers.
#
# Requirements:
# - macOS with Xcode installed
# - Xcode Command Line Tools
#
# Usage:
#   ./scripts/run_ios_simulator.sh
#   ./scripts/run_ios_simulator.sh --device "iPhone 15 Pro"
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
IOS_APP_DIR="$PROJECT_DIR/iosApp"

# Parse arguments
DEVICE_NAME="${1:-iPhone 15}"
if [[ "$1" == "--device" ]]; then
    DEVICE_NAME="$2"
fi

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📱 QR-SHIELD iOS Simulator Runner"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Check for macOS
if [[ "$(uname)" != "Darwin" ]]; then
    echo "❌ This script requires macOS with Xcode."
    echo "   For other platforms, use:"
    echo "   - Android: ./gradlew :androidApp:installDebug"
    echo "   - Desktop: ./gradlew :desktopApp:run"
    echo "   - Web: https://raoof128.github.io"
    exit 1
fi

# Check for Xcode
if ! command -v xcodebuild &> /dev/null; then
    echo "❌ Xcode is not installed or not in PATH."
    echo "   Install Xcode from the App Store, then run:"
    echo "   xcode-select --install"
    exit 1
fi

# Step 1: Build the KMP shared framework
echo "📦 Step 1/4: Building KMP shared framework..."
cd "$PROJECT_DIR"
./gradlew :common:compileKotlinIosSimulatorArm64 \
    --no-daemon \
    --quiet \
    2>&1 | tail -5

echo "✅ KMP framework built"
echo ""

# Step 2: Find or boot simulator
echo "📱 Step 2/4: Finding simulator '$DEVICE_NAME'..."

# Get list of available simulators
SIMULATORS=$(xcrun simctl list devices available | grep -E "iPhone|iPad")

# Find matching simulator
DEVICE_UDID=$(xcrun simctl list devices available | grep "$DEVICE_NAME" | head -1 | grep -oE '[A-F0-9-]{36}')

if [[ -z "$DEVICE_UDID" ]]; then
    echo "⚠️  Simulator '$DEVICE_NAME' not found."
    echo "   Available simulators:"
    echo "$SIMULATORS" | head -10
    echo ""
    echo "   Using first available iPhone simulator..."
    DEVICE_UDID=$(xcrun simctl list devices available | grep "iPhone" | head -1 | grep -oE '[A-F0-9-]{36}')
fi

if [[ -z "$DEVICE_UDID" ]]; then
    echo "❌ No iOS simulators available."
    echo "   Open Xcode > Settings > Platforms to download simulators."
    exit 1
fi

DEVICE_INFO=$(xcrun simctl list devices | grep "$DEVICE_UDID" | head -1)
echo "   Using: $DEVICE_INFO"
echo ""

# Boot simulator if needed
echo "🚀 Step 3/4: Booting simulator..."
xcrun simctl boot "$DEVICE_UDID" 2>/dev/null || true
open -a Simulator --args -CurrentDeviceUDID "$DEVICE_UDID"
sleep 3

echo "✅ Simulator booted"
echo ""

# Step 3: Build and install iOS app
echo "🔨 Step 4/4: Building and installing QR-SHIELD..."
cd "$IOS_APP_DIR"

# Build with xcodebuild
xcodebuild \
    -project QRShield.xcodeproj \
    -scheme QRShield \
    -sdk iphonesimulator \
    -destination "id=$DEVICE_UDID" \
    -configuration Debug \
    build \
    2>&1 | tail -10

# Get the app bundle path
APP_PATH=$(find ~/Library/Developer/Xcode/DerivedData -name "QRShield.app" -path "*/Build/Products/Debug-iphonesimulator/*" 2>/dev/null | head -1)

if [[ -z "$APP_PATH" ]]; then
    echo "⚠️  App not found in DerivedData, attempting alternative build..."
    # Alternative: use derived data directly
    xcodebuild \
        -project QRShield.xcodeproj \
        -scheme QRShield \
        -sdk iphonesimulator \
        -destination "id=$DEVICE_UDID" \
        -configuration Debug \
        install DSTROOT=. \
        2>&1 | tail -5
    APP_PATH="./Applications/QRShield.app"
fi

if [[ -d "$APP_PATH" ]]; then
    echo "📲 Installing app to simulator..."
    xcrun simctl install "$DEVICE_UDID" "$APP_PATH"
    
    echo "🚀 Launching QR-SHIELD..."
    xcrun simctl launch "$DEVICE_UDID" com.qrshield.ios
    
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "✅ QR-SHIELD is now running on iOS Simulator!"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "Try these test URLs in the app:"
    echo "  🟢 Safe:      https://google.com"
    echo "  🔴 Malicious: https://paypa1-secure.tk/login"
    echo "  ⚠️  Suspicious: https://bit.ly/3xYz123"
    echo ""
    echo "The Simulator window should now be in focus."
else
    echo "⚠️  Could not locate built app."
    echo "   Please open Xcode and build manually:"
    echo "   open $IOS_APP_DIR/QRShield.xcodeproj"
    echo ""
    echo "   Or run with Xcode command:"
    echo "   xcodebuild -project QRShield.xcodeproj -scheme QRShield -sdk iphonesimulator run"
fi

cd "$PROJECT_DIR"
