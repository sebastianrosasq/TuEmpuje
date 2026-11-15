package pe.tuempuje.app.ui.components

import android.animation.ValueAnimator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import pe.tuempuje.app.ui.LocalAppPreferences
import pe.tuempuje.app.ui.theme.EmpuTeal

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
