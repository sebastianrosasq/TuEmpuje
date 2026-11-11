package pe.tuempuje.app.ui.auth

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
fun AccountScreen(auth: FirebaseAuth, firestore: FirebaseFirestore) {
    val factory = remember(auth, firestore) { AccountViewModelFactory(auth, firestore) }
    val account: AccountViewModel = viewModel(factory = factory)
    val state by account.state.collectAsState()

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
            "Entorno de prueba local · usa únicamente datos ficticios",
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
            else -> ProfileForm(state, account)
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
            TextButton(enabled = !loading, onClick = { register = !register; password = "" }) {
                Text(if (register) "Ya tengo una cuenta" else "Crear una cuenta")
            }
        }
    }
}

@Composable
private fun ProfileForm(state: AccountUiState, account: AccountViewModel) {
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
            }
            TextButton(enabled = !state.loading, onClick = account::signOut) { Text("Cerrar sesión") }
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
