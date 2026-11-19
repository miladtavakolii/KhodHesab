package ir.khodhesab.domain.usecase

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.domain.repository.AccountRepository
import ir.khodhesab.domain.repository.CategoryRepository
import ir.khodhesab.domain.repository.TransactionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for management use cases ([ManageAccountUseCase], [ManageCategoryUseCase], [AddManualTransactionUseCase]).
 */
class ManageAccountAndCategoryUseCaseTest {

    private val accountRepository: AccountRepository = mockk()
    private val categoryRepository: CategoryRepository = mockk()
    private val transactionRepository: TransactionRepository = mockk()

    private lateinit var manageAccountUseCase: ManageAccountUseCase
    private lateinit var manageCategoryUseCase: ManageCategoryUseCase
    private lateinit var addManualTransactionUseCase: AddManualTransactionUseCase

    @Before
    fun setup() {
        manageAccountUseCase = ManageAccountUseCase(accountRepository)
        manageCategoryUseCase = ManageCategoryUseCase(categoryRepository)
        addManualTransactionUseCase = AddManualTransactionUseCase(transactionRepository, accountRepository)
    }

    @Test
    fun renameAccount_validatesNonBlankTitle() = runTest {
        val blankResult = manageAccountUseCase.renameAccount(1L, "   ")
        assertThat(blankResult.isFailure).isTrue()

        coEvery { accountRepository.updateAccountTitle(1L, "حساب پس‌انداز") } returns Unit
        val validResult = manageAccountUseCase.renameAccount(1L, " حساب پس‌انداز ")
        assertThat(validResult.isSuccess).isTrue()
    }

    @Test
    fun createCustomCategory_validatesNonBlankName() = runTest {
        val blankResult = manageCategoryUseCase.createCustomCategory("  ", TransactionKind.WITHDRAWAL, "icon", "#000", emptyList())
        assertThat(blankResult.isFailure).isTrue()
    }

    @Test
    fun addManualTransaction_rejectsNonPositiveAmount() = runTest {
        val result = addManualTransactionUseCase(
            accountId = 1L,
            categoryId = null,
            kind = TransactionKind.WITHDRAWAL,
            amount = 0L,
            timestamp = 1000L,
            description = null
        )
        assertThat(result.isFailure).isTrue()
    }
}
