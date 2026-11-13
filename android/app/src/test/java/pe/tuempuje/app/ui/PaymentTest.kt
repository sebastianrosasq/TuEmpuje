package pe.tuempuje.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PaymentTest {
    @Test fun acceptsOneSolAndDecimalAmounts() {
        assertEquals("S/ 1.00 · Por tarea", paymentText("1", "Por tarea"))
        assertEquals("S/ 12.50 · Por hora", paymentText("12,50", "Por hora"))
    }

    @Test fun rejectsInvalidAmountsAndUnits() {
        listOf("", "0", "0.99", "-1", "1.234", "1000000").forEach { assertNull(paymentText(it, "Por día")) }
        assertNull(paymentText("10", "Otro"))
    }
}
