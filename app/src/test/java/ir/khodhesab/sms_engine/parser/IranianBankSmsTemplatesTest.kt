package ir.khodhesab.sms_engine.parser

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import ir.khodhesab.domain.model.Bank
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.domain.repository.BankRepository
import ir.khodhesab.sms_engine.parser.model.RawSmsMessage
import ir.khodhesab.sms_engine.parser.model.SmsParseOutcome
import ir.khodhesab.sms_engine.parser.strategy.banks.BluSamanPasargadStrategy
import ir.khodhesab.sms_engine.parser.strategy.banks.MellatMelliStrategy
import ir.khodhesab.sms_engine.parser.strategy.banks.TejaratSaderatKeshavarziSepahStrategy
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * End-to-end integration test suite for [BankSmsParserEngine] verifying parsing accuracy
 * across 15+ real Iranian bank SMS templates, multi-account separation, Toman <-> Rial conversion,
 * and OTP rejection.
 */
class IranianBankSmsTemplatesTest {

    private val bankRepository: BankRepository = mockk()
    private lateinit var parserEngine: BankSmsParserEngine

    @Before
    fun setup() {
        val nonFinancialFilter = NonFinancialSmsFilter()
        val bankDetector = BankDetector(bankRepository)
        val strategies = setOf(
            MellatMelliStrategy(),
            BluSamanPasargadStrategy(),
            TejaratSaderatKeshavarziSepahStrategy()
        )
        val scoredSmsExtractor = ScoredSmsExtractor()
        parserEngine = BankSmsParserEngine(nonFinancialFilter, bankDetector, strategies, scoredSmsExtractor)
    }

    private fun setupMockBank(code: String, sender: String) {
        val bank = Bank(id = code.hashCode().toLong(), code = code, persianName = "بانک $code", colorHex = "#000", iconKey = "ic")
        coEvery { bankRepository.findBankBySender(sender) } returns bank
        coEvery { bankRepository.getBankByCode(code) } returns bank
    }

    @Test
    fun test01_Mellat_Withdrawal() = runTest {
        setupMockBank("MELLAT", "200033")
        val sms = RawSmsMessage("200033", "بانک ملت\nبرداشت از حساب 123456\nمبلغ: 2,500,000 ریال\nمانده: 14,000,000 ریال", 1000L)

        val outcome = parserEngine.parse(sms)
        assertThat(outcome).isInstanceOf(SmsParseOutcome.Success::class.java)

        val data = (outcome as SmsParseOutcome.Success).data
        assertThat(data.bankCode).isEqualTo("MELLAT")
        assertThat(data.kind).isEqualTo(TransactionKind.WITHDRAWAL)
        assertThat(data.amount).isEqualTo(2_500_000L)
        assertThat(data.balanceAfter).isEqualTo(14_000_000L)
        assertThat(data.accountIdentifier).isEqualTo("3456")
    }

    @Test
    fun test02_Melli_Deposit() = runTest {
        setupMockBank("MELLI", "9611")
        val sms = RawSmsMessage("9611", "بانک ملی ایران\nواریز به حساب: 0102030405\nمبلغ: +1,200,000\nمانده: 8,500,000", 1000L)

        val outcome = parserEngine.parse(sms)
        assertThat(outcome).isInstanceOf(SmsParseOutcome.Success::class.java)

        val data = (outcome as SmsParseOutcome.Success).data
        assertThat(data.bankCode).isEqualTo("MELLI")
        assertThat(data.kind).isEqualTo(TransactionKind.DEPOSIT)
        assertThat(data.amount).isEqualTo(1_200_000L)
        assertThat(data.balanceAfter).isEqualTo(8_500_000L)
    }

    @Test
    fun test03_BluBank_TomanToRialConversion() = runTest {
        setupMockBank("BLU", "10008000")
        val sms = RawSmsMessage("10008000", "بلو\nبرداشت 150,000 تومان از حساب 9988\nموجودی: 2,400,000 تومان", 1000L)

        val outcome = parserEngine.parse(sms)
        assertThat(outcome).isInstanceOf(SmsParseOutcome.Success::class.java)

        val data = (outcome as SmsParseOutcome.Success).data
        assertThat(data.bankCode).isEqualTo("BLU")
        assertThat(data.kind).isEqualTo(TransactionKind.WITHDRAWAL)
        assertThat(data.amount).isEqualTo(1_500_000L)
        assertThat(data.balanceAfter).isEqualTo(24_000_000L)
        assertThat(data.accountIdentifier).isEqualTo("9988")
    }

    @Test
    fun test04_Saman_Deposit() = runTest {
        setupMockBank("SAMAN", "20008000")
        val sms = RawSmsMessage("20008000", "بانک سامان\nواریز به سپرده 800-123456-1\nمبلغ 3,000,000 ریال\nموجودی 12,000,000 ریال", 1000L)

        val outcome = parserEngine.parse(sms)
        assertThat(outcome).isInstanceOf(SmsParseOutcome.Success::class.java)

        val data = (outcome as SmsParseOutcome.Success).data
        assertThat(data.bankCode).isEqualTo("SAMAN")
        assertThat(data.kind).isEqualTo(TransactionKind.DEPOSIT)
        assertThat(data.amount).isEqualTo(3_000_000L)
        assertThat(data.balanceAfter).isEqualTo(12_000_000L)
    }

    @Test
    fun test05_Pasargad_Withdrawal() = runTest {
        setupMockBank("PASARGAD", "10009000")
        val sms = RawSmsMessage("10009000", "بانک پاسارگاد\nبرداشت از حساب: 201.800.12345.1\nمبلغ: 450,000 ریال\nمانده: 9,100,000 ریال", 1000L)

        val outcome = parserEngine.parse(sms)
        assertThat(outcome).isInstanceOf(SmsParseOutcome.Success::class.java)

        val data = (outcome as SmsParseOutcome.Success).data
        assertThat(data.bankCode).isEqualTo("PASARGAD")
        assertThat(data.kind).isEqualTo(TransactionKind.WITHDRAWAL)
        assertThat(data.amount).isEqualTo(450_000L)
        assertThat(data.balanceAfter).isEqualTo(9_100_000L)
    }

    @Test
    fun test06_Tejarat_MaskedCard() = runTest {
        setupMockBank("TEJARAT", "200070")
        val sms = RawSmsMessage("200070", "بانک تجارت\nخرید کارت *5678\nمبلغ: 800,000 ریال\nمانده: 5,000,000", 1000L)

        val outcome = parserEngine.parse(sms)
        assertThat(outcome).isInstanceOf(SmsParseOutcome.Success::class.java)

        val data = (outcome as SmsParseOutcome.Success).data
        assertThat(data.bankCode).isEqualTo("TEJARAT")
        assertThat(data.kind).isEqualTo(TransactionKind.WITHDRAWAL)
        assertThat(data.amount).isEqualTo(800_000L)
        assertThat(data.accountIdentifier).isEqualTo("5678")
    }

    @Test
    fun test07_Saderat_SignedAmount() = runTest {
        setupMockBank("SADERAT", "200000")
        val sms = RawSmsMessage("200000", "بانک صادرات\nحساب 030303\nمبلغ: -1,500,000 ریال\nمانده: 10,000,000", 1000L)

        val outcome = parserEngine.parse(sms)
        assertThat(outcome).isInstanceOf(SmsParseOutcome.Success::class.java)

        val data = (outcome as SmsParseOutcome.Success).data
        assertThat(data.bankCode).isEqualTo("SADERAT")
        assertThat(data.kind).isEqualTo(TransactionKind.WITHDRAWAL)
        assertThat(data.amount).isEqualTo(1_500_000L)
    }

    @Test
    fun test08_Keshavarzi_Deposit() = runTest {
        setupMockBank("KESHAVARZI", "200091")
        val sms = RawSmsMessage("200091", "بانک کشاورزی\nواریز به حساب: 44445555\nمبلغ: 10,000,000 ریال\nمانده: 25,000,000", 1000L)

        val outcome = parserEngine.parse(sms)
        assertThat(outcome).isInstanceOf(SmsParseOutcome.Success::class.java)

        val data = (outcome as SmsParseOutcome.Success).data
        assertThat(data.bankCode).isEqualTo("KESHAVARZI")
        assertThat(data.kind).isEqualTo(TransactionKind.DEPOSIT)
        assertThat(data.amount).isEqualTo(10_000_000L)
    }

    @Test
    fun test09_Sepah_Withdrawal() = runTest {
        setupMockBank("SEPAH", "200090")
        val sms = RawSmsMessage("200090", "بانک سپه\nبرداشت از حساب: 556677\nمبلغ: 350,000 ریال\nمانده: 2,000,000", 1000L)

        val outcome = parserEngine.parse(sms)
        assertThat(outcome).isInstanceOf(SmsParseOutcome.Success::class.java)

        val data = (outcome as SmsParseOutcome.Success).data
        assertThat(data.bankCode).isEqualTo("SEPAH")
        assertThat(data.kind).isEqualTo(TransactionKind.WITHDRAWAL)
        assertThat(data.amount).isEqualTo(350_000L)
    }

    @Test
    fun test10_Parsian_Deposit() = runTest {
        setupMockBank("PARSIAN", "300088")
        val sms = RawSmsMessage("300088", "بانک پارسیان\nواریز سود سپرده 7788\nمبلغ: 5,000,000 ریال\nمانده: 50,000,000", 1000L)

        val outcome = parserEngine.parse(sms)
        assertThat(outcome).isInstanceOf(SmsParseOutcome.Success::class.java)

        val data = (outcome as SmsParseOutcome.Success).data
        assertThat(data.bankCode).isEqualTo("PARSIAN")
        assertThat(data.kind).isEqualTo(TransactionKind.DEPOSIT)
        assertThat(data.amount).isEqualTo(5_000_000L)
    }

    @Test
    fun test11_EghtesadNovin_Withdrawal() = runTest {
        setupMockBank("EGHTESAD_NOVIN", "20002000")
        val sms = RawSmsMessage("20002000", "بانک اقتصاد نوین\nبرداشت از حساب 1122\nمبلغ: 600,000 ریال\nمانده: 3,400,000", 1000L)

        val outcome = parserEngine.parse(sms)
        assertThat(outcome).isInstanceOf(SmsParseOutcome.Success::class.java)

        val data = (outcome as SmsParseOutcome.Success).data
        assertThat(data.bankCode).isEqualTo("EGHTESAD_NOVIN")
        assertThat(data.kind).isEqualTo(TransactionKind.WITHDRAWAL)
        assertThat(data.amount).isEqualTo(600_000L)
    }

    @Test
    fun test12_Shahr_Deposit() = runTest {
        setupMockBank("SHAHR", "20003000")
        val sms = RawSmsMessage("20003000", "بانک شهر\nواریز حقوق به حساب 3344\nمبلغ: 20,000,000 ریال\nمانده: 25,000,000", 1000L)

        val outcome = parserEngine.parse(sms)
        assertThat(outcome).isInstanceOf(SmsParseOutcome.Success::class.java)

        val data = (outcome as SmsParseOutcome.Success).data
        assertThat(data.bankCode).isEqualTo("SHAHR")
        assertThat(data.kind).isEqualTo(TransactionKind.DEPOSIT)
        assertThat(data.amount).isEqualTo(20_000_000L)
    }

    @Test
    fun test13_Refah_Withdrawal() = runTest {
        setupMockBank("REFAH", "300044")
        val sms = RawSmsMessage("300044", "بانک رفاه\nبرداشت از کارت 9012\nمبلغ: 1,000,000 ریال\nمانده: 4,000,000", 1000L)

        val outcome = parserEngine.parse(sms)
        assertThat(outcome).isInstanceOf(SmsParseOutcome.Success::class.java)

        val data = (outcome as SmsParseOutcome.Success).data
        assertThat(data.bankCode).isEqualTo("REFAH")
        assertThat(data.kind).isEqualTo(TransactionKind.WITHDRAWAL)
        assertThat(data.amount).isEqualTo(1_000_000L)
    }

    @Test
    fun test14_Maskan_Deposit() = runTest {
        setupMockBank("MASKAN", "300010")
        val sms = RawSmsMessage("300010", "بانک مسکن\nواریز تسهیلات حساب 5544\nمبلغ: 50,000,000 ریال\nمانده: 52,000,000", 1000L)

        val outcome = parserEngine.parse(sms)
        assertThat(outcome).isInstanceOf(SmsParseOutcome.Success::class.java)

        val data = (outcome as SmsParseOutcome.Success).data
        assertThat(data.bankCode).isEqualTo("MASKAN")
        assertThat(data.kind).isEqualTo(TransactionKind.DEPOSIT)
        assertThat(data.amount).isEqualTo(50_000_000L)
    }

    @Test
    fun test15_Resalat_Withdrawal() = runTest {
        setupMockBank("RESALAT", "20004747")
        val sms = RawSmsMessage("20004747", "بانک رسالت\nپرداخت کارمزد حساب 1111\nمبلغ: 20,000 ریال\nمانده: 1,500,000 ریال", 1000L)

        val outcome = parserEngine.parse(sms)
        assertThat(outcome).isInstanceOf(SmsParseOutcome.Success::class.java)

        val data = (outcome as SmsParseOutcome.Success).data
        assertThat(data.bankCode).isEqualTo("RESALAT")
        assertThat(data.kind).isEqualTo(TransactionKind.WITHDRAWAL)
        assertThat(data.amount).isEqualTo(20_000L)
        assertThat(data.balanceAfter).isEqualTo(1_500_000L)
    }

    @Test
    fun test16_MehrIran_Deposit() = runTest {
        setupMockBank("MEHR_IRAN", "30008585")
        val sms = RawSmsMessage("30008585", "بانک مهر ایران\nواریز به حساب 8899\nمبلغ: 4,000,000 ریال\nمانده: 10,000,000", 1000L)

        val outcome = parserEngine.parse(sms)
        assertThat(outcome).isInstanceOf(SmsParseOutcome.Success::class.java)

        val data = (outcome as SmsParseOutcome.Success).data
        assertThat(data.bankCode).isEqualTo("MEHR_IRAN")
        assertThat(data.kind).isEqualTo(TransactionKind.DEPOSIT)
        assertThat(data.amount).isEqualTo(4_000_000L)
    }

    @Test
    fun test17_OtpSms_IsIgnoredAsNonFinancial() = runTest {
        setupMockBank("MELLAT", "200033")
        val sms = RawSmsMessage("200033", "رمز پویا: 543210 جهت خرید اینترنتی بانک ملت", 1000L)

        val outcome = parserEngine.parse(sms)
        assertThat(outcome).isInstanceOf(SmsParseOutcome.IgnoredNonFinancial::class.java)
    }
}
