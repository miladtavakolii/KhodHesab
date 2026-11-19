package ir.khodhesab.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

/**
 * Navigation Host component for KhodHesab app.
 */
@Composable
fun KhodHesabNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    initialEditTransactionId: Long? = null
) {
    NavHost(
        navController = navController,
        startDestination = KhodHesabDestination.Dashboard.route,
        modifier = modifier
    ) {
        composable(KhodHesabDestination.Dashboard.route) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "صفحه خانه (داشبورد)",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        composable(KhodHesabDestination.Transactions.route) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val text = if (initialEditTransactionId != null) {
                    "ویرایش تراکنش #$initialEditTransactionId"
                } else {
                    "صفحه تراکنش‌ها"
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        composable(KhodHesabDestination.Reports.route) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "صفحه گزارش‌ها",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        composable(KhodHesabDestination.Categories.route) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "صفحه دسته‌بندی‌ها",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}
