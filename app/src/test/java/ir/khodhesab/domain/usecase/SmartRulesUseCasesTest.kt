package ir.khodhesab.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import ir.khodhesab.domain.model.Account
import ir.khodhesab.domain.model.Bank
import ir.khodhesab.domain.model.Category
import ir.khodhesab.domain.model.InternalTransferPair
import ir.khodhesab.domain.model.Transaction
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.domain.repository.CategoryRepository
import ir.khodhesab.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [ApplyKeywordRulesUseCase] and [DetectInternalTransfersUseCase].
 */
class SmartRulesUseCasesTest {

    private val transactionRepository: TransactionRepository = mockk()
    private val categoryRepository: CategoryRepository = mockk()

    private lateinit var applyKeywordRulesUseCase: ApplyKeywordRulesUseCase
    private lateinit var detectInternalTransfersUseCase: DetectInternalTransfersUseCase

    private val bank1 = Bank(id = 1, code = "MELLAT", persianName = "ملت", colorHex = "#R", iconKey = "i")
    private val bank2 = Bank(id = 2, code = "BLU", persianName = "بلو", colorHex = "#B", iconKey = "i")

    private val account1 = Account(id = 10, bank = bank1, accountIdentifier = "1234", title = "حساب ملت")
    private val account2 = Account(id = 20, bank = bank2, accountIdentifier = "5678", title = "حساب بلو")

    @Before
    fun setup() {
        applyKeywordRulesUseCase = ApplyKeywordRulesUseCase(transactionRepository, categoryRepository)
        detectInternalTransfersUseCase = DetectInternalTransfersUseCase(transactionRepository)
    }

    @Test
    fun applyKeywordRulesUseCase_matchesNormalizedKeywordsOnUncategorizedTransactions() = runTest {
        val categoryFood = Category(id = 5, name = "رستوران", kind = TransactionKind.WITHDRAWAL, iconKey = "food", colorHex = "#R", keywords = listOf("اسنپ فود", "کافه"))
        val txUncategorized = Transaction(id = 100, account = account1, category = null, kind = TransactionKind.WITHDRAWAL, amount = 100_000L, timestamp = 1000L, rawSmsBody = "خرید از اسنپ فود")

        every { transactionRepository.observeTransactions() } returns flowOf(listOf(txUncategorized))
        every { categoryRepository.observeAllCategories() } returns flowOf(listOf(categoryFood))
        coEvery { transactionRepository.updateTransactionDetails(100L, 5L, null) } returns Unit

        val matchedCount = applyKeywordRulesUseCase()
        assertThat(matchedCount).isEqualTo(1)
        coVerify(exactly = 1) { transactionRepository.updateTransactionDetails(100L, 5L, null) }
    }

    @Test
    fun detectInternalTransfersUseCase_pairsWithdrawalAndDepositSameAmountDifferentAccountsWithinWindow() = runTest {
        val withdrawal = Transaction(id = 1, account = account1, kind = TransactionKind.WITHDRAWAL, amount = 500_000L, timestamp = 10000L)
        val depositWithinWindow = Transaction(id = 2, account = account2, kind = TransactionKind.DEPOSIT, amount = 500_000L, timestamp = 12000L)

        every { transactionRepository.observeTransactions() } returns flowOf(listOf(withdrawal, depositWithinWindow))

        detectInternalTransfersUseCase.detectTransferPairs().test {
            val pairs = awaitItem()
            assertThat(pairs).hasSize(1)
            assertThat(pairs.first().withdrawalTransaction.id).isEqualTo(1L)
            assertThat(pairs.first().depositTransaction.id).isEqualTo(2L)
            assertThat(pairs.first().amount).isEqualTo(500_000L)
            awaitComplete()
        }
    }

    @Test
    fun confirmTransferPair_updatesBothTransactionsToTransferKind() = runTest {
        val withdrawal = Transaction(id = 1, account = account1, kind = TransactionKind.WITHDRAWAL, amount = 500_000L, timestamp = 10000L)
        val deposit = Transaction(id = 2, account = account2, kind = TransactionKind.DEPOSIT, amount = 500_000L, timestamp = 12000L)
        val pair = InternalTransferPair(withdrawal, deposit, 500_000L, 2000L)

        coEvery { transactionRepository.updateTransactionKind(1L, TransactionKind.TRANSFER) } returns Unit
        coEvery { transactionRepository.updateTransactionKind(2L, TransactionKind.TRANSFER) } returns Unit

        detectInternalTransfersUseCase.confirmTransferPair(pair)

        coVerify(exactly = 1) { transactionRepository.updateTransactionKind(1L, TransactionKind.TRANSFER) }
        coVerify(exactly = 1) { transactionRepository.updateTransactionKind(2L, TransactionKind.TRANSFER) }
    }
}
