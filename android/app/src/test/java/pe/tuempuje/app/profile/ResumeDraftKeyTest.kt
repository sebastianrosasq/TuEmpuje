package pe.tuempuje.app.profile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ResumeDraftKeyTest {
    @Test fun generalAndOfferDraftsRemainDistinctEvenWhenOfferIsNamedGeneral() {
        assertEquals("cuenta/general", resumeDraftKey("cuenta", null))
        assertEquals("cuenta/oferta/general", resumeDraftKey("cuenta", "general"))
        assertNotEquals(resumeDraftKey("cuenta", null), resumeDraftKey("cuenta", "general"))
    }
    @Test fun draftsAreScopedToTheSignedInAccount() {
        assertNotEquals(resumeDraftKey("primera", "oferta"), resumeDraftKey("segunda", "oferta"))
    }
}
