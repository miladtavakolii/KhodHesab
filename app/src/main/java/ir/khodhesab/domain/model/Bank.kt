package ir.khodhesab.domain.model

/**
 * Pure domain model representing an Iranian banking institution.
 *
 * @property id Unique bank identifier.
 * @property code Unique bank identifier code (e.g. `"MELLAT"`, `"MELLI"`).
 * @property persianName Official Persian name of the bank.
 * @property senderNumbers Known SMS sender numbers/shortcodes.
 * @property colorHex Accent color in hex format.
 * @property iconKey UI icon mapping key.
 */
data class Bank(
    val id: Long = 0,
    val code: String,
    val persianName: String,
    val senderNumbers: List<String> = emptyList(),
    val colorHex: String,
    val iconKey: String
)
