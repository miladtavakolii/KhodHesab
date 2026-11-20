package ir.khodhesab.data.repository

import ir.khodhesab.core.util.PersianTextNormalizer
import ir.khodhesab.data.local.dao.BankDao
import ir.khodhesab.data.mapper.toDomain
import ir.khodhesab.domain.model.Bank
import ir.khodhesab.domain.repository.BankRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data layer implementation of [BankRepository].
 */
@Singleton
class BankRepositoryImpl @Inject constructor(
    private val bankDao: BankDao
) : BankRepository {

    override fun observeAllBanks(): Flow<List<Bank>> {
        return bankDao.getAllBanks().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getBankByCode(code: String): Bank? {
        return bankDao.getBankByCode(code.uppercase())?.toDomain()
    }

    override suspend fun findBankBySender(senderNumber: String): Bank? {
        val cleanSender = PersianTextNormalizer.convertDigitsToEnglish(senderNumber).trim()
        val directMatch = bankDao.findBankBySenderNumber(cleanSender)
        if (directMatch != null) return directMatch.toDomain()

        val altSender = if (cleanSender.startsWith("+98")) {
            "0" + cleanSender.substring(3)
        } else if (cleanSender.startsWith("98") && cleanSender.length > 5) {
            "0" + cleanSender.substring(2)
        } else {
            cleanSender
        }

        val altMatch = bankDao.findBankBySenderNumber(altSender)
        if (altMatch != null) return altMatch.toDomain()

        val canonical = PersianTextNormalizer.canonicalSender(senderNumber)
        if (canonical.isEmpty()) return null

        val allBanks = bankDao.getAllBanksList().map { it.toDomain() }

        for (bank in allBanks) {
            for (num in bank.senderNumbers) {
                val canonNum = PersianTextNormalizer.canonicalSender(num)
                if (canonNum == canonical) {
                    return bank
                }
            }
        }

        for (bank in allBanks) {
            for (num in bank.senderNumbers) {
                val canonNum = PersianTextNormalizer.canonicalSender(num)
                if (canonNum.isNotEmpty() && (canonical.startsWith(canonNum) || canonNum.startsWith(canonical))) {
                    return bank
                }
            }
        }

        return null
    }
}
