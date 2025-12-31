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

@file:Suppress("InvalidPackageDeclaration")

package com.raouf.mehrguard.web

import com.raouf.mehrguard.core.PhishingEngine
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.Node
import org.w3c.dom.events.Event

// Constants for engine info
private const val HEURISTIC_COUNT = 25
private const val BRAND_COUNT = 52

/**
 * Mehr Guard Web Application
 *
 * Kotlin/JS implementation that runs the PhishingEngine entirely in the browser.
 * Demonstrates true cross-platform code sharing with the common module.
 * All analysis happens client-side - no data leaves the browser.
 */
fun main() {
    console.log("🛡️ Mehr Guard Web loaded - Kotlin/JS initialized")

    // Initialize PhishingEngine - same code as Android, iOS, and Desktop
    val engine = PhishingEngine()
    console.log("📦 PhishingEngine ready for analysis")

    // Initialize new engine components
    val heuristicsEngine = com.raouf.mehrguard.engine.HeuristicsEngine()
    val mlScorer = com.raouf.mehrguard.ml.EnsemblePhishingScorer.default
    val threatIntel = com.raouf.mehrguard.intel.ThreatIntelLookup.createDefault()
    val unicodeAnalyzer = com.raouf.mehrguard.security.UnicodeRiskAnalyzer()
    val psl = com.raouf.mehrguard.engine.PublicSuffixList()
    console.log("🧠 ML Scorer, Threat Intel, and Unicode Analyzer ready")

    // Get DOM elements
    val urlInput = document.getElementById("urlInput") as? HTMLInputElement
    val analyzeBtn = document.getElementById("analyzeBtn") as? HTMLButtonElement

    // Expose the analyze function globally for JavaScript to call
    window.asDynamic().mehrguardAnalyze = { url: String ->
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

                // Get enhanced analysis from new components
                val heuristicResult = heuristicsEngine.analyze(url)
                val mlResult = mlScorer.scoreWithDetails(url)
                val threatResult = threatIntel.lookup(url)

                console.log("✅ Analysis complete: Score=${assessment.score}, Verdict=${assessment.verdict}")
                console.log("   ML Score: ${(mlResult.ensembleScore * 100).toInt()}%")
                console.log("   Reason Codes: ${heuristicResult.reasons.size}")
                console.log("   Known Bad: ${threatResult.isKnownBad}")

                // Convert flags to JS array
                val flagsArray = assessment.flags.toTypedArray()

                // Call the display function defined in HTML with enhanced data
                window.asDynamic().displayResult(
                    assessment.score,
                    assessment.verdict.name,
                    flagsArray,
                    url
                )

                // Also expose enhanced analysis data for advanced UI
                val details = js("{}")
                details.score = assessment.score
                details.verdict = assessment.verdict.name
                details.mlScore = (mlResult.ensembleScore * 100).toInt()
                details.mlConfidence = (mlResult.confidence * 100).toInt()
                details.charScore = (mlResult.charScore * 100).toInt()
                details.featureScore = (mlResult.featureScore * 100).toInt()
                details.isKnownBad = threatResult.isKnownBad
                details.threatConfidence = threatResult.confidence.name
                details.heuristicScore = heuristicResult.score
                details.reasonCount = heuristicResult.reasons.size
                window.asDynamic().lastAnalysisDetails = details

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

    // Expose ML scoring function
    window.asDynamic().mehrguardMlScore = { url: String ->
        try {
            val result = mlScorer.scoreWithDetails(url)
            val obj = js("{}")
            obj.ensembleScore = result.ensembleScore
            obj.charScore = result.charScore
            obj.featureScore = result.featureScore
            obj.confidence = result.confidence
            obj.isPhishing = result.isPhishing
            obj.charRiskLevel = result.charRiskLevel.name
            obj
        } catch (e: Exception) {
            console.error("ML scoring error: ${e.message}")
            val err = js("{}")
            err.error = e.message
            err
        }
    }

    // Expose threat intel lookup
    window.asDynamic().mehrguardThreatLookup = { url: String ->
        try {
            val result = threatIntel.lookup(url)
            val obj = js("{}")
            obj.isKnownBad = result.isKnownBad
            obj.confidence = result.confidence.name
            obj.category = result.category?.name
            obj
        } catch (e: Exception) {
            console.error("Threat lookup error: ${e.message}")
            val err = js("{}")
            err.error = e.message
            err
        }
    }

    // Expose unicode risk analysis
    window.asDynamic().mehrguardUnicodeAnalysis = { host: String ->
        try {
            val result = unicodeAnalyzer.analyze(host)
            val safeDisplay = unicodeAnalyzer.getSafeDisplayHost(host)
            val obj = js("{}")
            obj.hasRisk = result.hasRisk
            obj.isPunycode = result.isPunycode
            obj.hasMixedScript = result.hasMixedScript
            obj.hasConfusables = result.hasConfusables
            obj.hasZeroWidth = result.hasZeroWidth
            obj.riskScore = result.riskScore
            obj.safeDisplayHost = safeDisplay
            obj
        } catch (e: Exception) {
            console.error("Unicode analysis error: ${e.message}")
            val err = js("{}")
            err.error = e.message
            err
        }
    }

    // Expose PSL domain parsing
    window.asDynamic().mehrguardParseDomain = { host: String ->
        try {
            val parsed = psl.parse(host)
            val obj = js("{}")
            obj.effectiveTld = parsed.effectiveTld
            obj.registrableDomain = parsed.registrableDomain
            obj.subdomainDepth = parsed.subdomainDepth
            obj
        } catch (e: Exception) {
            console.error("Domain parsing error: ${e.message}")
            val err = js("{}")
            err.error = e.message
            err
        }
    }

    // Expose heuristics analysis with reason codes
    window.asDynamic().mehrguardHeuristics = { url: String ->
        try {
            val result = heuristicsEngine.analyze(url)
            val obj = js("{}")
            obj.score = result.score
            obj.flagCount = result.flags.size
            obj.reasonCount = result.reasons.size
            obj.reasons = result.reasons.map { reason ->
                val r = js("{}")
                r.code = reason.code
                r.severity = reason.severity.name
                r.description = reason.description
                r
            }.toTypedArray()
            obj
        } catch (e: Exception) {
            console.error("Heuristics error: ${e.message}")
            val err = js("{}")
            err.error = e.message
            err
        }
    }

    // Expose engine info
    val engineInfo = js("{}")
    engineInfo.version = "1.19.0"
    engineInfo.mlModelSize = "~10KB"
    engineInfo.heuristicCount = HEURISTIC_COUNT
    engineInfo.brandCount = BRAND_COUNT
    engineInfo.threatIntelEntries = threatIntel.getStats().exactSetSize
    engineInfo.capabilities = arrayOf("heuristics", "ml", "brand_detection", "threat_intel", "unicode_analysis", "psl")
    window.asDynamic().mehrguardEngineInfo = engineInfo

    // Expose translation function
    window.asDynamic().mehrguardGetTranslation = { key: String ->
        try {
            val language = com.raouf.mehrguard.web.i18n.WebLanguage.current()
            try {
                com.raouf.mehrguard.web.i18n.WebStrings.get(com.raouf.mehrguard.web.i18n.WebStringKey.valueOf(key), language)
            } catch (e: Exception) {
                com.raouf.mehrguard.web.i18n.WebStrings.translate(key, language)
            }
        } catch (e: Exception) {
            key
        }
    }

    // Expose language code
    window.asDynamic().mehrguardGetLanguageCode = {
        try {
            com.raouf.mehrguard.web.i18n.WebLanguage.current().code
        } catch (e: Exception) {
            "en-US"
        }
    }

    // Expose language setter for dynamic switching
    window.asDynamic().mehrguardSetLanguage = { languageCode: String ->
        try {
            // Save to localStorage
            window.localStorage.setItem("mehrguard_language", languageCode)

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
    window.asDynamic().mehrguardGetAvailableLanguages = {
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
                window.asDynamic().mehrguardAnalyze(url)
            }
        }
    })

    // Initialize Localization
    initializeLocalization()

    // Log ready status
    console.log("🚀 Mehr Guard Web is ready!")
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

    window.asDynamic().mehrguardApplyTranslations = { root: dynamic ->
        val element = root as? Element ?: (document.body as? Element)
        if (element != null) {
            applyLocalization(element)
        }
    }
}

private fun applyLocalization(root: Element) {
    val language = com.raouf.mehrguard.web.i18n.WebLanguage.current()
    translateDataI18nElements(root, language)
    translateDataI18nAttributes(root, language)
    translateCommonAttributes(root, language)
    translateMetaContent(language)
    translateTextNodes(root, language)
}

private fun translateDataI18nElements(root: Element, language: com.raouf.mehrguard.web.i18n.WebLanguage) {
    val elements = root.querySelectorAll("[data-i18n]")
    for (i in 0 until elements.length) {
        val element = elements.item(i) as? org.w3c.dom.HTMLElement ?: continue
        val key = element.getAttribute("data-i18n") ?: continue

        val translation = try {
            com.raouf.mehrguard.web.i18n.WebStrings.get(com.raouf.mehrguard.web.i18n.WebStringKey.valueOf(key), language)
        } catch (e: Exception) {
            com.raouf.mehrguard.web.i18n.WebStrings.translate(key, language)
        }

        element.innerText = translation
    }
}

private fun translateDataI18nAttributes(root: Element, language: com.raouf.mehrguard.web.i18n.WebLanguage) {
    val attributes = listOf("placeholder", "title", "aria-label", "alt")
    attributes.forEach { attr ->
        val selector = "[data-i18n-$attr]"
        val elements = root.querySelectorAll(selector)
        for (i in 0 until elements.length) {
            val element = elements.item(i) as? Element ?: continue
            val key = element.getAttribute("data-i18n-$attr") ?: continue
            val translation = try {
                com.raouf.mehrguard.web.i18n.WebStrings.get(com.raouf.mehrguard.web.i18n.WebStringKey.valueOf(key), language)
            } catch (e: Exception) {
                com.raouf.mehrguard.web.i18n.WebStrings.translate(key, language)
            }
            element.setAttribute(attr, translation)
        }
    }
}

private fun translateCommonAttributes(root: Element, language: com.raouf.mehrguard.web.i18n.WebLanguage) {
    val attributes = listOf("placeholder", "title", "aria-label", "alt")
    val elements = root.querySelectorAll("*")
    for (i in 0 until elements.length) {
        val element = elements.item(i) as? Element ?: continue
        attributes.forEach { attr ->
            val value = element.getAttribute(attr) ?: return@forEach
            if (value.isBlank()) return@forEach
            val translation = com.raouf.mehrguard.web.i18n.WebStrings.translate(value, language)
            if (translation != value) {
                element.setAttribute(attr, translation)
            }
        }
    }
}

private fun translateMetaContent(language: com.raouf.mehrguard.web.i18n.WebLanguage) {
    val metaElements = document.querySelectorAll("meta[name]")
    val translatableMeta = setOf("description", "apple-mobile-web-app-title", "application-name")
    for (i in 0 until metaElements.length) {
        val element = metaElements.item(i) as? Element ?: continue
        val name = element.getAttribute("name") ?: continue
        if (!translatableMeta.contains(name)) continue
        val content = element.getAttribute("content") ?: continue
        if (content.isBlank()) continue
        val translation = com.raouf.mehrguard.web.i18n.WebStrings.translate(content, language)
        if (translation != content) {
            element.setAttribute("content", translation)
        }
    }
}

private fun translateTextNodes(root: Element, language: com.raouf.mehrguard.web.i18n.WebLanguage) {
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
            val translation = com.raouf.mehrguard.web.i18n.WebStrings.translate(trimmed, language)
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
