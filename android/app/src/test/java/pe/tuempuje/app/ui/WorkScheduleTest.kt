package pe.tuempuje.app.ui

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkScheduleTest {
    @Test fun includesDateAndPreservesDuration() {
        val date = dateAsUtcMillis(LocalDate.of(2026, 11, 15))
        assertEquals("15/11/2026 · 4 horas", workSchedule(date, " 4 horas "))
    }

    @Test fun allowsDateOrFlexibleDescription() {
        val date = dateAsUtcMillis(LocalDate.of(2026, 1, 2))
        assertEquals("02/01/2026", workSchedule(date, ""))
        assertEquals("Por coordinar", workSchedule(null, " Por coordinar "))
        assertEquals("Fecha por coordinar · 4 horas", workSchedule(null, "4 horas", flexible = true))
        assertEquals("Fecha por coordinar", workSchedule(null, "", flexible = true))
        assertEquals("", workSchedule(null, " "))
    }
}
