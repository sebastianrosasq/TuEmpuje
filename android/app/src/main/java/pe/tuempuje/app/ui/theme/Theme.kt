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
import pe.tuempuje.app.ui.VisualStyle

val Ink = Color(0xFF202B3D)
val Slate = Color(0xFF506174)
val Canvas = Color(0xFFF5F7FA)
val Quiet = Color(0xFFE7ECF2)
val Gold = Color(0xFFD6B88C)
val EmpuTeal = Color(0xFF187F76)
val EmpuLight = Color(0xFFE9F1F4)
val EmpuInk = Color(0xFF101A23)

object AppSpacing {
    val small = 8.dp
    val medium = 12.dp
    val large = 18.dp
    val screen = 22.dp
}

private val LightColors = lightColorScheme(
    primary = EmpuTeal,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC9EAE5),
    onPrimaryContainer = Color(0xFF103B36),
    secondary = Slate,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0E8F1),
    onSecondaryContainer = Color(0xFF223245),
    tertiary = Color(0xFF854C43),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDAD3),
    onTertiaryContainer = Color(0xFF39201B),
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
    primary = Color(0xFF8AD5C9),
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF16433F),
    onPrimaryContainer = Color(0xFFC4EEE7),
    secondary = Color(0xFFB4C7DB),
    onSecondary = Color(0xFF203044),
    secondaryContainer = Color(0xFF37485A),
    onSecondaryContainer = Color(0xFFD9E6F4),
    tertiary = Color(0xFFFFB4A7),
    onTertiary = Color(0xFF552C24),
    tertiaryContainer = Color(0xFF563A35),
    onTertiaryContainer = Color(0xFFFFDAD3),
    background = EmpuInk,
    onBackground = EmpuLight,
    surface = Color(0xFF1D2B3A),
    onSurface = EmpuLight,
    surfaceVariant = Color(0xFF354857),
    onSurfaceVariant = Color(0xFFCCDADF),
    outline = Color(0xFF91A3B5),
    outlineVariant = Color(0xFF455565),
)

private fun futuristicColors(background: Color, surface: Color, accent: Color, container: Color, onAccent: Color, secondary: Color) = darkColorScheme(
    primary = accent, onPrimary = onAccent, primaryContainer = container, onPrimaryContainer = Color(0xFFF2F6FA),
    secondary = secondary, onSecondary = background, secondaryContainer = surface, onSecondaryContainer = Color(0xFFE9F0F5),
    tertiary = accent, onTertiary = onAccent, tertiaryContainer = container, onTertiaryContainer = Color.White,
    background = background, onBackground = Color(0xFFF2F6FA), surface = surface, onSurface = Color(0xFFF2F6FA),
    surfaceVariant = container, onSurfaceVariant = Color(0xFFCBD7E2),
    surfaceContainerLowest = background, surfaceContainerLow = surface, surfaceContainer = surface,
    surfaceContainerHigh = container, surfaceContainerHighest = container,
    outline = Color(0xFF91A3B5), outlineVariant = Color(0xFF546778),
)

private val PrismaColors = futuristicColors(Color(0xFF1A1525), Color(0xFF272039), Color(0xFFCBB8FF), Color(0xFF403159), Color(0xFF24163F), Color(0xFFD3C5F1))
private val PulsoColors = futuristicColors(Color(0xFF111A22), Color(0xFF172F35), Color(0xFF50E3B1), Color(0xFF16443F), Color(0xFF062F25), Color(0xFFB3DDD2))
private val PetroleoColors = futuristicColors(Color(0xFF0E2023), Color(0xFF182F31), Color(0xFFFFB59B), Color(0xFF46362F), Color(0xFF482116), Color(0xFFB5DBD3))
private val TintaColors = futuristicColors(Color(0xFF121A28), Color(0xFF1B293B), Color(0xFFAAC5FF), Color(0xFF263D68), Color(0xFF08265D), Color(0xFFC0D4EB))
private val HorizonteColors = futuristicColors(Color(0xFF151B36), Color(0xFF242B4B), Color(0xFFFFA997), Color(0xFF4A304A), Color(0xFF4E1D17), Color(0xFFC7D8F0))

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
    visualStyle: VisualStyle = VisualStyle.ORIGINAL,
    content: @Composable () -> Unit,
) {
    MaterialExpressiveTheme(
        colorScheme = when (visualStyle) {
            VisualStyle.ORIGINAL -> if (darkTheme) DarkColors else LightColors
            VisualStyle.PRISMA -> PrismaColors
            VisualStyle.PULSO -> PulsoColors
            VisualStyle.PETROLEO -> PetroleoColors
            VisualStyle.TINTA -> TintaColors
            VisualStyle.HORIZONTE -> HorizonteColors
        },
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
