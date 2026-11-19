package ir.khodhesab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ir.khodhesab.data.local.entity.BankEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [BankEntity].
 */
@Dao
interface BankDao {

    /**
     * Inserts a list of banks ignoring conflicts on existing unique codes.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(banks: List<BankEntity>): List<Long>

    /**
     * Inserts a single bank record.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBank(bank: BankEntity): Long

    /**
     * Retrieves all Iranian banks sorted by Persian name.
     */
    @Query("SELECT * FROM banks ORDER BY persianName ASC")
    fun getAllBanks(): Flow<List<BankEntity>>

    /**
     * Finds a bank by its primary key ID.
     */
    @Query("SELECT * FROM banks WHERE id = :bankId LIMIT 1")
    suspend fun getBankById(bankId: Long): BankEntity?

    /**
     * Finds a bank by its unique string code (e.g., `"MELLAT"`).
     */
    @Query("SELECT * FROM banks WHERE code = :code LIMIT 1")
    suspend fun getBankByCode(code: String): BankEntity?

    /**
     * Finds a bank whose [BankEntity.senderNumbers] contains the given SMS sender string.
     */
    @Query("SELECT * FROM banks WHERE senderNumbers LIKE '%' || :sender || '%' LIMIT 1")
    suspend fun findBankBySenderNumber(sender: String): BankEntity?
}
