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
import com.google.firebase.firestore.Blob
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
    val status: String = "published",
    val createdAtSeconds: Long = 0,
    val noExperienceRequired: Boolean? = null,
    val workMode: String? = null,
    val publicBusinessName: String = "",
    val publicBusinessSector: String = "",
    val publicBusinessDescription: String = "",
)

data class Candidate(val id: String, val name: String, val status: String, val skills: String = "", val experience: String = "", val availability: String = "", val district: String = "", val education: String = "", val portfolio: String = "", val confirmed: Boolean = false)
data class ApplicationProfile(val name: String = "", val skills: String = "", val experience: String = "", val availability: String = "", val district: String = "", val education: String = "", val portfolio: String = "")
data class WorkReference(val id: String, val workerId: String, val authorName: String, val title: String, val text: String, val visible: Boolean, val moderationHidden: Boolean)

class OpportunityViewModel(private val services: FirebaseServices?) : ViewModel() {
    val opportunities = mutableStateListOf<Opportunity>()
    val applications = mutableStateMapOf<String, String>()
    val confirmedWork = mutableStateMapOf<String, Boolean>()
    val candidates = mutableStateListOf<Candidate>()
    val publicReferences = mutableStateListOf<WorkReference>()
    var currentReference by mutableStateOf<WorkReference?>(null)
        private set
    var referencesLoading by mutableStateOf(false)
        private set
    var referencesError by mutableStateOf<String?>(null)
        private set
    var userId by mutableStateOf<String?>(null)
        private set
    var profileReady by mutableStateOf(false)
        private set
    var applicationProfile by mutableStateOf(ApplicationProfile())
        private set
    var profileLoaded by mutableStateOf(false)
        private set
    var profileRole by mutableStateOf<String?>(null)
        private set
    var profileAccountType by mutableStateOf<String?>(null)
        private set
    var profileInterests by mutableStateOf<Set<String>>(emptySet())
        private set
    var profileOrganization by mutableStateOf("")
        private set
    var profileBusinessSector by mutableStateOf("")
        private set
    var profileBusinessDescription by mutableStateOf("")
        private set
    private var profileListener: ListenerRegistration? = null
    private var ownListener: ListenerRegistration? = null
    private val personalListeners = mutableMapOf<String, ListenerRegistration>()
    private val publicItems = mutableMapOf<String, Pair<Opportunity, Long>>()
    private val ownItems = mutableMapOf<String, Pair<Opportunity, Long>>()
    private val personalItems = mutableMapOf<String, Pair<Opportunity, Long>>()
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
    var ownError by mutableStateOf<String?>(null)
        private set
    var candidateError by mutableStateOf<String?>(null)
        private set

    private var opportunityListener: ListenerRegistration? = null
    private var applicationListener: ListenerRegistration? = null
    private var candidateListener: ListenerRegistration? = null
    private var referenceListener: ListenerRegistration? = null
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
        stopObservingReferences()
        profileListener?.remove()
        ownListener?.remove()
        personalListeners.values.forEach { it.remove() }
        personalListeners.clear()
        publicItems.clear(); ownItems.clear(); personalItems.clear()
        profileReady = false; profileLoaded = false; profileRole = null; profileAccountType = null; profileInterests = emptySet()
        applicationProfile = ApplicationProfile()
        profileOrganization = ""; profileBusinessSector = ""; profileBusinessDescription = ""
        opportunityListener = null
        applicationListener = null
        candidateListener = null
        userId = uid
        opportunities.clear()
        applications.clear()
        confirmedWork.clear()
        candidates.clear()
        error = null
        feedError = null
        feedFromCache = true
        applicationsError = null
        ownError = null
        if (uid == null || services == null) {
            loading = false
            applicationsLoading = false
            return
        }
        loading = true
        applicationsLoading = true
        profileListener = services.firestore.collection("users").document(uid).addSnapshotListener { profile, exception ->
            if (uid != userId) return@addSnapshotListener
            if (exception != null || profile == null) { profileLoaded = false; profileReady = false; return@addSnapshotListener }
            profileLoaded = profile.exists()
            profileReady = (profile.getString("skills")?.trim()?.length ?: 0) >= 2 && (profile.getString("availability")?.trim()?.length ?: 0) >= 2 && pe.tuempuje.app.auth.identityError(profile.getString("dni").orEmpty(), profile.getString("phone").orEmpty(), profile.getString("documentType") ?: "DNI") == null
            applicationProfile = ApplicationProfile(profile.getString("displayName").orEmpty(), profile.getString("skills").orEmpty(), profile.getString("experience").orEmpty(), profile.getString("availability").orEmpty(), profile.getString("district").orEmpty(), profile.getString("education").orEmpty(), profile.getString("portfolio").orEmpty())
            profileRole = profile.getString("role")
            profileAccountType = profile.getString("accountType")
            profileOrganization = profile.getString("organization").orEmpty()
            profileBusinessSector = profile.getString("businessSector").orEmpty()
            profileBusinessDescription = profile.getString("businessDescription").orEmpty()
            profileInterests = (profile.get("interests") as? List<*>)?.filterIsInstance<String>()?.toSet().orEmpty()
        }
        ownListener = services.firestore.collection("opportunities").whereEqualTo("ownerId", uid)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, exception ->
                if (uid != userId) return@addSnapshotListener
                if (exception != null) {
                    ownError = "No se pudieron cargar tus publicaciones, revisa tu conexión"
                    return@addSnapshotListener
                }
                ownError = null
                ownItems.clear()
                snapshot?.documents.orEmpty().filterNot { it.metadata.hasPendingWrites() }.forEach { doc ->
                    parseOpportunity(doc, uid)?.let { ownItems[doc.id] = it }
                }
                syncHistoryListeners(uid)
                refreshItems()
            }
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
                publicItems.clear()
                snapshot?.documents.orEmpty().filterNot { it.metadata.hasPendingWrites() }.forEach { doc ->
                    parseOpportunity(doc, uid)?.let { publicItems[doc.id] = it }
                }
                syncHistoryListeners(uid)
                refreshItems()
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
                confirmedWork.clear()
                snapshot?.documents.orEmpty().filterNot { it.metadata.hasPendingWrites() }.forEach { doc ->
                    val opportunityId = doc.reference.parent.parent?.id ?: return@forEach
                    applications[opportunityId] = doc.getString("status").orEmpty()
                    confirmedWork[opportunityId] = doc.getTimestamp("workerConfirmedAt") != null
                }
                syncHistoryListeners(uid)
                refreshItems()
            }
    }

    private fun syncHistoryListeners(uid: String) {
        val db = services?.firestore ?: return
        val needed = applications.keys.toSet() - publicItems.keys - ownItems.keys
        (personalListeners.keys.toSet() - needed).forEach { id ->
            personalListeners.remove(id)?.remove()
            personalItems.remove(id)
        }
        needed.filterNot { it in personalListeners }.forEach { id ->
            personalListeners[id] = db.collection("opportunities").document(id).addSnapshotListener { job, exception ->
                if (uid != userId) return@addSnapshotListener
                if (exception != null) {
                    applicationsError = "No se pudieron cargar algunas postulaciones, revisa tu conexión"
                    return@addSnapshotListener
                }
                personalItems.remove(id)
                if (job != null && job.exists()) parseOpportunity(job, uid)?.let { personalItems[id] = it }
                refreshItems()
            }
        }
    }

    private fun parseOpportunity(doc: com.google.firebase.firestore.DocumentSnapshot, uid: String): Pair<Opportunity, Long>? {
        val owner = doc.getString("ownerId") ?: return null
        return Opportunity(
            id = doc.id, ownerId = owner, title = doc.getString("title").orEmpty(), organization = doc.getString("ownerName").orEmpty(),
            category = doc.getString("category").orEmpty(), location = doc.getString("location").orEmpty(), pay = doc.getString("pay").orEmpty(),
            duration = doc.getString("duration").orEmpty(), description = doc.getString("description").orEmpty(),
            requirements = doc.getString("requirements").orEmpty(), vacancies = (doc.getLong("vacancies") ?: 1).toInt(),
            selectedCount = (doc.getLong("selectedCount") ?: 0).toInt(), own = owner == uid, status = doc.getString("status").orEmpty(), createdAtSeconds = doc.getTimestamp("createdAt")?.seconds ?: 0,
            noExperienceRequired = doc.getBoolean("noExperienceRequired"),
            workMode = doc.getString("workMode"),
            publicBusinessName = doc.getString("publicBusinessName").orEmpty(),
            publicBusinessSector = doc.getString("publicBusinessSector").orEmpty(),
            publicBusinessDescription = doc.getString("publicBusinessDescription").orEmpty(),
        ) to (doc.getTimestamp("createdAt")?.seconds ?: 0L)
    }

    private fun refreshItems() {
        val items = (personalItems + ownItems + publicItems).values.sortedByDescending { it.second }.map { it.first }
        opportunities.clear(); opportunities.addAll(items)
    }

    fun savePurpose(role: String, interests: Set<String>, companyName: String = "", onResult: (Boolean) -> Unit = {}) {
        val uid = userId ?: run { onResult(false); return }
        val db = services?.firestore ?: run { onResult(false); return }
        if (!profileLoaded || role !in listOf("worker", "employer")) { onResult(false); return }
        viewModelScope.launch {
            try {
                val profile = db.collection("users").document(uid)
                db.runTransaction { transaction ->
                    val existing = transaction.get(profile)
                    check(existing.exists())
                    val accountType = if (role == "employer") "company" else "person"
                    val updates = mutableMapOf<String, Any>("role" to role, "accountType" to accountType, "interests" to interests.sorted())
                    if (accountType == "company") {
                        check(companyName.trim().length in 2..100)
                        updates["organization"] = companyName.trim()
                        updates["businessType"] = "NEGOCIO"
                    } else if (existing.getString("businessType") == "NEGOCIO") updates["businessType"] = "PERSONA"
                    transaction.update(profile, updates)
                }.await()
                onResult(userId == uid)
            }
            catch (exception: CancellationException) { throw exception }
            catch (_: Exception) { error = "No se pudieron guardar tus preferencias, vuelve a intentarlo"; onResult(false) }
        }
    }

    fun retry() {
        val uid = services?.auth?.currentUser?.uid
        opportunityListener?.remove()
        opportunityListener = null
        observeUser(uid)
    }

    fun find(id: String): Opportunity? = opportunities.find { it.id == id }

    fun publish(title: String, description: String, location: String, pay: String, duration: String, requirements: String, vacancies: Int, category: String, noExperienceRequired: Boolean, workMode: String, includeBusiness: Boolean, onSuccess: () -> Unit) {
        if (profileAccountType !in listOf("person", "company")) { error = "Selecciona el tipo de cuenta antes de publicar"; return }
        val uid = userId ?: run { error = "Inicia sesión para publicar"; return }
        val db = services?.firestore ?: return
        if (listOf(title, description, location, pay, duration, requirements, category).any { it.isBlank() } || vacancies !in 1..99 || workMode !in listOf("Presencial", "Híbrido", "Remoto")) {
            error = "Revisa los campos de la oportunidad"
            return
        }
        operation("No se confirmó la publicación, revisa tu actividad antes de intentarlo otra vez") {
            val profile = db.collection("users").document(uid).get(Source.SERVER).await()
            val name = profile.getString("displayName") ?: throw IllegalStateException("Completa tu perfil antes de publicar")
            check(services.auth.currentUser?.uid == uid)
            val business = if (includeBusiness) {
                check(profileOrganization.length in 2..100 && profile.getString("organization").orEmpty() == profileOrganization &&
                    profile.getString("businessSector").orEmpty() == profileBusinessSector && profile.getString("businessDescription").orEmpty() == profileBusinessDescription)
                mapOf("publicBusinessName" to profileOrganization, "publicBusinessSector" to profileBusinessSector, "publicBusinessDescription" to profileBusinessDescription)
            } else emptyMap()
            db.collection("opportunities").document().set(mapOf(
                "ownerId" to uid, "ownerName" to name, "title" to title.trim(),
                "description" to description.trim(), "location" to location.trim(), "pay" to pay.trim(),
                "duration" to duration.trim(), "requirements" to requirements.trim(), "category" to category, "noExperienceRequired" to noExperienceRequired, "workMode" to workMode,
                "vacancies" to vacancies, "selectedCount" to 0, "status" to "published", "createdAt" to FieldValue.serverTimestamp(),
            ) + business).await()
            onSuccess()
        }
    }

    fun apply(id: String, cv: ByteArray? = null, onSuccess: () -> Unit) {
        if (profileAccountType != "person") { error = "Las postulaciones están disponibles para cuentas personales"; return }
        val uid = userId ?: run { error = "Inicia sesión para postular"; return }
        val db = services?.firestore ?: return
        val opportunity = find(id) ?: run { error = "La oportunidad ya no está disponible"; return }
        if (opportunity.own || (id in applications && applications[id] !in setOf("withdrawn", "cancelled"))) return
        if (!profileReady) { error = "Completa tus habilidades y disponibilidad en tu perfil antes de postular"; return }
        if (cv != null && (cv.size !in 5..700_000 || !cv.take(5).toByteArray().contentEquals("%PDF-".toByteArray()))) { error = "Selecciona un PDF válido de hasta 700 kB"; return }
        if (opportunity.selectedCount >= opportunity.vacancies) {
            error = "Las vacantes de esta oportunidad ya están cubiertas"
            return
        }
        operation("No se confirmó la postulación, revisa tu actividad antes de volver a intentarlo") {
            val profile = db.collection("users").document(uid).get(Source.SERVER).await()
            val name = profile.getString("displayName") ?: throw IllegalStateException("Completa tu perfil antes de postular")
            check(services.auth.currentUser?.uid == uid)
            val skills = profile.getString("skills").orEmpty()
            val availability = profile.getString("availability").orEmpty()
            val experience = profile.getString("experience").orEmpty()
            val district = profile.getString("district").orEmpty()
            val education = profile.getString("education").orEmpty()
            val portfolio = profile.getString("portfolio").orEmpty()
            val ref = db.collection("opportunities").document(id).collection("applications").document(uid)
            val batch = db.batch()
            if (applications[id] in setOf("withdrawn", "cancelled")) {
                batch.update(ref, mapOf("status" to "pending", "applicantName" to name, "applicantSkills" to skills, "applicantAvailability" to availability, "applicantExperience" to experience, "applicantDistrict" to district, "applicantEducation" to education, "applicantPortfolio" to portfolio, "createdAt" to FieldValue.serverTimestamp(),
                    "withdrawnAt" to FieldValue.delete(), "selectedAt" to FieldValue.delete(), "completedAt" to FieldValue.delete(), "reviewedAt" to FieldValue.delete(), "decisionAt" to FieldValue.delete()))
            } else batch.set(ref, mapOf(
                "applicantId" to uid, "applicantName" to name, "applicantSkills" to skills, "applicantAvailability" to availability, "applicantExperience" to experience, "applicantDistrict" to district, "applicantEducation" to education, "applicantPortfolio" to portfolio, "status" to "pending", "createdAt" to FieldValue.serverTimestamp(),
            ))
            val cvRef = ref.collection("privateFiles").document("cv")
            if (cv != null) batch.set(cvRef, mapOf("data" to Blob.fromBytes(cv), "contentType" to "application/pdf", "updatedAt" to FieldValue.serverTimestamp()))
            else if (applications[id] in setOf("withdrawn", "cancelled")) batch.delete(cvRef)
            batch.commit().await()
            onSuccess()
        }
    }

    suspend fun candidateCv(opportunityId: String, applicantId: String): ByteArray? {
        val db = services?.firestore ?: error("Servicio no disponible")
        val snapshot = db.collection("opportunities").document(opportunityId).collection("applications").document(applicantId)
            .collection("privateFiles").document("cv").get(Source.SERVER).await()
        val data = snapshot.getBlob("data")?.toBytes() ?: return null
        require(data.size in 5..700_000 && data.copyOfRange(0, 5).contentEquals("%PDF-".toByteArray()))
        return data
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
                    candidates.add(Candidate(doc.id, doc.getString("applicantName").orEmpty(), doc.getString("status").orEmpty(), doc.getString("applicantSkills").orEmpty(), doc.getString("applicantExperience").orEmpty(), doc.getString("applicantAvailability").orEmpty(), doc.getString("applicantDistrict").orEmpty(), doc.getString("applicantEducation").orEmpty(), doc.getString("applicantPortfolio").orEmpty(), doc.getTimestamp("workerConfirmedAt") != null))
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
                check(current.getString("ownerId") == userId && current.getString("status") in setOf("published", "closed") && selected < vacancies && candidate.getString("status") in setOf("pending", "reviewing"))
                transaction.update(job, mapOf("selectedCount" to selected + 1, "lastSelectedApplicantId" to candidateId))
                transaction.update(application, mapOf("status" to "selected", "selectedAt" to FieldValue.serverTimestamp()))
            }.await()
        }
    }

    fun withdraw(id: String, onSuccess: () -> Unit) {
        val uid = userId ?: return
        val db = services?.firestore ?: return
        operation("No se confirmó el retiro, revisa tu actividad antes de reintentar") {
            val job = db.collection("opportunities").document(id)
            val application = job.collection("applications").document(uid)
            db.runTransaction { tx ->
                val current = tx.get(application)
                val parent = tx.get(job)
                when (current.getString("status")) {
                    "pending", "reviewing" -> Unit
                    "selected" -> {
                        val count = parent.getLong("selectedCount") ?: 0
                        check(count > 0)
                        tx.update(job, mapOf("selectedCount" to count - 1, "lastWithdrawnApplicantId" to uid))
                    }
                    else -> error("La postulación no puede retirarse")
                }
                tx.update(application, mapOf("status" to "withdrawn", "withdrawnAt" to FieldValue.serverTimestamp()))
                tx.delete(application.collection("privateFiles").document("cv"))
            }.await()
            onSuccess()
        }
    }

    fun removeOpportunity(id: String, onSuccess: () -> Unit) {
        val db = services?.firestore ?: return
        if (find(id)?.own != true) return
        operation("No se confirmó la eliminación, revisa tu actividad antes de reintentar") {
            db.collection("opportunities").document(id).update(mapOf("status" to "removed", "removedAt" to FieldValue.serverTimestamp())).await()
            onSuccess()
        }
    }

    fun changeOpportunityStatus(id: String, close: Boolean, onSuccess: () -> Unit) {
        val db = services?.firestore ?: return
        val job = find(id) ?: return
        if (!job.own || job.status != (if (close) "published" else "closed")) return
        operation("No se confirmó el cambio de estado, revisa tu publicación") {
            db.collection("opportunities").document(id).update(mapOf(
                "status" to if (close) "closed" else "published", "statusChangedAt" to FieldValue.serverTimestamp(),
            )).await()
            onSuccess()
        }
    }

    fun updateCandidateStatus(opportunityId: String, candidateId: String, status: String) {
        val db = services?.firestore ?: return
        if (find(opportunityId)?.own != true || status !in setOf("reviewing", "rejected")) return
        operation("No se confirmó el cambio de la postulación, revisa la lista") {
            db.collection("opportunities").document(opportunityId).collection("applications").document(candidateId)
                .update(mapOf("status" to status, (if (status == "reviewing") "reviewedAt" else "decisionAt") to FieldValue.serverTimestamp())).await()
        }
    }

    fun cancelSelection(opportunityId: String, candidateId: String) {
        val db = services?.firestore ?: return
        if (find(opportunityId)?.own != true) return
        operation("No se confirmó la cancelación, revisa las vacantes antes de reintentar") {
            val job = db.collection("opportunities").document(opportunityId)
            val application = job.collection("applications").document(candidateId)
            db.runTransaction { tx ->
                val currentJob = tx.get(job)
                val currentApplication = tx.get(application)
                val count = currentJob.getLong("selectedCount") ?: 0L
                check(currentJob.getString("ownerId") == userId && currentJob.getString("status") in setOf("published", "closed") && count > 0 && currentApplication.getString("status") == "selected")
                tx.update(job, mapOf("selectedCount" to count - 1, "lastCancelledApplicantId" to candidateId))
                tx.update(application, mapOf("status" to "cancelled", "decisionAt" to FieldValue.serverTimestamp()))
            }.await()
        }
    }

    fun removePublicBusiness(id: String, onSuccess: () -> Unit) {
        val db = services?.firestore ?: return
        if (find(id)?.own != true) return
        operation("No se confirmó el cambio, revisa la publicación antes de reintentar") {
            db.collection("opportunities").document(id).update(mapOf(
                "publicBusinessName" to FieldValue.delete(), "publicBusinessSector" to FieldValue.delete(),
                "publicBusinessDescription" to FieldValue.delete(), "updatedAt" to FieldValue.serverTimestamp(),
            )).await()
            onSuccess()
        }
    }

    fun editOpportunity(id: String, title: String, description: String, location: String, pay: String, duration: String,
        requirements: String, vacancies: Int, category: String, noExperienceRequired: Boolean?, workMode: String?, includeBusiness: Boolean, onSuccess: () -> Unit) {
        val db = services?.firestore ?: return
        if (find(id)?.own != true) return
        operation("No se confirmó el cambio, revisa la publicación antes de reintentar") {
            val business = if (includeBusiness) {
                check(profileOrganization.length in 2..100)
                mapOf("publicBusinessName" to profileOrganization, "publicBusinessSector" to profileBusinessSector, "publicBusinessDescription" to profileBusinessDescription)
            } else mapOf("publicBusinessName" to FieldValue.delete(), "publicBusinessSector" to FieldValue.delete(), "publicBusinessDescription" to FieldValue.delete())
            db.collection("opportunities").document(id).update(mapOf(
                "title" to title.trim(), "description" to description.trim(), "location" to location.trim(), "pay" to pay.trim(),
                "duration" to duration.trim(), "requirements" to requirements.trim(), "vacancies" to vacancies,
                "category" to category, "updatedAt" to FieldValue.serverTimestamp(),
            ) + (if (noExperienceRequired != null) mapOf("noExperienceRequired" to noExperienceRequired) else emptyMap()) +
                (if (workMode != null) mapOf("workMode" to workMode) else emptyMap()) + business).await()
            onSuccess()
        }
    }

    fun completeCandidate(opportunityId: String, candidateId: String) {
        val db = services?.firestore ?: return
        if (find(opportunityId)?.own != true) return
        operation("No se confirmó la finalización, revisa la lista antes de intentarlo otra vez") {
            val application = db.collection("opportunities").document(opportunityId).collection("applications").document(candidateId)
            db.runTransaction { transaction ->
                val current = transaction.get(application)
                check(current.getString("status") == "selected")
                transaction.update(application, mapOf("status" to "completed", "completedAt" to FieldValue.serverTimestamp()))
            }.await()
        }
    }

    fun confirmCompletedWork(opportunityId: String) {
        val uid = userId ?: return
        val db = services?.firestore ?: return
        if (applications[opportunityId] != "completed" || confirmedWork[opportunityId] == true) return
        operation("No se confirmó la experiencia, revisa tu actividad antes de reintentar") {
            db.collection("opportunities").document(opportunityId).collection("applications").document(uid)
                .update("workerConfirmedAt", FieldValue.serverTimestamp()).await()
        }
    }

    private fun parseReference(doc: com.google.firebase.firestore.DocumentSnapshot): WorkReference = WorkReference(
        doc.id, doc.getString("workerId").orEmpty(), doc.getString("authorName").orEmpty(),
        doc.getString("title").orEmpty(), doc.getString("text").orEmpty(), doc.getBoolean("visible") == true, doc.getBoolean("moderationHidden") == true,
    )

    fun observeReference(opportunityId: String, applicantId: String) {
        stopObservingReferences()
        val db = services?.firestore ?: return
        referencesLoading = true
        referenceListener = db.collection("workReferences").document("${opportunityId}_${applicantId}").addSnapshotListener(MetadataChanges.INCLUDE) { doc, exception ->
            referencesLoading = false
            referencesError = if (exception != null) "No se pudo cargar la referencia" else null
            if (exception != null) currentReference = null
            else if (doc?.metadata?.hasPendingWrites() != true) currentReference = if (doc?.exists() == true) parseReference(doc) else null
        }
    }

    fun observePublicReferences(applicantId: String) {
        stopObservingReferences()
        val db = services?.firestore ?: return
        referencesLoading = true
        referenceListener = db.collection("workReferences").whereEqualTo("workerId", applicantId).whereEqualTo("visible", true)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, exception ->
                referencesLoading = false
                referencesError = if (exception != null) "No se pudieron cargar las referencias" else null
                publicReferences.clear()
                if (exception == null) snapshot?.documents?.filterNot { it.metadata.hasPendingWrites() }?.mapTo(publicReferences, ::parseReference)
            }
    }

    fun stopObservingReferences() {
        referenceListener?.remove(); referenceListener = null
        currentReference = null; publicReferences.clear(); referencesError = null; referencesLoading = false
    }

    fun writeReference(opportunity: Opportunity, applicantId: String, text: String, onSuccess: () -> Unit) {
        val uid = userId ?: return
        val db = services?.firestore ?: return
        val clean = text.trim()
        if (!opportunity.own || clean.length !in 10..500) { error = "Escribe una referencia de 10 a 500 caracteres"; return }
        operation("No se confirmó el envío de la referencia, revisa antes de reintentar") {
            db.collection("workReferences").document("${opportunity.id}_${applicantId}").set(mapOf(
                "opportunityId" to opportunity.id, "workerId" to applicantId, "authorId" to uid,
                "authorName" to opportunity.organization, "title" to opportunity.title, "text" to clean,
                "visible" to false, "createdAt" to FieldValue.serverTimestamp(),
            )).await()
            onSuccess()
        }
    }

    fun setReferenceVisibility(reference: WorkReference, visible: Boolean) {
        val uid = userId ?: return
        val db = services?.firestore ?: return
        if (reference.workerId != uid) return
        operation("No se confirmó el cambio de visibilidad") {
            db.collection("workReferences").document(reference.id).update("visible", visible).await()
        }
    }

    fun disputeReference(reference: WorkReference, details: String, onSuccess: () -> Unit) {
        val uid = userId ?: return
        val db = services?.firestore ?: return
        val clean = details.trim()
        if (reference.workerId != uid || clean.length !in 10..500) { error = "Describe el problema con entre 10 y 500 caracteres"; return }
        operation("No se confirmó el reporte de la referencia") {
            db.collection("referenceDisputes").document(reference.id).set(mapOf(
                "referenceId" to reference.id, "workerId" to uid, "details" to clean,
                "status" to "open", "createdAt" to FieldValue.serverTimestamp(),
            )).await()
            onSuccess()
        }
    }

    fun reportOpportunity(opportunityId: String, reason: String, details: String, onSuccess: () -> Unit) {
        val db = services?.firestore ?: return
        val uid = userId ?: return
        val cleanDetails = details.trim()
        val title = find(opportunityId)?.title
        if (reason !in listOf("Información engañosa", "Pago o condiciones", "Seguridad", "Otro") || cleanDetails.length !in 10..500) {
            error = "Describe el motivo con entre 10 y 500 caracteres"
            return
        }
        operation("No se confirmó el reporte, comprueba tu conexión antes de reintentar") {
            db.collection("reports").document("${uid}_${opportunityId}").set(mapOf(
                "reporterId" to uid, "opportunityId" to opportunityId, "reason" to reason,
                "details" to cleanDetails, "status" to "open", "createdAt" to FieldValue.serverTimestamp(),
            ) + (title?.let { mapOf("opportunityTitle" to it) } ?: emptyMap())).await()
            onSuccess()
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
        stopObservingReferences()
        services?.auth?.removeAuthStateListener(authListener)
        opportunityListener?.remove()
        applicationListener?.remove()
        candidateListener?.remove()
        profileListener?.remove()
        ownListener?.remove()
        personalListeners.values.forEach { it.remove() }
    }
}

class OpportunityViewModelFactory(private val services: FirebaseServices?) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(OpportunityViewModel::class.java))
        return OpportunityViewModel(services) as T
    }
}
