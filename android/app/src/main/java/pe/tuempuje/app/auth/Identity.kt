package pe.tuempuje.app.auth

internal fun identityError(dni: String, phone: String, documentType: String = "DNI"): String? = when {
    documentType == "DNI" && !dni.matches(Regex("[0-9]{8}")) -> "El DNI debe tener 8 dígitos"
    documentType == "CE" && !dni.matches(Regex("[0-9]{1,11}")) -> "Ingresa el carné de extranjería con hasta 11 dígitos"
    documentType == "PASAPORTE" && !dni.matches(Regex("[A-Z0-9]{1,15}")) -> "Ingresa el pasaporte con hasta 15 letras o dígitos"
    documentType !in setOf("DNI", "CE", "PASAPORTE") -> "Selecciona un tipo de documento"
    !phone.matches(Regex("9[0-9]{8}")) -> "Ingresa un celular peruano de 9 dígitos que empiece en 9"
    else -> null
}
