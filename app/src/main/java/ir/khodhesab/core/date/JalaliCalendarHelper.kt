package ir.khodhesab.core.date

import java.util.Calendar
import java.util.TimeZone

/**
 * Helper utility for Jalali (Persian) calendar conversions, date math, leap year calculations,
 * formatting, and financial date range boundaries.
 *
 * All operations are pure Kotlin without external dependencies and support flexible time zones.
 */
object JalaliCalendarHelper {

    /**
     * Persian month names in chronological order (Farvardin to Esfand).
     */
    val PERSIAN_MONTH_NAMES = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    /**
     * Converts a Gregorian date `(gy, gm, gd)` to [JalaliDate].
     *
     * @param gy Gregorian year (e.g. 2024).
     * @param gm Gregorian month (1 to 12).
     * @param gd Gregorian day of month (1 to 31).
     * @return Corresponding [JalaliDate].
     */
    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): JalaliDate {
        val gDaysInMonth = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

        val gy2 = gy - 1600
        val gm2 = gm - 1
        val gd2 = gd - 1

        var gDayNo = 365L * gy2 + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400
        for (i in 0 until gm2) {
            gDayNo += gDaysInMonth[i + 1]
        }
        if (gm2 > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) {
            gDayNo += 1
        }
        gDayNo += gd2

        var jDayNo = gDayNo - 79

        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        var i = 0
        while (i < 11 && jDayNo >= (if (i < 6) 31 else 30)) {
            jDayNo -= if (i < 6) 31 else 30
            i++
        }
        val jm = i + 1
        val jd = (jDayNo + 1).toInt()

        return JalaliDate(jy.toInt(), jm, jd)
    }

    /**
     * Converts a Jalali date `(jy, jm, jd)` to Gregorian date `(gy, gm, gd)`.
     *
     * @param jy Jalali year.
     * @param jm Jalali month (1 to 12).
     * @param jd Jalali day of month (1 to 31).
     * @return [Triple] containing `(year, month, day)` in Gregorian calendar.
     */
    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
        val jy2 = jy.toLong() - 979
        val jDayNo = 365 * jy2 + (jy2 / 33) * 8 + ((jy2 % 33) + 3) / 4 + (if (jm <= 7) (jm - 1) * 31 else (jm - 1) * 30 + 6) + (jd - 1)

        var gDayNo = jDayNo + 79

        var gy = 1600L + 400 * (gDayNo / 146097)
        gDayNo %= 146097

        var leap = true
        if (gDayNo >= 36525) {
            gDayNo--
            gy += 100 * (gDayNo / 36524)
            gDayNo %= 36524

            if (gDayNo >= 365) {
                gDayNo++
            } else {
                leap = false
            }
        }

        gy += 4 * (gDayNo / 1461)
        gDayNo %= 1461

        if (gDayNo >= 366) {
            leap = false
            gDayNo--
            gy += gDayNo / 365
            gDayNo %= 365
        }

        val gDaysInMonth = intArrayOf(0, 31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gm = 1
        while (gDayNo >= gDaysInMonth[gm]) {
            gDayNo -= gDaysInMonth[gm]
            gm++
        }
        val gd = (gDayNo + 1).toInt()

        return Triple(gy.toInt(), gm, gd)
    }

    /**
     * Checks if a given Jalali year is a leap year (366 days).
     *
     * Determined by checking if the year span to the next year contains 366 days.
     *
     * @param year Jalali year to check.
     * @return `true` if Esfand has 30 days, `false` if Esfand has 29 days.
     */
    fun isJalaliLeapYear(year: Int): Boolean {
        val (gy1, gm1, gd1) = jalaliToGregorian(year, 1, 1)
        val (gy2, gm2, gd2) = jalaliToGregorian(year + 1, 1, 1)

        val cal1 = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(gy1, gm1 - 1, gd1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val cal2 = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(gy2, gm2 - 1, gd2, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diffDays = (cal2.timeInMillis - cal1.timeInMillis) / (1000 * 60 * 60 * 24)
        return diffDays == 366L
    }

    /**
     * Returns the maximum number of days in a specific Jalali month.
     *
     * @param year Jalali year.
     * @param month Jalali month (1 to 12).
     * @return 31 days for months 1..6, 30 days for months 7..11, and 30 (leap) or 29 (common) for month 12.
     */
    fun getDaysInJalaliMonth(year: Int, month: Int): Int {
        require(month in 1..12) { "Jalali month must be between 1 and 12." }
        return when (month) {
            in 1..6 -> 31
            in 7..11 -> 30
            12 -> if (isJalaliLeapYear(year)) 30 else 29
            else -> 30
        }
    }

    /**
     * Converts epoch milliseconds to [JalaliDate] using the specified time zone.
     *
     * @param epochMillis UTC timestamp in milliseconds.
     * @param timeZone Target time zone (defaults to UTC).
     * @return Corresponding [JalaliDate] with time fields populated.
     */
    fun fromEpochMillis(epochMillis: Long, timeZone: TimeZone = TimeZone.getTimeZone("UTC")): JalaliDate {
        val calendar = Calendar.getInstance(timeZone).apply {
            timeInMillis = epochMillis
        }
        val gy = calendar.get(Calendar.YEAR)
        val gm = calendar.get(Calendar.MONTH) + 1
        val gd = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        val ms = calendar.get(Calendar.MILLISECOND)

        val jDate = gregorianToJalali(gy, gm, gd)
        return jDate.copy(hour = hour, minute = minute, second = second, millisecond = ms)
    }

    /**
     * Converts a [JalaliDate] to epoch milliseconds using the specified time zone.
     *
     * @param jalaliDate Date and time in Jalali calendar.
     * @param timeZone Target time zone (defaults to UTC).
     * @return UTC epoch timestamp in milliseconds.
     */
    fun toEpochMillis(jalaliDate: JalaliDate, timeZone: TimeZone = TimeZone.getTimeZone("UTC")): Long {
        val (gy, gm, gd) = jalaliToGregorian(jalaliDate.year, jalaliDate.month, jalaliDate.day)
        val calendar = Calendar.getInstance(timeZone).apply {
            set(Calendar.YEAR, gy)
            set(Calendar.MONTH, gm - 1)
            set(Calendar.DAY_OF_MONTH, gd)
            set(Calendar.HOUR_OF_DAY, jalaliDate.hour)
            set(Calendar.MINUTE, jalaliDate.minute)
            set(Calendar.SECOND, jalaliDate.second)
            set(Calendar.MILLISECOND, jalaliDate.millisecond)
        }
        return calendar.timeInMillis
    }

    /**
     * Returns start of day (00:00:00.000) and end of day (23:59:59.999) timestamps for the given epoch milliseconds.
     *
     * @param epochMillis Input timestamp.
     * @param timeZone Target time zone.
     * @return `Pair(startEpochMillis, endEpochMillis)`.
     */
    fun getDayRange(epochMillis: Long, timeZone: TimeZone = TimeZone.getTimeZone("UTC")): Pair<Long, Long> {
        val jDate = fromEpochMillis(epochMillis, timeZone)
        val start = toEpochMillis(jDate.copy(hour = 0, minute = 0, second = 0, millisecond = 0), timeZone)
        val end = toEpochMillis(jDate.copy(hour = 23, minute = 59, second = 59, millisecond = 999), timeZone)
        return Pair(start, end)
    }

    /**
     * Returns start of Iranian week (Saturday 00:00:00.000) and end of week (Friday 23:59:59.999).
     *
     * In the Iranian calendar system, Saturday is the first day of the week and Friday is the last.
     *
     * @param epochMillis Input timestamp.
     * @param timeZone Target time zone.
     * @return `Pair(saturdayStartMillis, fridayEndMillis)`.
     */
    fun getWeekRange(epochMillis: Long, timeZone: TimeZone = TimeZone.getTimeZone("UTC")): Pair<Long, Long> {
        val calendar = Calendar.getInstance(timeZone).apply {
            timeInMillis = epochMillis
        }
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysSinceSaturday = (dayOfWeek - Calendar.SATURDAY + 7) % 7

        val satCal = (calendar.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, -daysSinceSaturday)
        }
        val friCal = (calendar.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, 6 - daysSinceSaturday)
        }

        val satRange = getDayRange(satCal.timeInMillis, timeZone)
        val friRange = getDayRange(friCal.timeInMillis, timeZone)
        return Pair(satRange.first, friRange.second)
    }

    /**
     * Returns start of Jalali month (1st day 00:00:00.000) and end of Jalali month (last day 23:59:59.999).
     *
     * Correctly handles 31-day months (1..6), 30-day months (7..11), and 29/30 day Esfand.
     *
     * @param epochMillis Input timestamp.
     * @param timeZone Target time zone.
     * @return `Pair(monthStartMillis, monthEndMillis)`.
     */
    fun getMonthRange(epochMillis: Long, timeZone: TimeZone = TimeZone.getTimeZone("UTC")): Pair<Long, Long> {
        val jDate = fromEpochMillis(epochMillis, timeZone)
        val lastDay = getDaysInJalaliMonth(jDate.year, jDate.month)

        val start = toEpochMillis(
            JalaliDate(jDate.year, jDate.month, 1, 0, 0, 0, 0),
            timeZone
        )
        val end = toEpochMillis(
            JalaliDate(jDate.year, jDate.month, lastDay, 23, 59, 59, 999),
            timeZone
        )
        return Pair(start, end)
    }

    /**
     * Formats a [JalaliDate] in numeric format (e.g. `"۱۴۰۵/۰۸/۲۶"`).
     *
     * @param jalaliDate Input Jalali date.
     * @param usePersianDigits If `true`, returns Persian digits (`۱۴۰۵/۰۸/۲۶`), otherwise English (`1405/08/26`).
     * @return Formatted string.
     */
    fun formatNumeric(jalaliDate: JalaliDate, usePersianDigits: Boolean = true): String {
        val raw = "%04d/%02d/%02d".format(jalaliDate.year, jalaliDate.month, jalaliDate.day)
        return if (usePersianDigits) {
            raw.map { char ->
                if (char in '0'..'9') (char + 1728) else char
            }.joinToString("")
        } else {
            raw
        }
    }

    /**
     * Formats a [JalaliDate] in text format with month name (e.g. `"۲۶ آبان ۱۴۰۵"`).
     *
     * @param jalaliDate Input Jalali date.
     * @param usePersianDigits If `true`, uses Persian digits for day and year.
     * @return Formatted string.
     */
    fun formatText(jalaliDate: JalaliDate, usePersianDigits: Boolean = true): String {
        val monthName = PERSIAN_MONTH_NAMES.getOrElse(jalaliDate.month - 1) { "" }
        val raw = "${jalaliDate.day} $monthName ${jalaliDate.year}"
        return if (usePersianDigits) {
            raw.map { char ->
                if (char in '0'..'9') (char + 1728) else char
            }.joinToString("")
        } else {
            raw
        }
    }
}
