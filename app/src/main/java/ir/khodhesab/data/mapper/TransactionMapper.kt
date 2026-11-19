package ir.khodhesab.data.mapper

import ir.khodhesab.data.local.entity.TransactionEntity
import ir.khodhesab.data.local.relation.CategoryExpenseSummary
import ir.khodhesab.data.local.relation.TransactionWithDetails
import ir.khodhesab.domain.model.CategoryBreakdown
import ir.khodhesab.domain.model.Transaction

fun TransactionWithDetails.toDomain(): Transaction = Transaction(
    id = transaction.id,
    account = accountWithBank.toDomain(),
    category = category?.toDomain(),
    kind = transaction.type.toDomain(),
    amount = transaction.amount,
    balanceAfter = transaction.balanceAfter,
    timestamp = transaction.timestamp,
    description = transaction.description,
    rawSmsBody = transaction.rawSmsBody,
    smsHash = transaction.smsHash
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    accountId = account.id,
    categoryId = category?.id,
    type = kind.toEntity(),
    amount = amount,
    balanceAfter = balanceAfter,
    timestamp = timestamp,
    description = description,
    rawSmsBody = rawSmsBody,
    smsHash = smsHash
)

fun CategoryExpenseSummary.toDomain(totalPeriodAmount: Long): CategoryBreakdown {
    val categoryDomain = if (categoryId != null && categoryName != null && colorHex != null && iconKey != null) {
        ir.khodhesab.domain.model.Category(
            id = categoryId,
            name = categoryName,
            kind = ir.khodhesab.domain.model.TransactionKind.WITHDRAWAL,
            iconKey = iconKey,
            colorHex = colorHex
        )
    } else {
        null
    }

    val pct = if (totalPeriodAmount > 0) {
        (totalAmount.toFloat() / totalPeriodAmount.toFloat()) * 100f
    } else {
        0f
    }

    return CategoryBreakdown(
        category = categoryDomain,
        totalAmount = totalAmount,
        transactionCount = transactionCount,
        percentage = pct
    )
}
