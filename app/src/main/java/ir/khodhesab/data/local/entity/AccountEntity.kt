package ir.khodhesab.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a bank account or card belonging to a user.
 *
 * @property id Auto-generated primary key.
 * @property bankId Foreign key referencing [BankEntity.id].
 * @property accountIdentifier Account number or card number (e.g., last 4 digits `"1234"` or full account number).
 * @property title User-friendly display title (e.g., `"حساب اصلی ملت"`).
 * @property currentBalance Latest known account balance in Rials.
 * @property lastBalanceUpdate Timestamp (epoch millis) of the latest transaction that updated [currentBalance].
 * @property isActive Flag indicating whether the account is currently active.
 */
@Entity(
    tableName = "accounts",
    foreignKeys = [
        ForeignKey(
            entity = BankEntity::class,
            parentColumns = ["id"],
            childColumns = ["bankId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["bankId"]),
        Index(value = ["bankId", "accountIdentifier"], unique = true)
    ]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bankId: Long,
    val accountIdentifier: String,
    val title: String,
    val currentBalance: Long = 0L,
    val lastBalanceUpdate: Long = 0L,
    val isActive: Boolean = true
)
