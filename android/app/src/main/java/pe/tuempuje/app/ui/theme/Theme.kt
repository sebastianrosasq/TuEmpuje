package pe.tuempuje.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Ink = Color(0xFF202B3D)
val Slate = Color(0xFF506174)
val Canvas = Color(0xFFF5F7FA)
val Quiet = Color(0xFFE7ECF2)
val Gold = Color(0xFFD6B88C)

private val LightColors = lightColorScheme(
    primary = Ink,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDAE5F3),
    onPrimaryContainer = Color(0xFF192536),
    secondary = Slate,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0E8F1),
    onSecondaryContainer = Color(0xFF223245),
    tertiary = Color(0xFF6E5130),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF4DEC0),
    onTertiaryContainer = Color(0xFF2C1B09),
    background = Canvas,
    onBackground = Ink,
    surface = Color(0xFFFFFFFF),
    onSurface = Ink,
    surfaceVariant = Quiet,
    onSurfaceVariant = Color(0xFF46576B),
    outline = Color(0xFF708196),
    outlineVariant = Color(0xFFCBD5E0),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFBDD0E9),
    onPrimary = Color(0xFF172638),
    primaryContainer = Color(0xFF34475D),
    onPrimaryContainer = Color(0xFFDCE9F8),
    secondary = Color(0xFFB4C7DB),
    onSecondary = Color(0xFF203044),
    secondaryContainer = Color(0xFF37485A),
    onSecondaryContainer = Color(0xFFD9E6F4),
    tertiary = Color(0xFFE6C29A),
    onTertiary = Color(0xFF3E2B14),
    tertiaryContainer = Color(0xFF57412A),
    onTertiaryContainer = Color(0xFFFFDBB5),
    background = Color(0xFF101820),
    onBackground = Color(0xFFE1E8EF),
    surface = Color(0xFF18232F),
    onSurface = Color(0xFFE1E8EF),
    surfaceVariant = Color(0xFF354454),
    onSurfaceVariant = Color(0xFFC4D1DE),
    outline = Color(0xFF91A3B5),
    outlineVariant = Color(0xFF455565),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.3).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 21.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.1.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
)

@Composable
fun TuEmpujeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialExpressiveTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
