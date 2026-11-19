package ir.khodhesab.ui.screens.reports

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.khodhesab.core.date.JalaliCalendarHelper
import ir.khodhesab.data.backup.DatabaseBackupManager
import ir.khodhesab.data.export.CsvReportExporter
import ir.khodhesab.data.preferences.AppPreferencesRepository
import ir.khodhesab.domain.model.ReportPeriod
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.domain.repository.AccountRepository
import ir.khodhesab.domain.usecase.GetFinancialReportUseCase
import ir.khodhesab.domain.usecase.GetTrendSeriesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel managing financial analytics, Jalali period navigation, CSV export, and database backup/restore.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val getFinancialReportUseCase: GetFinancialReportUseCase,
    private val getTrendSeriesUseCase: GetTrendSeriesUseCase,
    private val accountRepository: AccountRepository,
    private val appPreferencesRepository: AppPreferencesRepository,
    private val csvReportExporter: CsvReportExporter,
    private val databaseBackupManager: DatabaseBackupManager
) : ViewModel() {

    private val selectedPeriodState = MutableStateFlow(ReportPeriod.MONTHLY)
    private val referenceTimestampState = MutableStateFlow(System.currentTimeMillis())
    private val selectedAccountIdState = MutableStateFlow<Long?>(null)
    private val selectedBreakdownKindState = MutableStateFlow(TransactionKind.WITHDRAWAL)
    private val shareFileUriState = MutableStateFlow<Uri?>(null)
    private val statusMessageState = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ReportsUiState> = combine(
        selectedPeriodState,
        referenceTimestampState,
        selectedAccountIdState,
        selectedBreakdownKindState
    ) { period, refTime, accountId, breakdownKind ->
        Params4(period, refTime, accountId, breakdownKind)
    }.flatMapLatest { p ->
        val periodTitle = formatPeriodTitle(p.period, p.refTime)

        val reportFlow = getFinancialReportUseCase(p.period, p.refTime, p.accountId)
        val trendFlow = getTrendSeriesUseCase(p.period, p.refTime, p.accountId)

        combine(
            reportFlow,
            trendFlow,
            accountRepository.observeAllAccounts(),
            appPreferencesRepository.currencyUnit
        ) { report, trendBuckets, accounts, unit ->
            val breakdowns = if (p.breakdownKind == TransactionKind.WITHDRAWAL) {
                report.expenseBreakdown
            } else {
                report.incomeBreakdown
            }

            combine(shareFileUriState, statusMessageState) { shareUri, statusMsg ->
                ReportsUiState(
                    isLoading = false,
                    selectedPeriod = p.period,
                    referenceTimestamp = p.refTime,
                    periodTitle = periodTitle,
                    accounts = accounts,
                    selectedAccountId = p.accountId,
                    selectedBreakdownKind = p.breakdownKind,
                    summary = report.summary,
                    categoryBreakdowns = breakdowns,
                    trendBuckets = trendBuckets,
                    periodTransactions = report.transactions,
                    currencyUnit = unit,
                    shareFileUri = shareUri,
                    statusMessage = statusMsg
                )
            }
        }.flatMapLatest { it }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = ReportsUiState()
    )

    fun onSelectPeriod(period: ReportPeriod) {
        selectedPeriodState.value = period
        referenceTimestampState.value = System.currentTimeMillis()
    }

    fun onPreviousPeriod() {
        val currentRef = referenceTimestampState.value
        val period = selectedPeriodState.value
        referenceTimestampState.value = shiftPeriod(period, currentRef, backward = true)
    }

    fun onNextPeriod() {
        val currentRef = referenceTimestampState.value
        val period = selectedPeriodState.value
        referenceTimestampState.value = shiftPeriod(period, currentRef, backward = false)
    }

    fun onSelectAccount(accountId: Long?) {
        selectedAccountIdState.value = accountId
    }

    fun onSelectBreakdownKind(kind: TransactionKind) {
        selectedBreakdownKindState.value = kind
    }

    fun onExportCsv(context: Context) {
        viewModelScope.launch {
            val state = uiState.value
            if (state.periodTransactions.isEmpty()) {
                statusMessageState.value = "تراکنشی در این دوره برای خروجی اکسل یافت نشد."
                return@launch
            }
            val uri = csvReportExporter.exportToCacheFile(
                context = context,
                transactions = state.periodTransactions,
                currencyUnit = state.currencyUnit,
                fileName = "khodhesab_report.csv"
            )
            shareFileUriState.value = uri
            statusMessageState.value = "فایل اکسل (CSV) با موفقیت آماده شد."
        }
    }

    fun onCreateBackup(context: Context) {
        viewModelScope.launch {
            databaseBackupManager.exportDatabaseBackup(context).fold(
                onSuccess = { uri ->
                    shareFileUriState.value = uri
                    statusMessageState.value = "پشتیبان دیتابیس با موفقیت ایجاد شد."
                },
                onFailure = {
                    statusMessageState.value = "خطا در ایجاد نسخه پشتیبان."
                }
            )
        }
    }

    fun onRestoreBackup(context: Context, sourceUri: Uri) {
        viewModelScope.launch {
            databaseBackupManager.restoreDatabaseBackup(context, sourceUri).fold(
                onSuccess = {
                    statusMessageState.value = "دیتابیس با موفقیت بازیابی شد."
                },
                onFailure = { e ->
                    statusMessageState.value = e.message ?: "خطا در بازیابی دیتابیس."
                }
            )
        }
    }

    fun onShareHandled() {
        shareFileUriState.value = null
    }

    fun onDismissMessage() {
        statusMessageState.value = null
    }

    private fun formatPeriodTitle(period: ReportPeriod, refTime: Long): String {
        val jDate = JalaliCalendarHelper.fromEpochMillis(refTime)
        return when (period) {
            ReportPeriod.DAILY -> JalaliCalendarHelper.formatText(jDate)
            ReportPeriod.WEEKLY -> "هفته ${JalaliCalendarHelper.formatText(jDate)}"
            ReportPeriod.MONTHLY -> "${JalaliCalendarHelper.PERSIAN_MONTH_NAMES[jDate.month - 1]} ${jDate.year}"
        }
    }

    private fun shiftPeriod(period: ReportPeriod, refTime: Long, backward: Boolean): Long {
        val sign = if (backward) -1 else 1
        return when (period) {
            ReportPeriod.DAILY -> refTime + sign * 24 * 60 * 60 * 1000L
            ReportPeriod.WEEKLY -> refTime + sign * 7 * 24 * 60 * 60 * 1000L
            ReportPeriod.MONTHLY -> {
                val jDate = JalaliCalendarHelper.fromEpochMillis(refTime)
                var newMonth = jDate.month + sign
                var newYear = jDate.year
                if (newMonth < 1) {
                    newMonth = 12
                    newYear -= 1
                } else if (newMonth > 12) {
                    newMonth = 1
                    newYear += 1
                }
                JalaliCalendarHelper.toEpochMillis(ir.khodhesab.core.date.JalaliDate(newYear, newMonth, 15))
            }
        }
    }

    private data class Params4<T1, T2, T3, T4>(
        val period: T1,
        val refTime: T2,
        val accountId: T3,
        val breakdownKind: T4
    )
}
