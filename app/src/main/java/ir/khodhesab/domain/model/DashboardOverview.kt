package ir.khodhesab.domain.model

/**
 * Pure domain model containing aggregated overview data for main dashboard UI.
 *
 * @property totalActiveBalance Sum of balances across all active accounts.
 * @property accounts List of active user accounts.
 * @property periodSummary Current period financial summary.
 * @property recentTransactions List of recent transactions.
 */
data class DashboardOverview(
    val totalActiveBalance: Long,
    val accounts: List<Account>,
    val periodSummary: FinancialSummary,
    val recentTransactions: List<Transaction>
)
