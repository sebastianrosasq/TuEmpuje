package pe.tuempuje.app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

data class Opportunity(
    val id: String,
    val ownerId: String,
    val title: String,
    val organization: String,
    val category: String,
    val location: String,
    val pay: String,
    val duration: String,
    val description: String,
    val requirements: String,
    val vacancies: Int,
    val selectedCount: Int,
    val own: Boolean,
)

data class Candidate(val id: String, val name: String, val status: String)

class OpportunityViewModel(private val services: FirebaseServices?) : ViewModel() {
    val opportunities = mutableStateListOf<Opportunity>()
    val applications = mutableStateMapOf<String, String>()
    val candidates = mutableStateListOf<Candidate>()
    var userId by mutableStateOf<String?>(null)
        private set
    var loading by mutableStateOf(false)
        private set
    var candidatesLoading by mutableStateOf(false)
        private set
    var applicationsLoading by mutableStateOf(false)
        private set
    var busy by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var feedError by mutableStateOf<String?>(null)
        private set
    var feedFromCache by mutableStateOf(true)
        private set
    var applicationsError by mutableStateOf<String?>(null)
        private set
    var candidateError by mutableStateOf<String?>(null)
        private set

    private var opportunityListener: ListenerRegistration? = null
    private var applicationListener: ListenerRegistration? = null
    private var candidateListener: ListenerRegistration? = null
    private val authListener = FirebaseAuth.AuthStateListener { source -> observeUser(source.currentUser?.uid) }

    init {
        if (services == null) error = "El servicio de desarrollo no está configurado"
        else services.auth.addAuthStateListener(authListener)
    }

    private fun observeUser(uid: String?) {
        if (uid == userId && opportunityListener != null) return
        opportunityListener?.remove()
        applicationListener?.remove()
        candidateListener?.remove()
        opportunityListener = null
        applicationListener = null
        candidateListener = null
        userId = uid
        opportunities.clear()
        applications.clear()
        candidates.clear()
        error = null
        feedError = null
        feedFromCache = true
        applicationsError = null
        if (uid == null || services == null) {
            loading = false
            applicationsLoading = false
            return
        }
        loading = true
        applicationsLoading = true
        opportunityListener = services.firestore.collection("opportunities")
            .whereEqualTo("status", "published")
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, exception ->
                if (uid != userId) return@addSnapshotListener
                loading = false
                if (exception != null) {
                    feedError = "No se pudieron cargar las oportunidades, revisa tu conexión"
                    return@addSnapshotListener
                }
                feedError = null
                feedFromCache = snapshot?.metadata?.isFromCache != false
                val items = snapshot?.documents.orEmpty().filterNot { it.metadata.hasPendingWrites() }.mapNotNull { doc ->
                    val owner = doc.getString("ownerId") ?: return@mapNotNull null
                    Opportunity(
                        id = doc.id,
                        ownerId = owner,
                        title = doc.getString("title").orEmpty(),
                        organization = doc.getString("ownerName").orEmpty(),
                        category = doc.getString("category").orEmpty(),
                        location = doc.getString("location").orEmpty(),
                        pay = doc.getString("pay").orEmpty(),
                        duration = doc.getString("duration").orEmpty(),
                        description = doc.getString("description").orEmpty(),
                        requirements = doc.getString("requirements").orEmpty(),
                        vacancies = (doc.getLong("vacancies") ?: 1).toInt(),
                        selectedCount = (doc.getLong("selectedCount") ?: 0).toInt(),
                        own = owner == uid,
                    ) to (doc.getTimestamp("createdAt")?.seconds ?: 0L)
                }.sortedByDescending { it.second }.map { it.first }
                opportunities.clear()
                opportunities.addAll(items)
            }
        applicationListener = services.firestore.collectionGroup("applications")
            .whereEqualTo("applicantId", uid)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, exception ->
                if (uid != userId) return@addSnapshotListener
                applicationsLoading = false
                if (exception != null) {
                    applicationsError = "No se pudieron cargar tus postulaciones, revisa tu conexión"
                    return@addSnapshotListener
                }
                applicationsError = null
                applications.clear()
                snapshot?.documents.orEmpty().filterNot { it.metadata.hasPendingWrites() }.forEach { doc ->
                    val opportunityId = doc.reference.parent.parent?.id ?: return@forEach
                    applications[opportunityId] = doc.getString("status").orEmpty()
                }
            }
    }

    fun retry() {
        val uid = services?.auth?.currentUser?.uid
        opportunityListener?.remove()
        opportunityListener = null
        observeUser(uid)
    }

    fun find(id: String): Opportunity? = opportunities.find { it.id == id }

    fun publish(title: String, description: String, location: String, pay: String, duration: String, requirements: String, vacancies: Int, category: String, onSuccess: () -> Unit) {
        val uid = userId ?: run { error = "Inicia sesión para publicar"; return }
        val db = services?.firestore ?: return
        if (listOf(title, description, location, pay, duration, requirements, category).any { it.isBlank() } || vacancies !in 1..99) {
            error = "Revisa los campos de la oportunidad"
            return
        }
        operation("No se confirmó la publicación, revisa tu actividad antes de intentarlo otra vez") {
            val profile = db.collection("users").document(uid).get(Source.SERVER).await()
            val name = profile.getString("displayName") ?: throw IllegalStateException("Completa tu perfil antes de publicar")
            check(services.auth.currentUser?.uid == uid)
            db.collection("opportunities").document().set(mapOf(
                "ownerId" to uid, "ownerName" to name, "title" to title.trim(),
                "description" to description.trim(), "location" to location.trim(), "pay" to pay.trim(),
                "duration" to duration.trim(), "requirements" to requirements.trim(), "category" to category,
                "vacancies" to vacancies, "selectedCount" to 0, "status" to "published", "createdAt" to FieldValue.serverTimestamp(),
            )).await()
            onSuccess()
        }
    }

    fun apply(id: String, onSuccess: () -> Unit) {
        val uid = userId ?: run { error = "Inicia sesión para postular"; return }
        val db = services?.firestore ?: return
        val opportunity = find(id) ?: run { error = "La oportunidad ya no está disponible"; return }
        if (opportunity.own || id in applications) return
        if (opportunity.selectedCount >= opportunity.vacancies) {
            error = "Las vacantes de esta oportunidad ya están cubiertas"
            return
        }
        operation("No se confirmó la postulación, revisa tu actividad antes de volver a intentarlo") {
            val profile = db.collection("users").document(uid).get(Source.SERVER).await()
            val name = profile.getString("displayName") ?: throw IllegalStateException("Completa tu perfil antes de postular")
            check(services.auth.currentUser?.uid == uid)
            db.collection("opportunities").document(id).collection("applications").document(uid).set(mapOf(
                "applicantId" to uid, "applicantName" to name, "status" to "pending", "createdAt" to FieldValue.serverTimestamp(),
            )).await()
            onSuccess()
        }
    }

    fun observeCandidates(id: String) {
        candidateListener?.remove()
        candidates.clear()
        candidateError = null
        val db = services?.firestore ?: return
        if (find(id)?.own != true) return
        candidatesLoading = true
        candidateListener = db.collection("opportunities").document(id).collection("applications")
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, exception ->
                candidatesLoading = false
                if (exception != null) {
                    candidateError = "No se pudieron cargar las postulaciones, revisa tu conexión"
                    return@addSnapshotListener
                }
                candidates.clear()
                snapshot?.documents.orEmpty().filterNot { it.metadata.hasPendingWrites() }.forEach { doc ->
                    candidates.add(Candidate(doc.id, doc.getString("applicantName").orEmpty(), doc.getString("status").orEmpty()))
                }
            }
    }

    fun stopObservingCandidates() { candidateListener?.remove(); candidateListener = null }

    fun selectCandidate(opportunityId: String, candidateId: String) {
        val db = services?.firestore ?: return
        if (find(opportunityId)?.own != true) return
        operation("No se confirmó la selección, revisa la lista antes de intentarlo otra vez") {
            val job = db.collection("opportunities").document(opportunityId)
            val application = job.collection("applications").document(candidateId)
            db.runTransaction { transaction ->
                val current = transaction.get(job)
                val candidate = transaction.get(application)
                val selected = current.getLong("selectedCount") ?: 0L
                val vacancies = current.getLong("vacancies") ?: 0L
                check(current.getString("ownerId") == userId && selected < vacancies && candidate.getString("status") == "pending")
                transaction.update(job, mapOf("selectedCount" to selected + 1, "lastSelectedApplicantId" to candidateId))
                transaction.update(application, mapOf("status" to "selected", "selectedAt" to FieldValue.serverTimestamp()))
            }.await()
        }
    }

    private fun operation(timeoutMessage: String, block: suspend () -> Unit) {
        if (busy) return
        busy = true
        error = null
        viewModelScope.launch {
            try {
                withTimeout(15_000) { block() }
            } catch (_: TimeoutCancellationException) {
                error = timeoutMessage
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                error = "No se pudo completar la operación, comprueba tus datos y la conexión"
            } finally {
                busy = false
            }
        }
    }

    override fun onCleared() {
        services?.auth?.removeAuthStateListener(authListener)
        opportunityListener?.remove()
        applicationListener?.remove()
        candidateListener?.remove()
    }
}

class OpportunityViewModelFactory(private val services: FirebaseServices?) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(OpportunityViewModel::class.java))
        return OpportunityViewModel(services) as T
    }
}
