package ir.khodhesab.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class SelectableCategoryIcon(
    val key: String,
    val label: String,
    val icon: ImageVector
)

/**
 * Helper utility mapping category and bank icon keys to Material [ImageVector] icons and parsing Hex colors safely.
 */
object CategoryIconMapper {

    val AVAILABLE_ICONS = listOf(
        SelectableCategoryIcon("ic_cat_food", "غذا و رستوران", Icons.Default.Restaurant),
        SelectableCategoryIcon("ic_cat_transport", "حمل و نقل", Icons.Default.DirectionsCar),
        SelectableCategoryIcon("ic_cat_shopping", "خرید و پوشاک", Icons.Default.ShoppingBag),
        SelectableCategoryIcon("ic_cat_bills", "قبوض و شارژ", Icons.Default.Receipt),
        SelectableCategoryIcon("ic_cat_housing", "مسکن و اجاره", Icons.Default.Home),
        SelectableCategoryIcon("ic_cat_health", "درمان و سلامت", Icons.Default.LocalHospital),
        SelectableCategoryIcon("ic_cat_leisure", "تفریح و سفر", Icons.Default.Flight),
        SelectableCategoryIcon("ic_cat_education", "آموزش و کتاب", Icons.Default.School),
        SelectableCategoryIcon("ic_cat_bank_fee", "کارمزد بانکی", Icons.Default.AccountBalance),
        SelectableCategoryIcon("ic_cat_salary", "حقوق و دستمزد", Icons.Default.Payments),
        SelectableCategoryIcon("ic_cat_business", "کسب‌وکار", Icons.Default.Work),
        SelectableCategoryIcon("ic_cat_investment", "سرمایه‌گذاری", Icons.Default.AttachMoney),
        SelectableCategoryIcon("ic_cat_subsidy", "یارانه", Icons.Default.Lightbulb),
        SelectableCategoryIcon("ic_cat_refund", "بازگشت وجه", Icons.Default.Store),
        SelectableCategoryIcon("ic_cat_gift", "هدیه", Icons.Default.CardGiftcard),
        SelectableCategoryIcon("ic_cat_sports", "ورزش", Icons.Default.FitnessCenter),
        SelectableCategoryIcon("ic_cat_cinema", "سینما و هنر", Icons.Default.Movie),
        SelectableCategoryIcon("ic_cat_other", "سایر", Icons.Default.Category)
    )

    val SELECTABLE_COLORS = listOf(
        "#FF5722", "#FF9800", "#E91E63", "#9C27B0", "#795548",
        "#F44336", "#00BCD4", "#3F51B5", "#607D8B", "#4CAF50",
        "#2196F3", "#009688"
    )

    fun getIcon(iconKey: String?): ImageVector {
        return AVAILABLE_ICONS.find { it.key.equals(iconKey, ignoreCase = true) }?.icon
            ?: when (iconKey?.lowercase()?.trim()) {
                "food" -> Icons.Default.Restaurant
                "transport" -> Icons.Default.DirectionsCar
                "shopping" -> Icons.Default.ShoppingBag
                "bills" -> Icons.Default.Receipt
                "housing" -> Icons.Default.Home
                "health" -> Icons.Default.LocalHospital
                "leisure" -> Icons.Default.Flight
                "education" -> Icons.Default.School
                "bank_fee" -> Icons.Default.AccountBalance
                "salary" -> Icons.Default.Payments
                "business" -> Icons.Default.Work
                "investment" -> Icons.Default.AttachMoney
                "subsidy" -> Icons.Default.Lightbulb
                "refund" -> Icons.Default.Store
                else -> Icons.Default.Category
            }
    }

    fun parseHexColor(hex: String?, fallback: Color = Color(0xFF607D8B)): Color {
        if (hex.isNullOrBlank()) return fallback
        return try {
            val cleanHex = hex.trim().removePrefix("#")
            val colorInt = when (cleanHex.length) {
                6 -> "FF$cleanHex".toLong(16)
                8 -> cleanHex.toLong(16)
                else -> return fallback
            }
            Color(colorInt)
        } catch (e: Exception) {
            fallback
        }
    }
}
