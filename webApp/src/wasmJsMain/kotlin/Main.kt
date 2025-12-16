package com.qrshield.web

import com.qrshield.core.PhishingEngine
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.KeyboardEvent

/**
 * QR-SHIELD Web Application (Wasm)
 *
 * Kotlin/Wasm implementation that runs the PhishingEngine using WebAssembly.
 * Demonstrates the future of web development with Kotlin.
 */
fun main() {
    println("🛡️ QR-SHIELD Web loaded - Kotlin/Wasm initialized")

    val engine = PhishingEngine()
    println("📦 PhishingEngine ready (Wasm)")

    // Export functions to global scope (polyfill for dynamic)
    // In strict Wasm, we should use @JsExport or attach to window via JS interop
    // Here we use a safe interop pattern
    
    val urlInput = document.getElementById("urlInput") as? HTMLInputElement
    val analyzeBtn = document.getElementById("analyzeBtn") as? HTMLButtonElement

    // Attach event listener directly since we can't easily export "qrshieldAnalyze" 
    // to global window without @JsExport setup or JS helper.
    // We'll hijack the button click.
    
    analyzeBtn?.onclick = {
        val url = urlInput?.value?.trim() ?: ""
        if (url.isNotBlank()) {
            analyzeUrl(engine, url, analyzeBtn)
        }
        null
    }

    urlInput?.onkeypress = { event ->
        if ((event as KeyboardEvent).key == "Enter") {
            val url = urlInput?.value?.trim() ?: ""
            if (url.isNotBlank()) {
                analyzeUrl(engine, url, analyzeBtn)
            }
        }
    }
}

fun analyzeUrl(engine: PhishingEngine, url: String, analyzeBtn: HTMLButtonElement?) {
    console.log("🔍 Analyzing URL: $url")

    // Show loading
    analyzeBtn?.className += " loading"
    analyzeBtn?.innerHTML = """<div class="spinner"></div><span>Analyzing...</span>"""
    analyzeBtn?.disabled = true

    window.setTimeout({
        try {
            val assessment = engine.analyzeBlocking(url)
            console.log("✅ Analysis complete: Score=${assessment.score}")

            // Call existing JS display function via interop
            displayResult(
                score = assessment.score,
                verdict = assessment.verdict.name,
                flags = assessment.flags.toTypedArray(),
                url = url
            )

        } catch (e: Exception) {
            console.error("❌ Analysis error: ${e.message}")
            showToast("Error: ${e.message}")
        } finally {
            analyzeBtn?.className = analyzeBtn?.className?.replace(" loading", "") ?: ""
            analyzeBtn?.innerHTML = """<span class="material-icons-round">search</span>Analyze URL"""
            analyzeBtn?.disabled = false
        }
    }, 100)
}

// Interop to call the existing JS functions defined in index.html
// In Wasm, we declare them as external with @JsName to map to actual JS names
@JsName("displayResult")
external fun displayResult(score: Int, verdict: String, flags: Array<String>, url: String)

@JsName("showToast")
external fun showToast(message: String)

