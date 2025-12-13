# ============================================================================
# 🛡️ QR-SHIELD Judge Build Helper (Windows PowerShell)
# ============================================================================
# Quick setup and run commands for competition judges
# Run: .\judge.ps1
# ============================================================================

Write-Host ""
Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Magenta
Write-Host "║              🛡️  QR-SHIELD Judge Build Helper              ║" -ForegroundColor Cyan
Write-Host "║                 Kotlin Multiplatform Demo                      ║" -ForegroundColor White
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Magenta
Write-Host ""

# ============================================================================
# Environment Checks
# ============================================================================

Write-Host "📋 Checking Environment..." -ForegroundColor Blue
Write-Host ""

# Check Java
try {
    $javaVersion = java -version 2>&1 | Select-String -Pattern 'version' | Select-Object -First 1
    Write-Host "  ✓ Java: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Java: NOT FOUND" -ForegroundColor Red
    Write-Host "    Install: https://adoptium.net/" -ForegroundColor Yellow
}

# Check Gradle Wrapper
if (Test-Path ".\gradlew.bat") {
    Write-Host "  ✓ Gradle Wrapper: Available" -ForegroundColor Green
} else {
    Write-Host "  ✗ Gradle Wrapper: NOT FOUND" -ForegroundColor Red
}

# Check Node
try {
    $nodeVersion = node --version 2>$null
    Write-Host "  ✓ Node.js: $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "  ○ Node.js: Not installed (optional, for E2E tests)" -ForegroundColor Yellow
}

Write-Host ""

# ============================================================================
# Quick Commands
# ============================================================================

Write-Host "🚀 Quick Run Commands" -ForegroundColor Blue
Write-Host ""
Write-Host "  1. Web Demo (Fastest - No build needed!)" -ForegroundColor Cyan
Write-Host "     → https://raoof128.github.io/?demo=true" -ForegroundColor Green
Write-Host ""
Write-Host "  2. Run All Tests" -ForegroundColor Cyan
Write-Host "     .\gradlew.bat :common:allTests" -ForegroundColor Yellow
Write-Host ""
Write-Host "  3. Run Desktop App" -ForegroundColor Cyan
Write-Host "     .\gradlew.bat :desktopApp:run" -ForegroundColor Yellow
Write-Host ""
Write-Host "  4. Build Android APK" -ForegroundColor Cyan
Write-Host "     .\gradlew.bat :androidApp:assembleDebug" -ForegroundColor Yellow
Write-Host "     → APK: androidApp\build\outputs\apk\debug\androidApp-debug.apk" -ForegroundColor Green
Write-Host ""
Write-Host "  5. Run Web Locally" -ForegroundColor Cyan
Write-Host "     .\gradlew.bat :webApp:jsBrowserRun" -ForegroundColor Yellow
Write-Host "     → Opens at http://localhost:8080" -ForegroundColor Green
Write-Host ""

# ============================================================================
# What to Test
# ============================================================================

Write-Host "🧪 What to Test" -ForegroundColor Blue
Write-Host ""
Write-Host "  Sample Malicious URL: https://paypa1-secure.tk/login" -ForegroundColor Magenta
Write-Host "  Expected Result: Score 85+, MALICIOUS verdict" -ForegroundColor Magenta
Write-Host "  Triggered Signals: Brand Impersonation, Suspicious TLD, Typosquatting" -ForegroundColor Magenta
Write-Host ""
Write-Host "  Sample Safe URL: https://google.com" -ForegroundColor Green
Write-Host "  Expected Result: Score <20, SAFE verdict" -ForegroundColor Green
Write-Host ""

# ============================================================================
# KMP Proof Points
# ============================================================================

Write-Host "📐 KMP Architecture Proof" -ForegroundColor Blue
Write-Host ""
Write-Host "  • Shared code: common\src\commonMain\kotlin\" -ForegroundColor Cyan
Write-Host "  • Platform code: androidApp\, iosApp\, desktopApp\, webApp\" -ForegroundColor Cyan
Write-Host "  • Run same test on all platforms: .\gradlew.bat allTests" -ForegroundColor Yellow
Write-Host ""

# ============================================================================
# Interactive Menu
# ============================================================================

Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Magenta
Write-Host ""
Write-Host "What would you like to do?" -ForegroundColor Cyan
Write-Host ""
Write-Host "  1) Open Web Demo (browser)"
Write-Host "  2) Run unit tests"
Write-Host "  3) Run desktop app"
Write-Host "  4) Build Android APK"
Write-Host "  5) Run web locally"
Write-Host "  6) Exit"
Write-Host ""
$choice = Read-Host "Enter choice [1-6]"

switch ($choice) {
    "1" {
        Write-Host "Opening Web Demo..." -ForegroundColor Green
        Start-Process "https://raoof128.github.io/?demo=true"
    }
    "2" {
        Write-Host "Running unit tests..." -ForegroundColor Green
        .\gradlew.bat :common:allTests
    }
    "3" {
        Write-Host "Running desktop app..." -ForegroundColor Green
        .\gradlew.bat :desktopApp:run
    }
    "4" {
        Write-Host "Building Android APK..." -ForegroundColor Green
        .\gradlew.bat :androidApp:assembleDebug
        Write-Host "APK: androidApp\build\outputs\apk\debug\androidApp-debug.apk" -ForegroundColor Green
    }
    "5" {
        Write-Host "Starting web server..." -ForegroundColor Green
        .\gradlew.bat :webApp:jsBrowserRun
    }
    "6" {
        Write-Host "Goodbye! 👋" -ForegroundColor Cyan
        exit
    }
    default {
        Write-Host "Invalid choice. Run .\judge.ps1 again." -ForegroundColor Yellow
    }
}
