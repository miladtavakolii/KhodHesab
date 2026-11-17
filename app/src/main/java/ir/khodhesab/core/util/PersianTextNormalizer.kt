package ir.khodhesab.core.util

/**
 * Utility for normalizing Persian and Arabic text in SMS messages.
 *
 * Provides operations to convert Persian/Arabic digits to English digits,
 * normalize Arabic character variants to Persian equivalents, sanitize numeric amounts
 * by stripping thousand separators, and clean up whitespace and line breaks.
 */
object PersianTextNormalizer {

    private val PERSIAN_ARABIC_DIGITS_MAP = mapOf(
        '۰' to '0', '۱' to '1', '۲' to '2', '۳' to '3', '۴' to '4',
        '۵' to '5', '۶' to '6', '۷' to '7', '۸' to '8', '۹' to '9',
        '٠' to '0', '١' to '1', '٢' to '2', '٣' to '3', '٤' to '4',
        '٥' to '5', '٦' to '6', '٧' to '7', '٨' to '8', '٩' to '9'
    )

    /**
     * Converts all Persian (`۰-۹`) and Arabic (`٠-٩`) digits in [input] to English digits (`0-9`).
     *
     * @param input The raw input string containing potential Persian/Arabic digits.
     * @return String with all Persian and Arabic digits converted to English digits.
     */
    fun convertDigitsToEnglish(input: String): String {
        val builder = StringBuilder(input.length)
        for (char in input) {
            builder.append(PERSIAN_ARABIC_DIGITS_MAP[char] ?: char)
        }
        return builder.toString()
    }

    /**
     * Normalizes Arabic character variants into standard Persian equivalents.
     *
     * Replacements performed:
     * - `ي` (`\u064A`) -> `ی` (`\u06CC`)
     * - `ك` (`\u0643`) -> `ک` (`\u06A9`)
     * - `ة` (`\u0629`) -> `ه` (`\u0647`)
     *
     * @param input The input text containing Arabic character variants.
     * @return Text with normalized Persian characters.
     */
    fun normalizeCharacters(input: String): String {
        return input
            .replace('ي', 'ی')
            .replace('ك', 'ک')
            .replace('ة', 'ه')
    }

    /**
     * Removes thousand separators (`,`, `،`, `٬`) from numeric strings.
     *
     * @param input The string containing numbers with separators.
     * @return String with thousand separators removed between or adjacent to digits.
     */
    fun removeThousandSeparators(input: String): String {
        return input
            .replace(Regex("(?<=\\d)[,،٬](?=\\d)"), "")
            .replace(Regex("[,،٬]"), "")
    }

    /**
     * Extracts a clean [Long] financial amount from a text string.
     *
     * First converts Persian/Arabic digits to English digits and strips thousand separators,
     * then extracts the largest contiguous sequence of digits as a [Long].
     *
     * @param input The text containing a financial amount (e.g., `"۱,۲۵۰,۰۰۰ ریال"`).
     * @return Extracted [Long] amount or `null` if no valid digits exist or parsing fails.
     */
    fun extractAmount(input: String): Long? {
        val englishDigits = convertDigitsToEnglish(input)
        val cleanNumberString = removeThousandSeparators(englishDigits)
        val matchResult = Regex("\\d+").find(cleanNumberString) ?: return null
        return matchResult.value.toLongOrNull()
    }

    /**
     * Trims extra whitespace and standardizes newlines.
     *
     * Replaces `\r\n` and `\r` with `\n`, reduces multiple spaces or tabs on a single line to a single space,
     * trims leading/trailing spaces from each line, and trims the overall string.
     *
     * @param input The raw input string.
     * @return Cleaned string with uniform whitespace and line endings.
     */
    fun normalizeWhitespaceAndNewlines(input: String): String {
        return input
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .lines()
            .joinToString("\n") { line ->
                line.replace(Regex("[ \\t]+"), " ").trim()
            }
            .trim()
    }

    /**
     * Performs complete text normalization on SMS content.
     *
     * Applies character normalization, digit conversion, and whitespace standardization.
     *
     * @param input The raw SMS text.
     * @return Fully normalized SMS text string.
     */
    fun normalize(input: String): String {
        val step1 = normalizeCharacters(input)
        val step2 = convertDigitsToEnglish(step1)
        return normalizeWhitespaceAndNewlines(step2)
    }
}
