package ir.khodhesab.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import ir.khodhesab.core.date.JalaliCalendarHelper
import ir.khodhesab.domain.model.Account
import ir.khodhesab.domain.model.Bank
import ir.khodhesab.domain.model.ReportPeriod
import ir.khodhesab.domain.model.Transaction
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [GetTrendSeriesUseCase].
 */
class GetTrendSeriesUseCaseTest {

    private val transactionRepository: TransactionRepository = mockk()
    private lateinit var getTrendSeriesUseCase: GetTrendSeriesUseCase

    private val bank = Bank(id = 1, code = "MELLAT", persianName = "ملت", colorHex = "#R", iconKey = "i")
    private val account = Account(id = 10, bank = bank, accountIdentifier = "1234", title = "ملت")

    @Before
    fun setup() {
        getTrendSeriesUseCase = GetTrendSeriesUseCase(transactionRepository)
    }

    @Test
    fun invoke_dailyPeriod_returns7DayBuckets() = runTest {
        val now = System.currentTimeMillis()
        val (weekStart, _) = JalaliCalendarHelper.getWeekRange(now)

        val txIncome = Transaction(id = 1, account = account, kind = TransactionKind.DEPOSIT, amount = 100_000L, timestamp = weekStart + 1000L)
        val txTransfer = Transaction(id = 2, account = account, kind = TransactionKind.TRANSFER, amount = 500_000L, timestamp = weekStart + 2000L)

        every { transactionRepository.observeTransactions(any(), any(), any(), any(), any()) } returns flowOf(listOf(txIncome, txTransfer))

        getTrendSeriesUseCase(ReportPeriod.DAILY, now).test {
            val buckets = awaitItem()
            assertThat(buckets).hasSize(7)
            assertThat(buckets.first().label).isEqualTo("شنبه")
            assertThat(buckets.first().totalIncome).isEqualTo(100_000L)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun invoke_monthlyPeriod_returns6MonthBuckets() = runTest {
        every { transactionRepository.observeTransactions(any(), any(), any(), any(), any()) } returns flowOf(emptyList())

        getTrendSeriesUseCase(ReportPeriod.MONTHLY).test {
            val buckets = awaitItem()
            assertThat(buckets).hasSize(6)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
