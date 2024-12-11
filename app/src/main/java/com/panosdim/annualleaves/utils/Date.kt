package com.panosdim.annualleaves.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
val displayDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
val showDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

fun LocalDate.toEpochMilli(): Long {
    return this.toEpochDay() * (1000 * 60 * 60 * 24)
}

fun Long.fromEpochMilli(): LocalDate {
    return LocalDate.ofEpochDay(this / (1000 * 60 * 60 * 24))
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
    val date = this.toLocalDate()
    return date.format(displayDateFormatter) ?: ""
}

fun LocalDate.toFormattedString(): String {
    return this.format(showDateFormatter)
}