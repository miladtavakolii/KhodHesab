package ir.khodhesab.sms_engine.parser.strategy.banks

import ir.khodhesab.sms_engine.parser.model.ParsedSmsData
import ir.khodhesab.sms_engine.parser.model.RawSmsMessage
import ir.khodhesab.sms_engine.parser.strategy.BankParserStrategy
import ir.khodhesab.sms_engine.parser.strategy.BaseRegexExtractor

/**
 * Specialized parsing strategy for Bank Tejarat, Bank Saderat, Bank Keshavarzi, and Bank Sepah.
 * Handles masked card numbers (`*5678`), deposit notations, and signed amount strings.
 */
class TejaratSaderatKeshavarziSepahStrategy : BankParserStrategy {

    override val supportedBankCodes: Set<String> = setOf("TEJARAT", "SADERAT", "KESHAVARZI", "SEPAH")

    override fun parse(
        normalizedBody: String,
        rawSms: RawSmsMessage,
        bankCode: String
    ): ParsedSmsData? {
        return BaseRegexExtractor.buildParsedSmsData(normalizedBody, rawSms, bankCode)
    }
}
