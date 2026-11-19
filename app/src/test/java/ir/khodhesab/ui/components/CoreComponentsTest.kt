package ir.khodhesab.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import ir.khodhesab.domain.model.Account
import ir.khodhesab.domain.model.Bank
import ir.khodhesab.domain.model.Category
import ir.khodhesab.domain.model.CurrencyUnit
import ir.khodhesab.domain.model.Transaction
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.ui.theme.KhodHesabTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Component tests for core reusable Compose UI widgets.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class CoreComponentsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bankAccountCard_rendersAccountTitleAndFormattedBalance() {
        var clicked = false
        val bank = Bank(code = "MELLAT", persianName = "بانک ملت", colorHex = "#E53935", iconKey = "ic")
        val account = Account(id = 1, bank = bank, accountIdentifier = "1234", title = "حساب ملت تستی", currentBalance = 10_000_000L)

        composeTestRule.setContent {
            KhodHesabTheme {
                BankAccountCard(
                    account = account,
                    isSelected = true,
                    currencyUnit = CurrencyUnit.TOMAN,
                    onClick = { clicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("حساب ملت تستی").assertIsDisplayed()
        composeTestRule.onNodeWithText("۱,۰۰۰,۰۰۰ تومان").assertIsDisplayed()

        composeTestRule.onNodeWithText("حساب ملت تستی").performClick()
        assertThat(clicked).isTrue()
    }

    @Test
    fun transactionListItem_rendersCategoryNameAndSignedAmount() {
        val bank = Bank(code = "BLU", persianName = "بلوبانک", colorHex = "#BLUE", iconKey = "ic")
        val account = Account(id = 2, bank = bank, accountIdentifier = "9988", title = "بلو")
        val category = Category(id = 5, name = "رستوران و غذا", kind = TransactionKind.WITHDRAWAL, iconKey = "food", colorHex = "#RED")
        val transaction = Transaction(id = 1, account = account, category = category, kind = TransactionKind.WITHDRAWAL, amount = 150_000L, timestamp = 1700000000000L)

        composeTestRule.setContent {
            KhodHesabTheme {
                TransactionListItem(
                    transaction = transaction,
                    currencyUnit = CurrencyUnit.TOMAN,
                    onClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("رستوران و غذا").assertIsDisplayed()
        composeTestRule.onNodeWithText("-۱۵,۰۰۰ تومان").assertIsDisplayed()
    }

    @Test
    fun currencyToggleChip_triggersCallbackOnSelection() {
        var selectedUnit = CurrencyUnit.TOMAN

        composeTestRule.setContent {
            KhodHesabTheme {
                CurrencyToggleChip(
                    selectedUnit = selectedUnit,
                    onUnitSelected = { selectedUnit = it }
                )
            }
        }

        composeTestRule.onNodeWithText("ریال").performClick()
        assertThat(selectedUnit).isEqualTo(CurrencyUnit.RIAL)
    }
}
