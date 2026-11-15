package pe.tuempuje.app.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AssistantOpportunityRulesTest {
    @Test fun remoteOpportunityDoesNotRequireDistrict() {
        assertTrue(assistantConditionsValid("Remoto", "", "50", "Por tarea", true, true))
        assertFalse(assistantConditionsValid("Presencial", "", "50", "Por tarea", true, true))
    }

    @Test fun previewRequiresRealPaymentAndBothConfirmations() {
        assertFalse(assistantConditionsValid("Presencial", "Cayma", "0", "Por día", true, true))
        assertFalse(assistantConditionsValid("Presencial", "Cayma", "50", "Por día", false, true))
        assertFalse(assistantConditionsValid("Presencial", "Cayma", "50", "Por día", true, false))
        assertFalse(assistantConditionsValid("Desconocida", "Cayma", "50", "Por día", true, true))
        assertTrue(assistantConditionsValid("Presencial", "Cayma", "50,50", "Por día", true, true))
    }

    @Test fun incompleteAiResponseCannotAdvanceToPublication() {
        assertFalse(assistantReviewValid("", "Editar dos videos", "Edición básica"))
        assertFalse(assistantReviewValid("Editor de video", "", "Edición básica"))
        assertFalse(assistantReviewValid("Editor de video", "Editar dos videos", ""))
        assertTrue(assistantReviewValid("Editor de video", "Editar dos videos", "Sin requisitos adicionales"))
    }
}
