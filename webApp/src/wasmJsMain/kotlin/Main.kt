@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.raouf.mehrguard.web

import com.raouf.mehrguard.core.PhishingEngine
import kotlin.js.JsAny
import kotlin.js.JsString

/**
 * Mehr Guard Web Application (Wasm)
 *
 * Kotlin/Wasm implementation that runs the PhishingEngine using WebAssembly.
 * Uses external declarations for browser API interop.
 *
 * @since 1.17.25
 */
fun main() {
    consoleLog("🛡️ Mehr Guard Web loaded - Kotlin/Wasm initialized")

    val engine = PhishingEngine()
    consoleLog("📦 PhishingEngine ready (Wasm)")

    // Expose the analysis function to JavaScript
    setupAnalyzeHandler(engine)
}

/**
 * Set up the analyze button click handler via JavaScript interop.
 */
private fun setupAnalyzeHandler(engine: PhishingEngine) {
    // Register the analysis function with the global window object
    registerAnalyzeFunction { url: JsString ->
        try {
            val urlKotlin = url.toString()
            consoleLog("🔍 Analyzing URL: $urlKotlin")
            
            val assessment = engine.analyzeBlocking(urlKotlin)
            consoleLog("✅ Analysis complete: Score=${assessment.score}")
            
            displayResultJs(
                assessment.score,
                assessment.verdict.name.toJsString(),
                urlKotlin.toJsString()
            )
        } catch (e: Exception) {
            consoleError("❌ Analysis error: ${e.message}")
            showToastJs("Error: ${e.message}".toJsString())
        }
    }
}

// ==================== External JS Interop Declarations ====================

// Console functions
@JsFun("(msg) => console.log(msg)")
private external fun consoleLog(msg: String)

@JsFun("(msg) => console.error(msg)")
private external fun consoleError(msg: String)

// Register analysis function with global scope
@JsFun("(fn) => { window.mehrguardAnalyze = fn; }")
private external fun registerAnalyzeFunction(fn: (JsString) -> Unit)

// Display result via existing JavaScript function
@JsFun("(score, verdict, url) => { if(typeof displayResult === 'function') displayResult(score, verdict, [], url); }")
private external fun displayResultJs(score: Int, verdict: JsString, url: JsString)

// Show toast notification
@JsFun("(msg) => { if(typeof showToast === 'function') showToast(msg); }")
private external fun showToastJs(msg: JsString)
