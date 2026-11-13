package pe.tuempuje.app.ui

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val dateFormat = DateTimeFormatter.ofPattern("dd/MM/uuuu")

internal fun dateAsUtcMillis(date: LocalDate): Long = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

internal fun displayWorkDate(dateMillis: Long): String =
    Instant.ofEpochMilli(dateMillis).atOffset(ZoneOffset.UTC).toLocalDate().format(dateFormat)

internal fun workSchedule(dateMillis: Long?, details: String, flexible: Boolean = false): String {
    val date = dateMillis?.let(::displayWorkDate) ?: if (flexible) "Fecha por coordinar" else null
    return listOfNotNull(date, details.trim().takeIf { it.isNotEmpty() }).joinToString(" · ")
}
