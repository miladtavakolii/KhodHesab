package ir.khodhesab.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ir.khodhesab.domain.model.Transaction
import ir.khodhesab.ui.screens.categories.CategoriesScreen
import ir.khodhesab.ui.screens.categories.CategoriesUiState
import ir.khodhesab.ui.screens.categories.CategoriesViewModel
import ir.khodhesab.ui.screens.dashboard.DashboardScreen
import ir.khodhesab.ui.screens.dashboard.DashboardUiState
import ir.khodhesab.ui.screens.dashboard.DashboardViewModel
import ir.khodhesab.ui.screens.transactions.TransactionsScreen
import ir.khodhesab.ui.screens.transactions.TransactionsUiState
import ir.khodhesab.ui.screens.transactions.TransactionsViewModel

/**
 * Navigation Host component for KhodHesab app.
 */
@Composable
fun KhodHesabNavHost(
    navController: NavHostController,
    dashboardState: DashboardUiState,
    dashboardViewModel: DashboardViewModel,
    transactionsState: TransactionsUiState,
    transactionsViewModel: TransactionsViewModel,
    categoriesState: CategoriesUiState,
    categoriesViewModel: CategoriesViewModel,
    onTransactionClick: (Transaction) -> Unit,
    onAddManualTransactionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = KhodHesabDestination.Dashboard.route,
        modifier = modifier
    ) {
        composable(KhodHesabDestination.Dashboard.route) {
            DashboardScreen(
                state = dashboardState,
                onSelectAccount = { dashboardViewModel.onSelectAccount(it) },
                onToggleCurrencyUnit = { dashboardViewModel.onToggleCurrencyUnit() },
                onStartHistoricalSmsSync = { dashboardViewModel.onStartHistoricalSmsSync() },
                onDismissSyncMessage = { dashboardViewModel.onDismissSyncMessage() },
                onRenameAccount = { id, title -> dashboardViewModel.onRenameAccount(id, title) },
                onToggleAccountActive = { id, active -> dashboardViewModel.onToggleAccountActive(id, active) },
                onTransactionClick = onTransactionClick,
                onNavigateToTransactions = {
                    navController.navigate(KhodHesabDestination.Transactions.route)
                },
                onAddManualTransactionClick = onAddManualTransactionClick
            )
        }

        composable(KhodHesabDestination.Transactions.route) {
            TransactionsScreen(
                state = transactionsState,
                onSearchQueryChange = { transactionsViewModel.onSearchQueryChange(it) },
                onAccountFilterChange = { transactionsViewModel.onAccountFilterChange(it) },
                onKindFilterChange = { transactionsViewModel.onKindFilterChange(it) },
                onToggleUncategorizedOnly = { transactionsViewModel.onToggleUncategorizedOnly() },
                onClearFilters = { transactionsViewModel.onClearFilters() },
                onTransactionClick = onTransactionClick
            )
        }

        composable(KhodHesabDestination.Reports.route) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "صفحه گزارش‌ها",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        composable(KhodHesabDestination.Categories.route) {
            CategoriesScreen(
                state = categoriesState,
                onSelectKind = { categoriesViewModel.onSelectKind(it) },
                onStartCreateCategory = { categoriesViewModel.onStartCreateCategory() },
                onStartEditCategory = { categoriesViewModel.onStartEditCategory(it) },
                onDismissEditor = { categoriesViewModel.onDismissEditor() },
                onSaveCategory = { name, icon, color, kws -> categoriesViewModel.onSaveCategory(name, icon, color, kws) },
                onDeleteCategory = { categoriesViewModel.onDeleteCategory(it) },
                onRunRetroactiveAutoCategorization = { categoriesViewModel.onRunRetroactiveAutoCategorization() },
                onConfirmInternalTransfer = { categoriesViewModel.onConfirmInternalTransfer(it) },
                onDismissFeedback = { categoriesViewModel.onDismissFeedback() }
            )
        }
    }
}
