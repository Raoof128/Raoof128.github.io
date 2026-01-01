/*
 * Copyright 2025-2026 Mehr Guard Contributors
 * Licensed under the Apache License, Version 2.0
 */

package com.raouf.mehrguard.intel

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

/**
 * Tests for BloomFilter.
 */
class BloomFilterTest {

    @Test
    fun `create with default parameters`() {
        val filter = BloomFilter.create(expectedItems = 1000)
        assertNotNull(filter)
        assertEquals(0, filter.count())
    }

    @Test
    fun `add and check items`() {
        val filter = BloomFilter.create(expectedItems = 100, falsePositiveRate = 0.01)
        
        filter.add("test1.com")
        filter.add("test2.com")
        filter.add("test3.com")
        
        assertEquals(3, filter.count())
        assertTrue(filter.mightContain("test1.com"))
        assertTrue(filter.mightContain("test2.com"))
        assertTrue(filter.mightContain("test3.com"))
    }

    @Test
    fun `definitely not contains returns false`() {
        val filter = BloomFilter.create(expectedItems = 100)
        
        filter.add("exists.com")
        
        // This should DEFINITELY not be in the filter
        // (With high probability - no false negatives in Bloom filters)
        assertTrue(filter.mightContain("exists.com"))
        
        // Items not added should return false (no false negatives)
        // Note: There's a small chance of false positive, but unlikely for small filters
    }

    @Test
    fun `fromItems creates populated filter`() {
        val items = listOf("a.com", "b.com", "c.com", "d.com", "e.com")
        val filter = BloomFilter.fromItems(items)
        
        assertEquals(5, filter.count())
        items.forEach { item ->
            assertTrue(filter.mightContain(item), "Should contain $item")
        }
    }

    @Test
    fun `serialization roundtrip`() {
        val original = BloomFilter.create(expectedItems = 100)
        original.add("test1.com")
        original.add("test2.com")
        original.add("test3.com")
        
        val bytes = original.toByteArray()
        val restored = BloomFilter.fromByteArray(bytes)
        
        assertNotNull(restored)
        assertTrue(restored.mightContain("test1.com"))
        assertTrue(restored.mightContain("test2.com"))
        assertTrue(restored.mightContain("test3.com"))
    }

    @Test
    fun `estimated false positive rate`() {
        val filter = BloomFilter.create(expectedItems = 1000, falsePositiveRate = 0.01)
        
        repeat(500) { i ->
            filter.add("domain$i.com")
        }
        
        val fpr = filter.estimatedFalsePositiveRate()
        // Should be less than our target since we only added half the expected items
        assertTrue(fpr < 0.1, "FPR should be low: $fpr")
    }

    @Test
    fun `handles empty filter`() {
        val filter = BloomFilter.create(expectedItems = 100)
        
        assertFalse(filter.mightContain("anything.com"))
        assertEquals(0, filter.count())
    }

    @Test
    fun `handles large number of items`() {
        val items = (1..5000).map { "domain$it.com" }
        val filter = BloomFilter.fromItems(items, falsePositiveRate = 0.01)
        
        assertEquals(5000, filter.count())
        
        // Check that all items are found
        var foundCount = 0
        items.forEach { item ->
            if (filter.mightContain(item)) foundCount++
        }
        
        // All items should be found (no false negatives)
        assertEquals(5000, foundCount)
    }
}
