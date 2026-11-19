package ir.khodhesab.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.khodhesab.data.preferences.AppPreferencesRepository
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.domain.repository.AccountRepository
import ir.khodhesab.domain.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel managing transactions list, multi-criteria filtering, and search queries.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val appPreferencesRepository: AppPreferencesRepository
) : ViewModel() {

    private val searchQueryState = MutableStateFlow("")
    private val selectedAccountIdState = MutableStateFlow<Long?>(null)
    private val selectedKindState = MutableStateFlow<TransactionKind?>(null)
    private val onlyUncategorizedState = MutableStateFlow(false)

    val uiState: StateFlow<TransactionsUiState> = combine(
        searchQueryState,
        selectedAccountIdState,
        selectedKindState,
        onlyUncategorizedState
    ) { query, accountId, kind, onlyUncat ->
        Tuple4(query, accountId, kind, onlyUncat)
    }.flatMapLatest { tuple ->
        combine(
            accountRepository.observeAllAccounts(),
            appPreferencesRepository.currencyUnit,
            transactionRepository.observeTransactions(
                accountId = tuple.accountId,
                kind = tuple.kind,
                searchQuery = tuple.query
            )
        ) { accounts, unit, txList ->
            val filtered = if (tuple.onlyUncat) {
                txList.filter { it.category == null }
            } else {
                txList
            }
            TransactionsUiState(
                isLoading = false,
                searchQuery = tuple.query,
                selectedAccountId = tuple.accountId,
                selectedKind = tuple.kind,
                onlyUncategorized = tuple.onlyUncat,
                accounts = accounts,
                transactions = filtered,
                currencyUnit = unit
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = TransactionsUiState()
    )

    fun onSearchQueryChange(newQuery: String) {
        searchQueryState.value = newQuery
    }

    fun onAccountFilterChange(accountId: Long?) {
        selectedAccountIdState.value = accountId
    }

    fun onKindFilterChange(kind: TransactionKind?) {
        selectedKindState.value = if (selectedKindState.value == kind) null else kind
    }

    fun onToggleUncategorizedOnly() {
        onlyUncategorizedState.value = !onlyUncategorizedState.value
    }

    fun onClearFilters() {
        searchQueryState.value = ""
        selectedAccountIdState.value = null
        selectedKindState.value = null
        onlyUncategorizedState.value = false
    }

    private data class Tuple4<T1, T2, T3, T4>(
        val query: T1,
        val accountId: T2,
        val kind: T3,
        val onlyUncat: T4
    )
}
