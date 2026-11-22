package ir.khodhesab.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.khodhesab.domain.model.CurrencyUnit
import ir.khodhesab.domain.model.FinancialSummary
import ir.khodhesab.ui.theme.DepositGreen
import ir.khodhesab.ui.theme.WithdrawalRed
import ir.khodhesab.ui.util.PersianCurrencyFormatter

/**
 * Row component displaying income (واریز), expense (برداشت), and net flow cards.
 *
 * @param summary [FinancialSummary] data model.
 * @param currencyUnit [CurrencyUnit] for amount formatting.
 */
@Composable
fun FinancialSummaryRow(
    summary: FinancialSummary,
    modifier: Modifier = Modifier,
    currencyUnit: CurrencyUnit = CurrencyUnit.TOMAN
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DepositGreen.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "کل دریافتی",
                    style = MaterialTheme.typography.labelMedium,
                    color = DepositGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = PersianCurrencyFormatter.formatAmount(summary.totalIncome, currencyUnit),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DepositGreen
                )
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = WithdrawalRed.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "کل پرداختی",
                    style = MaterialTheme.typography.labelMedium,
                    color = WithdrawalRed
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = PersianCurrencyFormatter.formatAmount(summary.totalExpense, currencyUnit),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = WithdrawalRed
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun FinancialSummaryRowPreview() {
    ir.khodhesab.ui.theme.KhodHesabTheme {
        FinancialSummaryRow(
            summary = FinancialSummary(
                totalIncome = 45_000_000L,
                totalExpense = 18_500_000L,
                transactionCount = 10
            ),
            currencyUnit = CurrencyUnit.TOMAN
        )
    }
}

