package ir.khodhesab.sms_engine.parser.model

/**
 * Data class representing a raw unparsed SMS message.
 *
 * @property sender SMS sender number or shortcode (e.g. `"+98200033"`, `"BANK MELLI"`).
 * @property body Raw text content of the SMS.
 * @property timestamp SMS arrival timestamp in epoch milliseconds.
 */
data class RawSmsMessage(
    val sender: String,
    val body: String,
    val timestamp: Long
)
