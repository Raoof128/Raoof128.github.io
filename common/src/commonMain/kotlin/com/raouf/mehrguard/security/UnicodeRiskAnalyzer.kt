/*
 * Copyright 2025-2026 QR-SHIELD Contributors
 * Licensed under the Apache License, Version 2.0
 */

package com.raouf.mehrguard.security

import com.raouf.mehrguard.model.ReasonCode

/**
 * Unicode Risk Analyzer - Detects homograph/IDN attacks.
 */
class UnicodeRiskAnalyzer {

    data class UnicodeRiskResult(
        val hasRisk: Boolean,
        val isPunycode: Boolean,
        val hasMixedScript: Boolean,
        val hasConfusables: Boolean,
        val hasZeroWidth: Boolean,
        val reasons: List<ReasonCode>,
        val riskScore: Int,
        val skeleton: String,
        val scripts: Set<UnicodeScript>
    ) {
        companion object {
            val SAFE = UnicodeRiskResult(false, false, false, false, false, emptyList(), 0, "", emptySet())
        }
    }

    enum class UnicodeScript { LATIN, CYRILLIC, GREEK, ARABIC, HEBREW, CJK, COMMON, OTHER }

    fun analyze(host: String): UnicodeRiskResult {
        if (host.isEmpty() || host.length > 255) return UnicodeRiskResult.SAFE

        val reasons = mutableListOf<ReasonCode>()
        var score = 0

        val isPunycode = host.contains("xn--")
        if (isPunycode) { reasons.add(ReasonCode.REASON_HOMOGRAPH); score += 30 }

        val hasZeroWidth = host.any { it in ZERO_WIDTH_CHARS }
        if (hasZeroWidth) { reasons.add(ReasonCode.REASON_ZERO_WIDTH_CHARS); score += 50 }

        val scripts = detectScripts(host)
        val hasMixedScript = scripts.size > 1 && UnicodeScript.LATIN in scripts && 
            (UnicodeScript.CYRILLIC in scripts || UnicodeScript.GREEK in scripts)
        if (hasMixedScript) { reasons.add(ReasonCode.REASON_MIXED_SCRIPT); score += 45 }

        val hasConfusables = host.any { it in CONFUSABLES.keys }
        if (hasConfusables) { reasons.add(ReasonCode.REASON_LOOKALIKE_CHARS); score += 35 }

        return UnicodeRiskResult(reasons.isNotEmpty(), isPunycode, hasMixedScript, 
            hasConfusables, hasZeroWidth, reasons, score.coerceAtMost(100), toSkeleton(host), scripts)
    }

    fun getSafeDisplayHost(host: String): String = when {
        host.isEmpty() -> host
        host.contains("xn--") -> "[IDN: $host]"
        host.all { it.code < 128 && it !in ZERO_WIDTH_CHARS } -> host
        host.any { it.code >= 128 } -> "⚠️$host"
        else -> host.filterNot { it in ZERO_WIDTH_CHARS }
    }

    fun toSkeleton(host: String): String = host.map { CONFUSABLES[it] ?: it }
        .filterNot { it in ZERO_WIDTH_CHARS }.joinToString("").lowercase()

    fun areConfusable(h1: String, h2: String) = toSkeleton(h1) == toSkeleton(h2) && h1 != h2

    private fun detectScripts(host: String): Set<UnicodeScript> = host.mapNotNull { getScript(it) }
        .filter { it != UnicodeScript.COMMON }.toSet()

    private fun getScript(c: Char) = when (c.code) {
        in 0x0041..0x005A, in 0x0061..0x007A, in 0x00C0..0x024F -> UnicodeScript.LATIN
        in 0x0400..0x052F -> UnicodeScript.CYRILLIC
        in 0x0370..0x03FF -> UnicodeScript.GREEK
        in 0x0590..0x05FF -> UnicodeScript.HEBREW
        in 0x0600..0x077F -> UnicodeScript.ARABIC
        in 0x4E00..0x9FFF, in 0x3040..0x30FF, in 0xAC00..0xD7AF -> UnicodeScript.CJK
        in 0x0030..0x0039, 0x002D, 0x002E -> UnicodeScript.COMMON
        else -> UnicodeScript.OTHER
    }

    companion object {
        val default = UnicodeRiskAnalyzer()

        val ZERO_WIDTH_CHARS = setOf('\u200B', '\u200C', '\u200D', '\uFEFF', '\u2060', '\u180E', '\u00AD')

        val CONFUSABLES = mapOf(
            'а' to 'a', 'е' to 'e', 'о' to 'o', 'р' to 'p', 'с' to 'c', 'х' to 'x', 'у' to 'y',
            'і' to 'i', 'ј' to 'j', 'ѕ' to 's', 'ν' to 'v', 'Α' to 'A', 'Β' to 'B', 'Ε' to 'E',
            'Ζ' to 'Z', 'Η' to 'H', 'Ι' to 'I', 'Κ' to 'K', 'Μ' to 'M', 'Ν' to 'N', 'Ο' to 'O',
            'Ρ' to 'P', 'Τ' to 'T', 'Υ' to 'Y', 'Χ' to 'X', 'ℂ' to 'C', 'ℍ' to 'H', 'ℕ' to 'N',
            'ℙ' to 'P', 'ℚ' to 'Q', 'ℝ' to 'R', 'ℤ' to 'Z', 'ℐ' to 'I', 'ℒ' to 'L', 'ℳ' to 'M',
            '!' to 'l', '|' to 'l', 'ı' to 'i', 'ɡ' to 'g', 'ɑ' to 'a'
        )
    }
}
