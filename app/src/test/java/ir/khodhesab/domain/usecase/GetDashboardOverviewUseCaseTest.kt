package ir.khodhesab.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import ir.khodhesab.domain.model.Account
import ir.khodhesab.domain.model.Bank
import ir.khodhesab.domain.model.FinancialSummary
import ir.khodhesab.domain.model.Transaction
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.domain.repository.AccountRepository
import ir.khodhesab.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [GetDashboardOverviewUseCase].
 */
class GetDashboardOverviewUseCaseTest {

    private val accountRepository: AccountRepository = mockk()
    private val transactionRepository: TransactionRepository = mockk()
    private lateinit var useCase: GetDashboardOverviewUseCase

    @Before
    fun setup() {
        useCase = GetDashboardOverviewUseCase(accountRepository, transactionRepository)
    }

    @Test
    fun invoke_combinesFlowsIntoDashboardOverview() = runTest {
        val bank = Bank(id = 1, code = "B", persianName = "بانک", colorHex = "#C", iconKey = "i")
        val account = Account(id = 1, bank = bank, accountIdentifier = "123", title = "تست")
        val tx = Transaction(id = 1, account = account, kind = TransactionKind.WITHDRAWAL, amount = 1000L, timestamp = 1000L)

        every { accountRepository.observeTotalActiveBalance() } returns flowOf(1_000_000L)
        every { accountRepository.observeAllAccounts() } returns flowOf(listOf(account))
        every { transactionRepository.observePeriodSummary(any(), any(), null) } returns flowOf(FinancialSummary(totalIncome = 500_000L, totalExpense = 100_000L, transactionCount = 1))
        every { transactionRepository.observeTransactions() } returns flowOf(listOf(tx))

        useCase(currentTimeMillis = 1700000000000L).test {
            val overview = awaitItem()
            assertThat(overview.totalActiveBalance).isEqualTo(1_000_000L)
            assertThat(overview.accounts).hasSize(1)
            assertThat(overview.periodSummary.netFlow).isEqualTo(400_000L)
            assertThat(overview.recentTransactions).hasSize(1)
            awaitComplete()
        }
    }
}
