package ir.khodhesab.domain.model

/**
 * Pure domain model representing a candidate pair of withdrawal and deposit transactions
 * identified as an internal account-to-account transfer.
 *
 * @property withdrawalTransaction Transaction representing money leaving Account A.
 * @property depositTransaction Transaction representing money entering Account B.
 * @property amount Transaction amount in Rials.
 * @property timeDifferenceMillis Milliseconds difference between the two transactions.
 */
data class InternalTransferPair(
    val withdrawalTransaction: Transaction,
    val depositTransaction: Transaction,
    val amount: Long,
    val timeDifferenceMillis: Long
)
