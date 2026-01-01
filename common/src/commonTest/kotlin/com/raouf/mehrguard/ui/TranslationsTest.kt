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

package com.raouf.mehrguard.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

/**
 * Tests for the multi-language Translations system.
 *
 * Verifies:
 * - All supported languages return correct translator
 * - Translation keys return appropriate values
 * - Fallback to English works for missing translations
 * - Format strings work correctly
 */
class TranslationsTest {

    @Test
    fun `English translator returns default text`() {
        val translator = Translations.forLanguage("en")
        
        assertEquals("en", translator.languageCode)
        assertEquals("English", translator.languageName)
        
        // Should return default text for all keys
        assertEquals("Safe", translator.get(LocalizationKeys.VERDICT_SAFE))
        assertEquals("Dangerous", translator.get(LocalizationKeys.VERDICT_MALICIOUS))
    }

    @Test
    fun `German translator returns German text`() {
        val translator = Translations.forLanguage("de")
        
        assertEquals("de", translator.languageCode)
        assertEquals("Deutsch", translator.languageName)
        
        // Should return German translations
        assertEquals("Sicher", translator.get(LocalizationKeys.VERDICT_SAFE))
        assertEquals("Gefährlich", translator.get(LocalizationKeys.VERDICT_MALICIOUS))
        assertEquals("Verlauf", translator.get(LocalizationKeys.TAB_HISTORY))
        assertEquals("Einstellungen", translator.get(LocalizationKeys.TAB_SETTINGS))
    }

    @Test
    fun `Spanish translator returns Spanish text`() {
        val translator = Translations.forLanguage("es")
        
        assertEquals("es", translator.languageCode)
        assertEquals("Español", translator.languageName)
        
        assertEquals("Seguro", translator.get(LocalizationKeys.VERDICT_SAFE))
        assertEquals("Peligroso", translator.get(LocalizationKeys.VERDICT_MALICIOUS))
    }

    @Test
    fun `French translator returns French text`() {
        val translator = Translations.forLanguage("fr")
        
        assertEquals("fr", translator.languageCode)
        assertEquals("Français", translator.languageName)
        
        assertEquals("Sûr", translator.get(LocalizationKeys.VERDICT_SAFE))
        assertEquals("Dangereux", translator.get(LocalizationKeys.VERDICT_MALICIOUS))
    }

    @Test
    fun `Japanese translator returns Japanese text`() {
        val translator = Translations.forLanguage("ja")
        
        assertEquals("ja", translator.languageCode)
        assertEquals("日本語", translator.languageName)
        
        assertEquals("安全", translator.get(LocalizationKeys.VERDICT_SAFE))
        assertEquals("危険", translator.get(LocalizationKeys.VERDICT_MALICIOUS))
    }

    @Test
    fun `unknown language falls back to English`() {
        val translator = Translations.forLanguage("zh")
        
        assertEquals("en", translator.languageCode)
        assertEquals("Safe", translator.get(LocalizationKeys.VERDICT_SAFE))
    }

    @Test
    fun `language code is case insensitive`() {
        val lower = Translations.forLanguage("de")
        val upper = Translations.forLanguage("DE")
        val mixed = Translations.forLanguage("De")
        
        assertEquals(lower.languageCode, upper.languageCode)
        assertEquals(lower.languageCode, mixed.languageCode)
    }

    @Test
    fun `language code handles locale strings`() {
        // Should handle locale strings like "de-DE" or "de_AT"
        val deDE = Translations.forLanguage("de-DE")
        val deAT = Translations.forLanguage("de_AT")
        
        assertEquals("de", deDE.languageCode)
        assertEquals("de", deAT.languageCode)
    }

    @Test
    fun `German has all critical translations`() {
        val de = Translations.forLanguage("de")
        
        // All critical user-facing strings should be translated
        val criticalKeys = listOf(
            LocalizationKeys.VERDICT_SAFE,
            LocalizationKeys.VERDICT_SUSPICIOUS,
            LocalizationKeys.VERDICT_MALICIOUS,
            LocalizationKeys.TAB_SCAN,
            LocalizationKeys.TAB_HISTORY,
            LocalizationKeys.TAB_SETTINGS,
            LocalizationKeys.ACTION_SCAN_ANOTHER,
            LocalizationKeys.ACTION_OPEN_URL,
            LocalizationKeys.ACTION_SHARE,
            LocalizationKeys.SIGNAL_BRAND_IMPERSONATION,
            LocalizationKeys.SIGNAL_HOMOGRAPH
        )
        
        criticalKeys.forEach { key ->
            val translation = de.get(key)
            // Should not fall back to English default
            assertNotEquals(key.defaultText, translation, "Missing German translation for: ${key.key}")
        }
    }

    @Test
    fun `format method replaces placeholders`() {
        val translator = Translations.forLanguage("de")
        
        val formatted = translator.format(LocalizationKeys.A11Y_RISK_SCORE_LABEL, 85)
        
        assertTrue(formatted.contains("85"), "Should contain the formatted number")
        assertTrue(formatted.contains("100"), "Should contain the max score")
    }

    @Test
    fun `supportedLanguages returns all languages`() {
        val languages = Translations.supportedLanguages()
        
        assertEquals(5, languages.size)
        assertTrue(languages.any { it.code == "en" })
        assertTrue(languages.any { it.code == "de" })
        assertTrue(languages.any { it.code == "es" })
        assertTrue(languages.any { it.code == "fr" })
        assertTrue(languages.any { it.code == "ja" })
    }

    @Test
    fun `isSupported returns correct values`() {
        assertTrue(Translations.isSupported("en"))
        assertTrue(Translations.isSupported("de"))
        assertTrue(Translations.isSupported("es"))
        assertTrue(Translations.isSupported("fr"))
        assertTrue(Translations.isSupported("ja"))
        
        assertFalse(Translations.isSupported("zh"))
        assertFalse(Translations.isSupported("ko"))
        assertFalse(Translations.isSupported("xyz"))
    }

    @Test
    fun `Germany locale tagline is correct`() {
        val de = Translations.forLanguage("de")
        
        val tagline = de.get(LocalizationKeys.APP_TAGLINE)
        assertEquals("Scannen. Erkennen. Schützen.", tagline)
    }

    @Test
    fun `missing translation falls back to default`() {
        val de = Translations.forLanguage("de")
        
        // Create a key that we know isn't translated
        val missingKey = LocalizedKey("some_untranslated_key", "Default Fallback Text")
        
        val result = de.get(missingKey)
        assertEquals("Default Fallback Text", result)
    }
}
