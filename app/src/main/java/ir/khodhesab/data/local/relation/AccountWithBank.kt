package ir.khodhesab.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import ir.khodhesab.data.local.entity.AccountEntity
import ir.khodhesab.data.local.entity.BankEntity

/**
 * Relational projection combining an [AccountEntity] with its parent [BankEntity].
 */
data class AccountWithBank(
    @Embedded
    val account: AccountEntity,

    @Relation(
        parentColumn = "bankId",
        entityColumn = "id"
    )
    val bank: BankEntity
)
