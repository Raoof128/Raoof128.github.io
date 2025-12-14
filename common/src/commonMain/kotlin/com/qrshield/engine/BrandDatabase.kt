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

package com.qrshield.engine

/**
 * Brand Database for Phishing Detection
 *
 * Contains comprehensive brand configurations for detecting impersonation attacks.
 * Includes global tech companies, financial institutions, Australian banks,
 * social media, e-commerce, and government services.
 *
 * SECURITY NOTES:
 * - Database is immutable and thread-safe
 * - Patterns are curated from real phishing campaigns
 * - Updated regularly with new threat intelligence
 *
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
object BrandDatabase {

    /**
     * Brand category for classification.
     */
    enum class BrandCategory {
        FINANCIAL,
        TECHNOLOGY,
        SOCIAL,
        ECOMMERCE,
        ENTERTAINMENT,
        LOGISTICS,
        GOVERNMENT
    }

    /**
     * Brand configuration with detection patterns.
     */
    data class BrandConfig(
        val officialDomains: Set<String>,
        val typosquats: List<String>,
        val homographs: List<String>,
        val combosquats: List<String>,
        val category: BrandCategory = BrandCategory.TECHNOLOGY
    )

    /**
     * Comprehensive brand database.
     */
    val brands: Map<String, BrandConfig> by lazy { buildBrandDatabase() }

    private fun buildBrandDatabase(): Map<String, BrandConfig> = mapOf(

        // === FINANCIAL - GLOBAL ===

        "paypal" to BrandConfig(
            officialDomains = setOf("paypal.com", "paypal.me"),
            typosquats = listOf("paypa1", "paypai", "paypol", "paypaI", "paypall", "pypai"),
            homographs = listOf("pаypal", "раypal"), // Cyrillic 'а'
            combosquats = listOf("paypal-secure", "paypal-login", "paypal-verify", "paypal-update", "paypal-support"),
            category = BrandCategory.FINANCIAL
        ),

        "stripe" to BrandConfig(
            officialDomains = setOf("stripe.com"),
            typosquats = listOf("str1pe", "striipe", "strpe"),
            homographs = listOf("strіpe"), // Cyrillic 'і'
            combosquats = listOf("stripe-payment", "stripe-login", "stripe-verify"),
            category = BrandCategory.FINANCIAL
        ),

        // === FINANCIAL - AUSTRALIAN BANKS ===

        "commbank" to BrandConfig(
            officialDomains = setOf("commbank.com.au", "commonwealth.bank"),
            typosquats = listOf("cornmbank", "commbenk", "comnbank", "c0mmbank", "combank"),
            homographs = listOf("соmmbank"), // Cyrillic 'о'
            combosquats = listOf("commbank-login", "commbank-secure", "commbank-netbank", "commbank-verify"),
            category = BrandCategory.FINANCIAL
        ),

        "nab" to BrandConfig(
            officialDomains = setOf("nab.com.au"),
            typosquats = listOf("n4b", "naab", "nnab"),
            homographs = listOf("nаb"), // Cyrillic 'а'
            combosquats = listOf("nab-login", "nab-secure", "nab-internet-banking"),
            category = BrandCategory.FINANCIAL
        ),

        "westpac" to BrandConfig(
            officialDomains = setOf("westpac.com.au"),
            typosquats = listOf("westpec", "westpacc", "w3stpac", "wetspac"),
            homographs = listOf("wеstpac"), // Cyrillic 'е'
            combosquats = listOf("westpac-login", "westpac-secure", "westpac-online"),
            category = BrandCategory.FINANCIAL
        ),

        "anz" to BrandConfig(
            officialDomains = setOf("anz.com.au", "anz.com"),
            typosquats = listOf("4nz", "annz", "anzz"),
            homographs = listOf("аnz"), // Cyrillic 'а'
            combosquats = listOf("anz-login", "anz-secure", "anz-internet-banking"),
            category = BrandCategory.FINANCIAL
        ),

        "bendigo" to BrandConfig(
            officialDomains = setOf("bendigobank.com.au"),
            typosquats = listOf("bendlgo", "bendig0", "bendiqo"),
            homographs = listOf("bеndigo"), // Cyrillic 'е'
            combosquats = listOf("bendigo-login", "bendigo-bank-login"),
            category = BrandCategory.FINANCIAL
        ),

        // === TECH GIANTS ===

        "google" to BrandConfig(
            officialDomains = setOf("google.com", "google.co.uk", "google.com.au", "gmail.com", "youtube.com"),
            typosquats = listOf("g00gle", "googie", "goog1e", "gooogle", "goggle", "gogle"),
            homographs = listOf("gооgle", "googlе"), // Cyrillic 'о', 'е'
            combosquats = listOf("google-login", "google-verify", "google-account", "google-security", "google-alert"),
            category = BrandCategory.TECHNOLOGY
        ),

        "microsoft" to BrandConfig(
            officialDomains = setOf("microsoft.com", "live.com", "outlook.com", "office.com", "azure.com"),
            typosquats = listOf("micr0soft", "rnicrosoft", "mircosoft", "microsft", "microsofl"),
            homographs = listOf("mісrosoft", "miсrosoft"), // Cyrillic 'і', 'с'
            combosquats = listOf("microsoft-login", "microsoft-account", "office-login", "microsoft-security", "office365-login"),
            category = BrandCategory.TECHNOLOGY
        ),

        "apple" to BrandConfig(
            officialDomains = setOf("apple.com", "icloud.com", "apple.co"),
            typosquats = listOf("app1e", "appie", "aple", "applle", "applе"),
            homographs = listOf("аpple", "apрle"), // Cyrillic 'а', 'р'
            combosquats = listOf("apple-id", "icloud-login", "apple-verify", "apple-support", "appleid-login"),
            category = BrandCategory.TECHNOLOGY
        ),

        "amazon" to BrandConfig(
            officialDomains = setOf("amazon.com", "amazon.co.uk", "amazon.com.au", "aws.amazon.com", "amzn.com"),
            typosquats = listOf("amaz0n", "arnazon", "amazom", "amazonn", "amazn"),
            homographs = listOf("аmazon", "amаzon"), // Cyrillic 'а'
            combosquats = listOf("amazon-prime", "amazon-order", "amazon-delivery", "amazon-login", "amazon-security"),
            category = BrandCategory.ECOMMERCE
        ),

        // === SOCIAL MEDIA ===

        "facebook" to BrandConfig(
            officialDomains = setOf("facebook.com", "fb.com", "meta.com", "messenger.com"),
            typosquats = listOf("faceb00k", "facebok", "faceboook", "facebk", "facbook"),
            homographs = listOf("fасebook", "facebооk"), // Cyrillic 'а', 'о'
            combosquats = listOf("facebook-login", "facebook-verify", "fb-security", "facebook-support"),
            category = BrandCategory.SOCIAL
        ),

        "instagram" to BrandConfig(
            officialDomains = setOf("instagram.com"),
            typosquats = listOf("1nstagram", "instagran", "lnstagram", "instaqram", "instagrom"),
            homographs = listOf("іnstagram", "instаgram"), // Cyrillic 'і', 'а'
            combosquats = listOf("instagram-verify", "instagram-login", "instagram-support"),
            category = BrandCategory.SOCIAL
        ),

        "twitter" to BrandConfig(
            officialDomains = setOf("twitter.com", "x.com"),
            typosquats = listOf("twltter", "tw1tter", "twiiter", "tvvitter", "twiter"),
            homographs = listOf("twіtter", "tωitter"), // Cyrillic 'і', Greek 'ω'
            combosquats = listOf("twitter-verify", "twitter-login", "twitter-support"),
            category = BrandCategory.SOCIAL
        ),

        "linkedin" to BrandConfig(
            officialDomains = setOf("linkedin.com"),
            typosquats = listOf("1inkedin", "linkedln", "linkdin", "linkedn", "llnkedin"),
            homographs = listOf("lіnkedin", "linkеdin"), // Cyrillic 'і', 'е'
            combosquats = listOf("linkedin-login", "linkedin-verify", "linkedin-job"),
            category = BrandCategory.SOCIAL
        ),

        "tiktok" to BrandConfig(
            officialDomains = setOf("tiktok.com"),
            typosquats = listOf("tikt0k", "tikttok", "tiktop", "tlktok"),
            homographs = listOf("tіktok"), // Cyrillic 'і'
            combosquats = listOf("tiktok-verify", "tiktok-login"),
            category = BrandCategory.SOCIAL
        ),

        // === STREAMING ===

        "netflix" to BrandConfig(
            officialDomains = setOf("netflix.com"),
            typosquats = listOf("netf1ix", "netfiix", "nettflix", "netfllx", "netlfix"),
            homographs = listOf("nеtflix", "netflіx"), // Cyrillic 'е', 'і'
            combosquats = listOf("netflix-billing", "netflix-update", "netflix-account", "netflix-payment"),
            category = BrandCategory.ENTERTAINMENT
        ),

        "spotify" to BrandConfig(
            officialDomains = setOf("spotify.com"),
            typosquats = listOf("spot1fy", "spotlfy", "spoitfy", "spotfy"),
            homographs = listOf("spоtify"), // Cyrillic 'о'
            combosquats = listOf("spotify-login", "spotify-premium", "spotify-verify"),
            category = BrandCategory.ENTERTAINMENT
        ),

        // === DELIVERY/LOGISTICS ===

        "auspost" to BrandConfig(
            officialDomains = setOf("auspost.com.au"),
            typosquats = listOf("ausp0st", "auspostt", "aspost", "austpost"),
            homographs = listOf("аuspost"), // Cyrillic 'а'
            combosquats = listOf("auspost-delivery", "auspost-tracking", "auspost-parcel"),
            category = BrandCategory.LOGISTICS
        ),

        "dhl" to BrandConfig(
            officialDomains = setOf("dhl.com", "dhl.com.au"),
            typosquats = listOf("dh1", "dhll", "d-hl"),
            homographs = listOf("dһl"), // Cyrillic 'һ'
            combosquats = listOf("dhl-tracking", "dhl-delivery", "dhl-parcel"),
            category = BrandCategory.LOGISTICS
        ),

        "fedex" to BrandConfig(
            officialDomains = setOf("fedex.com"),
            typosquats = listOf("fed3x", "fedx", "feddex"),
            homographs = listOf("fеdex"), // Cyrillic 'е'
            combosquats = listOf("fedex-tracking", "fedex-delivery"),
            category = BrandCategory.LOGISTICS
        ),

        // === GOVERNMENT (AU) ===

        "mygovau" to BrandConfig(
            officialDomains = setOf("my.gov.au", "mygov.gov.au"),
            typosquats = listOf("myg0v", "mygove", "myygov"),
            homographs = listOf("mуgov"), // Cyrillic 'у'
            combosquats = listOf("mygov-login", "mygov-verify", "mygov-au"),
            category = BrandCategory.GOVERNMENT
        ),

        "ato" to BrandConfig(
            officialDomains = setOf("ato.gov.au"),
            typosquats = listOf("at0", "attoo", "atoo"),
            homographs = listOf("аto"), // Cyrillic 'а'
            combosquats = listOf("ato-refund", "ato-login", "ato-tax"),
            category = BrandCategory.GOVERNMENT
        ),

        // === CRYPTOCURRENCY ===

        "coinbase" to BrandConfig(
            officialDomains = setOf("coinbase.com"),
            typosquats = listOf("c0inbase", "coinbasse", "colnbase", "coinbasee"),
            homographs = listOf("соinbase", "coinbаse"), // Cyrillic 'о', 'а'
            combosquats = listOf("coinbase-login", "coinbase-verify", "coinbase-wallet"),
            category = BrandCategory.FINANCIAL
        ),

        "binance" to BrandConfig(
            officialDomains = setOf("binance.com"),
            typosquats = listOf("b1nance", "blnance", "binnance", "binence"),
            homographs = listOf("bіnance", "binаnce"), // Cyrillic 'і', 'а'
            combosquats = listOf("binance-login", "binance-verify", "binance-wallet"),
            category = BrandCategory.FINANCIAL
        ),

        "metamask" to BrandConfig(
            officialDomains = setOf("metamask.io"),
            typosquats = listOf("metam4sk", "metamsk", "metamaask", "rnetamask"),
            homographs = listOf("metаmask"), // Cyrillic 'а'
            combosquats = listOf("metamask-verify", "metamask-connect", "metamask-wallet", "metamask-sync"),
            category = BrandCategory.FINANCIAL
        ),

        // === HEALTHCARE ===

        "medicare" to BrandConfig(
            officialDomains = setOf("servicesaustralia.gov.au", "humanservices.gov.au"),
            typosquats = listOf("medlcare", "med1care", "medicarre"),
            homographs = listOf("mediсare"), // Cyrillic 'с'
            combosquats = listOf("medicare-login", "medicare-claim", "medicare-rebate"),
            category = BrandCategory.GOVERNMENT
        ),

        // === EUROPEAN BANKS ===

        "hsbc" to BrandConfig(
            officialDomains = setOf("hsbc.com", "hsbc.co.uk", "hsbc.com.au"),
            typosquats = listOf("h5bc", "hsbcc", "hab"),
            homographs = listOf("hsbс"), // Cyrillic 'с'
            combosquats = listOf("hsbc-login", "hsbc-online", "hsbc-secure"),
            category = BrandCategory.FINANCIAL
        ),

        "barclays" to BrandConfig(
            officialDomains = setOf("barclays.co.uk", "barclays.com"),
            typosquats = listOf("barc1ays", "barclay", "barcelays"),
            homographs = listOf("barсlays"), // Cyrillic 'с'
            combosquats = listOf("barclays-login", "barclays-secure", "barclays-online"),
            category = BrandCategory.FINANCIAL
        ),

        "revolut" to BrandConfig(
            officialDomains = setOf("revolut.com"),
            typosquats = listOf("revo1ut", "revoluit", "revollut"),
            homographs = listOf("rеvolut"), // Cyrillic 'е'
            combosquats = listOf("revolut-login", "revolut-verify", "revolut-support"),
            category = BrandCategory.FINANCIAL
        ),

        // === ASIAN SERVICES ===

        "alipay" to BrandConfig(
            officialDomains = setOf("alipay.com"),
            typosquats = listOf("al1pay", "allpay", "alipey"),
            homographs = listOf("alіpay"), // Cyrillic 'і'
            combosquats = listOf("alipay-login", "alipay-verify"),
            category = BrandCategory.FINANCIAL
        ),

        "wechat" to BrandConfig(
            officialDomains = setOf("wechat.com", "weixin.qq.com"),
            typosquats = listOf("wech4t", "wechet", "wechatt"),
            homographs = listOf("weсhat"), // Cyrillic 'с'
            combosquats = listOf("wechat-login", "wechat-verify", "wechat-pay"),
            category = BrandCategory.SOCIAL
        ),

        // === MESSAGING APPS ===

        "whatsapp" to BrandConfig(
            officialDomains = setOf("whatsapp.com", "wa.me"),
            typosquats = listOf("whatsap", "whatssapp", "whatsaapp", "watsapp"),
            homographs = listOf("whаtsapp"), // Cyrillic 'а'
            combosquats = listOf("whatsapp-verify", "whatsapp-update", "whatsapp-web"),
            category = BrandCategory.SOCIAL
        ),

        "telegram" to BrandConfig(
            officialDomains = setOf("telegram.org", "t.me"),
            typosquats = listOf("te1egram", "telegran", "telegramm"),
            homographs = listOf("telеgram"), // Cyrillic 'е'
            combosquats = listOf("telegram-verify", "telegram-login", "telegram-web"),
            category = BrandCategory.SOCIAL
        ),

        // === MORE E-COMMERCE ===

        "ebay" to BrandConfig(
            officialDomains = setOf("ebay.com", "ebay.com.au", "ebay.co.uk"),
            typosquats = listOf("ebaay", "eb4y", "ebbay"),
            homographs = listOf("ebаy"), // Cyrillic 'а'
            combosquats = listOf("ebay-login", "ebay-verify", "ebay-order"),
            category = BrandCategory.ECOMMERCE
        ),

        "shopify" to BrandConfig(
            officialDomains = setOf("shopify.com"),
            typosquats = listOf("shoplfy", "shopity", "shop1fy"),
            homographs = listOf("shоpify"), // Cyrillic 'о'
            combosquats = listOf("shopify-login", "shopify-store", "shopify-admin"),
            category = BrandCategory.ECOMMERCE
        ),

        // === CLOUD SERVICES ===

        "dropbox" to BrandConfig(
            officialDomains = setOf("dropbox.com"),
            typosquats = listOf("dr0pbox", "dropboxx", "dropbx"),
            homographs = listOf("drоpbox"), // Cyrillic 'о'
            combosquats = listOf("dropbox-login", "dropbox-share", "dropbox-verify"),
            category = BrandCategory.TECHNOLOGY
        ),

        "zoom" to BrandConfig(
            officialDomains = setOf("zoom.us", "zoom.com"),
            typosquats = listOf("z00m", "zom", "zoomm"),
            homographs = listOf("zооm"), // Cyrillic 'о'
            combosquats = listOf("zoom-login", "zoom-meeting", "zoom-verify"),
            category = BrandCategory.TECHNOLOGY
        ),

        "slack" to BrandConfig(
            officialDomains = setOf("slack.com"),
            typosquats = listOf("s1ack", "slak", "slackk"),
            homographs = listOf("slaсk"), // Cyrillic 'с'
            combosquats = listOf("slack-login", "slack-invite", "slack-verify"),
            category = BrandCategory.TECHNOLOGY
        ),

        // === GAMING ===

        "steam" to BrandConfig(
            officialDomains = setOf("steampowered.com", "steamcommunity.com"),
            typosquats = listOf("stean", "steaam", "stearn", "steamm"),
            homographs = listOf("stеam"), // Cyrillic 'е'
            combosquats = listOf("steam-login", "steam-trade", "steam-guard", "steam-gift"),
            category = BrandCategory.ENTERTAINMENT
        ),

        "discord" to BrandConfig(
            officialDomains = setOf("discord.com", "discordapp.com"),
            typosquats = listOf("dlscord", "disc0rd", "discrod"),
            homographs = listOf("disсord"), // Cyrillic 'с'
            combosquats = listOf("discord-nitro", "discord-gift", "discord-verify"),
            category = BrandCategory.ENTERTAINMENT
        )
    )
}
