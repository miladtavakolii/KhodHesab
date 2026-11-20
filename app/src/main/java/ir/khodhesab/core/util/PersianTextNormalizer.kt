package ir.khodhesab.core.util

/**
 * Utility for normalizing Persian and Arabic text in SMS messages and canonicalizing sender IDs.
 *
 * Provides operations to convert Persian/Arabic digits to English digits,
 * normalize Arabic character variants to Persian equivalents, strip invisible Unicode control characters,
 * unify separators, sanitize numeric amounts, and canonicalize phone numbers / alphanumeric senders.
 */
object PersianTextNormalizer {

    private val PERSIAN_ARABIC_DIGITS_MAP = mapOf(
        '۰' to '0', '۱' to '1', '۲' to '2', '۳' to '3', '۴' to '4',
        '۵' to '5', '۶' to '6', '۷' to '7', '۸' to '8', '۹' to '9',
        '٠' to '0', '١' to '1', '٢' to '2', '٣' to '3', '٤' to '4',
        '٥' to '5', '٦' to '6', '٧' to '7', '٨' to '8', '٩' to '9'
    )

    private val INVISIBLE_CONTROL_REGEX = Regex("[\\u200E\\u200F\\u202A-\\u202E\\u2066-\\u2069\\uFEFF]")

    /**
     * Converts all Persian (`۰-۹`) and Arabic (`٠-٩`) digits in [input] to English digits (`0-9`).
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
     */
    fun normalizeCharacters(input: String): String {
        return input
            .replace('ي', 'ی')
            .replace('ك', 'ک')
            .replace('ة', 'ه')
    }

    /**
     * Strips invisible Unicode control/directional characters (`LRM`, `RLM`, `BOM`, etc.)
     * and replaces `\u200C` (ZWNJ) with a space `' '`.
     */
    fun stripControlCharacters(input: String): String {
        return input
            .replace('\u200C', ' ')
            .replace(INVISIBLE_CONTROL_REGEX, "")
    }

    /**
     * Unifies various Persian/Arabic/quote comma separators (`٬`, `،`, `'`) into standard ASCII comma (`,`).
     */
    fun unifySeparators(input: String): String {
        return input
            .replace('٬', ',')
            .replace('،', ',')
            .replace('\'', ',')
    }

    /**
     * Converts dot-separated thousand groups (e.g. `1.500.000` -> `1,500,000`) when followed by 3 digits.
     */
    fun convertDotSeparatedThousands(input: String): String {
        return input.replace(Regex("(?<=\\d)\\.(?=\\d{3}(?:\\D|$))"), ",")
    }

    /**
     * Removes thousand separators (`,`, `،`, `٬`) from numeric strings.
     */
    fun removeThousandSeparators(input: String): String {
        return input
            .replace(Regex("(?<=\\d)[,،٬](?=\\d)"), "")
            .replace(Regex("[,،٬]"), "")
    }

    /**
     * Extracts a clean [Long] financial amount from a text string.
     */
    fun extractAmount(input: String): Long? {
        val englishDigits = convertDigitsToEnglish(input)
        val cleanNumberString = removeThousandSeparators(englishDigits)
        val matchResult = Regex("\\d+").find(cleanNumberString) ?: return null
        return matchResult.value.toLongOrNull()
    }

    /**
     * Trims extra whitespace and standardizes newlines.
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
     * Normalizes SMS sender strings (phone numbers or alphanumeric sender IDs) to a canonical representation.
     *
     * Strips leading `+`, `00`, country code `98` (for numbers >= 10 digits), and leading zeros.
     * Keeps alphanumeric senders intact as lowercase string.
     */
    fun canonicalSender(sender: String): String {
        val asciiSender = convertDigitsToEnglish(sender.trim()).lowercase()
        val cleaned = asciiSender.filter { it.isLetterOrDigit() }

        if (cleaned.any { it.isLetter() }) {
            return cleaned
        }

        var digits = cleaned
        if (digits.startsWith("00")) {
            digits = digits.substring(2)
        }
        if (digits.length >= 10 && digits.startsWith("98")) {
            digits = digits.substring(2)
        }
        digits = digits.dropWhile { it == '0' }

        return digits.ifEmpty { cleaned }
    }

    /**
     * Performs complete text normalization on SMS content.
     */
    fun normalize(input: String): String {
        val step1 = stripControlCharacters(input)
        val step2 = normalizeCharacters(step1)
        val step3 = convertDigitsToEnglish(step2)
        val step4 = unifySeparators(step3)
        val step5 = convertDotSeparatedThousands(step4)
        return normalizeWhitespaceAndNewlines(step5)
    }
}
