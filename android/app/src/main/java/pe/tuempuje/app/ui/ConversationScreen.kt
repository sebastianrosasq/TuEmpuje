package pe.tuempuje.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import pe.tuempuje.app.data.FirebaseServices
import pe.tuempuje.app.data.Opportunity
import pe.tuempuje.app.ui.components.AppLoadingState
import pe.tuempuje.app.ui.components.AppNotice
import pe.tuempuje.app.ui.components.Empu
import pe.tuempuje.app.ui.components.EmpuMood
import java.text.DateFormat
import java.util.Date

private data class ChatMessage(val id: String, val sender: String, val text: String, val sentAt: Date?)

@Composable
fun ConversationScreen(services: FirebaseServices?, opportunity: Opportunity?, applicantId: String, onBack: () -> Unit) {
    val uid = services?.auth?.currentUser?.uid
    if (opportunity == null || uid == null || (uid != applicantId && !opportunity.own)) {
        Column(Modifier.padding(22.dp)) { AppNotice("Esta conversación no está disponible"); TextButton(onClick = onBack) { Text("Volver") } }
        return
    }
    val db = services?.firestore ?: return
    val application = remember(opportunity.id, applicantId) { db.collection("opportunities").document(opportunity.id).collection("applications").document(applicantId) }
    var messages by remember(opportunity.id, applicantId) { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var applicationStatus by remember(opportunity.id, applicantId) { mutableStateOf<String?>(null) }
    var loading by remember(opportunity.id, applicantId) { mutableStateOf(true) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var text by rememberSaveable(opportunity.id, applicantId) { mutableStateOf("") }
    var previousCount by remember(opportunity.id, applicantId) { mutableIntStateOf(0) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    DisposableEffect(application) {
        val appListener = application.addSnapshotListener { snapshot, exception ->
            if (exception != null) error = "No se pudo consultar la postulación"
            else applicationStatus = snapshot?.getString("status")
        }
        val messageListener = application.collection("messages").orderBy("createdAt").addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, exception ->
            loading = false
            if (exception != null) error = "No se pudo cargar la conversación, revisa tu conexión"
            else {
                error = null
                messages = snapshot?.documents.orEmpty().filterNot { it.metadata.hasPendingWrites() }.map { doc ->
                    ChatMessage(doc.id, doc.getString("senderId").orEmpty(), doc.getString("text").orEmpty(), doc.getTimestamp("createdAt")?.toDate())
                }
            }
        }
        onDispose { appListener.remove(); messageListener.remove() }
    }
    LaunchedEffect(messages.size) {
        val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
        if (messages.isNotEmpty() && (previousCount == 0 || lastVisible >= previousCount - 2 || messages.last().sender == uid)) {
            listState.animateScrollToItem(messages.lastIndex)
        }
        previousCount = messages.size
    }
    LaunchedEffect(messages.lastOrNull()?.id, uid, opportunity.id, applicantId) {
        messages.lastOrNull()?.let { message ->
            context.getSharedPreferences("mensajes_leidos", Context.MODE_PRIVATE).edit()
                .putString(conversationReadKey(uid, opportunity.id, applicantId), message.id).apply()
        }
    }
    val canSend = opportunity.status in setOf("published", "closed") && applicationStatus in setOf("pending", "reviewing", "selected", "completed")
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver") }
            Column(Modifier.weight(1f)) {
                Text("Conversación", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(opportunity.title, style = MaterialTheme.typography.bodySmall, maxLines = 2)
            }
            Empu(EmpuMood.HAPPY, Modifier.size(48.dp))
        }
        error?.let { AppNotice(it, isError = true) }
        if (loading) AppLoadingState("Cargando mensajes")
        LazyColumn(state = listState, modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
            if (!loading && messages.isEmpty()) item { AppNotice("Coordinen aquí los detalles de la oportunidad") }
            items(messages, key = { it.id }) { message ->
                val mine = message.sender == uid
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
                    Surface(shape = MaterialTheme.shapes.large, color = if (mine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.widthIn(max = 300.dp)) {
                        Column(Modifier.padding(14.dp)) {
                            Text(if (mine) "Tú" else "Otra persona", style = MaterialTheme.typography.labelSmall)
                            Text(message.text, style = MaterialTheme.typography.bodyLarge)
                            message.sentAt?.let { Text(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(it), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                    }
                }
            }
        }
        if (!loading && !canSend) AppNotice("Esta conversación está disponible solo como historial")
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = text, onValueChange = { text = it.take(2000) }, enabled = canSend && !busy,
                label = { Text("Mensaje") }, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default), modifier = Modifier.weight(1f), maxLines = 4)
            FilledIconButton(enabled = canSend && !busy && text.isNotBlank(), onClick = {
                val message = text.trim()
                if (message.isNotEmpty()) {
                    busy = true; error = null
                    scope.launch {
                        try {
                            withTimeout(15_000) {
                                application.collection("messages").document().set(mapOf("senderId" to uid, "text" to message, "createdAt" to FieldValue.serverTimestamp())).await()
                            }
                            if (text.trim() == message) text = ""
                        } catch (_: TimeoutCancellationException) { error = "No se confirmó el envío, revisa la conversación antes de volver a intentarlo" }
                        catch (exception: CancellationException) { throw exception }
                        catch (_: Exception) { error = "No se pudo enviar el mensaje, revisa tu conexión" }
                        finally { busy = false }
                    }
                }
            }, modifier = Modifier.size(52.dp)) { Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Enviar mensaje") }
        }
    }
}
