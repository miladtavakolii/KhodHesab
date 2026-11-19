package ir.khodhesab.data.mapper

import ir.khodhesab.data.local.entity.BankEntity
import ir.khodhesab.domain.model.Bank

fun BankEntity.toDomain(): Bank = Bank(
    id = id,
    code = code,
    persianName = persianName,
    senderNumbers = senderNumbers,
    colorHex = colorHex,
    iconKey = iconKey
)

fun Bank.toEntity(): BankEntity = BankEntity(
    id = id,
    code = code,
    persianName = persianName,
    senderNumbers = senderNumbers,
    colorHex = colorHex,
    iconKey = iconKey
)
