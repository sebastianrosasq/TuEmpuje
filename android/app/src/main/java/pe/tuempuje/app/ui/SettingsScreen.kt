package pe.tuempuje.app.ui

import android.content.Intent
import android.provider.Settings
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import pe.tuempuje.app.ui.components.Empu
import pe.tuempuje.app.ui.components.EmpuMood
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.OutlinedButton

@Composable
fun SettingsScreen(preferences: AppPreferences, onTerms: () -> Unit = {}, onBack: () -> Unit) {
    val context = LocalContext.current
    var permissionDenied by remember { mutableStateOf(false) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionDenied = !granted
        preferences.updateBackgroundNotifications(granted)
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TextButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
            Text("Volver")
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Personaliza tu espacio", modifier = Modifier.weight(1f), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            if (preferences.showMascot) Empu(EmpuMood.HAPPY, Modifier.size(104.dp))
        }
        Text("Elige cómo quieres usar TuEmpuje en este dispositivo", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(vertical = 10.dp)) {
                Text("Estilo visual", modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp), style = MaterialTheme.typography.titleLarge)
                VisualStyle.entries.forEach { style ->
                    val sample = when (style) {
                        VisualStyle.ORIGINAL -> Color(0xFF187F76)
                        VisualStyle.PRISMA -> Color(0xFFA893FF)
                        VisualStyle.PULSO -> Color(0xFF50E3B1)
                        VisualStyle.PETROLEO -> Color(0xFFD28463)
                        VisualStyle.TINTA -> Color(0xFF527CF6)
                        VisualStyle.HORIZONTE -> Color(0xFFFF806B)
                    }
                    Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).selectable(selected = preferences.visualStyle == style, role = Role.RadioButton, onClick = { preferences.updateVisualStyle(style) }).padding(horizontal = 18.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Spacer(Modifier.size(26.dp).background(sample, CircleShape))
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(style.label, style = MaterialTheme.typography.titleMedium)
                            Text(style.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        RadioButton(selected = preferences.visualStyle == style, onClick = null)
                    }
                }
                Text("Los cinco estilos nuevos usan fondos oscuros. El estilo original admite modo claro, oscuro o del sistema", modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (preferences.visualStyle == VisualStyle.ORIGINAL) {
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(vertical = 10.dp)) {
                Text("Apariencia", modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp), style = MaterialTheme.typography.titleLarge)
                listOf(
                    DisplayMode.SYSTEM to "Igual que el dispositivo",
                    DisplayMode.LIGHT to "Claro",
                    DisplayMode.DARK to "Oscuro",
                ).forEach { (mode, label) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                            .selectable(selected = preferences.displayMode == mode, role = Role.RadioButton, onClick = { preferences.updateDisplayMode(mode) })
                            .padding(horizontal = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        RadioButton(selected = preferences.displayMode == mode, onClick = null)
                    }
                }
            }
        }
        }
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Accesibilidad", style = MaterialTheme.typography.titleLarge)
                Text("El tamaño del texto sigue la configuración de tu teléfono", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(onClick = { runCatching { context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS)) } }, modifier = Modifier.fillMaxWidth()) { Text("Ajustar pantalla del dispositivo") }
            }
        }
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Privacidad", style = MaterialTheme.typography.titleLarge)
                Text("Tu documento, celular y correo de la cuenta son privados. Antes de postular revisa tu perfil y el contenido de cualquier CV que adjuntes", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(onClick = onTerms, modifier = Modifier.fillMaxWidth()) { Text("Términos y servicios") }
            }
        }
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Notificaciones", style = MaterialTheme.typography.titleLarge)
                PreferenceSwitch("Avisos de actividad", "Revisar mensajes y cambios de postulaciones aproximadamente cada hora", preferences.backgroundNotifications) { enabled ->
                    if (!enabled) { permissionDenied = false; preferences.updateBackgroundNotifications(false) }
                    else if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    else { permissionDenied = false; preferences.updateBackgroundNotifications(true) }
                }
                Text("Las revisiones requieren conexión y pueden demorarse por el ahorro de batería. La primera revisión establece una referencia", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (permissionDenied || (preferences.backgroundNotifications && !androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled())) {
                    Text("Activa las notificaciones en los ajustes del teléfono para recibir avisos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = { runCatching { context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)) } }) { Text("Abrir ajustes de notificaciones") }
                }
            }
        }
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Experiencia", style = MaterialTheme.typography.titleLarge)
                PreferenceSwitch("Mostrar a Empu", "Mostrar la mascota en la interfaz", preferences.showMascot, preferences::updateShowMascot)
                HorizontalDivider()
                PreferenceSwitch("Reducir movimiento", "Simplificar el movimiento decorativo y las transiciones", preferences.reduceMotion, preferences::updateReduceMotion)
            }
        }
        Text("Estos ajustes se guardan en el dispositivo, tus guardados se separan por cuenta", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PreferenceSwitch(title: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp).toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}
