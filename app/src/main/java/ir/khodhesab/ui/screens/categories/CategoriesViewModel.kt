package ir.khodhesab.ui.screens.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.khodhesab.ui.util.PersianCurrencyFormatter
import ir.khodhesab.domain.model.Category
import ir.khodhesab.domain.model.InternalTransferPair
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.domain.repository.CategoryRepository
import ir.khodhesab.domain.usecase.ApplyKeywordRulesUseCase
import ir.khodhesab.domain.usecase.DetectInternalTransfersUseCase
import ir.khodhesab.domain.usecase.ManageCategoryUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel managing category customization, keyword rules, and internal transfer detection.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val manageCategoryUseCase: ManageCategoryUseCase,
    private val applyKeywordRulesUseCase: ApplyKeywordRulesUseCase,
    private val detectInternalTransfersUseCase: DetectInternalTransfersUseCase
) : ViewModel() {

    private val selectedKindState = MutableStateFlow(TransactionKind.WITHDRAWAL)
    private val editingCategoryState = MutableStateFlow<Category?>(null)
    private val isCreatingCategoryState = MutableStateFlow(false)
    private val feedbackMessageState = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CategoriesUiState> = combine(
        selectedKindState,
        editingCategoryState,
        isCreatingCategoryState,
        feedbackMessageState
    ) { kind, editingCat, isCreating, feedbackMsg ->
        Params(kind, editingCat, isCreating, feedbackMsg)
    }.flatMapLatest { p ->
        combine(
            categoryRepository.observeCategoriesByKind(p.kind),
            detectInternalTransfersUseCase.detectTransferPairs()
        ) { categoryList, transferPairs ->
            CategoriesUiState(
                isLoading = false,
                selectedKind = p.kind,
                categories = categoryList,
                detectedTransferPairs = transferPairs,
                editingCategory = p.editingCat,
                isCreatingCategory = p.isCreating,
                feedbackMessage = p.feedbackMsg
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = CategoriesUiState()
    )

    fun onSelectKind(kind: TransactionKind) {
        selectedKindState.value = kind
    }

    fun onStartCreateCategory() {
        editingCategoryState.value = null
        isCreatingCategoryState.value = true
    }

    fun onStartEditCategory(category: Category) {
        isCreatingCategoryState.value = false
        editingCategoryState.value = category
    }

    fun onDismissEditor() {
        editingCategoryState.value = null
        isCreatingCategoryState.value = false
    }

    fun onSaveCategory(
        name: String,
        iconKey: String,
        colorHex: String,
        keywords: List<String>
    ) {
        viewModelScope.launch {
            val editing = editingCategoryState.value
            if (isCreatingCategoryState.value) {
                manageCategoryUseCase.createCustomCategory(
                    name = name,
                    kind = selectedKindState.value,
                    iconKey = iconKey,
                    colorHex = colorHex,
                    keywords = keywords
                )
                feedbackMessageState.value = "دسته‌بندی جدید با موفقیت ایجاد شد."
            } else if (editing != null) {
                val updated = editing.copy(
                    name = if (!editing.isDefault) name.trim() else editing.name,
                    iconKey = iconKey,
                    colorHex = colorHex,
                    keywords = keywords
                )
                categoryRepository.updateCategory(updated)
                feedbackMessageState.value = "تغییرات دسته‌بندی با موفقیت ذخیره شد."
            }
            onDismissEditor()
        }
    }

    fun onDeleteCategory(categoryId: Long) {
        viewModelScope.launch {
            manageCategoryUseCase.deleteCustomCategory(categoryId)
            feedbackMessageState.value = "دسته‌بندی سفارشی حذف شد."
            onDismissEditor()
        }
    }

    fun onRunRetroactiveAutoCategorization() {
        viewModelScope.launch {
            val count = applyKeywordRulesUseCase()
            val countPersian = PersianCurrencyFormatter.toPersianDigits(count.toString())
            feedbackMessageState.value = if (count > 0) {
                "$countPersian تراکنش دسته‌بندی نشده طبق کلیدواژه‌ها تنظیم شدند."
            } else {
                "تراکنش جدیدی برای دسته‌بندی طبق کلیدواژه‌ها یافت نشد."
            }
        }
    }

    fun onConfirmInternalTransfer(pair: InternalTransferPair) {
        viewModelScope.launch {
            detectInternalTransfersUseCase.confirmTransferPair(pair)
            feedbackMessageState.value = "تراکنش به عنوان انتقال بین حساب‌ها علامت‌گذاری شد."
        }
    }

    fun onDismissFeedback() {
        feedbackMessageState.value = null
    }

    private data class Params(
        val kind: TransactionKind,
        val editingCat: Category?,
        val isCreating: Boolean,
        val feedbackMsg: String?
    )
}
