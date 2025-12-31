#!/bin/bash
#
# Mehr Guard Smoke Test Suite
# Quick verification tests for all platforms
#
# Usage: ./scripts/smoke_test.sh [platform]
#   platform: all, common, desktop, android, web
#
# Copyright 2025-2026 Mehr Guard Contributors
# Licensed under Apache 2.0
#

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

PLATFORM=${1:-all}

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}      Mehr Guard Smoke Test Suite${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "Platform: ${YELLOW}$PLATFORM${NC}"
echo -e "Time: $(date)"
echo ""

PASSED=0
FAILED=0

# Test function
run_test() {
    local name=$1
    local command=$2
    
    echo -e "${YELLOW}Testing: $name...${NC}"
    
    if eval "$command" > /dev/null 2>&1; then
        echo -e "${GREEN}  ✅ PASSED${NC}"
        ((PASSED++))
    else
        echo -e "${RED}  ❌ FAILED${NC}"
        ((FAILED++))
    fi
}

# Common module tests (shared detection engine)
test_common() {
    echo -e "\n${BLUE}📦 Common Module Tests${NC}"
    echo "   (Shared detection engine across all platforms)"
    echo ""
    
    run_test "Compile Kotlin" "./gradlew :common:compileKotlinDesktop --quiet"
    run_test "Detection Engine Tests" "./gradlew :common:desktopTest --tests 'com.raouf.mehrguard.engine.*' --quiet"
    run_test "ML Model Tests" "./gradlew :common:desktopTest --tests 'com.raouf.mehrguard.ml.*' --quiet"
    run_test "All Common Tests" "./gradlew :common:desktopTest --quiet"
}

# Desktop app tests
test_desktop() {
    echo -e "\n${BLUE}🖥️  Desktop App Tests${NC}"
    echo ""
    
    run_test "Compile Desktop" "./gradlew :desktopApp:compileKotlinDesktop --quiet"
    run_test "Desktop Tests" "./gradlew :desktopApp:desktopTest --quiet"
    
    echo ""
    echo "   To run the desktop app: ./gradlew :desktopApp:run"
}

# Android app tests (requires Android SDK)
test_android() {
    echo -e "\n${BLUE}📱 Android App Tests${NC}"
    echo ""
    
    if command -v adb &> /dev/null; then
        run_test "Compile Android" "./gradlew :androidApp:compileDebugKotlin --quiet"
        run_test "Android Unit Tests" "./gradlew :androidApp:testDebugUnitTest --quiet"
        
        # Check if emulator/device is connected
        if adb devices | grep -q "device$"; then
            echo -e "   ${GREEN}Device detected! Running instrumentation tests...${NC}"
            run_test "Instrumentation Tests" "./gradlew :androidApp:connectedDebugAndroidTest --quiet"
        else
            echo -e "   ${YELLOW}No device connected - skipping instrumentation tests${NC}"
        fi
    else
        echo -e "${YELLOW}  ⚠️ Android SDK not found - skipping Android tests${NC}"
    fi
}

# Web app tests
test_web() {
    echo -e "\n${BLUE}🌐 Web App Tests${NC}"
    echo ""
    
    run_test "Compile Kotlin/JS" "./gradlew :webApp:compileKotlinJs --quiet"
    
    echo ""
    echo "   To test web: visit https://raoof128.github.io"
    echo "   To run locally: cd webApp && npm run dev"
}

# iOS tests (requires macOS)
test_ios() {
    echo -e "\n${BLUE}📱 iOS App Tests${NC}"
    echo ""
    
    if [[ "$OSTYPE" == "darwin"* ]]; then
        run_test "Compile iOS Framework" "./gradlew :common:linkDebugFrameworkIosSimulatorArm64 --quiet"
        echo ""
        echo "   To run iOS app: open iosApp/MehrGuard.xcodeproj"
    else
        echo -e "${YELLOW}  ⚠️ iOS tests require macOS - skipping${NC}"
    fi
}

# Run selected tests
case $PLATFORM in
    common)
        test_common
        ;;
    desktop)
        test_desktop
        ;;
    android)
        test_android
        ;;
    web)
        test_web
        ;;
    ios)
        test_ios
        ;;
    all)
        test_common
        test_desktop
        test_android
        test_web
        test_ios
        ;;
    *)
        echo -e "${RED}Unknown platform: $PLATFORM${NC}"
        echo "Usage: ./scripts/smoke_test.sh [all|common|desktop|android|web|ios]"
        exit 1
        ;;
esac

# Summary
echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}      Summary${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "  Passed: ${GREEN}$PASSED${NC}"
echo -e "  Failed: ${RED}$FAILED${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✅ All smoke tests passed!${NC}"
    exit 0
else
    echo -e "${RED}❌ Some tests failed. Check output above.${NC}"
    exit 1
fi
