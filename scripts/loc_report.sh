#!/bin/bash
#
# QR-SHIELD Lines of Code Report by SourceSet
# Proves shared code percentage across KMP targets
#
# Usage: ./scripts/loc_report.sh
#
# Copyright 2025-2026 QR-SHIELD Contributors
# Licensed under Apache 2.0
#

set -e

# Colors
BLUE='\033[0;34m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}      QR-SHIELD — Lines of Code by SourceSet${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

# Count LOC for Kotlin files
count_kotlin_loc() {
    local dir=$1
    if [ -d "$dir" ]; then
        find "$dir" -name "*.kt" -exec cat {} + 2>/dev/null | grep -v '^\s*$' | grep -v '^\s*//' | grep -v '^\s*\*' | wc -l | tr -d ' '
    else
        echo "0"
    fi
}

# Count files
count_kotlin_files() {
    local dir=$1
    if [ -d "$dir" ]; then
        find "$dir" -name "*.kt" 2>/dev/null | wc -l | tr -d ' '
    else
        echo "0"
    fi
}

echo -e "${YELLOW}📦 Shared Code (common module):${NC}"
echo ""

# Common module - SHARED across all platforms
COMMON_MAIN=$(count_kotlin_loc "common/src/commonMain/kotlin")
COMMON_MAIN_FILES=$(count_kotlin_files "common/src/commonMain/kotlin")
COMMON_TEST=$(count_kotlin_loc "common/src/commonTest/kotlin")
COMMON_TEST_FILES=$(count_kotlin_files "common/src/commonTest/kotlin")

echo "  common/src/commonMain/  → ${GREEN}$COMMON_MAIN LOC${NC} ($COMMON_MAIN_FILES files)"
echo "  common/src/commonTest/  → ${GREEN}$COMMON_TEST LOC${NC} ($COMMON_TEST_FILES files)"
echo ""

echo -e "${YELLOW}📱 Platform-Specific Implementations (expect/actual):${NC}"
echo ""

# Platform-specific in common module
ANDROID_COMMON=$(count_kotlin_loc "common/src/androidMain/kotlin")
ANDROID_COMMON_FILES=$(count_kotlin_files "common/src/androidMain/kotlin")
IOS_COMMON=$(count_kotlin_loc "common/src/iosMain/kotlin")
IOS_COMMON_FILES=$(count_kotlin_files "common/src/iosMain/kotlin")
DESKTOP_COMMON=$(count_kotlin_loc "common/src/desktopMain/kotlin")
DESKTOP_COMMON_FILES=$(count_kotlin_files "common/src/desktopMain/kotlin")
JS_COMMON=$(count_kotlin_loc "common/src/jsMain/kotlin")
JS_COMMON_FILES=$(count_kotlin_files "common/src/jsMain/kotlin")

echo "  common/src/androidMain/ → ${CYAN}$ANDROID_COMMON LOC${NC} ($ANDROID_COMMON_FILES files)"
echo "  common/src/iosMain/     → ${CYAN}$IOS_COMMON LOC${NC} ($IOS_COMMON_FILES files)"
echo "  common/src/desktopMain/ → ${CYAN}$DESKTOP_COMMON LOC${NC} ($DESKTOP_COMMON_FILES files)"
echo "  common/src/jsMain/      → ${CYAN}$JS_COMMON LOC${NC} ($JS_COMMON_FILES files)"
echo ""

echo -e "${YELLOW}📲 Platform Apps (UI Layer):${NC}"
echo ""

# Platform apps
ANDROID_APP=$(count_kotlin_loc "androidApp/src/main/kotlin")
ANDROID_APP_FILES=$(count_kotlin_files "androidApp/src/main/kotlin")
DESKTOP_APP=$(count_kotlin_loc "desktopApp/src/desktopMain/kotlin")
DESKTOP_APP_FILES=$(count_kotlin_files "desktopApp/src/desktopMain/kotlin")
WEB_APP=$(count_kotlin_loc "webApp/src/jsMain/kotlin")
WEB_APP_FILES=$(count_kotlin_files "webApp/src/jsMain/kotlin")

# iOS is Swift
IOS_SWIFT=$(find iosApp -name "*.swift" 2>/dev/null | xargs cat 2>/dev/null | grep -v '^\s*$' | grep -v '^\s*//' | wc -l | tr -d ' ')
IOS_SWIFT_FILES=$(find iosApp -name "*.swift" 2>/dev/null | wc -l | tr -d ' ')

echo "  androidApp/             → ${CYAN}$ANDROID_APP LOC${NC} ($ANDROID_APP_FILES files) — Compose UI"
echo "  desktopApp/             → ${CYAN}$DESKTOP_APP LOC${NC} ($DESKTOP_APP_FILES files) — Compose Desktop"
echo "  webApp/                 → ${CYAN}$WEB_APP LOC${NC} ($WEB_APP_FILES files) — Kotlin/JS"
echo "  iosApp/                 → ${CYAN}$IOS_SWIFT LOC${NC} ($IOS_SWIFT_FILES files) — SwiftUI (native)"
echo ""

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}      Summary${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

# Calculate totals
SHARED_TOTAL=$((COMMON_MAIN + COMMON_TEST))
PLATFORM_IMPL_TOTAL=$((ANDROID_COMMON + IOS_COMMON + DESKTOP_COMMON + JS_COMMON))
KOTLIN_UI_TOTAL=$((ANDROID_APP + DESKTOP_APP + WEB_APP))
TOTAL_KOTLIN=$((SHARED_TOTAL + PLATFORM_IMPL_TOTAL + KOTLIN_UI_TOTAL))
TOTAL_ALL=$((TOTAL_KOTLIN + IOS_SWIFT))

# Business logic is shared
BUSINESS_LOGIC=$COMMON_MAIN
SHARED_PERCENTAGE=$((BUSINESS_LOGIC * 100 / (BUSINESS_LOGIC + PLATFORM_IMPL_TOTAL)))

echo "  ┌─────────────────────────────┬──────────────┐"
echo "  │ Category                    │ Lines        │"
echo "  ├─────────────────────────────┼──────────────┤"
printf "  │ %-27s │ %12s │\n" "Shared Business Logic" "${COMMON_MAIN}"
printf "  │ %-27s │ %12s │\n" "Shared Tests" "${COMMON_TEST}"
printf "  │ %-27s │ %12s │\n" "Platform Implementations" "${PLATFORM_IMPL_TOTAL}"
printf "  │ %-27s │ %12s │\n" "Platform UI (Kotlin)" "${KOTLIN_UI_TOTAL}"
printf "  │ %-27s │ %12s │\n" "Platform UI (Swift)" "${IOS_SWIFT}"
echo "  ├─────────────────────────────┼──────────────┤"
printf "  │ %-27s │ %12s │\n" "TOTAL" "${TOTAL_ALL}"
echo "  └─────────────────────────────┴──────────────┘"
echo ""

echo -e "${GREEN}🎯 Shared Code Percentage: ${SHARED_PERCENTAGE}%${NC}"
echo "   (Business logic shared across all 5 platforms)"
echo ""

echo -e "${YELLOW}📊 Code Distribution:${NC}"
echo ""
echo "  Shared (commonMain):    ████████████████░░░░ ~$SHARED_PERCENTAGE%"
echo "  Platform-specific:      ░░░░░░░░░░░░░░░░████ ~$((100 - SHARED_PERCENTAGE))%"
echo ""

echo -e "${GREEN}✓ Report complete${NC}"
echo ""

# Save to file
REPORT_FILE="loc_report_$(date +%Y%m%d).txt"
echo "Saved to: $REPORT_FILE"

cat > "$REPORT_FILE" << EOF
QR-SHIELD Lines of Code Report
Generated: $(date)

SHARED CODE (common module):
  commonMain/kotlin:  $COMMON_MAIN LOC ($COMMON_MAIN_FILES files)
  commonTest/kotlin:  $COMMON_TEST LOC ($COMMON_TEST_FILES files)

PLATFORM IMPLEMENTATIONS (expect/actual):
  androidMain/:       $ANDROID_COMMON LOC
  iosMain/:           $IOS_COMMON LOC
  desktopMain/:       $DESKTOP_COMMON LOC
  jsMain/:            $JS_COMMON LOC

PLATFORM APPS (UI Layer):
  androidApp/:        $ANDROID_APP LOC (Compose)
  desktopApp/:        $DESKTOP_APP LOC (Compose Desktop)
  webApp/:            $WEB_APP LOC (Kotlin/JS)
  iosApp/:            $IOS_SWIFT LOC (SwiftUI)

SUMMARY:
  Shared Business Logic:     $COMMON_MAIN LOC
  Shared Tests:              $COMMON_TEST LOC
  Platform Implementations:  $PLATFORM_IMPL_TOTAL LOC
  Platform UI (Kotlin):      $KOTLIN_UI_TOTAL LOC
  Platform UI (Swift):       $IOS_SWIFT LOC
  ─────────────────────────────────────
  TOTAL:                     $TOTAL_ALL LOC

SHARED CODE PERCENTAGE: ${SHARED_PERCENTAGE}%
EOF
