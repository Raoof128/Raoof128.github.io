/*
 * Copyright 2025-2026 Mehr Guard Contributors
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

package com.raouf.mehrguard.privacy

import com.raouf.mehrguard.ml.FeatureExtractor
import com.raouf.mehrguard.model.Verdict

/**
 * Feedback Manager for the Ghost Protocol.
 *
 * Handles user feedback when detection results are incorrect,
 * using privacy-preserving techniques to improve the model.
 *
 * ## Workflow
 *
 * 1. User scans URL → Engine returns SAFE verdict
 * 2. User knows it's phishing → Taps "Report as Phishing"
 * 3. FeedbackManager extracts features (locally)
 * 4. PrivacyPreservingAnalytics encrypts gradient
 * 5. Encrypted report queued for batch transmission
 * 6. Server aggregates thousands of reports → Model improves
 * 7. User's URL is NEVER transmitted
 *
 * @author Mehr Guard Security Team
 * @since 1.3.0
 */
class FeedbackManager(
    private val analytics: PrivacyPreservingAnalytics = PrivacyPreservingAnalytics()
) {
    private val featureExtractor = FeatureExtractor()
    
    /**
     * Queue of encrypted reports waiting for batch transmission.
     */
    private val reportQueue = mutableListOf<PrivacyPreservingAnalytics.EncryptedGradient>()
    
    /**
     * Count of reports submitted in this session (for privacy budget tracking).
     */
    private var sessionReportCount = 0
    
    /**
     * Maximum reports per session to prevent privacy budget exhaustion.
     */
    private val maxReportsPerSession = 10
    
    /**
     * Feedback report result.
     */
    sealed class FeedbackResult {
        /** Feedback successfully processed and encrypted */
        data class Success(
            val encryptedGradient: PrivacyPreservingAnalytics.EncryptedGradient,
            val privacyBudgetUsed: Double,
            val reportsRemaining: Int
        ) : FeedbackResult()
        
        /** Report rejected due to rate limiting */
        data class RateLimited(val message: String) : FeedbackResult()
        
        /** Processing error */
        data class Error(val message: String) : FeedbackResult()
    }
    
    /**
     * Report a false negative: URL marked SAFE but is actually phishing.
     *
     * This is the dangerous case we want to catch and improve.
     *
     * @param url The URL that was incorrectly marked as safe
     * @param originalVerdict The original verdict from the engine (should be SAFE)
     * @return FeedbackResult with encrypted gradient or error
     */
    fun reportFalseNegative(
        url: String,
        originalVerdict: Verdict
    ): FeedbackResult {
        // Validate this is actually a false negative report
        if (originalVerdict != Verdict.SAFE) {
            return FeedbackResult.Error(
                "Cannot report false negative: original verdict was ${originalVerdict.name}"
            )
        }
        
        // Check rate limiting
        if (sessionReportCount >= maxReportsPerSession) {
            val budgetUsed = analytics.calculatePrivacyBudgetUsed(sessionReportCount)
            val formattedBudget = (kotlin.math.round(budgetUsed * 100) / 100).toString()
            return FeedbackResult.RateLimited(
                "Privacy budget exhausted. $sessionReportCount reports this session. " +
                "Total ε used: $formattedBudget"
            )
        }
        
        return try {
            // Step 1: Extract features locally
            val features = featureExtractor.extract(url)
            
            // Step 2: Process through privacy-preserving analytics
            val encryptedGradient = analytics.processUserFeedback(
                actualFeatures = features,
                feedbackType = PrivacyPreservingAnalytics.FeedbackType.FALSE_NEGATIVE
            )
            
            // Step 3: Queue for transmission
            reportQueue.add(encryptedGradient)
            sessionReportCount++
            
            // Calculate privacy budget used so far
            val budgetUsed = analytics.calculatePrivacyBudgetUsed(sessionReportCount)
            
            FeedbackResult.Success(
                encryptedGradient = encryptedGradient,
                privacyBudgetUsed = budgetUsed,
                reportsRemaining = maxReportsPerSession - sessionReportCount
            )
        } catch (e: Exception) {
            FeedbackResult.Error("Failed to process feedback: ${e.message}")
        }
    }
    
    /**
     * Report a false positive: URL marked SUSPICIOUS/MALICIOUS but is actually safe.
     *
     * Less critical but still useful for reducing false alarms.
     *
     * @param url The URL that was incorrectly flagged
     * @param originalVerdict The original verdict (should be SUSPICIOUS or MALICIOUS)
     * @return FeedbackResult with encrypted gradient or error
     */
    fun reportFalsePositive(
        url: String,
        originalVerdict: Verdict
    ): FeedbackResult {
        // Validate this is actually a false positive report
        if (originalVerdict == Verdict.SAFE) {
            return FeedbackResult.Error(
                "Cannot report false positive: URL was already marked SAFE"
            )
        }
        
        // Check rate limiting
        if (sessionReportCount >= maxReportsPerSession) {
            return FeedbackResult.RateLimited(
                "Session report limit reached ($maxReportsPerSession)"
            )
        }
        
        return try {
            val features = featureExtractor.extract(url)
            
            val encryptedGradient = analytics.processUserFeedback(
                actualFeatures = features,
                feedbackType = PrivacyPreservingAnalytics.FeedbackType.FALSE_POSITIVE
            )
            
            reportQueue.add(encryptedGradient)
            sessionReportCount++
            
            val budgetUsed = analytics.calculatePrivacyBudgetUsed(sessionReportCount)
            
            FeedbackResult.Success(
                encryptedGradient = encryptedGradient,
                privacyBudgetUsed = budgetUsed,
                reportsRemaining = maxReportsPerSession - sessionReportCount
            )
        } catch (e: Exception) {
            FeedbackResult.Error("Failed to process feedback: ${e.message}")
        }
    }
    
    /**
     * Get the current report queue for batch transmission.
     *
     * In a production system, this would be sent to a secure aggregation server.
     */
    fun getReportQueue(): List<PrivacyPreservingAnalytics.EncryptedGradient> {
        return reportQueue.toList()
    }
    
    /**
     * Clear the report queue after successful transmission.
     */
    fun clearReportQueue() {
        reportQueue.clear()
    }
    
    /**
     * Get current session statistics.
     */
    fun getSessionStats(): SessionStats {
        return SessionStats(
            reportsSubmitted = sessionReportCount,
            reportsRemaining = maxReportsPerSession - sessionReportCount,
            privacyBudgetUsed = analytics.calculatePrivacyBudgetUsed(sessionReportCount),
            queueSize = reportQueue.size
        )
    }
    
    /**
     * Session statistics.
     */
    data class SessionStats(
        val reportsSubmitted: Int,
        val reportsRemaining: Int,
        val privacyBudgetUsed: Double,
        val queueSize: Int
    )
    
    /**
     * Reset session (e.g., on app restart).
     */
    fun resetSession() {
        sessionReportCount = 0
        reportQueue.clear()
    }
    
    companion object {
        /**
         * Explanation for users about what happens when they report feedback.
         */
        val USER_EXPLANATION = """
            |## What happens when you report a mistake?
            |
            |When you tap "Report as Phishing" or "Actually Safe":
            |
            |1. We extract numeric features from the URL (locally on your device)
            |2. We compute the difference between what we expected and what we saw
            |3. We add mathematical noise to hide your specific data
            |4. We encrypt the noised result with a one-time key
            |5. The encrypted report is queued for batch submission
            |
            |**What we NEVER see:**
            |• The actual URL you visited
            |• Your identity or device ID
            |• When exactly you made the report
            |
            |**What we CAN learn (only in aggregate):**
            |• That our detection is missing some pattern
            |• What type of features need recalibration
            |
            |This uses the same privacy technology as Apple's differential 
            |privacy in iOS and Google's RAPPOR system.
        """.trimMargin()
    }
}
