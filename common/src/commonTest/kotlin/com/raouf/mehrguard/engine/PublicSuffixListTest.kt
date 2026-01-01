/*
 * Copyright 2025-2026 Mehr Guard Contributors
 * Licensed under the Apache License, Version 2.0
 */

package com.raouf.mehrguard.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for PublicSuffixList.
 */
class PublicSuffixListTest {

    private val psl = PublicSuffixList()

    @Test
    fun `simple TLD extraction`() {
        val result = psl.parse("www.example.com")
        assertEquals("com", result.effectiveTld)
        assertEquals("example.com", result.registrableDomain)
        assertEquals(listOf("www"), result.subdomains)
        assertEquals(1, result.subdomainDepth)
    }

    @Test
    fun `multi-part TLD co_uk`() {
        val result = psl.parse("www.example.co.uk")
        assertEquals("co.uk", result.effectiveTld)
        assertEquals("example.co.uk", result.registrableDomain)
        assertEquals(listOf("www"), result.subdomains)
    }

    @Test
    fun `multi-part TLD com_au`() {
        val result = psl.parse("mail.store.example.com.au")
        assertEquals("com.au", result.effectiveTld)
        assertEquals("example.com.au", result.registrableDomain)
        assertEquals(listOf("mail", "store"), result.subdomains)
        assertEquals(2, result.subdomainDepth)
    }

    @Test
    fun `no subdomain`() {
        val result = psl.parse("example.com")
        assertEquals("com", result.effectiveTld)
        assertEquals("example.com", result.registrableDomain)
        assertTrue(result.subdomains.isEmpty())
        assertEquals(0, result.subdomainDepth)
    }

    @Test
    fun `deep subdomains`() {
        val result = psl.parse("a.b.c.d.example.com")
        assertEquals("com", result.effectiveTld)
        assertEquals("example.com", result.registrableDomain)
        assertEquals(listOf("a", "b", "c", "d"), result.subdomains)
        assertEquals(4, result.subdomainDepth)
    }

    @Test
    fun `convenience methods work`() {
        assertEquals("example.co.uk", psl.getRegistrableDomain("www.example.co.uk"))
        assertEquals("co.uk", psl.getEffectiveTld("www.example.co.uk"))
        assertEquals(1, psl.getSubdomainDepth("www.example.co.uk"))
    }

    @Test
    fun `case insensitive`() {
        val result = psl.parse("WWW.EXAMPLE.COM")
        assertEquals("com", result.effectiveTld)
        assertEquals("example.com", result.registrableDomain)
    }

    @Test
    fun `empty host returns default`() {
        val result = psl.parse("")
        assertEquals("", result.effectiveTld)
    }

    @Test
    fun `single label host`() {
        val result = psl.parse("localhost")
        assertEquals("localhost", result.effectiveTld)
        assertEquals("localhost", result.registrableDomain)
    }

    @Test
    fun `Japanese TLD co_jp`() {
        val result = psl.parse("www.example.co.jp")
        assertEquals("co.jp", result.effectiveTld)
        assertEquals("example.co.jp", result.registrableDomain)
    }

    @Test
    fun `Brazilian TLD com_br`() {
        val result = psl.parse("loja.example.com.br")
        assertEquals("com.br", result.effectiveTld)
        assertEquals("example.com.br", result.registrableDomain)
    }
}
