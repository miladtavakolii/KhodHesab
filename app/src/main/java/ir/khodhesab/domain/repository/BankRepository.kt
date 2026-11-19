package ir.khodhesab.domain.repository

import ir.khodhesab.domain.model.Bank
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository contract for managing Iranian banking institutions.
 */
interface BankRepository {

    /**
     * Observes all available Iranian banks sorted by Persian name.
     */
    fun observeAllBanks(): Flow<List<Bank>>

    /**
     * Finds a bank by its unique string code (e.g., `"MELLAT"`, `"MELLI"`).
     */
    suspend fun getBankByCode(code: String): Bank?

    /**
     * Finds a bank associated with an incoming SMS sender shortcode or phone number.
     */
    suspend fun findBankBySender(senderNumber: String): Bank?
}
