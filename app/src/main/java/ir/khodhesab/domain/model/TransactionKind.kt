package ir.khodhesab.domain.model

/**
 * Pure domain representation of financial transaction kind.
 */
enum class TransactionKind {
    /**
     * Income / Deposit into account (واریز).
     */
    DEPOSIT,

    /**
     * Expense / Withdrawal from account (برداشت).
     */
    WITHDRAWAL,

    /**
     * Transfer between user's own accounts (انتقال).
     */
    TRANSFER
}
