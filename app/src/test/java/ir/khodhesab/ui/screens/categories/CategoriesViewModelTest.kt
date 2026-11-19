package ir.khodhesab.ui.screens.categories

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import ir.khodhesab.domain.model.Category
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.domain.repository.CategoryRepository
import ir.khodhesab.domain.usecase.ApplyKeywordRulesUseCase
import ir.khodhesab.domain.usecase.DetectInternalTransfersUseCase
import ir.khodhesab.domain.usecase.ManageCategoryUseCase
import ir.khodhesab.util.MainDispatcherRule
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [CategoriesViewModel].
 */
class CategoriesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val categoryRepository: CategoryRepository = mockk()
    private val manageCategoryUseCase: ManageCategoryUseCase = mockk()
    private val applyKeywordRulesUseCase: ApplyKeywordRulesUseCase = mockk()
    private val detectInternalTransfersUseCase: DetectInternalTransfersUseCase = mockk()

    private lateinit var viewModel: CategoriesViewModel

    private val category = Category(id = 1L, name = "رستوران", kind = TransactionKind.WITHDRAWAL, iconKey = "food", colorHex = "#RED", isDefault = true)

    @Before
    fun setup() {
        every { categoryRepository.observeCategoriesByKind(any()) } returns flowOf(listOf(category))
        every { detectInternalTransfersUseCase.detectTransferPairs() } returns flowOf(emptyList())

        viewModel = CategoriesViewModel(
            categoryRepository,
            manageCategoryUseCase,
            applyKeywordRulesUseCase,
            detectInternalTransfersUseCase
        )
    }

    @Test
    fun uiState_emitsInitialCategories() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.categories).hasSize(1)
            assertThat(state.selectedKind).isEqualTo(TransactionKind.WITHDRAWAL)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onRunRetroactiveAutoCategorization_invokesUseCaseAndSetsFeedback() = runTest {
        coEvery { applyKeywordRulesUseCase() } returns 5

        viewModel.onRunRetroactiveAutoCategorization()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.feedbackMessage).contains("۵ تراکنش")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onStartCreateCategory_setsIsCreatingCategoryTrue() = runTest {
        viewModel.onStartCreateCategory()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isCreatingCategory).isTrue()
            assertThat(state.editingCategory).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
