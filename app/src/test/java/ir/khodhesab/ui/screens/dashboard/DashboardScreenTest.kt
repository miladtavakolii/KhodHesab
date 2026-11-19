package ir.khodhesab.ui.screens.dashboard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import ir.khodhesab.domain.model.Account
import ir.khodhesab.domain.model.Bank
import ir.khodhesab.domain.model.CurrencyUnit
import ir.khodhesab.domain.model.FinancialSummary
import ir.khodhesab.ui.theme.KhodHesabTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * UI Component tests for [DashboardScreen].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dashboardScreen_rendersTitleAccountsAndFinancialSummary() {
        val bank = Bank(id = 1, code = "MELLAT", persianName = "بانک ملت", colorHex = "#RED", iconKey = "ic")
        val account = Account(id = 10L, bank = bank, accountIdentifier = "1234", title = "حساب اصلی ملت", currentBalance = 5_000_000L)

        val state = DashboardUiState(
            isLoading = false,
            totalBalance = 5_000_000L,
            accounts = listOf(account),
            periodSummary = FinancialSummary(totalIncome = 1_000_000L, totalExpense = 200_000L, transactionCount = 2),
            currencyUnit = CurrencyUnit.TOMAN,
            isInitialSmsSyncDone = true
        )

        composeTestRule.setContent {
            KhodHesabTheme {
                DashboardScreen(
                    state = state,
                    onSelectAccount = {},
                    onToggleCurrencyUnit = {},
                    onStartHistoricalSmsSync = {},
                    onDismissSyncMessage = {},
                    onRenameAccount = { _, _ -> },
                    onToggleAccountActive = { _, _ -> },
                    onTransactionClick = {},
                    onNavigateToTransactions = {},
                    onAddManualTransactionClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("خودحساب").assertIsDisplayed()
        composeTestRule.onNodeWithText("حساب اصلی ملت").assertIsDisplayed()
        composeTestRule.onNodeWithText("خلاصه مالی ماه جاری").assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_showsSyncBannerWhenSyncNotDone() {
        var syncStarted = false
        val state = DashboardUiState(
            isLoading = false,
            isInitialSmsSyncDone = false
        )

        composeTestRule.setContent {
            KhodHesabTheme {
                DashboardScreen(
                    state = state,
                    onSelectAccount = {},
                    onToggleCurrencyUnit = {},
                    onStartHistoricalSmsSync = { syncStarted = true },
                    onDismissSyncMessage = {},
                    onRenameAccount = { _, _ -> },
                    onToggleAccountActive = { _, _ -> },
                    onTransactionClick = {},
                    onNavigateToTransactions = {},
                    onAddManualTransactionClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("استخراج خودکار تراکنش‌ها").assertIsDisplayed()
        composeTestRule.onNodeWithText("شروع همگام‌سازی پیامک‌ها").performClick()

        assertThat(syncStarted).isTrue()
    }
}
