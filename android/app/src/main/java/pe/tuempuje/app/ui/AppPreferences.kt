package pe.tuempuje.app.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

enum class DisplayMode { SYSTEM, LIGHT, DARK }

class AppPreferences(context: Context) {
    private val storage = context.applicationContext.getSharedPreferences("apariencia", Context.MODE_PRIVATE)
    private var accountScope = "invitado"

    var displayMode by mutableStateOf(
        DisplayMode.entries.firstOrNull { it.name == storage.getString("tema", DisplayMode.SYSTEM.name) }
            ?: DisplayMode.SYSTEM,
    )
        private set
    var reduceMotion by mutableStateOf(storage.getBoolean("reducir_movimiento", false))
        private set
    var showMascot by mutableStateOf(storage.getBoolean("mostrar_empu", true))
        private set
    var savedIds by mutableStateOf(storage.getStringSet("guardados_invitado", emptySet())?.toSet().orEmpty())
        private set

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

    fun toggleSaved(id: String) {
        savedIds = if (id in savedIds) savedIds - id else savedIds + id
        storage.edit().putStringSet("guardados_$accountScope", savedIds.toSet()).apply()
    }

    fun selectAccount(uid: String?) {
        val next = uid ?: "invitado"
        if (next == accountScope) return
        accountScope = next
        savedIds = storage.getStringSet("guardados_$accountScope", emptySet())?.toSet().orEmpty()
    }
}

val LocalAppPreferences = staticCompositionLocalOf<AppPreferences?> { null }
