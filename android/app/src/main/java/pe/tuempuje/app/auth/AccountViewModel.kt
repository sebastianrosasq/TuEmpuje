package pe.tuempuje.app.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

data class AccountUiState(
    val loading: Boolean = true,
    val uid: String? = null,
    val email: String = "",
    val displayName: String = "",
    val skills: String = "",
    val experience: String = "",
    val availability: String = "",
    val needsProfile: Boolean = false,
    val profileLoadFailed: Boolean = false,
    val error: String? = null,
    val message: String? = null,
)

class AccountViewModel(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : ViewModel() {
    private val mutableState = MutableStateFlow(AccountUiState())
    val state: StateFlow<AccountUiState> = mutableState.asStateFlow()
    private var operationActive = false
    private var revision = 0
    private val unconfirmedProfileMessage = "No se confirmó el guardado del perfil, el servicio de prueba podría no estar disponible y la operación podría completarse al reconectar, puedes reintentar"

    private val authListener = FirebaseAuth.AuthStateListener { source ->
        if (!operationActive) refresh(source.currentUser)
    }

    init {
        auth.addAuthStateListener(authListener)
    }

    fun signIn(email: String, password: String) {
        val emailValue = email.trim()
        val validation = validateCredentials(emailValue, password)
        if (validation != null) {
            mutableState.value = mutableState.value.copy(error = validation)
            return
        }
        runOperation {
            val user = withTimeout(12_000) { auth.signInWithEmailAndPassword(emailValue, password).await() }.user
                ?: error("La sesión no se inició")
            loadProfile(user)
        }
    }

    fun resetPassword(email: String) {
        val cleanEmail = email.trim()
        if (cleanEmail.length > 254 || !Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            mutableState.value = mutableState.value.copy(error = "Ingresa un correo válido para recuperar tu cuenta", message = null)
            return
        }
        runOperation {
            withTimeout(12_000) { auth.sendPasswordResetEmail(cleanEmail).await() }
            mutableState.value = AccountUiState(loading = false, message = "Si la cuenta existe, recibirás un correo para recuperar el acceso")
        }
    }

    fun signInWithGoogle(idToken: String) {
        if (idToken.isBlank()) {
            reportGoogleError("No se pudo obtener la credencial de Google")
            return
        }
        runOperation {
            val user = withTimeout(12_000) {
                auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
            }.user ?: error("La sesión no se inició")
            val profile = withTimeout(12_000) { firestore.collection("users").document(user.uid).get(Source.SERVER).await() }
            if (profile.exists()) {
                mutableState.value = fromProfile(user, profile)
            } else {
                val name = user.displayName?.trim()?.take(80).orEmpty()
                if (validateName(name) == null && user.email != null) {
                    try {
                        writeProfile(user, name)
                        mutableState.value = signedIn(user, name)
                    } catch (exception: CancellationException) {
                        throw exception
                    } catch (exception: Exception) {
                        mutableState.value = signedIn(user, name).copy(needsProfile = true, error = unconfirmedProfileMessage)
                    }
                } else {
                    mutableState.value = signedIn(user).copy(needsProfile = true)
                }
            }
        }
    }

    fun reportGoogleError(message: String) {
        mutableState.value = mutableState.value.copy(error = message, message = null)
    }

    fun register(name: String, email: String, password: String) {
        val cleanName = name.trim()
        val emailValue = email.trim()
        val validation = validateName(cleanName) ?: validateCredentials(emailValue, password)
        if (validation != null) {
            mutableState.value = mutableState.value.copy(error = validation)
            return
        }
        runOperation(
            timeoutMessage = "No se confirmó la creación de la cuenta, comprueba el servicio de prueba e intenta iniciar sesión antes de registrarte otra vez",
        ) {
            val user = withTimeout(12_000) { auth.createUserWithEmailAndPassword(emailValue, password).await() }.user
                ?: error("No se creó la cuenta")
            try {
                writeProfile(user, cleanName)
                mutableState.value = signedIn(user, cleanName)
            } catch (exception: TimeoutCancellationException) {
                mutableState.value = signedIn(user).copy(
                    displayName = cleanName,
                    needsProfile = true,
                    error = unconfirmedProfileMessage,
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                mutableState.value = signedIn(user).copy(
                    displayName = cleanName,
                    needsProfile = true,
                    error = unconfirmedProfileMessage,
                )
            }
        }
    }

    fun saveExperience(skills: String, experience: String, availability: String) {
        val cleanSkills = skills.trim()
        val cleanExperience = experience.trim()
        val cleanAvailability = availability.trim()
        if (cleanSkills.length > 500 || cleanExperience.length > 1500 || cleanAvailability.length > 200) {
            mutableState.value = mutableState.value.copy(error = "Revisa la longitud de los datos del perfil")
            return
        }
        val user = auth.currentUser ?: return
        val previous = mutableState.value
        runOperation {
            try {
                withTimeout(12_000) {
                    firestore.collection("users").document(user.uid).set(
                        mapOf("skills" to cleanSkills, "experience" to cleanExperience, "availability" to cleanAvailability),
                        SetOptions.merge(),
                    ).await()
                }
                mutableState.value = previous.copy(loading = false, skills = cleanSkills, experience = cleanExperience, availability = cleanAvailability, error = null)
            } catch (exception: TimeoutCancellationException) {
                mutableState.value = previous.copy(loading = false, error = unconfirmedProfileMessage)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                mutableState.value = previous.copy(loading = false, error = "No se pudieron guardar los datos del perfil, revisa tu conexión")
            }
        }
    }

    fun saveProfile(name: String) {
        val cleanName = name.trim()
        val validation = validateName(cleanName)
        if (validation != null) {
            mutableState.value = mutableState.value.copy(error = validation)
            return
        }
        val user = auth.currentUser ?: run {
            mutableState.value = AccountUiState(loading = false, error = "Inicia sesión para guardar tu perfil")
            return
        }
        val previous = mutableState.value
        runOperation {
            try {
                writeProfile(user, cleanName)
                mutableState.value = previous.copy(loading = false, displayName = cleanName, needsProfile = false, error = null)
            } catch (exception: TimeoutCancellationException) {
                mutableState.value = previous.copy(loading = false, error = unconfirmedProfileMessage)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                mutableState.value = previous.copy(loading = false, error = unconfirmedProfileMessage)
            }
        }
    }

    fun retryProfile() {
        refresh(auth.currentUser)
    }

    fun signOut() {
        revision++
        operationActive = false
        auth.signOut()
        mutableState.value = AccountUiState(loading = false)
    }

    private fun refresh(user: FirebaseUser?) {
        val request = ++revision
        if (user == null) {
            mutableState.value = AccountUiState(loading = false)
            return
        }
        mutableState.value = signedIn(user).copy(loading = true)
        viewModelScope.launch {
            try {
                val profile = withTimeout(12_000) {
                    firestore.collection("users").document(user.uid).get(Source.SERVER).await()
                }
                if (request != revision || auth.currentUser?.uid != user.uid) return@launch
                if (profile.metadata.hasPendingWrites()) {
                    mutableState.value = signedIn(user).copy(profileLoadFailed = true, error = unconfirmedProfileMessage)
                } else if (profile.exists()) {
                    mutableState.value = fromProfile(user, profile)
                } else {
                    mutableState.value = signedIn(user).copy(needsProfile = true)
                }
            } catch (exception: TimeoutCancellationException) {
                if (request != revision || auth.currentUser?.uid != user.uid) return@launch
                mutableState.value = signedIn(user).copy(
                    profileLoadFailed = true,
                    error = "No se pudo consultar el perfil, comprueba la conexión y el servicio de prueba y reintenta",
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                if (request != revision || auth.currentUser?.uid != user.uid) return@launch
                mutableState.value = signedIn(user).copy(
                    profileLoadFailed = true,
                    error = "No se pudo consultar el perfil, comprueba la conexión y el servicio de prueba y reintenta",
                )
            }
        }
    }

    private suspend fun loadProfile(user: FirebaseUser) {
        val profile = withTimeout(12_000) { firestore.collection("users").document(user.uid).get(Source.SERVER).await() }
        mutableState.value = if (profile.metadata.hasPendingWrites()) {
            signedIn(user).copy(profileLoadFailed = true, error = unconfirmedProfileMessage)
        } else if (profile.exists()) {
            fromProfile(user, profile)
        } else {
            signedIn(user).copy(needsProfile = true)
        }
    }

    private suspend fun writeProfile(user: FirebaseUser, name: String) {
        val email = user.email ?: error("La cuenta no tiene correo")
        withTimeout(12_000) {
            firestore.collection("users").document(user.uid).set(
                mapOf("uid" to user.uid, "displayName" to name, "email" to email), SetOptions.merge(),
            ).await()
        }
    }

    private fun runOperation(
        timeoutMessage: String = "No se pudo confirmar la operación, comprueba la conexión y el servicio de prueba y reintenta",
        block: suspend () -> Unit,
    ) {
        if (operationActive) return
        operationActive = true
        revision++
        mutableState.value = mutableState.value.copy(loading = true, error = null, message = null)
        viewModelScope.launch {
            try {
                block()
            } catch (exception: TimeoutCancellationException) {
                val user = auth.currentUser
                mutableState.value = if (user == null) {
                    AccountUiState(loading = false, error = timeoutMessage)
                } else {
                    signedIn(user).copy(profileLoadFailed = true, error = timeoutMessage)
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                val user = auth.currentUser
                mutableState.value = if (user == null) {
                    AccountUiState(loading = false, error = errorMessage(exception))
                } else {
                    signedIn(user).copy(profileLoadFailed = true, error = errorMessage(exception))
                }
            } finally {
                operationActive = false
            }
        }
    }

    private fun signedIn(user: FirebaseUser, name: String = "") = AccountUiState(
        loading = false,
        uid = user.uid,
        email = user.email.orEmpty(),
        displayName = name,
    )

    private fun fromProfile(user: FirebaseUser, profile: DocumentSnapshot) = signedIn(user, profile.getString("displayName").orEmpty()).copy(
        skills = profile.getString("skills").orEmpty(),
        experience = profile.getString("experience").orEmpty(),
        availability = profile.getString("availability").orEmpty(),
    )

    private fun validateCredentials(email: String, password: String): String? = when {
        email.length > 254 || !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Ingresa un correo válido"
        password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
        else -> null
    }

    private fun validateName(name: String): String? =
        if (name.length !in 2..80) "El nombre debe tener entre 2 y 80 caracteres" else null

    private fun errorMessage(exception: Exception): String = when ((exception as? FirebaseAuthException)?.errorCode) {
        "ERROR_EMAIL_ALREADY_IN_USE" -> "Este correo ya tiene una cuenta"
        "ERROR_INVALID_EMAIL" -> "Ingresa un correo válido"
        "ERROR_WEAK_PASSWORD" -> "La contraseña no cumple los requisitos"
        "ERROR_INVALID_CREDENTIAL", "ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND" -> "Correo o contraseña incorrectos"
        "ERROR_NETWORK_REQUEST_FAILED" -> "No se pudo conectar, revisa tu conexión"
        "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos, vuelve a intentarlo más tarde"
        "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> "Ya existe una cuenta con ese correo, inicia sesión con el método que usaste antes"
        else -> "No se pudo completar la operación, revisa tu conexión y vuelve a intentarlo"
    }

    override fun onCleared() {
        auth.removeAuthStateListener(authListener)
    }
}

class AccountViewModelFactory(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(AccountViewModel::class.java))
        return AccountViewModel(auth, firestore) as T
    }
}
