#!/bin/bash
# Mehr Guard Test Runner
# Runs all platform tests

set -e

echo "🧪 Mehr Guard Test Runner"
echo "========================"

cd "$(dirname "$0")/.."

echo ""
echo "📋 Running common module tests..."
./gradlew :common:allTests --info

echo ""
echo "🤖 Running Android tests..."
./gradlew :androidApp:testDebugUnitTest

echo ""
echo "🖥️ Running Desktop tests..."
./gradlew :desktopApp:desktopTest

echo ""
echo "✅ All tests completed!"
echo ""

# Generate coverage report if available
if ./gradlew tasks | grep -q "koverReport"; then
    echo "📊 Generating coverage report..."
    ./gradlew koverReport
    echo "Coverage report: build/reports/kover/html/index.html"
fi
