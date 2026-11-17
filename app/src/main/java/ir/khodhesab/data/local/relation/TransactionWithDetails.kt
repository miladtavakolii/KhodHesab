package ir.khodhesab.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import ir.khodhesab.data.local.entity.AccountEntity
import ir.khodhesab.data.local.entity.CategoryEntity
import ir.khodhesab.data.local.entity.TransactionEntity

/**
 * Relational projection combining a [TransactionEntity] with its associated [AccountWithBank] and optional [CategoryEntity].
 */
data class TransactionWithDetails(
    @Embedded
    val transaction: TransactionEntity,

    @Relation(
        entity = AccountEntity::class,
        parentColumn = "accountId",
        entityColumn = "id"
    )
    val accountWithBank: AccountWithBank,

    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity?
)
