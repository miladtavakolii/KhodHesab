package ir.khodhesab.domain.usecase

import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.domain.repository.AccountRepository
import ir.khodhesab.domain.repository.BankRepository
import ir.khodhesab.domain.repository.CategoryRepository
import ir.khodhesab.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * UseCase encapsulating the complete business logic for processing and recording an SMS bank transaction.
 */
class RecordSmsTransactionUseCase @Inject constructor(
    private val bankRepository: BankRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        senderNumber: String,
        accountIdentifier: String,
        kind: TransactionKind,
        amount: Long,
        balanceAfter: Long?,
        timestamp: Long,
        rawSmsBody: String,
        smsHash: String
    ): RecordSmsResult {
        if (transactionRepository.existsBySmsHash(smsHash)) {
            return RecordSmsResult.DuplicateSkipped
        }

        val bank = bankRepository.findBankBySender(senderNumber)
            ?: return RecordSmsResult.BankNotRecognized

        val defaultTitle = "${bank.persianName} ($accountIdentifier)"
        val account = accountRepository.getOrCreateAccount(bank.id, accountIdentifier, defaultTitle)

        val matchedCategory = categoryRepository.matchCategoryByKeywords(rawSmsBody, kind)

        val transactionId = transactionRepository.insertTransaction(
            accountId = account.id,
            categoryId = matchedCategory?.id,
            kind = kind,
            amount = amount,
            balanceAfter = balanceAfter,
            timestamp = timestamp,
            description = null,
            rawSmsBody = rawSmsBody,
            smsHash = smsHash
        )

        if (balanceAfter != null && transactionId > 0L) {
            accountRepository.updateBalanceIfNewer(account.id, balanceAfter, timestamp)
        }

        return RecordSmsResult.Inserted(transactionId)
    }
}
