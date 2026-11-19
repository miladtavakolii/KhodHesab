package ir.khodhesab.ui.screens.dashboard

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import ir.khodhesab.data.preferences.AppPreferencesRepository
import ir.khodhesab.domain.model.Account
import ir.khodhesab.domain.model.Bank
import ir.khodhesab.domain.model.CurrencyUnit
import ir.khodhesab.domain.model.FinancialSummary
import ir.khodhesab.domain.repository.AccountRepository
import ir.khodhesab.domain.repository.TransactionRepository
import ir.khodhesab.domain.usecase.GetFinancialReportUseCase
import ir.khodhesab.domain.usecase.ManageAccountUseCase
import ir.khodhesab.sms_engine.importer.OldSmsImporter
import ir.khodhesab.sms_engine.importer.SmsImportProgress
import ir.khodhesab.util.MainDispatcherRule
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [DashboardViewModel].
 */
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val accountRepository: AccountRepository = mockk()
    private val transactionRepository: TransactionRepository = mockk()
    private val getFinancialReportUseCase: GetFinancialReportUseCase = mockk()
    private val manageAccountUseCase: ManageAccountUseCase = mockk()
    private val appPreferencesRepository: AppPreferencesRepository = mockk(relaxed = true)
    private val oldSmsImporter: OldSmsImporter = mockk()

    private lateinit var viewModel: DashboardViewModel

    private val bank = Bank(id = 1, code = "MELLAT", persianName = "بانک ملت", colorHex = "#RED", iconKey = "ic")
    private val account1 = Account(id = 10L, bank = bank, accountIdentifier = "1234", title = "حساب ۱", currentBalance = 1_000_000L)
    private val account2 = Account(id = 20L, bank = bank, accountIdentifier = "5678", title = "حساب ۲", currentBalance = 500_000L)

    @Before
    fun setup() {
        every { accountRepository.observeAllAccounts() } returns flowOf(listOf(account1, account2))
        every { accountRepository.observeTotalActiveBalance() } returns flowOf(1_500_000L)
        every { appPreferencesRepository.currencyUnit } returns flowOf(CurrencyUnit.TOMAN)
        every { appPreferencesRepository.isInitialSmsSyncDone } returns flowOf(true)

        every { transactionRepository.observePeriodSummary(any(), any(), any()) } returns flowOf(
            FinancialSummary(totalIncome = 300_000L, totalExpense = 100_000L, transactionCount = 2)
        )
        every { transactionRepository.observeTransactions(accountId = any()) } returns flowOf(emptyList())

        viewModel = DashboardViewModel(
            accountRepository,
            transactionRepository,
            getFinancialReportUseCase,
            manageAccountUseCase,
            appPreferencesRepository,
            oldSmsImporter
        )
    }

    @Test
    fun uiState_emitsInitialStateWithTotalActiveBalance() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.totalBalance).isEqualTo(1_500_000L)
            assertThat(state.accounts).hasSize(2)
            assertThat(state.currencyUnit).isEqualTo(CurrencyUnit.TOMAN)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onSelectAccount_filtersBalanceAndTransactionsByAccountId() = runTest {
        viewModel.onSelectAccount(10L)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.selectedAccountId).isEqualTo(10L)
            assertThat(state.totalBalance).isEqualTo(1_000_000L)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onStartHistoricalSmsSync_updatesProgressAndSetsStatusMessageOnCompletion() = runTest {
        val progress1 = SmsImportProgress(totalMessages = 10, processedCount = 5, isCompleted = false)
        val progress2 = SmsImportProgress(totalMessages = 10, processedCount = 10, recordedCount = 3, isCompleted = true)

        every { oldSmsImporter.importHistoricalSms() } returns flowOf(progress1, progress2)
        coEvery { appPreferencesRepository.setInitialSmsSyncDone(true) } returns Unit

        viewModel.onStartHistoricalSmsSync()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.syncStatusMessage).contains("۳ تراکنش جدید")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onRenameAccount_delegatesToManageAccountUseCase() = runTest {
        coEvery { manageAccountUseCase.renameAccount(10L, "عنوان جدید") } returns Result.success(Unit)

        viewModel.onRenameAccount(10L, "عنوان جدید")

        coEvery { manageAccountUseCase.renameAccount(10L, "عنوان جدید") }
    }
}
