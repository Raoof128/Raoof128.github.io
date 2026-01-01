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

package com.raouf.mehrguard.gamification

/**
 * Mock Phishing Challenge Dataset
 *
 * Contains curated examples of phishing URLs for the "Beat the Bot" game.
 * These URLs demonstrate various phishing techniques and are used to:
 * 1. Educate users about phishing patterns
 * 2. Test the detection engine's accuracy
 * 3. Provide progressive difficulty in the game
 *
 * ## Security Note
 * All URLs are synthetic and do not resolve to real malicious sites.
 * They are designed to trigger specific detection heuristics.
 *
 * @author Mehr Guard Security Team
 * @since 1.4.0
 */
object PhishingChallengeDataset {

    /**
     * Difficulty levels for the game.
     * Determines bot speed, accuracy, and URL complexity.
     */
    enum class Difficulty(
        val displayName: String,
        val botReactionDelayMs: Long,
        val botAccuracyPercent: Int,
        val scoreMultiplier: Float
    ) {
        BEGINNER("Beginner", botReactionDelayMs = 2000L, botAccuracyPercent = 70, scoreMultiplier = 1.0f),
        INTERMEDIATE("Intermediate", botReactionDelayMs = 1500L, botAccuracyPercent = 80, scoreMultiplier = 1.5f),
        ADVANCED("Advanced", botReactionDelayMs = 1000L, botAccuracyPercent = 90, scoreMultiplier = 2.0f),
        EXPERT("Expert", botReactionDelayMs = 500L, botAccuracyPercent = 95, scoreMultiplier = 3.0f),
        NIGHTMARE("Nightmare", botReactionDelayMs = 250L, botAccuracyPercent = 99, scoreMultiplier = 5.0f)
    }

    /**
     * A phishing challenge with metadata for game progression.
     */
    data class PhishingChallenge(
        val id: String,
        val url: String,
        val difficulty: Difficulty,
        val technique: PhishingTechnique,
        val expectedDetection: Boolean,
        val educationalHint: String,
        val detectionSignals: List<String>
    )

    /**
     * Phishing techniques demonstrated by challenges.
     */
    enum class PhishingTechnique(val displayName: String, val description: String) {
        TYPOSQUATTING("Typosquatting", "Misspelled brand names (e.g., paypa1 for paypal)"),
        HOMOGRAPH("Homograph Attack", "Using lookalike Unicode characters"),
        SUBDOMAIN_ABUSE("Subdomain Abuse", "Placing brand names in subdomains of malicious domains"),
        TLD_ABUSE("TLD Abuse", "Using suspicious free TLDs (.tk, .ml, .ga)"),
        IP_ADDRESS("IP Address", "Using raw IP addresses instead of domains"),
        URL_SHORTENER("URL Shortener", "Hiding destinations behind URL shorteners"),
        PATH_KEYWORDS("Suspicious Keywords", "Using credential/login keywords in paths"),
        CREDENTIAL_PARAMS("Credential Params", "Passing credentials in query parameters"),
        LONG_URL("URL Obfuscation", "Overly long URLs to hide intent"),
        PUNYCODE("Punycode Attack", "Using internationalized domain names"),
        BRAND_IMPERSONATION("Brand Impersonation", "Combining multiple techniques")
    }

    /**
     * Curated phishing challenges organized by difficulty.
     * Each challenge is designed to test specific detection capabilities.
     */
    val challenges: List<PhishingChallenge> = listOf(
        // === BEGINNER CHALLENGES ===
        PhishingChallenge(
            id = "beginner_1",
            url = "https://paypa1-secure.tk/login",
            difficulty = Difficulty.BEGINNER,
            technique = PhishingTechnique.TYPOSQUATTING,
            expectedDetection = true,
            educationalHint = "The number '1' replaces 'l' in 'paypal', and .tk is a high-risk TLD.",
            detectionSignals = listOf("Typosquat: paypal", "High-risk TLD: .tk", "Suspicious keyword: login")
        ),
        PhishingChallenge(
            id = "beginner_2",
            url = "http://192.168.1.1/admin/login.php",
            difficulty = Difficulty.BEGINNER,
            technique = PhishingTechnique.IP_ADDRESS,
            expectedDetection = true,
            educationalHint = "Legitimate services use domain names, not IP addresses.",
            detectionSignals = listOf("IP address host", "Insecure HTTP", "Suspicious extension: .php")
        ),
        PhishingChallenge(
            id = "beginner_3",
            url = "https://bit.ly/secure-login-verify",
            difficulty = Difficulty.BEGINNER,
            technique = PhishingTechnique.URL_SHORTENER,
            expectedDetection = true,
            educationalHint = "URL shorteners hide the true destination. Never trust them blindly.",
            detectionSignals = listOf("URL shortener detected", "Destination hidden")
        ),
        PhishingChallenge(
            id = "beginner_4",
            url = "https://secure-login-verify.ml/account",
            difficulty = Difficulty.BEGINNER,
            technique = PhishingTechnique.TLD_ABUSE,
            expectedDetection = true,
            educationalHint = "Free TLDs like .ml are commonly used by phishers.",
            detectionSignals = listOf("High-risk TLD: .ml", "Credential keywords in domain")
        ),

        // === INTERMEDIATE CHALLENGES ===
        PhishingChallenge(
            id = "intermediate_1",
            url = "https://account-verify.amazon.suspicious-domain.com/update",
            difficulty = Difficulty.INTERMEDIATE,
            technique = PhishingTechnique.SUBDOMAIN_ABUSE,
            expectedDetection = true,
            educationalHint = "The brand 'amazon' is in a subdomain, but the real domain is 'suspicious-domain.com'.",
            detectionSignals = listOf("Brand in subdomain", "Excessive subdomains", "Credential keyword: account")
        ),
        PhishingChallenge(
            id = "intermediate_2",
            url = "https://www.аpple.com/support",
            difficulty = Difficulty.INTERMEDIATE,
            technique = PhishingTechnique.HOMOGRAPH,
            expectedDetection = true,
            educationalHint = "The 'а' is a Cyrillic character that looks identical to Latin 'a'.",
            detectionSignals = listOf("Homograph attack: Cyrillic 'а'", "Unicode lookalike characters")
        ),
        PhishingChallenge(
            id = "intermediate_3",
            url = "https://login.microsoft-verify-account.com/signin?password=reset&token=abc123",
            difficulty = Difficulty.INTERMEDIATE,
            technique = PhishingTechnique.CREDENTIAL_PARAMS,
            expectedDetection = true,
            educationalHint = "Legitimate sites never pass credentials in URL query parameters.",
            detectionSignals = listOf("Brand impersonation: microsoft", "Credential params: password", "Suspicious domain structure")
        ),

        // === ADVANCED CHALLENGES ===
        PhishingChallenge(
            id = "advanced_1",
            url = "https://secure.banking-portal.net/auth/verify?session=x7k2mq9p&redirect=https://evil.com",
            difficulty = Difficulty.ADVANCED,
            technique = PhishingTechnique.BRAND_IMPERSONATION,
            expectedDetection = true,
            educationalHint = "Open redirects can be exploited to send users to malicious sites.",
            detectionSignals = listOf("Generic banking keywords", "Open redirect parameter", "Suspicious session token")
        ),
        PhishingChallenge(
            id = "advanced_2",
            url = "https://netflix.account-recovery-service.com/billing/update?ref=" + "x".repeat(100),
            difficulty = Difficulty.ADVANCED,
            technique = PhishingTechnique.LONG_URL,
            expectedDetection = true,
            educationalHint = "Long URLs with random strings are used to hide malicious intent.",
            detectionSignals = listOf("Brand impersonation: netflix", "Excessively long URL", "Billing keyword")
        ),

        // === EXPERT CHALLENGES (Edge cases that should NOT be detected) ===
        PhishingChallenge(
            id = "expert_safe_1",
            url = "https://www.google.com",
            difficulty = Difficulty.EXPERT,
            technique = PhishingTechnique.BRAND_IMPERSONATION, // Not actually impersonating
            expectedDetection = false,
            educationalHint = "A clean, legitimate URL from a well-known brand.",
            detectionSignals = emptyList()
        ),
        PhishingChallenge(
            id = "expert_safe_2",
            url = "https://github.com/user/repository",
            difficulty = Difficulty.EXPERT,
            technique = PhishingTechnique.SUBDOMAIN_ABUSE, // Not actually abusing
            expectedDetection = false,
            educationalHint = "Standard GitHub repository URL with user path.",
            detectionSignals = emptyList()
        ),
        PhishingChallenge(
            id = "expert_edge_1",
            url = "https://login.microsoftonline.com/common/oauth2/authorize",
            difficulty = Difficulty.EXPERT,
            technique = PhishingTechnique.CREDENTIAL_PARAMS,
            expectedDetection = false, // Real Microsoft OAuth URL
            educationalHint = "This is a legitimate Microsoft OAuth endpoint.",
            detectionSignals = emptyList()
        ),

        // === NIGHTMARE CHALLENGES (Very subtle) ===
        PhishingChallenge(
            id = "nightmare_1",
            url = "https://secure-paypal-verification.com/confirm?id=user@email.com",
            difficulty = Difficulty.NIGHTMARE,
            technique = PhishingTechnique.BRAND_IMPERSONATION,
            expectedDetection = true,
            educationalHint = "PayPal is in the domain but this is not paypal.com.",
            detectionSignals = listOf("Brand in non-official domain", "Email in query parameter")
        )
    )

    /**
     * Get challenges for a specific difficulty level.
     */
    fun getChallengesForDifficulty(difficulty: Difficulty): List<PhishingChallenge> =
        challenges.filter { it.difficulty == difficulty }

    /**
     * Get a random challenge for the given difficulty.
     */
    fun getRandomChallenge(difficulty: Difficulty): PhishingChallenge =
        getChallengesForDifficulty(difficulty).random()

    /**
     * Get all unique techniques used in challenges.
     */
    fun getTechniques(): List<PhishingTechnique> =
        challenges.map { it.technique }.distinct()

    /**
     * Calculate appropriate difficulty based on score.
     * Uses deterministic scaling, not random.
     */
    fun calculateDifficulty(score: Int, streak: Int): Difficulty {
        val effectiveScore = score + (streak * 10) // Streak bonus

        return when {
            effectiveScore >= 300 -> Difficulty.NIGHTMARE
            effectiveScore >= 200 -> Difficulty.EXPERT
            effectiveScore >= 100 -> Difficulty.ADVANCED
            effectiveScore >= 50 -> Difficulty.INTERMEDIATE
            else -> Difficulty.BEGINNER
        }
    }
}
