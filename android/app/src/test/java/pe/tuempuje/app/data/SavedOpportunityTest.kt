package pe.tuempuje.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SavedOpportunityTest {
    private val opportunity = Opportunity(
        id = "oportunidad", ownerId = "autor", title = "Apoyo en edición",
        organization = "Taller", category = "Creativo", location = "Arequipa",
        pay = "S/ 60", duration = "Un día", description = "Editar un video",
        requirements = "Edición básica", vacancies = 2, selectedCount = 1, own = false,
    )

    @Test fun legacyEntryHasNoInventedDetails() {
        assertNull(SavedOpportunity(account = "persona", id = "anterior").asOpportunity())
    }

    @Test fun snapshotPreservesDetailsAndOwnerForAccount() {
        val restored = SavedOpportunity.from("autor", opportunity).asOpportunity()!!
        assertEquals(opportunity.copy(own = true), restored)
        assertTrue(restored.own)
        assertFalse(SavedOpportunity.from("otra", opportunity).asOpportunity()!!.own)
    }
}
