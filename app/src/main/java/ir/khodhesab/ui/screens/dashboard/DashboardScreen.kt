package ir.khodhesab.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.khodhesab.domain.model.Account
import ir.khodhesab.domain.model.Transaction
import ir.khodhesab.ui.components.BankAccountCard
import ir.khodhesab.ui.components.CurrencyToggleChip
import ir.khodhesab.ui.components.EmptyStateView
import ir.khodhesab.ui.components.FinancialSummaryRow
import ir.khodhesab.ui.components.TransactionListItem

/**
 * Main Dashboard Screen displaying accounts carousel, monthly cash flow summary, recent transactions,
 * and historical SMS sync controls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onSelectAccount: (Long?) -> Unit,
    onToggleCurrencyUnit: () -> Unit,
    onStartHistoricalSmsSync: () -> Unit,
    onDismissSyncMessage: () -> Unit,
    onRenameAccount: (Long, String) -> Unit,
    onToggleAccountActive: (Long, Boolean) -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onNavigateToTransactions: () -> Unit,
    onAddManualTransactionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editingAccount by remember { mutableStateOf<Account?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.syncStatusMessage) {
        state.syncStatusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            onDismissSyncMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "خودحساب",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    CurrencyToggleChip(
                        selectedUnit = state.currencyUnit,
                        onUnitSelected = { onToggleCurrencyUnit() }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onStartHistoricalSmsSync) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "همگام‌سازی پیامک‌ها"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddManualTransactionClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "تراکنش دستی"
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            if (state.smsSyncProgress != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "در حال بررسی و استخراج پیامک‌های بانکی...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { state.smsSyncProgress.progressFraction },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else if (!state.isInitialSmsSyncDone) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "استخراج خودکار تراکنش‌ها",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "برای استخراج تراکنش‌های قبلی از پیامک‌های بانکی، روی دکمه شروع کلیک کنید.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onStartHistoricalSmsSync) {
                            Text("شروع همگام‌سازی پیامک‌ها")
                        }
                    }
                }
            }

            Text(
                text = "حساب‌های من",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    BankAccountCard(
                        account = null,
                        isSelected = state.selectedAccountId == null,
                        currencyUnit = state.currencyUnit,
                        onClick = { onSelectAccount(null) },
                        modifier = Modifier.width(260.dp)
                    )
                }

                items(state.accounts, key = { it.id }) { acc ->
                    BankAccountCard(
                        account = acc,
                        isSelected = state.selectedAccountId == acc.id,
                        currencyUnit = state.currencyUnit,
                        onClick = { onSelectAccount(acc.id) },
                        modifier = Modifier.width(260.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "خلاصه مالی ماه جاری",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                FinancialSummaryRow(
                    summary = state.periodSummary,
                    currencyUnit = state.currencyUnit
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "آخرین تراکنش‌ها",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateToTransactions) {
                    Text("مشاهده همه")
                }
            }

            if (state.recentTransactions.isEmpty()) {
                EmptyStateView(
                    title = "تراکنشی ثبت نشده است",
                    subtitle = "با ورود پیامک‌های جدید یا ثبت تراکنش دستی، لیست تراکنش‌ها اینجا نمایش داده می‌شود.",
                    actionText = "همگام‌سازی پیامک‌ها",
                    onActionClick = onStartHistoricalSmsSync
                )
            } else {
                Column {
                    state.recentTransactions.forEach { tx ->
                        TransactionListItem(
                            transaction = tx,
                            currencyUnit = state.currencyUnit,
                            onClick = { onTransactionClick(tx) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    editingAccount?.let { acc ->
        RenameAccountDialog(
            account = acc,
            onDismiss = { editingAccount = null },
            onSave = { newTitle, isActive ->
                onRenameAccount(acc.id, newTitle)
                onToggleAccountActive(acc.id, isActive)
            }
        )
    }
}
