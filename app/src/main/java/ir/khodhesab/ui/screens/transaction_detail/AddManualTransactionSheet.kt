package ir.khodhesab.ui.screens.transaction_detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.khodhesab.domain.model.Account
import ir.khodhesab.domain.model.CurrencyUnit
import ir.khodhesab.domain.model.TransactionKind

/**
 * Modal BottomSheet for adding manual cash/non-SMS transactions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddManualTransactionSheet(
    accounts: List<Account>,
    currencyUnit: CurrencyUnit,
    onAdd: (accountId: Long, kind: TransactionKind, amountRials: Long, description: String?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 0L) }
    var selectedKind by remember { mutableStateOf(TransactionKind.WITHDRAWAL) }
    var amountInput by remember { mutableStateOf("") }
    var descriptionInput by remember { mutableStateOf("") }

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
            Text(
                text = "ثبت تراکنش دستی",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedKind == TransactionKind.WITHDRAWAL,
                    onClick = { selectedKind = TransactionKind.WITHDRAWAL },
                    label = { Text("برداشت") }
                )
                FilterChip(
                    selected = selectedKind == TransactionKind.DEPOSIT,
                    onClick = { selectedKind = TransactionKind.DEPOSIT },
                    label = { Text("واریز") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = amountInput,
                onValueChange = { amountInput = it.filter { c -> c.isDigit() } },
                label = { Text("مبلغ (${if (currencyUnit == CurrencyUnit.TOMAN) "تومان" else "ریال"})") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = descriptionInput,
                onValueChange = { descriptionInput = it },
                label = { Text("شرح / توضیحات") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val rawAmount = amountInput.toLongOrNull() ?: 0L
                    val amountRials = if (currencyUnit == CurrencyUnit.TOMAN) rawAmount * 10L else rawAmount
                    if (amountRials > 0L && selectedAccountId > 0L) {
                        onAdd(selectedAccountId, selectedKind, amountRials, descriptionInput.ifEmpty { null })
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ثبت تراکنش")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun AddManualTransactionSheetPreview() {
    ir.khodhesab.ui.theme.KhodHesabTheme {
        val bank = ir.khodhesab.domain.model.Bank(code = "MELLAT", persianName = "بانک ملت", colorHex = "#E53935", iconKey = "ic")
        val account = Account(id = 1, bank = bank, accountIdentifier = "1234", title = "حساب ملت", currentBalance = 10_000_000L)
        AddManualTransactionSheet(
            accounts = listOf(account),
            currencyUnit = CurrencyUnit.TOMAN,
            onAdd = { _, _, _, _ -> },
            onDismiss = {}
        )
    }
}

