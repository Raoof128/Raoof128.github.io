package com.qrshield.desktop

data class UserProfile(
    val name: String,
    val role: String,
    val initials: String
)

object SampleData {
    val userProfile = UserProfile(
        name = "Security Analyst",
        role = "Offline Operations",
        initials = "SA"
    )
    const val accountPlan = "Enterprise Plan"
}
