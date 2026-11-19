package ir.khodhesab.sms_engine.parser

import ir.khodhesab.core.util.PersianTextNormalizer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Filter utility to identify and reject non-financial SMS messages (OTP passwords, login alerts, promos).
 */
@Singleton
class NonFinancialSmsFilter @Inject constructor() {

    private val otpKeywords = listOf(
        "رمز پویا", "رمز یکبار مصرف", "رمز یک بار مصرف", "رمز دوم",
        "کد تایید", "کد ورود", "کد فعالسازی", "کد فعال سازی", "OTP", "CVV2",
        "کد صحه‌گذاری", "رمز پویای"
    )

    private val securityAlertKeywords = listOf(
        "ورود به همراه بانک", "ورود به همراهبانک", "ورود به اینترنت بانک",
        "تغییر رمز", "مسدود گردید", "مسدود شد", "غیرفعال گردید"
    )

    private val transactionKeywords = listOf(
        "واریز", "برداشت", "خرید", "انتقال", "کاهش", "افزایش", "مبلغ", "مانده", "موجودی", "کارمزد", "پایا", "ساتنا"
    )

    /**
     * Checks whether an SMS body represents a non-financial message.
     *
     * @param body Raw or normalized SMS body text.
     * @return `null` if the message is a valid candidate for transaction parsing,
     * or a non-null reason string if it should be ignored.
     */
    fun checkNonFinancial(body: String): String? {
        val normalized = PersianTextNormalizer.normalize(body)

        // 1. Check OTP keywords
        for (keyword in otpKeywords) {
            val normalizedKeyword = PersianTextNormalizer.normalize(keyword)
            if (normalized.contains(normalizedKeyword, ignoreCase = true)) {
                return "پیامک حاوی رمز پویا / کد تایید است ($keyword)."
            }
        }

        // 2. Check Security / Login alerts without financial amount
        for (keyword in securityAlertKeywords) {
            val normalizedKeyword = PersianTextNormalizer.normalize(keyword)
            if (normalized.contains(normalizedKeyword, ignoreCase = true)) {
                return "پیامک امنیتی / ورود به سامانه است ($keyword)."
            }
        }

        // 3. Ensure at least one transaction keyword exists
        val hasTransactionKeyword = transactionKeywords.any {
            normalized.contains(PersianTextNormalizer.normalize(it), ignoreCase = true)
        }
        if (!hasTransactionKeyword) {
            return "پیامک فاقد کلیدواژه‌های تراکنش مالی است."
        }

        // 4. Ensure at least one number sequence exists
        val amount = PersianTextNormalizer.extractAmount(normalized)
        if (amount == null) {
            return "پیامک فاقد مبلغ عددی است."
        }

        return null
    }
}
