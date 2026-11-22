package ir.khodhesab.ui.screens.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.khodhesab.domain.model.Account

/**
 * Dialog for customizing account display title and active status.
 *
 * @param account Target [Account] domain model.
 * @param onDismiss Dismiss dialog callback.
 * @param onSave Save title & active status callback.
 */
@Composable
fun RenameAccountDialog(
    account: Account,
    onDismiss: () -> Unit,
    onSave: (newTitle: String, isActive: Boolean) -> Unit
) {
    var titleText by remember { mutableStateOf(account.title) }
    var isActiveState by remember { mutableStateOf(account.isActive) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "ویرایش حساب ${account.bank.persianName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    label = { Text("نام حساب") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "حساب فعال باشد",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isActiveState,
                        onCheckedChange = { isActiveState = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (titleText.isNotBlank()) {
                        onSave(titleText.trim(), isActiveState)
                        onDismiss()
                    }
                }
            ) {
                Text("ذخیره")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun RenameAccountDialogPreview() {
    ir.khodhesab.ui.theme.KhodHesabTheme {
        val bank = ir.khodhesab.domain.model.Bank(code = "MELLAT", persianName = "بانک ملت", colorHex = "#E53935", iconKey = "ic")
        val account = Account(id = 1, bank = bank, accountIdentifier = "1234", title = "حساب ملت", currentBalance = 10_000_000L)
        RenameAccountDialog(
            account = account,
            onDismiss = {},
            onSave = { _, _ -> }
        )
    }
}

