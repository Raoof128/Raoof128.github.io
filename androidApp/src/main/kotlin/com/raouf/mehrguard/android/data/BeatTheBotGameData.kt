package com.raouf.mehrguard.android.data

import com.raouf.mehrguard.android.ui.viewmodels.GameUrl

object BeatTheBotGameData {
    val levels = listOf(
        GameUrl(
            url = "http://secure-login-apple.id.support.com",
            isPhishing = true,
            context = "Urgent: Your account has been locked. Verify identity now.",
            sender = "Apple Support",
            signals = listOf("SUBDOMAIN_ABUSE", "URGENCY", "HTTP_ONLY")
        ),
        GameUrl(
            url = "https://www.paypal.com/signin",
            isPhishing = false,
            context = "Receipt for your payment to Spotify.",
            sender = "PayPal",
            signals = emptyList()
        ),
        GameUrl(
            url = "http://netflix-cancel-membership.net",
            isPhishing = true,
            context = "We couldn't process your payment. Update details to avoid suspension.",
            sender = "Netflix",
            signals = listOf("TLD_ABUSE", "BRAND_IMPERSONATION")
        ),
        GameUrl(
            url = "https://accounts.google.com",
            isPhishing = false,
            context = "New sign-in from iPhone 15 detected.",
            sender = "Google",
            signals = emptyList()
        ),
        GameUrl(
            url = "https://amazon-order-status.xy-update.com",
            isPhishing = true,
            context = "Problem with your delivery. Schedule redelivery.",
            sender = "Amazon Logistics",
            signals = listOf("HOMOGRAPH_ATTACK", "SUSPICIOUS_DOMAIN")
        ),
        GameUrl(
            url = "http://verify-bank-of-america.com.login.php",
            isPhishing = true,
            context = "Unauthorized access detected. Login to secure your funds.",
            sender = "BankOfAmerica",
            signals = listOf("EXTENSION_MASKING", "HTTP_ONLY", "BRAND_IMPERSONATION")
        ),
        GameUrl(
            url = "https://github.com/login",
            isPhishing = false,
            context = "Please verify your device via 2FA.",
            sender = "GitHub",
            signals = emptyList()
        ),
        GameUrl(
            url = "https://microsoft-security-alert.ml",
            isPhishing = true,
            context = "Critical security alert. Action required immediately.",
            sender = "MS Security",
            signals = listOf("TLD_ABUSE", "URGENCY")
        ),
        GameUrl(
            url = "https://www.chase.com",
            isPhishing = false,
            context = "Your monthly statement is ready to view.",
            sender = "Chase Bank",
            signals = emptyList()
        ),
        GameUrl(
            url = "http://192.168.1.1/login",
            isPhishing = true,
            context = "Router firmware update required.",
            sender = "Admin",
            signals = listOf("IP_ADDRESS", "HTTP_ONLY")
        )
    )
}
