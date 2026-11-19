package ir.khodhesab.sms_engine

import ir.khodhesab.domain.usecase.RecordSmsResult
import ir.khodhesab.domain.usecase.RecordSmsTransactionUseCase
import ir.khodhesab.sms_engine.parser.BankSmsParserEngine
import ir.khodhesab.sms_engine.parser.model.RawSmsMessage
import ir.khodhesab.sms_engine.parser.model.SmsParseOutcome
import javax.inject.Inject
import javax.inject.Singleton

/**
 * High-level coordinator that processes incoming raw SMS messages through the parser engine
 * and records them via [RecordSmsTransactionUseCase].
 */
@Singleton
class SmsProcessingCoordinator @Inject constructor(
    private val parserEngine: BankSmsParserEngine,
    private val recordSmsTransactionUseCase: RecordSmsTransactionUseCase
) {

    /**
     * Processes a single raw SMS message.
     *
     * @param rawSms Raw incoming SMS message.
     * @return [SmsProcessResult] indicating final outcome.
     */
    suspend fun processSms(rawSms: RawSmsMessage): SmsProcessResult {
        return when (val parseOutcome = parserEngine.parse(rawSms)) {
            is SmsParseOutcome.IgnoredNonFinancial -> SmsProcessResult.Ignored(parseOutcome.reason)
            is SmsParseOutcome.UnrecognizedBank -> SmsProcessResult.Unrecognized(parseOutcome.sender)
            is SmsParseOutcome.ParseFailed -> SmsProcessResult.Failed(parseOutcome.reason)
            is SmsParseOutcome.Success -> {
                val data = parseOutcome.data
                when (val recordResult = recordSmsTransactionUseCase(
                    senderNumber = rawSms.sender,
                    accountIdentifier = data.accountIdentifier,
                    kind = data.kind,
                    amount = data.amount,
                    balanceAfter = data.balanceAfter,
                    timestamp = data.timestamp,
                    rawSmsBody = data.rawBody,
                    smsHash = data.smsHash
                )) {
                    is RecordSmsResult.Inserted -> SmsProcessResult.Recorded(recordResult.transactionId, data.bankCode, data.amount)
                    is RecordSmsResult.DuplicateSkipped -> SmsProcessResult.Duplicate
                    is RecordSmsResult.BankNotRecognized -> SmsProcessResult.Unrecognized(rawSms.sender)
                }
            }
        }
    }

    /**
     * Batch processes multiple raw SMS messages.
     *
     * @param rawMessages List of raw SMS messages.
     * @return List of [SmsProcessResult] for each message.
     */
    suspend fun processBatch(rawMessages: List<RawSmsMessage>): List<SmsProcessResult> {
        return rawMessages.map { processSms(it) }
    }
}
