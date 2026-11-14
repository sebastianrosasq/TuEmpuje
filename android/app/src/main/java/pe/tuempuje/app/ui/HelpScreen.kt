package pe.tuempuje.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.tuempuje.app.ui.components.Empu
import pe.tuempuje.app.ui.components.EmpuMood

@Composable
fun HelpScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TextButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null); Text("Volver") }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Ayuda y seguridad", modifier = Modifier.weight(1f), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Empu(EmpuMood.HAPPY, Modifier.size(80.dp))
        }
        HelpTopic("Buscar y postular", "Elige una oportunidad, revisa sus condiciones y completa tus habilidades y disponibilidad antes de enviar tu postulación")
        HelpTopic("Publicar una oportunidad", "Selecciona la opción de ofrecer trabajo en tu perfil, describe las tareas, condiciones, pago y vacantes antes de publicar")
        HelpTopic("Tus datos", "Al postular puedes revisar qué información del perfil se comparte. Tu documento, celular, nacionalidad y correo permanecen en tu perfil privado")
        HelpTopic("Coordina con cuidado", "Revisa el lugar, las tareas y el pago antes de aceptar, no compartas contraseñas ni códigos de acceso y desconfía de cobros para postular")
        HelpTopic("Reportar una oportunidad", "Desde el detalle de la publicación puedes enviar un reporte. El registro de reportes está disponible y su revisión aún se encuentra en preparación")
        HelpTopic("Acerca de TuEmpuje", "Una plataforma para conectar primeras experiencias, personas y oportunidades de trabajo")
    }
}

@Composable
private fun HelpTopic(title: String, body: String) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
