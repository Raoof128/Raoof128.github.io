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

package com.qrshield.security

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

/**
 * Comprehensive tests for RateLimiter and Throttler.
 */
class RateLimiterTest {

    // === BASIC RATE LIMITING ===

    @Test
    fun `first request is allowed`() {
        val limiter = RateLimiter(maxRequests = 5, windowMs = 60000)
        assertTrue(limiter.isAllowed())
    }

    @Test
    fun `requests within limit are allowed`() {
        val limiter = RateLimiter(maxRequests = 5, windowMs = 60000)
        repeat(5) {
            assertTrue(limiter.tryAcquire().allowed, "Request $it should be allowed")
        }
    }

    @Test
    fun `exceeding limit blocks requests`() {
        val limiter = RateLimiter(maxRequests = 3, windowMs = 60000)

        repeat(3) { limiter.tryAcquire() }

        val result = limiter.tryAcquire()
        assertFalse(result.allowed)
    }

    // === TRY ACQUIRE TESTS ===

    @Test
    fun `tryAcquire returns correct remaining count`() {
        val limiter = RateLimiter(maxRequests = 5, windowMs = 60000)

        val result1 = limiter.tryAcquire()
        assertEquals(4, result1.remaining)

        val result2 = limiter.tryAcquire()
        assertEquals(3, result2.remaining)
    }

    @Test
    fun `tryAcquire returns reset time when blocked`() {
        val limiter = RateLimiter(maxRequests = 2, windowMs = 60000)

        limiter.tryAcquire()
        limiter.tryAcquire()

        val result = limiter.tryAcquire()
        assertFalse(result.allowed)
        assertTrue(result.resetMs > 0)
    }

    // === GET STATUS TESTS ===

    @Test
    fun `getStatus shows current count`() {
        val limiter = RateLimiter(maxRequests = 10, windowMs = 60000)

        limiter.tryAcquire()
        limiter.tryAcquire()
        limiter.tryAcquire()

        val status = limiter.getStatus()
        assertEquals(3, status.currentCount)
        assertEquals(7, status.remaining)
    }

    @Test
    fun `getStatus shows correct limit`() {
        val limiter = RateLimiter(maxRequests = 100, windowMs = 60000)

        val status = limiter.getStatus()
        assertEquals(100, status.limit)
    }

    // === RESET TESTS ===

    @Test
    fun `reset clears all requests`() {
        val limiter = RateLimiter(maxRequests = 3, windowMs = 60000)

        repeat(3) { limiter.tryAcquire() }
        assertFalse(limiter.isAllowed())

        limiter.reset()

        assertTrue(limiter.isAllowed())
    }

    // === FACTORY METHODS ===

    @Test
    fun `forUi creates limiter with 60 requests per minute`() {
        val limiter = RateLimiter.forUi()
        val status = limiter.getStatus()
        assertEquals(60, status.limit)
    }

    @Test
    fun `forBatch creates limiter with 100 requests per minute`() {
        val limiter = RateLimiter.forBatch()
        val status = limiter.getStatus()
        assertEquals(100, status.limit)
    }

    @Test
    fun `forApi creates limiter with 30 requests per minute`() {
        val limiter = RateLimiter.forApi()
        val status = limiter.getStatus()
        assertEquals(30, status.limit)
    }

    @Test
    fun `forSensitive creates strict limiter`() {
        val limiter = RateLimiter.forSensitive()
        val status = limiter.getStatus()
        assertEquals(5, status.limit)
    }

    // === RATE LIMIT RESULT TESTS ===

    @Test
    fun `result contains all expected fields`() {
        val limiter = RateLimiter(maxRequests = 10, windowMs = 60000)
        val result = limiter.tryAcquire()

        assertNotNull(result.allowed)
        assertNotNull(result.remaining)
        assertNotNull(result.resetMs)
        assertNotNull(result.limit)
    }

    @Test
    fun `result has message`() {
        val limiter = RateLimiter(maxRequests = 10, windowMs = 60000)
        val result = limiter.tryAcquire()
        assertTrue(result.message.isNotEmpty())
    }

    // === EDGE CASES ===

    @Test
    fun `single request limit works`() {
        val limiter = RateLimiter(maxRequests = 1, windowMs = 60000)

        assertTrue(limiter.tryAcquire().allowed)
        assertFalse(limiter.tryAcquire().allowed)
    }
}

/**
 * Comprehensive tests for Throttler.
 */
class ThrottlerTest {

    // === BASIC THROTTLING ===

    @Test
    fun `first request is allowed`() {
        val throttler = Throttler(minDelayMs = 100)
        assertTrue(throttler.isAllowed())
    }

    @Test
    fun `immediate second request is blocked`() {
        val throttler = Throttler(minDelayMs = 1000)

        throttler.tryAcquire()

        assertFalse(throttler.isAllowed())
    }

    // === TRY ACQUIRE TESTS ===

    @Test
    fun `tryAcquire returns allowed for first request`() {
        val throttler = Throttler(minDelayMs = 100)
        val result = throttler.tryAcquire()
        assertTrue(result.allowed)
    }

    @Test
    fun `tryAcquire returns wait time when blocked`() {
        val throttler = Throttler(minDelayMs = 1000)

        throttler.tryAcquire()
        val result = throttler.tryAcquire()

        assertFalse(result.allowed)
        assertTrue(result.waitMs > 0)
    }

    // === RECORD TESTS ===

    @Test
    fun `record updates last operation time`() {
        val throttler = Throttler(minDelayMs = 100)
        throttler.record()

        assertFalse(throttler.isAllowed())
    }

    // === FACTORY METHODS ===

    @Test
    fun `forScans creates throttler`() {
        val throttler = Throttler.forScans()
        assertNotNull(throttler)
    }

    @Test
    fun `forBatch creates throttler`() {
        val throttler = Throttler.forBatch()
        assertNotNull(throttler)
    }

    // === THROTTLE RESULT TESTS ===

    @Test
    fun `result contains allowed flag`() {
        val throttler = Throttler(minDelayMs = 100)
        val result = throttler.tryAcquire()
        assertNotNull(result.allowed)
    }

    @Test
    fun `result contains wait time`() {
        val throttler = Throttler(minDelayMs = 100)
        throttler.tryAcquire()
        val result = throttler.tryAcquire()
        assertNotNull(result.waitMs)
    }
}
