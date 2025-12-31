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

package com.raouf.mehrguard.crypto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFails

/**
 * Tests for SecureECDH wrapper implementation.
 *
 * These tests verify:
 * - Key generation produces correct sizes
 * - Platform secure RNG is used (non-zero, varying outputs)
 * - Input validation catches malformed keys
 * - Memory clearing works
 *
 * Note: Full ECDH exchange verification is covered by SecureAggregationTest.
 */
class SecureECDHTest {

    // ==================== Key Generation Tests ====================

    @Test
    fun generateKeyPair_produces_valid_key_sizes() {
        val keyPair = SecureECDH.generateKeyPair()
        
        assertEquals(SecureECDH.KEY_SIZE, keyPair.privateKey.size)
        assertEquals(SecureECDH.KEY_SIZE, keyPair.publicKey.size)
    }

    @Test
    fun generateKeyPair_produces_non_zero_keys() {
        val keyPair = SecureECDH.generateKeyPair()
        
        assertTrue(keyPair.privateKey.any { it != 0.toByte() }, 
            "Private key should be non-zero")
        assertTrue(keyPair.publicKey.any { it != 0.toByte() }, 
            "Public key should be non-zero")
    }

    @Test
    fun generateKeyPair_produces_different_keys_each_time() {
        val keyPair1 = SecureECDH.generateKeyPair()
        val keyPair2 = SecureECDH.generateKeyPair()
        val keyPair3 = SecureECDH.generateKeyPair()
        
        // Private keys should differ
        assertTrue(!keyPair1.privateKey.contentEquals(keyPair2.privateKey))
        assertTrue(!keyPair2.privateKey.contentEquals(keyPair3.privateKey))
        
        // Public keys should differ
        assertTrue(!keyPair1.publicKey.contentEquals(keyPair2.publicKey))
        assertTrue(!keyPair2.publicKey.contentEquals(keyPair3.publicKey))
    }

    @Test
    fun generateKeyPair_clamps_private_key_correctly() {
        val keyPair = SecureECDH.generateKeyPair()
        val privateKey = keyPair.privateKey
        
        // RFC 7748-style clamping:
        // - Lowest 3 bits of first byte are 0
        // - Highest bit of last byte is 0
        // - Second highest bit of last byte is 1
        
        assertEquals(0, privateKey[0].toInt() and 0x07, 
            "Lowest 3 bits should be cleared")
        assertEquals(0, privateKey[31].toInt() and 0x80, 
            "Highest bit should be cleared")
        assertEquals(0x40, privateKey[31].toInt() and 0x40, 
            "Second highest bit should be set")
    }

    // ==================== Shared Secret Tests ====================

    @Test
    fun shared_secret_has_correct_size() {
        val alice = SecureECDH.generateKeyPair()
        val bob = SecureECDH.generateKeyPair()
        
        val secret = SecureECDH.computeSharedSecret(alice.privateKey, bob.publicKey)
        
        assertEquals(SecureECDH.SECRET_SIZE, secret.raw.size)
    }

    @Test
    fun shared_secret_is_non_zero() {
        val alice = SecureECDH.generateKeyPair()
        val bob = SecureECDH.generateKeyPair()
        
        val secret = SecureECDH.computeSharedSecret(alice.privateKey, bob.publicKey)
        
        assertTrue(secret.raw.any { it != 0.toByte() }, 
            "Secret should be non-zero")
    }

    @Test
    fun shared_secret_is_deterministic() {
        val alice = SecureECDH.generateKeyPair()
        val bob = SecureECDH.generateKeyPair()
        
        val secret1 = SecureECDH.computeSharedSecret(alice.privateKey, bob.publicKey)
        val secret2 = SecureECDH.computeSharedSecret(alice.privateKey, bob.publicKey)
        
        assertTrue(secret1.raw.contentEquals(secret2.raw),
            "Same inputs should produce same shared secret")
    }

    @Test
    fun different_peers_produce_different_secrets() {
        val alice = SecureECDH.generateKeyPair()
        val bob = SecureECDH.generateKeyPair()
        val charlie = SecureECDH.generateKeyPair()
        
        val aliceBobSecret = SecureECDH.computeSharedSecret(alice.privateKey, bob.publicKey)
        val aliceCharlieSecret = SecureECDH.computeSharedSecret(alice.privateKey, charlie.publicKey)
        
        assertTrue(
            !aliceBobSecret.raw.contentEquals(aliceCharlieSecret.raw),
            "Different peers should produce different secrets"
        )
    }

    // ==================== Input Validation Tests ====================

    @Test
    fun computeSharedSecret_rejects_wrong_key_size() {
        val alice = SecureECDH.generateKeyPair()
        val shortKey = ByteArray(16) { 1 }
        val longKey = ByteArray(64) { 1 }
        
        assertFails("Should reject short private key") {
            SecureECDH.computeSharedSecret(shortKey, alice.publicKey)
        }
        
        assertFails("Should reject long private key") {
            SecureECDH.computeSharedSecret(longKey, alice.publicKey)
        }
        
        assertFails("Should reject short public key") {
            SecureECDH.computeSharedSecret(alice.privateKey, shortKey)
        }
        
        assertFails("Should reject long public key") {
            SecureECDH.computeSharedSecret(alice.privateKey, longKey)
        }
    }

    @Test
    fun computeSharedSecret_rejects_zero_public_key() {
        val alice = SecureECDH.generateKeyPair()
        val zeroKey = ByteArray(32) { 0 }
        
        assertFails("Should reject all-zero public key") {
            SecureECDH.computeSharedSecret(alice.privateKey, zeroKey)
        }
    }

    // ==================== Memory Safety Tests ====================

    @Test
    fun clear_zeroes_private_key() {
        val keyPair = SecureECDH.generateKeyPair()
        val originalPrivate = keyPair.privateKey.copyOf()
        
        keyPair.clear()
        
        assertTrue(keyPair.privateKey.all { it == 0.toByte() }, 
            "Private key should be zeroed after clear()")
        assertTrue(originalPrivate.any { it != 0.toByte() }, 
            "Original copy should still have data")
    }

    @Test
    fun sharedSecret_clear_zeroes_raw_bytes() {
        val alice = SecureECDH.generateKeyPair()
        val bob = SecureECDH.generateKeyPair()
        
        val secret = SecureECDH.computeSharedSecret(alice.privateKey, bob.publicKey)
        val originalSecret = secret.raw.copyOf()
        
        secret.clear()
        
        assertTrue(secret.raw.all { it == 0.toByte() }, 
            "Secret should be zeroed after clear()")
        assertTrue(originalSecret.any { it != 0.toByte() }, 
            "Original copy should still have data")
    }

    // ==================== Platform Secure RNG Tests ====================

    @Test
    fun platform_secure_rng_provides_entropy() {
        // Generate multiple key pairs and verify they're different
        val keyPairs = (1..5).map { SecureECDH.generateKeyPair() }
        
        val uniquePrivateKeys = keyPairs.map { it.privateKey.toList() }.distinct()
        val uniquePublicKeys = keyPairs.map { it.publicKey.toList() }.distinct()
        
        assertTrue(uniquePrivateKeys.size >= 4, 
            "5 random key pairs should have at least 4 unique private keys")
        assertTrue(uniquePublicKeys.size >= 4, 
            "5 random key pairs should have at least 4 unique public keys")
    }
}
