package ir.khodhesab.core.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests verifying behavior of [SmsHashGenerator].
 */
class SmsHashGeneratorTest {

    @Test
    fun generateHash_isDeterministicForIdenticalInputs() {
        val sender = "BANK-MELLI"
        val body = "برداشت 100000"
        val timestamp = 1700000000000L

        val hash1 = SmsHashGenerator.generateHash(sender, body, timestamp)
        val hash2 = SmsHashGenerator.generateHash(sender, body, timestamp)

        assertThat(hash1).isEqualTo(hash2)
        assertThat(hash1).hasLength(64)
    }

    @Test
    fun generateHash_isSenderCaseInsensitive() {
        val body = "برداشت 100000"
        val timestamp = 1700000000000L

        val hashUpper = SmsHashGenerator.generateHash("Bank-Melli", body, timestamp)
        val hashLower = SmsHashGenerator.generateHash("bank-melli", body, timestamp)

        assertThat(hashUpper).isEqualTo(hashLower)
    }

    @Test
    fun generateHash_producesDifferentHashesForDifferentBodies() {
        val sender = "BANK-MELLI"
        val timestamp = 1700000000000L

        val hash1 = SmsHashGenerator.generateHash(sender, "برداشت 100", timestamp)
        val hash2 = SmsHashGenerator.generateHash(sender, "واریز 100", timestamp)

        assertThat(hash1).isNotEqualTo(hash2)
    }

    @Test
    fun generateHash_producesDifferentHashesForDifferentTimestamps() {
        val sender = "BANK-MELLI"
        val body = "برداشت 100"

        val hash1 = SmsHashGenerator.generateHash(sender, body, 1000L)
        val hash2 = SmsHashGenerator.generateHash(sender, body, 2000L)

        assertThat(hash1).isNotEqualTo(hash2)
    }
}
