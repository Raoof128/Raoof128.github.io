/*
 * Copyright 2025-2026 QR-SHIELD Contributors
 * Licensed under the Apache License, Version 2.0
 */

package com.qrshield.engine

/**
 * Public Suffix List for eTLD+1 computation.
 * Bundled snapshot of top PSL entries for offline operation.
 */
class PublicSuffixList {

    data class ParseResult(
        val host: String,
        val effectiveTld: String,
        val registrableDomain: String,
        val subdomains: List<String>,
        val subdomainDepth: Int
    )

    fun parse(host: String): ParseResult {
        if (host.isBlank() || host.length > 255) {
            return ParseResult(host, "", host, emptyList(), 0)
        }

        val hostLower = host.lowercase().trim()
        val parts = hostLower.split(".").filter { it.isNotEmpty() }
        
        if (parts.isEmpty()) {
            return ParseResult(host, "", host, emptyList(), 0)
        }

        // Find the effective TLD
        val etld = findEffectiveTld(parts)
        val etldParts = etld.split(".").size

        // Registrable domain is eTLD + 1 label
        val registrableDomain = if (parts.size > etldParts) {
            parts.takeLast(etldParts + 1).joinToString(".")
        } else {
            hostLower
        }

        // Subdomains are everything before the registrable domain
        val subdomainCount = parts.size - etldParts - 1
        val subdomains = if (subdomainCount > 0) {
            parts.take(subdomainCount)
        } else {
            emptyList()
        }

        return ParseResult(
            host = hostLower,
            effectiveTld = etld,
            registrableDomain = registrableDomain,
            subdomains = subdomains,
            subdomainDepth = subdomains.size
        )
    }

    private fun findEffectiveTld(parts: List<String>): String {
        if (parts.isEmpty()) return ""

        // Check multi-part TLDs first (e.g., co.uk, com.au)
        if (parts.size >= 2) {
            val twoPartTld = "${parts[parts.size - 2]}.${parts.last()}"
            if (twoPartTld in MULTI_PART_TLDS) {
                return twoPartTld
            }
        }

        // Check three-part TLDs (e.g., pvt.k12.ma.us)
        if (parts.size >= 3) {
            val threePartTld = "${parts[parts.size - 3]}.${parts[parts.size - 2]}.${parts.last()}"
            if (threePartTld in THREE_PART_TLDS) {
                return threePartTld
            }
        }

        // Default to single TLD
        return parts.last()
    }

    fun getRegistrableDomain(host: String): String = parse(host).registrableDomain
    fun getEffectiveTld(host: String): String = parse(host).effectiveTld
    fun getSubdomainDepth(host: String): Int = parse(host).subdomainDepth

    companion object {
        val default = PublicSuffixList()

        // Multi-part TLDs (eTLDs with 2 parts)
        val MULTI_PART_TLDS = setOf(
            // UK
            "co.uk", "org.uk", "me.uk", "ac.uk", "gov.uk", "ltd.uk", "plc.uk", "net.uk", "sch.uk",
            // Australia
            "com.au", "net.au", "org.au", "edu.au", "gov.au", "asn.au", "id.au",
            // Brazil
            "com.br", "net.br", "org.br", "gov.br", "edu.br",
            // Japan
            "co.jp", "or.jp", "ne.jp", "ac.jp", "go.jp", "gr.jp", "ed.jp",
            // New Zealand
            "co.nz", "org.nz", "net.nz", "govt.nz", "ac.nz", "school.nz",
            // South Africa
            "co.za", "org.za", "gov.za", "edu.za", "net.za",
            // India
            "co.in", "org.in", "net.in", "gov.in", "ac.in", "edu.in", "res.in",
            // Germany
            "com.de",
            // France
            "asso.fr", "com.fr", "gouv.fr",
            // Spain
            "com.es", "org.es", "gob.es", "edu.es",
            // Italy
            "co.it",
            // Russia
            "com.ru", "org.ru", "net.ru",
            // China
            "com.cn", "org.cn", "net.cn", "gov.cn", "edu.cn", "ac.cn",
            // Korea
            "co.kr", "or.kr", "ne.kr", "go.kr", "ac.kr",
            // Hong Kong
            "com.hk", "org.hk", "gov.hk", "edu.hk",
            // Singapore
            "com.sg", "org.sg", "gov.sg", "edu.sg",
            // Malaysia
            "com.my", "org.my", "gov.my", "edu.my",
            // Indonesia
            "co.id", "or.id", "go.id", "ac.id",
            // Thailand
            "co.th", "or.th", "go.th", "ac.th",
            // Vietnam
            "com.vn", "org.vn", "gov.vn", "edu.vn",
            // Philippines
            "com.ph", "org.ph", "gov.ph", "edu.ph",
            // Pakistan
            "com.pk", "org.pk", "gov.pk", "edu.pk",
            // Turkey
            "com.tr", "org.tr", "gov.tr", "edu.tr",
            // Egypt
            "com.eg", "org.eg", "gov.eg", "edu.eg",
            // Saudi Arabia
            "com.sa", "org.sa", "gov.sa", "edu.sa",
            // UAE
            "co.ae", "org.ae", "gov.ae", "ac.ae",
            // Mexico
            "com.mx", "org.mx", "gob.mx", "edu.mx",
            // Argentina
            "com.ar", "org.ar", "gob.ar", "edu.ar",
            // Colombia
            "com.co", "org.co", "gov.co", "edu.co",
            // Generic
            "co.com", "or.at", "co.at"
        )

        // Three-part TLDs (rare but exist)
        val THREE_PART_TLDS = setOf(
            "pvt.k12.ma.us", "chtr.k12.ma.us",
            "lib.or.us", "k12.or.us"
        )
    }
}
