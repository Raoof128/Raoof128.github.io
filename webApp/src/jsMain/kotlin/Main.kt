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
import org.w3c.dom.Element
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.Node
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

    // Expose language setter for dynamic switching
    window.asDynamic().qrshieldSetLanguage = { languageCode: String ->
        try {
            // Save to localStorage
            window.localStorage.setItem("qrshield_language", languageCode)
            
            // Re-apply translations to the page
            val body = document.body as? Element
            if (body != null) {
                applyLocalization(body)
            }
            
            console.log("🌍 Language set to: $languageCode")
            true
        } catch (e: Exception) {
            console.error("❌ Failed to set language: ${e.message}")
            false
        }
    }

    // Expose list of available languages
    window.asDynamic().qrshieldGetAvailableLanguages = {
        arrayOf(
            js("({code: 'en', name: 'English'})"),
            js("({code: 'ar', name: 'العربية'})"),
            js("({code: 'de', name: 'Deutsch'})"),
            js("({code: 'es', name: 'Español'})"),
            js("({code: 'fr', name: 'Français'})"),
            js("({code: 'hi', name: 'हिन्दी'})"),
            js("({code: 'id', name: 'Bahasa Indonesia'})"),
            js("({code: 'it', name: 'Italiano'})"),
            js("({code: 'ja', name: '日本語'})"),
            js("({code: 'ko', name: '한국어'})"),
            js("({code: 'pt', name: 'Português'})"),
            js("({code: 'ru', name: 'Русский'})"),
            js("({code: 'th', name: 'ไทย'})"),
            js("({code: 'tr', name: 'Türkçe'})"),
            js("({code: 'vi', name: 'Tiếng Việt'})"),
            js("({code: 'zh', name: '中文'})")
        )
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
    console.log("🌍 Initializing localization")
    val body = document.body as? Element
    if (body != null) {
        applyLocalization(body)
    }

    window.asDynamic().qrshieldApplyTranslations = { root: dynamic ->
        val element = root as? Element ?: (document.body as? Element)
        if (element != null) {
            applyLocalization(element)
        }
    }
}

private fun applyLocalization(root: Element) {
    val language = com.qrshield.web.i18n.WebLanguage.current()
    translateDataI18nElements(root, language)
    translateDataI18nAttributes(root, language)
    translateCommonAttributes(root, language)
    translateMetaContent(language)
    translateTextNodes(root, language)
}

private fun translateDataI18nElements(root: Element, language: com.qrshield.web.i18n.WebLanguage) {
    val elements = root.querySelectorAll("[data-i18n]")
    for (i in 0 until elements.length) {
        val element = elements.item(i) as? org.w3c.dom.HTMLElement ?: continue
        val key = element.getAttribute("data-i18n") ?: continue

        val translation = try {
            com.qrshield.web.i18n.WebStrings.get(com.qrshield.web.i18n.WebStringKey.valueOf(key), language)
        } catch (e: Exception) {
            com.qrshield.web.i18n.WebStrings.translate(key, language)
        }

        element.innerText = translation
    }
}

private fun translateDataI18nAttributes(root: Element, language: com.qrshield.web.i18n.WebLanguage) {
    val attributes = listOf("placeholder", "title", "aria-label", "alt")
    attributes.forEach { attr ->
        val selector = "[data-i18n-$attr]"
        val elements = root.querySelectorAll(selector)
        for (i in 0 until elements.length) {
            val element = elements.item(i) as? Element ?: continue
            val key = element.getAttribute("data-i18n-$attr") ?: continue
            val translation = try {
                com.qrshield.web.i18n.WebStrings.get(com.qrshield.web.i18n.WebStringKey.valueOf(key), language)
            } catch (e: Exception) {
                com.qrshield.web.i18n.WebStrings.translate(key, language)
            }
            element.setAttribute(attr, translation)
        }
    }
}

private fun translateCommonAttributes(root: Element, language: com.qrshield.web.i18n.WebLanguage) {
    val attributes = listOf("placeholder", "title", "aria-label", "alt")
    val elements = root.querySelectorAll("*")
    for (i in 0 until elements.length) {
        val element = elements.item(i) as? Element ?: continue
        attributes.forEach { attr ->
            val value = element.getAttribute(attr) ?: return@forEach
            if (value.isBlank()) return@forEach
            val translation = com.qrshield.web.i18n.WebStrings.translate(value, language)
            if (translation != value) {
                element.setAttribute(attr, translation)
            }
        }
    }
}

private fun translateMetaContent(language: com.qrshield.web.i18n.WebLanguage) {
    val metaElements = document.querySelectorAll("meta[name]")
    val translatableMeta = setOf("description", "apple-mobile-web-app-title", "application-name")
    for (i in 0 until metaElements.length) {
        val element = metaElements.item(i) as? Element ?: continue
        val name = element.getAttribute("name") ?: continue
        if (!translatableMeta.contains(name)) continue
        val content = element.getAttribute("content") ?: continue
        if (content.isBlank()) continue
        val translation = com.qrshield.web.i18n.WebStrings.translate(content, language)
        if (translation != content) {
            element.setAttribute("content", translation)
        }
    }
}

private fun translateTextNodes(root: Element, language: com.qrshield.web.i18n.WebLanguage) {
    val elements = root.querySelectorAll("*")
    for (i in 0 until elements.length) {
        val element = elements.item(i) as? Element ?: continue
        if (isIconElement(element)) continue
        if (element.closest("[data-i18n]") != null) continue

        val children = element.childNodes
        for (j in 0 until children.length) {
            val node = children.item(j) ?: continue
            if (node.nodeType != Node.TEXT_NODE) continue
            val rawText = node.nodeValue ?: continue
            val trimmed = rawText.trim()
            if (trimmed.isBlank()) continue
            val translation = com.qrshield.web.i18n.WebStrings.translate(trimmed, language)
            if (translation != trimmed) {
                val leading = rawText.takeWhile { it.isWhitespace() }
                val trailing = rawText.takeLastWhile { it.isWhitespace() }
                node.nodeValue = leading + translation + trailing
            }
        }
    }
}

private fun isIconElement(element: Element): Boolean {
    val classAttr = element.getAttribute("class") ?: return false
    val classes = classAttr.split(" ")
    return classes.any {
        it.startsWith("material-icons") || it.startsWith("material-symbols")
    }
}
