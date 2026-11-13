package pe.tuempuje.app.ui.components

import android.animation.ValueAnimator
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import pe.tuempuje.app.ui.LocalAppPreferences
import pe.tuempuje.app.ui.theme.EmpuInk
import pe.tuempuje.app.ui.theme.EmpuLight
import pe.tuempuje.app.ui.theme.EmpuTeal
import kotlin.math.cos
import kotlin.math.sin

enum class EmpuMood(val riveValue: Float) {
    IDLE(0f), LOOKING(1f), PASSWORD(2f), THINKING(3f), LOADING(4f),
    HAPPY(5f), SUCCESS(6f), SAD(7f), EMPTY(8f),
}

@Composable
internal fun EmpuFallback(mood: EmpuMood, modifier: Modifier = Modifier) {
    val motionEnabled = ValueAnimator.areAnimatorsEnabled() && LocalAppPreferences.current?.reduceMotion != true
    val owner = LocalLifecycleOwner.current
    var isVisible by remember(owner) { mutableStateOf(owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, _ ->
            isVisible = owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    var blink by remember { mutableStateOf(false) }
    LaunchedEffect(mood, motionEnabled, isVisible) {
        blink = false
        if (mood == EmpuMood.IDLE && motionEnabled && isVisible) {
            while (true) {
                delay(4500)
                blink = true
                delay(120)
                blink = false
            }
        }
    }
    val look by animateFloatAsState(
        targetValue = if (mood == EmpuMood.LOOKING || mood == EmpuMood.THINKING) 6f else 0f,
        animationSpec = tween(if (motionEnabled) 180 else 0), label = "mirada",
    )
    val drift = if (mood == EmpuMood.IDLE && motionEnabled && isVisible) {
        val transition = rememberInfiniteTransition(label = "reposo")
        val value by transition.animateFloat(
            initialValue = 0f, targetValue = -3f / 240f,
            animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
            label = "movimiento",
        )
        value
    } else 0f
    Canvas(modifier.aspectRatio(1f).graphicsLayer { translationY = drift * size.height }) {
        val u = size.minDimension / 240f
        fun p(x: Float, y: Float) = Offset(x * u, y * u)
        fun line(x1: Float, y1: Float, x2: Float, y2: Float, color: androidx.compose.ui.graphics.Color, width: Float) =
            drawLine(color, p(x1, y1), p(x2, y2), width * u, cap = StrokeCap.Round)
        val body = Path().apply {
            moveTo(52 * u, 185 * u); lineTo(188 * u, 185 * u)
            quadraticTo(202 * u, 185 * u, 202 * u, 171 * u)
            lineTo(202 * u, 74 * u); quadraticTo(202 * u, 60 * u, 188 * u, 60 * u)
            lineTo(145 * u, 60 * u); quadraticTo(131 * u, 60 * u, 131 * u, 74 * u)
            lineTo(131 * u, 79 * u); quadraticTo(131 * u, 91 * u, 119 * u, 91 * u)
            lineTo(94 * u, 91 * u); quadraticTo(82 * u, 91 * u, 82 * u, 103 * u)
            lineTo(82 * u, 108 * u); quadraticTo(82 * u, 120 * u, 70 * u, 120 * u)
            lineTo(52 * u, 120 * u); quadraticTo(38 * u, 120 * u, 38 * u, 134 * u)
            lineTo(38 * u, 171 * u); quadraticTo(38 * u, 185 * u, 52 * u, 185 * u); close()
        }
        if (mood != EmpuMood.PASSWORD) {
            line(38f, 143f, 25f, 149f, EmpuTeal, 9f)
            val angle = when (mood) {
                EmpuMood.HAPPY -> -.70; EmpuMood.SUCCESS -> -1.05
                EmpuMood.THINKING -> -.35; EmpuMood.SAD -> .35; else -> 0.0
            }
            val endX = (13 * cos(angle) - 6 * sin(angle)).toFloat()
            val endY = (13 * sin(angle) + 6 * cos(angle)).toFloat()
            line(202f, 143f, 202f + endX, 143f + endY, EmpuTeal, 9f)
        }
        drawPath(body, EmpuTeal)
        if (mood == EmpuMood.PASSWORD) {
            drawCircle(EmpuInk, 14 * u, p(92f, 108f)); drawCircle(EmpuInk, 14 * u, p(145f, 108f))
            line(40f, 140f, 87f, 112f, EmpuInk, 9f); line(198f, 140f, 150f, 112f, EmpuInk, 9f)
        } else {
            val eyeY = 108f
            if (blink) {
                line(82f, eyeY, 102f, eyeY, EmpuLight, 4f); line(135f, eyeY, 155f, eyeY, EmpuLight, 4f)
            } else {
                drawOval(EmpuLight, p(82f, 94f), Size(20 * u, 28 * u))
                drawOval(EmpuInk, p(90f + look, 104f), Size(8 * u, 10 * u))
                if (mood == EmpuMood.SUCCESS) {
                    line(135f, 109f, 145f, 104f, EmpuLight, 4f); line(145f, 104f, 155f, 109f, EmpuLight, 4f)
                } else {
                    drawOval(EmpuLight, p(135f, 94f), Size(20 * u, 28 * u))
                    drawOval(EmpuInk, p(143f + look, 104f), Size(8 * u, 10 * u))
                }
            }
        }
        val mouth = Path().apply {
            moveTo(107 * u, 142 * u)
            val curve = when (mood) {
                EmpuMood.SAD -> 136f
                EmpuMood.THINKING, EmpuMood.LOADING, EmpuMood.EMPTY -> 142f
                EmpuMood.HAPPY, EmpuMood.SUCCESS -> 153f
                else -> 147f
            }
            quadraticTo(120 * u, curve * u, 133 * u, 142 * u)
        }
        drawPath(mouth, EmpuLight, style = Stroke(4 * u, cap = StrokeCap.Round))
        if (mood == EmpuMood.THINKING) {
            line(79f, 87f, 98f, 91f, EmpuLight, 4f); line(138f, 91f, 155f, 84f, EmpuLight, 4f)
        }
        if (mood == EmpuMood.HAPPY) {
            drawCircle(EmpuLight, 3.5f * u, p(69f, 130f)); drawCircle(EmpuLight, 3.5f * u, p(168f, 130f))
        }
        if (mood == EmpuMood.SUCCESS) line(210f, 70f, 210f, 84f, EmpuTeal, 4f)
    }
}
