package ir.khodhesab.sms_engine.parser.strategy

import com.google.common.truth.Truth.assertThat
import ir.khodhesab.domain.model.TransactionKind
import org.junit.Test

/**
 * Unit tests for [BaseRegexExtractor].
 */
class BaseRegexExtractorTest {

    @Test
    fun detectTransactionKind_identifiesWithdrawalAndDepositByKeywords() {
        val withdrawalText = "برداشت از حساب 1234\nمبلغ: 50,000\nمانده: 100,000"
        val depositText = "واریز به حساب 1234\nمبلغ: 100,000\nمانده: 200,000"

        assertThat(BaseRegexExtractor.detectTransactionKind(withdrawalText)).isEqualTo(TransactionKind.WITHDRAWAL)
        assertThat(BaseRegexExtractor.detectTransactionKind(depositText)).isEqualTo(TransactionKind.DEPOSIT)
    }

    @Test
    fun detectTransactionKind_identifiesKindByPlusAndMinusSigns() {
        val minusText = "مبلغ: 1,200,000-\nمانده: 5,000,000"
        val plusText = "مبلغ: +500,000\nمانده: 5,000,000"

        assertThat(BaseRegexExtractor.detectTransactionKind(minusText)).isEqualTo(TransactionKind.WITHDRAWAL)
        assertThat(BaseRegexExtractor.detectTransactionKind(plusText)).isEqualTo(TransactionKind.DEPOSIT)
    }

    @Test
    fun extractAmountInRials_convertsTomanToRial() {
        val tomanSms = "بلو\nبرداشت 150,000 تومان از حساب\nموجودی: 500,000 تومان"
        val rialSms = "مبلغ: 1,500,000 ریال\nمانده: 5,000,000 ریال"

        val amountTomanInRial = BaseRegexExtractor.extractAmountInRials(tomanSms)
        val amountRialInRial = BaseRegexExtractor.extractAmountInRials(rialSms)

        assertThat(amountTomanInRial).isEqualTo(1_500_000L)
        assertThat(amountRialInRial).isEqualTo(1_500_000L)
    }

    @Test
    fun extractAccountIdentifier_extractsLast4DigitsCleanly() {
        val text1 = "برداشت از حساب 0102030405"
        val text2 = "کارت 6037****1234"

        assertThat(BaseRegexExtractor.extractAccountIdentifier(text1)).isEqualTo("0405")
        assertThat(BaseRegexExtractor.extractAccountIdentifier(text2)).isEqualTo("1234")
    }
}
