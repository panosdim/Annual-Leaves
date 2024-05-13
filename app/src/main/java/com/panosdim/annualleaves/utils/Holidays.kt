package com.panosdim.annualleaves.utils

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month

fun getOrthodoxEaster(year: Int): LocalDate {
    val a = year % 4
    val b = year % 7
    val c = year % 19
    val d = (19 * c + 15) % 30
    val e = (2 * a + 4 * b - d + 34) % 7
    val month = (d + e + 114) / 31
    val day = ((d + e + 114) % 31) + 1
    // Orthodox Easter is calculated based on the Julian calendar, then converted to Gregorian
    val easterJulian = LocalDate.of(year, month, day)
    return easterJulian.plusDays(13) // Add 13 days for Julian to Gregorian conversion
}

fun getHolidays(year: Int): List<LocalDate> {
    val holidays = mutableListOf<LocalDate>()
    holidays.add(LocalDate.of(year, Month.JANUARY, 1)) // New Year's Day
    holidays.add(LocalDate.of(year, Month.JANUARY, 6)) // Christmas Day (January 6th)

    val easter = getOrthodoxEaster(year)
    holidays.add(easter.minusDays(48))             // Clean Monday (48 days before Easter)
    holidays.add(LocalDate.of(year, Month.MARCH, 25)) // Annunciation (March 25th)
    holidays.add(easter.minusDays(2))              // Good Friday
    holidays.add(easter.plusDays(1))                  // Easter Monday

    // Check if 1st May is moved
    val greatWeekStart = easter.minusDays(7)
    val greatWeekEnd = easter.plusDays(1)
    val firstMay = LocalDate.of(year, Month.MAY, 1)
    if (firstMay.dayOfWeek == DayOfWeek.SATURDAY || firstMay.dayOfWeek == DayOfWeek.SUNDAY) {
        holidays.add(firstMay.plusDays(DayOfWeek.MONDAY.value - firstMay.dayOfWeek.value.toLong()))
    } else if (firstMay.isAfter(greatWeekStart) && firstMay.isBefore(greatWeekEnd)) {
        holidays.add(easter.plusDays(2))
    } else {
        holidays.add(firstMay)
    }

    holidays.add(easter.plusDays(50))                     // Pentecost
    holidays.add(LocalDate.of(year, Month.AUGUST, 15))   // Dominion of the Theotokos (August 15th)
    holidays.add(LocalDate.of(year, Month.OCTOBER, 28))  // Saint Demetrius' Day (October 26th)
    holidays.add(LocalDate.of(year, Month.DECEMBER, 25)) // Christmas Day (December 25th)
    holidays.add(LocalDate.of(year, Month.DECEMBER, 26)) // Boxing Day (December 26th)

    return holidays
}

fun calculateWorkingDays(start: LocalDate?, end: LocalDate?): Int {
    if (start == null || end == null) return 0

    val holidays = mutableListOf<LocalDate>()
    if (start.year == end.year) {
        holidays.addAll(getHolidays(start.year))
    } else {
        holidays.addAll(getHolidays(start.year))
        holidays.addAll(getHolidays(end.year))
    }

    var workingDays = 0
    var currentDate: LocalDate = start
    while (!currentDate.isAfter(end)) {
        if (!listOf(
                DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY
            ).contains(currentDate.dayOfWeek) && !holidays.contains(currentDate)
        ) {
            workingDays++
        }
        currentDate = currentDate.plusDays(1)
    }

    return workingDays
}