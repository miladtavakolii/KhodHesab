package ir.khodhesab.ui.screens.categories

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import ir.khodhesab.domain.model.Category
import ir.khodhesab.domain.model.TransactionKind
import ir.khodhesab.ui.theme.KhodHesabTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Component tests for [CategoriesScreen].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class CategoriesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun categoriesScreen_rendersCategoriesListAndKindTabs() {
        var selectedKind: TransactionKind? = null
        val category = Category(id = 1L, name = "رستوران تستی", kind = TransactionKind.WITHDRAWAL, iconKey = "food", colorHex = "#RED", isDefault = true)

        val state = CategoriesUiState(
            isLoading = false,
            selectedKind = TransactionKind.WITHDRAWAL,
            categories = listOf(category)
        )

        composeTestRule.setContent {
            KhodHesabTheme {
                CategoriesScreen(
                    state = state,
                    onSelectKind = { selectedKind = it },
                    onStartCreateCategory = {},
                    onStartEditCategory = {},
                    onDismissEditor = {},
                    onSaveCategory = { _, _, _, _ -> },
                    onDeleteCategory = {},
                    onRunRetroactiveAutoCategorization = {},
                    onConfirmInternalTransfer = {},
                    onDismissFeedback = {}
                )
            }
        }

        composeTestRule.onNodeWithText("مدیریت دسته‌بندی‌ها").assertIsDisplayed()
        composeTestRule.onNodeWithText("رستوران تستی").assertIsDisplayed()

        composeTestRule.onNodeWithText("واریز (درآمد)").performClick()
        assertThat(selectedKind).isEqualTo(TransactionKind.DEPOSIT)
    }

    @Test
    fun categoriesScreen_opensCreationSheetOnFabClick() {
        var createClicked = false
        val state = CategoriesUiState(isLoading = false)

        composeTestRule.setContent {
            KhodHesabTheme {
                CategoriesScreen(
                    state = state,
                    onSelectKind = {},
                    onStartCreateCategory = { createClicked = true },
                    onStartEditCategory = {},
                    onDismissEditor = {},
                    onSaveCategory = { _, _, _, _ -> },
                    onDeleteCategory = {},
                    onRunRetroactiveAutoCategorization = {},
                    onConfirmInternalTransfer = {},
                    onDismissFeedback = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("دسته‌بندی جدید").performClick()
        assertThat(createClicked).isTrue()
    }
}
