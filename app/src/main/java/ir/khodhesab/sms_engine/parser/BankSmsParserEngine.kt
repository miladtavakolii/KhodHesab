package ir.khodhesab.sms_engine.parser

import ir.khodhesab.core.util.PersianTextNormalizer
import ir.khodhesab.sms_engine.parser.model.RawSmsMessage
import ir.khodhesab.sms_engine.parser.model.SmsParseOutcome
import ir.khodhesab.sms_engine.parser.strategy.BankParserStrategy
import ir.khodhesab.sms_engine.parser.strategy.GenericIranianBankStrategy
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Core engine orchestrating SMS filtering, bank detection, and regex strategy parsing.
 */
@Singleton
class BankSmsParserEngine @Inject constructor(
    private val nonFinancialFilter: NonFinancialSmsFilter,
    private val bankDetector: BankDetector,
    private val strategies: Set<@JvmSuppressWildcards BankParserStrategy>
) {

    private val fallbackStrategy = GenericIranianBankStrategy()

    /**
     * Parses a raw SMS message and produces an [SmsParseOutcome].
     *
     * @param rawSms Input raw SMS message.
     * @return [SmsParseOutcome.Success], [SmsParseOutcome.IgnoredNonFinancial], [SmsParseOutcome.UnrecognizedBank], or [SmsParseOutcome.ParseFailed].
     */
    suspend fun parse(rawSms: RawSmsMessage): SmsParseOutcome {
        val ignoreReason = nonFinancialFilter.checkNonFinancial(rawSms.body)
        if (ignoreReason != null) {
            return SmsParseOutcome.IgnoredNonFinancial(ignoreReason)
        }

        val normalizedBody = PersianTextNormalizer.normalize(rawSms.body)

        val bank = bankDetector.detectBank(rawSms.sender, normalizedBody)
            ?: return SmsParseOutcome.UnrecognizedBank(rawSms.sender)

        val strategy = strategies.find { it.supportedBankCodes.contains(bank.code) } ?: fallbackStrategy

        val parsedData = strategy.parse(normalizedBody, rawSms, bank.code)
            ?: return SmsParseOutcome.ParseFailed(bank.code, "امکان استخراج اطلاعات تراکنش از متن پیامک وجود نداشت.")

        return SmsParseOutcome.Success(parsedData)
    }
}
