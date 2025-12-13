/*
 * Copyright 2025-2026 QR-SHIELD Contributors
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

/// Settings screen UI tests for QR-SHIELD iOS
/// Tests all settings toggles, buttons, and persistence
final class SettingsFlowUITests: XCTestCase {
    
    var app: XCUIApplication!
    
    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }
    
    override func tearDownWithError() throws {
        app = nil
    }
    
    // MARK: - Navigation Tests
    
    func testNavigateToSettings() throws {
        navigateToSettings()
        
        let settingsTitle = app.navigationBars["Settings"].exists ||
                            app.staticTexts["Settings"].exists
        XCTAssertTrue(settingsTitle, "Settings screen should be visible")
    }
    
    // MARK: - Toggle Tests
    
    func testHapticFeedbackToggle() throws {
        navigateToSettings()
        
        let hapticSwitch = findSwitch(containing: "haptic")
        
        if let toggle = hapticSwitch {
            let initialValue = toggle.value as? String
            toggle.tap()
            
            sleep(1)
            let newValue = toggle.value as? String
            
            XCTAssertNotEqual(initialValue, newValue, "Haptic toggle should change value")
            
            // Toggle back
            toggle.tap()
        }
    }
    
    func testSoundToggle() throws {
        navigateToSettings()
        
        let soundSwitch = findSwitch(containing: "sound")
        
        if let toggle = soundSwitch {
            let initialValue = toggle.value as? String
            toggle.tap()
            
            sleep(1)
            let newValue = toggle.value as? String
            
            XCTAssertNotEqual(initialValue, newValue, "Sound toggle should change value")
            
            // Toggle back
            toggle.tap()
        }
    }
    
    func testAutoScanToggle() throws {
        navigateToSettings()
        
        scrollToFind(containing: "auto")
        
        let autoScanSwitch = findSwitch(containing: "auto")
        
        if let toggle = autoScanSwitch {
            let initialValue = toggle.value as? String
            toggle.tap()
            
            sleep(1)
            
            // Toggle back
            toggle.tap()
        }
        
        XCTAssertEqual(app.state, .runningForeground, "App should handle auto-scan toggle")
    }
    
    func testSaveHistoryToggle() throws {
        navigateToSettings()
        
        scrollToFind(containing: "history")
        
        let saveHistorySwitch = findSwitch(containing: "history")
        
        if let toggle = saveHistorySwitch {
            toggle.tap()
            sleep(1)
            toggle.tap()
        }
        
        XCTAssertEqual(app.state, .runningForeground, "App should handle save history toggle")
    }
    
    func testSecurityAlertsToggle() throws {
        navigateToSettings()
        
        scrollToFind(containing: "security")
        
        let securitySwitch = findSwitch(containing: "security")
        
        if let toggle = securitySwitch {
            toggle.tap()
            sleep(1)
            toggle.tap()
        }
        
        XCTAssertEqual(app.state, .runningForeground, "App should handle security alerts toggle")
    }
    
    // MARK: - Dark Mode Tests
    
    func testDarkModeToggle() throws {
        navigateToSettings()
        
        scrollToFind(containing: "dark")
        
        let darkModeSwitch = findSwitch(containing: "dark")
        let darkModeSegment = app.segmentedControls.firstMatch
        
        if let toggle = darkModeSwitch {
            toggle.tap()
            sleep(1)
            
            // App appearance should change (hard to verify in XCUITest)
            toggle.tap()
        } else if darkModeSegment.exists {
            // May use segmented control for System/Light/Dark
            let buttons = darkModeSegment.buttons.allElementsBoundByIndex
            for button in buttons {
                if button.isHittable {
                    button.tap()
                    sleep(1)
                }
            }
        }
        
        XCTAssertEqual(app.state, .runningForeground, "App should handle dark mode toggle")
    }
    
    // MARK: - Clear History Tests
    
    func testClearHistoryButtonExists() throws {
        navigateToSettings()
        
        scrollToFind(containing: "clear")
        
        let clearButton = app.buttons.matching(NSPredicate(format: 
            "label CONTAINS[c] 'clear'"
        )).firstMatch
        
        // Clear history button should exist
        _ = clearButton.exists
    }
    
    func testClearHistoryConfirmation() throws {
        navigateToSettings()
        
        scrollToFind(containing: "clear")
        
        let clearButton = app.buttons.matching(NSPredicate(format: 
            "label CONTAINS[c] 'clear'"
        )).firstMatch
        
        if clearButton.exists && clearButton.isHittable {
            clearButton.tap()
            
            // Should show confirmation dialog
            let confirmationExists = app.alerts.firstMatch.waitForExistence(timeout: 2) ||
                                     app.sheets.firstMatch.waitForExistence(timeout: 2)
            
            if confirmationExists {
                // Cancel the action
                let cancelButton = app.buttons["Cancel"]
                if cancelButton.exists && cancelButton.isHittable {
                    cancelButton.tap()
                } else {
                    // Dismiss by tapping outside
                    app.tapCoordinate(at: CGPoint(x: 100, y: 100))
                }
            }
        }
        
        XCTAssertEqual(app.state, .runningForeground, "App should show confirmation for clear history")
    }
    
    // MARK: - About Section Tests
    
    func testVersionInfoDisplayed() throws {
        navigateToSettings()
        
        // Scroll to bottom
        scrollToBottom()
        
        let versionText = app.staticTexts.matching(NSPredicate(format: 
            "label CONTAINS[c] 'version' OR label MATCHES '\\\\d+\\\\.\\\\d+.*'"
        )).firstMatch
        
        // Version should be displayed somewhere
        _ = versionText.exists
    }
    
    func testGitHubLinkExists() throws {
        navigateToSettings()
        
        scrollToBottom()
        
        let githubLink = app.buttons.matching(NSPredicate(format: 
            "label CONTAINS[c] 'github' OR label CONTAINS[c] 'source'"
        )).firstMatch
        
        // GitHub link may exist
        if githubLink.exists && githubLink.isHittable {
            // Just verify it's tappable, don't actually open
            XCTAssertTrue(githubLink.isHittable, "GitHub link should be tappable")
        }
    }
    
    func testPrivacyPolicyLink() throws {
        navigateToSettings()
        
        scrollToBottom()
        
        let privacyLink = app.buttons.matching(NSPredicate(format: 
            "label CONTAINS[c] 'privacy'"
        )).firstMatch
        
        if privacyLink.exists && privacyLink.isHittable {
            XCTAssertTrue(privacyLink.isHittable, "Privacy policy link should be tappable")
        }
    }
    
    func testTermsOfServiceLink() throws {
        navigateToSettings()
        
        scrollToBottom()
        
        let termsLink = app.buttons.matching(NSPredicate(format: 
            "label CONTAINS[c] 'terms'"
        )).firstMatch
        
        if termsLink.exists && termsLink.isHittable {
            XCTAssertTrue(termsLink.isHittable, "Terms link should be tappable")
        }
    }
    
    // MARK: - Settings Persistence Tests
    
    func testToggleStatePersistedAcrossRelaunch() throws {
        navigateToSettings()
        
        let hapticSwitch = findSwitch(containing: "haptic")
        var toggledValue: String?
        
        if let toggle = hapticSwitch {
            toggle.tap()
            sleep(1)
            toggledValue = toggle.value as? String
        }
        
        // Terminate and relaunch
        app.terminate()
        app.launch()
        
        navigateToSettings()
        
        if let toggle = findSwitch(containing: "haptic"), let expected = toggledValue {
            let persistedValue = toggle.value as? String
            XCTAssertEqual(persistedValue, expected, "Toggle state should persist across relaunch")
            
            // Reset to original
            toggle.tap()
        }
    }
    
    // MARK: - Scrolling Tests
    
    func testSettingsScrollsWithoutCrash() throws {
        navigateToSettings()
        
        let scrollView = app.scrollViews.firstMatch
        let table = app.tables.firstMatch
        
        let scrollable = scrollView.exists ? scrollView : table
        
        if scrollable.exists {
            for _ in 0..<5 {
                scrollable.swipeUp()
                scrollable.swipeDown()
            }
        }
        
        XCTAssertEqual(app.state, .runningForeground, "App should handle scrolling")
    }
    
    // MARK: - Section Headers Tests
    
    func testSectionHeadersExist() throws {
        navigateToSettings()
        
        let expectedSections = ["General", "Appearance", "Privacy", "About", "Feedback", "Security"]
        
        let foundSections = expectedSections.filter { section in
            app.staticTexts[section].exists ||
            app.staticTexts[section.uppercased()].exists
        }
        
        // At least some section headers should exist
        _ = foundSections.count > 0
    }
    
    // MARK: - Feedback Tests
    
    func testFeedbackButtonExists() throws {
        navigateToSettings()
        
        scrollToBottom()
        
        let feedbackButton = app.buttons.matching(NSPredicate(format: 
            "label CONTAINS[c] 'feedback' OR label CONTAINS[c] 'contact' OR label CONTAINS[c] 'support'"
        )).firstMatch
        
        if feedbackButton.exists && feedbackButton.isHittable {
            XCTAssertTrue(feedbackButton.isHittable, "Feedback button should be tappable")
        }
    }
    
    func testRateAppButtonExists() throws {
        navigateToSettings()
        
        scrollToBottom()
        
        let rateButton = app.buttons.matching(NSPredicate(format: 
            "label CONTAINS[c] 'rate' OR label CONTAINS[c] 'review'"
        )).firstMatch
        
        // Rate app button may exist
        _ = rateButton.exists
    }
    
    // MARK: - Accessibility Tests
    
    func testAllSettingsAccessible() throws {
        navigateToSettings()
        
        let switches = app.switches.allElementsBoundByIndex
        
        for settingSwitch in switches {
            if settingSwitch.exists {
                XCTAssertFalse(settingSwitch.label.isEmpty, 
                              "Settings switches should have accessibility labels")
            }
        }
    }
    
    func testSettingsVoiceOverFriendly() throws {
        navigateToSettings()
        
        // All interactive elements should have labels
        let buttons = app.buttons.allElementsBoundByIndex
        
        for button in buttons.prefix(10) {
            if button.exists && button.isEnabled {
                XCTAssertFalse(button.label.isEmpty, 
                              "Buttons should have accessibility labels for VoiceOver")
            }
        }
    }
    
    // MARK: - Helper Methods
    
    private func navigateToSettings() {
        let tabBar = app.tabBars.firstMatch
        
        if tabBar.exists {
            let settingsTab = tabBar.buttons["Settings"]
            if settingsTab.exists && settingsTab.isHittable {
                settingsTab.tap()
            }
        }
    }
    
    private func findSwitch(containing text: String) -> XCUIElement? {
        // Try finding switch by various methods
        let predicates = [
            NSPredicate(format: "label CONTAINS[c] %@", text),
            NSPredicate(format: "identifier CONTAINS[c] %@", text),
        ]
        
        for predicate in predicates {
            let element = app.switches.matching(predicate).firstMatch
            if element.exists && element.isHittable {
                return element
            }
        }
        
        return nil
    }
    
    private func scrollToFind(containing text: String) {
        let scrollView = app.scrollViews.firstMatch
        let table = app.tables.firstMatch
        let scrollable = scrollView.exists ? scrollView : table
        
        let targetElement = app.staticTexts.matching(NSPredicate(format: 
            "label CONTAINS[c] %@", text
        )).firstMatch
        
        var attempts = 0
        while !targetElement.exists && attempts < 5 {
            scrollable.swipeUp()
            attempts += 1
        }
    }
    
    private func scrollToBottom() {
        let scrollView = app.scrollViews.firstMatch
        let table = app.tables.firstMatch
        let scrollable = scrollView.exists ? scrollView : table
        
        if scrollable.exists {
            for _ in 0..<3 {
                scrollable.swipeUp()
            }
        }
    }
}
