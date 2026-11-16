package pe.tuempuje.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import java.time.LocalDate

internal data class AssistantOpportunity(
    val draft: OpportunityDraft,
    val category: String,
    val mode: String,
    val location: String,
    val amount: String,
    val payUnit: String,
    val noExperience: Boolean,
    val dateChoice: String,
    val includeBusiness: Boolean,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun OpportunityAssistantDialog(
    services: FirebaseServices?, businessName: String, profileDistrict: String,
    onDismiss: () -> Unit, onUse: (AssistantOpportunity) -> Unit,
) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var input by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("Otro") }
    var mode by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf(profileDistrict) }
    var amount by rememberSaveable { mutableStateOf("") }
    var payUnit by rememberSaveable { mutableStateOf("Por tarea") }
    var noExperience by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var dateChoice by rememberSaveable { mutableStateOf("Por coordinar") }
    val includeBusiness = businessName.isNotBlank()
    var noAdditionalRequirements by rememberSaveable { mutableStateOf(false) }
    var consent by rememberSaveable { mutableStateOf(false) }
    var adult by rememberSaveable { mutableStateOf(false) }
    var title by rememberSaveable { mutableStateOf("") }
    var tasks by rememberSaveable { mutableStateOf("") }
    var requirements by rememberSaveable { mutableStateOf("") }
    var missing by rememberSaveable { mutableStateOf("") }
    var editingPreview by rememberSaveable { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showValidation by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    LaunchedEffect(step) { scrollState.scrollTo(0) }
    val workLocation = if (mode == "Remoto") "Remoto" else location.trim()
    val firstValid = input.trim().length >= 10 && noExperience != null
    val conditionsValid = assistantConditionsValid(mode, location, amount, payUnit, consent, adult)
    val previewValid = assistantReviewValid(title, tasks, requirements)
    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        icon = { Empu(if (busy) EmpuMood.LOADING else if (step == 2) EmpuMood.SUCCESS else EmpuMood.THINKING, Modifier.size(96.dp)) },
        title = { Text(when (step) { 0 -> "Creemos tu oportunidad"; 1 -> "Solo faltan las condiciones"; else -> "Así se verá tu publicación" }) },
        text = {
            Column(Modifier.verticalScroll(scrollState), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Paso ${step + 1} de 3", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                LinearProgressIndicator(progress = { (step + 1) / 3f }, modifier = Modifier.fillMaxWidth())
                error?.let { AppNotice(it, isError = true) }
                when (step) {
                    0 -> {
                        Text("Cuéntame qué necesitas en pocas palabras. Prepararé una publicación para que la revises")
                        if (businessName.isNotBlank()) {
                            Column { Text("Publicar como $businessName", fontWeight = FontWeight.SemiBold); Text("Datos guardados en tu perfil de empresa", style = MaterialTheme.typography.bodySmall) }
                        }
                        OutlinedTextField(value = input, onValueChange = { input = it.take(2000) }, label = { Text("¿Qué necesitas?") }, placeholder = { Text("Editar dos videos para mi tienda") }, minLines = 2, enabled = !busy, modifier = Modifier.fillMaxWidth(), isError = showValidation && input.trim().length < 10, supportingText = if (showValidation && input.trim().length < 10) { { Text("Escribe al menos 10 caracteres") } } else null)
                        Text("Tipo de trabajo", fontWeight = FontWeight.SemiBold)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { workInterests.forEach { option -> FilterChip(selected = category == option, onClick = { category = option }, label = { Text(option) }) } }
                        Text("¿Se necesita experiencia?", fontWeight = FontWeight.SemiBold)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(selected = noExperience == true, onClick = { noExperience = true }, label = { Text("No es necesaria") })
                            FilterChip(selected = noExperience == false, onClick = { noExperience = false }, label = { Text("Sí es necesaria") })
                        }
                        if (showValidation && noExperience == null) Text("Elige una opción", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        Row(Modifier.fillMaxWidth().toggleable(value = noAdditionalRequirements, role = Role.Checkbox, onValueChange = { noAdditionalRequirements = it }), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = noAdditionalRequirements, onCheckedChange = null)
                            Text("Sin otros requisitos específicos", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    1 -> {
                        Text("Marca las opciones y escribe el monto que ofrecerás")
                        Text("Modalidad", fontWeight = FontWeight.SemiBold)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("Presencial", "Híbrido", "Remoto").forEach { option -> FilterChip(selected = mode == option, onClick = { mode = option }, label = { Text(option) }) } }
                        if (showValidation && mode.isBlank()) Text("Elige la modalidad", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        if (mode != "Remoto") OutlinedTextField(value = location, onValueChange = { location = it.take(120) }, label = { Text("Distrito o lugar") }, supportingText = { Text(if (profileDistrict.isNotBlank() && location == profileDistrict) "Del perfil, puedes cambiarlo" else "Indica dónde se realizará") }, isError = showValidation && workLocation.length !in 2..120, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(value = amount, onValueChange = { amount = it.filter { char -> char.isDigit() || char == '.' || char == ',' }.take(9) }, label = { Text("Pago ofrecido") }, prefix = { Text("S/ ") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), isError = showValidation && paymentText(amount, payUnit) == null, supportingText = if (showValidation && paymentText(amount, payUnit) == null) { { Text("Ingresa un monto desde S/ 1") } } else null, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("Por hora", "Por día", "Por tarea", "Por mes").forEach { option -> FilterChip(selected = payUnit == option, onClick = { payUnit = option }, label = { Text(option) }) } }
                        Text("¿Cuándo?", fontWeight = FontWeight.SemiBold)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("Por coordinar", "Hoy", "Mañana").forEach { option -> FilterChip(selected = dateChoice == option, onClick = { dateChoice = option }, label = { Text(option) }) } }
                        Text("Para crear la propuesta se envían tu descripción y la experiencia requerida a Google. Evita datos personales. En el nivel gratuito, Google puede usar este contenido para mejorar sus productos", style = MaterialTheme.typography.bodySmall)
                        Row(Modifier.fillMaxWidth().toggleable(value = consent, role = Role.Checkbox, enabled = !busy, onValueChange = { consent = it }), verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = consent, onCheckedChange = null); Text("Acepto enviar estos datos", style = MaterialTheme.typography.bodySmall) }
                        Row(Modifier.fillMaxWidth().toggleable(value = adult, role = Role.Checkbox, enabled = !busy, onValueChange = { adult = it }), verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = adult, onCheckedChange = null); Text("Confirmo que tengo 18 años o más", style = MaterialTheme.typography.bodySmall) }
                        if (services == null || services.isLocal) AppNotice("Empu necesita conexión al servicio de IA. Puedes publicar con el formulario manual")
                        if (showValidation && (!consent || !adult)) Text("Marca las dos casillas para continuar", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        if (busy) AppLoadingState("Empu está preparando tu publicación")
                    }
                    else -> {
                        Text("Revisa y edita la propuesta antes de publicarla")
                        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceVariant) {
                            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text(category.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                Text(title.ifBlank { "Título pendiente" }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                if (includeBusiness && businessName.isNotBlank()) Text(businessName, style = MaterialTheme.typography.labelLarge)
                                Text("$mode · $workLocation")
                                Text(paymentText(amount, payUnit).orEmpty(), fontWeight = FontWeight.SemiBold)
                                Text("$dateChoice · 1 vacante")
                                Text(if (noExperience == true) "No requiere experiencia previa" else "Requiere experiencia previa")
                                Spacer(Modifier.height(8.dp))
                                Text("Tareas", fontWeight = FontWeight.Bold)
                                Text(tasks.ifBlank { "Descripción pendiente" })
                                Spacer(Modifier.height(6.dp))
                                Text("Conocimientos o requisitos", fontWeight = FontWeight.Bold)
                                Text(requirements.ifBlank { "Requisitos pendientes" })
                            }
                        }
                        OutlinedButton(onClick = { editingPreview = !editingPreview }, modifier = Modifier.fillMaxWidth()) { Text(if (editingPreview) "Listo" else "Editar el texto") }
                        if (editingPreview) {
                            OutlinedTextField(value = title, onValueChange = { title = it.take(120) }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth(), isError = showValidation && title.trim().length !in 3..120)
                            OutlinedTextField(value = tasks, onValueChange = { tasks = it.take(3000) }, label = { Text("Descripción y tareas") }, minLines = 3, modifier = Modifier.fillMaxWidth(), isError = showValidation && tasks.trim().length !in 10..3000)
                            OutlinedTextField(value = requirements, onValueChange = { requirements = it.take(1500) }, label = { Text("Conocimientos o requisitos") }, minLines = 2, modifier = Modifier.fillMaxWidth(), isError = showValidation && requirements.trim().length !in 2..1500)
                        }
                        if (missing.isNotBlank()) AppNotice(missing)
                        if (showValidation && !previewValid) AppNotice("Completa el título, las tareas y los requisitos para continuar", isError = true)
                        Text("Una persona revisa la publicación antes de hacerla visible", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            when (step) {
                0 -> TextButton(enabled = !busy, onClick = { showValidation = true; if (firstValid) { step = 1; showValidation = false } }) { Text("Continuar") }
                1 -> TextButton(enabled = !busy && services != null && !services.isLocal, onClick = {
                    showValidation = true
                    if (mode.isBlank()) scope.launch { scrollState.animateScrollTo(0) }
                    if (conditionsValid) {
                        busy = true; error = null; showValidation = false
                        scope.launch {
                            try {
                                val draft = EmpuAssistant(requireNotNull(services)).draftOpportunity(input, noExperience)
                                title = draft.title; tasks = draft.tasks
                                requirements = if (draft.requirements.isBlank() && noAdditionalRequirements) "Sin requisitos adicionales" else draft.requirements
                                missing = if (noAdditionalRequirements && draft.requirements.isBlank()) "" else draft.missing
                                step = 2
                            } catch (_: TimeoutCancellationException) { error = "La respuesta tardó demasiado. Inténtalo otra vez o usa el formulario manual" }
                            catch (e: CancellationException) { throw e }
                            catch (e: Exception) { error = assistantErrorMessage(e) }
                            finally { busy = false }
                        }
                    }
                }) { Text("Crear vista previa") }
                else -> TextButton(enabled = !busy, onClick = { showValidation = true; if (previewValid) onUse(AssistantOpportunity(OpportunityDraft(title, tasks, requirements, missing), category, mode, workLocation, amount, payUnit, noExperience == true, dateChoice, includeBusiness)) else editingPreview = true }) { Text("Revisar para publicar") }
            }
        },
        dismissButton = { TextButton(enabled = !busy, onClick = { if (step > 0) { step--; showValidation = false; error = null } else onDismiss() }) { Text(if (step == 0) "Cerrar" else "Atrás") } },
    )
}
