package ir.khodhesab.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Helper utility mapping category and bank icon keys to Material [ImageVector] icons and parsing Hex colors safely.
 */
object CategoryIconMapper {

    /**
     * Maps an icon key string to its corresponding [ImageVector].
     *
     * @param iconKey Key string (e.g. `"ic_cat_food"`, `"ic_cat_transport"`).
     * @return Corresponding Material [ImageVector].
     */
    fun getIcon(iconKey: String?): ImageVector {
        return when (iconKey?.lowercase()?.trim()) {
            "ic_cat_food", "food" -> Icons.Default.Restaurant
            "ic_cat_transport", "transport" -> Icons.Default.DirectionsCar
            "ic_cat_shopping", "shopping" -> Icons.Default.ShoppingBag
            "ic_cat_bills", "bills" -> Icons.Default.Receipt
            "ic_cat_housing", "housing" -> Icons.Default.Home
            "ic_cat_health", "health" -> Icons.Default.LocalHospital
            "ic_cat_leisure", "leisure" -> Icons.Default.Flight
            "ic_cat_education", "education" -> Icons.Default.School
            "ic_cat_bank_fee", "bank_fee" -> Icons.Default.AccountBalance
            "ic_cat_salary", "salary" -> Icons.Default.Payments
            "ic_cat_business", "business" -> Icons.Default.Work
            "ic_cat_investment", "investment" -> Icons.Default.AttachMoney
            "ic_cat_subsidy", "subsidy" -> Icons.Default.Lightbulb
            "ic_cat_refund", "refund" -> Icons.Default.Store
            else -> Icons.Default.Category
        }
    }

    /**
     * Safely parses a hex color string into a Compose [Color].
     *
     * @param hex Hex string (e.g. `"#FF5722"` or `"FF5722"`).
     * @param fallback Fallback [Color] if parsing fails.
     * @return Parsed [Color].
     */
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
