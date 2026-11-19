package ir.khodhesab.domain.model

/**
 * Currency unit options for displaying financial amounts in UI.
 */
enum class CurrencyUnit {
    /**
     * Toman (تومان) — Common Iranian display unit (1 Toman = 10 Rials).
     */
    TOMAN,

    /**
     * Rial (ریال) — Official banking unit stored internally in database.
     */
    RIAL
}
