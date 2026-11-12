package pe.tuempuje.app.ui.auth

import org.junit.Assert.assertEquals
import org.junit.Test
import pe.tuempuje.app.ui.components.EmpuMood

class AccountEmpuMoodTest {
    private fun mood(
        loading: Boolean = false,
        error: Boolean = false,
        email: Boolean = false,
        password: Boolean = false,
        visible: Boolean = false,
    ) = accountEmpuMood(loading, error, email, password, visible)

    @Test fun `el foco en una contraseña oculta prevalece incluso con error`() {
        assertEquals(EmpuMood.PASSWORD, mood(error = true, password = true))
    }

    @Test fun `mostrar la contraseña retira la expresión de ocultar`() {
        assertEquals(EmpuMood.IDLE, mood(password = true, visible = true))
    }

    @Test fun `el foco en el correo cambia la mirada`() {
        assertEquals(EmpuMood.LOOKING, mood(email = true))
    }

    @Test fun `la carga y el error usan estados distintos`() {
        assertEquals(EmpuMood.LOADING, mood(loading = true))
        assertEquals(EmpuMood.SAD, mood(error = true))
    }

    @Test fun `cada expresión conserva su identificador de Rive`() {
        assertEquals((0..8).map(Int::toFloat), EmpuMood.entries.map { it.riveValue })
    }
}
