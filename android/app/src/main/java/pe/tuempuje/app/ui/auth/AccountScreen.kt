package pe.tuempuje.app.ui.auth

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import pe.tuempuje.app.data.SavedDatabase
import com.google.firebase.firestore.FirebaseFirestore
import pe.tuempuje.app.auth.AccountUiState
import pe.tuempuje.app.auth.AccountViewModel
import pe.tuempuje.app.auth.AccountViewModelFactory
import pe.tuempuje.app.ui.LocalAppPreferences
import pe.tuempuje.app.ui.components.AppNotice
import pe.tuempuje.app.ui.components.AppPrimaryButton
import pe.tuempuje.app.ui.components.AppLoadingState
import pe.tuempuje.app.ui.components.Empu
import pe.tuempuje.app.ui.components.EmpuMood
import pe.tuempuje.app.BuildConfig
import pe.tuempuje.app.R
import pe.tuempuje.app.profile.ResumePdf
import pe.tuempuje.app.profile.ResumeDraftStore
import pe.tuempuje.app.data.FirebaseServices
import pe.tuempuje.app.ui.ResumeAssistantScreen
import pe.tuempuje.app.ui.CvOpportunityContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

@Composable
fun AccountScreen(auth: FirebaseAuth, firestore: FirebaseFirestore, onSettings: () -> Unit, onSaved: () -> Unit, onChoosePurpose: () -> Unit = {}, resetCode: String? = null, onResetConsumed: () -> Unit = {}, authGate: Boolean = false, onHelp: () -> Unit = {}, openWorkProfile: Boolean = false, onWorkProfileOpened: () -> Unit = {}, cvOpportunity: CvOpportunityContext? = null, onCvClosed: () -> Unit = {}) {
    val factory = remember(auth, firestore) { AccountViewModelFactory(auth, firestore) }
    val account: AccountViewModel = viewModel(factory = factory)
    val state by account.state.collectAsState()
    var showExperience by rememberSaveable(state.uid) { mutableStateOf(false) }
    var showRecovery by rememberSaveable { mutableStateOf(false) }
    val isEmployer = LocalAppPreferences.current?.purpose == "employer"
    LaunchedEffect(openWorkProfile, state.uid, state.profileLoadFailed, state.loading) {
        if (openWorkProfile && state.uid != null && !state.profileLoadFailed && !state.loading && !isEmployer) {
            showExperience = true
            onWorkProfileOpened()
        }
    }
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val scope = rememberCoroutineScope()
    var pdfMessage by rememberSaveable(state.uid) { mutableStateOf<String?>(null) }
    var pdfBusy by remember { mutableStateOf(false) }
    var pdfProfile by remember { mutableStateOf<AccountUiState?>(null) }
    val createPdf = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { destination ->
        val profile = pdfProfile
        pdfProfile = null
        if (destination != null && profile != null) scope.launch {
            pdfBusy = true
            pdfMessage = try {
                withContext(Dispatchers.IO) { ResumePdf.write(context.contentResolver, destination, profile) }
                "CV guardado en el lugar que elegiste"
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                "No se pudo guardar el CV, inténtalo nuevamente"
            } finally {
                pdfBusy = false
            }
        }
    }
    var confirmSignOut by rememberSaveable(state.uid) { mutableStateOf(false) }
    var confirmDelete by rememberSaveable(state.uid) { mutableStateOf(false) }
    var deletePassword by remember { mutableStateOf("") }
    var deleteGoogleBusy by remember { mutableStateOf(false) }
    val hasPassword = auth.currentUser?.providerData?.any { it.providerId == "password" } == true
    val hasGoogle = auth.currentUser?.providerData?.any { it.providerId == "google.com" } == true
    val deleted: (String) -> Unit = { uid ->
        scope.launch {
            SavedDatabase.get(context).saved().clearAccount(uid)
            ResumeDraftStore(context).clearAccount(uid)
            runCatching { credentialManager.clearCredentialState(ClearCredentialStateRequest()) }
        }
    }
    var showCvAssistant by rememberSaveable(state.uid) { mutableStateOf(false) }
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
    if (confirmDelete && state.uid != null) {
        AlertDialog(
            onDismissRequest = { if (!state.loading) { confirmDelete = false; deletePassword = "" } },
            icon = { Empu(EmpuMood.SAD, Modifier.size(112.dp)) },
            title = { Text("Eliminar cuenta") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Se eliminarán tu perfil, tus publicaciones y postulaciones, esta acción no se puede deshacer")
                    if (BuildConfig.FIREBASE_LOCAL) Text("Confirma tu identidad para continuar") else Text("La eliminación de cuentas aún no está disponible")
                    if (BuildConfig.FIREBASE_LOCAL && hasPassword) OutlinedTextField(value = deletePassword, onValueChange = { deletePassword = it },
                        label = { Text("Contraseña") }, singleLine = true, visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
                }
            },
            confirmButton = {
                Column {
                    if (BuildConfig.FIREBASE_LOCAL && hasPassword) TextButton(enabled = !state.loading && deletePassword.isNotEmpty(), onClick = {
                        val credential = EmailAuthProvider.getCredential(state.email, deletePassword)
                        deletePassword = ""; confirmDelete = false; account.deleteAccount(credential, deleted)
                    }) { Text("Eliminar con contraseña") }
                    if (BuildConfig.FIREBASE_LOCAL && hasGoogle) TextButton(enabled = !state.loading && !deleteGoogleBusy, onClick = {
                        deleteGoogleBusy = true
                        scope.launch {
                            try {
                                val option = GetSignInWithGoogleOption.Builder(BuildConfig.FIREBASE_WEB_CLIENT_ID).build()
                                val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
                                val credential = credentialManager.getCredential(context, request).credential
                                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                    val token = GoogleIdTokenCredential.createFrom(credential.data).idToken
                                    confirmDelete = false
                                    account.deleteAccount(GoogleAuthProvider.getCredential(token, null), deleted)
                                } else account.reportGoogleError("No se pudo confirmar la cuenta de Google")
                            } catch (_: GetCredentialCancellationException) {
                            } catch (exception: CancellationException) { throw exception
                            } catch (_: Exception) { account.reportGoogleError("No se pudo confirmar la cuenta de Google")
                            } finally { deleteGoogleBusy = false }
                        }
                    }) { Text("Eliminar con Google") }
                }
            },
            dismissButton = { TextButton(enabled = !state.loading, onClick = { confirmDelete = false; deletePassword = "" }) { Text("Cancelar") } },
        )
    }
    LaunchedEffect(state.error) {
        if (state.error != null) scrollState.animateScrollTo(0)
    }

    LaunchedEffect(resetCode) {
        if (resetCode != null) {
            showRecovery = true
            account.verifyResetCode(resetCode)
            onResetConsumed()
        }
    }
    if (showRecovery) {
        RecoveryScreen(state, account, onBack = { showRecovery = false; account.clearRecovery() })
        return
    }
    if ((showCvAssistant || cvOpportunity != null) && state.uid != null) {
        ResumeAssistantScreen(state, FirebaseServices(auth, firestore, BuildConfig.FIREBASE_LOCAL), cvOpportunity) { showCvAssistant = false; onCvClosed() }
        return
    }
    if (showExperience && state.uid != null) {
        if (isEmployer) BusinessEditor(state, account, onBack = { showExperience = false })
        else ExperienceEditor(state, account, onBack = { showExperience = false })
        return
    }

    Column(
        modifier = Modifier.verticalScroll(scrollState).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(if (authGate) "Bienvenido a TuEmpuje" else "Perfil", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (!authGate) IconButton(onClick = onSettings) { Icon(Icons.Rounded.Settings, contentDescription = "Personalizar aplicación") }
        }
        Text(
            if (authGate) "Inicia sesión o crea tu cuenta para continuar" else "Administra tu cuenta y personaliza tu experiencia",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (state.loading) {
            AppLoadingState("Cargando cuenta")
        }
        state.error?.let { AccountError(it) }
        state.message?.let { AppNotice(it) }
        when {
            state.uid == null && auth.currentUser != null -> Unit
            state.uid != null && state.loading && state.displayName.isBlank() -> Unit
            state.uid == null -> AuthForm(account, state.loading, state.error != null, onRecover = { account.clearFeedback(); showRecovery = true })
            state.profileLoadFailed -> ProfileUnavailable(account, state.loading) { confirmSignOut = true }
            else -> ProfileForm(state, account, isEmployer, onShowExperience = { showExperience = true }, onSignOut = { confirmSignOut = true })
        }
        if (authGate && state.uid == null && auth.currentUser == null) TextButton(enabled = !state.loading, onClick = onChoosePurpose) { Text("Cambiar tipo de cuenta") }
        if (state.uid != null && !authGate) {
            TextButton(enabled = !state.loading, onClick = { confirmDelete = true }) { Text("Eliminar cuenta", color = MaterialTheme.colorScheme.error) }
            if (!isEmployer && !state.profileLoadFailed) {
                Text("Tu desarrollo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                OutlinedButton(enabled = !state.loading && !pdfBusy, onClick = {
                    pdfMessage = null
                    pdfProfile = state
                    createPdf.launch("TuEmpuje-CV.pdf")
                }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(if (pdfBusy) "Guardando CV…" else "Guardar mi CV en PDF") }
                Text("El archivo se crea con los datos de tu perfil y se guarda en tu dispositivo, no se envía automáticamente", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                pdfMessage?.let { AppNotice(it) }
                OutlinedButton(onClick = { showCvAssistant = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Crear CV con Empu") }
            }
        }
        if (!authGate) {
            Text(if (isEmployer) "Cuenta de empresa" else "Cuenta personal · puedes buscar trabajo y pedir ayuda", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Tu espacio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (!isEmployer) OutlinedButton(onClick = onSaved, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Oportunidades guardadas") }
            OutlinedButton(onClick = onHelp, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Ayuda y seguridad") }
        }
    }
}

@Composable
private fun AuthForm(account: AccountViewModel, loading: Boolean, hasError: Boolean, onRecover: () -> Unit) {
    var register by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var dni by rememberSaveable { mutableStateOf("") }
    var documentType by rememberSaveable { mutableStateOf("DNI") }
    var phone by rememberSaveable { mutableStateOf("") }
    var nationality by rememberSaveable { mutableStateOf("") }
    var district by rememberSaveable { mutableStateOf("") }
    var organization by rememberSaveable { mutableStateOf("") }
    val preferences = LocalAppPreferences.current
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var attempted by rememberSaveable { mutableStateOf(false) }
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
            Empu(mood, Modifier.align(Alignment.CenterHorizontally).size(120.dp))
            Text(
                if (register) "Crear cuenta" else "Iniciar sesión",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            if (register) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(80) },
                    label = { Text("Nombre y apellido") },
                    enabled = !loading,
                    singleLine = true,
                    isError = attempted && name.trim().length !in 2..80,
                    supportingText = if (attempted && name.trim().length !in 2..80) { { Text("Ingresa tu nombre completo") } } else null,
                    modifier = Modifier.fillMaxWidth(),
                )
                DocumentField(documentType, dni, !loading, onTypeChange = { documentType = it; dni = "" }, onValueChange = { dni = it }, showError = attempted)
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it.filter(Char::isDigit).take(9) },
                    label = { Text("Celular") }, enabled = !loading, singleLine = true,
                    isError = attempted && !phone.matches(Regex("9[0-9]{8}")),
                    supportingText = if (attempted && !phone.matches(Regex("9[0-9]{8}"))) { { Text("Ingresa un celular peruano de 9 dígitos") } } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(value = nationality, onValueChange = { nationality = it.take(80) }, label = { Text("Nacionalidad") }, enabled = !loading, singleLine = true,
                    isError = attempted && nationality.trim().length !in 2..80,
                    supportingText = if (attempted && nationality.trim().length !in 2..80) { { Text("Ingresa tu nacionalidad") } } else null,
                    modifier = Modifier.fillMaxWidth())
                if (preferences?.purpose == "employer") {
                    OutlinedTextField(value = district, onValueChange = { district = it.take(100) }, label = { Text("Distrito donde necesitas apoyo") }, enabled = !loading, singleLine = true,
                        isError = attempted && district.trim().length !in 2..100,
                        supportingText = if (attempted && district.trim().length !in 2..100) { { Text("Ingresa tu distrito") } } else null,
                        modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = organization, onValueChange = { organization = it.take(100) }, label = { Text("Nombre de la empresa") }, enabled = !loading, singleLine = true, isError = attempted && organization.trim().length !in 2..100, supportingText = if (attempted && organization.trim().length !in 2..100) { { Text("Ingresa el nombre de la empresa") } } else null, modifier = Modifier.fillMaxWidth())
                }
                Text("Tu documento y celular se guardan en tu perfil privado, su formato no verifica tu identidad", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                enabled = !loading,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = attempted && !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches(),
                supportingText = if (attempted && !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) { { Text("Ingresa un correo válido") } } else null,
                modifier = Modifier.fillMaxWidth().onFocusChanged { emailFocused = it.isFocused },
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                enabled = !loading,
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = attempted && password.length < 6,
                supportingText = if (attempted && password.length < 6) { { Text("Debe tener al menos 6 caracteres") } } else null,
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
            if (register) {
                OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, enabled = !loading,
                    label = { Text("Confirmar contraseña") }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = attempted && (confirmPassword.isEmpty() || confirmPassword != password),
                    supportingText = if (attempted && (confirmPassword.isEmpty() || confirmPassword != password)) { { Text("Las contraseñas deben coincidir") } } else null,
                    modifier = Modifier.fillMaxWidth())
            }
            AppPrimaryButton(
                label = if (register) "Crear cuenta" else "Entrar",
                onClick = {
                    attempted = true
                    if (register) {
                        if (confirmPassword.isNotEmpty() && confirmPassword == password) account.register(name, email, password, dni, phone, nationality, preferences?.purpose, preferences?.interests.orEmpty(), district, organization, documentType)
                    } else account.signIn(email, password)
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
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Color(0xFF1F1F1F), disabledContainerColor = Color.White, disabledContentColor = Color(0xFF747775)),
                border = BorderStroke(1.dp, Color(0xFF747775)),
            ) {
                Image(painter = painterResource(R.drawable.google_g), contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text(if (BuildConfig.FIREBASE_WEB_CLIENT_ID.isBlank()) "Acceso con Google no disponible por ahora" else "Continuar con Google")
            }
            if (!register) {
                TextButton(enabled = !loading, onClick = onRecover) { Text("Recuperar contraseña") }
            }
            TextButton(enabled = !loading, onClick = { register = !register; password = ""; confirmPassword = ""; attempted = false; account.clearFeedback() }) {
                Text(if (register) "Ya tengo una cuenta" else "Crear una cuenta")
            }
        }
    }
}

private fun documentLabel(type: String) = when (type) {
    "CE" -> "Carné de extranjería"
    "PASAPORTE" -> "Pasaporte"
    else -> "DNI"
}

@Composable
private fun DocumentField(type: String, value: String, enabled: Boolean, onTypeChange: (String) -> Unit, onValueChange: (String) -> Unit, showError: Boolean = false) {
    var expanded by remember { mutableStateOf(false) }
    Text("Tipo de documento", style = MaterialTheme.typography.labelLarge)
    Box {
        OutlinedButton(onClick = { expanded = true }, enabled = enabled, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(documentLabel(type)) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf("DNI", "CE", "PASAPORTE").forEach { option ->
                DropdownMenuItem(text = { Text(documentLabel(option)) }, onClick = { expanded = false; if (option != type) onTypeChange(option) })
            }
        }
    }
    OutlinedTextField(
        value = value,
        onValueChange = { raw ->
            val clean = if (type == "PASAPORTE") raw.uppercase(java.util.Locale.ROOT).filter { it in 'A'..'Z' || it in '0'..'9' }.take(15)
            else raw.filter { it in '0'..'9' }.take(if (type == "DNI") 8 else 11)
            onValueChange(clean)
        },
        label = { Text(if (type == "DNI") "Número de DNI" else "Número de ${documentLabel(type).lowercase()}") },
        enabled = enabled, singleLine = true,
        isError = showError && pe.tuempuje.app.auth.identityError(value, "900000000", type) != null,
        supportingText = if (showError) pe.tuempuje.app.auth.identityError(value, "900000000", type)?.let { message -> { Text(message) } } else null,
        keyboardOptions = KeyboardOptions(keyboardType = if (type == "PASAPORTE") KeyboardType.Ascii else KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun RecoveryScreen(state: AccountUiState, account: AccountViewModel, onBack: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var mismatch by remember { mutableStateOf(false) }
    BackHandler(enabled = !state.loading) { onBack() }
    Column(Modifier.verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TextButton(onClick = onBack, enabled = !state.loading) { Text("Volver a iniciar sesión") }
        Empu(if (state.message != null) EmpuMood.SUCCESS else EmpuMood.THINKING, Modifier.align(Alignment.CenterHorizontally).size(112.dp))
        Text("Recuperar contraseña", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        state.error?.let { AccountError(it) }
        state.message?.let { AppNotice(it) }
        when {
            state.recoveryComplete -> {
                AppNotice("Contraseña actualizada, ya puedes iniciar sesión")
                AppPrimaryButton("Volver", onClick = onBack)
            }
            state.recoveryEmail != null -> {
                Text("Elige una contraseña nueva para ${state.recoveryEmail}")
                OutlinedTextField(value = password, onValueChange = { password = it; mismatch = false }, label = { Text("Contraseña nueva") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), enabled = !state.loading, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = confirm, onValueChange = { confirm = it; mismatch = false }, label = { Text("Repite la contraseña") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), enabled = !state.loading, modifier = Modifier.fillMaxWidth())
                if (mismatch) AccountError("Las contraseñas no coinciden")
                AppPrimaryButton("Guardar contraseña", onClick = { if (password == confirm) account.confirmResetPassword(password) else mismatch = true }, enabled = !state.loading)
            }
            else -> {
                Text("Escribe el correo de tu cuenta, te enviaremos un enlace para elegir una contraseña nueva")
                OutlinedTextField(value = email, onValueChange = { email = it.take(254) }, label = { Text("Correo electrónico") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), enabled = !state.loading, singleLine = true, modifier = Modifier.fillMaxWidth())
                AppPrimaryButton("Enviar enlace", onClick = { account.resetPassword(email) }, enabled = !state.loading)
                if (state.message != null) Text("Abre el enlace del correo para cambiar la contraseña", style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (state.loading) AppLoadingState("Verificando información")
    }
}

@Composable
private fun ProfileForm(state: AccountUiState, account: AccountViewModel, isEmployer: Boolean, onShowExperience: () -> Unit, onSignOut: () -> Unit) {
    var editing by rememberSaveable(state.uid, state.needsProfile) { mutableStateOf(state.needsProfile) }
    var name by rememberSaveable(state.uid, state.displayName) { mutableStateOf(state.displayName) }
    var dni by rememberSaveable(state.uid, state.dni) { mutableStateOf(state.dni) }
    var documentType by rememberSaveable(state.uid, state.documentType) { mutableStateOf(state.documentType) }
    var phone by rememberSaveable(state.uid, state.phone) { mutableStateOf(state.phone) }
    var nationality by rememberSaveable(state.uid, state.nationality) { mutableStateOf(state.nationality) }
    var district by rememberSaveable(state.uid, state.district) { mutableStateOf(state.district) }
    var organization by rememberSaveable(state.uid, state.organization) { mutableStateOf(state.organization) }
    val preferences = LocalAppPreferences.current
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
                if (preferences?.showMascot != false) Empu(EmpuMood.HAPPY, Modifier.size(88.dp))
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
                DocumentField(documentType, dni, !state.loading, onTypeChange = { documentType = it; dni = "" }, onValueChange = { dni = it })
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it.filter(Char::isDigit).take(9) },
                    label = { Text("Celular") }, enabled = !state.loading, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(value = nationality, onValueChange = { nationality = it.take(80) }, label = { Text("Nacionalidad") }, enabled = !state.loading, singleLine = true, modifier = Modifier.fillMaxWidth())
                if (isEmployer) {
                    OutlinedTextField(value = district, onValueChange = { district = it.take(100) }, label = { Text("Distrito donde necesitas apoyo") }, enabled = !state.loading, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = organization, onValueChange = { organization = it.take(100) }, label = { Text("Nombre de la empresa") }, enabled = !state.loading, modifier = Modifier.fillMaxWidth())
                }
                Text("Tu documento y celular se guardan en tu perfil privado, su formato no verifica tu identidad", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(
                    onClick = { account.saveProfile(name, dni, phone, nationality, preferences?.purpose, preferences?.interests.orEmpty(), district, organization, documentType) },
                    enabled = !state.loading,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) { Text("Guardar perfil") }
                if (!state.needsProfile) {
                    TextButton(enabled = !state.loading, onClick = { name = state.displayName; dni = state.dni; documentType = state.documentType; phone = state.phone; nationality = state.nationality; district = state.district; organization = state.organization; editing = false }) { Text("Cancelar") }
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
                        if (state.nationality.isNotBlank()) Text(state.nationality, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${documentLabel(state.documentType)} ••••${state.dni.takeLast(4)} · ${state.phone}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                OutlinedButton(enabled = !state.loading, onClick = { editing = true }) { Text("Editar perfil") }
                OutlinedButton(enabled = !state.loading, onClick = onShowExperience, modifier = Modifier.fillMaxWidth()) { Text(if (isEmployer) "Datos para publicar" else "Mi perfil laboral") }
            }
            TextButton(enabled = !state.loading, onClick = onSignOut) { Text("Cerrar sesión") }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExperienceEditor(state: AccountUiState, account: AccountViewModel, onBack: () -> Unit) {
    var skills by rememberSaveable(state.uid) { mutableStateOf(state.skills) }
    var experience by rememberSaveable(state.uid) { mutableStateOf(state.experience) }
    var availability by rememberSaveable(state.uid) { mutableStateOf(state.availability) }
    var district by rememberSaveable(state.uid) { mutableStateOf(state.district) }
    val organization = state.organization
    var education by rememberSaveable(state.uid) { mutableStateOf(state.education) }
    var portfolio by rememberSaveable(state.uid) { mutableStateOf(state.portfolio) }
    var confirmDiscard by rememberSaveable { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    val hasDraft = skills != state.skills || experience != state.experience || availability != state.availability || district != state.district || organization != state.organization || education != state.education || portfolio != state.portfolio
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
        Text("Mi perfil laboral", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        AppNotice("Al postular se comparten tus datos laborales. Si adjuntas un CV, revisa también los datos de contacto que incluiste en el documento")
        state.error?.let { AccountError(it) }
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Habilidades y disponibilidad", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("Tus conocimientos también pueden venir de estudios o proyectos personales", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val completed = listOf(skills.trim().length >= 2, availability.trim().length >= 2).count { it }
                Text("Datos laborales para postular: $completed de 2", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(value = skills, onValueChange = { skills = it.take(500) }, enabled = !state.loading, label = { Text("Habilidades") }, placeholder = { Text("Ejemplo: edición de video") }, modifier = Modifier.fillMaxWidth())
                Text("Agrega habilidades o escríbelas con tus palabras", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Edición de video", "Atención al cliente", "Excel", "Programación", "Diseño").forEach { suggestion ->
                        val entries = skills.split(',').map { it.trim() }.filter { it.isNotEmpty() }
                        val selected = entries.any { it.equals(suggestion, ignoreCase = true) }
                        FilterChip(selected = selected, enabled = !state.loading, onClick = {
                            val updated = if (selected) entries.filterNot { it.equals(suggestion, ignoreCase = true) } else entries + suggestion
                            val result = updated.joinToString(", ")
                            if (result.length <= 500) skills = result
                        }, label = { Text(suggestion) })
                    }
                }
                OutlinedTextField(value = availability, onValueChange = { availability = it.take(200) }, enabled = !state.loading, label = { Text("Disponibilidad") }, placeholder = { Text("Ejemplo: fines de semana") }, modifier = Modifier.fillMaxWidth())
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Mañanas", "Tardes", "Fines de semana", "Por coordinar").forEach { suggestion ->
                        FilterChip(selected = availability == suggestion, enabled = !state.loading, onClick = { availability = suggestion }, label = { Text(suggestion) })
                    }
                }
                Text("Experiencia y proyectos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(value = experience, onValueChange = { experience = it.take(1500) }, enabled = !state.loading, label = { Text("Proyectos o experiencia") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                Text("Si aún no tienes experiencia puedes dejar este campo vacío", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Habilidades y disponibilidad son necesarias para postular, la experiencia es opcional", style = MaterialTheme.typography.bodySmall)
                Text("Formación y ubicación", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(value = district, onValueChange = { district = it.take(100) }, enabled = !state.loading, label = { Text("Distrito") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = education, onValueChange = { education = it.take(500) }, enabled = !state.loading, label = { Text("Estudios o formación") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = portfolio, onValueChange = { portfolio = it.take(500) }, enabled = !state.loading, label = { Text("Portafolio o proyectos") }, modifier = Modifier.fillMaxWidth())
                AppPrimaryButton("Guardar cambios", enabled = hasDraft && !state.loading, onClick = { saving = true; account.saveExperience(skills, experience, availability, district, organization, education, portfolio) })
                if (state.loading) AppLoadingState("Guardando perfil")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BusinessEditor(state: AccountUiState, account: AccountViewModel, onBack: () -> Unit) {
    val type = "NEGOCIO"
    var organization by rememberSaveable(state.uid) { mutableStateOf(state.organization) }
    var district by rememberSaveable(state.uid) { mutableStateOf(state.district) }
    var sector by rememberSaveable(state.uid) { mutableStateOf(state.businessSector) }
    var description by rememberSaveable(state.uid) { mutableStateOf(state.businessDescription) }
    var discard by rememberSaveable { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var attempted by rememberSaveable { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val changed = organization != state.organization || district != state.district || sector != state.businessSector || description != state.businessDescription
    val leave = { if (!state.loading) { if (changed) discard = true else onBack() } }
    BackHandler { leave() }
    LaunchedEffect(state.loading, state.error, saving) {
        if (saving && !state.loading) { saving = false; if (state.error == null) onBack() }
    }
    LaunchedEffect(state.error) { if (state.error != null) scrollState.animateScrollTo(0) }
    if (discard) AlertDialog(onDismissRequest = { discard = false }, title = { Text("¿Descartar los cambios?") },
        text = { Text("Los datos que no guardaste se perderán") },
        confirmButton = { TextButton(onClick = onBack) { Text("Descartar") } },
        dismissButton = { TextButton(onClick = { discard = false }) { Text("Seguir editando") } })
    Column(Modifier.verticalScroll(scrollState).padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TextButton(onClick = leave, enabled = !state.loading) { Text("Volver al perfil") }
        Text("Datos para publicar", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Cuéntanos desde dónde ofreces oportunidades", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        state.error?.let { AccountError(it) }
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Perfil de empresa", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(value = organization, onValueChange = { organization = it.take(100) }, enabled = !state.loading,
                    label = { Text(if (type == "NEGOCIO") "Nombre del negocio" else "Negocio u organización, opcional") }, singleLine = true,
                    isError = attempted && type == "NEGOCIO" && organization.trim().length !in 2..100,
                    supportingText = if (attempted && type == "NEGOCIO" && organization.trim().length !in 2..100) { { Text("Ingresa el nombre del negocio") } } else null,
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = district, onValueChange = { district = it.take(100) }, enabled = !state.loading,
                    label = { Text("Distrito") }, singleLine = true, isError = attempted && district.trim().length !in 2..100,
                    supportingText = if (attempted && district.trim().length !in 2..100) { { Text("Ingresa tu distrito") } } else null,
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = sector, onValueChange = { sector = it.take(80) }, enabled = !state.loading,
                    label = { Text("Rubro, opcional") }, placeholder = { Text("Ejemplo: fotografía, comercio o servicios") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it.take(500) }, enabled = !state.loading,
                    label = { Text("A qué te dedicas, opcional") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                Text("Tus datos son privados, al crear o editar una oportunidad podrás elegir si compartes el nombre, el rubro y la descripción del negocio", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Los cambios no actualizan automáticamente las publicaciones anteriores", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                AppPrimaryButton("Guardar datos", enabled = changed && !state.loading, onClick = { attempted = true; saving = true; account.saveBusinessProfile(type, organization, district, sector, description) })
                if (state.loading) AppLoadingState("Guardando datos")
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
