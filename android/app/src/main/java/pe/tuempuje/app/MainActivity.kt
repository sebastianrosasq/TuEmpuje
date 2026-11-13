package pe.tuempuje.app

import android.os.Bundle
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                TuEmpujeTheme(darkTheme = darkTheme) { TuEmpujeApp(services = services) }
            }
        }
    }
}
