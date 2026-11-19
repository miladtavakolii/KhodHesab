package ir.khodhesab.data.mapper

import ir.khodhesab.data.local.entity.CategoryEntity
import ir.khodhesab.data.local.model.TransactionType
import ir.khodhesab.domain.model.Category
import ir.khodhesab.domain.model.TransactionKind

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    kind = type.toDomain(),
    iconKey = iconKey,
    colorHex = colorHex,
    isDefault = isDefault,
    keywords = keywords
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    type = kind.toEntity(),
    iconKey = iconKey,
    colorHex = colorHex,
    isDefault = isDefault,
    keywords = keywords
)

fun TransactionType.toDomain(): TransactionKind = when (this) {
    TransactionType.DEPOSIT -> TransactionKind.DEPOSIT
    TransactionType.WITHDRAWAL -> TransactionKind.WITHDRAWAL
    TransactionType.TRANSFER -> TransactionKind.TRANSFER
}

fun TransactionKind.toEntity(): TransactionType = when (this) {
    TransactionKind.DEPOSIT -> TransactionType.DEPOSIT
    TransactionKind.WITHDRAWAL -> TransactionType.WITHDRAWAL
    TransactionKind.TRANSFER -> TransactionType.TRANSFER
}
