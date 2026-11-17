package ir.khodhesab.data.local.model

/**
 * Represents the type of financial transaction.
 */
enum class TransactionType {
    /**
     * Income or deposit into an account (واریز).
     */
    DEPOSIT,

    /**
     * Expense or withdrawal from an account (برداشت).
     */
    WITHDRAWAL,

    /**
     * Transfer between user's own accounts (انتقال).
     */
    TRANSFER
}
