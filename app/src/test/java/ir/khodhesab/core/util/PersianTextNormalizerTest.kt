package ir.khodhesab.core.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Comprehensive unit tests for [PersianTextNormalizer].
 */
class PersianTextNormalizerTest {

    @Test
    fun convertDigitsToEnglish_convertsPersianDigitsCorrectly() {
        val input = "۰۱۲۳۴۵۶۷۸۹"
        val result = PersianTextNormalizer.convertDigitsToEnglish(input)
        assertThat(result).isEqualTo("0123456789")
    }

    @Test
    fun convertDigitsToEnglish_convertsArabicDigitsCorrectly() {
        val input = "٠١٢٣٤٥٦٧٨٩"
        val result = PersianTextNormalizer.convertDigitsToEnglish(input)
        assertThat(result).isEqualTo("0123456789")
    }

    @Test
    fun convertDigitsToEnglish_handlesMixedText() {
        val input = "مبلغ: ۱,۲۵۰,۰۰۰ ریال (کد: ٠١٢)"
        val result = PersianTextNormalizer.convertDigitsToEnglish(input)
        assertThat(result).isEqualTo("مبلغ: 1,250,000 ریال (کد: 012)")
    }

    @Test
    fun normalizeCharacters_convertsArabicVariantsToPersian() {
        val input = "بانك ملي - برداشت يك ميليون تومان"
        val result = PersianTextNormalizer.normalizeCharacters(input)
        assertThat(result).isEqualTo("بانک ملی - برداشت یک میلیون تومان")
    }

    @Test
    fun stripControlCharacters_removesInvisibleUnicodeAndReplacesZwnjWithSpace() {
        val input = "بانک\u200Cملی\u200E \u200F۱,۵۰۰,۰۰۰\uFEFF"
        val result = PersianTextNormalizer.stripControlCharacters(input)
        assertThat(result).isEqualTo("بانک ملی ۱,۵۰۰,۰۰۰")
    }

    @Test
    fun convertDotSeparatedThousands_convertsDotsToCommas() {
        val input = "مبلغ 1.500.000 ریال"
        val result = PersianTextNormalizer.convertDotSeparatedThousands(input)
        assertThat(result).isEqualTo("مبلغ 1,500,000 ریال")
    }

    @Test
    fun canonicalSender_normalizesNumericAndAlphanumericSendersEquivalently() {
        assertThat(PersianTextNormalizer.canonicalSender("+98 2000 1234")).isEqualTo("20001234")
        assertThat(PersianTextNormalizer.canonicalSender("009820001234")).isEqualTo("20001234")
        assertThat(PersianTextNormalizer.canonicalSender("020001234")).isEqualTo("20001234")
        assertThat(PersianTextNormalizer.canonicalSender("20001234")).isEqualTo("20001234")
        assertThat(PersianTextNormalizer.canonicalSender("BankMellat")).isEqualTo("bankmellat")
    }

    @Test
    fun normalize_executesEndToEndNormalization() {
        val rawSms = "  بانك ملي\u200E\r\nمبلغ: ۱.۵۰۰.۰۰۰ ريال  "
        val normalized = PersianTextNormalizer.normalize(rawSms)
        assertThat(normalized).isEqualTo("بانک ملی\nمبلغ: 1,500,000 ریال")
    }
}
