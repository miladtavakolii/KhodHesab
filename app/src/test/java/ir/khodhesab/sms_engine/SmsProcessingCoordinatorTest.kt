package ir.khodhesab.sms_engine

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.domain.usecase.RecordSmsResult
import ir.khodhesab.domain.usecase.RecordSmsTransactionUseCase
import ir.khodhesab.sms_engine.parser.BankSmsParserEngine
import ir.khodhesab.sms_engine.parser.model.ParsedSmsData
import ir.khodhesab.sms_engine.parser.model.RawSmsMessage
import ir.khodhesab.sms_engine.parser.model.SmsParseOutcome
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [SmsProcessingCoordinator].
 */
class SmsProcessingCoordinatorTest {

    private val parserEngine: BankSmsParserEngine = mockk()
    private val recordSmsUseCase: RecordSmsTransactionUseCase = mockk()
    private lateinit var coordinator: SmsProcessingCoordinator

    @Before
    fun setup() {
        coordinator = SmsProcessingCoordinator(parserEngine, recordSmsUseCase)
    }

    @Test
    fun processSms_returnsRecordedWhenParseAndRecordSucceed() = runTest {
        val rawSms = RawSmsMessage("200033", "برداشت از حساب 1234\nمبلغ: 100,000 ریال\nمانده: 500,000 ریال", 1000L)
        val parsedData = ParsedSmsData("MELLAT", "1234", TransactionKind.WITHDRAWAL, 100_000L, 500_000L, 1000L, rawSms.body, "HASH_1")

        coEvery { parserEngine.parse(rawSms) } returns SmsParseOutcome.Success(parsedData)
        coEvery { recordSmsUseCase("200033", "1234", TransactionKind.WITHDRAWAL, 100_000L, 500_000L, 1000L, rawSms.body, "HASH_1") } returns RecordSmsResult.Inserted(50L)

        val result = coordinator.processSms(rawSms)
        assertThat(result).isInstanceOf(SmsProcessResult.Recorded::class.java)

        val recorded = result as SmsProcessResult.Recorded
        assertThat(recorded.transactionId).isEqualTo(50L)
        assertThat(recorded.bankCode).isEqualTo("MELLAT")
        assertThat(recorded.amount).isEqualTo(100_000L)
    }

    @Test
    fun processSms_returnsIgnoredWhenOtpDetected() = runTest {
        val rawSms = RawSmsMessage("9611", "رمز پویا: 123456", 1000L)
        coEvery { parserEngine.parse(rawSms) } returns SmsParseOutcome.IgnoredNonFinancial("رمز پویا")

        val result = coordinator.processSms(rawSms)
        assertThat(result).isInstanceOf(SmsProcessResult.Ignored::class.java)
    }
}
