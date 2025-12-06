package com.qrshield.security

import kotlinx.datetime.Clock

/**
 * Rate Limiter for QR-SHIELD
 * 
 * Prevents abuse by limiting the number of operations per time window.
 * Uses a sliding window algorithm for smooth rate limiting.
 * 
 * SECURITY NOTES:
 * - Thread-safe using synchronized blocks
 * - Memory-bounded with automatic cleanup
 * - Configurable per use case (UI, API, batch)
 * 
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
class RateLimiter(
    private val maxRequests: Int,
    private val windowMs: Long,
    private val clock: () -> Long = { Clock.System.now().toEpochMilliseconds() }
) {
    
    /**
     * Sliding window of request timestamps.
     */
    private val requests = mutableListOf<Long>()
    
    /**
     * Lock for thread safety.
     */
    private val lock = Any()
    
    /**
     * Check if an operation is allowed within rate limits.
     * 
     * @return true if operation is allowed, false if rate limited
     */
    fun isAllowed(): Boolean = synchronized(lock) {
        val now = clock()
        cleanup(now)
        
        if (requests.size >= maxRequests) {
            return false
        }
        
        requests.add(now)
        return true
    }
    
    /**
     * Record an operation and check if allowed.
     * 
     * @return RateLimitResult with allowed status and metadata
     */
    fun tryAcquire(): RateLimitResult = synchronized(lock) {
        val now = clock()
        cleanup(now)
        
        val remaining = maxRequests - requests.size
        val resetTime = if (requests.isNotEmpty()) {
            requests.first() + windowMs
        } else {
            now + windowMs
        }
        
        if (remaining <= 0) {
            return RateLimitResult(
                allowed = false,
                remaining = 0,
                resetMs = resetTime - now,
                limit = maxRequests
            )
        }
        
        requests.add(now)
        return RateLimitResult(
            allowed = true,
            remaining = remaining - 1,
            resetMs = resetTime - now,
            limit = maxRequests
        )
    }
    
    /**
     * Get current rate limit status without consuming.
     */
    fun getStatus(): RateLimitStatus = synchronized(lock) {
        val now = clock()
        cleanup(now)
        
        return RateLimitStatus(
            currentCount = requests.size,
            limit = maxRequests,
            remaining = (maxRequests - requests.size).coerceAtLeast(0),
            windowMs = windowMs
        )
    }
    
    /**
     * Reset the rate limiter (for testing).
     */
    fun reset() = synchronized(lock) {
        requests.clear()
    }
    
    /**
     * Remove expired requests from the window.
     */
    private fun cleanup(now: Long) {
        val cutoff = now - windowMs
        requests.removeAll { it < cutoff }
    }
    
    /**
     * Rate limit check result.
     */
    data class RateLimitResult(
        val allowed: Boolean,
        val remaining: Int,
        val resetMs: Long,
        val limit: Int
    ) {
        /** Human-readable message */
        val message: String
            get() = if (allowed) {
                "Request allowed. $remaining requests remaining."
            } else {
                "Rate limit exceeded. Try again in ${resetMs / 1000} seconds."
            }
    }
    
    /**
     * Current rate limit status.
     */
    data class RateLimitStatus(
        val currentCount: Int,
        val limit: Int,
        val remaining: Int,
        val windowMs: Long
    )
    
    companion object {
        /**
         * Create limiter for UI operations (generous limits).
         * 60 scans per minute.
         */
        fun forUi(): RateLimiter = RateLimiter(
            maxRequests = 60,
            windowMs = 60_000L
        )
        
        /**
         * Create limiter for batch operations.
         * 100 operations per minute.
         */
        fun forBatch(): RateLimiter = RateLimiter(
            maxRequests = 100,
            windowMs = 60_000L
        )
        
        /**
         * Create limiter for API endpoints.
         * 30 requests per minute.
         */
        fun forApi(): RateLimiter = RateLimiter(
            maxRequests = 30,
            windowMs = 60_000L
        )
        
        /**
         * Create strict limiter for sensitive operations.
         * 5 attempts per 5 minutes.
         */
        fun forSensitive(): RateLimiter = RateLimiter(
            maxRequests = 5,
            windowMs = 300_000L
        )
    }
}

/**
 * Throttler that enforces minimum delay between operations.
 * 
 * Use when you need to ensure a minimum gap between operations
 * rather than limiting the total count.
 */
class Throttler(
    private val minDelayMs: Long,
    private val clock: () -> Long = { Clock.System.now().toEpochMilliseconds() }
) {
    
    private var lastOperationTime: Long = 0L
    private val lock = Any()
    
    /**
     * Check if enough time has passed since last operation.
     * 
     * @return true if operation is allowed
     */
    fun isAllowed(): Boolean = synchronized(lock) {
        val now = clock()
        return now - lastOperationTime >= minDelayMs
    }
    
    /**
     * Record an operation if allowed.
     * 
     * @return ThrottleResult with status
     */
    fun tryAcquire(): ThrottleResult = synchronized(lock) {
        val now = clock()
        val elapsed = now - lastOperationTime
        
        if (elapsed >= minDelayMs) {
            lastOperationTime = now
            return ThrottleResult(
                allowed = true,
                waitMs = 0L
            )
        }
        
        return ThrottleResult(
            allowed = false,
            waitMs = minDelayMs - elapsed
        )
    }
    
    /**
     * Force record operation (bypass check).
     */
    fun record() = synchronized(lock) {
        lastOperationTime = clock()
    }
    
    /**
     * Throttle result.
     */
    data class ThrottleResult(
        val allowed: Boolean,
        val waitMs: Long
    )
    
    companion object {
        /** Throttler for scan operations (1 per 100ms) */
        fun forScans(): Throttler = Throttler(100L)
        
        /** Throttler for batch operations (1 per 50ms) */
        fun forBatch(): Throttler = Throttler(50L)
    }
}
