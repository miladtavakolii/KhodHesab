package ir.khodhesab.ui.screens.reports

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.khodhesab.domain.model.ReportPeriod
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.ui.components.EmptyStateView
import ir.khodhesab.ui.components.FinancialSummaryRow
import ir.khodhesab.ui.screens.reports.components.CategoryDonutChart
import ir.khodhesab.ui.screens.reports.components.IncomeExpenseBarChart

/**
 * Analytics and Financial Reports screen displaying Jalali period charts, Donut breakdown, CSV export, and database backup.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    state: ReportsUiState,
    onSelectPeriod: (ReportPeriod) -> Unit,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    onSelectAccount: (Long?) -> Unit,
    onSelectBreakdownKind: (TransactionKind) -> Unit,
    onExportCsv: () -> Unit,
    onCreateBackup: () -> Unit,
    onRestoreBackup: (Uri) -> Unit,
    onShareHandled: () -> Unit,
    onDismissMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showMenu by remember { mutableStateOf(false) }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { onRestoreBackup(it) }
    }

    LaunchedEffect(state.shareFileUri) {
        state.shareFileUri?.let { uri ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری فایل"))
            onShareHandled()
        }
    }

    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            onDismissMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "گزارش‌های تحلیلی",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "منو")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("خروجی اکسل (CSV)") },
                            onClick = {
                                showMenu = false
                                onExportCsv()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("پشتیبان‌گیری دیتابیس") },
                            onClick = {
                                showMenu = false
                                onCreateBackup()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("بازیابی بکاپ") },
                            onClick = {
                                showMenu = false
                                restoreLauncher.launch(arrayOf("*/*"))
                            }
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            TabRow(
                selectedTabIndex = when (state.selectedPeriod) {
                    ReportPeriod.DAILY -> 0
                    ReportPeriod.WEEKLY -> 1
                    ReportPeriod.MONTHLY -> 2
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = state.selectedPeriod == ReportPeriod.DAILY,
                    onClick = { onSelectPeriod(ReportPeriod.DAILY) },
                    text = { Text("روزانه") }
                )
                Tab(
                    selected = state.selectedPeriod == ReportPeriod.WEEKLY,
                    onClick = { onSelectPeriod(ReportPeriod.WEEKLY) },
                    text = { Text("هفتگی") }
                )
                Tab(
                    selected = state.selectedPeriod == ReportPeriod.MONTHLY,
                    onClick = { onSelectPeriod(ReportPeriod.MONTHLY) },
                    text = { Text("ماهانه") }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousPeriod) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "قبلی")
                }
                Text(
                    text = state.periodTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onNextPeriod) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "بعدی")
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.selectedAccountId == null,
                        onClick = { onSelectAccount(null) },
                        label = { Text("همه حساب‌ها") }
                    )
                }
                items(state.accounts, key = { it.id }) { acc ->
                    FilterChip(
                        selected = state.selectedAccountId == acc.id,
                        onClick = { onSelectAccount(acc.id) },
                        label = { Text(acc.title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                FinancialSummaryRow(
                    summary = state.summary,
                    currencyUnit = state.currencyUnit
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "روند واریز و برداشت",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    IncomeExpenseBarChart(buckets = state.trendBuckets)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تفکیک دسته‌بندی‌ها",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = state.selectedBreakdownKind == TransactionKind.WITHDRAWAL,
                                onClick = { onSelectBreakdownKind(TransactionKind.WITHDRAWAL) },
                                label = { Text("برداشت") }
                            )
                            FilterChip(
                                selected = state.selectedBreakdownKind == TransactionKind.DEPOSIT,
                                onClick = { onSelectBreakdownKind(TransactionKind.DEPOSIT) },
                                label = { Text("واریز") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (state.categoryBreakdowns.isEmpty()) {
                        EmptyStateView(
                            title = "تراکنشی در این دوره ثبت نشده",
                            subtitle = "هیچ تراکنشی منطبق با فیلترها و دوره انتخابی پیدا نشد.",
                            icon = Icons.Default.PieChart
                        )
                    } else {
                        CategoryDonutChart(
                            breakdowns = state.categoryBreakdowns,
                            currencyUnit = state.currencyUnit
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun ReportsScreenPreview() {
    ir.khodhesab.ui.theme.KhodHesabTheme {
        val cat1 = ir.khodhesab.domain.model.Category(id = 1, name = "خوراکی", iconKey = "ic_cat_food", colorHex = "#FF5722", kind = TransactionKind.WITHDRAWAL)
        val cat2 = ir.khodhesab.domain.model.Category(id = 2, name = "مسکن", iconKey = "ic_cat_home", colorHex = "#9C27B0", kind = TransactionKind.WITHDRAWAL)
        val breakdowns = listOf(
            ir.khodhesab.domain.model.CategoryBreakdown(category = cat1, totalAmount = 6_000_000L, transactionCount = 4, percentage = 60f),
            ir.khodhesab.domain.model.CategoryBreakdown(category = cat2, totalAmount = 4_000_000L, transactionCount = 2, percentage = 40f)
        )
        val buckets = listOf(
            ir.khodhesab.domain.model.TrendBucket(label = "فروردین", startMillis = 0L, endMillis = 1L, totalIncome = 15_000_000L, totalExpense = 10_000_000L),
            ir.khodhesab.domain.model.TrendBucket(label = "اردیبهشت", startMillis = 0L, endMillis = 1L, totalIncome = 18_000_000L, totalExpense = 12_000_000L)
        )
        val state = ReportsUiState(
            selectedPeriod = ReportPeriod.MONTHLY,
            periodTitle = "فروردین ۱۴۰۳",
            summary = ir.khodhesab.domain.model.FinancialSummary(totalIncome = 33_000_000L, totalExpense = 22_000_000L, transactionCount = 6),
            trendBuckets = buckets,
            categoryBreakdowns = breakdowns,
            currencyUnit = ir.khodhesab.domain.model.CurrencyUnit.TOMAN
        )
        ReportsScreen(
            state = state,
            onSelectPeriod = {},
            onPreviousPeriod = {},
            onNextPeriod = {},
            onSelectAccount = {},
            onSelectBreakdownKind = {},
            onExportCsv = {},
            onCreateBackup = {},
            onRestoreBackup = {},
            onShareHandled = {},
            onDismissMessage = {}
        )
    }
}


