package ir.khodhesab.sms_engine.parser.strategy.banks

import ir.khodhesab.sms_engine.parser.model.ParsedSmsData
import ir.khodhesab.sms_engine.parser.model.RawSmsMessage
import ir.khodhesab.sms_engine.parser.strategy.BankParserStrategy
import ir.khodhesab.sms_engine.parser.strategy.BaseRegexExtractor

/**
 * Specialized parsing strategy for BluBank (`BLU`), Bank Saman (`SAMAN`), and Bank Pasargad (`PASARGAD`).
 * Handles Toman <-> Rial conversion for BluBank and dots/dashes in account/deposit numbers.
 */
class BluSamanPasargadStrategy : BankParserStrategy {

    override val supportedBankCodes: Set<String> = setOf("BLU", "SAMAN", "PASARGAD")

    override fun parse(
        normalizedBody: String,
        rawSms: RawSmsMessage,
        bankCode: String
    ): ParsedSmsData? {
        return BaseRegexExtractor.buildParsedSmsData(normalizedBody, rawSms, bankCode)
    }
}
