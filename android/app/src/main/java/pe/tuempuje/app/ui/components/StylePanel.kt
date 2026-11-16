package pe.tuempuje.app.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import pe.tuempuje.app.ui.VisualStyle

/** Decorative surfaces are drawn once per size and never intercept input. */
fun Modifier.stylePanel(style: VisualStyle, accent: Color): Modifier {
    if (style == VisualStyle.ORIGINAL) return this
    return drawWithCache {
        val glow = Brush.linearGradient(listOf(accent.copy(alpha = .26f), Color.Transparent, accent.copy(alpha = .08f)), start = Offset(size.width, 0f), end = Offset(0f, size.height))
        val line = accent.copy(alpha = .30f)
        val pattern = Path().apply {
            when (style) {
                VisualStyle.PRISMA -> {
                    moveTo(size.width * .68f, 0f); lineTo(size.width, size.height * .46f)
                    lineTo(size.width * .75f, size.height); lineTo(size.width * .9f, 0f)
                }
                VisualStyle.PULSO -> {
                    moveTo(size.width * .35f, size.height)
                    cubicTo(size.width * .58f, size.height * .35f, size.width * .78f, size.height * 1.2f, size.width, size.height * .35f)
                }
                VisualStyle.HORIZONTE -> {
                    moveTo(size.width * .42f, size.height)
                    quadraticTo(size.width * .8f, size.height * .22f, size.width * 1.08f, size.height * .67f)
                }
                VisualStyle.PETROLEO -> {
                    moveTo(size.width * .65f, size.height)
                    quadraticTo(size.width * .9f, size.height * .45f, size.width, size.height * .55f)
                }
                VisualStyle.TINTA -> {
                    moveTo(size.width * .65f, size.height); lineTo(size.width, size.height * .38f)
                    moveTo(size.width * .75f, size.height); lineTo(size.width, size.height * .55f)
                }
                VisualStyle.ORIGINAL -> Unit
            }
        }
        val facet = Path().apply {
            moveTo(size.width * .74f, 0f)
            lineTo(size.width, size.height * .22f)
            lineTo(size.width, size.height * .9f)
            close()
        }
        val pulseLines = (0..3).map { index ->
            Path().apply {
                val offset = index * size.height * .075f
                moveTo(size.width * .42f, size.height + offset)
                cubicTo(size.width * .61f, size.height * .24f + offset, size.width * .78f, size.height * 1.05f + offset, size.width, size.height * .28f + offset)
            }
        }
        onDrawBehind {
            drawRect(glow)
            when (style) {
                VisualStyle.PRISMA -> drawPath(facet, accent.copy(alpha = .10f))
                VisualStyle.PULSO -> pulseLines.forEach { drawPath(it, accent.copy(alpha = .14f), style = Stroke(1f)) }
                VisualStyle.HORIZONTE -> drawCircle(accent.copy(alpha = .10f), radius = size.height * .4f, center = Offset(size.width * .92f, size.height * 1.05f))
                else -> Unit
            }
            drawPath(pattern, line, style = Stroke(width = 1.2f))
            if (style == VisualStyle.HORIZONTE) drawArc(line, 205f, 115f, false, topLeft = Offset(size.width * .65f, size.height * .3f), size = Size(size.width * .55f, size.height), style = Stroke(1.1f))
        }
    }
}
