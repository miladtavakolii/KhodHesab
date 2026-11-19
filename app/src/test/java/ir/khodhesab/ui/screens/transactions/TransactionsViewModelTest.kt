package ir.khodhesab.ui.screens.transactions

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import ir.khodhesab.data.preferences.AppPreferencesRepository
import ir.khodhesab.domain.model.Account
import ir.khodhesab.domain.model.Bank
import ir.khodhesab.domain.model.Category
import ir.khodhesab.domain.model.CurrencyUnit
import ir.khodhesab.domain.model.Transaction
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.domain.repository.AccountRepository
import ir.khodhesab.domain.repository.TransactionRepository
import ir.khodhesab.util.MainDispatcherRule
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [TransactionsViewModel].
 */
class TransactionsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val transactionRepository: TransactionRepository = mockk()
    private val accountRepository: AccountRepository = mockk()
    private val appPreferencesRepository: AppPreferencesRepository = mockk()

    private lateinit var viewModel: TransactionsViewModel

    private val bank = Bank(id = 1, code = "MELLAT", persianName = "ملت", colorHex = "#R", iconKey = "i")
    private val account = Account(id = 10, bank = bank, accountIdentifier = "1234", title = "ملت")
    private val category = Category(id = 5, name = "غذا", kind = TransactionKind.WITHDRAWAL, iconKey = "food", colorHex = "#R")

    private val txCategorized = Transaction(id = 1, account = account, category = category, kind = TransactionKind.WITHDRAWAL, amount = 1000L, timestamp = 1000L)
    private val txUncategorized = Transaction(id = 2, account = account, category = null, kind = TransactionKind.WITHDRAWAL, amount = 2000L, timestamp = 2000L)

    @Before
    fun setup() {
        every { accountRepository.observeAllAccounts() } returns flowOf(listOf(account))
        every { appPreferencesRepository.currencyUnit } returns flowOf(CurrencyUnit.TOMAN)
        every { transactionRepository.observeTransactions(any(), any(), any(), any(), any()) } returns flowOf(listOf(txCategorized, txUncategorized))

        viewModel = TransactionsViewModel(transactionRepository, accountRepository, appPreferencesRepository)
    }

    @Test
    fun uiState_emitsInitialAllTransactions() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.transactions).hasSize(2)
            assertThat(state.onlyUncategorized).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onToggleUncategorizedOnly_filtersOutCategorizedTransactions() = runTest {
        viewModel.onToggleUncategorizedOnly()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.onlyUncategorized).isTrue()
            assertThat(state.transactions).hasSize(1)
            assertThat(state.transactions.first().id).isEqualTo(2L)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onClearFilters_resetsSearchAndFilters() = runTest {
        viewModel.onSearchQueryChange("خرید")
        viewModel.onToggleUncategorizedOnly()
        viewModel.onClearFilters()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.searchQuery).isEmpty()
            assertThat(state.onlyUncategorized).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
