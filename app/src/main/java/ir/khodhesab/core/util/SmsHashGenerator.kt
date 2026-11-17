package ir.khodhesab.core.util

import java.security.MessageDigest

/**
 * Generator for deterministic SHA-256 hash values of SMS messages.
 *
 * Used for deduplication to prevent importing the same SMS multiple times.
 */
object SmsHashGenerator {

    /**
     * Generates a deterministic SHA-256 hash string for an SMS message.
     *
     * @param sender The sender's address or bank identifier (e.g., `"BANK-MELLI"`).
     * @param normalizedBody The pre-normalized SMS body text.
     * @param timestampBucket Timestamp bucket in epoch milliseconds representing receipt time.
     * @return 64-character lowercase SHA-256 hex string.
     */
    fun generateHash(sender: String, normalizedBody: String, timestampBucket: Long): String {
        val canonicalPayload = "${sender.trim().uppercase()}|$normalizedBody|$timestampBucket"
        val bytes = MessageDigest.getInstance("SHA-256").digest(canonicalPayload.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
