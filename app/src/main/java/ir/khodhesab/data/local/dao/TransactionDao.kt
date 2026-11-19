package ir.khodhesab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import ir.khodhesab.data.local.entity.TransactionEntity
import ir.khodhesab.data.local.model.TransactionType
import ir.khodhesab.data.local.relation.CategoryExpenseSummary
import ir.khodhesab.data.local.relation.TransactionWithDetails
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [TransactionEntity].
 */
@Dao
interface TransactionDao {

    /**
     * Inserts a transaction record. Ignores duplicates if `smsHash` unique constraint is violated.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    /**
     * Updates a transaction record. Returns number of updated rows.
     */
    @Update
    suspend fun updateTransaction(transaction: TransactionEntity): Int

    /**
     * Updates category and user description for a specific transaction.
     */
    @Query("UPDATE transactions SET categoryId = :categoryId, description = :description WHERE id = :transactionId")
    suspend fun updateCategoryAndDescription(transactionId: Long, categoryId: Long?, description: String?): Int

    /**
     * Updates the transaction type for a specific transaction.
     */
    @Query("UPDATE transactions SET type = :type WHERE id = :transactionId")
    suspend fun updateTransactionType(transactionId: Long, type: TransactionType): Int

    /**
     * Deletes a transaction by ID.
     */
    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteTransaction(transactionId: Long): Int

    /**
     * Checks if a transaction with the given SMS hash already exists.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM transactions WHERE smsHash = :hash LIMIT 1)")
    suspend fun existsBySmsHash(hash: String): Boolean

    /**
     * Retrieves transactions with flexible filtering by account, type, date range, and search query.
     */
    @Transaction
    @Query(
        """
        SELECT * FROM transactions
        WHERE (:accountId IS NULL OR accountId = :accountId)
          AND (:type IS NULL OR type = :type)
          AND (:startTime IS NULL OR timestamp >= :startTime)
          AND (:endTime IS NULL OR timestamp <= :endTime)
          AND (:searchQuery IS NULL OR description LIKE '%' || :searchQuery || '%' OR rawSmsBody LIKE '%' || :searchQuery || '%')
        ORDER BY timestamp DESC
        """
    )
    fun getTransactionsFiltered(
        accountId: Long?,
        type: TransactionType?,
        startTime: Long?,
        endTime: Long?,
        searchQuery: String?
    ): Flow<List<TransactionWithDetails>>

    /**
     * Calculates the sum of transaction amounts by type and optional date range and account filter.
     */
    @Query(
        """
        SELECT SUM(amount) FROM transactions
        WHERE type = :type
          AND timestamp >= :startTime
          AND timestamp <= :endTime
          AND (:accountId IS NULL OR accountId = :accountId)
        """
    )
    fun getSumByTypeAndDateRange(
        type: TransactionType,
        startTime: Long,
        endTime: Long,
        accountId: Long?
    ): Flow<Long?>

    /**
     * Aggregates total transaction amount and count broken down by category for reporting.
     */
    @Query(
        """
        SELECT 
            t.categoryId AS categoryId,
            c.name AS categoryName,
            c.colorHex AS colorHex,
            c.iconKey AS iconKey,
            SUM(t.amount) AS totalAmount,
            COUNT(t.id) AS transactionCount
        FROM transactions t
        LEFT JOIN categories c ON t.categoryId = c.id
        WHERE t.type = :type
          AND t.timestamp >= :startTime
          AND t.timestamp <= :endTime
          AND (:accountId IS NULL OR t.accountId = :accountId)
        GROUP BY t.categoryId
        ORDER BY totalAmount DESC
        """
    )
    fun getCategoryBreakdown(
        type: TransactionType,
        startTime: Long,
        endTime: Long,
        accountId: Long?
    ): Flow<List<CategoryExpenseSummary>>
}
