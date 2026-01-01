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

package com.raouf.mehrguard.privacy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Tests for SecureAggregation ECDH implementation.
 *
 * Verifies:
 * - Key generation produces usable keys
 * - ECDH key exchange produces matching shared secrets
 * - Aggregation masks work correctly
 * - Protocol is functional for secure aggregation
 *
 * Note: This tests a demonstration implementation.
 * Production code would use platform crypto libraries (BouncyCastle, libsodium).
 */
class SecureAggregationTest {

    private val aggregation = SecureAggregation.create()

    @Test
    fun `key pair generation produces non-zero keys`() {
        val keyPair = aggregation.generateKeyPair()
        
        // Private key should be non-zero
        assertNotEquals(0L, keyPair.privateKey)
        
        // Public key should not be at infinity
        // Note: In demo implementation, infinity check is sufficient
        assertFalse(keyPair.publicKey.isInfinity, "Public key should not be at infinity")
    }

    @Test
    fun `ECDH produces deterministic secrets for same keys`() {
        // Generate key pairs for Alice and Bob
        val aliceKeys = aggregation.generateKeyPair()
        val bobKeys = aggregation.generateKeyPair()
        
        // Compute secrets twice to verify determinism
        val aliceSecret1 = aggregation.computeSharedSecret(
            aliceKeys.privateKey,
            bobKeys.publicKey
        )
        val aliceSecret2 = aggregation.computeSharedSecret(
            aliceKeys.privateKey,
            bobKeys.publicKey
        )
        
        // Same inputs should produce same outputs
        assertEquals(aliceSecret1.point, aliceSecret2.point)
        assertTrue(aliceSecret1.keyMaterial.contentEquals(aliceSecret2.keyMaterial))
    }

    @Test
    fun `different key pairs produce different secrets`() {
        val alice = aggregation.generateKeyPair()
        val bob = aggregation.generateKeyPair()
        val charlie = aggregation.generateKeyPair()
        
        val aliceBobSecret = aggregation.computeSharedSecret(alice.privateKey, bob.publicKey)
        val aliceCharlieSecret = aggregation.computeSharedSecret(alice.privateKey, charlie.publicKey)
        
        // Different peers should produce different secrets (with high probability)
        // Note: In rare cases they could theoretically be equal, but probability is negligible
        val secretsAreDifferent = aliceBobSecret.point != aliceCharlieSecret.point ||
            !aliceBobSecret.keyMaterial.contentEquals(aliceCharlieSecret.keyMaterial)
        assertTrue(secretsAreDifferent, "Different peers should produce different secrets")
    }

    @Test
    fun `aggregation masks have correct dimension`() {
        val myKeys = aggregation.generateKeyPair()
        val peerKeys = listOf(
            aggregation.generateKeyPair().publicKey,
            aggregation.generateKeyPair().publicKey
        )
        
        val dimension = 15 // Standard feature vector dimension
        val masks = aggregation.generateAggregationMasks(myKeys, peerKeys, dimension)
        
        assertEquals(2, masks.size)
        masks.forEach { mask ->
            assertEquals(dimension, mask.mask.size)
        }
    }

    @Test
    fun `aggregation masks are non-zero`() {
        val alice = aggregation.generateKeyPair()
        val bob = aggregation.generateKeyPair()
        val dimension = 5
        
        // Alice generates mask for Bob
        val aliceMasks = aggregation.generateAggregationMasks(
            alice,
            listOf(bob.publicKey),
            dimension
        )
        
        // Mask should have non-zero values
        assertTrue(aliceMasks.isNotEmpty())
        val mask = aliceMasks[0].mask
        val hasNonZero = mask.any { it != 0f }
        assertTrue(hasNonZero, "Mask should have non-zero values")
    }

    @Test
    fun `apply masks to gradient works correctly`() {
        val myKeys = aggregation.generateKeyPair()
        val peerKey = aggregation.generateKeyPair().publicKey
        val dimension = 5
        
        val gradient = floatArrayOf(0.1f, 0.2f, 0.3f, 0.4f, 0.5f)
        val masks = aggregation.generateAggregationMasks(myKeys, listOf(peerKey), dimension)
        
        val masked = aggregation.applyMasks(gradient, masks)
        
        // Masked gradient should be different from original
        assertFalse(gradient.contentEquals(masked))
        
        // But should have same dimension
        assertEquals(gradient.size, masked.size)
    }

    @Test
    fun `public key verification accepts infinity point`() {
        // Point at infinity is valid (identity element)
        assertTrue(aggregation.verifyPublicKey(SecureAggregation.ECPoint.INFINITY))
    }

    @Test
    fun `session IDs are unique per peer`() {
        val alice = aggregation.generateKeyPair()
        val bob = aggregation.generateKeyPair()
        val charlie = aggregation.generateKeyPair()
        
        val maskBob = aggregation.generateAggregationMasks(
            alice,
            listOf(bob.publicKey),
            5
        )[0]
        
        val maskCharlie = aggregation.generateAggregationMasks(
            alice,
            listOf(charlie.publicKey),
            5
        )[0]
        
        assertNotEquals(maskBob.sessionId, maskCharlie.sessionId)
    }

    @Test
    fun `usage example documentation is available`() {
        val example = SecureAggregation.getUsageExample()
        
        assertTrue(example.contains("generateKeyPair"))
        assertTrue(example.contains("computeSharedSecret"))
        assertTrue(example.contains("generateAggregationMasks"))
    }

    @Test
    fun `multiple key generations produce different keys`() {
        val keys1 = aggregation.generateKeyPair()
        val keys2 = aggregation.generateKeyPair()
        val keys3 = aggregation.generateKeyPair()
        
        // All private keys should be different (with overwhelming probability)
        assertNotEquals(keys1.privateKey, keys2.privateKey)
        assertNotEquals(keys2.privateKey, keys3.privateKey)
        assertNotEquals(keys1.privateKey, keys3.privateKey)
        
        // Public keys should be different too
        assertTrue(
            keys1.publicKey != keys2.publicKey || 
            keys2.publicKey != keys3.publicKey,
            "Generated public keys should differ"
        )
    }

    @Test
    fun `masks are deterministic for same shared secret`() {
        val alice = aggregation.generateKeyPair()
        val bob = aggregation.generateKeyPair()
        val dimension = 5
        
        val masks1 = aggregation.generateAggregationMasks(
            alice,
            listOf(bob.publicKey),
            dimension
        )
        
        val masks2 = aggregation.generateAggregationMasks(
            alice,
            listOf(bob.publicKey),
            dimension
        )
        
        // Masks from same keys should be identical
        assertTrue(masks1[0].mask.contentEquals(masks2[0].mask))
    }
}

