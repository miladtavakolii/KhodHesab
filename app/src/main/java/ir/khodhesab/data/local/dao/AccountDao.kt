package ir.khodhesab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import ir.khodhesab.data.local.entity.AccountEntity
import ir.khodhesab.data.local.relation.AccountWithBank
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [AccountEntity].
 */
@Dao
interface AccountDao {

    /**
     * Inserts an account. Ignores if `(bankId, accountIdentifier)` composite unique index exists.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAccount(account: AccountEntity): Long

    /**
     * Updates an account record. Returns number of updated rows.
     */
    @Update
    suspend fun updateAccount(account: AccountEntity): Int

    /**
     * Updates an account's user-customized display title.
     */
    @Query("UPDATE accounts SET title = :newTitle WHERE id = :accountId")
    suspend fun updateAccountTitle(accountId: Long, newTitle: String): Int

    /**
     * Updates an account's [AccountEntity.currentBalance] and [AccountEntity.lastBalanceUpdate] timestamp
     * ONLY IF the new [transactionTimestamp] is greater than or equal to [AccountEntity.lastBalanceUpdate].
     * This prevents older out-of-order SMS imports from overwriting a newer known balance.
     */
    @Query(
        """
        UPDATE accounts 
        SET currentBalance = :newBalance, lastBalanceUpdate = :transactionTimestamp 
        WHERE id = :accountId AND :transactionTimestamp >= lastBalanceUpdate
        """
    )
    suspend fun updateBalanceIfNewer(accountId: Long, newBalance: Long, transactionTimestamp: Long): Int

    /**
     * Finds an account by bank ID and account/card identifier string.
     */
    @Query("SELECT * FROM accounts WHERE bankId = :bankId AND accountIdentifier = :identifier LIMIT 1")
    suspend fun getAccountByBankAndIdentifier(bankId: Long, identifier: String): AccountEntity?

    /**
     * Retrieves an account by its primary key ID.
     */
    @Query("SELECT * FROM accounts WHERE id = :accountId LIMIT 1")
    suspend fun getAccountById(accountId: Long): AccountEntity?

    /**
     * Observes all user accounts paired with their respective bank details.
     */
    @Transaction
    @Query("SELECT * FROM accounts ORDER BY isActive DESC, title ASC")
    fun getAllAccountsWithBank(): Flow<List<AccountWithBank>>

    /**
     * Calculates the total current balance across all active accounts (`isActive = 1`).
     */
    @Query("SELECT SUM(currentBalance) FROM accounts WHERE isActive = 1")
    fun getTotalActiveBalance(): Flow<Long?>
}
