package ir.khodhesab.ui.screens.reports.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.khodhesab.domain.model.CategoryBreakdown
import ir.khodhesab.domain.model.CurrencyUnit
import ir.khodhesab.ui.util.CategoryIconMapper
import ir.khodhesab.ui.util.PersianCurrencyFormatter

/**
 * Custom Canvas Donut Chart rendering proportional arcs and breakdown list for category expense/income distribution.
 */
@Composable
fun CategoryDonutChart(
    breakdowns: List<CategoryBreakdown>,
    currencyUnit: CurrencyUnit,
    modifier: Modifier = Modifier
) {
    if (breakdowns.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val strokeWidth = 32.dp

        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(180.dp)) {
                var startAngle = -90f
                breakdowns.forEach { item ->
                    val sweepAngle = (item.percentage / 100f) * 360f
                    val color = CategoryIconMapper.parseHexColor(item.category?.colorHex)

                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
                    )
                    startAngle += sweepAngle
                }
            }

            val total = breakdowns.sumOf { it.totalAmount }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "مجموع",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = PersianCurrencyFormatter.formatAmount(total, currencyUnit),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            breakdowns.forEach { item ->
                val color = CategoryIconMapper.parseHexColor(item.category?.colorHex)
                val icon = CategoryIconMapper.getIcon(item.category?.iconKey)
                val name = item.category?.name ?: "دسته‌بندی نشده"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = PersianCurrencyFormatter.formatAmount(item.totalAmount, currencyUnit),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${PersianCurrencyFormatter.toPersianDigits(String.format("%.1f", item.percentage))}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun CategoryDonutChartPreview() {
    ir.khodhesab.ui.theme.KhodHesabTheme {
        val cat1 = ir.khodhesab.domain.model.Category(id = 1, name = "خوراکی", iconKey = "ic_cat_food", colorHex = "#FF5722", kind = ir.khodhesab.domain.model.TransactionKind.WITHDRAWAL)
        val cat2 = ir.khodhesab.domain.model.Category(id = 2, name = "حمل و نقل", iconKey = "ic_cat_transport", colorHex = "#2196F3", kind = ir.khodhesab.domain.model.TransactionKind.WITHDRAWAL)
        val breakdowns = listOf(
            CategoryBreakdown(category = cat1, totalAmount = 3_000_000L, transactionCount = 3, percentage = 75f),
            CategoryBreakdown(category = cat2, totalAmount = 1_000_000L, transactionCount = 1, percentage = 25f)
        )
        CategoryDonutChart(
            breakdowns = breakdowns,
            currencyUnit = CurrencyUnit.TOMAN
        )
    }
}

