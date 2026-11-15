package pe.tuempuje.app.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pe.tuempuje.app.auth.AccountUiState
import pe.tuempuje.app.data.EmpuAssistant
import pe.tuempuje.app.data.FirebaseServices
import pe.tuempuje.app.data.ResumeSuggestion
import pe.tuempuje.app.data.assistantErrorMessage
import pe.tuempuje.app.profile.ResumePdf
import pe.tuempuje.app.profile.ResumeStyle
import pe.tuempuje.app.profile.ResumeDraft
import pe.tuempuje.app.profile.ResumeDraftStore
import pe.tuempuje.app.ui.components.*

data class CvOpportunityContext(val id: String, val title: String, val requirements: String)

@Composable
fun ResumeAssistantScreen(profile: AccountUiState, services: FirebaseServices, opportunity: CvOpportunityContext? = null, onBack: () -> Unit) {
    val context = LocalContext.current
    val draftStore = remember(context) { ResumeDraftStore(context) }
    val uid = profile.uid.orEmpty()
    val initial = remember(uid, opportunity?.id) {
        draftStore.load(uid, opportunity?.id) ?: opportunity?.let { draftStore.load(uid, null)?.copy(goal = it.title) }
            ?: ResumeDraft(goal = opportunity?.title.orEmpty(), skills = profile.skills, experience = profile.experience, education = profile.education)
    }
    var step by rememberSaveable(uid, opportunity?.id) { mutableIntStateOf(0) }
    var style by rememberSaveable(uid, opportunity?.id) { mutableStateOf(initial.style) }
    var goal by rememberSaveable(uid, opportunity?.id) { mutableStateOf(initial.goal) }
    var summary by rememberSaveable(uid, opportunity?.id) { mutableStateOf(initial.summary) }
    var skills by rememberSaveable(uid, opportunity?.id) { mutableStateOf(initial.skills) }
    var experience by rememberSaveable(uid, opportunity?.id) { mutableStateOf(initial.experience) }
    var education by rememberSaveable(uid, opportunity?.id) { mutableStateOf(initial.education) }
    var instruction by rememberSaveable(uid, opportunity?.id) { mutableStateOf(initial.instruction) }
    var questions by rememberSaveable(profile.uid) { mutableStateOf("") }
    var consent by rememberSaveable(profile.uid) { mutableStateOf(false) }
    var adult by rememberSaveable(profile.uid) { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var suggestion by remember { mutableStateOf<ResumeSuggestion?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var confirmExit by remember { mutableStateOf(false) }
    var confirmEmpty by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var pdfProfile by remember { mutableStateOf<AccountUiState?>(null) }
    val savePdf = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        val selected = pdfProfile
        pdfProfile = null
        if (uri != null && selected != null) scope.launch {
            busy = true
            try {
                withContext(Dispatchers.IO) { ResumePdf.write(context.contentResolver, uri, selected, ResumeStyle.valueOf(style), goal, summary) }
                message = "CV guardado en el lugar que elegiste"
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { error = "No se pudo guardar el CV. Inténtalo de nuevo" }
            finally { busy = false }
        }
    }
    fun back() { if (step > 0) step-- else confirmExit = true }
    BackHandler { if (!busy) back() }
    if (confirmExit) AlertDialog(onDismissRequest = { confirmExit = false }, title = { Text("¿Salir del creador de CV?") },
        text = { Text("Los cambios que no hayas guardado en este dispositivo se perderán") },
        confirmButton = { TextButton(onClick = onBack) { Text("Salir") } },
        dismissButton = { TextButton(onClick = { confirmExit = false }) { Text("Seguir editando") } })
    if (confirmEmpty) AlertDialog(onDismissRequest = { confirmEmpty = false }, title = { Text("¿Empezar desde cero?") },
        text = { Text("Se limpiarán las habilidades, proyectos y estudios de este borrador. Los datos de tu perfil seguirán guardados") },
        confirmButton = { TextButton(onClick = { skills = ""; experience = ""; education = ""; summary = ""; instruction = ""; questions = ""; confirmEmpty = false }) { Text("Limpiar borrador") } },
        dismissButton = { TextButton(onClick = { confirmEmpty = false }) { Text("Cancelar") } })
    suggestion?.let { draft ->
        AlertDialog(onDismissRequest = { suggestion = null }, title = { Text("Propuesta de Empu") },
            text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Comprueba que cada dato sea verdadero antes de usarlo")
                if (draft.summary.isNotBlank()) Text("Perfil: ${draft.summary}")
                if (draft.skills.isNotBlank()) Text("Habilidades: ${draft.skills}")
                if (draft.experience.isNotBlank()) Text("Proyectos o experiencia: ${draft.experience}")
                if (draft.education.isNotBlank()) Text("Formación: ${draft.education}")
                if (draft.questions.isNotBlank()) AppNotice(draft.questions)
            } },
            confirmButton = { TextButton(onClick = {
                if (draft.summary.isNotBlank()) summary = draft.summary
                if (draft.skills.isNotBlank()) skills = draft.skills
                if (draft.experience.isNotBlank()) experience = draft.experience
                if (draft.education.isNotBlank()) education = draft.education
                questions = draft.questions
                suggestion = null; step = 2
            }) { Text("Usar y editar") } },
            dismissButton = { TextButton(onClick = { suggestion = null }) { Text("Descartar") } })
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TextButton(enabled = !busy, onClick = ::back) { Text("Volver") }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Tu CV con Empu", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Paso ${step + 1} de 3", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            if (LocalAppPreferences.current?.showMascot != false) Empu(if (busy) EmpuMood.LOADING else EmpuMood.HAPPY, Modifier.size(104.dp), accessory = EmpuAccessory.WORK)
        }
        LinearProgressIndicator(progress = { (step + 1) / 3f }, modifier = Modifier.fillMaxWidth())
        error?.let { AppNotice(it, isError = true) }
        message?.let { AppNotice(it) }
        TextButton(enabled = !busy, onClick = {
            val draft = ResumeDraft(style, goal, summary, skills, experience, education, instruction)
            scope.launch {
                val saved = withContext(Dispatchers.IO) { draftStore.save(uid, opportunity?.id, draft) }
                if (saved) { message = "Borrador guardado en este dispositivo"; error = null }
                else error = "No se pudo guardar el borrador en este dispositivo"
            }
        }) { Text("Guardar borrador") }
        if (step == 0) {
            Text("Elige un diseño", style = MaterialTheme.typography.titleLarge)
            Text("Puedes cambiarlo antes de guardar el documento")
            listOf(ResumeStyle.PROFESSIONAL to "Profesional", ResumeStyle.MODERN to "Moderno", ResumeStyle.FIRST_EXPERIENCE to "Primera experiencia").forEach { (option, label) ->
                ResumeTemplateOption(label, option, selected = style == option.name) { style = option.name }
            }
            AppNotice("El diseño de primera experiencia destaca tus proyectos. La foto y la importación de PDF aún no están disponibles")
            Button(onClick = { step = 1 }, modifier = Modifier.fillMaxWidth()) { Text("Continuar") }
        } else if (step == 1) {
            Text("Cuéntame sobre ti", style = MaterialTheme.typography.titleLarge)
            Text("Puedes utilizar lo que ya guardaste en tu perfil y modificarlo para este CV")
            TextButton(onClick = { confirmEmpty = true }, enabled = !busy) { Text("Empezar desde cero") }
            if (opportunity != null) AppNotice("Preparando una versión para: ${opportunity.title}. Revisa el resultado antes de usarlo")
            if (questions.isNotBlank()) AppNotice(questions)
            OutlinedTextField(value = goal, onValueChange = { goal = it.take(150) }, label = { Text("Puesto que buscas, opcional") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = skills, onValueChange = { skills = it.take(1500) }, label = { Text("Habilidades") }, minLines = 2, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = experience, onValueChange = { experience = it.take(2500) }, label = { Text("Proyectos o experiencia") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = education, onValueChange = { education = it.take(1500) }, label = { Text("Estudios o cursos") }, minLines = 2, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = instruction, onValueChange = { instruction = it.take(500) }, label = { Text("¿Qué quieres destacar? Opcional") }, minLines = 2, modifier = Modifier.fillMaxWidth())
            Text("Si solicitas ayuda, se enviarán a Gemini el puesto, sus requisitos si elegiste una oferta y los textos anteriores. No incluyas DNI, teléfono ni correo en estos campos. Google puede usar el contenido para mejorar sus productos en el nivel gratuito", style = MaterialTheme.typography.bodySmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = consent, onCheckedChange = { consent = it }, enabled = !busy)
                Text("Acepto enviar estos textos a Gemini", style = MaterialTheme.typography.bodySmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = adult, onCheckedChange = { adult = it }, enabled = !busy)
                Text("Confirmo que tengo 18 años o más", style = MaterialTheme.typography.bodySmall)
            }
            if (services.isLocal) AppNotice("Para usar Gemini necesitas la conexión de desarrollo. Puedes continuar manualmente")
            if (busy) AppLoadingState("Empu está preparando sugerencias")
            OutlinedButton(enabled = !busy && consent && adult && !services.isLocal && listOf(skills, experience, education).any { it.isNotBlank() }, onClick = {
                busy = true; error = null
                scope.launch {
                    try { suggestion = EmpuAssistant(services).draftResume(goal, skills, experience, education, instruction, opportunity?.requirements.orEmpty()) }
                    catch (_: TimeoutCancellationException) { error = "La respuesta tardó demasiado. Puedes continuar manualmente" }
                    catch (e: CancellationException) { throw e }
                    catch (e: Exception) { error = assistantErrorMessage(e) }
                    finally { busy = false }
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Pedir sugerencias a Empu") }
            Button(enabled = !busy, onClick = { step = 2 }, modifier = Modifier.fillMaxWidth()) { Text("Continuar sin IA") }
        } else {
            Text("Revisa tu CV", style = MaterialTheme.typography.titleLarge)
            Text("El PDF incluirá el nombre y los datos de contacto de tu perfil. Los cambios hechos aquí no modifican tu perfil")
            OutlinedTextField(value = summary, onValueChange = { summary = it.take(1200) }, label = { Text("Presentación, opcional") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = skills, onValueChange = { skills = it.take(1500) }, label = { Text("Habilidades") }, minLines = 2, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = experience, onValueChange = { experience = it.take(2500) }, label = { Text("Proyectos o experiencia") }, minLines = 2, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = education, onValueChange = { education = it.take(1500) }, label = { Text("Estudios o cursos") }, minLines = 2, modifier = Modifier.fillMaxWidth())
            AppNotice("Diseño: ${when (style) { ResumeStyle.MODERN.name -> "Moderno"; ResumeStyle.FIRST_EXPERIENCE.name -> "Primera experiencia"; else -> "Profesional" }} · ${goal.ifBlank { "CV general" }}")
            Text("Vista previa orientativa", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            ResumePreview(profile, style, goal, summary, skills, experience, education)
            if (questions.isNotBlank()) {
                AppNotice(questions)
                OutlinedButton(onClick = { step = 1 }, modifier = Modifier.fillMaxWidth()) { Text("Responder a Empu") }
            }
            Button(enabled = !busy, onClick = {
                error = null; message = null
                pdfProfile = profile.copy(skills = skills, experience = experience, education = education)
                savePdf.launch("TuEmpuje-CV.pdf")
            }, modifier = Modifier.fillMaxWidth()) { Text("Guardar PDF") }
        }
    }
}

@Composable
private fun ResumePreview(profile: AccountUiState, style: String, goal: String, summary: String, skills: String, experience: String, education: String) {
    val ink = androidx.compose.ui.graphics.Color(0xFF202B3D)
    val accent = when (style) { ResumeStyle.MODERN.name -> androidx.compose.ui.graphics.Color(0xFF187F76); ResumeStyle.FIRST_EXPERIENCE.name -> androidx.compose.ui.graphics.Color(0xFF3D5585); else -> ink }
    Surface(shape = RoundedCornerShape(6.dp), color = androidx.compose.ui.graphics.Color.White, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), modifier = Modifier.fillMaxWidth()) {
        if (style == ResumeStyle.MODERN.name) Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(Modifier.weight(.38f).background(androidx.compose.ui.graphics.Color(0xFFDEF1EE), RoundedCornerShape(4.dp)).padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("Contacto" to listOf(profile.email, profile.phone, profile.district).filter { it.isNotBlank() }.joinToString("\n"), "Habilidades" to skills, "Formación" to education, "Disponibilidad" to profile.availability, "Portafolio" to profile.portfolio).filter { it.second.isNotBlank() }.forEach { (title, value) ->
                    Text(title.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = accent)
                    Text(value, style = MaterialTheme.typography.bodySmall, color = ink)
                    Spacer(Modifier.height(3.dp))
                }
            }
            Column(Modifier.weight(.62f).padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(profile.displayName.ifBlank { "Tu nombre" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ink)
                Text(goal.ifBlank { "Perfil laboral" }, style = MaterialTheme.typography.labelSmall, color = accent)
                listOf("Perfil" to summary, "Experiencia y proyectos" to experience).filter { it.second.isNotBlank() }.forEach { (title, value) ->
                    Spacer(Modifier.height(4.dp))
                    Text(title.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = accent)
                    Text(value, style = MaterialTheme.typography.bodySmall, color = ink)
                }
            }
        } else Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(profile.displayName.ifBlank { "Tu nombre" }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ink)
            Text(goal.ifBlank { "Perfil laboral" }, style = MaterialTheme.typography.labelMedium, color = accent)
            Text(listOf(profile.email, profile.phone, profile.district).filter { it.isNotBlank() }.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = ink)
            listOf("Perfil" to summary, "Habilidades" to skills, "Experiencia y proyectos" to experience, "Formación" to education).filter { it.second.isNotBlank() }.forEach { (title, value) ->
                Spacer(Modifier.height(4.dp))
                Text(title.uppercase(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = accent)
                Text(value, style = MaterialTheme.typography.bodySmall, color = ink)
            }
        }
    }
}

@Composable
private fun ResumeTemplateOption(label: String, style: ResumeStyle, selected: Boolean, onSelect: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(onClick = onSelect, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth(),
        color = if (selected) colors.primaryContainer else colors.surface,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outlineVariant)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(shape = RoundedCornerShape(5.dp), color = colors.surface, modifier = Modifier.width(52.dp).height(68.dp)) {
                Column(Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.fillMaxWidth().height(if (style == ResumeStyle.PROFESSIONAL) 6.dp else 13.dp).background(if (style == ResumeStyle.FIRST_EXPERIENCE) colors.secondary else colors.primary, RoundedCornerShape(2.dp)))
                    repeat(4) { index -> Box(Modifier.fillMaxWidth(if (index == 2) .6f else .9f).height(3.dp).background(colors.outlineVariant, RoundedCornerShape(2.dp))) }
                }
            }
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(when (style) { ResumeStyle.PROFESSIONAL -> "Sobrio y fácil de leer"; ResumeStyle.MODERN -> "Con detalles de color"; ResumeStyle.FIRST_EXPERIENCE -> "Destaca tus proyectos" }, style = MaterialTheme.typography.bodySmall)
            }
            RadioButton(selected = selected, onClick = null)
        }
    }
}
