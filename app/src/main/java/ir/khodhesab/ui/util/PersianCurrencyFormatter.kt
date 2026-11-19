package ir.khodhesab.ui.util

import ir.khodhesab.domain.model.CurrencyUnit
import ir.khodhesab.domain.model.TransactionKind
import kotlin.math.abs

/**
 * Utility for formatting internal Rial amounts into Persian-digit currency strings.
 */
object PersianCurrencyFormatter {

    private val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    /**
     * Converts all English digits in [input] string to Persian digits (`۰-۹`).
     *
     * @param input Raw string containing English digits.
     * @return String with English digits replaced by Persian digits.
     */
    fun toPersianDigits(input: String): String {
        return input.map { char ->
            if (char in '0'..'9') PERSIAN_DIGITS[char - '0'] else char
        }.joinToString("")
    }

    /**
     * Formats an internal Rial amount into a Persian currency string based on [unit].
     *
     * @param amountRials Raw amount in Rials stored in database.
     * @param unit Target [CurrencyUnit] (`TOMAN` or `RIAL`).
     * @param includeUnitSuffix If `true`, appends `"تومان"` or `"ریال"` suffix.
     * @return Formatted Persian currency string (e.g. `"۱۵۰,۰۰۰ تومان"`).
     */
    fun formatAmount(
        amountRials: Long,
        unit: CurrencyUnit = CurrencyUnit.TOMAN,
        includeUnitSuffix: Boolean = true
    ): String {
        val displayAmount = when (unit) {
            CurrencyUnit.TOMAN -> amountRials / 10L
            CurrencyUnit.RIAL -> amountRials
        }

        val absAmount = abs(displayAmount)
        val formattedNumber = toPersianDigits("%1$,d".format(absAmount))
        val sign = if (displayAmount < 0) "-" else ""

        val suffix = if (includeUnitSuffix) {
            when (unit) {
                CurrencyUnit.TOMAN -> " تومان"
                CurrencyUnit.RIAL -> " ریال"
            }
        } else {
            ""
        }

        return "$sign$formattedNumber$suffix"
    }

    /**
     * Formats a transaction amount with signed indicator prefix (`+` for DEPOSIT, `-` for WITHDRAWAL).
     *
     * @param amountRials Transaction amount in Rials.
     * @param kind [TransactionKind] of the transaction.
     * @param unit Target [CurrencyUnit].
     * @return Formatted signed string (e.g. `"+۱۵۰,۰۰۰ تومان"` or `"-۵۰,۰۰۰ تومان"`).
     */
    fun formatSignedAmount(
        amountRials: Long,
        kind: TransactionKind,
        unit: CurrencyUnit = CurrencyUnit.TOMAN
    ): String {
        val baseText = formatAmount(amountRials, unit, includeUnitSuffix = true)
        return when (kind) {
            TransactionKind.DEPOSIT -> "+$baseText"
            TransactionKind.WITHDRAWAL -> "-$baseText"
            TransactionKind.TRANSFER -> baseText
        }
    }

    /**
     * Formats large amounts into compact Persian representation for chart axes or summary cards
     * (e.g. `"۱.۵ میلیون"`).
     *
     * @param amountRials Amount in Rials.
     * @param unit Target [CurrencyUnit].
     * @return Compact Persian formatted string.
     */
    fun formatCompact(amountRials: Long, unit: CurrencyUnit = CurrencyUnit.TOMAN): String {
        val displayAmount = when (unit) {
            CurrencyUnit.TOMAN -> amountRials / 10L
            CurrencyUnit.RIAL -> amountRials
        }

        val absAmount = abs(displayAmount)
        val suffixUnit = when (unit) {
            CurrencyUnit.TOMAN -> "تومان"
            CurrencyUnit.RIAL -> "ریال"
        }

        return when {
            absAmount >= 1_000_000_000L -> {
                val billion = absAmount.toDouble() / 1_000_000_000.0
                toPersianDigits("%.1f میلیارد %s".format(billion, suffixUnit))
            }
            absAmount >= 1_000_000L -> {
                val million = absAmount.toDouble() / 1_000_000.0
                toPersianDigits("%.1f میلیون %s".format(million, suffixUnit))
            }
            absAmount >= 1_000L -> {
                val thousand = absAmount.toDouble() / 1_000.0
                toPersianDigits("%.0f هزار %s".format(thousand, suffixUnit))
            }
            else -> {
                formatAmount(amountRials, unit, includeUnitSuffix = true)
            }
        }
    }
}
