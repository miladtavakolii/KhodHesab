package ir.khodhesab.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Top-level destinations for KhodHesab navigation bar.
 */
sealed class KhodHesabDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Dashboard : KhodHesabDestination("dashboard", "خانه", Icons.Default.Home)
    data object Transactions : KhodHesabDestination("transactions", "تراکنش‌ها", Icons.AutoMirrored.Filled.ReceiptLong)
    data object Reports : KhodHesabDestination("reports", "گزارش‌ها", Icons.Default.PieChart)
    data object Categories : KhodHesabDestination("categories", "دسته‌بندی‌ها", Icons.Default.Category)

    companion object {
        val items = listOf(Dashboard, Transactions, Reports, Categories)
    }
}
