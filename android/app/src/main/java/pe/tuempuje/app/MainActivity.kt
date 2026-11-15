package pe.tuempuje.app

import android.os.Bundle
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import pe.tuempuje.app.data.FirebaseEnvironment
import pe.tuempuje.app.ui.AppPreferences
import pe.tuempuje.app.ui.DisplayMode
import pe.tuempuje.app.ui.LocalAppPreferences
import pe.tuempuje.app.ui.TuEmpujeApp
import pe.tuempuje.app.ui.theme.TuEmpujeTheme

class MainActivity : ComponentActivity() {
    private var resetCode by mutableStateOf<String?>(null)
    private var notificationDestination by mutableStateOf<String?>(null)
    private var notificationRevision by mutableStateOf(0)

    private fun acceptNotification(intent: Intent?) {
        val destination = intent?.getStringExtra("notification_destination")
        if (destination in setOf("actividad", "mensajes")) {
            notificationDestination = destination
            notificationRevision++
            intent?.removeExtra("notification_destination")
        }
    }

    private fun acceptResetLink(intent: Intent?) {
        val link = intent?.takeIf { it.action == Intent.ACTION_VIEW }?.data ?: return
        val host = (if (BuildConfig.FIREBASE_LOCAL) "demo-tuempuje" else BuildConfig.FIREBASE_PROJECT_ID) + ".firebaseapp.com"
        if (link.scheme != "https" || link.host != host || link.path != "/__/auth/action") return
        if (link.getQueryParameters("mode") != listOf("resetPassword")) return
        resetCode = link.getQueryParameters("oobCode").singleOrNull()?.takeIf { it.isNotBlank() && it.length <= 1024 }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        acceptResetLink(intent)
        acceptNotification(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        acceptResetLink(intent)
        acceptNotification(intent)
        val services = FirebaseEnvironment.create(applicationContext)
        setContent {
            val preferences = remember { AppPreferences(applicationContext) }
            DisposableEffect(preferences) { onDispose { preferences.close() } }
            val darkTheme = when (preferences.displayMode) {
                DisplayMode.SYSTEM -> isSystemInDarkTheme()
                DisplayMode.LIGHT -> false
                DisplayMode.DARK -> true
            }
            val view = LocalView.current
            SideEffect {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
            CompositionLocalProvider(LocalAppPreferences provides preferences) {
                TuEmpujeTheme(darkTheme = darkTheme) { TuEmpujeApp(services = services, resetCode = resetCode, onResetConsumed = { resetCode = null }, notificationDestination = notificationDestination, notificationRevision = notificationRevision) }
            }
        }
    }
}
