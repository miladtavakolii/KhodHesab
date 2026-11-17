package ir.khodhesab.core.date

/**
 * Represents a date and time in the Jalali (Persian / Solar Hijri) calendar system.
 *
 * @property year Jalali year (e.g., 1403).
 * @property month Jalali month (1 to 12).
 * @property day Jalali day of month (1 to 31).
 * @property hour Hour of day in 24-hour format (0 to 23).
 * @property minute Minute of hour (0 to 59).
 * @property second Second of minute (0 to 59).
 * @property millisecond Millisecond of second (0 to 999).
 */
data class JalaliDate(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int = 0,
    val minute: Int = 0,
    val second: Int = 0,
    val millisecond: Int = 0
)
