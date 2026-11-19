package ir.khodhesab.sms_engine

/**
 * Result representing the outcome of coordinating an SMS message through parsing and database recording.
 */
sealed interface SmsProcessResult {
    /**
     * Transaction successfully parsed and inserted into database.
     *
     * @property transactionId Database ID of the created transaction.
     * @property bankCode Code of the bank.
     * @property amount Amount in Rials.
     */
    data class Recorded(val transactionId: Long, val bankCode: String, val amount: Long) : SmsProcessResult

    /**
     * Transaction was skipped because an identical SMS hash already exists.
     */
    data object Duplicate : SmsProcessResult

    /**
     * SMS was ignored as non-financial (e.g. OTP / security alert).
     *
     * @property reason Reason for ignoring.
     */
    data class Ignored(val reason: String) : SmsProcessResult

    /**
     * Bank could not be recognized for this SMS sender/body.
     */
    data class Unrecognized(val sender: String) : SmsProcessResult

    /**
     * Bank was recognized, but regex parsing or database insertion failed.
     */
    data class Failed(val reason: String) : SmsProcessResult
}
