package pe.tuempuje.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pe.tuempuje.app.data.Opportunity
import pe.tuempuje.app.data.OpportunityViewModel
import pe.tuempuje.app.ui.components.AppNotice
import pe.tuempuje.app.ui.components.AppPrimaryButton
import pe.tuempuje.app.ui.components.Empu
import pe.tuempuje.app.ui.components.EmpuMood

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditOpportunityScreen(opportunity: Opportunity?, model: OpportunityViewModel, onBack: () -> Unit) {
    if (opportunity == null || !opportunity.own || opportunity.status != "published") {
        Column(Modifier.padding(24.dp)) { AppNotice("Esta publicación ya no está disponible"); TextButton(onClick = onBack) { Text("Volver") } }
        return
    }
    var title by rememberSaveable(opportunity.id) { mutableStateOf(opportunity.title) }
    var description by rememberSaveable(opportunity.id) { mutableStateOf(opportunity.description) }
    var location by rememberSaveable(opportunity.id) { mutableStateOf(opportunity.location) }
    var schedule by rememberSaveable(opportunity.id) { mutableStateOf(opportunity.duration) }
    var requirements by rememberSaveable(opportunity.id) { mutableStateOf(opportunity.requirements) }
    var vacancies by rememberSaveable(opportunity.id) { mutableStateOf(opportunity.vacancies.toString()) }
    var category by rememberSaveable(opportunity.id) { mutableStateOf(opportunity.category) }
    var noExperience by rememberSaveable(opportunity.id) { mutableStateOf(opportunity.noExperienceRequired) }
    var workMode by rememberSaveable(opportunity.id) { mutableStateOf(opportunity.workMode) }
    var includeBusiness by rememberSaveable(opportunity.id) { mutableStateOf(opportunity.publicBusinessName.isNotBlank()) }
    val initialPay = remember(opportunity.id) { Regex("^S/\\s*([0-9]+(?:[.,][0-9]{1,2})?)\\s*·\\s*(Por hora|Por día|Por tarea|Por mes)$").matchEntire(opportunity.pay) }
    val initialAmount = initialPay?.groupValues?.get(1).orEmpty()
    val initialUnit = initialPay?.groupValues?.get(2) ?: "Por tarea"
    var amount by rememberSaveable(opportunity.id) { mutableStateOf(initialAmount) }
    var unit by rememberSaveable(opportunity.id) { mutableStateOf(initialUnit) }
    var expanded by remember { mutableStateOf(false) }
    var attempted by remember { mutableStateOf(false) }
    var confirm by remember { mutableStateOf(false) }
    var discard by remember { mutableStateOf(false) }
    val payment = paymentText(amount, unit)
    val valid = title.trim().length in 3..120 && description.trim().length in 10..3000 && location.trim().length in 2..120 &&
        requirements.trim().length in 2..1500 && schedule.trim().length in 2..120 && vacancies.toIntOrNull() in 1..99 && payment != null && (!includeBusiness || model.profileOrganization.trim().length >= 2)
    val changed = title != opportunity.title || description != opportunity.description || location != opportunity.location ||
        schedule != opportunity.duration || requirements != opportunity.requirements || vacancies != opportunity.vacancies.toString() ||
        category != opportunity.category || amount != initialAmount || unit != initialUnit || noExperience != opportunity.noExperienceRequired || workMode != opportunity.workMode || includeBusiness != opportunity.publicBusinessName.isNotBlank()
    val leave = { if (changed) discard = true else onBack() }
    BackHandler { leave() }
    if (discard) AlertDialog(onDismissRequest = { discard = false }, title = { Text("¿Descartar los cambios?") },
        text = { Text("Los cambios que no guardes se perderán") }, confirmButton = { TextButton(onClick = onBack) { Text("Descartar") } },
        dismissButton = { TextButton(onClick = { discard = false }) { Text("Seguir editando") } })
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, icon = { Empu(EmpuMood.THINKING, Modifier.size(100.dp)) },
        title = { Text("Guardar cambios") }, text = { Text("Las nuevas condiciones serán visibles para quienes consulten la oportunidad") },
        confirmButton = { TextButton(enabled = !model.busy, onClick = {
            model.editOpportunity(opportunity.id, title, description, location, payment.orEmpty(), schedule, requirements, vacancies.toIntOrNull() ?: 0, category, noExperience, workMode, includeBusiness) { confirm = false; onBack() }
        }) { Text("Guardar") } }, dismissButton = { TextButton(onClick = { confirm = false }) { Text("Revisar") } })
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TextButton(onClick = leave) { Text("Volver") }
        Text("Editar publicación", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Revisa las condiciones antes de guardar los cambios")
        model.error?.let { AppNotice(it, isError = true) }
        if (attempted && !valid) AppNotice("Revisa los campos y el monto de pago", isError = true)
        OutlinedTextField(title, { title = it.take(120) }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth(), isError = attempted && title.trim().length !in 3..120)
        OutlinedTextField(description, { description = it.take(3000) }, label = { Text("Tareas") }, minLines = 3, modifier = Modifier.fillMaxWidth(), isError = attempted && description.trim().length !in 10..3000)
        OutlinedTextField(requirements, { requirements = it.take(1500) }, label = { Text("Conocimientos y requisitos") }, minLines = 2, modifier = Modifier.fillMaxWidth())
        Text("¿Necesita experiencia previa?", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = noExperience == true, onClick = { noExperience = true }, label = { Text("No es necesaria") })
            FilterChip(selected = noExperience == false, onClick = { noExperience = false }, label = { Text("Sí es necesaria") })
        }
        if (model.profileOrganization.isNotBlank() || includeBusiness) {
            Row(Modifier.fillMaxWidth().toggleable(value = includeBusiness, role = androidx.compose.ui.semantics.Role.Checkbox, onValueChange = { includeBusiness = it }), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = includeBusiness, onCheckedChange = null)
                Text(if (model.profileOrganization.isNotBlank()) "Mostrar datos de ${model.profileOrganization}" else "Mostrar datos del negocio")
            }
            if (includeBusiness && model.profileOrganization.trim().length < 2) Text("Completa el nombre del negocio en tu perfil o desactiva esta opción", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            else if (includeBusiness) Text("Se mostrarán los datos actuales de tu perfil", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("Tipo de trabajo", style = MaterialTheme.typography.titleMedium)
        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(workInterests.size) { index -> val item = workInterests[index]; FilterChip(selected = category == item, onClick = { category = item }, label = { Text(item) }) }
        }
        Text("Modalidad de trabajo", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Presencial", "Híbrido", "Remoto").forEach { mode ->
                FilterChip(selected = workMode == mode, onClick = {
                    if (mode == "Remoto" && location.isBlank()) location = "Remoto"
                    else if (mode != "Remoto" && location.equals("Remoto", ignoreCase = true)) location = ""
                    workMode = mode
                }, label = { Text(mode) })
            }
        }
        OutlinedTextField(location, { location = it.take(120) }, label = { Text("Lugar o modalidad") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(schedule, { schedule = it.take(120) }, label = { Text("Fecha y duración") }, modifier = Modifier.fillMaxWidth())
        if (initialPay == null) Text("Pago anterior: ${opportunity.pay}. Ingresa el monto para actualizarlo", style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(amount, { amount = it.filter { c -> c.isDigit() || c == '.' || c == ',' }.take(9) }, label = { Text("Monto") }, prefix = { Text("S/ ") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(unit, {}, readOnly = true, label = { Text("Frecuencia de pago") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth())
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                listOf("Por hora", "Por día", "Por tarea", "Por mes").forEach { item -> DropdownMenuItem(text = { Text(item) }, onClick = { unit = item; expanded = false }) }
            }
        }
        OutlinedTextField(vacancies, { vacancies = it.filter(Char::isDigit).take(2) }, label = { Text("Vacantes") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        AppPrimaryButton("Revisar cambios", enabled = !model.busy && opportunity.selectedCount == 0, onClick = { attempted = true; if (valid) confirm = true })
        if (opportunity.selectedCount > 0) AppNotice("La publicación tiene personas seleccionadas y no puede editarse")
    }
}
