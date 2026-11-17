package ir.khodhesab.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ir.khodhesab.data.local.model.TransactionType

/**
 * Represents a transaction category for financial accounting.
 *
 * @property id Auto-generated primary key.
 * @property name Category display name (e.g., `"خوراک و رستوران"`).
 * @property type Associated [TransactionType] (`DEPOSIT` or `WITHDRAWAL`).
 * @property iconKey Icon identifier for UI display.
 * @property colorHex Color code in hex format (e.g., `"#FF5722"`).
 * @property isDefault `true` for system pre-seeded categories, `false` for custom user categories.
 * @property keywords List of Persian/English keywords used for automatic SMS categorisation.
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: TransactionType,
    val iconKey: String,
    val colorHex: String,
    val isDefault: Boolean = false,
    val keywords: List<String> = emptyList()
)
