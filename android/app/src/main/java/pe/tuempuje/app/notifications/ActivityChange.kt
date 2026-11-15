package pe.tuempuje.app.notifications

internal fun hasRelevantActivity(
    hasBaseline: Boolean,
    previousStatus: String?,
    previousMessage: String?,
    status: String,
    message: String?,
    sender: String?,
    uid: String,
    employer: Boolean,
): Boolean {
    if (!hasBaseline) return false
    if (previousStatus == null) return (employer && status == "pending") || (message != null && sender != uid)
    if (previousStatus != status && ((employer && status == "withdrawn") || (!employer && status in setOf("reviewing", "selected", "rejected", "cancelled", "completed")))) return true
    return message != null && message != previousMessage && sender != uid
}
