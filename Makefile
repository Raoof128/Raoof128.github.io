# Makefile for QR-SHIELD
# Kotlin Multiplatform QRishing Detector

.PHONY: all build clean test lint format check android desktop docs help

# Default target
all: check build

# ============================================
# Build Targets
# ============================================

## Build all modules
build:
	@echo "🔨 Building all modules..."
	./gradlew build

## Build common module only
build-common:
	@echo "🔨 Building common module..."
	./gradlew :common:build

## Build Android app
android:
	@echo "🤖 Building Android app..."
	./gradlew :androidApp:assembleDebug

## Build Android release APK
android-release:
	@echo "🤖 Building Android release..."
	./gradlew :androidApp:assembleRelease

## Build Desktop app
desktop:
	@echo "🖥️ Building Desktop app..."
	./gradlew :desktopApp:build

## Run Desktop app
run-desktop:
	@echo "🖥️ Running Desktop app..."
	./gradlew :desktopApp:run

## Create Desktop distribution packages
package-desktop:
	@echo "📦 Creating Desktop packages..."
	./gradlew :desktopApp:packageDistributionForCurrentOS

# ============================================
# Test Targets
# ============================================

## Run all tests
test:
	@echo "🧪 Running all tests..."
	./gradlew allTests

## Run common module tests
test-common:
	@echo "🧪 Running common module tests..."
	./gradlew :common:allTests

## Run Android tests
test-android:
	@echo "🧪 Running Android tests..."
	./gradlew :androidApp:testDebugUnitTest

## Run Desktop tests
test-desktop:
	@echo "🧪 Running Desktop tests..."
	./gradlew :desktopApp:desktopTest

## Run tests with coverage
coverage:
	@echo "📊 Running tests with coverage..."
	./gradlew koverReport
	@echo "Coverage report: build/reports/kover/html/index.html"

## Run property-based/fuzz tests
test-fuzz:
	@echo "🎲 Running property-based/fuzz tests..."
	./gradlew :common:allTests --tests "*PropertyBasedTests*"
	@echo "✅ Fuzz tests completed!"

## Run performance regression tests (fail if latency > 50ms)
test-performance:
	@echo "⚡ Running performance regression tests..."
	./gradlew :common:allTests --tests "*PerformanceRegressionTest*"
	@echo "✅ Performance tests passed!"

## Run benchmark tests
test-benchmark:
	@echo "📈 Running benchmarks..."
	./gradlew :common:allTests --tests "*Benchmark*"
	@echo "Benchmark results printed above."

## Run iOS UI tests (requires macOS with Xcode)
test-ios-ui:
	@echo "📱 Running iOS UI tests..."
	cd iosApp && xcodebuild test \
		-scheme QRShield \
		-destination 'platform=iOS Simulator,name=iPhone 15' \
		-testPlan QRShieldUITests || true
	@echo "✅ iOS UI tests completed!"

## Run Web E2E tests with Playwright
test-web-e2e:
	@echo "🌐 Running Web E2E tests..."
	cd webApp/e2e && npm install && npx playwright install && npm test
	@echo "✅ Web E2E tests completed!"

## Run Web E2E tests in headed mode (visible browser)
test-web-e2e-headed:
	@echo "🌐 Running Web E2E tests (headed)..."
	cd webApp/e2e && npm install && npx playwright install && npm run test:headed

## Generate Web E2E test report
test-web-e2e-report:
	@echo "📊 Opening Playwright test report..."
	cd webApp/e2e && npm run test:report

## Run all quality tests (fuzz + performance + coverage)
test-quality:
	@echo "🔬 Running quality test suite..."
	$(MAKE) test-fuzz
	$(MAKE) test-performance
	$(MAKE) coverage
	@echo "✅ All quality tests passed!"

# ============================================
# Code Quality
# ============================================

## Run Detekt static analysis
lint:
	@echo "🔍 Running Detekt..."
	./gradlew detekt

## Format code with Ktlint
format:
	@echo "✨ Formatting code..."
	./gradlew ktlintFormat

## Check code formatting
check-format:
	@echo "🔍 Checking code format..."
	./gradlew ktlintCheck

## Run all checks (lint + format + test)
check: lint check-format test
	@echo "✅ All checks passed!"

# ============================================
# Dependencies
# ============================================

## Update dependencies
deps-update:
	@echo "📦 Checking for dependency updates..."
	./gradlew dependencyUpdates

## Refresh dependencies
deps-refresh:
	@echo "🔄 Refreshing dependencies..."
	./gradlew --refresh-dependencies

# ============================================
# Cleanup
# ============================================

## Clean build artifacts
clean:
	@echo "🧹 Cleaning build artifacts..."
	./gradlew clean

## Deep clean (includes Gradle cache)
clean-all: clean
	@echo "🧹 Deep cleaning..."
	rm -rf .gradle
	rm -rf **/build
	rm -rf ~/.gradle/caches/modules-2/files-2.1/com.qrshield*

# ============================================
# Documentation
# ============================================

## Generate KDoc documentation
docs:
	@echo "📚 Generating documentation..."
	./gradlew dokkaHtml
	@echo "Documentation: build/dokka/html/index.html"

# ============================================
# Development
# ============================================

## Setup development environment
setup:
	@echo "🔧 Setting up development environment..."
	chmod +x scripts/*.sh
	./scripts/setup.sh

## Sync Gradle project
sync:
	@echo "🔄 Syncing Gradle project..."
	./gradlew --refresh-dependencies

## Generate SQLDelight schema
generate-db:
	@echo "📊 Generating SQLDelight schema..."
	./gradlew generateCommonMainQRShieldDatabaseInterface

# ============================================
# CI/CD
# ============================================

## Run CI checks (same as GitHub Actions)
ci: clean check android desktop
	@echo "✅ CI checks completed!"

## Create release artifacts
release: clean
	@echo "📦 Creating release artifacts..."
	./gradlew :androidApp:assembleRelease
	./gradlew :desktopApp:packageDistributionForCurrentOS
	@echo "✅ Release artifacts created!"

# ============================================
# Help
# ============================================

## Show this help message
help:
	@echo ""
	@echo "🛡️ QR-SHIELD Makefile"
	@echo "====================="
	@echo ""
	@echo "Usage: make [target]"
	@echo ""
	@echo "Build:"
	@echo "  build          Build all modules"
	@echo "  build-common   Build common module only"
	@echo "  android        Build Android debug APK"
	@echo "  android-release Build Android release APK"
	@echo "  desktop        Build Desktop app"
	@echo "  run-desktop    Run Desktop app"
	@echo "  package-desktop Create Desktop installers"
	@echo ""
	@echo "Test:"
	@echo "  test           Run all tests"
	@echo "  test-common    Run common module tests"
	@echo "  test-android   Run Android unit tests"
	@echo "  test-desktop   Run Desktop tests"
	@echo "  coverage       Run tests with coverage report"
	@echo "  test-fuzz      Run property-based/fuzz tests"
	@echo "  test-performance Run performance regression tests"
	@echo "  test-benchmark Run benchmark tests"
	@echo "  test-ios-ui    Run iOS XCUITest suite"
	@echo "  test-web-e2e   Run Playwright web E2E tests"
	@echo "  test-quality   Run all quality tests (fuzz+perf+coverage)"
	@echo ""
	@echo "Quality:"
	@echo "  lint           Run Detekt static analysis"
	@echo "  format         Format code with Ktlint"
	@echo "  check-format   Check code formatting"
	@echo "  check          Run all quality checks"
	@echo ""
	@echo "Docs:"
	@echo "  docs           Generate KDoc documentation"
	@echo ""
	@echo "Other:"
	@echo "  clean          Clean build artifacts"
	@echo "  clean-all      Deep clean including caches"
	@echo "  setup          Setup development environment"
	@echo "  ci             Run CI checks locally"
	@echo "  release        Create release artifacts"
	@echo "  help           Show this help message"
	@echo ""
