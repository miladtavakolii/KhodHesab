package ir.khodhesab.domain.model

/**
 * Pure domain model representing aggregated financial income, expense, and net flow summary.
 *
 * @property totalIncome Aggregate income (deposits) in Rials.
 * @property totalExpense Aggregate expense (withdrawals) in Rials.
 * @property transactionCount Total number of transactions in summary period.
 */
data class FinancialSummary(
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val transactionCount: Int = 0
) {
    /**
     * Net financial flow calculated as [totalIncome] - [totalExpense].
     */
    val netFlow: Long
        get() = totalIncome - totalExpense
}
