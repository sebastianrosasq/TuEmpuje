package pe.tuempuje.app.auth

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class IdentityTest {
    @Test fun acceptsPeruvianDniAndMobileFormat() {
        assertNull(identityError("12345678", "987654321"))
    }

    @Test fun rejectsInvalidIdentityAndMobileFormats() {
        assertNotNull(identityError("1234567", "987654321"))
        assertNotNull(identityError("12345678", "187654321"))
        assertNotNull(identityError("12345678", "98765432"))
    }

    @Test fun acceptsOtherIdentityDocumentsAndRejectsInvalidFormats() {
        assertNull(identityError("12345678901", "987654321", "CE"))
        assertNull(identityError("AB1234567", "987654321", "PASAPORTE"))
        assertNotNull(identityError("ABC123", "987654321", "CE"))
        assertNotNull(identityError("AB12345678901234", "987654321", "PASAPORTE"))
        assertNotNull(identityError("12345678", "987654321", "OTRO"))
    }
}
