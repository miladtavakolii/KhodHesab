package ir.khodhesab.sms_engine.parser

import com.google.common.truth.Truth.assertThat
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.sms_engine.parser.model.RawSmsMessage
import org.junit.Before
import org.junit.Test

/**
 * Integration tests verifying real-world messy, unseen Iranian bank SMS formats using context-window scoring.
 */
class RealWorldMessySmsTest {

    private lateinit var extractor: ScoredSmsExtractor

    @Before
    fun setup() {
        extractor = ScoredSmsExtractor()
    }

    @Test
    fun parse_unseenNeobankSmsWithInvisibleUnicode_extractsAccurately() {
        val body = "\u200Eانتقال به حساب شما\u200F\nمبلغ: \uFEFF3,500,000 تومان\nموجودى: 12.000.000 تومان"
        val rawSms = RawSmsMessage("+98 5000 9999", body, 1700000000000L)

        val parsed = extractor.parse("OTHER", body, rawSms)

        assertThat(parsed).isNotNull()
        assertThat(parsed?.amount).isEqualTo(35_000_000L)
        assertThat(parsed?.kind).isEqualTo(TransactionKind.DEPOSIT)
        assertThat(parsed?.bankCode).isEqualTo("OTHER")
    }

    @Test
    fun parse_smsContainingBothWithdrawalAndDepositKeywords_resolvesKindByProximity() {
        val body = "انتقال از حساب 4433\nپرداخت وجه بابت برداشت\nمبلغ: 800,000 ریال\nموجودی جدید: 5,000,000"
        val rawSms = RawSmsMessage("200040", body, 1700000000000L)

        val parsed = extractor.parse("SINA", body, rawSms)

        assertThat(parsed).isNotNull()
        assertThat(parsed?.amount).isEqualTo(800_000L)
        assertThat(parsed?.kind).isEqualTo(TransactionKind.WITHDRAWAL)
    }
}
