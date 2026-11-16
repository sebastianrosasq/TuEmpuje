package pe.tuempuje.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.tuempuje.app.ui.components.AppNotice

@Composable
fun TermsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TextButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null); Text("Volver") }
        Text("Términos y servicios", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        AppNotice("Información provisional del proyecto. Antes de abrir el servicio al público deben definirse el responsable, el canal de contacto y las condiciones definitivas")
        val sections = listOf(
            "Para qué sirve TuEmpuje" to "La aplicación facilita el encuentro entre personas que buscan oportunidades y quienes necesitan apoyo. Las condiciones de cada trabajo se acuerdan entre sus participantes",
            "Uso de la cuenta" to "La cuenta es personal. Mantén tus credenciales seguras y proporciona información veraz. TuEmpuje está dirigido a personas de 18 años o más",
            "Publicaciones y postulaciones" to "Quien publica debe describir las tareas y condiciones con claridad. Una postulación comparte el perfil laboral indicado y, si lo eliges, el CV adjunto. Revisa su contenido antes de enviarlo",
            "Seguridad y convivencia" to "No compartas contraseñas o códigos de acceso ni envíes dinero para postular. Reporta las publicaciones o conductas que consideres engañosas o inseguras",
            "Asistencia de Empu" to "Empu puede proponer textos que debes revisar. Si solicitas ayuda, el contenido indicado se envía a Google para generar sugerencias, según el aviso mostrado antes de cada solicitud",
            "Datos y disponibilidad" to "Los datos de cuenta, las postulaciones y los CV se usan para prestar las funciones solicitadas. El acceso a un CV se limita a las partes de la postulación. La eliminación completa de cuentas en la nube aún no está habilitada",
            "Alcance del servicio" to "TuEmpuje no verifica por sí solo la identidad de las partes ni garantiza la contratación o el pago. Las funciones pueden cambiar y las partes deben confirmar los detalles directamente",
        )
        sections.forEach { (title, body) ->
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Text("Referencias sobre protección de datos", style = MaterialTheme.typography.titleMedium)
        listOf(
            "Ley de Protección de Datos Personales" to "https://www.gob.pe/institucion/congreso-de-la-republica/normas-legales/243470",
            "Reglamento de protección de datos" to "https://www.gob.pe/institucion/anpd/normas-legales/6554453-16-2024-jus",
        ).forEach { (label, url) ->
            TextButton(onClick = { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } }) { Text(label) }
        }
    }
}
