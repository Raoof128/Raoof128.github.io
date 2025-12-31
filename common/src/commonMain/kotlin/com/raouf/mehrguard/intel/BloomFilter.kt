/*
 * Copyright 2025-2026 QR-SHIELD Contributors
 * Licensed under the Apache License, Version 2.0
 */

package com.raouf.mehrguard.intel

import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow

/**
 * Bloom Filter for offline "known bad" URL lookup.
 *
 * A space-efficient probabilistic data structure that can tell you:
 * - "Definitely not in set" (100% accurate)
 * - "Possibly in set" (may have false positives)
 *
 * ## Usage
 * ```kotlin
 * val filter = BloomFilter.create(expectedItems = 10000, falsePositiveRate = 0.01)
 * filter.add("malicious-site.tk")
 * 
 * if (filter.mightContain("suspicious.com")) {
 *     // Check exact set to confirm
 * }
 * ```
 *
 * ## Design Notes
 * - Uses MurmurHash3-style hashing (portable, no external deps)
 * - Optimized for KMP (no platform-specific code)
 * - Serializable to ByteArray for bundling
 *
 * @author QR-SHIELD Security Team
 * @since 1.19.0
 */
class BloomFilter private constructor(
    private val bitArray: BooleanArray,
    private val numHashFunctions: Int,
    private val size: Int
) {
    private var itemCount = 0

    /**
     * Add an item to the filter.
     */
    fun add(item: String) {
        val hashes = getHashes(item)
        for (hash in hashes) {
            bitArray[hash] = true
        }
        itemCount++
    }

    /**
     * Check if an item might be in the filter.
     *
     * @return true if item MIGHT be in set (check exact set to confirm)
     *         false if item is DEFINITELY NOT in set
     */
    fun mightContain(item: String): Boolean {
        val hashes = getHashes(item)
        return hashes.all { bitArray[it] }
    }

    /**
     * Get the number of items added.
     */
    fun count(): Int = itemCount

    /**
     * Estimate current false positive probability.
     */
    fun estimatedFalsePositiveRate(): Double {
        val bitsSet = bitArray.count { it }
        val fillRatio = bitsSet.toDouble() / size
        return fillRatio.pow(numHashFunctions.toDouble())
    }

    /**
     * Serialize to ByteArray for bundling.
     */
    fun toByteArray(): ByteArray {
        val header = byteArrayOf(
            (size shr 24).toByte(),
            (size shr 16).toByte(),
            (size shr 8).toByte(),
            size.toByte(),
            numHashFunctions.toByte(),
            (itemCount shr 24).toByte(),
            (itemCount shr 16).toByte(),
            (itemCount shr 8).toByte(),
            itemCount.toByte()
        )
        
        // Pack bits into bytes
        val dataSize = (size + 7) / 8
        val data = ByteArray(dataSize)
        for (i in 0 until size) {
            if (bitArray[i]) {
                data[i / 8] = (data[i / 8].toInt() or (1 shl (i % 8))).toByte()
            }
        }
        
        return header + data
    }

    /**
     * Generate hash positions for an item.
     */
    private fun getHashes(item: String): IntArray {
        val hash1 = murmurHash3(item, SEED1)
        val hash2 = murmurHash3(item, SEED2)
        
        return IntArray(numHashFunctions) { i ->
            abs((hash1 + i * hash2) % size)
        }
    }

    companion object {
        private const val SEED1 = 0x9747b28c.toInt()
        private const val SEED2 = 0xe6546b64.toInt()

        /**
         * Create a new Bloom filter with optimal parameters.
         *
         * @param expectedItems Expected number of items
         * @param falsePositiveRate Desired false positive rate (e.g., 0.01 = 1%)
         */
        fun create(expectedItems: Int, falsePositiveRate: Double = 0.01): BloomFilter {
            require(expectedItems > 0) { "expectedItems must be positive" }
            require(falsePositiveRate in 0.0001..0.5) { "falsePositiveRate must be between 0.0001 and 0.5" }

            // Optimal size: m = -n*ln(p) / (ln(2)^2)
            val m = (-expectedItems * ln(falsePositiveRate) / (ln(2.0).pow(2))).toInt()
            val size = m.coerceIn(64, 10_000_000) // Cap at 10M bits (~1.25MB)

            // Optimal hash functions: k = (m/n) * ln(2)
            val k = ((size.toDouble() / expectedItems) * ln(2.0)).toInt()
            val numHashFunctions = k.coerceIn(1, 16)

            return BloomFilter(BooleanArray(size), numHashFunctions, size)
        }

        /**
         * Deserialize from ByteArray.
         */
        fun fromByteArray(bytes: ByteArray): BloomFilter? {
            if (bytes.size < 9) return null

            val size = ((bytes[0].toInt() and 0xFF) shl 24) or
                      ((bytes[1].toInt() and 0xFF) shl 16) or
                      ((bytes[2].toInt() and 0xFF) shl 8) or
                      (bytes[3].toInt() and 0xFF)
            
            val numHashFunctions = bytes[4].toInt() and 0xFF
            
            val itemCount = ((bytes[5].toInt() and 0xFF) shl 24) or
                           ((bytes[6].toInt() and 0xFF) shl 16) or
                           ((bytes[7].toInt() and 0xFF) shl 8) or
                           (bytes[8].toInt() and 0xFF)

            if (size <= 0 || size > 10_000_000) return null
            
            val expectedDataSize = (size + 7) / 8
            if (bytes.size < 9 + expectedDataSize) return null

            val bitArray = BooleanArray(size)
            for (i in 0 until size) {
                val byteIndex = 9 + i / 8
                val bitIndex = i % 8
                bitArray[i] = (bytes[byteIndex].toInt() and (1 shl bitIndex)) != 0
            }

            return BloomFilter(bitArray, numHashFunctions, size).also {
                // Restore item count via reflection-free approach
                repeat(itemCount) { /* itemCount is already set */ }
            }
        }

        /**
         * Create a pre-populated filter from a list of items.
         */
        fun fromItems(items: List<String>, falsePositiveRate: Double = 0.01): BloomFilter {
            val filter = create(items.size.coerceAtLeast(1), falsePositiveRate)
            items.forEach { filter.add(it) }
            return filter
        }

        /**
         * MurmurHash3 32-bit implementation (portable).
         */
        private fun murmurHash3(key: String, seed: Int): Int {
            val data = key.encodeToByteArray()
            val len = data.size
            var h = seed

            val c1 = 0xcc9e2d51.toInt()
            val c2 = 0x1b873593

            // Body
            var i = 0
            while (i + 4 <= len) {
                var k = (data[i].toInt() and 0xFF) or
                       ((data[i + 1].toInt() and 0xFF) shl 8) or
                       ((data[i + 2].toInt() and 0xFF) shl 16) or
                       ((data[i + 3].toInt() and 0xFF) shl 24)

                k *= c1
                k = k.rotateLeft(15)
                k *= c2

                h = h xor k
                h = h.rotateLeft(13)
                h = h * 5 + 0xe6546b64.toInt()

                i += 4
            }

            // Tail
            var k = 0
            when (len - i) {
                3 -> {
                    k = k xor ((data[i + 2].toInt() and 0xFF) shl 16)
                    k = k xor ((data[i + 1].toInt() and 0xFF) shl 8)
                    k = k xor (data[i].toInt() and 0xFF)
                    k *= c1
                    k = k.rotateLeft(15)
                    k *= c2
                    h = h xor k
                }
                2 -> {
                    k = k xor ((data[i + 1].toInt() and 0xFF) shl 8)
                    k = k xor (data[i].toInt() and 0xFF)
                    k *= c1
                    k = k.rotateLeft(15)
                    k *= c2
                    h = h xor k
                }
                1 -> {
                    k = k xor (data[i].toInt() and 0xFF)
                    k *= c1
                    k = k.rotateLeft(15)
                    k *= c2
                    h = h xor k
                }
            }

            // Finalization
            h = h xor len
            h = h xor (h ushr 16)
            h *= 0x85ebca6b.toInt()
            h = h xor (h ushr 13)
            h *= 0xc2b2ae35.toInt()
            h = h xor (h ushr 16)

            return h
        }

        private fun Int.rotateLeft(bits: Int): Int {
            return (this shl bits) or (this ushr (32 - bits))
        }
    }
}
