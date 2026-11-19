package ir.khodhesab.ui.screens.transaction_detail

import ir.khodhesab.domain.model.Account
import ir.khodhesab.domain.model.Category
import ir.khodhesab.domain.model.CurrencyUnit
import ir.khodhesab.domain.model.Transaction
import ir.khodhesab.domain.model.TransactionKind

/**
 * UI State for viewing and editing a single transaction, or adding a manual entry.
 */
data class TransactionDetailUiState(
    val isLoading: Boolean = false,
    val transaction: Transaction? = null,
    val categoriesOfKind: List<Category> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val selectedCategoryId: Long? = null,
    val selectedKind: TransactionKind = TransactionKind.WITHDRAWAL,
    val description: String = "",
    val currencyUnit: CurrencyUnit = CurrencyUnit.TOMAN,
    val isDismissed: Boolean = false
)
