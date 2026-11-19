package ir.khodhesab.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.khodhesab.core.date.JalaliCalendarHelper
import ir.khodhesab.domain.model.CurrencyUnit
import ir.khodhesab.domain.model.Transaction
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.ui.theme.DepositGreen
import ir.khodhesab.ui.theme.TransferBlue
import ir.khodhesab.ui.theme.WithdrawalRed
import ir.khodhesab.ui.util.CategoryIconMapper
import ir.khodhesab.ui.util.PersianCurrencyFormatter

/**
 * List item component displaying transaction details.
 *
 * @param transaction [Transaction] domain model to render.
 * @param currencyUnit [CurrencyUnit] for amount formatting.
 * @param onClick Click listener callback.
 */
@Composable
fun TransactionListItem(
    transaction: Transaction,
    currencyUnit: CurrencyUnit = CurrencyUnit.TOMAN,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryName = transaction.category?.name ?: "دسته‌بندی نشده"
    val categoryIcon = CategoryIconMapper.getIcon(transaction.category?.iconKey)
    val categoryColor = transaction.category?.colorHex?.let { CategoryIconMapper.parseHexColor(it) } ?: MaterialTheme.colorScheme.onSurfaceVariant

    val amountColor = when (transaction.kind) {
        TransactionKind.DEPOSIT -> DepositGreen
        TransactionKind.WITHDRAWAL -> WithdrawalRed
        TransactionKind.TRANSFER -> TransferBlue
    }

    val jalaliDate = JalaliCalendarHelper.fromEpochMillis(transaction.timestamp)
    val dateText = JalaliCalendarHelper.formatText(jalaliDate)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(categoryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = categoryColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${transaction.account.bank.persianName} • $dateText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Text(
            text = PersianCurrencyFormatter.formatSignedAmount(transaction.amount, transaction.kind, currencyUnit),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = amountColor
        )
    }
}
