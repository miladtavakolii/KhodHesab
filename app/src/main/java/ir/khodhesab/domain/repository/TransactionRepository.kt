package ir.khodhesab.domain.repository

import ir.khodhesab.domain.model.CategoryBreakdown
import ir.khodhesab.domain.model.FinancialSummary
import ir.khodhesab.domain.model.Transaction
import ir.khodhesab.domain.model.TransactionKind
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository contract for transaction recording, querying, filtering, and reporting.
 */
interface TransactionRepository {

    /**
     * Observes transactions matching optional account, kind, date range, and search query filters.
     */
    fun observeTransactions(
        accountId: Long? = null,
        kind: TransactionKind? = null,
        startTime: Long? = null,
        endTime: Long? = null,
        searchQuery: String? = null
    ): Flow<List<Transaction>>

    /**
     * Checks if a transaction with the given SMS SHA-256 hash already exists.
     */
    suspend fun existsBySmsHash(smsHash: String): Boolean

    /**
     * Inserts a new transaction record. Returns the new transaction ID or -1L if duplicate.
     */
    suspend fun insertTransaction(
        accountId: Long,
        categoryId: Long?,
        kind: TransactionKind,
        amount: Long,
        balanceAfter: Long?,
        timestamp: Long,
        description: String?,
        rawSmsBody: String?,
        smsHash: String?
    ): Long

    /**
     * Updates category assignment and description notes for an existing transaction.
     */
    suspend fun updateTransactionDetails(transactionId: Long, categoryId: Long?, description: String?)

    /**
     * Updates the transaction kind (e.g., converting an expense/income to an internal transfer).
     */
    suspend fun updateTransactionKind(transactionId: Long, kind: TransactionKind)

    /**
     * Deletes a transaction by ID.
     */
    suspend fun deleteTransaction(transactionId: Long)

    /**
     * Observes period financial summary (income, expense, net flow) excluding internal transfers.
     */
    fun observePeriodSummary(startTime: Long, endTime: Long, accountId: Long? = null): Flow<FinancialSummary>

    /**
     * Observes expense or income breakdown by category with percentage distribution.
     */
    fun observeCategoryBreakdown(kind: TransactionKind, startTime: Long, endTime: Long, accountId: Long? = null): Flow<List<CategoryBreakdown>>
}
