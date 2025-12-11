/*
 * Copyright 2024 QR-SHIELD Contributors
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

package com.qrshield.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

/**
 * Comprehensive tests for HomographDetector.
 */
class HomographDetectorTest {

    private val detector = HomographDetector()

    // === PUNYCODE DETECTION ===

    @Test
    fun `detects punycode domain with xn prefix`() {
        val result = detector.detect("xn--pypal-4ve.com")
        assertTrue(result.isHomograph)
        assertNotNull(result.punycode)
        assertTrue(result.score > 0)
    }

    @Test
    fun `detects punycode in subdomain`() {
        val result = detector.detect("login.xn--pypal-4ve.com")
        assertTrue(result.isHomograph)
    }

    // === CYRILLIC HOMOGRAPH DETECTION ===

    @Test
    fun `detects Cyrillic a lookalike`() {
        val result = detector.detect("pаypal.com")  // Uses Cyrillic 'а'
        assertTrue(result.isHomograph)
        assertTrue(result.detectedCharacters.isNotEmpty())
    }

    @Test
    fun `detects Cyrillic e lookalike`() {
        val result = detector.detect("googlе.com")  // Uses Cyrillic 'е'
        assertTrue(result.isHomograph)
    }

    @Test
    fun `detects Cyrillic o lookalike`() {
        val result = detector.detect("gооgle.com")  // Uses Cyrillic 'о'
        assertTrue(result.isHomograph)
    }

    @Test
    fun `detects Cyrillic c lookalike`() {
        val result = detector.detect("faсebook.com")  // Uses Cyrillic 'с'
        assertTrue(result.isHomograph)
    }

    @Test
    fun `detects Cyrillic p lookalike`() {
        val result = detector.detect("аррle.com")  // Uses Cyrillic 'р'
        assertTrue(result.isHomograph)
    }

    // === GREEK HOMOGRAPH DETECTION ===

    @Test
    fun `detects Greek omicron lookalike`() {
        val result = detector.detect("gοοgle.com")  // Uses Greek 'ο'
        assertTrue(result.isHomograph)
    }

    @Test
    fun `detects Greek alpha lookalike`() {
        val result = detector.detect("αmazon.com")  // Uses Greek 'α'
        assertTrue(result.isHomograph)
    }

    // === SAFE DOMAIN TESTS ===

    @Test
    fun `pure ASCII domain is not homograph`() {
        val result = detector.detect("google.com")
        assertFalse(result.isHomograph)
        assertTrue(result.detectedCharacters.isEmpty())
        assertEquals(0, result.score)
    }

    @Test
    fun `normal domain with numbers is not homograph`() {
        val result = detector.detect("example123.com")
        assertFalse(result.isHomograph)
    }

    @Test
    fun `domain with hyphens is not homograph`() {
        val result = detector.detect("my-site-name.com")
        assertFalse(result.isHomograph)
    }

    // === SCORE CALCULATION TESTS ===

    @Test
    fun `multiple homograph chars increase score`() {
        val singleResult = detector.detect("pаypal.com")  // 1 Cyrillic char
        val multiResult = detector.detect("pаypаl.com")  // 2 Cyrillic chars
        
        assertTrue(multiResult.score >= singleResult.score)
    }

    @Test
    fun `score is capped at 50`() {
        // Create domain with many homograph characters
        val result = detector.detect("xn--pуpаl-4vе.com")
        assertTrue(result.score <= 50)
    }

    @Test
    fun `punycode adds 20 to score`() {
        val result = detector.detect("xn--example-cua.com")
        assertTrue(result.score >= 20)
    }

    // === DETECTED CHARACTER INFO ===

    @Test
    fun `detected characters have position info`() {
        val result = detector.detect("pаypal.com")  // 'а' at position 1
        
        assertTrue(result.detectedCharacters.isNotEmpty())
        val detected = result.detectedCharacters.first()
        assertTrue(detected.position >= 0)
    }

    @Test
    fun `detected characters have unicode name`() {
        val result = detector.detect("gооgle.com")  // Cyrillic 'о'
        
        val detected = result.detectedCharacters.first()
        assertTrue(detected.unicodeName.isNotEmpty())
        assertTrue(detected.unicodeName.contains("Cyrillic", ignoreCase = true) || 
                   detected.unicodeName.contains("Unicode", ignoreCase = true))
    }

    @Test
    fun `detected characters have lookalike info`() {
        val result = detector.detect("pаypal.com")  
        
        val detected = result.detectedCharacters.first()
        assertEquals('a', detected.lookalike)
    }

    // === MIXED SCRIPT DETECTION ===

    @Test
    fun `detects mixed Cyrillic and Greek`() {
        val result = detector.detect("pаyρal.com")  // Cyrillic 'а' + Greek 'ρ'
        assertTrue(result.isHomograph)
        assertTrue(result.detectedCharacters.isNotEmpty())
    }

    // === HOMOGRAPH MAP COVERAGE ===

    @Test
    fun `homograph map contains Cyrillic letters`() {
        val map = HomographDetector.HOMOGRAPH_MAP
        assertTrue(map.containsKey('а'))  // Cyrillic a
        assertTrue(map.containsKey('е'))  // Cyrillic e
        assertTrue(map.containsKey('о'))  // Cyrillic o
    }

    @Test
    fun `homograph map contains Greek letters`() {
        val map = HomographDetector.HOMOGRAPH_MAP
        assertTrue(map.containsKey('ο'))  // Greek omicron
        assertTrue(map.containsKey('α'))  // Greek alpha
    }

    @Test
    fun `homograph map has correct lookalikes`() {
        val map = HomographDetector.HOMOGRAPH_MAP
        assertEquals('a', map['а'])  // Cyrillic a -> Latin a
        assertEquals('o', map['о'])  // Cyrillic o -> Latin o
    }
}
