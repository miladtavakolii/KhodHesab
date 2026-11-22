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

    @Test
    fun checkNonFinancial_rejectsPromotionalSmsWithAmountAndBuyKeyword() {
        val promoBody = "تخفیف ویژه خرید بالای 500,000 تومان از فروشگاه..."
        val result = filter.checkNonFinancial(promoBody)

        assertThat(result).isNotNull()
        assertThat(result).contains("Missing balance keyword")
    }

    @Test
    fun checkNonFinancial_rejectsOperatorSmsWithAmountAndPayKeyword() {
        val operatorBody = "مشترک گرامی قبض 250,000 ریال پرداخت شد. لغو 11"
        val result = filter.checkNonFinancial(operatorBody)

        assertThat(result).isNotNull()
    }

    @Test
    fun checkNonFinancial_rejectsDeliveryInvoiceSmsWithoutBalanceKeyword() {
        val deliveryBody = "فاکتور سفارش شما به مبلغ 1,200,000 ریال صادر شد."
        val result = filter.checkNonFinancial(deliveryBody)

        assertThat(result).isNotNull()
        assertThat(result).contains("Missing balance keyword")
    }

    @Test
    fun checkNonFinancial_acceptsGenuineBankSmsWithBalanceFromUnknownSender() {
        val unknownBankSms = "واریز 3,500,000 ریال به حساب 4321\nمانده: 18,500,000 ریال"
        val result = filter.checkNonFinancial(unknownBankSms)

        assertThat(result).isNull()
    }
}
