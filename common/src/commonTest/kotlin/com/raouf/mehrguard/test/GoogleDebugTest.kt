package com.raouf.mehrguard.test

import com.raouf.mehrguard.core.PhishingEngine
import kotlin.test.Test
import kotlin.test.assertEquals

class GoogleDebugTest {
    @Test
    fun `debug google com verdict`() {
        val engine = PhishingEngine()
        val result = engine.analyzeBlocking("https://www.google.com")
        
        println("\n=== GOOGLE.COM DEBUG ===")
        println("Score: ${result.score}")
        println("Verdict: ${result.verdict}")
        println("Heuristic: ${result.details.heuristicScore}/40")
        println("ML: ${result.details.mlScore}/30")
        println("Brand: ${result.details.brandScore}/20")
        println("TLD: ${result.details.tldScore}/10")
        println("Flags: ${result.flags}")
        println("======================\n")
        
        // Score 9 should be SAFE (threshold is 25)
        assertEquals("SAFE", result.verdict.toString(), 
            "Score ${result.score} should produce SAFE verdict (threshold=25)")
    }
}
