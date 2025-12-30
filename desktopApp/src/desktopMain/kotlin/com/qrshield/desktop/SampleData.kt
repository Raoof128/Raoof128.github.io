package com.qrshield.desktop

data class UserProfile(
    val name: String,
    val role: String,
    val initials: String
)

/**
 * Demo input for testing the scan flow.
 * Used by judges to verify the Desktop app processes URLs correctly.
 */
data class JudgeDemoInput(
    val label: String,
    val input: String,
    val expectedVerdict: String,
    val description: String
)

object SampleData {
    val userProfile = UserProfile(
        name = "Security Analyst",
        role = "Offline Operations",
        initials = "SA"
    )
    const val accountPlan = "Enterprise Plan"

    /**
     * Judge Demo Input Pack - 3 sample inputs for competition judges.
     * 
     * These inputs can be used to verify the Desktop scan flow works correctly:
     * 1. Paste URL in Dashboard → Analyze button
     * 2. Paste URL in LiveScan → Paste URL action
     * 3. Import QR image containing one of these URLs
     * 
     * All inputs work OFFLINE - no network dependencies.
     */
    val judgeDemoInputs = listOf(
        JudgeDemoInput(
            label = "Safe URL (Benign)",
            input = "https://www.google.com/search?q=kotlin+multiplatform",
            expectedVerdict = "SAFE",
            description = "A legitimate Google search URL. Expected: green SAFE verdict with high confidence."
        ),
        JudgeDemoInput(
            label = "Phishing URL (Malicious)",
            input = "https://secure-bankofamerica-login.tk/verify?token=abc123&redirect=http://evil.com",
            expectedVerdict = "MALICIOUS",
            description = "Obvious phishing pattern: brand impersonation (bankofamerica), suspicious TLD (.tk), " +
                "redirect to external domain, token parameter. Expected: red DANGEROUS verdict."
        ),
        JudgeDemoInput(
            label = "QR Payload Text (Suspicious)",
            input = "https://bit.ly/3xYz123",
            expectedVerdict = "SUSPICIOUS",
            description = "URL shortener (bit.ly) hides the actual destination. Expected: yellow SUSPICIOUS verdict."
        )
    )
}
