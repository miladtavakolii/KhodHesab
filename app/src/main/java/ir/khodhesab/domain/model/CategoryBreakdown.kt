package ir.khodhesab.domain.model

/**
 * Pure domain model representing expense or income breakdown for a single category.
 *
 * @property category Associated [Category] (null for uncategorized transactions).
 * @property totalAmount Aggregated financial amount in Rials.
 * @property transactionCount Count of transactions in this category.
 * @property percentage Percentage of total period spending/income (0.0 to 100.0).
 */
data class CategoryBreakdown(
    val category: Category?,
    val totalAmount: Long,
    val transactionCount: Int,
    val percentage: Float
)
