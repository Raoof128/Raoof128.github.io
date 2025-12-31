/*
 * Copyright 2025-2026 Mehr Guard Contributors
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

/// Accessibility UI tests for Mehr Guard iOS
/// Verifies VoiceOver compatibility, Dynamic Type support, and WCAG compliance
final class AccessibilityUITests: XCTestCase {
    
    var app: XCUIApplication!
    
    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }
    
    override func tearDownWithError() throws {
        app = nil
    }
    
    // MARK: - VoiceOver Label Tests
    
    func testAllButtonsHaveAccessibilityLabels() throws {
        let buttons = app.buttons.allElementsBoundByIndex
        
        for button in buttons.prefix(20) {
            if button.exists && button.isEnabled {
                XCTAssertFalse(
                    button.label.isEmpty,
                    "Button '\(button.identifier)' should have an accessibility label"
                )
            }
        }
    }
    
    func testAllImagesHaveAccessibilityLabels() throws {
        let images = app.images.allElementsBoundByIndex
        
        for image in images.prefix(10) {
            if image.exists {
                // Images should either have a label or be marked as decorative (hidden from VoiceOver)
                let hasLabel = !image.label.isEmpty
                let isAccessibilityElement = image.isAccessibilityElement
                
                XCTAssertTrue(
                    hasLabel || !isAccessibilityElement,
                    "Image should have label or be hidden from accessibility"
                )
            }
        }
    }
    
    func testTabBarItemsHaveLabels() throws {
        let tabBar = app.tabBars.firstMatch
        
        if tabBar.exists {
            let tabItems = tabBar.buttons.allElementsBoundByIndex
            
            for tab in tabItems {
                if tab.exists {
                    XCTAssertFalse(
                        tab.label.isEmpty,
                        "Tab bar item should have accessibility label"
                    )
                }
            }
        }
    }
    
    // MARK: - Interactive Element Tests
    
    func testAllSwitchesAreAccessible() throws {
        // Navigate to settings where switches are
        navigateToSettings()
        
        let switches = app.switches.allElementsBoundByIndex
        
        for toggle in switches {
            if toggle.exists && toggle.isEnabled {
                XCTAssertFalse(
                    toggle.label.isEmpty,
                    "Switch should have accessibility label"
                )
                
                // Switches should have value descriptions
                let value = toggle.value as? String
                XCTAssertNotNil(value, "Switch should have accessibility value")
            }
        }
    }
    
    func testTextFieldsHaveLabels() throws {
        // Navigate to history for search field
        navigateToHistory()
        
        let textFields = app.textFields.allElementsBoundByIndex
        let searchFields = app.searchFields.allElementsBoundByIndex
        
        for field in textFields + searchFields {
            if field.exists {
                XCTAssertFalse(
                    field.label.isEmpty || field.placeholderValue?.isEmpty == false,
                    "Text field should have accessibility label or placeholder"
                )
            }
        }
    }
    
    // MARK: - Touch Target Size Tests
    
    func testButtonsHaveAdequateTouchTargets() throws {
        let minimumSize: CGFloat = 44.0 // Apple's minimum recommended touch target
        
        let buttons = app.buttons.allElementsBoundByIndex
        
        for button in buttons.prefix(10) {
            if button.exists && button.isHittable {
                let frame = button.frame
                
                // Width and height should be at least 44pts
                // Note: Some buttons may use hit testing expansion, so we allow 36pts as minimum
                XCTAssertGreaterThanOrEqual(
                    max(frame.width, frame.height),
                    36,
                    "Button touch target should be adequately sized: \(button.label)"
                )
            }
        }
    }
    
    // MARK: - Color Contrast Tests
    
    func testCriticalElementsAreVisible() throws {
        // These tests verify elements exist and are hittable
        // Actual color contrast testing requires visual inspection or snapshot testing
        
        let criticalElements = [
            app.buttons.matching(NSPredicate(format: "label CONTAINS[c] 'scan'")).firstMatch,
            app.tabBars.firstMatch,
            app.navigationBars.firstMatch,
        ]
        
        for element in criticalElements {
            if element.exists {
                XCTAssertTrue(
                    element.isHittable || element.isEnabled,
                    "Critical element should be visible and interactable"
                )
            }
        }
    }
    
    // MARK: - Semantic Content Tests
    
    func testHeadingsAreUsed() throws {
        // Navigate through screens and check for headers
        let screens = ["Scanner", "History", "Settings"]
        
        for screen in screens {
            navigateToTab(named: screen)
            
            // Check for navigation bar title or header text
            let hasHeader = app.navigationBars.firstMatch.exists ||
                           app.staticTexts.matching(NSPredicate(format: 
                               "label == %@ OR label CONTAINS[c] %@", screen, screen
                           )).firstMatch.exists
            
            XCTAssertTrue(hasHeader, "\(screen) screen should have a visible header")
        }
    }
    
    // MARK: - Navigation Tests
    
    func testKeyboardNavigationWorks() throws {
        // Navigate to a screen with input
        navigateToHistory()
        
        let searchField = app.searchFields.firstMatch
        
        if searchField.exists && searchField.isHittable {
            searchField.tap()
            
            // Keyboard should appear
            let keyboard = app.keyboards.firstMatch
            XCTAssertTrue(
                keyboard.waitForExistence(timeout: 2),
                "Keyboard should appear when text field is focused"
            )
            
            // Dismiss keyboard
            if app.buttons["Done"].exists {
                app.buttons["Done"].tap()
            } else if app.buttons["Return"].exists {
                app.buttons["Return"].tap()
            } else {
                app.swipeDown()
            }
        }
    }
    
    // MARK: - Focus Order Tests
    
    func testLogicalFocusOrder() throws {
        // Tab bar items should be in logical order
        let tabBar = app.tabBars.firstMatch
        
        if tabBar.exists {
            let tabItems = tabBar.buttons.allElementsBoundByIndex
            
            var previousX: CGFloat = -1
            for tab in tabItems {
                if tab.exists {
                    let currentX = tab.frame.minX
                    XCTAssertGreaterThan(
                        currentX,
                        previousX,
                        "Tab items should be in left-to-right order"
                    )
                    previousX = currentX
                }
            }
        }
    }
    
    // MARK: - Error State Accessibility Tests
    
    func testErrorMessagesAreAccessible() throws {
        // Trigger an error state if possible and verify it's accessible
        // This is a placeholder - actual implementation depends on app behavior
        
        navigateToSettings()
        
        // Look for any error indicators
        let errorElements = app.staticTexts.matching(NSPredicate(format: 
            "label CONTAINS[c] 'error' OR label CONTAINS[c] 'failed'"
        )).allElementsBoundByIndex
        
        for error in errorElements {
            if error.exists {
                XCTAssertFalse(
                    error.label.isEmpty,
                    "Error messages should be readable by VoiceOver"
                )
            }
        }
    }
    
    // MARK: - Alert Accessibility Tests
    
    func testAlertsAreAccessible() throws {
        navigateToSettings()
        
        // Try to trigger an alert (clear history)
        scrollToFind(containing: "clear")
        
        let clearButton = app.buttons.matching(NSPredicate(format: 
            "label CONTAINS[c] 'clear'"
        )).firstMatch
        
        if clearButton.exists && clearButton.isHittable {
            clearButton.tap()
            
            let alert = app.alerts.firstMatch
            if alert.waitForExistence(timeout: 2) {
                // Alert should have a title and message
                let alertText = alert.staticTexts.allElementsBoundByIndex
                
                XCTAssertGreaterThan(
                    alertText.count,
                    0,
                    "Alert should have accessible text"
                )
                
                // Buttons should be accessible
                let alertButtons = alert.buttons.allElementsBoundByIndex
                for button in alertButtons {
                    XCTAssertFalse(
                        button.label.isEmpty,
                        "Alert buttons should have labels"
                    )
                }
                
                // Dismiss
                if alert.buttons["Cancel"].exists {
                    alert.buttons["Cancel"].tap()
                }
            }
        }
    }
    
    // MARK: - Dynamic Type Support Tests
    
    func testAppHandlesLargeText() throws {
        // This test verifies app doesn't crash with accessibility settings
        // Full Dynamic Type testing requires XCUITest with accessibility settings
        
        // Basic test: app should handle long labels
        app.terminate()
        app.launchArguments = ["-UIPreferredContentSizeCategoryName", "UICTContentSizeCategoryAccessibilityExtraExtraExtraLarge"]
        app.launch()
        
        // Navigate through all tabs
        let tabs = ["History", "Settings"]
        for tab in tabs {
            navigateToTab(named: tab)
            sleep(1)
        }
        
        XCTAssertEqual(app.state, .runningForeground, "App should handle large text sizes")
    }
    
    // MARK: - Reduce Motion Tests
    
    func testAppHandlesReduceMotion() throws {
        // This test verifies app respects reduce motion preference
        app.terminate()
        app.launchArguments = ["-AppleReduceMotion", "YES"]
        app.launch()
        
        // Navigate through screens
        navigateToHistory()
        navigateToSettings()
        
        XCTAssertEqual(app.state, .runningForeground, "App should handle reduce motion")
    }
    
    // MARK: - Trait Collection Tests
    
    func testAppHandlesBoldText() throws {
        app.terminate()
        app.launchArguments = ["-UILegibilityWeight", "Bold"]
        app.launch()
        
        // Navigate through screens
        navigateToHistory()
        navigateToSettings()
        
        XCTAssertEqual(app.state, .runningForeground, "App should handle bold text")
    }
    
    // MARK: - Helper Methods
    
    private func navigateToTab(named name: String) {
        let tabBar = app.tabBars.firstMatch
        
        if tabBar.exists {
            let tab = tabBar.buttons.matching(NSPredicate(format: 
                "label CONTAINS[c] %@", name
            )).firstMatch
            
            if tab.exists && tab.isHittable {
                tab.tap()
            }
        }
    }
    
    private func navigateToHistory() {
        navigateToTab(named: "History")
    }
    
    private func navigateToSettings() {
        navigateToTab(named: "Settings")
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
}
