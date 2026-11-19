package ir.khodhesab.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.khodhesab.core.date.JalaliCalendarHelper
import ir.khodhesab.ui.util.PersianCurrencyFormatter
import ir.khodhesab.data.preferences.AppPreferencesRepository
import ir.khodhesab.domain.model.CurrencyUnit
import ir.khodhesab.domain.repository.AccountRepository
import ir.khodhesab.domain.repository.TransactionRepository
import ir.khodhesab.domain.usecase.GetFinancialReportUseCase
import ir.khodhesab.domain.usecase.ManageAccountUseCase
import ir.khodhesab.sms_engine.importer.OldSmsImporter
import ir.khodhesab.sms_engine.importer.SmsImportProgress
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
 * Stateful ViewModel managing dashboard UI state, account filtering, SMS historical import, and account actions.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val getFinancialReportUseCase: GetFinancialReportUseCase,
    private val manageAccountUseCase: ManageAccountUseCase,
    private val appPreferencesRepository: AppPreferencesRepository,
    private val oldSmsImporter: OldSmsImporter
) : ViewModel() {

    private val selectedAccountIdState = MutableStateFlow<Long?>(null)
    private val smsSyncProgressState = MutableStateFlow<SmsImportProgress?>(null)
    private val syncStatusMessageState = MutableStateFlow<String?>(null)

    private val nowMillis = System.currentTimeMillis()

    val uiState: StateFlow<DashboardUiState> = combine(
        selectedAccountIdState,
        accountRepository.observeAllAccounts(),
        accountRepository.observeTotalActiveBalance(),
        appPreferencesRepository.currencyUnit,
        appPreferencesRepository.isInitialSmsSyncDone
    ) { selectedId, accounts, totalActiveBal, unit, syncDone ->
        val selectedAccount = accounts.find { it.id == selectedId }
        val displayBalance = selectedAccount?.currentBalance ?: totalActiveBal

        val (start, end) = JalaliCalendarHelper.getMonthRange(nowMillis)
        val periodSummaryFlow = transactionRepository.observePeriodSummary(start, end, selectedId)
        val txFlow = transactionRepository.observeTransactions(accountId = selectedId)

        combine(periodSummaryFlow, txFlow, smsSyncProgressState, syncStatusMessageState) { summary, txList, syncProgress, statusMsg ->
            DashboardUiState(
                isLoading = false,
                totalBalance = displayBalance,
                accounts = accounts,
                selectedAccountId = selectedId,
                periodSummary = summary,
                recentTransactions = txList.take(15),
                currencyUnit = unit,
                isInitialSmsSyncDone = syncDone,
                smsSyncProgress = syncProgress,
                syncStatusMessage = statusMsg
            )
        }
    }.flatMapLatest { it }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = DashboardUiState()
    )

    fun onSelectAccount(accountId: Long?) {
        selectedAccountIdState.value = accountId
    }

    fun onToggleCurrencyUnit() {
        val currentUnit = uiState.value.currencyUnit
        val newUnit = if (currentUnit == CurrencyUnit.TOMAN) CurrencyUnit.RIAL else CurrencyUnit.TOMAN
        viewModelScope.launch {
            appPreferencesRepository.setCurrencyUnit(newUnit)
        }
    }

    fun onStartHistoricalSmsSync() {
        if (smsSyncProgressState.value != null) return

        viewModelScope.launch {
            oldSmsImporter.importHistoricalSms().collect { progress ->
                smsSyncProgressState.value = progress
                if (progress.isCompleted) {
                    appPreferencesRepository.setInitialSmsSyncDone(true)
                    smsSyncProgressState.value = null
                    val recordedCountPersian = PersianCurrencyFormatter.toPersianDigits(progress.recordedCount.toString())
                    syncStatusMessageState.value = "$recordedCountPersian تراکنش جدید با موفقیت استخراج و ثبت شد."
                }
            }
        }
    }

    fun onDismissSyncMessage() {
        syncStatusMessageState.value = null
    }

    fun onRenameAccount(accountId: Long, newTitle: String) {
        viewModelScope.launch {
            manageAccountUseCase.renameAccount(accountId, newTitle)
        }
    }

    fun onToggleAccountActive(accountId: Long, isActive: Boolean) {
        viewModelScope.launch {
            manageAccountUseCase.toggleAccountActive(accountId, isActive)
        }
    }
}
