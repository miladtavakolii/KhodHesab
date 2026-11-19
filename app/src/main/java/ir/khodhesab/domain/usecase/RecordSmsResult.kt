package ir.khodhesab.domain.usecase

/**
 * Sealed result type representing the outcome of an SMS transaction recording operation.
 */
sealed interface RecordSmsResult {
    /**
     * Successfully recorded new SMS transaction.
     *
     * @property transactionId Database ID of the newly inserted transaction.
     */
    data class Inserted(val transactionId: Long) : RecordSmsResult

    /**
     * SMS was skipped because an identical SHA-256 hash already exists in the database.
     */
    data object DuplicateSkipped : RecordSmsResult

    /**
     * The SMS sender number or shortcode could not be resolved to any supported bank.
     */
    data object BankNotRecognized : RecordSmsResult
}
