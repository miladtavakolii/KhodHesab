package ir.khodhesab.data.local.relation

/**
 * Aggregation projection data class for expense/income breakdown reports by category.
 *
 * @property categoryId Unique category identifier (nullable for uncategorized transactions).
 * @property categoryName Display name of the category.
 * @property colorHex Color code in hex format.
 * @property iconKey Icon identifier key.
 * @property totalAmount Total aggregated financial amount in Rials.
 * @property transactionCount Count of transactions in this category.
 */
data class CategoryExpenseSummary(
    val categoryId: Long?,
    val categoryName: String?,
    val colorHex: String?,
    val iconKey: String?,
    val totalAmount: Long,
    val transactionCount: Int
)
