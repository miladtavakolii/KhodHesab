package ir.khodhesab.sms_engine.parser

import ir.khodhesab.core.util.PersianTextNormalizer
import ir.khodhesab.domain.model.Bank
import ir.khodhesab.domain.repository.BankRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Detector responsible for resolving an SMS message to an Iranian [Bank] domain entity.
 *
 * Utilizes multi-stage detection with fallback:
 * 1. Canonical sender number / shortcode matching via [BankRepository.findBankBySender].
 * 2. Keyword fallback matching on normalized SMS body.
 * 3. Alphanumeric sender ID matching.
 * 4. Fallback to "OTHER" bank entity so valid transactions are never discarded.
 */
@Singleton
class BankDetector @Inject constructor(
    private val bankRepository: BankRepository
) {

    private val bankKeywordMap = mapOf(
        "MELLAT" to listOf("بانک ملت", "ملت", "mellat"),
        "MELLI" to listOf("بانک ملی", "ملی ایران", "ملی", "melli"),
        "BLU" to listOf("بلوبانک", "بلو بانک", "بلو", "blubank", "blu"),
        "SAMAN" to listOf("بانک سامان", "سامان", "saman"),
        "PASARGAD" to listOf("بانک پاسارگاد", "پاسارگاد", "pasargad"),
        "TEJARAT" to listOf("بانک تجارت", "تجارت", "tejarat"),
        "SADERAT" to listOf("بانک صادرات", "صادرات", "saderat"),
        "SEPAH" to listOf("بانک سپه", "سپه", "sepah"),
        "KESHAVARZI" to listOf("بانک کشاورزی", "کشاورزی", "keshavarzi"),
        "PARSIAN" to listOf("بانک پارسیان", "پارسیان", "parsian"),
        "EGHTESAD_NOVIN" to listOf("بانک اقتصاد نوین", "اقتصاد نوین", "enbank"),
        "SHAHR" to listOf("بانک شهر", "شهر", "shahr"),
        "DAY" to listOf("بانک دی", "دی", "day bank"),
        "SINA" to listOf("بانک سینا", "سینا", "sina"),
        "REFAH" to listOf("بانک رفاه", "رفاه کارگران", "رفاه", "refah"),
        "MASKAN" to listOf("بانک مسکن", "مسکن", "maskan"),
        "RESALAT" to listOf("بانک رسالت", "رسالت", "resalat"),
        "MEHR_IRAN" to listOf("بانک مهر ایران", "مهر ایران", "mehr")
    )

    /**
     * Detects and resolves the [Bank] associated with the given raw SMS sender and body.
     */
    suspend fun detectBank(sender: String, body: String): Bank? {
        val senderMatch = bankRepository.findBankBySender(sender)
        if (senderMatch != null) return senderMatch

        val normalizedBody = PersianTextNormalizer.normalize(body)
        for ((code, keywords) in bankKeywordMap) {
            for (keyword in keywords) {
                val normalizedKeyword = PersianTextNormalizer.normalize(keyword)
                if (normalizedBody.contains(normalizedKeyword, ignoreCase = true)) {
                    val bankByCode = bankRepository.getBankByCode(code)
                    if (bankByCode != null) return bankByCode
                }
            }
        }

        val canonicalSender = PersianTextNormalizer.canonicalSender(sender)
        for ((code, keywords) in bankKeywordMap) {
            for (keyword in keywords) {
                val canonKw = PersianTextNormalizer.canonicalSender(keyword)
                if (canonKw.length >= 3 && canonicalSender.contains(canonKw, ignoreCase = true)) {
                    val bankByCode = bankRepository.getBankByCode(code)
                    if (bankByCode != null) return bankByCode
                }
            }
        }

        return bankRepository.getBankByCode("OTHER")
            ?: Bank(id = 9999, code = "OTHER", persianName = "سایر بانک‌ها", colorHex = "#546E7A", iconKey = "ic_bank_other")
    }
}
