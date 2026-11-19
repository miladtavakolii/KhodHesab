package ir.khodhesab.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import ir.khodhesab.domain.model.FinancialSummary
import ir.khodhesab.domain.model.ReportPeriod
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [GetFinancialReportUseCase].
 */
class GetFinancialReportUseCaseTest {

    private val transactionRepository: TransactionRepository = mockk()
    private lateinit var useCase: GetFinancialReportUseCase

    @Before
    fun setup() {
        useCase = GetFinancialReportUseCase(transactionRepository)
    }

    @Test
    fun invoke_delegatesJalaliPeriodRangesAndQueriesRepository() = runTest {
        every { transactionRepository.observePeriodSummary(any(), any(), null) } returns flowOf(FinancialSummary(totalIncome = 200L, totalExpense = 50L, transactionCount = 2))
        every { transactionRepository.observeCategoryBreakdown(TransactionKind.WITHDRAWAL, any(), any(), null) } returns flowOf(emptyList())
        every { transactionRepository.observeCategoryBreakdown(TransactionKind.DEPOSIT, any(), any(), null) } returns flowOf(emptyList())
        every { transactionRepository.observeTransactions(accountId = null, startTime = any(), endTime = any()) } returns flowOf(emptyList())

        useCase(period = ReportPeriod.MONTHLY, referenceTimestamp = 1700000000000L).test {
            val report = awaitItem()
            assertThat(report.period).isEqualTo(ReportPeriod.MONTHLY)
            assertThat(report.summary.totalIncome).isEqualTo(200L)
            assertThat(report.summary.netFlow).isEqualTo(150L)
            awaitComplete()
        }
    }
}
