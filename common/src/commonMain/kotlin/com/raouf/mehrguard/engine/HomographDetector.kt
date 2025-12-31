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

package com.raouf.mehrguard.engine

/**
 * Homograph Attack Detector for QR-SHIELD
 *
 * Detects IDN (Internationalized Domain Name) homograph attacks where Unicode
 * characters are used to impersonate ASCII Latin characters, creating
 * visually identical but technically different domain names.
 *
 * ## Security Rationale
 *
 * Homograph attacks exploit the visual similarity between characters from
 * different Unicode scripts. For example:
 * - Cyrillic 'а' (U+0430) looks identical to Latin 'a' (U+0061)
 * - Greek 'ο' (U+03BF) looks identical to Latin 'o' (U+006F)
 *
 * Attackers register domains like "gооgle.com" (with Cyrillic 'о') that appear
 * identical to "google.com" but resolve to completely different servers.
 *
 * ## Detection Strategy
 *
 * 1. **Script Scanning**: Check each character against known confusables map
 * 2. **Punycode Detection**: Flag domains starting with "xn--" (IDN encoding)
 * 3. **Mixed Script Detection**: Multiple non-Latin scripts is highly suspicious
 * 4. **Risk Scoring**: Score based on number and type of confusable characters
 *
 * ## Dangerous Unicode Blocks
 *
 * | Block | Range | Risk Level | Why Dangerous |
 * |-------|-------|------------|---------------|
 * | Cyrillic | U+0400-04FF | Very High | Many perfect Latin lookalikes |
 * | Greek | U+0370-03FF | High | Common lookalikes (α→a, ο→o) |
 * | Armenian | U+0530-058F | Medium | Some lookalikes |
 * | Georgian | U+10A0-10FF | Medium | Some lookalikes |
 *
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 * @see <a href="https://unicode.org/reports/tr39/">Unicode Security Mechanisms</a>
 */
class HomographDetector {

    /**
     * Result of homograph analysis on a domain.
     *
     * @property isHomograph True if any confusable characters detected
     * @property score Risk score from 0 (none) to 50 (severe)
     * @property detectedCharacters List of specific confusable chars found
     * @property punycode Original punycode representation if detected
     */
    data class HomographResult(
        val isHomograph: Boolean,
        val score: Int,
        val detectedCharacters: List<DetectedChar>,
        val punycode: String?
    )

    /**
     * Information about a detected confusable character.
     *
     * @property char The Unicode character found
     * @property position Index position in the domain string
     * @property unicodeName Human-readable script name (e.g., "Cyrillic")
     * @property lookalike The ASCII character it impersonates
     */
    data class DetectedChar(
        val char: Char,
        val position: Int,
        val unicodeName: String,
        val lookalike: Char
    )

    /**
     * Detect homograph attacks in a domain name.
     *
     * Scans all characters in the domain for known confusables and calculates
     * a risk score based on the number and type of matches.
     *
     * ## Scoring Logic
     * - Each confusable character: +15 points
     * - Punycode domain (xn-- prefix): +20 points
     * - Multiple different scripts: +10 points
     * - Maximum capped at 50 points
     *
     * @param domain The domain name to analyze (e.g., "gооgle.com")
     * @return [HomographResult] with detection details and risk score
     */
    fun detect(domain: String): HomographResult {
        val detectedChars = mutableListOf<DetectedChar>()
        var hasPunycode = false

        // Check for punycode prefix
        if (domain.contains("xn--")) {
            hasPunycode = true
        }

        // Scan each character for homographs
        domain.forEachIndexed { index, char ->
            val lookalike = findHomograph(char)
            if (lookalike != null) {
                detectedChars.add(
                    DetectedChar(
                        char = char,
                        position = index,
                        unicodeName = getUnicodeName(char),
                        lookalike = lookalike
                    )
                )
            }
        }

        val score = calculateScore(detectedChars, hasPunycode)

        return HomographResult(
            isHomograph = detectedChars.isNotEmpty() || hasPunycode,
            score = score,
            detectedCharacters = detectedChars,
            punycode = if (hasPunycode) domain else null
        )
    }

    /**
     * Check if a character is a homograph lookalike
     */
    private fun findHomograph(char: Char): Char? {
        return HOMOGRAPH_MAP[char]
    }

    /**
     * Get Unicode name for character (simplified)
     */
    private fun getUnicodeName(char: Char): String {
        return when {
            char.code in 0x0400..0x04FF -> "Cyrillic"
            char.code in 0x0370..0x03FF -> "Greek"
            char.code in 0x0530..0x058F -> "Armenian"
            char.code in 0x10A0..0x10FF -> "Georgian"
            else -> "Unicode U+${char.code.toString(16).uppercase()}"
        }
    }

    private fun calculateScore(detectedChars: List<DetectedChar>, hasPunycode: Boolean): Int {
        var score = 0

        // Each homograph character adds points
        score += detectedChars.size * 15

        // Punycode domains are suspicious
        if (hasPunycode) score += 20

        // Multiple different scripts is very suspicious
        val scripts = detectedChars.map { it.unicodeName }.toSet()
        if (scripts.size > 1) score += 10

        return score.coerceAtMost(50)
    }

    companion object {
        /**
         * Map of confusable Unicode characters to their Latin lookalikes
         * Source: Unicode confusables list
         */
        val HOMOGRAPH_MAP = mapOf(
            // Cyrillic lookalikes
            'а' to 'a',  // Cyrillic Small Letter A
            'е' to 'e',  // Cyrillic Small Letter Ie
            'о' to 'o',  // Cyrillic Small Letter O
            'р' to 'p',  // Cyrillic Small Letter Er
            'с' to 'c',  // Cyrillic Small Letter Es
            'х' to 'x',  // Cyrillic Small Letter Ha
            'у' to 'y',  // Cyrillic Small Letter U
            'і' to 'i',  // Cyrillic Small Letter Byelorussian-Ukrainian I
            'ј' to 'j',  // Cyrillic Small Letter Je

            // Cyrillic Capital lookalikes
            'А' to 'A',
            'В' to 'B',
            'Е' to 'E',
            'К' to 'K',
            'М' to 'M',
            'Н' to 'H',
            'О' to 'O',
            'Р' to 'P',
            'С' to 'C',
            'Т' to 'T',
            'Х' to 'X',

            // Greek lookalikes
            'ο' to 'o',  // Greek Small Letter Omicron
            'α' to 'a',  // Greek Small Letter Alpha
            'ν' to 'v',  // Greek Small Letter Nu
            'τ' to 't',  // Greek Small Letter Tau

            // Other common confusables
            'ı' to 'i',  // Latin Small Letter Dotless I
            'ɑ' to 'a',  // Latin Small Letter Alpha
            'ɡ' to 'g',  // Latin Small Letter Script G
            'ｏ' to 'o', // Fullwidth Latin Small Letter O
            'ａ' to 'a', // Fullwidth Latin Small Letter A
        )
    }
}
