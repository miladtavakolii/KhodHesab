package ir.khodhesab.sms_engine.parser.strategy

import ir.khodhesab.sms_engine.parser.model.ParsedSmsData
import ir.khodhesab.sms_engine.parser.model.RawSmsMessage

/**
 * Interface contract for bank-specific SMS parsing strategies.
 */
interface BankParserStrategy {

    /**
     * Set of unique bank codes supported by this strategy (e.g. `setOf("MELLAT", "MELLI")`).
     * An empty set indicates a universal fallback strategy.
     */
    val supportedBankCodes: Set<String>

    /**
     * Parses normalized SMS body and raw message details into a [ParsedSmsData] object.
     *
     * @param normalizedBody Pre-normalized text of the SMS.
     * @param rawSms Original raw SMS message instance.
     * @param bankCode Resolved bank code identifier.
     * @return Extracted [ParsedSmsData] or `null` if regex parsing failed.
     */
    fun parse(normalizedBody: String, rawSms: RawSmsMessage, bankCode: String): ParsedSmsData?
}
