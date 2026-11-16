package pe.tuempuje.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import pe.tuempuje.app.ui.LocalAppPreferences
import pe.tuempuje.app.ui.VisualStyle

@Composable
fun BrandMark(modifier: Modifier = Modifier) {
    val top = remember { PathParser().parsePathString("M86,107 L86,65 Q86,51 100,46 L185,13 Q198,8 200,0 L200,48 Q200,60 187,65 L100,100 Q92,104 86,107 Z").toPath() }
    val middle = remember { PathParser().parsePathString("M0,170 L0,132 Q0,119 14,114 L86,87 L86,124 Q86,138 72,144 Z").toPath() }
    val base = remember { PathParser().parsePathString("M0,230 L0,193 Q0,178 14,173 L158,119 Q176,112 179,94 L179,145 Q179,163 163,170 Z").toPath() }
    val accent = when (LocalAppPreferences.current?.visualStyle ?: VisualStyle.ORIGINAL) {
        VisualStyle.ORIGINAL -> Color(0xFF20B3A0)
        VisualStyle.PRISMA -> Color(0xFFA893FF)
        VisualStyle.PULSO -> Color(0xFF50E3B1)
        VisualStyle.PETROLEO -> Color(0xFFD28463)
        VisualStyle.TINTA -> Color(0xFF527CF6)
        VisualStyle.HORIZONTE -> Color(0xFFFF806B)
    }
    Canvas(modifier) {
        val factor = minOf(size.width / 200f, size.height / 230f)
        val dx = (size.width - 200f * factor) / 2f
        val dy = (size.height - 230f * factor) / 2f
        drawContext.transform.translate(dx, dy)
        scale(factor, factor, pivot = Offset.Zero) {
            drawPath(top, accent)
            drawPath(middle, accent)
            drawPath(base, Color(0xFFD0DBE7))
        }
    }
}
