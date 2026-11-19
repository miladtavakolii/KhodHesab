package ir.khodhesab.ui.screens.reports

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import ir.khodhesab.data.backup.DatabaseBackupManager
import ir.khodhesab.data.export.CsvReportExporter
import ir.khodhesab.data.preferences.AppPreferencesRepository
import ir.khodhesab.domain.model.CurrencyUnit
import ir.khodhesab.domain.model.FinancialSummary
import ir.khodhesab.domain.model.ReportPeriod
import ir.khodhesab.domain.repository.AccountRepository
import ir.khodhesab.domain.usecase.FinancialReport
import ir.khodhesab.domain.usecase.GetFinancialReportUseCase
import ir.khodhesab.domain.usecase.GetTrendSeriesUseCase
import ir.khodhesab.util.MainDispatcherRule
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [ReportsViewModel].
 */
class ReportsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getFinancialReportUseCase: GetFinancialReportUseCase = mockk()
    private val getTrendSeriesUseCase: GetTrendSeriesUseCase = mockk()
    private val accountRepository: AccountRepository = mockk()
    private val appPreferencesRepository: AppPreferencesRepository = mockk()
    private val csvReportExporter: CsvReportExporter = mockk()
    private val databaseBackupManager: DatabaseBackupManager = mockk()

    private lateinit var viewModel: ReportsViewModel

    private val sampleReport = FinancialReport(
        period = ReportPeriod.MONTHLY,
        startTime = 1000L,
        endTime = 2000L,
        summary = FinancialSummary(totalIncome = 500_000L, totalExpense = 200_000L, transactionCount = 3),
        expenseBreakdown = emptyList(),
        incomeBreakdown = emptyList(),
        transactions = emptyList()
    )

    @Before
    fun setup() {
        every { getFinancialReportUseCase(any(), any(), any()) } returns flowOf(sampleReport)
        every { getTrendSeriesUseCase(any(), any(), any()) } returns flowOf(emptyList())
        every { accountRepository.observeAllAccounts() } returns flowOf(emptyList())
        every { appPreferencesRepository.currencyUnit } returns flowOf(CurrencyUnit.TOMAN)

        viewModel = ReportsViewModel(
            getFinancialReportUseCase,
            getTrendSeriesUseCase,
            accountRepository,
            appPreferencesRepository,
            csvReportExporter,
            databaseBackupManager
        )
    }

    @Test
    fun uiState_emitsInitialMonthlyReport() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.selectedPeriod).isEqualTo(ReportPeriod.MONTHLY)
            assertThat(state.summary.totalIncome).isEqualTo(500_000L)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onSelectPeriod_updatesPeriodState() = runTest {
        viewModel.onSelectPeriod(ReportPeriod.DAILY)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.selectedPeriod).isEqualTo(ReportPeriod.DAILY)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
