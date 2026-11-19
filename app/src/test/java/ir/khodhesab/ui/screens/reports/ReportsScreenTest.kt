package ir.khodhesab.ui.screens.reports

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import ir.khodhesab.domain.model.FinancialSummary
import ir.khodhesab.domain.model.ReportPeriod
import ir.khodhesab.ui.theme.KhodHesabTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Component tests for [ReportsScreen].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class ReportsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun reportsScreen_rendersTitlePeriodTabsAndSummary() {
        var selectedPeriod: ReportPeriod? = null
        val state = ReportsUiState(
            isLoading = false,
            selectedPeriod = ReportPeriod.MONTHLY,
            periodTitle = "آبان ۱۴۰۵",
            summary = FinancialSummary(totalIncome = 1_000_000L, totalExpense = 300_000L, transactionCount = 4)
        )

        composeTestRule.setContent {
            KhodHesabTheme {
                ReportsScreen(
                    state = state,
                    onSelectPeriod = { selectedPeriod = it },
                    onPreviousPeriod = {},
                    onNextPeriod = {},
                    onSelectAccount = {},
                    onSelectBreakdownKind = {},
                    onExportCsv = {},
                    onCreateBackup = {},
                    onRestoreBackup = {},
                    onShareHandled = {},
                    onDismissMessage = {}
                )
            }
        }

        composeTestRule.onNodeWithText("گزارش‌های تحلیلی").assertIsDisplayed()
        composeTestRule.onNodeWithText("آبان ۱۴۰۵").assertIsDisplayed()

        composeTestRule.onNodeWithText("روزانه").performClick()
        assertThat(selectedPeriod).isEqualTo(ReportPeriod.DAILY)
    }
}
