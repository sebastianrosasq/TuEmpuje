package pe.tuempuje.app.ui.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.credentials.CredentialManager
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import pe.tuempuje.app.auth.AccountUiState
import pe.tuempuje.app.auth.AccountViewModel
import pe.tuempuje.app.auth.AccountViewModelFactory
import pe.tuempuje.app.ui.components.AppNotice
import pe.tuempuje.app.ui.components.AppPrimaryButton
import pe.tuempuje.app.ui.components.AppLoadingState
import pe.tuempuje.app.ui.components.Empu
import pe.tuempuje.app.ui.components.EmpuMood
import pe.tuempuje.app.BuildConfig
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

@Composable
fun AccountScreen(auth: FirebaseAuth, firestore: FirebaseFirestore, isLocal: Boolean, onSettings: () -> Unit, onSaved: () -> Unit, onMascotDemo: () -> Unit) {
    val factory = remember(auth, firestore) { AccountViewModelFactory(auth, firestore) }
    val account: AccountViewModel = viewModel(factory = factory)
    val state by account.state.collectAsState()
    var showExperience by rememberSaveable(state.uid) { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val scope = rememberCoroutineScope()
    var confirmSignOut by rememberSaveable(state.uid) { mutableStateOf(false) }
    var showCvInfo by rememberSaveable { mutableStateOf(false) }
    val signOut = {
        account.signOut()
        scope.launch { runCatching { credentialManager.clearCredentialState(ClearCredentialStateRequest()) } }
        Unit
    }

    if (confirmSignOut) {
        AlertDialog(
            onDismissRequest = { confirmSignOut = false },
            icon = { Empu(EmpuMood.THINKING, Modifier.size(112.dp)) },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Podrás volver a entrar con tu cuenta cuando quieras") },
            confirmButton = { TextButton(onClick = { confirmSignOut = false; signOut() }) { Text("Cerrar sesión") } },
            dismissButton = { TextButton(onClick = { confirmSignOut = false }) { Text("Quedarme") } },
        )
    }
    if (showCvInfo) {
        AlertDialog(
            onDismissRequest = { showCvInfo = false },
            icon = { Empu(EmpuMood.THINKING, Modifier.size(112.dp)) },
            title = { Text("Tu futuro CV") },
            text = { Text("Más adelante podrás recibir recomendaciones y preparar un CV con la información que decidas proporcionar. Esta herramienta aún no está disponible") },
            confirmButton = { TextButton(onClick = { showCvInfo = false }) { Text("Entendido") } },
        )
    }

    LaunchedEffect(state.error) {
        if (state.error != null) scrollState.animateScrollTo(0)
    }

    if (showExperience && state.uid != null) {
        ExperienceEditor(state, account, onBack = { showExperience = false })
        return
    }

    Column(
        modifier = Modifier.verticalScroll(scrollState).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Perfil", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = onSettings) { Icon(Icons.Rounded.Settings, contentDescription = "Personalizar aplicación") }
        }
        Text(
            "Administra tu cuenta y personaliza tu experiencia",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AppNotice(
            if (isLocal) "Entorno de prueba local · usa únicamente datos ficticios"
            else "Entorno de desarrollo en la nube · usa únicamente datos ficticios",
        )

        if (state.loading) {
            AppLoadingState("Cargando cuenta")
        }
        state.error?.let { AccountError(it) }
        state.message?.let { AppNotice(it) }
        when {
            state.uid == null -> AuthForm(account, state.loading, state.error != null)
            state.profileLoadFailed -> ProfileUnavailable(account, state.loading) { confirmSignOut = true }
            else -> ProfileForm(state, account, onShowExperience = { showExperience = true }, onSignOut = { confirmSignOut = true })
        }
        if (state.uid != null) {
            Text("Tu desarrollo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            OutlinedButton(onClick = { showCvInfo = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Asistente para mi CV · Próximamente") }
        }
        Text("Tu espacio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        OutlinedButton(onClick = onSaved, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Oportunidades guardadas") }
        if (BuildConfig.DEBUG) {
            TextButton(onClick = onMascotDemo) { Text("Probar expresiones de Empu") }
        }
    }
}

@Composable
private fun AuthForm(account: AccountViewModel, loading: Boolean, hasError: Boolean) {
    var register by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var emailFocused by remember { mutableStateOf(false) }
    var passwordFocused by remember { mutableStateOf(false) }
    var googleBusy by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val scope = rememberCoroutineScope()
    val mood = accountEmpuMood(loading, hasError, emailFocused, passwordFocused, passwordVisible)

    Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.large) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Empu(mood, Modifier.align(Alignment.CenterHorizontally).size(96.dp))
            Text(
                if (register) "Crear cuenta" else "Iniciar sesión",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            if (register) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    enabled = !loading,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                enabled = !loading,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth().onFocusChanged { emailFocused = it.isFocused },
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                enabled = !loading,
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth().onFocusChanged { passwordFocused = it.isFocused },
            )
            AppPrimaryButton(
                label = if (register) "Crear cuenta" else "Entrar",
                onClick = {
                    if (register) account.register(name, email, password)
                    else account.signIn(email, password)
                },
                enabled = !loading,
            )
            OutlinedButton(
                onClick = {
                    googleBusy = true
                    scope.launch {
                        try {
                            val option = GetSignInWithGoogleOption.Builder(BuildConfig.FIREBASE_WEB_CLIENT_ID).build()
                            val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
                            val credential = credentialManager.getCredential(context, request).credential
                            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                account.signInWithGoogle(GoogleIdTokenCredential.createFrom(credential.data).idToken)
                            } else {
                                account.reportGoogleError("No se pudo leer la credencial de Google")
                            }
                        } catch (_: GetCredentialCancellationException) {
                        } catch (_: NoCredentialException) {
                            account.reportGoogleError("No se encontró una cuenta de Google disponible en este dispositivo")
                        } catch (exception: CancellationException) {
                            throw exception
                        } catch (_: Exception) {
                            account.reportGoogleError("No se pudo iniciar sesión con Google, inténtalo nuevamente")
                        } finally {
                            googleBusy = false
                        }
                    }
                },
                enabled = !loading && !googleBusy && BuildConfig.FIREBASE_WEB_CLIENT_ID.isNotBlank(),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) { Text(if (BuildConfig.FIREBASE_WEB_CLIENT_ID.isBlank()) "Google no está configurado en este entorno" else "Continuar con Google") }
            if (!register) {
                TextButton(enabled = !loading, onClick = { account.resetPassword(email) }) { Text("Recuperar contraseña") }
            }
            TextButton(enabled = !loading, onClick = { register = !register; password = "" }) {
                Text(if (register) "Ya tengo una cuenta" else "Crear una cuenta")
            }
        }
    }
}

@Composable
private fun ProfileForm(state: AccountUiState, account: AccountViewModel, onShowExperience: () -> Unit, onSignOut: () -> Unit) {
    var editing by rememberSaveable(state.uid, state.needsProfile) { mutableStateOf(state.needsProfile) }
    var name by rememberSaveable(state.uid, state.displayName) { mutableStateOf(state.displayName) }
    LaunchedEffect(state.loading) {
        if (!state.loading && state.error == null && !state.needsProfile) editing = false
    }

    Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.large) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (state.needsProfile) "Completa tu perfil" else "Tu cuenta",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Empu(EmpuMood.HAPPY, Modifier.size(54.dp))
            }
            if (editing) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    enabled = !state.loading,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = { account.saveProfile(name) },
                    enabled = !state.loading,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) { Text("Guardar perfil") }
                if (!state.needsProfile) {
                    TextButton(enabled = !state.loading, onClick = { name = state.displayName; editing = false }) { Text("Cancelar") }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(state.displayName.trim().firstOrNull()?.uppercase() ?: "", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(state.displayName, style = MaterialTheme.typography.titleMedium)
                        Text(state.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                OutlinedButton(enabled = !state.loading, onClick = { editing = true }) { Text("Editar nombre") }
                OutlinedButton(enabled = !state.loading, onClick = onShowExperience, modifier = Modifier.fillMaxWidth()) { Text("Habilidades y experiencia") }
            }
            TextButton(enabled = !state.loading, onClick = onSignOut) { Text("Cerrar sesión") }
        }
    }
}

@Composable
private fun ExperienceEditor(state: AccountUiState, account: AccountViewModel, onBack: () -> Unit) {
    var skills by rememberSaveable(state.uid) { mutableStateOf(state.skills) }
    var experience by rememberSaveable(state.uid) { mutableStateOf(state.experience) }
    var availability by rememberSaveable(state.uid) { mutableStateOf(state.availability) }
    var confirmDiscard by rememberSaveable { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    val hasDraft = skills != state.skills || experience != state.experience || availability != state.availability
    val leave = { if (!state.loading) { if (hasDraft) confirmDiscard = true else onBack() } }
    BackHandler { leave() }
    LaunchedEffect(state.loading, state.error, saving) {
        if (saving && !state.loading) {
            saving = false
            if (state.error == null) onBack()
        }
    }
    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            icon = { Empu(EmpuMood.THINKING, Modifier.size(112.dp)) },
            title = { Text("¿Descartar los cambios?") },
            text = { Text("Los cambios que no guardaste se perderán") },
            confirmButton = { TextButton(onClick = { confirmDiscard = false; onBack() }) { Text("Salir") } },
            dismissButton = { TextButton(onClick = { confirmDiscard = false }) { Text("Seguir revisando") } },
        )
    }
    Column(Modifier.verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TextButton(onClick = leave, enabled = !state.loading) { Text("Volver al perfil") }
        Text("Habilidades y experiencia", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        AppNotice("Estos datos son privados y todavía no se comparten con quienes publican oportunidades")
        state.error?.let { AccountError(it) }
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Cuéntanos sobre ti", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("Tus conocimientos también pueden venir de estudios o proyectos personales", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(value = skills, onValueChange = { skills = it.take(500) }, enabled = !state.loading, label = { Text("Habilidades") }, placeholder = { Text("Ejemplo: edición de video") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = experience, onValueChange = { experience = it.take(1500) }, enabled = !state.loading, label = { Text("Proyectos o experiencia") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = availability, onValueChange = { availability = it.take(200) }, enabled = !state.loading, label = { Text("Disponibilidad") }, placeholder = { Text("Ejemplo: fines de semana") }, modifier = Modifier.fillMaxWidth())
                AppPrimaryButton("Guardar cambios", enabled = hasDraft && !state.loading, onClick = { saving = true; account.saveExperience(skills, experience, availability) })
                if (state.loading) AppLoadingState("Guardando perfil")
            }
        }
    }
}

@Composable
private fun ProfileUnavailable(account: AccountViewModel, loading: Boolean, onSignOut: () -> Unit) {
    OutlinedButton(enabled = !loading, onClick = account::retryProfile) { Text("Reintentar") }
    TextButton(enabled = !loading, onClick = onSignOut) { Text("Cerrar sesión") }
}

@Composable
private fun AccountError(message: String) {
    AppNotice(message, isError = true)
}
