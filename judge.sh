#!/bin/bash
# ============================================================================
# 🛡️ Mehr Guard Judge Build Helper
# ============================================================================
# Quick setup and run commands for competition judges
# Run: ./judge.sh
# ============================================================================

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo ""
echo -e "${PURPLE}╔════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${PURPLE}║${NC}              🛡️  ${CYAN}Mehr Guard Judge Build Helper${NC}              ${PURPLE}║${NC}"
echo -e "${PURPLE}║${NC}                 Kotlin Multiplatform Demo                      ${PURPLE}║${NC}"
echo -e "${PURPLE}╚════════════════════════════════════════════════════════════════╝${NC}"
echo ""

# ============================================================================
# Environment Checks
# ============================================================================

echo -e "${BLUE}📋 Checking Environment...${NC}"
echo ""

# Check Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    echo -e "  ${GREEN}✓${NC} Java: $JAVA_VERSION"
else
    echo -e "  ${RED}✗${NC} Java: NOT FOUND"
    echo -e "    ${YELLOW}Install: brew install openjdk@17${NC}"
fi

# Check Gradle
if [ -f "./gradlew" ]; then
    echo -e "  ${GREEN}✓${NC} Gradle Wrapper: Available"
else
    echo -e "  ${RED}✗${NC} Gradle Wrapper: NOT FOUND"
fi

# Check Node (for web E2E tests)
if command -v node &> /dev/null; then
    NODE_VERSION=$(node --version)
    echo -e "  ${GREEN}✓${NC} Node.js: $NODE_VERSION"
else
    echo -e "  ${YELLOW}○${NC} Node.js: Not installed (optional, for E2E tests)"
fi

# Check Xcode (for iOS)
if command -v xcodebuild &> /dev/null; then
    XCODE_VERSION=$(xcodebuild -version | head -n 1)
    echo -e "  ${GREEN}✓${NC} Xcode: $XCODE_VERSION"
else
    echo -e "  ${YELLOW}○${NC} Xcode: Not installed (optional, for iOS build)"
fi

echo ""

# ============================================================================
# Quick Commands
# ============================================================================

echo -e "${BLUE}🚀 Quick Run Commands${NC}"
echo ""
echo -e "  ${CYAN}1. Web Demo (Fastest - No build needed!)${NC}"
echo -e "     ${GREEN}→ https://raoof128.github.io/?demo=true${NC}"
echo ""
echo -e "  ${CYAN}2. Run All Tests${NC}"
echo -e "     ${YELLOW}./gradlew :common:allTests${NC}"
echo ""
echo -e "  ${CYAN}3. Run Desktop App${NC}"
echo -e "     ${YELLOW}./gradlew :desktopApp:run${NC}"
echo ""
echo -e "  ${CYAN}4. Build Android APK${NC}"
echo -e "     ${YELLOW}./gradlew :androidApp:assembleDebug${NC}"
echo -e "     ${GREEN}→ APK: androidApp/build/outputs/apk/debug/androidApp-debug.apk${NC}"
echo ""
echo -e "  ${CYAN}5. Run Web Locally${NC}"
echo -e "     ${YELLOW}./gradlew :webApp:jsBrowserRun${NC}"
echo -e "     ${GREEN}→ Opens at http://localhost:8080${NC}"
echo ""
echo -e "  ${CYAN}6. Build iOS Framework${NC}"
echo -e "     ${YELLOW}./gradlew :common:linkDebugFrameworkIosSimulatorArm64${NC}"
echo -e "     ${GREEN}→ Then open iosApp/MehrGuard.xcodeproj${NC}"
echo ""

# ============================================================================
# What to Test
# ============================================================================

echo -e "${BLUE}🧪 What to Test${NC}"
echo ""
echo -e "  ${PURPLE}Sample Malicious URL:${NC} https://paypa1-secure.tk/login"
echo -e "  ${PURPLE}Expected Result:${NC} Score 85+, MALICIOUS verdict"
echo -e "  ${PURPLE}Triggered Signals:${NC} Brand Impersonation, Suspicious TLD, Typosquatting"
echo ""
echo -e "  ${GREEN}Sample Safe URL:${NC} https://google.com"
echo -e "  ${GREEN}Expected Result:${NC} Score <20, SAFE verdict"
echo ""

# ============================================================================
# KMP Proof Points
# ============================================================================

echo -e "${BLUE}📐 KMP Architecture Proof${NC}"
echo ""
echo -e "  • Shared code: ${CYAN}common/src/commonMain/kotlin/${NC}"
echo -e "  • Platform code: ${CYAN}androidApp/, iosApp/, desktopApp/, webApp/${NC}"
echo -e "  • Run same test on all platforms: ${YELLOW}./gradlew allTests${NC}"
echo ""

# ============================================================================
# Interactive Menu
# ============================================================================

echo -e "${PURPLE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "${CYAN}What would you like to do?${NC}"
echo ""
echo "  1) Open Web Demo (browser)"
echo "  2) Run unit tests"
echo "  3) Run desktop app"
echo "  4) Build Android APK"
echo "  5) Run web locally (JS)"
echo "  6) Run web locally (Wasm - NEW)"
echo "  7) Exit"
echo ""
read -p "Enter choice [1-7]: " choice

case $choice in
    1)
        echo -e "${GREEN}Opening Web Demo...${NC}"
        open "https://raoof128.github.io/?demo=true" 2>/dev/null || xdg-open "https://raoof128.github.io/?demo=true" 2>/dev/null || echo "Open: https://raoof128.github.io/?demo=true"
        ;;
    2)
        echo -e "${GREEN}Running unit tests...${NC}"
        ./gradlew :common:allTests
        ;;
    3)
        echo -e "${GREEN}Running desktop app...${NC}"
        ./gradlew :desktopApp:run
        ;;
    4)
        echo -e "${GREEN}Building Android APK...${NC}"
        ./gradlew :androidApp:assembleDebug
        echo -e "${GREEN}APK: androidApp/build/outputs/apk/debug/androidApp-debug.apk${NC}"
        ;;
    5)
        echo -e "${GREEN}Starting web server (JS)...${NC}"
        ./gradlew :webApp:jsBrowserRun
        ;;
    6)
        echo -e "${GREEN}Starting web server (Wasm)...${NC}"
        ./gradlew :webApp:wasmJsBrowserRun
        ;;
    7)
        echo -e "${CYAN}Goodbye! 👋${NC}"
        exit 0
        ;;
    *)
        echo -e "${YELLOW}Invalid choice. Run ./judge.sh again.${NC}"
        ;;
esac
