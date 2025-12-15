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

package com.qrshield.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Tests for DetectionConfig.
 */
class DetectionConfigTest {

    @Test
    fun `default config has valid values`() {
        val config = DetectionConfig.DEFAULT

        assertEquals(10, config.safeThreshold)
        assertEquals(50, config.suspiciousThreshold)
        assertEquals(100, config.heuristicWeight + config.mlWeight + config.brandWeight + config.tldWeight)
    }

    @Test
    fun `strict config is stricter than default`() {
        val strict = DetectionConfig.STRICT
        val default = DetectionConfig.DEFAULT

        assertTrue(strict.safeThreshold < default.safeThreshold)
        assertTrue(strict.suspiciousThreshold < default.suspiciousThreshold)
    }

    @Test
    fun `lenient config is more lenient than default`() {
        val lenient = DetectionConfig.LENIENT
        val default = DetectionConfig.DEFAULT

        assertTrue(lenient.safeThreshold > default.safeThreshold)
        assertTrue(lenient.suspiciousThreshold > default.suspiciousThreshold)
    }

    @Test
    fun `weights must sum to 100`() {
        assertFailsWith<IllegalArgumentException> {
            DetectionConfig(
                heuristicWeight = 50,
                mlWeight = 50,
                brandWeight = 50,
                tldWeight = 50
            )
        }
    }

    @Test
    fun `safe threshold must be less than suspicious`() {
        assertFailsWith<IllegalArgumentException> {
            DetectionConfig(
                safeThreshold = 60,
                suspiciousThreshold = 50
            )
        }
    }

    @Test
    fun `can parse from JSON`() {
        val json = """
            {
              "safeThreshold": 15,
              "suspiciousThreshold": 60,
              "heuristicWeight": 40,
              "mlWeight": 30,
              "brandWeight": 20,
              "tldWeight": 10
            }
        """

        val config = DetectionConfig.fromJson(json)

        assertEquals(15, config.safeThreshold)
        assertEquals(60, config.suspiciousThreshold)
        assertEquals(40, config.heuristicWeight)
    }

    @Test
    fun `can export to JSON`() {
        val config = DetectionConfig.DEFAULT
        val json = config.toJson()

        assertTrue(json.contains("\"safeThreshold\": 10"))
        assertTrue(json.contains("\"suspiciousThreshold\": 50"))
    }

    @Test
    fun `performance config disables ML`() {
        val perf = DetectionConfig.PERFORMANCE

        assertEquals(false, perf.enableMl)
        assertEquals(false, perf.enableCounterfactuals)
    }
}
