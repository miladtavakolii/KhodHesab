package ir.khodhesab.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ir.khodhesab.data.local.model.TransactionType

/**
 * Represents a single financial transaction record.
 *
 * @property id Auto-generated primary key.
 * @property accountId Foreign key referencing [AccountEntity.id].
 * @property categoryId Foreign key referencing nullable [CategoryEntity.id].
 * @property type The transaction type ([TransactionType.DEPOSIT], [TransactionType.WITHDRAWAL], or [TransactionType.TRANSFER]).
 * @property amount Transaction amount in Rials (must be positive).
 * @property balanceAfter Remaining balance after this transaction in Rials, if reported by SMS.
 * @property timestamp Transaction timestamp in epoch milliseconds.
 * @property description Optional user note or description.
 * @property rawSmsBody Original SMS message text if created via SMS import.
 * @property smsHash Unique SHA-256 hash of SMS to prevent duplicate imports. Nullable for manual transactions.
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["accountId"]),
        Index(value = ["categoryId"]),
        Index(value = ["timestamp"]),
        Index(value = ["smsHash"], unique = true)
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountId: Long,
    val categoryId: Long? = null,
    val type: TransactionType,
    val amount: Long,
    val balanceAfter: Long? = null,
    val timestamp: Long,
    val description: String? = null,
    val rawSmsBody: String? = null,
    val smsHash: String? = null
)
