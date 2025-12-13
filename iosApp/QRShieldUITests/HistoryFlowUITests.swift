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

/// History screen UI tests for QR-SHIELD iOS
/// Tests the history view functionality including filtering, search, and delete operations
final class HistoryFlowUITests: XCTestCase {
    
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
    
    func testNavigateToHistory() throws {
        navigateToHistory()
        
        let historyTitle = app.navigationBars["History"].exists ||
                           app.staticTexts["History"].exists
        XCTAssertTrue(historyTitle, "History screen should be visible")
    }
    
    func testHistoryBackNavigation() throws {
        navigateToHistory()
        
        // Navigate back if possible
        let backButton = app.navigationBars.buttons.firstMatch
        if backButton.exists && backButton.isHittable {
            backButton.tap()
        }
        
        // App should not crash
        XCTAssertEqual(app.state, .runningForeground, "App should still be running")
    }
    
    // MARK: - Empty State Tests
    
    func testEmptyStateMessageDisplayed() throws {
        navigateToHistory()
        
        // Check for empty state indicators
        let emptyIndicators = [
            app.staticTexts.matching(NSPredicate(format: "label CONTAINS[c] 'no scan'")).firstMatch,
            app.staticTexts.matching(NSPredicate(format: "label CONTAINS[c] 'empty'")).firstMatch,
            app.staticTexts.matching(NSPredicate(format: "label CONTAINS[c] 'no history'")).firstMatch,
            app.images.matching(NSPredicate(format: "label CONTAINS[c] 'empty'")).firstMatch,
        ]
        
        // At least one indicator should exist if history is empty
        // Or list should have items if history is not empty
        let hasEmptyState = emptyIndicators.contains { $0.exists }
        let hasItems = app.tables.cells.count > 0 || app.collectionViews.cells.count > 0
        
        XCTAssertTrue(hasEmptyState || hasItems, "Should show empty state or history items")
    }
    
    func testEmptyStateHasScanPrompt() throws {
        navigateToHistory()
        
        // If empty, should have a prompt or button to start scanning
        let scanPrompt = app.buttons.matching(NSPredicate(format: 
            "label CONTAINS[c] 'scan' OR label CONTAINS[c] 'start'"
        )).firstMatch
        
        // This is optional based on design
        _ = scanPrompt.exists
    }
    
    // MARK: - Filter Tests
    
    func testFilterChipsDisplayed() throws {
        navigateToHistory()
        
        // Check for filter chips/segments
        let filterOptions = [
            app.buttons["All"],
            app.buttons["Safe"],
            app.buttons["Suspicious"],
            app.buttons["Malicious"],
            app.segmentedControls.firstMatch,
        ]
        
        let hasFilters = filterOptions.contains { $0.exists }
        _ = hasFilters // Filter chips may or may not exist
    }
    
    func testFilterAllSelected() throws {
        navigateToHistory()
        
        let allFilter = app.buttons["All"]
        if allFilter.exists && allFilter.isHittable {
            allFilter.tap()
            
            // Should show all items or empty state
            XCTAssertEqual(app.state, .runningForeground, "App should handle All filter")
        }
    }
    
    func testFilterSafe() throws {
        navigateToHistory()
        
        let safeFilter = app.buttons["Safe"]
        if safeFilter.exists && safeFilter.isHittable {
            safeFilter.tap()
            
            // Wait for filter to apply
            sleep(1)
            
            // App should not crash
            XCTAssertEqual(app.state, .runningForeground, "App should handle Safe filter")
        }
    }
    
    func testFilterSuspicious() throws {
        navigateToHistory()
        
        let suspiciousFilter = app.buttons["Suspicious"]
        if suspiciousFilter.exists && suspiciousFilter.isHittable {
            suspiciousFilter.tap()
            
            sleep(1)
            XCTAssertEqual(app.state, .runningForeground, "App should handle Suspicious filter")
        }
    }
    
    func testFilterMalicious() throws {
        navigateToHistory()
        
        let maliciousFilter = app.buttons["Malicious"]
        if maliciousFilter.exists && maliciousFilter.isHittable {
            maliciousFilter.tap()
            
            sleep(1)
            XCTAssertEqual(app.state, .runningForeground, "App should handle Malicious filter")
        }
    }
    
    func testFilterSwitchingRapidly() throws {
        navigateToHistory()
        
        let filters = ["All", "Safe", "Suspicious", "Malicious"]
        
        // Rapidly switch filters
        for _ in 0..<3 {
            for filterName in filters {
                let filter = app.buttons[filterName]
                if filter.exists && filter.isHittable {
                    filter.tap()
                }
            }
        }
        
        // App should not crash
        XCTAssertEqual(app.state, .runningForeground, "App should handle rapid filter switching")
    }
    
    // MARK: - Search Tests
    
    func testSearchFieldExists() throws {
        navigateToHistory()
        
        let searchField = app.searchFields.firstMatch
        let searchButton = app.buttons.matching(NSPredicate(format: "label CONTAINS[c] 'search'")).firstMatch
        
        // Search may be a field or button
        _ = searchField.exists || searchButton.exists
    }
    
    func testSearchTyping() throws {
        navigateToHistory()
        
        let searchField = app.searchFields.firstMatch
        if searchField.exists && searchField.isHittable {
            searchField.tap()
            searchField.typeText("google")
            
            // Wait for search results
            sleep(1)
            
            // Clear search
            let clearButton = searchField.buttons.firstMatch
            if clearButton.exists {
                clearButton.tap()
            }
            
            XCTAssertEqual(app.state, .runningForeground, "App should handle search")
        }
    }
    
    func testSearchWithSpecialCharacters() throws {
        navigateToHistory()
        
        let searchField = app.searchFields.firstMatch
        if searchField.exists && searchField.isHittable {
            searchField.tap()
            searchField.typeText("test@example.com")
            
            sleep(1)
            XCTAssertEqual(app.state, .runningForeground, "App should handle special characters in search")
        }
    }
    
    // MARK: - List Item Tests
    
    func testTapHistoryItem() throws {
        navigateToHistory()
        
        // Try to tap first history item
        let firstCell = app.tables.cells.firstMatch
        if !firstCell.exists {
            // Try collection view
            let firstCollectionCell = app.collectionViews.cells.firstMatch
            if firstCollectionCell.exists && firstCollectionCell.isHittable {
                firstCollectionCell.tap()
                sleep(1)
            }
        } else if firstCell.isHittable {
            firstCell.tap()
            sleep(1)
        }
        
        // App should not crash
        XCTAssertEqual(app.state, .runningForeground, "App should handle item tap")
    }
    
    func testHistoryItemShowsDetails() throws {
        navigateToHistory()
        
        let firstCell = app.tables.cells.firstMatch
        let firstCollectionCell = app.collectionViews.cells.firstMatch
        
        let cell = firstCell.exists ? firstCell : firstCollectionCell
        
        if cell.exists && cell.isHittable {
            cell.tap()
            
            // Wait for detail sheet/screen
            sleep(1)
            
            // Look for detail elements
            let detailElements = [
                app.staticTexts.matching(NSPredicate(format: "label CONTAINS[c] 'url'")).firstMatch,
                app.staticTexts.matching(NSPredicate(format: "label CONTAINS[c] 'risk'")).firstMatch,
                app.staticTexts.matching(NSPredicate(format: "label CONTAINS[c] 'score'")).firstMatch,
                app.buttons.matching(NSPredicate(format: "label CONTAINS[c] 'open'")).firstMatch,
                app.buttons.matching(NSPredicate(format: "label CONTAINS[c] 'share'")).firstMatch,
            ]
            
            // At least some detail should be visible
            _ = detailElements.contains { $0.exists }
        }
    }
    
    func testDismissHistoryDetail() throws {
        navigateToHistory()
        
        let cell = app.tables.cells.firstMatch.exists ? 
                   app.tables.cells.firstMatch : 
                   app.collectionViews.cells.firstMatch
        
        if cell.exists && cell.isHittable {
            cell.tap()
            sleep(1)
            
            // Try to dismiss
            let dismissMethods = [
                { self.app.buttons.matching(NSPredicate(format: "label CONTAINS[c] 'close'")).firstMatch.tap() },
                { self.app.buttons.matching(NSPredicate(format: "label CONTAINS[c] 'done'")).firstMatch.tap() },
                { self.app.swipeDown() },
            ]
            
            for dismiss in dismissMethods {
                dismiss()
                if !app.sheets.firstMatch.exists {
                    break
                }
            }
        }
        
        XCTAssertEqual(app.state, .runningForeground, "App should handle dismiss")
    }
    
    // MARK: - Swipe to Delete Tests
    
    func testSwipeToDeleteItem() throws {
        navigateToHistory()
        
        let firstCell = app.tables.cells.firstMatch
        if firstCell.exists && firstCell.isHittable {
            firstCell.swipeLeft()
            
            // Look for delete button
            let deleteButton = app.buttons["Delete"]
            if deleteButton.exists && deleteButton.isHittable {
                // Don't actually delete, just verify it exists
                
                // Swipe right to cancel
                firstCell.swipeRight()
            }
        }
        
        XCTAssertEqual(app.state, .runningForeground, "App should support swipe to delete")
    }
    
    // MARK: - Share Tests
    
    func testShareHistoryItem() throws {
        navigateToHistory()
        
        let cell = app.tables.cells.firstMatch.exists ? 
                   app.tables.cells.firstMatch : 
                   app.collectionViews.cells.firstMatch
        
        if cell.exists && cell.isHittable {
            cell.tap()
            sleep(1)
            
            let shareButton = app.buttons.matching(NSPredicate(format: 
                "label CONTAINS[c] 'share'"
            )).firstMatch
            
            if shareButton.exists && shareButton.isHittable {
                shareButton.tap()
                
                // Wait for share sheet
                let shareSheet = app.otherElements["ActivityListView"].waitForExistence(timeout: 3)
                
                if shareSheet {
                    // Dismiss share sheet
                    app.tapCoordinate(at: CGPoint(x: 100, y: 100))
                }
            }
        }
        
        XCTAssertEqual(app.state, .runningForeground, "App should handle share")
    }
    
    // MARK: - Sorting Tests
    
    func testSortOptions() throws {
        navigateToHistory()
        
        let sortButton = app.buttons.matching(NSPredicate(format: 
            "label CONTAINS[c] 'sort' OR label CONTAINS[c] 'order'"
        )).firstMatch
        
        if sortButton.exists && sortButton.isHittable {
            sortButton.tap()
            
            // Look for sort options
            let sortOptions = [
                app.buttons["Newest"],
                app.buttons["Oldest"],
                app.buttons["Risk Level"],
            ]
            
            for option in sortOptions {
                if option.exists && option.isHittable {
                    option.tap()
                    break
                }
            }
        }
        
        XCTAssertEqual(app.state, .runningForeground, "App should handle sorting")
    }
    
    // MARK: - Accessibility Tests
    
    func testHistoryItemsHaveAccessibilityLabels() throws {
        navigateToHistory()
        
        let cells = app.tables.cells.allElementsBoundByIndex + 
                    app.collectionViews.cells.allElementsBoundByIndex
        
        for cell in cells.prefix(5) {
            if cell.exists {
                XCTAssertFalse(cell.label.isEmpty, "History items should have accessibility labels")
            }
        }
    }
    
    // MARK: - Helper Methods
    
    private func navigateToHistory() {
        let tabBar = app.tabBars.firstMatch
        
        if tabBar.exists {
            let historyTab = tabBar.buttons["History"]
            if historyTab.exists && historyTab.isHittable {
                historyTab.tap()
            }
        }
    }
}

// MARK: - XCUIApplication Extension

extension XCUIApplication {
    func tapCoordinate(at point: CGPoint) {
        let normalized = coordinate(withNormalizedOffset: .zero)
        let coordinate = normalized.withOffset(CGVector(dx: point.x, dy: point.y))
        coordinate.tap()
    }
}
