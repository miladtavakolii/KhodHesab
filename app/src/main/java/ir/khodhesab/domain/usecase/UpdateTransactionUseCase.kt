package ir.khodhesab.domain.usecase

import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * UseCase for updating transaction category, notes, or marking as internal transfer.
 */
class UpdateTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend fun updateCategoryAndNote(transactionId: Long, categoryId: Long?, description: String?) {
        transactionRepository.updateTransactionDetails(transactionId, categoryId, description?.trim())
    }

    suspend fun markAsTransfer(transactionId: Long) {
        transactionRepository.updateTransactionKind(transactionId, TransactionKind.TRANSFER)
    }

    suspend fun deleteTransaction(transactionId: Long) {
        transactionRepository.deleteTransaction(transactionId)
    }
}
