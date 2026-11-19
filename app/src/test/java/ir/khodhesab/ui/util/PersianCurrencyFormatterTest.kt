package ir.khodhesab.ui.util

import com.google.common.truth.Truth.assertThat
import ir.khodhesab.domain.model.CurrencyUnit
import ir.khodhesab.domain.model.TransactionKind
import org.junit.Test

/**
 * Unit tests for [PersianCurrencyFormatter].
 */
class PersianCurrencyFormatterTest {

    @Test
    fun toPersianDigits_convertsEnglishDigitsToPersian() {
        val input = "Total: 1234567890"
        val output = PersianCurrencyFormatter.toPersianDigits(input)
        assertThat(output).isEqualTo("Total: ۱۲۳۴۵۶۷۸۹۰")
    }

    @Test
    fun formatAmount_convertsRialsToTomanCorrectly() {
        val rials = 1_500_000L
        val formattedToman = PersianCurrencyFormatter.formatAmount(rials, unit = CurrencyUnit.TOMAN)
        val formattedRial = PersianCurrencyFormatter.formatAmount(rials, unit = CurrencyUnit.RIAL)

        assertThat(formattedToman).isEqualTo("۱۵۰,۰۰۰ تومان")
        assertThat(formattedRial).isEqualTo("۱,۵۰۰,۰۰۰ ریال")
    }

    @Test
    fun formatSignedAmount_attachesPlusAndMinusSigns() {
        val deposit = PersianCurrencyFormatter.formatSignedAmount(100_000L, TransactionKind.DEPOSIT, CurrencyUnit.TOMAN)
        val withdrawal = PersianCurrencyFormatter.formatSignedAmount(50_000L, TransactionKind.WITHDRAWAL, CurrencyUnit.TOMAN)

        assertThat(deposit).isEqualTo("+۱۰,۰۰۰ تومان")
        assertThat(withdrawal).isEqualTo("-۵,۰۰۰ تومان")
    }

    @Test
    fun formatCompact_formatsMillionsAndThousandsInPersian() {
        val compactMillion = PersianCurrencyFormatter.formatCompact(15_000_000L, CurrencyUnit.TOMAN)
        assertThat(compactMillion).contains("میلیون")
        assertThat(compactMillion).contains("۱")
    }
}
