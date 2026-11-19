package ir.khodhesab.data.mapper

import ir.khodhesab.data.local.entity.AccountEntity
import ir.khodhesab.data.local.relation.AccountWithBank
import ir.khodhesab.domain.model.Account

fun AccountWithBank.toDomain(): Account = Account(
    id = account.id,
    bank = bank.toDomain(),
    accountIdentifier = account.accountIdentifier,
    title = account.title,
    currentBalance = account.currentBalance,
    lastBalanceUpdate = account.lastBalanceUpdate,
    isActive = account.isActive
)

fun Account.toEntity(): AccountEntity = AccountEntity(
    id = id,
    bankId = bank.id,
    accountIdentifier = accountIdentifier,
    title = title,
    currentBalance = currentBalance,
    lastBalanceUpdate = lastBalanceUpdate,
    isActive = isActive
)
