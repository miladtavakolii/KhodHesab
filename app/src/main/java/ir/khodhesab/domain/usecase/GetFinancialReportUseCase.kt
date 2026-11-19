package ir.khodhesab.domain.usecase

import ir.khodhesab.core.date.JalaliCalendarHelper
import ir.khodhesab.domain.model.CategoryBreakdown
import ir.khodhesab.domain.model.FinancialSummary
import ir.khodhesab.domain.model.ReportPeriod
import ir.khodhesab.domain.model.Transaction
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Container data class for financial report state.
 */
data class FinancialReport(
    val period: ReportPeriod,
    val startTime: Long,
    val endTime: Long,
    val summary: FinancialSummary,
    val expenseBreakdown: List<CategoryBreakdown>,
    val incomeBreakdown: List<CategoryBreakdown>,
    val transactions: List<Transaction>
)

/**
 * UseCase for computing Jalali period-based financial reports.
 */
class GetFinancialReportUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(
        period: ReportPeriod,
        referenceTimestamp: Long,
        accountId: Long? = null
    ): Flow<FinancialReport> {
        val (start, end) = when (period) {
            ReportPeriod.DAILY -> JalaliCalendarHelper.getDayRange(referenceTimestamp)
            ReportPeriod.WEEKLY -> JalaliCalendarHelper.getWeekRange(referenceTimestamp)
            ReportPeriod.MONTHLY -> JalaliCalendarHelper.getMonthRange(referenceTimestamp)
        }

        val summaryFlow = transactionRepository.observePeriodSummary(start, end, accountId)
        val expenseBreakdownFlow = transactionRepository.observeCategoryBreakdown(TransactionKind.WITHDRAWAL, start, end, accountId)
        val incomeBreakdownFlow = transactionRepository.observeCategoryBreakdown(TransactionKind.DEPOSIT, start, end, accountId)
        val txFlow = transactionRepository.observeTransactions(accountId = accountId, startTime = start, endTime = end)

        return combine(summaryFlow, expenseBreakdownFlow, incomeBreakdownFlow, txFlow) { summary, expBreakdown, incBreakdown, txs ->
            FinancialReport(
                period = period,
                startTime = start,
                endTime = end,
                summary = summary,
                expenseBreakdown = expBreakdown,
                incomeBreakdown = incBreakdown,
                transactions = txs
            )
        }
    }
}
