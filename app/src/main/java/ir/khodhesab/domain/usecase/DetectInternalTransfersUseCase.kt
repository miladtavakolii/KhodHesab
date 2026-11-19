package ir.khodhesab.domain.usecase

import ir.khodhesab.domain.model.InternalTransferPair
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.math.abs

/**
 * UseCase for detecting and confirming internal account-to-account transfer pairs.
 */
class DetectInternalTransfersUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {

    /**
     * Scans active transactions to detect potential internal transfer pairs within [timeWindowMillis].
     *
     * @param timeWindowMillis Maximum allowed time gap between withdrawal and deposit (defaults to 15 minutes).
     * @return [Flow] of detected [InternalTransferPair] list.
     */
    fun detectTransferPairs(timeWindowMillis: Long = 15 * 60 * 1000L): Flow<List<InternalTransferPair>> {
        return transactionRepository.observeTransactions().map { allTxs ->
            val nonTransfers = allTxs.filter { it.kind != TransactionKind.TRANSFER }
            val withdrawals = nonTransfers.filter { it.kind == TransactionKind.WITHDRAWAL }
            val deposits = nonTransfers.filter { it.kind == TransactionKind.DEPOSIT }

            val pairs = mutableListOf<InternalTransferPair>()

            for (withdrawal in withdrawals) {
                val matchedDeposit = deposits.find { dep ->
                    dep.account.id != withdrawal.account.id &&
                            dep.amount == withdrawal.amount &&
                            abs(dep.timestamp - withdrawal.timestamp) <= timeWindowMillis
                }

                if (matchedDeposit != null) {
                    val timeDiff = abs(matchedDeposit.timestamp - withdrawal.timestamp)
                    pairs.add(
                        InternalTransferPair(
                            withdrawalTransaction = withdrawal,
                            depositTransaction = matchedDeposit,
                            amount = withdrawal.amount,
                            timeDifferenceMillis = timeDiff
                        )
                    )
                }
            }

            pairs.distinctBy { it.withdrawalTransaction.id to it.depositTransaction.id }
        }
    }

    /**
     * Marks both withdrawal and deposit transactions as [TransactionKind.TRANSFER] so they are excluded
     * from income and expense summaries.
     */
    suspend fun confirmTransferPair(pair: InternalTransferPair) {
        transactionRepository.updateTransactionKind(pair.withdrawalTransaction.id, TransactionKind.TRANSFER)
        transactionRepository.updateTransactionKind(pair.depositTransaction.id, TransactionKind.TRANSFER)
    }
}
