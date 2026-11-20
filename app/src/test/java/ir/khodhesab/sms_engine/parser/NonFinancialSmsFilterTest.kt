package ir.khodhesab.sms_engine.parser

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [NonFinancialSmsFilter].
 */
class NonFinancialSmsFilterTest {

    private lateinit var filter: NonFinancialSmsFilter

    @Before
    fun setup() {
        filter = NonFinancialSmsFilter()
    }

    @Test
    fun checkNonFinancial_rejectsOtpMessages() {
        val otpBody = "رمز پویا: ۱۲۳۴۵۶ جهت خرید اینترنتی"
        val result = filter.checkNonFinancial(otpBody)

        assertThat(result).isNotNull()
        assertThat(result).contains("رمز پویا")
    }

    @Test
    fun checkNonFinancial_rejectsSecurityAlerts() {
        val alertBody = "ورود به همراه بانک ملت انجام شد."
        val result = filter.checkNonFinancial(alertBody)

        assertThat(result).isNotNull()
        assertThat(result).contains("امنیتی")
    }

    @Test
    fun checkNonFinancial_rejectsFailedTransactionNotices() {
        val failedBody = "تراکنش برداشت 500,000 ریال ناموفق بود."
        val result = filter.checkNonFinancial(failedBody)

        assertThat(result).isNotNull()
        assertThat(result).contains("ناموفق")
    }

    @Test
    fun checkNonFinancial_acceptsValidTransactionMessage() {
        val txBody = "برداشت از حساب 123456\nمبلغ: 2,500,000 ریال\nمانده: 14,000,000 ریال"
        val result = filter.checkNonFinancial(txBody)

        assertThat(result).isNull()
    }
}
