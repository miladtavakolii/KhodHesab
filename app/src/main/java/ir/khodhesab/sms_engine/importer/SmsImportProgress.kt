package ir.khodhesab.sms_engine.importer

/**
 * Progress status for historical SMS inbox scan and import.
 *
 * @property totalMessages Total SMS messages found in inbox matching criteria.
 * @property processedCount Number of messages processed so far.
 * @property recordedCount Number of new transactions successfully inserted.
 * @property duplicateCount Number of duplicate SMS messages skipped.
 * @property ignoredOrUnrecognizedCount Number of non-financial, OTP, or non-bank SMS messages skipped.
 * @property isCompleted `true` when import scanning is complete.
 */
data class SmsImportProgress(
    val totalMessages: Int = 0,
    val processedCount: Int = 0,
    val recordedCount: Int = 0,
    val duplicateCount: Int = 0,
    val ignoredOrUnrecognizedCount: Int = 0,
    val isCompleted: Boolean = false
) {
    /**
     * Computed progress fraction between `0.0f` and `1.0f`.
     */
    val progressFraction: Float
        get() = if (totalMessages > 0) (processedCount.toFloat() / totalMessages.toFloat()).coerceIn(0f, 1f) else 0f
}
