package ir.khodhesab.core.date

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.TimeZone

/**
 * Unit tests covering [JalaliCalendarHelper] conversion accuracy, leap years, month boundaries,
 * Iranian week ranges, and text formatting.
 */
class JalaliCalendarHelperTest {

    private val testTimeZone = TimeZone.getTimeZone("UTC")

    @Test
    fun GregorianToJalali_knownDatesConvertedAccurately() {
        // 2024-03-20 -> 1403-01-01 (Nowruz)
        val j1 = JalaliCalendarHelper.gregorianToJalali(2024, 3, 20)
        assertThat(j1.year).isEqualTo(1403)
        assertThat(j1.month).isEqualTo(1)
        assertThat(j1.day).isEqualTo(1)

        // 2026-11-17 -> 1405-08-26
        val j2 = JalaliCalendarHelper.gregorianToJalali(2026, 11, 17)
        assertThat(j2.year).isEqualTo(1405)
        assertThat(j2.month).isEqualTo(8)
        assertThat(j2.day).isEqualTo(26)
    }

    @Test
    fun JalaliToGregorian_knownDatesConvertedAccurately() {
        // 1403-01-01 -> 2024-03-20
        val (gy, gm, gd) = JalaliCalendarHelper.jalaliToGregorian(1403, 1, 1)
        assertThat(gy).isEqualTo(2024)
        assertThat(gm).isEqualTo(3)
        assertThat(gd).isEqualTo(20)
    }

    @Test
    fun isJalaliLeapYear_identifiesLeapYearsCorrectly() {
        // 1399 and 1403 are leap years (Esfand has 30 days)
        assertThat(JalaliCalendarHelper.isJalaliLeapYear(1399)).isTrue()
        assertThat(JalaliCalendarHelper.isJalaliLeapYear(1403)).isTrue()

        // 1400, 1401, 1402 are common years (Esfand has 29 days)
        assertThat(JalaliCalendarHelper.isJalaliLeapYear(1400)).isFalse()
        assertThat(JalaliCalendarHelper.isJalaliLeapYear(1401)).isFalse()
        assertThat(JalaliCalendarHelper.isJalaliLeapYear(1402)).isFalse()
    }

    @Test
    fun getDaysInJalaliMonth_firstHalfOfYearHas31Days() {
        for (m in 1..6) {
            assertThat(JalaliCalendarHelper.getDaysInJalaliMonth(1403, m)).isEqualTo(31)
        }
    }

    @Test
    fun getDaysInJalaliMonth_secondHalfOfYearHas30DaysExceptEsfand() {
        for (m in 7..11) {
            assertThat(JalaliCalendarHelper.getDaysInJalaliMonth(1403, m)).isEqualTo(30)
        }
    }

    @Test
    fun getDaysInJalaliMonth_esfandDiffersInLeapVsCommonYear() {
        // Leap year Esfand
        assertThat(JalaliCalendarHelper.getDaysInJalaliMonth(1403, 12)).isEqualTo(30)
        // Common year Esfand
        assertThat(JalaliCalendarHelper.getDaysInJalaliMonth(1402, 12)).isEqualTo(29)
    }

    @Test
    fun getDayRange_calculatesStartAndEndOfDay() {
        val jDate = JalaliDate(1403, 1, 15, 14, 30, 0)
        val millis = JalaliCalendarHelper.toEpochMillis(jDate, testTimeZone)

        val (start, end) = JalaliCalendarHelper.getDayRange(millis, testTimeZone)

        val startJalali = JalaliCalendarHelper.fromEpochMillis(start, testTimeZone)
        val endJalali = JalaliCalendarHelper.fromEpochMillis(end, testTimeZone)

        assertThat(startJalali.day).isEqualTo(15)
        assertThat(startJalali.hour).isEqualTo(0)
        assertThat(startJalali.minute).isEqualTo(0)

        assertThat(endJalali.day).isEqualTo(15)
        assertThat(endJalali.hour).isEqualTo(23)
        assertThat(endJalali.minute).isEqualTo(59)
    }

    @Test
    fun getWeekRange_calculatesIranianWeekSaturdayToFriday() {
        // 2024-03-23 is Saturday (1403-01-04)
        // 2024-03-27 is Wednesday (1403-01-08)
        val wednesdayMillis = JalaliCalendarHelper.toEpochMillis(
            JalaliDate(1403, 1, 8, 12, 0, 0),
            testTimeZone
        )

        val (satStart, friEnd) = JalaliCalendarHelper.getWeekRange(wednesdayMillis, testTimeZone)

        val satDate = JalaliCalendarHelper.fromEpochMillis(satStart, testTimeZone)
        val friDate = JalaliCalendarHelper.fromEpochMillis(friEnd, testTimeZone)

        // Saturday should be 1403-01-04
        assertThat(satDate.day).isEqualTo(4)
        assertThat(satDate.hour).isEqualTo(0)

        // Friday should be 1403-01-10
        assertThat(friDate.day).isEqualTo(10)
        assertThat(friDate.hour).isEqualTo(23)
    }

    @Test
    fun getMonthRange_calculatesStartAndEndOfJalaliMonth() {
        val millis = JalaliCalendarHelper.toEpochMillis(
            JalaliDate(1403, 8, 15, 10, 0, 0),
            testTimeZone
        )

        val (monthStart, monthEnd) = JalaliCalendarHelper.getMonthRange(millis, testTimeZone)

        val startJalali = JalaliCalendarHelper.fromEpochMillis(monthStart, testTimeZone)
        val endJalali = JalaliCalendarHelper.fromEpochMillis(monthEnd, testTimeZone)

        assertThat(startJalali.month).isEqualTo(8)
        assertThat(startJalali.day).isEqualTo(1)

        assertThat(endJalali.month).isEqualTo(8)
        assertThat(endJalali.day).isEqualTo(30) // Aban has 30 days
    }

    @Test
    fun formatNumeric_formatsWithPersianAndEnglishDigits() {
        val date = JalaliDate(1405, 8, 26)
        val persianFormatted = JalaliCalendarHelper.formatNumeric(date, usePersianDigits = true)
        val englishFormatted = JalaliCalendarHelper.formatNumeric(date, usePersianDigits = false)

        assertThat(persianFormatted).isEqualTo("۱۴۰۵/۰۸/۲۶")
        assertThat(englishFormatted).isEqualTo("1405/08/26")
    }

    @Test
    fun formatText_formatsWithMonthName() {
        val date = JalaliDate(1405, 8, 26)
        val persianFormatted = JalaliCalendarHelper.formatText(date, usePersianDigits = true)
        val englishFormatted = JalaliCalendarHelper.formatText(date, usePersianDigits = false)

        assertThat(persianFormatted).isEqualTo("۲۶ آبان ۱۴۰۵")
        assertThat(englishFormatted).isEqualTo("26 Aban 1405".replace("Aban", "آبان"))
    }
}
