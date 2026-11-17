package pe.tuempuje.app.ui.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import pe.tuempuje.app.ui.components.AppEmptyState
import pe.tuempuje.app.ui.components.AppLoadingState
import pe.tuempuje.app.ui.components.AppNotice

private data class MyReport(val title: String, val reason: String, val details: String, val status: String, val resolution: String, val createdAt: Long)

@Composable
internal fun MyReportsScreen(uid: String, firestore: FirebaseFirestore, onBack: () -> Unit) {
    var reports by remember(uid) { mutableStateOf<List<MyReport>>(emptyList()) }
    var loading by remember(uid) { mutableStateOf(true) }
    var error by remember(uid) { mutableStateOf(false) }
    var revision by remember(uid) { mutableIntStateOf(0) }
    BackHandler(onBack = onBack)
    DisposableEffect(uid, revision) {
        loading = true
        error = false
        val listener = firestore.collection("reports").whereEqualTo("reporterId", uid).limit(50)
            .addSnapshotListener { snapshot, failure ->
                loading = false
                error = failure != null
                if (failure == null && snapshot != null) reports = snapshot.documents.map {
                    MyReport(it.getString("opportunityTitle").orEmpty(), it.getString("reason").orEmpty(), it.getString("details").orEmpty(),
                        it.getString("status").orEmpty(), it.getString("resolution").orEmpty(), it.getTimestamp("createdAt")?.seconds ?: 0L)
                }.sortedByDescending { it.createdAt }
            }
        onDispose { listener.remove() }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TextButton(onClick = onBack) { Text("Volver") }
        Text("Mis reportes", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Consulta el estado de los reportes que enviaste", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        when {
            loading -> AppLoadingState("Cargando reportes")
            error -> {
                AppNotice("No se pudieron cargar tus reportes. Revisa tu conexión e inténtalo otra vez", isError = true)
                OutlinedButton(onClick = { revision++ }) { Text("Reintentar") }
            }
            reports.isEmpty() -> AppEmptyState("Aún no tienes reportes", "Si una oportunidad parece sospechosa, puedes reportarla desde su detalle")
            else -> reports.forEach { report ->
                Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(report.title.ifBlank { "Oportunidad reportada" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(report.reason, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(when {
                            report.status != "resolved" -> "Pendiente de revisión"
                            report.resolution == "removed" -> "Revisado · publicación retirada"
                            report.resolution == "kept" -> "Revisado · publicación mantenida"
                            else -> "Revisado"
                        }, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                        Text(report.details, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
