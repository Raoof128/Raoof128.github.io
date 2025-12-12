#!/bin/bash
# Build KMP iOS Framework for Simulator
# Run this before opening Xcode

set -e

echo "🔨 Building KMP iOS Framework for Simulator..."

cd "$(dirname "$0")/.."

# Build the debug framework for iOS Simulator (Apple Silicon)
./gradlew :common:linkDebugFrameworkIosSimulatorArm64 --no-daemon

# Copy framework to iosApp directory for Xcode linking
FRAMEWORK_SRC="common/build/bin/iosSimulatorArm64/debugFramework/common.framework"
FRAMEWORK_DST="iosApp/Frameworks"

mkdir -p "$FRAMEWORK_DST"
rm -rf "$FRAMEWORK_DST/common.framework"
cp -R "$FRAMEWORK_SRC" "$FRAMEWORK_DST/"

echo ""
echo "✅ Framework built and copied to: $FRAMEWORK_DST/common.framework"
echo ""
echo "📱 Next steps:"
echo "   1. Open iosApp/QRShield.xcodeproj in Xcode"
echo "   2. Select 'QRShield' target → 'General' tab"
echo "   3. Under 'Frameworks, Libraries, and Embedded Content'"
echo "   4. Click '+' → 'Add Other...' → 'Add Files...'"
echo "   5. Navigate to iosApp/Frameworks/common.framework"
echo "   6. Set 'Embed' to 'Embed & Sign'"
echo "   7. Select iPhone Simulator and run!"
echo ""
