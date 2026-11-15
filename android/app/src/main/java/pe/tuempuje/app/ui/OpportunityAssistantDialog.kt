package pe.tuempuje.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import pe.tuempuje.app.data.EmpuAssistant
import pe.tuempuje.app.data.FirebaseServices
import pe.tuempuje.app.data.OpportunityDraft
import pe.tuempuje.app.data.assistantErrorMessage
import pe.tuempuje.app.ui.components.AppLoadingState
import pe.tuempuje.app.ui.components.AppNotice
import pe.tuempuje.app.ui.components.Empu
import pe.tuempuje.app.ui.components.EmpuMood

@Composable
fun OpportunityAssistantDialog(services: FirebaseServices?, onDismiss: () -> Unit, onUse: (OpportunityDraft) -> Unit) {
    var input by rememberSaveable { mutableStateOf("") }
    var consent by rememberSaveable { mutableStateOf(false) }
    var adult by rememberSaveable { mutableStateOf(false) }
    var result by remember { mutableStateOf<OpportunityDraft?>(null) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        icon = { Empu(if (busy) EmpuMood.LOADING else EmpuMood.THINKING, Modifier.size(112.dp)) },
        title = { Text("Redactar con Empu") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Cuéntame qué necesitas. Prepararé un borrador para que lo revises")
                OutlinedTextField(value = input, onValueChange = { input = it.take(2000); result = null },
                    label = { Text("Describe la oportunidad") }, placeholder = { Text("Necesito ayuda para editar dos videos para mi tienda") },
                    minLines = 3, enabled = !busy, modifier = Modifier.fillMaxWidth())
                Text("No incluyas DNI, teléfonos ni datos privados. El texto se enviará a Gemini y Google puede usarlo para mejorar sus productos en el nivel gratuito", style = MaterialTheme.typography.bodySmall)
                if (services == null || services.isLocal) AppNotice("Empu necesita la conexión de desarrollo para usar Gemini. Puedes completar el formulario manualmente")
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = consent, onCheckedChange = { consent = it }, enabled = !busy)
                    Text("Acepto enviar esta descripción a Gemini", style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = adult, onCheckedChange = { adult = it }, enabled = !busy)
                    Text("Confirmo que tengo 18 años o más", style = MaterialTheme.typography.bodySmall)
                }
                if (busy) AppLoadingState("Preparando borrador")
                error?.let { AppNotice(it, isError = true) }
                result?.let { draft ->
                    Text("Borrador para revisar", style = MaterialTheme.typography.titleMedium)
                    if (draft.title.isNotBlank()) Text("Título: ${draft.title}")
                    if (draft.tasks.isNotBlank()) Text("Tareas: ${draft.tasks}")
                    if (draft.requirements.isNotBlank()) Text("Requisitos: ${draft.requirements}")
                    if (draft.missing.isNotBlank()) AppNotice(draft.missing)
                    Text("Revisa los datos: la IA puede equivocarse", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            if (result != null) TextButton(enabled = !busy, onClick = { result?.let(onUse) }) { Text("Usar borrador") }
            else TextButton(enabled = !busy && consent && adult && input.trim().length >= 10 && services != null && !services.isLocal, onClick = {
                busy = true; error = null
                scope.launch {
                    try { result = EmpuAssistant(requireNotNull(services)).draftOpportunity(input) }
                    catch (_: TimeoutCancellationException) { error = "La respuesta tardó demasiado. Puedes volver a intentar o completar el formulario" }
                    catch (e: CancellationException) { throw e }
                    catch (e: Exception) { error = assistantErrorMessage(e) }
                    finally { busy = false }
                }
            }) { Text("Preparar borrador") }
        },
        dismissButton = { TextButton(enabled = !busy, onClick = onDismiss) { Text("Cerrar") } },
    )
}
