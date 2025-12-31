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

/// Performance and launch tests for Mehr Guard iOS
final class PerformanceUITests: XCTestCase {
    
    var app: XCUIApplication!
    
    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
    }
    
    override func tearDownWithError() throws {
        app = nil
    }
    
    // MARK: - Launch Performance Tests
    
    func testLaunchPerformance() throws {
        if #available(iOS 13.0, *) {
            measure(metrics: [XCTApplicationLaunchMetric()]) {
                XCUIApplication().launch()
            }
        }
    }
    
    func testLaunchToReadyState() throws {
        app.launch()
        
        // Measure time to reach ready state (scan button visible)
        let scanButton = app.buttons.matching(NSPredicate(format: 
            "label CONTAINS[c] 'scan'"
        )).firstMatch
        
        let exists = scanButton.waitForExistence(timeout: 5)
        XCTAssertTrue(exists, "App should reach ready state within 5 seconds")
    }
    
    // MARK: - Navigation Performance Tests
    
    func testTabSwitchingPerformance() throws {
        app.launch()
        
        measure {
            let tabBar = app.tabBars.firstMatch
            
            if tabBar.exists {
                // Switch to History
                let historyTab = tabBar.buttons["History"]
                if historyTab.exists { historyTab.tap() }
                
                // Switch to Settings
                let settingsTab = tabBar.buttons["Settings"]
                if settingsTab.exists { settingsTab.tap() }
                
                // Switch back to Scanner
                let scannerTab = tabBar.buttons.matching(NSPredicate(format: "label CONTAINS[c] 'scan'")).firstMatch
                if scannerTab.exists { scannerTab.tap() }
            }
        }
    }
    
    // MARK: - Memory Tests
    
    func testScrollingHistoryDoesNotCrash() throws {
        app.launch()
        
        // Navigate to History
        let tabBar = app.tabBars.firstMatch
        if tabBar.exists {
            let historyTab = tabBar.buttons["History"]
            if historyTab.exists { historyTab.tap() }
        }
        
        // Scroll if there's content
        let scrollView = app.scrollViews.firstMatch
        if scrollView.exists {
            for _ in 0..<5 {
                scrollView.swipeUp()
                scrollView.swipeDown()
            }
        }
        
        // App should not crash
        XCTAssertEqual(app.state, .runningForeground, "App should still be running after scrolling")
    }
    
    func testScrollingSettingsDoesNotCrash() throws {
        app.launch()
        
        // Navigate to Settings
        let tabBar = app.tabBars.firstMatch
        if tabBar.exists {
            let settingsTab = tabBar.buttons["Settings"]
            if settingsTab.exists { settingsTab.tap() }
        }
        
        // Scroll
        let scrollView = app.scrollViews.firstMatch
        if scrollView.exists {
            for _ in 0..<5 {
                scrollView.swipeUp()
                scrollView.swipeDown()
            }
        }
        
        // App should not crash
        XCTAssertEqual(app.state, .runningForeground, "App should still be running after scrolling settings")
    }
    
    // MARK: - Stress Tests
    
    func testRapidTabSwitching() throws {
        app.launch()
        
        let tabBar = app.tabBars.firstMatch
        guard tabBar.exists else { return }
        
        // Rapidly switch tabs
        for _ in 0..<20 {
            let historyTab = tabBar.buttons["History"]
            if historyTab.exists { historyTab.tap() }
            
            let settingsTab = tabBar.buttons["Settings"]
            if settingsTab.exists { settingsTab.tap() }
            
            let scannerTab = tabBar.buttons.matching(NSPredicate(format: "label CONTAINS[c] 'scan'")).firstMatch
            if scannerTab.exists { scannerTab.tap() }
        }
        
        // App should still be responsive
        XCTAssertEqual(app.state, .runningForeground, "App should handle rapid tab switching")
    }
    
    func testMultipleScanAttempts() throws {
        app.launch()
        
        // Find scan button
        let scanButton = app.buttons.matching(NSPredicate(format: "label CONTAINS[c] 'scan'")).firstMatch
        guard scanButton.exists else { return }
        
        // Repeatedly tap scan and close
        for _ in 0..<5 {
            if scanButton.exists && scanButton.isHittable {
                scanButton.tap()
            }
            
            sleep(1)
            
            // Try to close
            let closeButton = app.buttons.matching(NSPredicate(format: 
                "label CONTAINS[c] 'close' OR label CONTAINS[c] 'cancel'"
            )).firstMatch
            
            if closeButton.exists && closeButton.isHittable {
                closeButton.tap()
            }
            
            sleep(1)
        }
        
        // App should not crash
        XCTAssertEqual(app.state, .runningForeground, "App should handle multiple scan attempts")
    }
}
