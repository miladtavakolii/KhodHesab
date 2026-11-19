package ir.khodhesab.sms_engine.parser.model

/**
 * Sealed interface representing the outcome of parsing an SMS message.
 */
sealed interface SmsParseOutcome {
    /**
     * Successfully parsed financial transaction SMS.
     */
    data class Success(val data: ParsedSmsData) : SmsParseOutcome

    /**
     * Message ignored because it is a non-financial SMS (OTP, dynamic password, security alert).
     *
     * @property reason Human-readable reason for ignoring.
     */
    data class IgnoredNonFinancial(val reason: String) : SmsParseOutcome

    /**
     * SMS sender or content could not be identified with any supported Iranian bank.
     *
     * @property sender Raw sender address string.
     */
    data class UnrecognizedBank(val sender: String) : SmsParseOutcome

    /**
     * Recognized bank, but regex extraction failed to parse valid transaction details.
     *
     * @property bankCode Code of the identified bank.
     * @property reason Description of extraction failure.
     */
    data class ParseFailed(val bankCode: String, val reason: String) : SmsParseOutcome
}
