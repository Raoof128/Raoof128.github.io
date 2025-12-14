#!/bin/bash
# ═══════════════════════════════════════════════════════════════════════════
# QR-SHIELD iOS Demo Builder for Judges
# ═══════════════════════════════════════════════════════════════════════════
#
# This script builds the iOS KMP framework and opens Xcode for quick
# evaluation. No Xcode configuration required!
#
# Usage: ./scripts/build_ios_demo.sh
# ═══════════════════════════════════════════════════════════════════════════

set -e

echo "🛡️  QR-SHIELD iOS Demo Builder"
echo "════════════════════════════════════════════════════════════"
echo ""

# Check for Xcode
if ! command -v xcodebuild &> /dev/null; then
    echo "❌ Error: Xcode is not installed or not in PATH"
    echo "   Please install Xcode from the Mac App Store"
    exit 1
fi

# Check for Gradle wrapper
if [ ! -f "./gradlew" ]; then
    echo "❌ Error: gradlew not found. Are you in the project root?"
    exit 1
fi

# Detect architecture
ARCH=$(uname -m)
if [ "$ARCH" = "arm64" ]; then
    FRAMEWORK_TARGET="linkDebugFrameworkIosSimulatorArm64"
    echo "📱 Detected: Apple Silicon Mac (arm64)"
else
    FRAMEWORK_TARGET="linkDebugFrameworkIosX64"
    echo "📱 Detected: Intel Mac (x64)"
fi

echo ""
echo "Step 1/3: Building KMP iOS Framework..."
echo "────────────────────────────────────────────────────────────"
./gradlew :common:$FRAMEWORK_TARGET --quiet

echo ""
echo "✅ Framework built successfully!"
echo ""
echo "Step 2/3: Opening Xcode..."
echo "────────────────────────────────────────────────────────────"
open iosApp/QRShield.xcodeproj

echo ""
echo "Step 3/3: Ready to run!"
echo "════════════════════════════════════════════════════════════"
echo ""
echo "  📱 In Xcode:"
echo "     1. Select 'iPhone 16 Pro' simulator (or any iOS 17+)"
echo "     2. Press ⌘+R to build and run"
echo ""
echo "  🧪 Test URLs to try:"
echo "     • MALICIOUS: https://paypa1-secure.tk/login"
echo "     • SAFE: https://google.com"
echo ""
echo "  ℹ️  First build may take 1-2 minutes while Xcode indexes."
echo ""
echo "🛡️  Happy judging!"
