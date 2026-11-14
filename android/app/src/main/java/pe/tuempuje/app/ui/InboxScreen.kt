package pe.tuempuje.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.tuempuje.app.data.FirebaseServices
import pe.tuempuje.app.data.OpportunityViewModel
import pe.tuempuje.app.ui.components.AppEmptyState
import pe.tuempuje.app.ui.components.AppLoadingState
import pe.tuempuje.app.ui.components.AppNotice
import pe.tuempuje.app.ui.components.Empu
import pe.tuempuje.app.ui.components.EmpuMood

private data class InboxEntry(val jobId: String, val applicantId: String, val title: String, val person: String, val readOnly: Boolean)

@Composable
fun InboxScreen(services: FirebaseServices?, model: OpportunityViewModel, onOpen: (String, String) -> Unit) {
    val uid = model.userId
    val jobs = model.opportunities.toList()
    val applications = model.applications.toMap()
    val ownJobs = jobs.filter { it.own }
    val others = jobs.filter { !it.own && it.id in applications }
    val entries = remember(uid) { mutableStateMapOf<String, List<InboxEntry>>() }
    val pending = remember(uid) { mutableStateMapOf<String, Boolean>() }
    var error by remember(uid) { mutableStateOf<String?>(null) }
    var retryGeneration by remember(uid) { mutableIntStateOf(0) }
    DisposableEffect(uid, ownJobs.map { Triple(it.id, it.title, it.status) }, retryGeneration) {
        entries.clear(); pending.clear(); error = null
        val listeners = if (uid == null || services == null) emptyList() else ownJobs.map { job ->
            pending[job.id] = true
            services.firestore.collection("opportunities").document(job.id).collection("applications").addSnapshotListener { snapshot, exception ->
                pending[job.id] = false
                if (exception != null) error = "No se pudieron cargar todas las conversaciones, revisa tu conexión"
                else entries[job.id] = snapshot?.documents.orEmpty().filterNot { it.metadata.hasPendingWrites() }.map { doc ->
                    InboxEntry(job.id, doc.id, job.title, doc.getString("applicantName").orEmpty().ifBlank { "Postulante" }, job.status != "published" || doc.getString("status") == "withdrawn")
                }
            }
        }
        onDispose { listeners.forEach { it.remove() } }
    }
    val workerEntries = if (uid == null) emptyList() else others.map { job -> InboxEntry(job.id, uid, job.title, job.organization, job.status != "published" || applications[job.id] == "withdrawn") }
    val allEntries = (workerEntries + entries.values.flatten()).sortedWith(compareBy({ it.title }, { it.person }))
    val modelError = model.applicationsError ?: model.ownError ?: model.feedError
    LazyColumn(contentPadding = PaddingValues(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Mensajes", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Conversaciones de tus oportunidades", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Empu(EmpuMood.HAPPY, Modifier.size(82.dp))
            }
        }
        item { Text("Puedes conversar con las personas vinculadas a una postulación", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        error?.let { message -> item { AppNotice(message, isError = true); TextButton(onClick = { retryGeneration++ }) { Text("Reintentar conversaciones") } } }
        modelError?.let { message -> item { AppNotice(message, isError = true); TextButton(onClick = model::retry) { Text("Reintentar") } } }
        if (model.loading || model.applicationsLoading || pending.values.any { it }) item { AppLoadingState("Cargando conversaciones") }
        if (!model.loading && !model.applicationsLoading && pending.values.none { it } && allEntries.isEmpty() && error == null && modelError == null) {
            item { AppEmptyState("Aún no tienes conversaciones", "Cuando haya una postulación podrás coordinar los detalles aquí") }
        }
        items(allEntries, key = { "${it.jobId}/${it.applicantId}" }) { entry ->
            Surface(modifier = Modifier.fillMaxWidth().clickable(role = Role.Button) { onOpen(entry.jobId, entry.applicantId) }, shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Rounded.ChatBubbleOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text(entry.person, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(entry.title, style = MaterialTheme.typography.bodyMedium)
                        if (entry.readOnly) Text("Solo historial", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}
