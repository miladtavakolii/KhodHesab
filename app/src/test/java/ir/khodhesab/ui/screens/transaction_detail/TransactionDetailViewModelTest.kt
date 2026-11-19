package ir.khodhesab.ui.screens.transaction_detail

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
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
import ir.khodhesab.domain.repository.CategoryRepository
import ir.khodhesab.domain.repository.TransactionRepository
import ir.khodhesab.domain.usecase.AddManualTransactionUseCase
import ir.khodhesab.domain.usecase.UpdateTransactionUseCase
import ir.khodhesab.util.MainDispatcherRule
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [TransactionDetailViewModel].
 */
class TransactionDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val transactionRepository: TransactionRepository = mockk()
    private val categoryRepository: CategoryRepository = mockk()
    private val accountRepository: AccountRepository = mockk()
    private val updateTransactionUseCase: UpdateTransactionUseCase = mockk()
    private val addManualTransactionUseCase: AddManualTransactionUseCase = mockk()
    private val appPreferencesRepository: AppPreferencesRepository = mockk()

    private lateinit var viewModel: TransactionDetailViewModel

    private val bank = Bank(id = 1, code = "MELLAT", persianName = "ملت", colorHex = "#R", iconKey = "i")
    private val account = Account(id = 10, bank = bank, accountIdentifier = "1234", title = "ملت")
    private val category = Category(id = 5, name = "غذا", kind = TransactionKind.WITHDRAWAL, iconKey = "food", colorHex = "#R")
    private val transaction = Transaction(id = 100, account = account, category = category, kind = TransactionKind.WITHDRAWAL, amount = 50_000L, timestamp = 1000L)

    @Before
    fun setup() {
        every { transactionRepository.observeTransactions() } returns flowOf(listOf(transaction))
        every { categoryRepository.observeCategoriesByKind(any()) } returns flowOf(listOf(category))
        every { accountRepository.observeAllAccounts() } returns flowOf(listOf(account))
        every { appPreferencesRepository.currencyUnit } returns flowOf(CurrencyUnit.TOMAN)

        viewModel = TransactionDetailViewModel(
            transactionRepository,
            categoryRepository,
            accountRepository,
            updateTransactionUseCase,
            addManualTransactionUseCase,
            appPreferencesRepository
        )
    }

    @Test
    fun loadTransaction_updatesUiStateWithTransactionDetails() = runTest {
        viewModel.loadTransaction(100L)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.transaction?.id).isEqualTo(100L)
            assertThat(state.selectedCategoryId).isEqualTo(5L)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onSaveTransaction_invokesUpdateUseCaseAndDismisses() = runTest {
        coEvery { updateTransactionUseCase.updateCategoryAndNote(any(), any(), any()) } returns Unit

        viewModel.loadTransaction(100L)
        viewModel.uiState.test {
            awaitItem()
            viewModel.onSaveTransaction()
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { updateTransactionUseCase.updateCategoryAndNote(100L, any(), any()) }
    }

    @Test
    fun onDeleteTransaction_invokesDeleteUseCase() = runTest {
        coEvery { updateTransactionUseCase.deleteTransaction(100L) } returns Unit

        viewModel.loadTransaction(100L)
        viewModel.onDeleteTransaction()

        coVerify { updateTransactionUseCase.deleteTransaction(100L) }
    }
}
