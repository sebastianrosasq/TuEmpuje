package pe.tuempuje.app.ui.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import pe.tuempuje.app.auth.AccountUiState
import pe.tuempuje.app.auth.AccountViewModel
import pe.tuempuje.app.auth.AccountViewModelFactory

@Composable
fun AccountScreen(auth: FirebaseAuth, firestore: FirebaseFirestore, isLocal: Boolean) {
    val factory = remember(auth, firestore) { AccountViewModelFactory(auth, firestore) }
    val account: AccountViewModel = viewModel(factory = factory)
    val state by account.state.collectAsState()
    var showExperience by rememberSaveable(state.uid) { mutableStateOf(false) }

    if (showExperience && state.uid != null) {
        BackHandler { showExperience = false }
        ExperiencePreview(onBack = { showExperience = false })
        return
    }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Perfil", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "Las oportunidades y postulaciones siguen siendo simuladas en esta versión",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            if (isLocal) "Entorno de prueba local · usa únicamente datos ficticios"
            else "Entorno de desarrollo en la nube · usa únicamente datos ficticios",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (state.loading) {
            CircularProgressIndicator()
            Text("Cargando cuenta", style = MaterialTheme.typography.bodyMedium)
        }
        state.error?.let { AccountError(it) }
        when {
            state.uid == null -> AuthForm(account, state.loading)
            state.profileLoadFailed -> ProfileUnavailable(account, state.loading)
            else -> ProfileForm(state, account, onShowExperience = { showExperience = true })
        }
    }
}

@Composable
private fun AuthForm(account: AccountViewModel, loading: Boolean) {
    var register by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.large) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                enabled = !loading,
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = {
                    if (register) account.register(name, email, password)
                    else account.signIn(email, password)
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) { Text(if (register) "Crear cuenta" else "Entrar") }
            OutlinedButton(
                onClick = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) { Text("Continuar con Google · próximamente") }
            if (!register) {
                TextButton(enabled = false, onClick = {}) { Text("Recuperar contraseña · próximamente") }
            }
            TextButton(enabled = !loading, onClick = { register = !register; password = "" }) {
                Text(if (register) "Ya tengo una cuenta" else "Crear una cuenta")
            }
        }
    }
}

@Composable
private fun ProfileForm(state: AccountUiState, account: AccountViewModel, onShowExperience: () -> Unit) {
    var editing by rememberSaveable(state.uid, state.needsProfile) { mutableStateOf(state.needsProfile) }
    var name by rememberSaveable(state.uid, state.displayName) { mutableStateOf(state.displayName) }
    LaunchedEffect(state.loading) {
        if (!state.loading && state.error == null && !state.needsProfile) editing = false
    }

    Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.large) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                if (state.needsProfile) "Completa tu perfil" else "Tu cuenta",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
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
                Text(state.displayName, style = MaterialTheme.typography.titleMedium)
                Text(state.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(enabled = !state.loading, onClick = { editing = true }) { Text("Editar nombre") }
                OutlinedButton(enabled = !state.loading, onClick = onShowExperience, modifier = Modifier.fillMaxWidth()) { Text("Habilidades y experiencia") }
            }
            TextButton(enabled = !state.loading, onClick = account::signOut) { Text("Cerrar sesión") }
        }
    }
}

@Composable
private fun ExperiencePreview(onBack: () -> Unit) {
    var skills by rememberSaveable { mutableStateOf("") }
    var experience by rememberSaveable { mutableStateOf("") }
    var availability by rememberSaveable { mutableStateOf("") }
    Column(Modifier.verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TextButton(onClick = onBack) { Text("Volver al perfil") }
        Text("Habilidades y experiencia", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Vista de diseño · estos datos todavía no se guardan", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Cuéntanos sobre ti", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("Tus conocimientos también pueden venir de estudios o proyectos personales", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(value = skills, onValueChange = { skills = it }, label = { Text("Habilidades") }, placeholder = { Text("Ejemplo: edición de video") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = experience, onValueChange = { experience = it }, label = { Text("Proyectos o experiencia") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = availability, onValueChange = { availability = it }, label = { Text("Disponibilidad") }, placeholder = { Text("Ejemplo: fines de semana") }, modifier = Modifier.fillMaxWidth())
                Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Guardar · próximamente") }
            }
        }
    }
}

@Composable
private fun ProfileUnavailable(account: AccountViewModel, loading: Boolean) {
    OutlinedButton(enabled = !loading, onClick = account::retryProfile) { Text("Reintentar") }
    TextButton(enabled = !loading, onClick = account::signOut) { Text("Cerrar sesión") }
}

@Composable
private fun AccountError(message: String) {
    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.medium) {
        Text(
            message,
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
