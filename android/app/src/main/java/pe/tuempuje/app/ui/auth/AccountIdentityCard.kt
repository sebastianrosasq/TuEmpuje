package pe.tuempuje.app.ui.auth

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pe.tuempuje.app.auth.AccountUiState
import pe.tuempuje.app.profile.ReferenceResumeTemplates

/** Apariencia privada de este dispositivo, separada por cuenta. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AccountIdentityCard(state: AccountUiState, isEmployer: Boolean) {
    val context = LocalContext.current
    val uid = state.uid.orEmpty()
    val prefs = remember(context) { context.getSharedPreferences("perfil_visual", android.content.Context.MODE_PRIVATE) }
    var photo by remember(uid) { mutableStateOf(prefs.getString("photo/$uid", "").orEmpty()) }
    var color by remember(uid) { mutableIntStateOf(prefs.getInt("color/$uid", 0).coerceIn(0, 2)) }
    var expanded by rememberSaveable(uid) { mutableStateOf(false) }
    var error by remember(uid) { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            val valid = withContext(Dispatchers.IO) {
                val bitmap = ReferenceResumeTemplates.photo(context.contentResolver, uri)
                (bitmap != null).also { bitmap?.recycle() }
            }
            val retained = valid && runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }.isSuccess
            if (retained) { photo = uri.toString(); prefs.edit().putString("photo/$uid", photo).apply(); error = null }
            else error = "No se pudo conservar esta imagen. Elige otra foto"
        }
    }
    val bitmap by produceState<android.graphics.Bitmap?>(initialValue = null, photo) {
        value = withContext(Dispatchers.IO) { ReferenceResumeTemplates.photo(context.contentResolver, photo.takeIf { it.isNotBlank() }?.let(Uri::parse)) }
    }
    val colors = MaterialTheme.colorScheme
    val background = when (color) { 1 -> colors.secondaryContainer; 2 -> colors.tertiaryContainer; else -> colors.surface.copy(alpha = .8f) }
    val foreground = when (color) { 1 -> colors.onSecondaryContainer; 2 -> colors.onTertiaryContainer; else -> colors.onSurface }
    Surface(shape = MaterialTheme.shapes.large, color = colors.primaryContainer, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.background(Brush.linearGradient(listOf(colors.primaryContainer, colors.secondaryContainer))).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(64.dp).background(background, CircleShape).clip(CircleShape), contentAlignment = Alignment.Center) {
                    val image = bitmap
                    if (image != null) Image(image.asImageBitmap(), contentDescription = "Foto de perfil", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    else Text(state.displayName.trim().firstOrNull()?.uppercase() ?: "", style = MaterialTheme.typography.headlineSmall, color = foreground, fontWeight = FontWeight.Bold)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(state.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.onPrimaryContainer)
                    Text(state.email, style = MaterialTheme.typography.bodyMedium, color = colors.onPrimaryContainer)
                    Text(if (isEmployer) "Cuenta de empresa" else "Cuenta personal", style = MaterialTheme.typography.labelMedium, color = colors.onPrimaryContainer)
                }
            }
            TextButton(onClick = { expanded = !expanded }) { Text(if (expanded) "Cerrar personalización" else "Personalizar mi imagen") }
            if (expanded) {
                Text("Tu foto y color se guardan solo en este dispositivo y no se comparten en las postulaciones", style = MaterialTheme.typography.bodySmall, color = colors.onPrimaryContainer)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { picker.launch(arrayOf("image/*")) }) { Text(if (photo.isBlank()) "Elegir foto" else "Cambiar foto") }
                    if (photo.isNotBlank()) TextButton(onClick = { photo = ""; prefs.edit().remove("photo/$uid").apply(); error = null }) { Text("Quitar") }
                }
                Text("Color del avatar", style = MaterialTheme.typography.labelLarge, color = colors.onPrimaryContainer)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Suave", "Secundario", "Acento").forEachIndexed { index, label ->
                        FilterChip(selected = color == index, onClick = { color = index; prefs.edit().putInt("color/$uid", index).apply() }, label = { Text(label) })
                    }
                }
                error?.let { Text(it, color = colors.error, style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}
