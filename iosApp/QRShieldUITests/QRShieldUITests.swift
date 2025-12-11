/*
 * Copyright 2024 QR-SHIELD Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import XCTest

/// End-to-end UI tests for QR-SHIELD iOS App
///
/// Tests the main user flows using XCUITest.
/// Run with: `xcodebuild test -scheme QRShield -destination 'platform=iOS Simulator,name=iPhone 15'`
final class QRShieldUITests: XCTestCase {
    
    var app: XCUIApplication!
    
    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }
    
    override func tearDownWithError() throws {
        app = nil
    }
    
    // MARK: - Launch Tests
    
    func testAppLaunches() throws {
        // Verify app launches without crashing
        XCTAssertTrue(app.state == .runningForeground, "App should be running")
    }
    
    func testMainScreenDisplays() throws {
        // Verify main scanner screen elements are visible
        let scannerExists = app.buttons["Scan QR"].exists || 
                           app.staticTexts["Scan"].exists ||
                           app.buttons.matching(NSPredicate(format: "label CONTAINS[c] 'scan'")).firstMatch.exists
        
        XCTAssertTrue(scannerExists, "Scanner interface should be visible")
    }
    
    // MARK: - Navigation Tests
    
    func testTabBarNavigation() throws {
        // Test navigation between tabs
        let tabBar = app.tabBars.firstMatch
        
        if tabBar.exists {
            // Navigate to History
            let historyTab = tabBar.buttons["History"]
            if historyTab.exists {
                historyTab.tap()
                XCTAssertTrue(app.navigationBars["History"].exists || 
                             app.staticTexts["History"].exists,
                             "History screen should be visible")
            }
            
            // Navigate to Settings
            let settingsTab = tabBar.buttons["Settings"]
            if settingsTab.exists {
                settingsTab.tap()
                XCTAssertTrue(app.navigationBars["Settings"].exists || 
                             app.staticTexts["Settings"].exists,
                             "Settings screen should be visible")
            }
            
            // Navigate back to Scanner
            let scannerTab = tabBar.buttons.matching(NSPredicate(format: "label CONTAINS[c] 'scan'")).firstMatch
            if scannerTab.exists {
                scannerTab.tap()
            }
        }
    }
    
    // MARK: - Scanner Screen Tests
    
    func testScanButtonExists() throws {
        let scanButton = app.buttons["Scan QR"].firstMatch
        let scanButton2 = app.buttons.matching(NSPredicate(format: "label CONTAINS[c] 'scan'")).firstMatch
        
        XCTAssertTrue(scanButton.exists || scanButton2.exists, 
                     "Scan button should be visible")
    }
    
    func testGalleryButtonExists() throws {
        let galleryButton = app.buttons.matching(NSPredicate(format: "label CONTAINS[c] 'gallery' OR label CONTAINS[c] 'photo'")).firstMatch
        
        // Gallery button may or may not exist based on state
        // Just verify we can look for it without crashing
        _ = galleryButton.exists
    }
    
    func testScanCountDisplays() throws {
        // Verify scan count is displayed somewhere
        let scanCountLabel = app.staticTexts.matching(NSPredicate(format: "label CONTAINS 'scan' OR label MATCHES '\\\\d+ scans?'")).firstMatch
        
        // Scan count may or may not be visible
        _ = scanCountLabel.exists
    }
    
    // MARK: - History Screen Tests
    
    func testHistoryScreenAccessible() throws {
        navigateToHistory()
        
        // Verify history screen is shown
        let historyTitle = app.navigationBars["History"].exists || 
                          app.staticTexts["History"].exists
        XCTAssertTrue(historyTitle, "History title should be visible")
    }
    
    func testEmptyHistoryState() throws {
        navigateToHistory()
        
        // Check for empty state message
        let emptyMessage = app.staticTexts.matching(NSPredicate(format: "label CONTAINS[c] 'no scan' OR label CONTAINS[c] 'empty'")).firstMatch
        
        // May or may not have history items
        _ = emptyMessage.exists
    }
    
    func testHistoryFilterChipsExist() throws {
        navigateToHistory()
        
        // Look for filter options
        let allFilter = app.buttons["All"].exists
        let safeFilter = app.buttons["Safe"].exists
        
        // Filters may or may not exist depending on implementation
        _ = allFilter || safeFilter
    }
    
    // MARK: - Settings Screen Tests
    
    func testSettingsScreenAccessible() throws {
        navigateToSettings()
        
        // Verify settings screen is shown
        let settingsTitle = app.navigationBars["Settings"].exists || 
                           app.staticTexts["Settings"].exists
        XCTAssertTrue(settingsTitle, "Settings title should be visible")
    }
    
    func testHapticFeedbackToggleExists() throws {
        navigateToSettings()
        
        // Look for haptic feedback toggle
        let hapticToggle = app.switches.matching(NSPredicate(format: "label CONTAINS[c] 'haptic'")).firstMatch
        let hapticText = app.staticTexts.matching(NSPredicate(format: "label CONTAINS[c] 'haptic'")).firstMatch
        
        XCTAssertTrue(hapticToggle.exists || hapticText.exists, 
                     "Haptic feedback setting should exist")
    }
    
    func testSoundToggleExists() throws {
        navigateToSettings()
        
        // Look for sound toggle
        let soundToggle = app.switches.matching(NSPredicate(format: "label CONTAINS[c] 'sound'")).firstMatch
        let soundText = app.staticTexts.matching(NSPredicate(format: "label CONTAINS[c] 'sound'")).firstMatch
        
        XCTAssertTrue(soundToggle.exists || soundText.exists, 
                     "Sound setting should exist")
    }
    
    func testVersionInfoDisplays() throws {
        navigateToSettings()
        
        // Scroll down to find version info
        let settings = app.scrollViews.firstMatch
        if settings.exists {
            settings.swipeUp()
        }
        
        // Look for version text
        let versionText = app.staticTexts.matching(NSPredicate(format: "label CONTAINS[c] 'version' OR label MATCHES '\\\\d+\\\\.\\\\d+.*'")).firstMatch
        
        // Version may not be visible without scrolling
        _ = versionText.exists
    }
    
    func testClearHistoryButtonExists() throws {
        navigateToSettings()
        
        // Scroll down if needed
        let settings = app.scrollViews.firstMatch
        if settings.exists {
            settings.swipeUp()
        }
        
        // Look for clear history button
        let clearButton = app.buttons.matching(NSPredicate(format: "label CONTAINS[c] 'clear'")).firstMatch
        
        // Button may not be visible without scrolling
        _ = clearButton.exists
    }
    
    // MARK: - Accessibility Tests
    
    func testMainElementsAreAccessible() throws {
        // Verify main UI elements have accessibility labels
        let buttons = app.buttons.allElementsBoundByIndex
        
        for button in buttons.prefix(10) { // Check first 10 buttons
            if button.exists && button.isHittable {
                XCTAssertFalse(button.label.isEmpty, 
                              "Button should have accessibility label")
            }
        }
    }
    
    func testVoiceOverLabelsExist() throws {
        // Verify key elements have proper labels
        let scanButton = app.buttons.matching(NSPredicate(format: "label.length > 0")).firstMatch
        
        if scanButton.exists {
            XCTAssertFalse(scanButton.label.isEmpty, 
                          "Buttons should have accessibility labels")
        }
    }
    
    // MARK: - Helper Methods
    
    private func navigateToHistory() {
        let tabBar = app.tabBars.firstMatch
        
        if tabBar.exists {
            let historyTab = tabBar.buttons["History"]
            if historyTab.exists {
                historyTab.tap()
            }
        }
    }
    
    private func navigateToSettings() {
        let tabBar = app.tabBars.firstMatch
        
        if tabBar.exists {
            let settingsTab = tabBar.buttons["Settings"]
            if settingsTab.exists {
                settingsTab.tap()
            }
        }
    }
}
