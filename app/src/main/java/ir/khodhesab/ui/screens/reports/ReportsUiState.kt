package ir.khodhesab.ui.screens.reports

import android.net.Uri
import ir.khodhesab.domain.model.Account
import ir.khodhesab.domain.model.CategoryBreakdown
import ir.khodhesab.domain.model.CurrencyUnit
import ir.khodhesab.domain.model.FinancialSummary
import ir.khodhesab.domain.model.ReportPeriod
import ir.khodhesab.domain.model.Transaction
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.domain.model.TrendBucket

/**
 * UI State for the Financial Reports & Analytics screen.
 */
data class ReportsUiState(
    val isLoading: Boolean = true,
    val selectedPeriod: ReportPeriod = ReportPeriod.MONTHLY,
    val referenceTimestamp: Long = System.currentTimeMillis(),
    val periodTitle: String = "",
    val accounts: List<Account> = emptyList(),
    val selectedAccountId: Long? = null,
    val selectedBreakdownKind: TransactionKind = TransactionKind.WITHDRAWAL,
    val summary: FinancialSummary = FinancialSummary(0L, 0L, 0),
    val categoryBreakdowns: List<CategoryBreakdown> = emptyList(),
    val trendBuckets: List<TrendBucket> = emptyList(),
    val periodTransactions: List<Transaction> = emptyList(),
    val currencyUnit: CurrencyUnit = CurrencyUnit.TOMAN,
    val shareFileUri: Uri? = null,
    val statusMessage: String? = null
)
