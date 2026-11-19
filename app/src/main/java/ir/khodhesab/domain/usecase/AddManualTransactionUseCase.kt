package ir.khodhesab.domain.usecase

import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.domain.repository.AccountRepository
import ir.khodhesab.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * UseCase for adding manual non-SMS transactions.
 */
class AddManualTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(
        accountId: Long,
        categoryId: Long?,
        kind: TransactionKind,
        amount: Long,
        timestamp: Long,
        description: String?,
        updateAccountBalance: Boolean = false,
        newAccountBalance: Long? = null
    ): Result<Long> {
        if (amount <= 0L) {
            return Result.failure(IllegalArgumentException("مبلغ تراکنش باید بزرگتر از صفر باشد."))
        }

        val txId = transactionRepository.insertTransaction(
            accountId = accountId,
            categoryId = categoryId,
            kind = kind,
            amount = amount,
            balanceAfter = newAccountBalance,
            timestamp = timestamp,
            description = description,
            rawSmsBody = null,
            smsHash = null
        )

        if (txId > 0L && updateAccountBalance && newAccountBalance != null) {
            accountRepository.updateBalanceIfNewer(accountId, newAccountBalance, timestamp)
        }

        return Result.success(txId)
    }
}
