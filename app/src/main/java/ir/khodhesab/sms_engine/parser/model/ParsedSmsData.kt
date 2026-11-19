package ir.khodhesab.sms_engine.parser.model

import ir.khodhesab.domain.model.TransactionKind

/**
 * Data class holding structured details extracted from a bank transaction SMS.
 *
 * @property bankCode Bank code identifier (e.g., `"MELLAT"`, `"MELLI"`, `"BLU"`).
 * @property accountIdentifier Extracted account or card identifier (e.g., `"1234"` or `"DEFAULT"`).
 * @property kind Extracted transaction kind ([TransactionKind.DEPOSIT], [TransactionKind.WITHDRAWAL], or [TransactionKind.TRANSFER]).
 * @property amount Extracted financial transaction amount normalized to Rials.
 * @property balanceAfter Remaining account balance normalized to Rials (nullable).
 * @property timestamp Transaction timestamp in epoch milliseconds.
 * @property rawBody Original body text of the SMS message.
 * @property smsHash Unique SHA-256 hash string calculated for deduplication.
 */
data class ParsedSmsData(
    val bankCode: String,
    val accountIdentifier: String,
    val kind: TransactionKind,
    val amount: Long,
    val balanceAfter: Long?,
    val timestamp: Long,
    val rawBody: String,
    val smsHash: String
)
