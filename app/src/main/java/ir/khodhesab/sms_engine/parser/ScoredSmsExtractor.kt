package ir.khodhesab.sms_engine.parser

import ir.khodhesab.core.util.PersianTextNormalizer
import ir.khodhesab.core.util.SmsHashGenerator
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.sms_engine.parser.model.ParsedSmsData
import ir.khodhesab.sms_engine.parser.model.RawSmsMessage
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resilient Context-Window Token Scoring SMS Extractor inspired by ForApp.
 *
 * Scans normalized SMS bodies for all candidate numbers, filters out non-amount numeric formats
 * (dates, times, masked cards, Sheba IDs), and ranks candidates using proximity-weighted contextual scoring
 * to accurately extract transaction amount, remaining balance, transaction kind, and account identifier.
 */
@Singleton
class ScoredSmsExtractor @Inject constructor() {

    private val NUMBER_REGEX = Regex("\\d{1,3}(?:,\\d{3})+|\\d{4,}")

    private val DEPOSIT_KEYWORDS = listOf(
        "واریز", "انتقال به حساب شما", "انتقال به", "دریافت وجه", "دریافت پل",
        "بستانکار", "افزایش", "سود", "حقوق", "یارانه", "برگشت"
    )

    private val WITHDRAWAL_KEYWORDS = listOf(
        "برداشت", "خرید", "کسر", "پرداخت", "انتقال از حساب", "انتقال از",
        "بدهکار", "کاهش", "کارمزد", "قسط"
    )

    /**
     * Data class holding a scored candidate number with contextual properties.
     */
    data class ScoredCandidate(
        val rawValue: String,
        val cleanValue: Long,
        val score: Int,
        val startIndex: Int,
        val endIndex: Int,
        val preContext: String,
        val postContext: String,
        val unitMultiplier: Long,
        val isBalance: Boolean
    )

    /**
     * Extracts structured transaction data from normalized SMS text using context-window scoring.
     *
     * @param bankCode Code of the detected bank (e.g. `"MELLAT"`, `"OTHER"`).
     * @param normalizedBody Fully normalized SMS text.
     * @param rawSms Raw incoming SMS message container.
     * @return Extracted [ParsedSmsData] or `null` if no valid candidate amount scored >= 0.
     */
    fun parse(bankCode: String, normalizedBody: String, rawSms: RawSmsMessage): ParsedSmsData? {
        val text = PersianTextNormalizer.normalize(normalizedBody)
        val matches = NUMBER_REGEX.findAll(text).toList()

        val candidates = mutableListOf<ScoredCandidate>()
        val balanceCandidates = mutableListOf<ScoredCandidate>()

        for (match in matches) {
            val s = match.range.first
            val e = match.range.last + 1
            val raw = match.value

            val beforeChar = if (s > 0) text[s - 1] else ' '
            val afterChar = if (e < text.length) text[e] else ' '

            // 1. Adjacent-Character Exclusion Filter
            if (beforeChar == '/' || afterChar == '/') continue
            if (afterChar == ':') continue
            if (beforeChar == ':' && s >= 2 && text[s - 2].isDigit()) continue
            if (beforeChar == '*' || afterChar == '*') continue
            if (beforeChar == '_' || afterChar == '_') continue
            if (beforeChar == '-' && s >= 2 && text[s - 2].isDigit()) continue
            if (afterChar == '-' && e + 1 < text.length && text[e + 1].isDigit()) continue

            val cleanDigits = raw.replace(",", "")
            if (cleanDigits.length > 12) continue

            val cleanVal = cleanDigits.toLongOrNull() ?: continue
            if (cleanVal <= 0) continue

            // 2. Context Extraction (up to 25 chars before on same line, up to 12 chars after)
            val lineStart = text.lastIndexOf('\n', s).let { if (it == -1) 0 else it + 1 }
            val preStart = (s - 25).coerceAtLeast(lineStart)
            val pre = text.substring(preStart, s)
            val postEnd = (e + 12).coerceAtMost(text.length)
            val post = text.substring(e, postEnd)

            var score = 0
            var isBalance = false

            val preTrimmed = pre.trim()
            val isBalanceContext = pre.contains("مانده") || pre.contains("موجودی") || pre.contains("باقیمانده")
            val isAccountContext = (pre.contains("حساب") || pre.contains("کارت") || pre.contains("شبا") ||
                    pre.contains("کد") || pre.contains("پیگیری") || pre.contains("مرجع")) &&
                    !(pre.contains("مبلغ") || pre.contains("واریز") || pre.contains("برداشت"))

            if (isBalanceContext) {
                isBalance = true
                score -= 10
            } else if (isAccountContext) {
                score -= 4
            }

            if (pre.contains("مبلغ")) score += 5
            if (pre.contains("واریز") || pre.contains("برداشت") || pre.contains("خرید") ||
                pre.contains("پرداخت") || pre.contains("انتقال")) score += 4

            if (preTrimmed.endsWith("+") || afterChar == '+') score += 4
            if (preTrimmed.endsWith("-") || afterChar == '-') score += 3

            val postClean = post.trim().lowercase()
            var unitMultiplier = 1L
            if (postClean.startsWith("تومان")) {
                unitMultiplier = 10L
                score += 2
            } else if (postClean.startsWith("ریال") || postClean.startsWith("rial") || postClean.startsWith("irr")) {
                score += 2
            }

            if (raw.contains(',')) score += 1

            val candidate = ScoredCandidate(
                rawValue = raw,
                cleanValue = cleanVal * unitMultiplier,
                score = score,
                startIndex = s,
                endIndex = e,
                preContext = pre,
                postContext = post,
                unitMultiplier = unitMultiplier,
                isBalance = isBalance
            )

            if (isBalance) {
                balanceCandidates.add(candidate)
            } else {
                candidates.add(candidate)
            }
        }

        val bestAmountCandidate = candidates.filter { it.score >= 0 }.maxByOrNull { it.score } ?: return null
        val bestBalanceCandidate = balanceCandidates.maxByOrNull { it.score }

        val kind = detectKindByProximity(text, bestAmountCandidate)
        val accountId = extractAccountIdentifier(text)
        val hash = SmsHashGenerator.generateHash(rawSms.sender, normalizedBody, rawSms.timestamp)

        return ParsedSmsData(
            bankCode = bankCode,
            accountIdentifier = accountId,
            kind = kind,
            amount = bestAmountCandidate.cleanValue,
            balanceAfter = bestBalanceCandidate?.cleanValue,
            timestamp = rawSms.timestamp,
            rawBody = rawSms.body,
            smsHash = hash
        )
    }

    private fun detectKindByProximity(text: String, candidate: ScoredCandidate): TransactionKind {
        val s = candidate.startIndex
        val e = candidate.endIndex
        val beforeChar = if (s > 0) text[s - 1] else ' '
        val afterChar = if (e < text.length) text[e] else ' '

        if (beforeChar == '+' || afterChar == '+' || candidate.preContext.trim().endsWith("+")) {
            return TransactionKind.DEPOSIT
        }
        if (beforeChar == '-' || afterChar == '-' || candidate.preContext.trim().endsWith("-")) {
            return TransactionKind.WITHDRAWAL
        }

        val depositPos = DEPOSIT_KEYWORDS.maxOfOrNull { kw ->
            val normKw = PersianTextNormalizer.normalize(kw)
            text.lastIndexOf(normKw, s)
        } ?: -1

        val withdrawalPos = WITHDRAWAL_KEYWORDS.maxOfOrNull { kw ->
            val normKw = PersianTextNormalizer.normalize(kw)
            text.lastIndexOf(normKw, s)
        } ?: -1

        return if (depositPos > withdrawalPos) {
            TransactionKind.DEPOSIT
        } else {
            TransactionKind.WITHDRAWAL
        }
    }

    fun extractAccountIdentifier(text: String): String {
        val maskedMatch = Regex("(?:\\d{4}\\*+|\\*+)(\\d{4})").find(text)
        if (maskedMatch != null) {
            return maskedMatch.groupValues[1]
        }

        val keywordMatch = Regex("(?:حساب|سپرده|کارت)[:\\s]*([0-9\\-]+)").find(text)
        if (keywordMatch != null) {
            val digits = keywordMatch.groupValues[1].replace("-", "")
            if (digits.length >= 4) {
                return digits.takeLast(4)
            } else if (digits.isNotEmpty()) {
                return digits
            }
        }

        return "DEFAULT"
    }
}
