package pe.tuempuje.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import pe.tuempuje.app.ui.TuEmpujeApp
import pe.tuempuje.app.ui.theme.TuEmpujeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TuEmpujeTheme { TuEmpujeApp() }
        }
    }
}
