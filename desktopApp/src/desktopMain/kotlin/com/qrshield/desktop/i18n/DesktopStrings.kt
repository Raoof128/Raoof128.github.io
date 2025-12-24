package com.qrshield.desktop.i18n

import java.util.Locale

enum class AppLanguage(val code: String, val displayName: String) {
    English("en", "English"),
    German("de", "Deutsch"),
    Spanish("es", "Español"),
    French("fr", "Français"),
    Italian("it", "Italiano"),
    Portuguese("pt", "Português"),
    Russian("ru", "Русский"),
    ChineseSimplified("zh", "中文(简体)"),
    Japanese("ja", "日本語"),
    Korean("ko", "한국어"),
    Hindi("hi", "हिन्दी"),
    Arabic("ar", "العربية"),
    Turkish("tr", "Türkçe"),
    Vietnamese("vi", "Tiếng Việt"),
    Indonesian("in", "Bahasa Indonesia"),
    Thai("th", "ไทย");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return when (code.lowercase()) {
                "de", "de-de", "de_at", "de-ch" -> German
                "es", "es-es", "es-mx", "es-419" -> Spanish
                "fr", "fr-fr", "fr-ca" -> French
                "it", "it-it" -> Italian
                "pt", "pt-br", "pt-pt" -> Portuguese
                "ru", "ru-ru" -> Russian
                "zh", "zh-cn", "zh-hans", "zh-sg" -> ChineseSimplified
                "ja", "ja-jp" -> Japanese
                "ko", "ko-kr" -> Korean
                "hi", "hi-in" -> Hindi
                "ar", "ar-sa", "ar-eg" -> Arabic
                "tr", "tr-tr" -> Turkish
                "vi", "vi-vn" -> Vietnamese
                "in", "id", "id-id" -> Indonesian
                "th", "th-th" -> Thai
                else -> English
            }
        }

        fun systemDefault(): AppLanguage {
            val language = Locale.getDefault().language
            return fromCode(language)
        }
    }
}

enum class DesktopStringKey(val defaultText: String) {
    AppName("QR-SHIELD"),
    MenuMain("Main Menu"),
    MenuSecurity("Security"),
    MenuSystem("System"),
    NavDashboard("Dashboard"),
    NavScanMonitor("Scan Monitor"),
    NavScanHistory("Scan History"),
    NavTrustCentre("Trust Centre"),
    NavReports("Reports"),
    NavTraining("Beat the Bot"),
    NavSettings("Settings")
}

object DesktopStrings {
    private val keyByDefaultText = DesktopStringKey.values().associateBy { it.defaultText }

    fun text(key: DesktopStringKey, language: AppLanguage): String {
        return when (language) {
            AppLanguage.German -> GermanStrings[key] ?: key.defaultText
            AppLanguage.Spanish -> SpanishStrings[key] ?: key.defaultText
            AppLanguage.French -> FrenchStrings[key] ?: key.defaultText
            AppLanguage.Italian -> ItalianStrings[key] ?: key.defaultText
            AppLanguage.Portuguese -> PortugueseStrings[key] ?: key.defaultText
            AppLanguage.Russian -> RussianStrings[key] ?: key.defaultText
            AppLanguage.ChineseSimplified -> ChineseSimplifiedStrings[key] ?: key.defaultText
            AppLanguage.Japanese -> JapaneseStrings[key] ?: key.defaultText
            AppLanguage.Korean -> KoreanStrings[key] ?: key.defaultText
            AppLanguage.Hindi -> HindiStrings[key] ?: key.defaultText
            AppLanguage.Arabic -> ArabicStrings[key] ?: key.defaultText
            AppLanguage.Turkish -> TurkishStrings[key] ?: key.defaultText
            AppLanguage.Vietnamese -> VietnameseStrings[key] ?: key.defaultText
            AppLanguage.Indonesian -> IndonesianStrings[key] ?: key.defaultText
            AppLanguage.Thai -> ThaiStrings[key] ?: key.defaultText
            AppLanguage.English -> key.defaultText
        }
    }

    fun translate(text: String, language: AppLanguage): String {
        val key = keyByDefaultText[text]
        if (key != null) {
            return text(key, language)
        }
        return when (language) {
            AppLanguage.German -> GermanCommonStrings[text] ?: text
            AppLanguage.Spanish -> SpanishCommonStrings[text] ?: text
            AppLanguage.French -> FrenchCommonStrings[text] ?: text
            AppLanguage.Italian -> ItalianCommonStrings[text] ?: text
            AppLanguage.Portuguese -> PortugueseCommonStrings[text] ?: text
            AppLanguage.Russian -> RussianCommonStrings[text] ?: text
            AppLanguage.ChineseSimplified -> ChineseSimplifiedCommonStrings[text] ?: text
            AppLanguage.Japanese -> JapaneseCommonStrings[text] ?: text
            AppLanguage.Korean -> KoreanCommonStrings[text] ?: text
            AppLanguage.Hindi -> HindiCommonStrings[text] ?: text
            AppLanguage.Arabic -> ArabicCommonStrings[text] ?: text
            AppLanguage.Turkish -> TurkishCommonStrings[text] ?: text
            AppLanguage.Vietnamese -> VietnameseCommonStrings[text] ?: text
            AppLanguage.Indonesian -> IndonesianCommonStrings[text] ?: text
            AppLanguage.Thai -> ThaiCommonStrings[text] ?: text
            AppLanguage.English -> text
        }
    }

    fun format(text: String, language: AppLanguage, vararg args: Any): String {
        val template = translate(text, language)
        return String.format(Locale.forLanguageTag(language.code), template, *args)
    }
}

