package pe.tuempuje.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VisualStyleTest {
    @Test fun `stored styles resolve and unrecognized values return to original`() {
        VisualStyle.entries.forEach { assertEquals(it, visualStyleFromStorage(it.name)) }
        assertEquals(VisualStyle.ORIGINAL, visualStyleFromStorage(null))
        assertEquals(VisualStyle.ORIGINAL, visualStyleFromStorage("desconocido"))
    }

    @Test fun `original follows display choice and other styles stay dark`() {
        assertFalse(darkAppearance(VisualStyle.ORIGINAL, DisplayMode.SYSTEM, false))
        assertTrue(darkAppearance(VisualStyle.ORIGINAL, DisplayMode.SYSTEM, true))
        assertFalse(darkAppearance(VisualStyle.ORIGINAL, DisplayMode.LIGHT, true))
        assertTrue(darkAppearance(VisualStyle.ORIGINAL, DisplayMode.DARK, false))
        VisualStyle.entries.filter { it != VisualStyle.ORIGINAL }.forEach { style ->
            DisplayMode.entries.forEach { display -> assertTrue(darkAppearance(style, display, false)) }
        }
    }
}
