/*
 * Copyright 2025-2026 Mehr Guard Contributors
 * Licensed under the Apache License, Version 2.0
 */

package com.raouf.mehrguard.intel

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

/**
 * Tests for ThreatIntelLookup.
 */
class ThreatIntelLookupTest {

    @Test
    fun `create default threat lookup`() {
        val lookup = ThreatIntelLookup.createDefault()
        assertNotNull(lookup)
        
        val metadata = lookup.getMetadata()
        assertTrue(metadata.entryCount > 0)
        assertEquals("mehrguard-bundled", metadata.source)
    }

    @Test
    fun `known bad domain is detected`() {
        val lookup = ThreatIntelLookup.create(
            listOf("malicious.tk", "phishing.com", "evil.site")
        )
        
        val result = lookup.lookup("malicious.tk")
        assertTrue(result.isKnownBad)
        assertEquals(ThreatIntelLookup.LookupResult.Confidence.CONFIRMED, result.confidence)
    }

    @Test
    fun `clean domain returns clean result`() {
        val lookup = ThreatIntelLookup.create(listOf("bad.com"))
        
        val result = lookup.lookup("good.com")
        assertFalse(result.isKnownBad)
        assertEquals(ThreatIntelLookup.LookupResult.Confidence.CLEAN, result.confidence)
    }

    @Test
    fun `url normalization works`() {
        val lookup = ThreatIntelLookup.create(listOf("malicious.tk"))
        
        // All of these should match
        assertTrue(lookup.isDenied("https://malicious.tk"))
        assertTrue(lookup.isDenied("http://malicious.tk"))
        assertTrue(lookup.isDenied("https://www.malicious.tk"))
        assertTrue(lookup.isDenied("MALICIOUS.TK"))
        assertTrue(lookup.isDenied("https://malicious.tk/path/to/page"))
        assertTrue(lookup.isDenied("https://malicious.tk?query=param"))
    }

    @Test
    fun `stats returns correct values`() {
        val domains = listOf("a.com", "b.com", "c.com")
        val lookup = ThreatIntelLookup.create(domains)
        
        val stats = lookup.getStats()
        assertEquals(3, stats.exactSetSize)
        assertEquals(3, stats.bloomFilterSize)
        assertTrue(stats.estimatedFPR >= 0.0)
    }

    @Test
    fun `default lookup contains bundled domains`() {
        val lookup = ThreatIntelLookup.createDefault()
        
        // Check some bundled domains
        assertTrue(lookup.isDenied("paypa1-secure.com"))
        assertTrue(lookup.isDenied("amaz0n-verify.com"))
    }

    @Test
    fun `two-stage lookup reduces false positives`() {
        val lookup = ThreatIntelLookup.create(
            listOf("confirmed-bad.com")
        )
        
        // Confirmed bad
        val badResult = lookup.lookup("confirmed-bad.com")
        assertTrue(badResult.isKnownBad)
        assertEquals(ThreatIntelLookup.LookupResult.Confidence.CONFIRMED, badResult.confidence)
        
        // Clean domain
        val cleanResult = lookup.lookup("definitely-safe.org")
        assertFalse(cleanResult.isKnownBad)
    }
}
