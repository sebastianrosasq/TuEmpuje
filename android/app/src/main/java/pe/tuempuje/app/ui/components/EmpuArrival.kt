package pe.tuempuje.app.ui.components

import android.animation.ValueAnimator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import pe.tuempuje.app.ui.LocalAppPreferences
import pe.tuempuje.app.ui.theme.EmpuTeal

@Composable
private fun idleTilt(allowMotion: Boolean): State<Float> {
    val owner = LocalLifecycleOwner.current
    var visible by remember(owner) { mutableStateOf(owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, _ -> visible = owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    if (!allowMotion || !visible) return remember { mutableStateOf(0f) }
    return rememberInfiniteTransition(label = "empu-reposo").animateFloat(
        initialValue = -1.4f, targetValue = 1.4f,
        animationSpec = infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "balanceo",
    )
}

@Composable
fun EmpuArrival(mood: EmpuMood, modifier: Modifier = Modifier, accessory: EmpuAccessory = EmpuAccessory.NONE) {
    val allowMotion = LocalAppPreferences.current?.reduceMotion != true && ValueAnimator.areAnimatorsEnabled()
    val drop = remember { Animatable(if (allowMotion) -90f else 0f) }
    val swing = remember { Animatable(if (allowMotion) -14f else 0f) }
    val thread = remember { Animatable(if (allowMotion) 1f else 0f) }
    LaunchedEffect(allowMotion) {
        if (!allowMotion) { drop.snapTo(0f); swing.snapTo(0f); thread.snapTo(0f); return@LaunchedEffect }
        coroutineScope {
            launch { drop.animateTo(0f, keyframes { durationMillis = 700; -90f at 0; 8f at 500; -5f at 590; 0f at 700 }) }
            launch { swing.animateTo(0f, keyframes { durationMillis = 850; -14f at 0; 8f at 500; -4f at 680; 0f at 850 }) }
            launch { kotlinx.coroutines.delay(450); thread.animateTo(0f, tween(300, easing = FastOutSlowInEasing)) }
        }
    }
    Box(modifier) {
        if (allowMotion && thread.value > 0f) Canvas(Modifier.matchParentSize()) {
            drawLine(EmpuTeal.copy(alpha = thread.value), Offset(size.width * .5f, 0f),
                Offset(size.width * .5f + swing.value * density, size.height * .38f), 2f * density, cap = StrokeCap.Round)
        }
        Empu(mood, Modifier.fillMaxSize().graphicsLayer { translationY = drop.value * density; translationX = swing.value * density; rotationZ = swing.value }, accessory)
    }
}

@Composable
fun EmpuGreeting(modifier: Modifier = Modifier, accessory: EmpuAccessory = EmpuAccessory.NONE) {
    val allowMotion = LocalAppPreferences.current?.reduceMotion != true && ValueAnimator.areAnimatorsEnabled()
    val idle = idleTilt(allowMotion)
    val drop = remember { Animatable(if (allowMotion) -100f else 0f) }
    val tilt = remember { Animatable(if (allowMotion) -65f else 0f) }
    val wave = remember { Animatable(-1f) }
    LaunchedEffect(allowMotion) {
        if (!allowMotion) { drop.snapTo(0f); tilt.snapTo(0f); wave.snapTo(-1f); return@LaunchedEffect }
        coroutineScope {
            launch { drop.animateTo(0f, keyframes { durationMillis = 700; -100f at 0; 8f at 480; -9f at 570; 0f at 700 }) }
            launch { tilt.animateTo(0f, keyframes { durationMillis = 700; -65f at 0; 16f at 480; -7f at 580; 0f at 700 }) }
        }
        wave.snapTo(0f)
        wave.animateTo(2f, tween(700, easing = LinearEasing))
        wave.snapTo(-1f)
    }
    Empu(EmpuMood.HAPPY, modifier.graphicsLayer { translationY = drop.value * density; rotationZ = tilt.value + idle.value }, accessory, wave = wave.value)
}

@Composable
fun EmpuWalkIn(mood: EmpuMood, modifier: Modifier = Modifier, accessory: EmpuAccessory = EmpuAccessory.NONE) {
    val allowMotion = LocalAppPreferences.current?.reduceMotion != true && ValueAnimator.areAnimatorsEnabled()
    val idle = idleTilt(allowMotion)
    val distance = remember { Animatable(if (allowMotion) 28f else 0f) }
    val stride = remember { Animatable(0f) }
    LaunchedEffect(allowMotion) {
        if (!allowMotion) { distance.snapTo(0f); stride.snapTo(0f); return@LaunchedEffect }
        coroutineScope {
            launch { distance.animateTo(0f, tween(700, easing = FastOutSlowInEasing)) }
            launch { stride.animateTo(12.56637f, tween(700, easing = LinearEasing)); stride.snapTo(0f) }
        }
    }
    Empu(mood, modifier.graphicsLayer { translationX = distance.value * density; rotationZ = idle.value }, accessory, walkPhase = stride.value)
}
