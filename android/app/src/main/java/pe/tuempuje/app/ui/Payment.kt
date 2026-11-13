package pe.tuempuje.app.ui

import java.math.BigDecimal
import java.math.RoundingMode

internal fun paymentText(amount: String, unit: String): String? {
    val normalized = amount.trim().replace(',', '.')
    if (!normalized.matches(Regex("[0-9]{1,6}(\\.[0-9]{1,2})?"))) return null
    val value = normalized.toBigDecimalOrNull() ?: return null
    if (value < BigDecimal.ONE || value > BigDecimal("999999.99")) return null
    if (unit !in listOf("Por hora", "Por día", "Por tarea", "Por mes")) return null
    return "S/ ${value.setScale(2, RoundingMode.UNNECESSARY).toPlainString()} · $unit"
}
