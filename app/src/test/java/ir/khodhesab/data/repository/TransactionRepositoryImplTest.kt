package ir.khodhesab.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import ir.khodhesab.data.local.dao.TransactionDao
import ir.khodhesab.data.local.entity.AccountEntity
import ir.khodhesab.data.local.entity.BankEntity
import ir.khodhesab.data.local.entity.TransactionEntity
import ir.khodhesab.data.local.model.TransactionType
import ir.khodhesab.data.local.relation.AccountWithBank
import ir.khodhesab.data.local.relation.CategoryExpenseSummary
import ir.khodhesab.data.local.relation.TransactionWithDetails
import ir.khodhesab.domain.model.TransactionKind
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [TransactionRepositoryImpl].
 */
class TransactionRepositoryImplTest {

    private val transactionDao: TransactionDao = mockk()
    private lateinit var repository: TransactionRepositoryImpl

    @Before
    fun setup() {
        repository = TransactionRepositoryImpl(transactionDao)
    }

    @Test
    fun observePeriodSummary_excludesTransfersFromCountAndCalculatesNet() = runTest {
        val bank = BankEntity(id = 1, code = "MELLI", persianName = "ملی", senderNumbers = emptyList(), colorHex = "#B", iconKey = "i")
        val account = AccountEntity(id = 1, bankId = 1, accountIdentifier = "123", title = "A")
        val accWithBank = AccountWithBank(account, bank)

        val txWithdrawal = TransactionWithDetails(
            transaction = TransactionEntity(id = 1, accountId = 1, type = TransactionType.WITHDRAWAL, amount = 100_000L, timestamp = 1000L),
            accountWithBank = accWithBank,
            category = null
        )
        val txTransfer = TransactionWithDetails(
            transaction = TransactionEntity(id = 2, accountId = 1, type = TransactionType.TRANSFER, amount = 500_000L, timestamp = 2000L),
            accountWithBank = accWithBank,
            category = null
        )

        every { transactionDao.getSumByTypeAndDateRange(TransactionType.DEPOSIT, 0L, 5000L, null) } returns flowOf(300_000L)
        every { transactionDao.getSumByTypeAndDateRange(TransactionType.WITHDRAWAL, 0L, 5000L, null) } returns flowOf(100_000L)
        every { transactionDao.getTransactionsFiltered(null, null, 0L, 5000L, null) } returns flowOf(listOf(txWithdrawal, txTransfer))

        repository.observePeriodSummary(0L, 5000L).test {
            val summary = awaitItem()
            assertThat(summary.totalIncome).isEqualTo(300_000L)
            assertThat(summary.totalExpense).isEqualTo(100_000L)
            assertThat(summary.netFlow).isEqualTo(200_000L)
            assertThat(summary.transactionCount).isEqualTo(1)
            awaitComplete()
        }
    }

    @Test
    fun observeCategoryBreakdown_calculatesPercentageDistribution() = runTest {
        val summaryList = listOf(
            CategoryExpenseSummary(categoryId = 1L, categoryName = "غذا", colorHex = "#RED", iconKey = "food", totalAmount = 500_000L, transactionCount = 2)
        )
        every { transactionDao.getSumByTypeAndDateRange(TransactionType.WITHDRAWAL, 0L, 5000L, null) } returns flowOf(1_000_000L)
        every { transactionDao.getCategoryBreakdown(TransactionType.WITHDRAWAL, 0L, 5000L, null) } returns flowOf(summaryList)

        repository.observeCategoryBreakdown(TransactionKind.WITHDRAWAL, 0L, 5000L).test {
            val list = awaitItem()
            assertThat(list).hasSize(1)
            assertThat(list.first().percentage).isEqualTo(50.0f)
            awaitComplete()
        }
    }
}
