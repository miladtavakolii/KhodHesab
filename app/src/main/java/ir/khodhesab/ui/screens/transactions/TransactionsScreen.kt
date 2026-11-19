package ir.khodhesab.ui.screens.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.khodhesab.domain.model.Transaction
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.ui.components.EmptyStateView
import ir.khodhesab.ui.components.TransactionListItem

/**
 * Screen displaying filtered list of transactions with search bar and quick filter chips.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    state: TransactionsUiState,
    onSearchQueryChange: (String) -> Unit,
    onAccountFilterChange: (Long?) -> Unit,
    onKindFilterChange: (TransactionKind?) -> Unit,
    onToggleUncategorizedOnly: () -> Unit,
    onClearFilters: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "تراکنش‌ها",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (state.searchQuery.isNotEmpty() || state.selectedAccountId != null || state.selectedKind != null || state.onlyUncategorized) {
                        IconButton(onClick = onClearFilters) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "پاکسازی فیلترها"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("جستجو در شرح یا متن پیامک...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "پاک کردن")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.onlyUncategorized,
                        onClick = onToggleUncategorizedOnly,
                        label = { Text("دسته‌بندی نشده") }
                    )
                }

                item {
                    FilterChip(
                        selected = state.selectedKind == TransactionKind.WITHDRAWAL,
                        onClick = { onKindFilterChange(TransactionKind.WITHDRAWAL) },
                        label = { Text("برداشت") }
                    )
                }

                item {
                    FilterChip(
                        selected = state.selectedKind == TransactionKind.DEPOSIT,
                        onClick = { onKindFilterChange(TransactionKind.DEPOSIT) },
                        label = { Text("واریز") }
                    )
                }

                item {
                    FilterChip(
                        selected = state.selectedKind == TransactionKind.TRANSFER,
                        onClick = { onKindFilterChange(TransactionKind.TRANSFER) },
                        label = { Text("انتقال بین حساب") }
                    )
                }

                items(state.accounts, key = { it.id }) { acc ->
                    FilterChip(
                        selected = state.selectedAccountId == acc.id,
                        onClick = { onAccountFilterChange(if (state.selectedAccountId == acc.id) null else acc.id) },
                        label = { Text(acc.title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (state.transactions.isEmpty()) {
                EmptyStateView(
                    title = "هیچ تراکنشی یافت نشد",
                    subtitle = "هیچ تراکنشی منطبق با فیلترها یا جستجوی شما پیدا نشد.",
                    icon = Icons.Default.FilterList,
                    actionText = "پاکسازی فیلترها",
                    onActionClick = onClearFilters,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(state.transactions, key = { it.id }) { tx ->
                        TransactionListItem(
                            transaction = tx,
                            currencyUnit = state.currencyUnit,
                            onClick = { onTransactionClick(tx) }
                        )
                    }
                }
            }
        }
    }
}
