package ir.khodhesab.ui.screens.transaction_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.khodhesab.data.preferences.AppPreferencesRepository
import ir.khodhesab.domain.model.CurrencyUnit
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.domain.repository.AccountRepository
import ir.khodhesab.domain.repository.CategoryRepository
import ir.khodhesab.domain.repository.TransactionRepository
import ir.khodhesab.domain.usecase.AddManualTransactionUseCase
import ir.khodhesab.domain.usecase.UpdateTransactionUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel managing transaction editing, categorization, note updates, and manual transaction creation.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val addManualTransactionUseCase: AddManualTransactionUseCase,
    private val appPreferencesRepository: AppPreferencesRepository
) : ViewModel() {

    private val transactionIdState = MutableStateFlow<Long?>(null)
    private val selectedCategoryIdState = MutableStateFlow<Long?>(null)
    private val selectedKindState = MutableStateFlow<TransactionKind>(TransactionKind.WITHDRAWAL)
    private val descriptionState = MutableStateFlow("")
    private val isDismissedState = MutableStateFlow(false)

    val uiState: StateFlow<TransactionDetailUiState> = combine(
        transactionIdState,
        selectedCategoryIdState,
        selectedKindState,
        descriptionState,
        isDismissedState
    ) { txId, catId, kind, desc, dismissed ->
        Params(txId, catId, kind, desc, dismissed)
    }.flatMapLatest { p ->
        if (p.txId == null) {
            flowOf(
                TransactionDetailUiState(
                    isLoading = false,
                    selectedCategoryId = p.catId,
                    selectedKind = p.kind,
                    description = p.desc,
                    isDismissed = p.dismissed
                )
            )
        } else {
            combine(
                transactionRepository.observeTransactions(),
                categoryRepository.observeCategoriesByKind(p.kind),
                accountRepository.observeAllAccounts(),
                appPreferencesRepository.currencyUnit
            ) { allTxs, categories, accounts, unit ->
                val tx = allTxs.find { it.id == p.txId }
                TransactionDetailUiState(
                    isLoading = false,
                    transaction = tx,
                    categoriesOfKind = categories,
                    accounts = accounts,
                    selectedCategoryId = p.catId ?: tx?.category?.id,
                    selectedKind = p.kind ?: tx?.kind ?: TransactionKind.WITHDRAWAL,
                    description = p.desc.ifEmpty { tx?.description ?: "" },
                    currencyUnit = unit,
                    isDismissed = p.dismissed
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = TransactionDetailUiState()
    )

    fun loadTransaction(transactionId: Long) {
        transactionIdState.value = transactionId
        isDismissedState.value = false
    }

    fun onCategorySelect(categoryId: Long?) {
        selectedCategoryIdState.value = categoryId
    }

    fun onKindSelect(kind: TransactionKind) {
        selectedKindState.value = kind
        selectedCategoryIdState.value = null
    }

    fun onDescriptionChange(newDesc: String) {
        descriptionState.value = newDesc
    }

    fun onSaveTransaction() {
        val txId = transactionIdState.value ?: return
        viewModelScope.launch {
            val catId = selectedCategoryIdState.value ?: uiState.value.selectedCategoryId
            val desc = descriptionState.value.ifEmpty { uiState.value.description }
            val kind = selectedKindState.value

            updateTransactionUseCase.updateCategoryAndNote(txId, catId, desc)
            if (kind == TransactionKind.TRANSFER) {
                updateTransactionUseCase.markAsTransfer(txId)
            }
            isDismissedState.value = true
        }
    }

    fun onDeleteTransaction() {
        val txId = transactionIdState.value ?: return
        viewModelScope.launch {
            updateTransactionUseCase.deleteTransaction(txId)
            isDismissedState.value = true
        }
    }

    fun onAddManualTransaction(
        accountId: Long,
        categoryId: Long?,
        kind: TransactionKind,
        amountRials: Long,
        description: String?,
        updateAccountBalance: Boolean,
        newAccountBalanceRials: Long?
    ) {
        viewModelScope.launch {
            addManualTransactionUseCase(
                accountId = accountId,
                categoryId = categoryId,
                kind = kind,
                amount = amountRials,
                timestamp = System.currentTimeMillis(),
                description = description,
                updateAccountBalance = updateAccountBalance,
                newAccountBalance = newAccountBalanceRials
            )
            isDismissedState.value = true
        }
    }

    private data class Params(
        val txId: Long?,
        val catId: Long?,
        val kind: TransactionKind,
        val desc: String,
        val dismissed: Boolean
    )
}
