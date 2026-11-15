package pe.tuempuje.app.ui

internal fun assistantConditionsValid(mode: String, location: String, amount: String, payUnit: String, consent: Boolean, adult: Boolean): Boolean =
    mode in setOf("Presencial", "Híbrido", "Remoto") &&
        (mode == "Remoto" || location.trim().length in 2..120) &&
        paymentText(amount, payUnit) != null && consent && adult

internal fun assistantReviewValid(title: String, tasks: String, requirements: String): Boolean =
    title.trim().length in 3..120 && tasks.trim().length in 10..3000 && requirements.trim().length in 2..1500
