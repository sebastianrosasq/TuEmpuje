package pe.tuempuje.app.ui.components

import android.animation.ValueAnimator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import pe.tuempuje.app.ui.theme.EmpuInk
import pe.tuempuje.app.ui.theme.EmpuLight
import pe.tuempuje.app.ui.theme.EmpuTeal
import pe.tuempuje.app.ui.LocalAppPreferences

enum class EmpuMood(val riveValue: Float) {
    IDLE(0f), LOOKING(1f), PASSWORD(2f), THINKING(3f), LOADING(4f),
    HAPPY(5f), SUCCESS(6f), SAD(7f), EMPTY(8f),
}

/** La expresión depende del estado de la interfaz, nunca del contenido de los campos. */
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
    var loadingBeat by remember { mutableStateOf(false) }
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
    LaunchedEffect(mood, motionEnabled, isVisible) {
        loadingBeat = false
        if (mood == EmpuMood.LOADING && motionEnabled && isVisible) {
            while (true) {
                delay(650)
                loadingBeat = !loadingBeat
            }
        }
    }
    val look by animateFloatAsState(
        targetValue = if (mood == EmpuMood.LOOKING) 1f else 0f,
        animationSpec = tween(if (motionEnabled) 180 else 0),
        label = "mirada",
    )
    val tilt by animateFloatAsState(
        targetValue = when (mood) {
            EmpuMood.THINKING -> -3f
            EmpuMood.SAD -> 2f
            EmpuMood.SUCCESS -> -2f
            else -> 0f
        },
        animationSpec = tween(if (motionEnabled) 180 else 0),
        label = "expresión",
    )
    val breathing = if (mood == EmpuMood.IDLE && motionEnabled && isVisible) {
        val transition = rememberInfiniteTransition(label = "respiración")
        val scale by transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.012f,
            animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Reverse),
            label = "respiración",
        )
        scale
    } else 1f
    Canvas(modifier.aspectRatio(1f).graphicsLayer {
        scaleY = breathing
        rotationZ = tilt
        transformOrigin = TransformOrigin(.5f, .9f)
    }) {
        val s = size.minDimension
        fun p(x: Float, y: Float) = Offset(x * s, y * s)
        val body = Path().apply {
            moveTo(.15f * s, .83f * s)
            lineTo(.15f * s, .61f * s)
            quadraticTo(.15f * s, .54f * s, .22f * s, .54f * s)
            lineTo(.36f * s, .54f * s)
            lineTo(.36f * s, .39f * s)
            quadraticTo(.36f * s, .32f * s, .43f * s, .32f * s)
            lineTo(.56f * s, .32f * s)
            lineTo(.56f * s, .17f * s)
            quadraticTo(.56f * s, .10f * s, .63f * s, .10f * s)
            lineTo(.79f * s, .10f * s)
            quadraticTo(.86f * s, .10f * s, .86f * s, .17f * s)
            lineTo(.86f * s, .83f * s)
            quadraticTo(.86f * s, .91f * s, .78f * s, .91f * s)
            lineTo(.23f * s, .91f * s)
            quadraticTo(.15f * s, .91f * s, .15f * s, .83f * s)
            close()
        }
        drawPath(body, EmpuTeal)
        val eyesY = .65f
        if (mood != EmpuMood.PASSWORD) {
            drawLine(EmpuTeal, p(.17f, .73f), p(.09f, .78f), .055f * s, cap = StrokeCap.Round)
            drawLine(EmpuTeal, p(.84f, .73f), p(.92f, .78f), .055f * s, cap = StrokeCap.Round)
        }
        if (blink && mood == EmpuMood.IDLE) {
            listOf(.43f, .66f).forEach { x ->
                drawLine(EmpuLight, p(x - .045f, eyesY), p(x + .045f, eyesY), strokeWidth = .025f * s, cap = StrokeCap.Round)
            }
        } else {
            listOf(.43f, .66f).forEach { x ->
                drawCircle(EmpuLight, radius = .07f * s, center = p(x, eyesY))
                drawCircle(EmpuInk, radius = .027f * s, center = p(x + .018f * look, eyesY))
            }
        }
        when (mood) {
            EmpuMood.SAD -> drawArc(
                EmpuLight, 205f, 130f, false, topLeft = p(.475f, .80f), size = Size(.14f * s, .09f * s),
                style = Stroke(.018f * s, cap = StrokeCap.Round),
            )
            EmpuMood.EMPTY -> drawCircle(EmpuLight, .023f * s, p(.545f, .80f), style = Stroke(.012f * s))
            EmpuMood.THINKING, EmpuMood.LOADING -> drawLine(EmpuLight, p(.50f, .80f), p(.59f, .80f), .018f * s, cap = StrokeCap.Round)
            else -> drawArc(
                EmpuLight, 10f, 160f, false, topLeft = p(.475f, .76f), size = Size(.14f * s, .09f * s),
                style = Stroke(.018f * s, cap = StrokeCap.Round),
            )
        }
        if (mood == EmpuMood.THINKING) {
            drawLine(EmpuLight, p(.59f, .54f), p(.67f, .52f), .016f * s, cap = StrokeCap.Round)
        }
        if (mood == EmpuMood.LOADING) {
            drawCircle(EmpuLight, .018f * s, p(.30f, if (loadingBeat) .45f else .48f))
            drawCircle(EmpuLight, .012f * s, p(.25f, if (loadingBeat) .49f else .46f))
        }
        if (mood == EmpuMood.PASSWORD) {
            drawCircle(EmpuInk, .09f * s, p(.43f, eyesY))
            drawCircle(EmpuInk, .09f * s, p(.66f, eyesY))
            drawLine(EmpuInk, p(.18f, .76f), p(.41f, .65f), .07f * s, cap = StrokeCap.Round)
            drawLine(EmpuInk, p(.83f, .76f), p(.68f, .65f), .07f * s, cap = StrokeCap.Round)
        }
        if (mood == EmpuMood.SUCCESS || mood == EmpuMood.HAPPY) {
            drawCircle(EmpuLight, .018f * s, p(.26f, .68f))
            drawCircle(EmpuLight, .018f * s, p(.78f, .68f))
        }
        if (mood == EmpuMood.SUCCESS) {
            drawLine(EmpuLight, p(.89f, .28f), p(.95f, .24f), .018f * s, cap = StrokeCap.Round)
            drawLine(EmpuLight, p(.90f, .36f), p(.97f, .36f), .018f * s, cap = StrokeCap.Round)
        }
    }
}
