package com.panosdim.annualleaves.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

fun LocalDate.toEpochMilli(): Long {
    return this.toEpochDay() * (1000 * 60 * 60 * 24)
}

fun Long.toLocalDate(): LocalDate {
    return LocalDate.ofEpochDay(this / (1000 * 60 * 60 * 24))
}

fun String.toLocalDate(): LocalDate {
    return try {
        LocalDate.parse(
            this,
            dateFormatter
        )
    } catch (ex: DateTimeParseException) {
        LocalDate.now()
    }
}

fun String.formatDate(): String {
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    val date = this.toLocalDate()
    return date.format(dateFormatter) ?: ""
}

fun isDateInPreviousYear(date: LocalDate): Boolean {
    val previousYear = LocalDate.now().year - 1
    return date.year == previousYear
}