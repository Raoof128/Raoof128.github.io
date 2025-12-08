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

package com.qrshield.android.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import org.junit.Rule
import org.junit.Test

/**
 * Baseline Profile Generator for QR-SHIELD
 * 
 * Generates a baseline profile that improves app startup
 * and runtime performance by pre-compiling critical paths.
 * 
 * Run this with:
 * ./gradlew :androidApp:generateBaselineProfile
 * 
 * The generated profile will be included in release builds.
 */
class BaselineProfileGenerator {
    
    @get:Rule
    val rule = BaselineProfileRule()
    
    @Test
    fun generateBaselineProfile() {
        rule.collect(
            packageName = "com.qrshield.android",
            includeInStartupProfile = true
        ) {
            // Critical startup path
            pressHome()
            startActivityAndWait()
            
            // Wait for UI to settle
            Thread.sleep(1000)
            
            // Simulate user journeys for profile collection
            
            // Journey 1: Navigate through tabs
            device.findObject(
                androidx.test.uiautomator.By.desc("History screen, tap to navigate")
            )?.click()
            Thread.sleep(500)
            
            device.findObject(
                androidx.test.uiautomator.By.desc("Settings screen, tap to navigate")
            )?.click()
            Thread.sleep(500)
            
            device.findObject(
                androidx.test.uiautomator.By.desc("QR code scanner screen, tap to navigate")
            )?.click()
            Thread.sleep(500)
            
            // Journey 2: Start scanning (if permission granted)
            device.findObject(
                androidx.test.uiautomator.By.desc("Start scanning for QR codes using camera")
            )?.click()
            Thread.sleep(1000)
            
            // Close scanner
            device.findObject(
                androidx.test.uiautomator.By.desc("Close scanner and return to home")
            )?.click()
        }
    }
}
