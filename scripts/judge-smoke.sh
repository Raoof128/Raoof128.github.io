#!/bin/bash
#
# QR-SHIELD Judge Smoke Test
# ==========================
# Quick verification script for contest judges.
# Run this on a fresh clone to verify the project builds on all platforms.
#
# Usage: ./scripts/judge-smoke.sh
#
# Copyright 2025-2026 QR-SHIELD Contributors
# Licensed under Apache 2.0
#

set -euo pipefail

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}     🛡️ QR-SHIELD Judge Smoke Test${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "Time: $(date)"
echo -e "Platform: $(uname -s) $(uname -m)"
echo ""

PASSED=0
FAILED=0

# Test function
run_test() {
    local name=$1
    local command=$2
    
    echo -e "${YELLOW}⏳ $name...${NC}"
    
    if eval "$command" > /dev/null 2>&1; then
        echo -e "${GREEN}  ✅ PASSED${NC}"
        ((PASSED++))
    else
        echo -e "${RED}  ❌ FAILED${NC}"
        ((FAILED++))
    fi
}

echo -e "${BLUE}Step 1/6: Clean${NC}"
run_test "gradle clean" "./gradlew clean --quiet"

echo ""
echo -e "${BLUE}Step 2/6: Common Module (Shared Detection Engine)${NC}"
run_test "shared tests" "./gradlew :common:desktopTest --quiet"

echo ""
echo -e "${BLUE}Step 3/6: Android App${NC}"
run_test "android build" "./gradlew :androidApp:assembleDebug --quiet"

echo ""
echo -e "${BLUE}Step 4/6: Desktop App${NC}"
run_test "desktop package" "./gradlew :desktopApp:packageDistributionForCurrentOS --quiet"

echo ""
echo -e "${BLUE}Step 5/6: Web App (JavaScript)${NC}"
run_test "web JS build" "./gradlew :webApp:jsBrowserProductionWebpack --quiet"

echo ""
echo -e "${BLUE}Step 6/6: Web App (WebAssembly)${NC}"
run_test "web Wasm build" "./gradlew :webApp:wasmJsBrowserProductionWebpack --quiet"

# Summary
echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}     Summary${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "  Passed: ${GREEN}$PASSED${NC}"
echo -e "  Failed: ${RED}$FAILED${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${GREEN}  ✅ OK: smoke test passed${NC}"
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
    echo "Next steps for judges:"
    echo "  • Run desktop app: ./gradlew :desktopApp:run"
    echo "  • Try web demo: https://raoof128.github.io"
    echo "  • Verify claims: ./judge/verify_all.sh"
    echo ""
    exit 0
else
    echo -e "${RED}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${RED}  ❌ FAILED: See errors above${NC}"
    echo -e "${RED}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    exit 1
fi
