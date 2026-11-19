package ir.khodhesab.sms_engine.parser.strategy

import ir.khodhesab.sms_engine.parser.model.ParsedSmsData
import ir.khodhesab.sms_engine.parser.model.RawSmsMessage

/**
 * Resilient universal fallback parsing strategy for all Iranian banks.
 */
class GenericIranianBankStrategy : BankParserStrategy {

    override val supportedBankCodes: Set<String> = emptySet()

    override fun parse(
        normalizedBody: String,
        rawSms: RawSmsMessage,
        bankCode: String
    ): ParsedSmsData? {
        return BaseRegexExtractor.buildParsedSmsData(normalizedBody, rawSms, bankCode)
    }
}
