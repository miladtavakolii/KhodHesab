package ir.khodhesab.ui.screens.transactions

import ir.khodhesab.domain.model.Account
import ir.khodhesab.domain.model.CurrencyUnit
import ir.khodhesab.domain.model.Transaction
import ir.khodhesab.domain.model.TransactionKind

/**
 * UI state for the Transactions list and filtering screen.
 *
 * @property isLoading `true` while loading transactions.
 * @property searchQuery Text search query matching transaction note or raw SMS body.
 * @property selectedAccountId Selected account filter (`null` for all accounts).
 * @property selectedKind Selected transaction kind filter (`null` for all kinds).
 * @property onlyUncategorized `true` if filtering only transactions without a category.
 * @property accounts List of available user accounts for filter selection.
 * @property transactions List of filtered transactions matching criteria.
 * @property currencyUnit Preferred display [CurrencyUnit].
 */
data class TransactionsUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val selectedAccountId: Long? = null,
    val selectedKind: TransactionKind? = null,
    val onlyUncategorized: Boolean = false,
    val accounts: List<Account> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val currencyUnit: CurrencyUnit = CurrencyUnit.TOMAN
)
