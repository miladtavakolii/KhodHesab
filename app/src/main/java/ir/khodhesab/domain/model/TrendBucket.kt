package ir.khodhesab.domain.model

/**
 * Pure domain model representing an aggregated time-series bucket for financial trend charts.
 *
 * @property label Short Persian label for chart X-axis (e.g., `"شنبه"`, `"فروردین"`).
 * @property startMillis Start boundary timestamp in epoch milliseconds.
 * @property endMillis End boundary timestamp in epoch milliseconds.
 * @property totalIncome Aggregate income amount in Rials.
 * @property totalExpense Aggregate expense amount in Rials.
 */
data class TrendBucket(
    val label: String,
    val startMillis: Long,
    val endMillis: Long,
    val totalIncome: Long,
    val totalExpense: Long
)
