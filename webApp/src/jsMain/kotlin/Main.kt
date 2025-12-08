package com.qrshield.web

import com.qrshield.core.PhishingEngine
import com.qrshield.model.Verdict
import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLUListElement

/**
 * QR-SHIELD Web Application
 * 
 * Kotlin/JS implementation that runs the PhishingEngine in the browser.
 * Demonstrates true cross-platform code sharing with the common module.
 */
fun main() {
    console.log("🛡️ QR-SHIELD Web loaded")
    
    // Initialize PhishingEngine
    val engine = PhishingEngine()
    
    // Get DOM elements
    val urlInput = document.getElementById("urlInput") as? HTMLInputElement
    val analyzeBtn = document.getElementById("analyzeBtn") as? HTMLButtonElement
    val resultDiv = document.getElementById("result") as? HTMLDivElement
    val scoreDisplay = document.getElementById("scoreDisplay") as? HTMLDivElement
    val verdictDisplay = document.getElementById("verdictDisplay") as? HTMLDivElement
    val flagsDisplay = document.getElementById("flagsDisplay") as? HTMLUListElement
    
    // Handle analyze button click
    analyzeBtn?.addEventListener("click", {
        val url = urlInput?.value ?: return@addEventListener
        if (url.isBlank()) return@addEventListener
        
        // Run analysis using shared KMP PhishingEngine
        val assessment = engine.analyze(url)
        
        // Display results
        resultDiv?.style?.display = "block"
        scoreDisplay?.textContent = assessment.score.toString()
        verdictDisplay?.textContent = assessment.verdict.name
        
        // Set result color based on verdict
        resultDiv?.className = when (assessment.verdict) {
            Verdict.SAFE -> "result safe"
            Verdict.SUSPICIOUS -> "result suspicious"
            Verdict.MALICIOUS -> "result malicious"
            else -> "result"
        }
        
        // Display flags
        flagsDisplay?.innerHTML = ""
        assessment.flags.forEach { flag ->
            val li = document.createElement("li")
            li.textContent = "⚠️ $flag"
            flagsDisplay?.appendChild(li)
        }
        
        console.log("Analysis complete: Score=${assessment.score}, Verdict=${assessment.verdict}")
    })
    
    // Handle enter key in input
    urlInput?.addEventListener("keypress", { event ->
        if (event.asDynamic().key == "Enter") {
            analyzeBtn?.click()
        }
    })
}
