package ir.khodhesab.domain.usecase

import ir.khodhesab.core.date.JalaliCalendarHelper
import ir.khodhesab.domain.model.ReportPeriod
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.domain.model.TrendBucket
import ir.khodhesab.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * UseCase that calculates time-series financial trend buckets for daily, weekly, and monthly Jalali periods.
 */
class GetTrendSeriesUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {

    /**
     * Observes transactions and aggregates them into chronological [TrendBucket] list.
     *
     * @param period Target [ReportPeriod] (`DAILY`, `WEEKLY`, or `MONTHLY`).
     * @param referenceTimestamp Epoch millis within the target period.
     * @param accountId Optional account ID filter (`null` for all accounts).
     * @return [Flow] emitting list of [TrendBucket] items.
     */
    operator fun invoke(
        period: ReportPeriod,
        referenceTimestamp: Long = System.currentTimeMillis(),
        accountId: Long? = null
    ): Flow<List<TrendBucket>> {
        val bucketsInfo = buildBucketRanges(period, referenceTimestamp)

        val overallStart = bucketsInfo.first().second
        val overallEnd = bucketsInfo.last().third

        return transactionRepository.observeTransactions(
            startTime = overallStart,
            endTime = overallEnd,
            accountId = accountId
        ).map { transactions ->
            val nonTransfers = transactions.filter { it.kind != TransactionKind.TRANSFER }

            bucketsInfo.map { (label, start, end) ->
                val bucketTxs = nonTransfers.filter { it.timestamp in start..end }
                val income = bucketTxs.filter { it.kind == TransactionKind.DEPOSIT }.sumOf { it.amount }
                val expense = bucketTxs.filter { it.kind == TransactionKind.WITHDRAWAL }.sumOf { it.amount }

                TrendBucket(
                    label = label,
                    startMillis = start,
                    endMillis = end,
                    totalIncome = income,
                    totalExpense = expense
                )
            }
        }
    }

    private fun buildBucketRanges(
        period: ReportPeriod,
        refTime: Long
    ): List<Triple<String, Long, Long>> {
        val list = mutableListOf<Triple<String, Long, Long>>()

        when (period) {
            ReportPeriod.DAILY -> {
                val (weekStart, _) = JalaliCalendarHelper.getWeekRange(refTime)
                val dayNames = listOf("شنبه", "۱شنبه", "۲شنبه", "۳شنبه", "۴شنبه", "۵شنبه", "جمعه")
                val dayMillis = 24 * 60 * 60 * 1000L
                for (i in 0..6) {
                    val start = weekStart + i * dayMillis
                    val end = start + dayMillis - 1L
                    list.add(Triple(dayNames[i], start, end))
                }
            }
            ReportPeriod.WEEKLY -> {
                val (currentWeekStart, _) = JalaliCalendarHelper.getWeekRange(refTime)
                val weekMillis = 7 * 24 * 60 * 60 * 1000L
                for (i in 3 downTo 0) {
                    val start = currentWeekStart - i * weekMillis
                    val end = start + weekMillis - 1L
                    val jDate = JalaliCalendarHelper.fromEpochMillis(start)
                    val label = "${jDate.day} ${JalaliCalendarHelper.PERSIAN_MONTH_NAMES.getOrElse(jDate.month - 1) { "" }}"
                    list.add(Triple(label, start, end))
                }
            }
            ReportPeriod.MONTHLY -> {
                val currentJalali = JalaliCalendarHelper.fromEpochMillis(refTime)
                for (i in 5 downTo 0) {
                    var targetMonth = currentJalali.month - i
                    var targetYear = currentJalali.year
                    while (targetMonth < 1) {
                        targetMonth += 12
                        targetYear -= 1
                    }
                    val sampleMillis = JalaliCalendarHelper.toEpochMillis(
                        ir.khodhesab.core.date.JalaliDate(targetYear, targetMonth, 15)
                    )
                    val (start, end) = JalaliCalendarHelper.getMonthRange(sampleMillis)
                    val monthName = JalaliCalendarHelper.PERSIAN_MONTH_NAMES.getOrElse(targetMonth - 1) { "" }
                    list.add(Triple(monthName, start, end))
                }
            }
        }

        return list
    }
}
