package ir.khodhesab.domain.repository

import ir.khodhesab.domain.model.Account
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository contract for managing user bank accounts and active balances.
 */
interface AccountRepository {

    /**
     * Observes all user bank accounts along with their associated bank details.
     */
    fun observeAllAccounts(): Flow<List<Account>>

    /**
     * Observes the aggregated current balance across all active accounts in Rials.
     */
    fun observeTotalActiveBalance(): Flow<Long>

    /**
     * Retrieves an existing account by `(bankId, accountIdentifier)` or atomically creates a new one.
     */
    suspend fun getOrCreateAccount(bankId: Long, accountIdentifier: String, defaultTitle: String): Account

    /**
     * Updates an account's user-customized display title.
     */
    suspend fun updateAccountTitle(accountId: Long, newTitle: String)

    /**
     * Toggles an account's active status.
     */
    suspend fun setAccountActive(accountId: Long, isActive: Boolean)

    /**
     * Updates an account's balance only if [timestamp] is greater than or equal to the last recorded update.
     */
    suspend fun updateBalanceIfNewer(accountId: Long, newBalance: Long, timestamp: Long)
}
