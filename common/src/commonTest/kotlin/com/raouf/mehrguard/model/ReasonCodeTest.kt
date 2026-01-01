/*
 * Copyright 2025-2026 Mehr Guard Contributors
 * Licensed under the Apache License, Version 2.0
 */

package com.raouf.mehrguard.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Tests for ReasonCode enum and Severity levels.
 */
class ReasonCodeTest {

    @Test
    fun `all reason codes have unique codes`() {
        val codes = ReasonCode.entries.map { it.code }
        val uniqueCodes = codes.toSet()
        assertEquals(codes.size, uniqueCodes.size, "Duplicate code strings found")
    }

    @Test
    fun `fromCode returns correct reason code`() {
        assertEquals(ReasonCode.REASON_HOMOGRAPH, ReasonCode.fromCode("HOMOGRAPH"))
        assertEquals(ReasonCode.REASON_IP_HOST, ReasonCode.fromCode("IP_HOST"))
        assertEquals(ReasonCode.REASON_JAVASCRIPT_URL, ReasonCode.fromCode("JAVASCRIPT_URL"))
    }

    @Test
    fun `fromCode returns null for unknown code`() {
        assertEquals(null, ReasonCode.fromCode("UNKNOWN_CODE"))
        assertEquals(null, ReasonCode.fromCode(""))
    }

    @Test
    fun `bySeverity returns correct codes`() {
        val criticalCodes = ReasonCode.bySeverity(Severity.CRITICAL)
        assertTrue(criticalCodes.contains(ReasonCode.REASON_JAVASCRIPT_URL))
        assertTrue(criticalCodes.contains(ReasonCode.REASON_DATA_URI))
        assertTrue(criticalCodes.contains(ReasonCode.REASON_AT_SYMBOL_INJECTION))
    }

    @Test
    fun `urgent returns critical and high severity`() {
        val urgent = ReasonCode.urgent()
        assertTrue(urgent.all { it.severity == Severity.CRITICAL || it.severity == Severity.HIGH })
        assertTrue(urgent.contains(ReasonCode.REASON_HOMOGRAPH))
        assertTrue(urgent.contains(ReasonCode.REASON_JAVASCRIPT_URL))
        assertFalse(urgent.contains(ReasonCode.REASON_URL_SHORTENER)) // LOW severity
    }

    @Test
    fun `severity isWarning returns correct value`() {
        assertTrue(Severity.CRITICAL.isWarning)
        assertTrue(Severity.HIGH.isWarning)
        assertTrue(Severity.MEDIUM.isWarning)
        assertTrue(Severity.LOW.isWarning)
        assertFalse(Severity.INFO.isWarning)
    }

    @Test
    fun `severity isUrgent returns correct value`() {
        assertTrue(Severity.CRITICAL.isUrgent)
        assertTrue(Severity.HIGH.isUrgent)
        assertFalse(Severity.MEDIUM.isUrgent)
        assertFalse(Severity.LOW.isUrgent)
        assertFalse(Severity.INFO.isUrgent)
    }

    @Test
    fun `all reason codes have descriptions`() {
        ReasonCode.entries.forEach { code ->
            assertNotNull(code.description)
            assertTrue(code.description.isNotEmpty(), "${code.name} has empty description")
        }
    }
}
