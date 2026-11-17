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
    fun normalizeCharacters_convertsTehMarbutaToHeh() {
        val input = "خاطرة"
        val result = PersianTextNormalizer.normalizeCharacters(input)
        assertThat(result).isEqualTo("خاطره")
    }

    @Test
    fun removeThousandSeparators_removesVariousSeparators() {
        val input = "1,250,000 و ۱،۵۰۰،۰۰۰ و ۱۰٬۰۰۰"
        val result = PersianTextNormalizer.removeThousandSeparators(input)
        assertThat(result).isEqualTo("1250000 و ۱۵۰۰۰۰۰ و ۱۰۰۰۰")
    }

    @Test
    fun extractAmount_extractsValidAmountFromPersianRialString() {
        val input = "برداشت مبلغ ۱,۲۵۰,۰۰۰ ریال از حساب"
        val amount = PersianTextNormalizer.extractAmount(input)
        assertThat(amount).isEqualTo(1250000L)
    }

    @Test
    fun extractAmount_returnsNullForInvalidOrEmptyString() {
        assertThat(PersianTextNormalizer.extractAmount("هیچ عددی نیست")).isNull()
        assertThat(PersianTextNormalizer.extractAmount("")).isNull()
    }

    @Test
    fun normalizeWhitespaceAndNewlines_trimsAndStandardizesNewlines() {
        val input = "  سطر اول   \r\n\r\n   سطر دوم    \r   "
        val result = PersianTextNormalizer.normalizeWhitespaceAndNewlines(input)
        assertThat(result).isEqualTo("سطر اول\n\nسطر دوم")
    }

    @Test
    fun normalize_executesEndToEndNormalization() {
        val rawSms = "  بانك ملي\r\nمبلغ: ۱,۵۰۰,۰۰۰ ريال  "
        val normalized = PersianTextNormalizer.normalize(rawSms)
        assertThat(normalized).isEqualTo("بانک ملی\nمبلغ: 1,500,000 ریال")
    }
}
