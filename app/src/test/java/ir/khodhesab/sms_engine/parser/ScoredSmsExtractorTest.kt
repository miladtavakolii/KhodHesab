package ir.khodhesab.sms_engine.parser

import com.google.common.truth.Truth.assertThat
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.sms_engine.parser.model.RawSmsMessage
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [ScoredSmsExtractor].
 */
class ScoredSmsExtractorTest {

    private lateinit var extractor: ScoredSmsExtractor

    @Before
    fun setup() {
        extractor = ScoredSmsExtractor()
    }

    @Test
    fun parse_extractsAmountAndBalanceWithTomanConversion() {
        val body = "برداشت از حساب *1234\nمبلغ: 150,000 تومان\nمانده: 5.000.000 تومان"
        val rawSms = RawSmsMessage("200033", body, 1700000000000L)

        val parsed = extractor.parse("MELLAT", body, rawSms)

        assertThat(parsed).isNotNull()
        assertThat(parsed?.amount).isEqualTo(1_500_000L)
        assertThat(parsed?.balanceAfter).isEqualTo(50_000_000L)
        assertThat(parsed?.kind).isEqualTo(TransactionKind.WITHDRAWAL)
        assertThat(parsed?.accountIdentifier).isEqualTo("1234")
    }

    @Test
    fun parse_handlesTrailingMinusSignForWithdrawal() {
        val body = "حساب 800-123456-1\n1,500,000- ریال\nمانده: 10,000,000"
        val rawSms = RawSmsMessage("2000", body, 1700000000000L)

        val parsed = extractor.parse("MELLI", body, rawSms)

        assertThat(parsed).isNotNull()
        assertThat(parsed?.amount).isEqualTo(1_500_000L)
        assertThat(parsed?.kind).isEqualTo(TransactionKind.WITHDRAWAL)
    }

    @Test
    fun parse_ignoresDatesTimesAndMaskedCardsAsAmountCandidates() {
        val body = "تاریخ 1405/08/26 زمان 14:30\nکارت 6037****1234\nواریز: 2,000,000 ریال\nمانده: 5,000,000 ریال"
        val rawSms = RawSmsMessage("9611", body, 1700000000000L)

        val parsed = extractor.parse("MELLI", body, rawSms)

        assertThat(parsed).isNotNull()
        assertThat(parsed?.amount).isEqualTo(2_000_000L)
        assertThat(parsed?.kind).isEqualTo(TransactionKind.DEPOSIT)
        assertThat(parsed?.accountIdentifier).isEqualTo("1234")
    }

    @Test
    fun parse_rejectsSmsWithoutBalanceKeyword() {
        val body = "تخفیف ویژه خرید بالای 500,000 تومان از فروشگاه..."
        val rawSms = RawSmsMessage("PROMO", body, 1700000000000L)

        val parsed = extractor.parse("OTHER", body, rawSms)

        assertThat(parsed).isNull()
    }

    @Test
    fun parse_acceptsGenuineBankSmsWithBalanceFromOtherBank() {
        val body = "واریز مبلغ 3,500,000 ریال به حساب 4321\nمانده: 18,500,000 ریال"
        val rawSms = RawSmsMessage("+989120000000", body, 1700000000000L)

        val parsed = extractor.parse("OTHER", body, rawSms)

        assertThat(parsed).isNotNull()
        assertThat(parsed?.amount).isEqualTo(3_500_000L)
        assertThat(parsed?.balanceAfter).isEqualTo(18_500_000L)
        assertThat(parsed?.bankCode).isEqualTo("OTHER")
    }
}
