package ir.khodhesab.domain.model

/**
 * Pure domain model representing a single financial transaction.
 *
 * @property id Unique transaction identifier.
 * @property account Associated [Account] domain model.
 * @property category Associated optional [Category] domain model.
 * @property kind Transaction kind ([TransactionKind.DEPOSIT], [TransactionKind.WITHDRAWAL], or [TransactionKind.TRANSFER]).
 * @property amount Transaction amount in Rials.
 * @property balanceAfter Account balance remaining after transaction, if reported.
 * @property timestamp Epoch timestamp in milliseconds.
 * @property description Optional user note or description.
 * @property rawSmsBody Original SMS body if created via SMS import.
 * @property smsHash Unique SHA-256 hash string for SMS deduplication.
 */
data class Transaction(
    val id: Long = 0,
    val account: Account,
    val category: Category? = null,
    val kind: TransactionKind,
    val amount: Long,
    val balanceAfter: Long? = null,
    val timestamp: Long,
    val description: String? = null,
    val rawSmsBody: String? = null,
    val smsHash: String? = null
)
