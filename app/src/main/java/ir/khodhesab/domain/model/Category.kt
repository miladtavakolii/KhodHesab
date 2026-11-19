package ir.khodhesab.domain.model

/**
 * Pure domain model representing a transaction category.
 *
 * @property id Unique category identifier.
 * @property name Category display name.
 * @property kind Associated [TransactionKind].
 * @property iconKey UI icon mapping key.
 * @property colorHex Category hex color.
 * @property isDefault `true` for system categories, `false` for custom categories.
 * @property keywords Keywords used for SMS auto-categorization.
 */
data class Category(
    val id: Long = 0,
    val name: String,
    val kind: TransactionKind,
    val iconKey: String,
    val colorHex: String,
    val isDefault: Boolean = false,
    val keywords: List<String> = emptyList()
)
