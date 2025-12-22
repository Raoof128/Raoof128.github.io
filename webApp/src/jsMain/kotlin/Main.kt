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

package com.qrshield.web

import com.qrshield.core.PhishingEngine
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event

/**
 * QR-SHIELD Web Application
 *
 * Kotlin/JS implementation that runs the PhishingEngine entirely in the browser.
 * Demonstrates true cross-platform code sharing with the common module.
 * All analysis happens client-side - no data leaves the browser.
 */
fun main() {
    console.log("🛡️ QR-SHIELD Web loaded - Kotlin/JS initialized")

    // Initialize PhishingEngine - same code as Android, iOS, and Desktop
    val engine = PhishingEngine()
    console.log("📦 PhishingEngine ready for analysis")

    // Get DOM elements
    val urlInput = document.getElementById("urlInput") as? HTMLInputElement
    val analyzeBtn = document.getElementById("analyzeBtn") as? HTMLButtonElement

    // Expose the analyze function globally for JavaScript to call
    window.asDynamic().qrshieldAnalyze = { url: String ->
        console.log("🔍 Analyzing URL: $url")

        // Show loading state
        analyzeBtn?.classList?.add("loading")
        analyzeBtn?.innerHTML = """<div class="spinner"></div><span>Analyzing...</span>"""
        analyzeBtn?.disabled = true

        // Run analysis asynchronously to allow UI update
        window.setTimeout({
            try {
                // Run analysis using SHARED KMP PhishingEngine
                val assessment = engine.analyzeBlocking(url)

                console.log("✅ Analysis complete: Score=${assessment.score}, Verdict=${assessment.verdict}")

                // Convert flags to JS array
                val flagsArray = assessment.flags.toTypedArray()

                // Call the display function defined in HTML
                window.asDynamic().displayResult(
                    assessment.score,
                    assessment.verdict.name,
                    flagsArray,
                    url
                )
            } catch (e: Exception) {
                console.error("❌ Analysis error: ${e.message}")
                window.asDynamic().showToast("Error analyzing URL: ${e.message}")

                // Reset button
                analyzeBtn?.classList?.remove("loading")
                analyzeBtn?.innerHTML = """<span class="material-icons-round">search</span>Analyze URL"""
                analyzeBtn?.disabled = false
            }
        }, 100)

    }

    // Expose translation function
    window.asDynamic().qrshieldGetTranslation = { key: String -> 
        try {
            val language = com.qrshield.web.i18n.WebLanguage.current()
            try {
                com.qrshield.web.i18n.WebStrings.get(com.qrshield.web.i18n.WebStringKey.valueOf(key), language)
            } catch (e: Exception) {
                com.qrshield.web.i18n.WebStrings.translate(key, language)
            }
        } catch (e: Exception) {
            key
        }
    }

    // Expose language code
    window.asDynamic().qrshieldGetLanguageCode = {
        try {
            com.qrshield.web.i18n.WebLanguage.current().code
        } catch (e: Exception) {
            "en-US"
        }
    }

    // Handle enter key in input
    urlInput?.addEventListener("keypress", { event: Event ->
        if (event.asDynamic().key == "Enter") {
            event.preventDefault()
            val url = urlInput.value?.trim() ?: ""
            if (url.isNotBlank()) {
                window.asDynamic().qrshieldAnalyze(url)
            }
        }
    })

    // Initialize Localization
    initializeLocalization()

    // Log ready status
    console.log("🚀 QR-SHIELD Web is ready!")
    console.log("   • Heuristic analysis: ✓")
    console.log("   • ML scoring: ✓")
    console.log("   • Brand detection: ✓")
    console.log("   • TLD analysis: ✓")
    console.log("   • 100% client-side: ✓")
}

fun initializeLocalization() {
    val language = com.qrshield.web.i18n.WebLanguage.current()
    console.log("🌍 Initializing localization for: ${language.code}")

    val elements = document.querySelectorAll("[data-i18n]")
    for (i in 0 until elements.length) {
        val element = elements.item(i) as? org.w3c.dom.HTMLElement ?: continue
        val key = element.getAttribute("data-i18n") ?: continue

        val translation = try {
             com.qrshield.web.i18n.WebStrings.get(com.qrshield.web.i18n.WebStringKey.valueOf(key), language)
        } catch (e: Exception) {
             com.qrshield.web.i18n.WebStrings.translate(key, language)
        }
        
        // Preserve HTML structure if needed, but for now simple text replacement
        // Check if we need to preserve children (like icons)?
        // If the element has children (like <span class="material-icons">), innerText will wipe them.
        // Strategy: Only update text nodes or assume element contains ONLY text.
        // OR: Look for a specific <span> inside, or assume the element IS the text container.
        // For <a class="nav-link"><span>Dashboard</span></a>, we should put data-i18n on the inner span.
        element.innerText = translation
    }
}
