/*
 * Copyright 2025-2026 Mehr Guard Contributors
 * Licensed under the Apache License, Version 2.0
 */

package com.raouf.mehrguard.security

import com.raouf.mehrguard.model.ReasonCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Tests for UnicodeRiskAnalyzer.
 */
class UnicodeRiskAnalyzerTest {

    private val analyzer = UnicodeRiskAnalyzer()

    @Test
    fun `safe ASCII host has no risk`() {
        val result = analyzer.analyze("example.com")
        assertFalse(result.hasRisk)
        assertEquals(0, result.riskScore)
        assertTrue(result.reasons.isEmpty())
    }

    @Test
    fun `punycode domain detected as homograph`() {
        val result = analyzer.analyze("xn--pple-43d.com")
        assertTrue(result.hasRisk)
        assertTrue(result.isPunycode)
        assertTrue(result.reasons.contains(ReasonCode.REASON_HOMOGRAPH))
    }

    @Test
    fun `zero-width characters detected`() {
        val result = analyzer.analyze("goo\u200Bgle.com")
        assertTrue(result.hasRisk)
        assertTrue(result.hasZeroWidth)
        assertTrue(result.reasons.contains(ReasonCode.REASON_ZERO_WIDTH_CHARS))
    }

    @Test
    fun `mixed Latin and Cyrillic detected`() {
        // "а" is Cyrillic, rest is Latin
        val result = analyzer.analyze("аpple.com")
        assertTrue(result.hasRisk)
        assertTrue(result.hasMixedScript)
        assertTrue(result.reasons.contains(ReasonCode.REASON_MIXED_SCRIPT))
    }

    @Test
    fun `confusable characters detected`() {
        // Contains Cyrillic 'а' which looks like Latin 'a'
        val result = analyzer.analyze("pаypal.com")
        assertTrue(result.hasConfusables)
        assertTrue(result.reasons.contains(ReasonCode.REASON_LOOKALIKE_CHARS))
    }

    @Test
    fun `safe display host for ASCII`() {
        val display = analyzer.getSafeDisplayHost("example.com")
        assertEquals("example.com", display)
    }

    @Test
    fun `safe display host for punycode shows warning`() {
        val display = analyzer.getSafeDisplayHost("xn--domain.com")
        assertTrue(display.startsWith("[IDN:"))
    }

    @Test
    fun `skeleton normalizes confusables`() {
        val skeleton1 = analyzer.toSkeleton("аpple.com")  // Cyrillic а
        val skeleton2 = analyzer.toSkeleton("apple.com")   // Latin a
        assertEquals(skeleton1, skeleton2)
    }

    @Test
    fun `areConfusable detects similar domains`() {
        assertTrue(analyzer.areConfusable("аpple.com", "apple.com"))
        assertFalse(analyzer.areConfusable("apple.com", "apple.com"))
        assertFalse(analyzer.areConfusable("google.com", "facebook.com"))
    }

    @Test
    fun `empty host returns safe result`() {
        val result = analyzer.analyze("")
        assertFalse(result.hasRisk)
    }

    @Test
    fun `score is capped at 100`() {
        // Create a host with multiple issues
        val result = analyzer.analyze("xn--test\u200Bаbc.com")
        assertTrue(result.riskScore <= 100)
    }
}
