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
    val requirements: String = "Revisa las tareas y consulta cualquier duda antes de postular",
    val vacancies: Int = 1,
    val own: Boolean = false,
)

class OpportunityViewModel : ViewModel() {
    val opportunities = mutableStateListOf(
        Opportunity("1", "Apoyo en sesión de fotografía", "Estudio Lumi", "Audiovisual", "Cercado, Arequipa", "S/ 60 por jornada", "4 horas", "Apoya en la organización de luces, fondos y equipos durante una sesión de fotografía de productos.", "Interés en fotografía y disposición para seguir indicaciones"),
        Opportunity("2", "Edición de videos cortos", "Café de Barrio", "Creativo", "Remoto", "S/ 90 por proyecto", "3 videos", "Edita tres videos breves para redes sociales con material proporcionado por el negocio.", "Manejo básico de una herramienta de edición de video"),
        Opportunity("3", "Asistente para evento local", "Punto Cultural", "Eventos", "Yanahuara, Arequipa", "S/ 70 por jornada", "5 horas", "Apoya en el registro de asistentes y la preparación de materiales para un evento cultural.", "Comunicación clara y disponibilidad durante el evento", vacancies = 2),
    )
    val applications = mutableStateListOf<String>()

    fun find(id: String): Opportunity? = opportunities.find { it.id == id }

    fun apply(id: String) {
        if (id !in applications && find(id)?.own == false) applications.add(id)
    }

    fun publish(title: String, description: String, location: String, pay: String, duration: String, requirements: String, vacancies: Int, category: String): Boolean {
        if (listOf(title, description, location, pay, duration, requirements, category).any { it.isBlank() } || vacancies !in 1..99) return false
        opportunities.add(0, Opportunity(
            id = java.util.UUID.randomUUID().toString(),
            title = title.trim(),
            organization = "Tu publicación de prueba",
            category = category,
            location = location.trim(),
            pay = pay.trim(),
            duration = duration.trim(),
            description = description.trim(),
            requirements = requirements.trim(),
            vacancies = vacancies,
            own = true,
        ))
        return true
    }
}
