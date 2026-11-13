package pe.tuempuje.app.auth

internal fun identityError(dni: String, phone: String): String? = when {
    !dni.matches(Regex("[0-9]{8}")) -> "El DNI debe tener 8 dígitos"
    !phone.matches(Regex("9[0-9]{8}")) -> "Ingresa un celular peruano de 9 dígitos que empiece en 9"
    else -> null
}
