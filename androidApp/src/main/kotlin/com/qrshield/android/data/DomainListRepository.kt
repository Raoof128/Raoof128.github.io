/*
 * Copyright 2025-2026 QR-SHIELD Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package com.qrshield.android.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
// DataStore extension for domain lists
val Context.domainListsDataStore: DataStore<Preferences> by preferencesDataStore(name = "domain_lists")

/**
 * Domain List Entry for allowlist/blocklist
 */
data class DomainEntry(
    val domain: String,
    val addedAt: Long = System.currentTimeMillis(),
    val source: DomainSource = DomainSource.MANUAL,
    val type: DomainType = DomainType.NEUTRAL
)

enum class DomainSource {
    MANUAL, ENTERPRISE, AUTO_LEARNED, SCANNED
}

enum class DomainType {
    NEUTRAL, MALICIOUS, SUSPICIOUS, PHISHING
}

/**
 * Repository for managing allowlist and blocklist with DataStore persistence
 */
class DomainListRepository(private val context: Context) {
    
    private object Keys {
        val ALLOWLIST = stringSetPreferencesKey("allowlist_domains")
        val BLOCKLIST = stringSetPreferencesKey("blocklist_domains")
    }
    
    // === ALLOWLIST ===
    
    /**
     * Observe allowlist changes as Flow
     */
    val allowlist: Flow<List<DomainEntry>> = context.domainListsDataStore.data.map { prefs ->
        val domains = prefs[Keys.ALLOWLIST] ?: defaultAllowlist
        domains.map { parseEntry(it, DomainSource.MANUAL) }
    }
    
    /**
     * Add domain to allowlist
     */
    suspend fun addToAllowlist(domain: String, source: DomainSource = DomainSource.MANUAL) {
        val normalized = normalizeDomain(domain)
        context.domainListsDataStore.edit { prefs ->
            val current = prefs[Keys.ALLOWLIST]?.toMutableSet() ?: mutableSetOf()
            current.add(encodeEntry(normalized, source))
            prefs[Keys.ALLOWLIST] = current
        }
    }
    
    /**
     * Remove domain from allowlist
     */
    suspend fun removeFromAllowlist(domain: String) {
        val normalized = normalizeDomain(domain)
        context.domainListsDataStore.edit { prefs ->
            val current = prefs[Keys.ALLOWLIST]?.toMutableSet() ?: mutableSetOf()
            current.removeAll { it.startsWith("$normalized|") || it == normalized }
            prefs[Keys.ALLOWLIST] = current
        }
    }
    
    /**
     * Check if domain is in allowlist
     */
    suspend fun isAllowlisted(domain: String): Boolean {
        val normalized = normalizeDomain(domain)
        val prefs = context.domainListsDataStore.data.first()
        val domains = prefs[Keys.ALLOWLIST] ?: emptySet()
        return domains.any { 
            val parsed = it.split("|").first()
            normalized == parsed || normalized.endsWith(".$parsed")
        }
    }
    
    // === BLOCKLIST ===
    
    /**
     * Observe blocklist changes as Flow
     */
    val blocklist: Flow<List<DomainEntry>> = context.domainListsDataStore.data.map { prefs ->
        val domains = prefs[Keys.BLOCKLIST] ?: defaultBlocklist
        domains.map { parseEntry(it, DomainSource.SCANNED, DomainType.MALICIOUS) }
    }
    
    /**
     * Add domain to blocklist
     */
    suspend fun addToBlocklist(
        domain: String, 
        source: DomainSource = DomainSource.MANUAL,
        type: DomainType = DomainType.MALICIOUS
    ) {
        val normalized = normalizeDomain(domain)
        context.domainListsDataStore.edit { prefs ->
            val current = prefs[Keys.BLOCKLIST]?.toMutableSet() ?: mutableSetOf()
            current.add(encodeEntry(normalized, source, type))
            prefs[Keys.BLOCKLIST] = current
        }
    }
    
    /**
     * Remove domain from blocklist
     */
    suspend fun removeFromBlocklist(domain: String) {
        val normalized = normalizeDomain(domain)
        context.domainListsDataStore.edit { prefs ->
            val current = prefs[Keys.BLOCKLIST]?.toMutableSet() ?: mutableSetOf()
            current.removeAll { it.startsWith("$normalized|") || it == normalized }
            prefs[Keys.BLOCKLIST] = current
        }
    }
    
    /**
     * Check if domain is in blocklist
     */
    suspend fun isBlocklisted(domain: String): Boolean {
        val normalized = normalizeDomain(domain)
        val prefs = context.domainListsDataStore.data.first()
        val domains = prefs[Keys.BLOCKLIST] ?: emptySet()
        return domains.any {
            val parsed = it.split("|").first()
            normalized == parsed || normalized.endsWith(".$parsed")
        }
    }
    
    // === HELPERS ===
    
    private fun normalizeDomain(domain: String): String {
        return domain
            .lowercase()
            .removePrefix("http://")
            .removePrefix("https://")
            .removePrefix("www.")
            .substringBefore("/")
            .substringBefore("?")
            .trim()
    }
    
    private fun encodeEntry(
        domain: String, 
        source: DomainSource,
        type: DomainType = DomainType.NEUTRAL
    ): String {
        val timestamp = System.currentTimeMillis()
        return "$domain|$timestamp|${source.name}|${type.name}"
    }
    
    private fun parseEntry(
        encoded: String, 
        defaultSource: DomainSource,
        defaultType: DomainType = DomainType.NEUTRAL
    ): DomainEntry {
        val parts = encoded.split("|")
        return DomainEntry(
            domain = parts.getOrElse(0) { encoded },
            addedAt = parts.getOrElse(1) { "0" }.toLongOrNull() ?: 0L,
            source = parts.getOrElse(2) { defaultSource.name }.let { 
                runCatching { DomainSource.valueOf(it) }.getOrElse { defaultSource }
            },
            type = parts.getOrElse(3) { defaultType.name }.let {
                runCatching { DomainType.valueOf(it) }.getOrElse { defaultType }
            }
        )
    }
    
    companion object {
        // Default trusted domains
        private val defaultAllowlist = setOf(
            "google.com|0|ENTERPRISE|NEUTRAL",
            "microsoft.com|0|ENTERPRISE|NEUTRAL",
            "apple.com|0|ENTERPRISE|NEUTRAL",
            "github.com|0|AUTO_LEARNED|NEUTRAL",
            "slack.com|0|ENTERPRISE|NEUTRAL"
        )
        
        // Default blocked domains (for demo)
        private val defaultBlocklist = setOf(
            "malicious-site.net|0|SCANNED|MALICIOUS",
            "phishing-login.com|0|SCANNED|PHISHING",
            "suspicious-domain.org|0|MANUAL|SUSPICIOUS"
        )
    }
}
