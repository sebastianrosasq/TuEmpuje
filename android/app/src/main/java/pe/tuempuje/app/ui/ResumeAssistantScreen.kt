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
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.asImageBitmap
import android.content.Intent
import android.net.Uri
import android.content.ActivityNotFoundException
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
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
import pe.tuempuje.app.profile.readCvForAnalysis
import pe.tuempuje.app.profile.ReferenceResumeTemplates
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
    var style by rememberSaveable(uid, opportunity?.id) { mutableStateOf(initial.style.takeIf { value -> ReferenceResumeTemplates.styles.any { it.name == value } } ?: ResumeStyle.CENTRADO.name) }
    var goal by rememberSaveable(uid, opportunity?.id) { mutableStateOf(initial.goal) }
    var summary by rememberSaveable(uid, opportunity?.id) { mutableStateOf(initial.summary) }
    var skills by rememberSaveable(uid, opportunity?.id) { mutableStateOf(initial.skills) }
    var experience by rememberSaveable(uid, opportunity?.id) { mutableStateOf(initial.experience) }
    var education by rememberSaveable(uid, opportunity?.id) { mutableStateOf(initial.education) }
    var instruction by rememberSaveable(uid, opportunity?.id) { mutableStateOf(initial.instruction) }
    var languages by rememberSaveable(uid, opportunity?.id) { mutableStateOf(initial.languages) }
    var references by rememberSaveable(uid, opportunity?.id) { mutableStateOf(initial.references) }
    var photoUri by rememberSaveable(uid, opportunity?.id) { mutableStateOf(initial.photoUri) }
    var answers by rememberSaveable(uid, opportunity?.id) { mutableStateOf(initial.answers) }
    var questions by rememberSaveable(profile.uid) { mutableStateOf("") }
    var consent by rememberSaveable(profile.uid) { mutableStateOf(false) }
    var adult by rememberSaveable(profile.uid) { mutableStateOf(false) }
    var pdfConsent by remember(profile.uid) { mutableStateOf(false) }
    var selectedImport by remember(profile.uid, opportunity?.id) { mutableStateOf<Uri?>(null) }
    var busy by remember { mutableStateOf(false) }
    var suggestion by remember { mutableStateOf<ResumeSuggestion?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var lastPdf by rememberSaveable(uid, opportunity?.id) { mutableStateOf<String?>(null) }
    var confirmExit by remember { mutableStateOf(false) }
    var confirmEmpty by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val selectImport = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        selectedImport = uri
        pdfConsent = false
        error = null
    }
    LaunchedEffect(uid, opportunity?.id, style, goal, summary, skills, experience, education, instruction, languages, references, photoUri, answers) {
        if (uid.isNotBlank()) {
            delay(350)
            val draft = ResumeDraft(style, goal, summary, skills, experience, education, instruction, languages, references, photoUri, answers)
            withContext(Dispatchers.IO) { draftStore.save(uid, opportunity?.id, draft) }
        }
    }
    var pdfProfile by remember { mutableStateOf<AccountUiState?>(null) }
    val savePdf = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        val selected = pdfProfile
        pdfProfile = null
        if (uri != null && selected != null) scope.launch {
            busy = true
            try {
                withContext(Dispatchers.IO) { ResumePdf.write(context.contentResolver, uri, selected, ResumeStyle.valueOf(style), goal, summary, photoUri.takeIf { it.isNotBlank() }?.let(Uri::parse), languages, references) }
                val retained = runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }.isSuccess
                val remembered = retained && withContext(Dispatchers.IO) { draftStore.savePdfUri(uid, opportunity?.id, uri.toString()) }
                lastPdf = uri.toString()
                message = if (remembered) "CV guardado. Ya puedes abrirlo o elegirlo al postular" else "CV guardado. Si lo adjuntas más tarde, vuelve a elegir el archivo"
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { error = "No se pudo guardar el CV. Inténtalo de nuevo" }
            finally { busy = false }
        }
    }
    val selectPhoto = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val permission = runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }.isSuccess
            if (permission) scope.launch {
                val valid = withContext(Dispatchers.IO) {
                    val image = ReferenceResumeTemplates.photo(context.contentResolver, uri)
                    (image != null).also { image?.recycle() }
                }
                if (valid) { photoUri = uri.toString(); error = null }
                else error = "No se pudo abrir la imagen. Elige otra foto"
            } else error = "No se pudo conservar el acceso a la foto. Elige otra imagen"
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
        confirmButton = { TextButton(onClick = { skills = ""; experience = ""; education = ""; summary = ""; instruction = ""; languages = ""; references = ""; answers = ""; questions = ""; confirmEmpty = false }) { Text("Limpiar borrador") } },
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
            val draft = ResumeDraft(style, goal, summary, skills, experience, education, instruction, languages, references, photoUri, answers)
            scope.launch {
                val saved = withContext(Dispatchers.IO) { draftStore.save(uid, opportunity?.id, draft) }
                if (saved) { message = "Borrador guardado en este dispositivo"; error = null }
                else error = "No se pudo guardar el borrador en este dispositivo"
            }
        }) { Text("Guardar borrador") }
        if (step == 0) {
            Text("Elige un diseño", style = MaterialTheme.typography.titleLarge)
            Text("Desliza para comparar los diseños. Puedes cambiarlo antes de guardar el documento")
            val example = profile.copy(skills = "Tus habilidades", experience = "Tu experiencia o proyectos", education = "Tus estudios o cursos")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                items(ReferenceResumeTemplates.styles, key = { it.name }) { option ->
                    ResumeTemplateOption(option, example, selected = style == option.name, modifier = Modifier.width(190.dp)) { style = option.name }
                }
            }
            AppNotice("Los ejemplos muestran la distribución del documento. Puedes cambiar el diseño y añadir una foto antes de guardar")
            Button(onClick = { step = 1 }, modifier = Modifier.fillMaxWidth()) { Text("Continuar") }
        } else if (step == 1) {
            Text("Cuéntame sobre ti", style = MaterialTheme.typography.titleLarge)
            Text("Puedes utilizar lo que ya guardaste en tu perfil y modificarlo para este CV")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = adult, onCheckedChange = { adult = it }, enabled = !busy)
                Text("Confirmo que tengo 18 años o más", style = MaterialTheme.typography.bodySmall)
            }
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("¿Ya tienes un CV?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Puedes seleccionar un PDF de hasta 4 MB para que Empu sugiera información. El archivo no se añade a una postulación automáticamente", style = MaterialTheme.typography.bodyMedium)
                    OutlinedButton(enabled = !busy, onClick = { selectImport.launch(arrayOf("application/pdf")) }) {
                        Text(if (selectedImport == null) "Elegir PDF" else "Cambiar PDF")
                    }
                    if (selectedImport != null) {
                        Text("El PDF completo, incluidos los datos personales que contenga, se enviará a Google para analizarlo. Revisa el archivo antes de continuar", style = MaterialTheme.typography.bodySmall)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = pdfConsent, onCheckedChange = { pdfConsent = it }, enabled = !busy)
                            Text("Acepto enviar este PDF para su análisis", style = MaterialTheme.typography.bodySmall)
                        }
                        OutlinedButton(enabled = !busy && pdfConsent && adult && !services.isLocal, onClick = {
                            val uri = selectedImport ?: return@OutlinedButton
                            busy = true; error = null
                            scope.launch {
                                try {
                                    val pdf = withContext(Dispatchers.IO) { readCvForAnalysis(context.contentResolver, uri) }
                                    suggestion = EmpuAssistant(services).reviewResumePdf(pdf, opportunity?.requirements.orEmpty())
                                } catch (_: TimeoutCancellationException) { error = "La respuesta tardó demasiado. Puedes continuar manualmente" }
                                catch (e: CancellationException) { throw e }
                                catch (e: IllegalArgumentException) { error = e.message ?: "Selecciona un PDF válido" }
                                catch (e: Exception) { error = assistantErrorMessage(e) }
                                finally { busy = false }
                            }
                        }, modifier = Modifier.fillMaxWidth()) { Text("Analizar PDF con Empu") }
                        if (services.isLocal) Text("El análisis necesita conexión al servicio de IA. Puedes continuar manualmente", style = MaterialTheme.typography.bodySmall)
                        TextButton(enabled = !busy, onClick = { selectedImport = null; pdfConsent = false }) { Text("Quitar PDF") }
                    }
                }
            }
            TextButton(onClick = { confirmEmpty = true }, enabled = !busy) { Text("Empezar desde cero") }
            if (opportunity != null) AppNotice("Preparando una versión para: ${opportunity.title}. Revisa el resultado antes de usarlo")
            if (questions.isNotBlank()) AppNotice(questions)
            if (questions.isNotBlank()) OutlinedTextField(value = answers, onValueChange = { answers = it.take(1200) }, label = { Text("Tus respuestas para Empu") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = goal, onValueChange = { goal = it.take(150) }, label = { Text("Puesto que buscas, opcional") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = skills, onValueChange = { skills = it.take(1500) }, label = { Text("Habilidades") }, minLines = 2, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = experience, onValueChange = { experience = it.take(2500) }, label = { Text("Proyectos o experiencia") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = education, onValueChange = { education = it.take(1500) }, label = { Text("Estudios o cursos") }, minLines = 2, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = languages, onValueChange = { languages = it.take(800) }, label = { Text("Idiomas y nivel, opcional") }, minLines = 2, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = references, onValueChange = { references = it.take(1000) }, label = { Text("Referencias, opcional") }, minLines = 2, modifier = Modifier.fillMaxWidth())
            OutlinedButton(onClick = { selectPhoto.launch(arrayOf("image/*")) }, enabled = !busy) { Text(if (photoUri.isBlank()) "Añadir foto opcional" else "Cambiar foto") }
            if (photoUri.isNotBlank()) TextButton(onClick = { photoUri = "" }) { Text("Quitar foto") }
            Text("Tu foto se guarda solo en el documento y no se envía a la IA", style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(value = instruction, onValueChange = { instruction = it.take(500) }, label = { Text("¿Qué quieres destacar? Opcional") }, minLines = 2, modifier = Modifier.fillMaxWidth())
            Text("Para sugerir tu CV se envían a Google el puesto, requisitos y los textos que ingreses aquí. Evita DNI, teléfono y correo en esos campos. En el nivel gratuito, Google puede usar este contenido para mejorar sus productos", style = MaterialTheme.typography.bodySmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = consent, onCheckedChange = { consent = it }, enabled = !busy)
                Text("Acepto enviar estos datos", style = MaterialTheme.typography.bodySmall)
            }
            if (services.isLocal) AppNotice("Empu necesita conexión al servicio de IA. Puedes continuar manualmente")
            if (!consent || !adult || listOf(skills, experience, education).none { it.isNotBlank() }) Text("Para pedir sugerencias, completa al menos un campo y marca las dos casillas", style = MaterialTheme.typography.bodySmall)
            if (busy) AppLoadingState("Empu está preparando sugerencias")
            OutlinedButton(enabled = !busy && consent && adult && !services.isLocal && listOf(skills, experience, education).any { it.isNotBlank() }, onClick = {
                busy = true; error = null
                scope.launch {
                    try { suggestion = EmpuAssistant(services).draftResume(goal, skills, experience, education, instruction, opportunity?.requirements.orEmpty(), answers) }
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
            AppNotice("Diseño: ${ReferenceResumeTemplates.label(ResumeStyle.valueOf(style))} · ${goal.ifBlank { "CV general" }}")
            Text("Vista previa de la primera página", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            ResumeDocumentPreview(profile.copy(skills = skills, experience = experience, education = education), ResumeStyle.valueOf(style), goal, summary, photoUri, languages, references)
            if (questions.isNotBlank()) {
                AppNotice(questions)
                OutlinedButton(onClick = { step = 1 }, modifier = Modifier.fillMaxWidth()) { Text("Responder a Empu") }
            }
            Button(enabled = !busy, onClick = {
                error = null; message = null
                pdfProfile = profile.copy(skills = skills, experience = experience, education = education)
                savePdf.launch("TuEmpuje-CV.pdf")
            }, modifier = Modifier.fillMaxWidth()) { Text("Guardar PDF") }
            lastPdf?.let { saved ->
                OutlinedButton(onClick = {
                    try { context.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(Uri.parse(saved), "application/pdf").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)) }
                    catch (_: ActivityNotFoundException) { error = "No hay una aplicación para abrir PDF" }
                    catch (_: SecurityException) { error = "No se pudo abrir el archivo, vuelve a seleccionarlo desde el dispositivo" }
                }, modifier = Modifier.fillMaxWidth()) { Text("Abrir mi CV") }
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text(if (opportunity != null) "Volver a la oportunidad" else "Terminar") }
            }
        }
    }
}

@Composable
private fun ResumeDocumentPreview(profile: AccountUiState, style: ResumeStyle, goal: String, summary: String, photoUri: String = "", languages: String = "", references: String = "") {
    val context = LocalContext.current
    val bitmap by produceState<android.graphics.Bitmap?>(null, profile, style, goal, summary, photoUri, languages, references) {
        value = withContext(Dispatchers.Default) {
            val photo = ReferenceResumeTemplates.photo(context.contentResolver, photoUri.takeIf { it.isNotBlank() }?.let(Uri::parse))
            try { ReferenceResumeTemplates.preview(profile, style, goal, summary, photo, languages, references) }
            finally { photo?.recycle() }
        }
    }
    Surface(shape = RoundedCornerShape(6.dp), color = androidx.compose.ui.graphics.Color.White,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), modifier = Modifier.fillMaxWidth()) {
        bitmap?.let { Image(it.asImageBitmap(), contentDescription = "Vista previa del CV ${ReferenceResumeTemplates.label(style)}", modifier = Modifier.fillMaxWidth().aspectRatio(595f / 842f)) }
            ?: Box(Modifier.fillMaxWidth().aspectRatio(595f / 842f), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    }
}

@Composable
private fun ResumeTemplateOption(style: ResumeStyle, profile: AccountUiState, selected: Boolean, modifier: Modifier = Modifier, onSelect: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(onClick = onSelect, shape = MaterialTheme.shapes.large, modifier = modifier,
        color = if (selected) colors.primaryContainer else colors.surface,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outlineVariant)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(ReferenceResumeTemplates.label(style), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                RadioButton(selected = selected, onClick = null)
            }
            ResumeDocumentPreview(profile, style, "Puesto que buscas", "Tu presentación profesional", languages = "Tus idiomas")
        }
    }
}
