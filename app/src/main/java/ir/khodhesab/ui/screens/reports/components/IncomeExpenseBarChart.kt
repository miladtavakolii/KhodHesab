package ir.khodhesab.ui.screens.reports.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ir.khodhesab.domain.model.TrendBucket
import ir.khodhesab.ui.theme.DepositGreen
import ir.khodhesab.ui.theme.WithdrawalRed

/**
 * Custom Canvas grouped bar chart displaying side-by-side Income (green) vs. Expense (red) bars across trend buckets.
 */
@Composable
fun IncomeExpenseBarChart(
    buckets: List<TrendBucket>,
    modifier: Modifier = Modifier
) {
    if (buckets.isEmpty()) return

    val maxVal = buckets.flatMap { listOf(it.totalIncome, it.totalExpense) }.maxOrNull()?.coerceAtLeast(1L) ?: 1L

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.width(12.dp).height(12.dp)) { drawCircle(DepositGreen) }
                Spacer(modifier = Modifier.width(4.dp))
                Text("واریز", style = MaterialTheme.typography.labelSmall)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.width(12.dp).height(12.dp)) { drawCircle(WithdrawalRed) }
                Spacer(modifier = Modifier.width(4.dp))
                Text("برداشت", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            val chartWidth = size.width
            val chartHeight = size.height
            val groupCount = buckets.size
            val groupWidth = chartWidth / groupCount
            val barWidth = (groupWidth * 0.35f).coerceAtMost(24.dp.toPx())

            buckets.forEachIndexed { i, bucket ->
                val groupCenterX = i * groupWidth + groupWidth / 2f

                val incHeight = (bucket.totalIncome.toFloat() / maxVal.toFloat()) * (chartHeight - 20.dp.toPx())
                val expHeight = (bucket.totalExpense.toFloat() / maxVal.toFloat()) * (chartHeight - 20.dp.toPx())

                val incLeft = groupCenterX - barWidth - 2.dp.toPx()
                val expLeft = groupCenterX + 2.dp.toPx()

                drawRoundRect(
                    color = DepositGreen,
                    topLeft = Offset(incLeft, chartHeight - incHeight),
                    size = Size(barWidth, incHeight),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )

                drawRoundRect(
                    color = WithdrawalRed,
                    topLeft = Offset(expLeft, chartHeight - expHeight),
                    size = Size(barWidth, expHeight),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            buckets.forEach { bucket ->
                Text(
                    text = bucket.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
