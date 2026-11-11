package pe.tuempuje.app.data

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

data class Opportunity(
    val id: String,
    val title: String,
    val organization: String,
    val category: String,
    val location: String,
    val pay: String,
    val duration: String,
    val description: String,
    val own: Boolean = false,
)

class OpportunityViewModel : ViewModel() {
    val opportunities = mutableStateListOf(
        Opportunity("1", "Apoyo en sesión de fotografía", "Estudio Lumi", "Audiovisual", "Cercado, Arequipa", "S/ 60 por jornada", "4 horas", "Apoya en la organización de luces, fondos y equipos durante una sesión de fotografía de productos."),
        Opportunity("2", "Edición de videos cortos", "Café de Barrio", "Creativo", "Remoto", "S/ 90 por proyecto", "3 videos", "Edita tres videos breves para redes sociales con material proporcionado por el negocio."),
        Opportunity("3", "Asistente para evento local", "Punto Cultural", "Eventos", "Yanahuara, Arequipa", "S/ 70 por jornada", "5 horas", "Apoya en el registro de asistentes y la preparación de materiales para un evento cultural."),
    )
    val applications = mutableStateListOf<String>()

    fun find(id: String): Opportunity? = opportunities.find { it.id == id }

    fun apply(id: String) {
        if (id !in applications && find(id)?.own == false) applications.add(id)
    }

    fun publish(title: String, description: String, location: String, pay: String): Boolean {
        if (listOf(title, description, location, pay).any { it.isBlank() }) return false
        opportunities.add(0, Opportunity(
            id = java.util.UUID.randomUUID().toString(),
            title = title.trim(),
            organization = "Tu publicación de prueba",
            category = "Oportunidad",
            location = location.trim(),
            pay = pay.trim(),
            duration = "Por definir",
            description = description.trim(),
            own = true,
        ))
        return true
    }
}
