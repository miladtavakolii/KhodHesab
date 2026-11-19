package ir.khodhesab.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.khodhesab.domain.model.Account
import ir.khodhesab.domain.model.Bank
import ir.khodhesab.domain.model.CurrencyUnit
import ir.khodhesab.ui.theme.KhodHesabTheme
import ir.khodhesab.ui.util.CategoryIconMapper
import ir.khodhesab.ui.util.PersianCurrencyFormatter

/**
 * Card component displaying account balance and bank details.
 *
 * @param account [Account] domain model to render. Null for "All Accounts" summary card.
 * @param isSelected `true` if this card is currently selected.
 * @param currencyUnit [CurrencyUnit] for balance display.
 * @param onClick Click listener callback.
 */
@Composable
fun BankAccountCard(
    account: Account?,
    isSelected: Boolean = false,
    currencyUnit: CurrencyUnit = CurrencyUnit.TOMAN,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bankColor = account?.bank?.let { CategoryIconMapper.parseHexColor(it.colorHex) } ?: MaterialTheme.colorScheme.primary
    val title = account?.title ?: "همه حساب‌ها"
    val subtitle = account?.let { "${it.bank.persianName} (${it.accountIdentifier})" } ?: "مجموع موجودی حساب‌های فعال"
    val balance = account?.currentBalance ?: 0L

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (isSelected) Modifier.border(2.dp, bankColor, RoundedCornerShape(16.dp)) else Modifier
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) bankColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(bankColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (account != null) Icons.Default.CreditCard else Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = bankColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = PersianCurrencyFormatter.formatAmount(balance, currencyUnit),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = bankColor
            )
        }
    }
}

@Preview
@Composable
private fun BankAccountCardPreview() {
    KhodHesabTheme {
        val bank = Bank(code = "MELLAT", persianName = "بانک ملت", colorHex = "#E53935", iconKey = "ic")
        val account = Account(id = 1, bank = bank, accountIdentifier = "1234", title = "حساب اصلی ملت", currentBalance = 15_000_000L)
        BankAccountCard(account = account, isSelected = true, onClick = {})
    }
}
