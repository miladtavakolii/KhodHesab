package ir.khodhesab.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a financial institution/bank in Iran.
 *
 * @property id Auto-generated primary key.
 * @property code Unique bank identifier code (e.g. `"MELLAT"`, `"MELLI"`, `"BLU"`).
 * @property persianName Official Persian name of the bank (e.g. `"بانک ملت"`).
 * @property senderNumbers Known SMS sender shortcodes or numbers associated with this bank.
 * @property colorHex Brand primary accent color hex code (e.g. `"#D32F2F"`).
 * @property iconKey Icon asset key used for UI mapping.
 */
@Entity(
    tableName = "banks",
    indices = [
        Index(value = ["code"], unique = true)
    ]
)
data class BankEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val code: String,
    val persianName: String,
    val senderNumbers: List<String>,
    val colorHex: String,
    val iconKey: String
)
