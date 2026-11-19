package ir.khodhesab.domain.usecase

import ir.khodhesab.core.date.JalaliCalendarHelper
import ir.khodhesab.domain.model.DashboardOverview
import ir.khodhesab.domain.repository.AccountRepository
import ir.khodhesab.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * UseCase providing a reactive stream of [DashboardOverview] data for the main UI.
 */
class GetDashboardOverviewUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(currentTimeMillis: Long = System.currentTimeMillis()): Flow<DashboardOverview> {
        val totalBalanceFlow = accountRepository.observeTotalActiveBalance()
        val accountsFlow = accountRepository.observeAllAccounts()

        val (monthStart, monthEnd) = JalaliCalendarHelper.getMonthRange(currentTimeMillis)
        val periodSummaryFlow = transactionRepository.observePeriodSummary(monthStart, monthEnd)
        val recentTxFlow = transactionRepository.observeTransactions()

        return combine(totalBalanceFlow, accountsFlow, periodSummaryFlow, recentTxFlow) { totalBal, accounts, summary, recentTxs ->
            DashboardOverview(
                totalActiveBalance = totalBal,
                accounts = accounts,
                periodSummary = summary,
                recentTransactions = recentTxs.take(10)
            )
        }
    }
}
