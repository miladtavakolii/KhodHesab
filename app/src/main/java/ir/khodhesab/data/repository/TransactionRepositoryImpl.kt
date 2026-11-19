package ir.khodhesab.data.repository

import ir.khodhesab.data.local.dao.TransactionDao
import ir.khodhesab.data.local.entity.TransactionEntity
import ir.khodhesab.data.local.model.TransactionType
import ir.khodhesab.data.mapper.toDomain
import ir.khodhesab.data.mapper.toEntity
import ir.khodhesab.domain.model.CategoryBreakdown
import ir.khodhesab.domain.model.FinancialSummary
import ir.khodhesab.domain.model.Transaction
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data layer implementation of [TransactionRepository].
 */
@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun observeTransactions(
        accountId: Long?,
        kind: TransactionKind?,
        startTime: Long?,
        endTime: Long?,
        searchQuery: String?
    ): Flow<List<Transaction>> {
        val typeEntity = kind?.toEntity()
        val query = searchQuery?.trim()?.takeIf { it.isNotEmpty() }
        return transactionDao.getTransactionsFiltered(
            accountId = accountId,
            type = typeEntity,
            startTime = startTime,
            endTime = endTime,
            searchQuery = query
        ).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun existsBySmsHash(smsHash: String): Boolean {
        return transactionDao.existsBySmsHash(smsHash)
    }

    override suspend fun insertTransaction(
        accountId: Long,
        categoryId: Long?,
        kind: TransactionKind,
        amount: Long,
        balanceAfter: Long?,
        timestamp: Long,
        description: String?,
        rawSmsBody: String?,
        smsHash: String?
    ): Long {
        val entity = TransactionEntity(
            accountId = accountId,
            categoryId = categoryId,
            type = kind.toEntity(),
            amount = amount,
            balanceAfter = balanceAfter,
            timestamp = timestamp,
            description = description,
            rawSmsBody = rawSmsBody,
            smsHash = smsHash
        )
        return transactionDao.insertTransaction(entity)
    }

    override suspend fun updateTransactionDetails(
        transactionId: Long,
        categoryId: Long?,
        description: String?
    ) {
        transactionDao.updateCategoryAndDescription(transactionId, categoryId, description)
    }

    override suspend fun updateTransactionKind(transactionId: Long, kind: TransactionKind) {
        transactionDao.updateTransactionType(transactionId, kind.toEntity())
    }

    override suspend fun deleteTransaction(transactionId: Long) {
        transactionDao.deleteTransaction(transactionId)
    }

    override fun observePeriodSummary(
        startTime: Long,
        endTime: Long,
        accountId: Long?
    ): Flow<FinancialSummary> {
        val depositFlow = transactionDao.getSumByTypeAndDateRange(
            type = TransactionType.DEPOSIT,
            startTime = startTime,
            endTime = endTime,
            accountId = accountId
        )
        val withdrawalFlow = transactionDao.getSumByTypeAndDateRange(
            type = TransactionType.WITHDRAWAL,
            startTime = startTime,
            endTime = endTime,
            accountId = accountId
        )
        val txListFlow = observeTransactions(accountId = accountId, startTime = startTime, endTime = endTime)

        return combine(depositFlow, withdrawalFlow, txListFlow) { income, expense, txList ->
            val validTxCount = txList.count { it.kind != TransactionKind.TRANSFER }
            FinancialSummary(
                totalIncome = income ?: 0L,
                totalExpense = expense ?: 0L,
                transactionCount = validTxCount
            )
        }
    }

    override fun observeCategoryBreakdown(
        kind: TransactionKind,
        startTime: Long,
        endTime: Long,
        accountId: Long?
    ): Flow<List<CategoryBreakdown>> {
        val sumFlow = transactionDao.getSumByTypeAndDateRange(
            type = kind.toEntity(),
            startTime = startTime,
            endTime = endTime,
            accountId = accountId
        )
        val breakdownFlow = transactionDao.getCategoryBreakdown(
            type = kind.toEntity(),
            startTime = startTime,
            endTime = endTime,
            accountId = accountId
        )

        return combine(sumFlow, breakdownFlow) { totalSum, breakdownList ->
            val periodTotal = totalSum ?: 0L
            breakdownList.map { it.toDomain(periodTotal) }
        }
    }
}
