package ir.khodhesab.ui.screens.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.khodhesab.domain.model.Category
import ir.khodhesab.domain.model.InternalTransferPair
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.ui.util.CategoryIconMapper
import ir.khodhesab.ui.util.PersianCurrencyFormatter

/**
 * Screen for managing transaction categories, keyword auto-categorization rules, and internal transfer detection.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CategoriesScreen(
    state: CategoriesUiState,
    onSelectKind: (TransactionKind) -> Unit,
    onStartCreateCategory: () -> Unit,
    onStartEditCategory: (Category) -> Unit,
    onDismissEditor: () -> Unit,
    onSaveCategory: (name: String, iconKey: String, colorHex: String, keywords: List<String>) -> Unit,
    onDeleteCategory: (Long) -> Unit,
    onRunRetroactiveAutoCategorization: () -> Unit,
    onConfirmInternalTransfer: (InternalTransferPair) -> Unit,
    onDismissFeedback: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.feedbackMessage) {
        state.feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            onDismissFeedback()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "مدیریت دسته‌بندی‌ها",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onRunRetroactiveAutoCategorization) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = "دسته‌بندی هوشمند"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onStartCreateCategory,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "دسته‌بندی جدید")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.detectedTransferPairs.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "شناسایی انتقال بین حساب‌ها (${state.detectedTransferPairs.size} مورد)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        state.detectedTransferPairs.forEach { pair ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "انتقال ${PersianCurrencyFormatter.formatAmount(pair.amount)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Button(onClick = { onConfirmInternalTransfer(pair) }) {
                                    Text("تایید انتقال")
                                }
                            }
                        }
                    }
                }
            }

            TabRow(
                selectedTabIndex = if (state.selectedKind == TransactionKind.WITHDRAWAL) 0 else 1,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = state.selectedKind == TransactionKind.WITHDRAWAL,
                    onClick = { onSelectKind(TransactionKind.WITHDRAWAL) },
                    text = { Text("برداشت (هزینه)") }
                )
                Tab(
                    selected = state.selectedKind == TransactionKind.DEPOSIT,
                    onClick = { onSelectKind(TransactionKind.DEPOSIT) },
                    text = { Text("واریز (درآمد)") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(state.categories, key = { it.id }) { cat ->
                    val color = CategoryIconMapper.parseHexColor(cat.colorHex)
                    val icon = CategoryIconMapper.getIcon(cat.iconKey)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { onStartEditCategory(cat) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(color.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = color,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = cat.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (cat.isDefault) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "(پیش‌فرض)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (cat.keywords.isNotEmpty()) {
                                        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            cat.keywords.take(3).forEach { kw ->
                                                AssistChip(
                                                    onClick = {},
                                                    label = { Text(kw, style = MaterialTheme.typography.labelSmall) }
                                                )
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = "بدون کلیدواژه خودکار",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            IconButton(onClick = { onStartEditCategory(cat) }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "ویرایش"
                                )
                            }
                        }
                    }
                }
            }
        }

        if (state.isCreatingCategory || state.editingCategory != null) {
            CategoryEditorBottomSheet(
                editingCategory = state.editingCategory,
                isCreating = state.isCreatingCategory,
                onSave = onSaveCategory,
                onDelete = onDeleteCategory,
                onDismiss = onDismissEditor
            )
        }
    }
}
