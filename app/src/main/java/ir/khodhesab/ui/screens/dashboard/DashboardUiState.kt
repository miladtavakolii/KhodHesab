package ir.khodhesab.ui.screens.dashboard

import ir.khodhesab.domain.model.Account
import ir.khodhesab.domain.model.CurrencyUnit
import ir.khodhesab.domain.model.FinancialSummary
import ir.khodhesab.domain.model.Transaction
import ir.khodhesab.sms_engine.importer.SmsImportProgress

/**
 * UI State for the Dashboard screen.
 *
 * @property isLoading `true` while initial data is loading.
 * @property totalBalance Total active balance across active accounts (or selected account balance).
 * @property accounts List of all user accounts.
 * @property selectedAccountId Selected account ID for filtering (`null` represents "All Accounts").
 * @property periodSummary Monthly Jalali financial summary for current selection.
 * @property recentTransactions Recent transaction records for current selection.
 * @property currencyUnit User preferred [CurrencyUnit] (`TOMAN` or `RIAL`).
 * @property isInitialSmsSyncDone Flag indicating if historical SMS import has been executed.
 * @property smsSyncProgress Progress data while scanning historical SMS, or `null` when idle.
 * @property syncStatusMessage Status notification message after SMS sync completion.
 */
data class DashboardUiState(
    val isLoading: Boolean = true,
    val totalBalance: Long = 0L,
    val accounts: List<Account> = emptyList(),
    val selectedAccountId: Long? = null,
    val periodSummary: FinancialSummary = FinancialSummary(0L, 0L, 0),
    val recentTransactions: List<Transaction> = emptyList(),
    val currencyUnit: CurrencyUnit = CurrencyUnit.TOMAN,
    val isInitialSmsSyncDone: Boolean = false,
    val smsSyncProgress: SmsImportProgress? = null,
    val syncStatusMessage: String? = null
)
