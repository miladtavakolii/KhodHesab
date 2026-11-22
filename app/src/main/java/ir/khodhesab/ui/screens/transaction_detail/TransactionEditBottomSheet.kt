package ir.khodhesab.ui.screens.transaction_detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.khodhesab.core.date.JalaliCalendarHelper
import ir.khodhesab.domain.model.Category
import ir.khodhesab.domain.model.CurrencyUnit
import ir.khodhesab.domain.model.Transaction
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.ui.util.CategoryIconMapper
import ir.khodhesab.ui.util.PersianCurrencyFormatter

/**
 * Modal BottomSheet for viewing transaction details, categorizing, editing notes, and deleting.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionEditBottomSheet(
    transaction: Transaction,
    categories: List<Category>,
    selectedCategoryId: Long?,
    selectedKind: TransactionKind,
    description: String,
    currencyUnit: CurrencyUnit,
    onCategorySelect: (Long?) -> Unit,
    onKindSelect: (TransactionKind) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = transaction.account.bank.persianName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = JalaliCalendarHelper.formatText(JalaliCalendarHelper.fromEpochMillis(transaction.timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف تراکنش",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = PersianCurrencyFormatter.formatAmount(transaction.amount, currencyUnit),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "نوع تراکنش",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedKind == TransactionKind.WITHDRAWAL,
                    onClick = { onKindSelect(TransactionKind.WITHDRAWAL) },
                    label = { Text("برداشت") }
                )
                FilterChip(
                    selected = selectedKind == TransactionKind.DEPOSIT,
                    onClick = { onKindSelect(TransactionKind.DEPOSIT) },
                    label = { Text("واریز") }
                )
                FilterChip(
                    selected = selectedKind == TransactionKind.TRANSFER,
                    onClick = { onKindSelect(TransactionKind.TRANSFER) },
                    label = { Text("انتقال بین حساب") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "دسته‌بندی",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategoryId == cat.id
                    val color = CategoryIconMapper.parseHexColor(cat.colorHex)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelect(if (isSelected) null else cat.id) },
                        leadingIcon = {
                            Icon(
                                imageVector = CategoryIconMapper.getIcon(cat.iconKey),
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else color,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text(cat.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("توضیحات / یادداشت") },
                modifier = Modifier.fillMaxWidth()
            )

            if (transaction.rawSmsBody != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "متن پیامک بانکی:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = transaction.rawSmsBody,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ذخیره تغییرات")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun TransactionEditBottomSheetPreview() {
    ir.khodhesab.ui.theme.KhodHesabTheme {
        val bank = ir.khodhesab.domain.model.Bank(code = "MELLAT", persianName = "بانک ملت", colorHex = "#E53935", iconKey = "ic")
        val account = ir.khodhesab.domain.model.Account(id = 1, bank = bank, accountIdentifier = "1234", title = "حساب ملت", currentBalance = 10_000_000L)
        val category = Category(id = 1, name = "خوراکی", iconKey = "ic_cat_food", colorHex = "#FF5722", kind = TransactionKind.WITHDRAWAL)
        val transaction = Transaction(
            id = 1,
            account = account,
            category = category,
            kind = TransactionKind.WITHDRAWAL,
            amount = 500_000L,
            timestamp = System.currentTimeMillis(),
            rawSmsBody = "برداشت 500,000 ریال از حساب 1234",
            description = "خرید فروشگاه"
        )
        TransactionEditBottomSheet(
            transaction = transaction,
            categories = listOf(category),
            selectedCategoryId = 1L,
            selectedKind = TransactionKind.WITHDRAWAL,
            description = "خرید فروشگاه",
            currencyUnit = CurrencyUnit.TOMAN,
            onCategorySelect = {},
            onKindSelect = {},
            onDescriptionChange = {},
            onSave = {},
            onDelete = {},
            onDismiss = {}
        )
    }
}

