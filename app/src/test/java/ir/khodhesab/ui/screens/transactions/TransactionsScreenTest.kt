package ir.khodhesab.ui.screens.transactions

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import ir.khodhesab.domain.model.Account
import ir.khodhesab.domain.model.Bank
import ir.khodhesab.domain.model.Category
import ir.khodhesab.domain.model.Transaction
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.ui.theme.KhodHesabTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Component tests for [TransactionsScreen].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class TransactionsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun transactionsScreen_rendersFilterChipsAndTransactionList() {
        val bank = Bank(id = 1, code = "MELLAT", persianName = "ملت", colorHex = "#R", iconKey = "i")
        val account = Account(id = 10, bank = bank, accountIdentifier = "1234", title = "حساب ملت")
        val category = Category(id = 5, name = "رستوران", kind = TransactionKind.WITHDRAWAL, iconKey = "food", colorHex = "#R")
        val tx = Transaction(id = 1, account = account, category = category, kind = TransactionKind.WITHDRAWAL, amount = 100_000L, timestamp = 1700000000000L)

        val state = TransactionsUiState(
            isLoading = false,
            accounts = listOf(account),
            transactions = listOf(tx)
        )

        composeTestRule.setContent {
            KhodHesabTheme {
                TransactionsScreen(
                    state = state,
                    onSearchQueryChange = {},
                    onAccountFilterChange = {},
                    onKindFilterChange = {},
                    onToggleUncategorizedOnly = {},
                    onClearFilters = {},
                    onTransactionClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("تراکنش‌ها").assertIsDisplayed()
        composeTestRule.onNodeWithText("دسته‌بندی نشده").assertIsDisplayed()
        composeTestRule.onNodeWithText("رستوران").assertIsDisplayed()
    }

    @Test
    fun transactionsScreen_showsEmptyStateWhenListIsEmpty() {
        var clearClicked = false
        val state = TransactionsUiState(
            isLoading = false,
            onlyUncategorized = true,
            transactions = emptyList()
        )

        composeTestRule.setContent {
            KhodHesabTheme {
                TransactionsScreen(
                    state = state,
                    onSearchQueryChange = {},
                    onAccountFilterChange = {},
                    onKindFilterChange = {},
                    onToggleUncategorizedOnly = {},
                    onClearFilters = { clearClicked = true },
                    onTransactionClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("هیچ تراکنشی یافت نشد").assertIsDisplayed()
        composeTestRule.onNodeWithText("پاکسازی فیلترها").performClick()

        assertThat(clearClicked).isTrue()
    }
}
