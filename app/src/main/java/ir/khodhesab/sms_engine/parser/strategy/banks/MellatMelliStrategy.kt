package ir.khodhesab.sms_engine.parser.strategy.banks

import ir.khodhesab.sms_engine.parser.model.ParsedSmsData
import ir.khodhesab.sms_engine.parser.model.RawSmsMessage
import ir.khodhesab.sms_engine.parser.strategy.BankParserStrategy
import ir.khodhesab.sms_engine.parser.strategy.BaseRegexExtractor

/**
 * Specialized parsing strategy for Bank Mellat (`MELLAT`) and Bank Melli (`MELLI`).
 * Handles compact multi-line templates with explicit or signed amounts.
 */
class MellatMelliStrategy : BankParserStrategy {

    override val supportedBankCodes: Set<String> = setOf("MELLAT", "MELLI")

    override fun parse(
        normalizedBody: String,
        rawSms: RawSmsMessage,
        bankCode: String
    ): ParsedSmsData? {
        return BaseRegexExtractor.buildParsedSmsData(normalizedBody, rawSms, bankCode)
    }
}
