package com.qrshield.android.data

import com.qrshield.android.ui.viewmodels.GameUrl

object BeatTheBotGameData {
    val levels = listOf(
        GameUrl(
            url = "http://secure-login-apple.id.support.com",
            isPhishing = true,
            context = "Urgent: Your account has been locked. Verify identity now.",
            sender = "Apple Support"
        ),
        GameUrl(
            url = "https://www.paypal.com/signin",
            isPhishing = false,
            context = "Receipt for your payment to Spotify.",
            sender = "PayPal"
        ),
        GameUrl(
            url = "http://netflix-cancel-membership.net",
            isPhishing = true,
            context = "We couldn't process your payment. Update details to avoid suspension.",
            sender = "Netflix"
        ),
        GameUrl(
            url = "https://accounts.google.com",
            isPhishing = false,
            context = "New sign-in from iPhone 15 detected.",
            sender = "Google"
        ),
        GameUrl(
            url = "https://amazon-order-status.xy-update.com",
            isPhishing = true,
            context = "Problem with your delivery. Schedule redelivery.",
            sender = "Amazon Logistics"
        )
    )
}
