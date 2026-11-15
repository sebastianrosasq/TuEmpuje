package pe.tuempuje.app.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AssistantErrorTest {
    @Test fun quotaErrorGivesActionableMessageWithoutEchoingServerText() {
        val result = assistantErrorMessage(IllegalStateException("429 quota texto privado"))
        assertTrue(result.contains("límite"))
        assertFalse(result.contains("texto privado"))
    }
    @Test fun appCheckErrorDoesNotRevealUnderlyingToken() {
        val result = assistantErrorMessage(IllegalStateException("App Check secreto de prueba"))
        assertTrue(result.contains("App Check"))
        assertFalse(result.contains("secreto"))
    }
}
