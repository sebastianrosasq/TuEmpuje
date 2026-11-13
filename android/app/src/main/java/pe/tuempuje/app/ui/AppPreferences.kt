package pe.tuempuje.app.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.tuempuje.app.data.Opportunity
import pe.tuempuje.app.data.SavedDatabase
import pe.tuempuje.app.data.SavedOpportunity

enum class DisplayMode { SYSTEM, LIGHT, DARK }

class AppPreferences(context: Context) {
    private val storage = context.applicationContext.getSharedPreferences("apariencia", Context.MODE_PRIVATE)
    private var accountScope: String? = null
    private val savedDao = SavedDatabase.get(context).saved()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val savedLock = Mutex()
    private var accountJob: Job? = null

    var purpose by mutableStateOf(storage.getString("proposito", null))
        private set
    var interests by mutableStateOf(storage.getStringSet("intereses", emptySet()).orEmpty().toSet())
        private set

    fun updatePurpose(value: String, selected: Set<String>) {
        purpose = value
        interests = selected.toSet()
        storage.edit().putString("proposito", value).putStringSet("intereses", selected.toSet()).apply()
    }

    var displayMode by mutableStateOf(
        DisplayMode.entries.firstOrNull { it.name == storage.getString("tema", DisplayMode.SYSTEM.name) }
            ?: DisplayMode.SYSTEM,
    )
        private set
    var reduceMotion by mutableStateOf(storage.getBoolean("reducir_movimiento", false))
        private set
    var showMascot by mutableStateOf(storage.getBoolean("mostrar_empu", true))
        private set
    var savedItems by mutableStateOf<List<SavedOpportunity>>(emptyList())
        private set
    val savedIds: Set<String> get() = savedItems.mapTo(mutableSetOf()) { it.id }
    var savedError by mutableStateOf<String?>(null)
        private set
    var savedLoading by mutableStateOf(true)
        private set

    init { selectAccount(null) }

    fun updateDisplayMode(value: DisplayMode) {
        displayMode = value
        storage.edit().putString("tema", value.name).apply()
    }

    fun updateReduceMotion(value: Boolean) {
        reduceMotion = value
        storage.edit().putBoolean("reducir_movimiento", value).apply()
    }

    fun updateShowMascot(value: Boolean) {
        showMascot = value
        storage.edit().putBoolean("mostrar_empu", value).apply()
    }

    fun toggleSaved(opportunity: Opportunity) {
        val account = accountScope ?: return
        scope.launch {
            try {
                savedLock.withLock {
                    if (savedDao.contains(account, opportunity.id)) savedDao.remove(account, opportunity.id)
                    else savedDao.save(SavedOpportunity.from(account, opportunity))
                }
                savedError = null
            } catch (_: Exception) {
                savedError = "No se pudo actualizar el guardado en este dispositivo"
            }
        }
    }

    fun removeSaved(id: String) {
        val account = accountScope ?: return
        scope.launch {
            try {
                savedLock.withLock { savedDao.remove(account, id) }
                savedError = null
            } catch (_: Exception) {
                savedError = "No se pudo quitar el guardado en este dispositivo"
            }
        }
    }

    fun refreshSaved(opportunities: List<Opportunity>) {
        val account = accountScope ?: return
        val saved = savedIds
        if (saved.isEmpty()) return
        scope.launch {
            try {
                savedLock.withLock {
                    opportunities.filter { it.id in saved }.forEach {
                        if (savedDao.contains(account, it.id)) savedDao.save(SavedOpportunity.from(account, it))
                    }
                }
            } catch (_: Exception) {
                savedError = "No se pudieron actualizar las copias guardadas"
            }
        }
    }

    fun selectAccount(uid: String?) {
        val next = uid ?: "invitado"
        if (next == accountScope) return
        accountScope = next
        accountJob?.cancel()
        savedItems = emptyList()
        savedError = null
        savedLoading = true
        accountJob = scope.launch {
            try {
                savedLock.withLock {
                    if (!storage.getBoolean("migrado_room_$next", false)) {
                        val previous = storage.getStringSet("guardados_$next", emptySet()).orEmpty().toSet()
                        savedDao.migrate(previous.map { SavedOpportunity(account = next, id = it) })
                        storage.edit().putBoolean("migrado_room_$next", true).apply()
                    }
                }
                savedDao.observe(next).collect { savedItems = it; savedLoading = false }
            } catch (exception: kotlinx.coroutines.CancellationException) {
                throw exception
            } catch (_: Exception) {
                savedError = "No se pudieron cargar los guardados de este dispositivo"
                savedLoading = false
            }
        }
    }

    fun close() { scope.cancel() }
}

val LocalAppPreferences = staticCompositionLocalOf<AppPreferences?> { null }
