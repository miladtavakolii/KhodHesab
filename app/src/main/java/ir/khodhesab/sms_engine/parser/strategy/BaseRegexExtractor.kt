package ir.khodhesab.sms_engine.parser.strategy

import ir.khodhesab.core.util.PersianTextNormalizer
import ir.khodhesab.core.util.SmsHashGenerator
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.sms_engine.parser.model.ParsedSmsData
import ir.khodhesab.sms_engine.parser.model.RawSmsMessage

/**
 * Reusable helper providing extraction algorithms for transaction kind, amount, balance,
 * and account identifiers from Iranian bank SMS messages.
 */
object BaseRegexExtractor {

    private val withdrawalKeywords = listOf("برداشت", "کاهش", "خرید", "پرداخت", "کارمزد", "انتقال از", "قسط")
    private val depositKeywords = listOf("واریز", "افزایش", "سود", "انتقال به", "حقوق", "یارانه")

    /**
     * Detects [TransactionKind] based on Persian action keywords and signed prefixes (`+` / `-`).
     */
    fun detectTransactionKind(normalizedBody: String): TransactionKind {
        // 1. Check explicit positive or negative sign directly attached to amount (prefix or suffix)
        if (Regex("(?:مبلغ|واریز|برداشت|انتقال)[:\\s]*\\+\\s*[\\d,،٬]+").containsMatchIn(normalizedBody) ||
            Regex("\\+\\s*[\\d,،٬]+\\s*(?:ریال|تومان)?").containsMatchIn(normalizedBody) ||
            Regex("(?:مبلغ|واریز|برداشت|انتقال)[:\\s]*[\\d,،٬]+\\s*\\+").containsMatchIn(normalizedBody) ||
            Regex("[\\d,،٬]+\\+\\s*(?:ریال|تومان)?").containsMatchIn(normalizedBody)) {
            return TransactionKind.DEPOSIT
        }
        if (Regex("(?:مبلغ|واریز|برداشت|انتقال)[:\\s]*-\\s*[\\d,،٬]+").containsMatchIn(normalizedBody) ||
            Regex("-\\s*[\\d,،٬]+\\s*(?:ریال|تومان)").containsMatchIn(normalizedBody) ||
            Regex("(?:مبلغ|واریز|برداشت|انتقال)[:\\s]*[\\d,،٬]+\\s*-").containsMatchIn(normalizedBody) ||
            Regex("[\\d,،٬]+-\\s*(?:ریال|تومان)").containsMatchIn(normalizedBody)) {
            return TransactionKind.WITHDRAWAL
        }

        // 2. Check withdrawal vs deposit keywords
        val withdrawalScore = withdrawalKeywords.count { normalizedBody.contains(it, ignoreCase = true) }
        val depositScore = depositKeywords.count { normalizedBody.contains(it, ignoreCase = true) }

        return if (depositScore > withdrawalScore) {
            TransactionKind.DEPOSIT
        } else if (withdrawalScore > depositScore) {
            TransactionKind.WITHDRAWAL
        } else if (depositScore > 0) {
            TransactionKind.DEPOSIT
        } else {
            TransactionKind.WITHDRAWAL
        }
    }

    /**
     * Extracts and normalizes transaction amount in Rials.
     * Automatically converts Toman amounts to Rials (multiplies by 10) if `"تومان"` is present near the amount.
     */
    fun extractAmountInRials(normalizedBody: String): Long? {
        val amountRegexList = listOf(
            Regex("(?:مبلغ|واریز|برداشت|خرید|انتقال|وجه)[:\\s]*([+-]?\\s*[\\d,،٬]+[+-]?)"),
            Regex("([+-]?\\s*[\\d,،٬]+[+-]?)\\s*(?:ریال|تومان)"),
            Regex("([\\d,،٬]+)\\s*(?:ریال|تومان)?")
        )

        for (regex in amountRegexList) {
            val match = regex.find(normalizedBody) ?: continue
            val rawNumStr = match.groupValues[1]
            val amount = PersianTextNormalizer.extractAmount(rawNumStr)
            if (amount != null && amount > 0L) {
                val isToman = normalizedBody.contains("تومان") && !normalizedBody.substringAfter(rawNumStr, "").startsWith(" ریال")
                return if (isToman) amount * 10L else amount
            }
        }
        return null
    }

    /**
     * Extracts and normalizes remaining balance in Rials.
     */
    fun extractBalanceInRials(normalizedBody: String): Long? {
        val balanceRegexList = listOf(
            Regex("(?:مانده|موجودی|باقیمانده)[:\\s]*([\\d,،٬]+)"),
            Regex("(?:مانده|موجودی)[:\\s]*([\\d,،٬]+)\\s*(?:ریال|تومان)"),
            Regex("موجودی[:\\s]*([\\d,،٬]+)")
        )

        for (regex in balanceRegexList) {
            val match = regex.find(normalizedBody) ?: continue
            val rawNumStr = match.groupValues[1]
            val balance = PersianTextNormalizer.extractAmount(rawNumStr)
            if (balance != null) {
                val isToman = normalizedBody.contains("تومان")
                return if (isToman) balance * 10L else balance
            }
        }
        return null
    }

    /**
     * Extracts and normalizes account or card identifier string.
     * Returns last 4 significant digits or `"DEFAULT"` if no identifier is present.
     */
    fun extractAccountIdentifier(normalizedBody: String): String {
        val identifierRegexList = listOf(
            Regex("(?:حساب|سپرده|کارت)[:\\s]*([0-9.*-]+)"),
            Regex("\\*+(\\d{4})"),
            Regex("(\\d{4})\\*+\\d{4}")
        )

        for (regex in identifierRegexList) {
            val match = regex.find(normalizedBody) ?: continue
            val rawId = match.groupValues[1].replace("*", "").replace("-", "").replace(".", "").trim()
            val cleanDigits = PersianTextNormalizer.convertDigitsToEnglish(rawId).filter { it.isDigit() }
            if (cleanDigits.isNotEmpty()) {
                return if (cleanDigits.length > 4) cleanDigits.takeLast(4) else cleanDigits
            }
        }
        return "DEFAULT"
    }

    /**
     * Universal helper to build a complete [ParsedSmsData] object.
     */
    fun buildParsedSmsData(
        normalizedBody: String,
        rawSms: RawSmsMessage,
        bankCode: String
    ): ParsedSmsData? {
        val amount = extractAmountInRials(normalizedBody) ?: return null
        val kind = detectTransactionKind(normalizedBody)
        val balance = extractBalanceInRials(normalizedBody)
        val accountId = extractAccountIdentifier(normalizedBody)

        val hasBalanceKeyword = normalizedBody.contains("مانده") ||
                normalizedBody.contains("موجودی") ||
                normalizedBody.contains("باقیمانده")
        if (balance == null && !hasBalanceKeyword) {
            return null
        }

        val hash = SmsHashGenerator.generateHash(rawSms.sender, normalizedBody, rawSms.timestamp)

        return ParsedSmsData(
            bankCode = bankCode,
            accountIdentifier = accountId,
            kind = kind,
            amount = amount,
            balanceAfter = balance,
            timestamp = rawSms.timestamp,
            rawBody = rawSms.body,
            smsHash = hash
        )
    }
}
