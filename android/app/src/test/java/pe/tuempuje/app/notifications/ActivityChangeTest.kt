package pe.tuempuje.app.notifications

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivityChangeTest {
    @Test fun firstCheckCreatesBaselineWithoutNotifying() {
        assertFalse(hasRelevantActivity(false, null, null, "pending", "m1", "other", "me", true))
    }
    @Test fun newApplicationNotifiesEmployerOnly() {
        assertTrue(hasRelevantActivity(true, null, null, "pending", null, null, "me", true))
        assertFalse(hasRelevantActivity(true, null, null, "pending", null, null, "me", false))
    }
    @Test fun selectedCandidateAndWithdrawnApplicationNotifyOtherSide() {
        assertTrue(hasRelevantActivity(true, "pending", "", "selected", null, null, "me", false))
        assertTrue(hasRelevantActivity(true, "pending", "", "withdrawn", null, null, "me", true))
        assertFalse(hasRelevantActivity(true, "pending", "", "withdrawn", null, null, "me", false))
    }
    @Test fun newMessageNotifiesRecipientButNotSender() {
        assertTrue(hasRelevantActivity(true, "pending", "old", "pending", "new", "other", "me", false))
        assertFalse(hasRelevantActivity(true, "pending", "old", "pending", "new", "me", "me", false))
        assertFalse(hasRelevantActivity(true, "pending", "new", "pending", "new", "other", "me", false))
        assertTrue(hasRelevantActivity(true, null, null, "pending", "new", "other", "me", false))
        assertFalse(hasRelevantActivity(true, null, null, "pending", "new", "me", "me", false))
    }
}
