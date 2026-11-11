package pe.tuempuje.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Ink = Color(0xFF202B3D)
val Slate = Color(0xFF506174)
val Canvas = Color(0xFFF5F7FA)
val Quiet = Color(0xFFE7ECF2)
val Gold = Color(0xFFD6B88C)

private val Palette = lightColorScheme(
    primary = Ink,
    onPrimary = Color.White,
    secondary = Slate,
    background = Canvas,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Quiet,
    onSurfaceVariant = Slate,
    outline = Color(0xFFBBC6D0),
)

@Composable
fun TuEmpujeTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Palette, content = content)
}
