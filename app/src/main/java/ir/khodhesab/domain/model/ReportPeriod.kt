package ir.khodhesab.domain.model

/**
 * Time period options for financial reports and analytics.
 */
enum class ReportPeriod {
    /**
     * Daily financial report.
     */
    DAILY,

    /**
     * Iranian weekly financial report (Saturday to Friday).
     */
    WEEKLY,

    /**
     * Jalali monthly financial report.
     */
    MONTHLY
}
