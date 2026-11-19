package ir.khodhesab.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ir.khodhesab.domain.model.Transaction
import ir.khodhesab.ui.screens.categories.CategoriesViewModel
import ir.khodhesab.ui.screens.dashboard.DashboardViewModel
import ir.khodhesab.ui.screens.transaction_detail.AddManualTransactionSheet
import ir.khodhesab.ui.screens.transaction_detail.TransactionDetailViewModel
import ir.khodhesab.ui.screens.transaction_detail.TransactionEditBottomSheet
import ir.khodhesab.ui.screens.transactions.TransactionsViewModel

/**
 * Root App Scaffold containing Material3 NavigationBar, NavHost, and modal sheets for transaction detail editing and manual entry.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    initialEditTransactionId: Long? = null,
    dashboardViewModel: DashboardViewModel = hiltViewModel(),
    transactionsViewModel: TransactionsViewModel = hiltViewModel(),
    categoriesViewModel: CategoriesViewModel = hiltViewModel(),
    transactionDetailViewModel: TransactionDetailViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val dashboardState by dashboardViewModel.uiState.collectAsState()
    val transactionsState by transactionsViewModel.uiState.collectAsState()
    val categoriesState by categoriesViewModel.uiState.collectAsState()
    val detailState by transactionDetailViewModel.uiState.collectAsState()

    var activeEditingTransaction by remember { mutableStateOf<Transaction?>(null) }
    var showAddManualSheet by remember { mutableStateOf(false) }

    LaunchedEffect(initialEditTransactionId) {
        if (initialEditTransactionId != null && initialEditTransactionId > 0L) {
            transactionDetailViewModel.loadTransaction(initialEditTransactionId)
            navController.navigate(KhodHesabDestination.Transactions.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(detailState.transaction) {
        if (detailState.transaction != null && !detailState.isDismissed) {
            activeEditingTransaction = detailState.transaction
        }
    }

    LaunchedEffect(detailState.isDismissed) {
        if (detailState.isDismissed) {
            activeEditingTransaction = null
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                KhodHesabDestination.items.forEach { destination ->
                    val selected = currentRoute == destination.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute != destination.route) {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.title
                            )
                        },
                        label = { Text(text = destination.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        KhodHesabNavHost(
            navController = navController,
            dashboardState = dashboardState,
            dashboardViewModel = dashboardViewModel,
            transactionsState = transactionsState,
            transactionsViewModel = transactionsViewModel,
            categoriesState = categoriesState,
            categoriesViewModel = categoriesViewModel,
            onTransactionClick = { tx ->
                transactionDetailViewModel.loadTransaction(tx.id)
                activeEditingTransaction = tx
            },
            onAddManualTransactionClick = { showAddManualSheet = true },
            modifier = Modifier.padding(innerPadding)
        )

        activeEditingTransaction?.let { tx ->
            TransactionEditBottomSheet(
                transaction = tx,
                categories = detailState.categoriesOfKind,
                selectedCategoryId = detailState.selectedCategoryId,
                selectedKind = detailState.selectedKind,
                description = detailState.description,
                currencyUnit = detailState.currencyUnit,
                onCategorySelect = { transactionDetailViewModel.onCategorySelect(it) },
                onKindSelect = { transactionDetailViewModel.onKindSelect(it) },
                onDescriptionChange = { transactionDetailViewModel.onDescriptionChange(it) },
                onSave = { transactionDetailViewModel.onSaveTransaction() },
                onDelete = { transactionDetailViewModel.onDeleteTransaction() },
                onDismiss = { activeEditingTransaction = null }
            )
        }

        if (showAddManualSheet) {
            AddManualTransactionSheet(
                accounts = dashboardState.accounts,
                currencyUnit = dashboardState.currencyUnit,
                onAdd = { accountId, kind, amount, desc ->
                    transactionDetailViewModel.onAddManualTransaction(
                        accountId = accountId,
                        categoryId = null,
                        kind = kind,
                        amountRials = amount,
                        description = desc,
                        updateAccountBalance = true,
                        newAccountBalanceRials = null
                    )
                },
                onDismiss = { showAddManualSheet = false }
            )
        }
    }
}
