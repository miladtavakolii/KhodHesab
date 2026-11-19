package ir.khodhesab.ui.screens.categories

import ir.khodhesab.domain.model.Category
import ir.khodhesab.domain.model.InternalTransferPair
import ir.khodhesab.domain.model.TransactionKind

/**
 * UI State for the Category Management screen.
 *
 * @property isLoading `true` while loading category list.
 * @property selectedKind Active transaction kind tab ([TransactionKind.WITHDRAWAL] or [TransactionKind.DEPOSIT]).
 * @property categories List of categories matching [selectedKind].
 * @property detectedTransferPairs Detected candidate internal transfer pairs across accounts.
 * @property editingCategory Currently selected category for editing, or `null` when idle.
 * @property isCreatingCategory `true` when modal editor is in creation mode for a new custom category.
 * @property feedbackMessage Status message to display in UI Snackbar.
 */
data class CategoriesUiState(
    val isLoading: Boolean = true,
    val selectedKind: TransactionKind = TransactionKind.WITHDRAWAL,
    val categories: List<Category> = emptyList(),
    val detectedTransferPairs: List<InternalTransferPair> = emptyList(),
    val editingCategory: Category? = null,
    val isCreatingCategory: Boolean = false,
    val feedbackMessage: String? = null
)
