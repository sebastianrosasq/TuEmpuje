package pe.tuempuje.app.ui.components

import android.animation.ValueAnimator
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import pe.tuempuje.app.R
import pe.tuempuje.app.ui.LocalAppPreferences

@Composable
fun WelcomeIntro(onFinished: () -> Unit) {
    val preferences = LocalAppPreferences.current
    val allowMotion = preferences?.reduceMotion != true && ValueAnimator.areAnimatorsEnabled()
    val showMascot = preferences?.showMascot != false
    var logoVisible by remember { mutableStateOf(!showMascot) }
    val flip = remember { Animatable(0f) }
    val wave = remember { Animatable(0f) }
    val opacity = remember { Animatable(1f) }

    BackHandler { onFinished() }
    LaunchedEffect(allowMotion, showMascot) {
        if (!allowMotion) {
            onFinished()
            return@LaunchedEffect
        }
        if (showMascot) {
            wave.animateTo(-6f, tween(100))
            wave.animateTo(6f, tween(100))
            wave.animateTo(0f, tween(100))
            delay(100)
            flip.animateTo(90f, tween(130, easing = FastOutSlowInEasing))
            logoVisible = true
            flip.snapTo(-90f)
            flip.animateTo(0f, tween(130, easing = FastOutSlowInEasing))
        }
        delay(140)
        opacity.animateTo(0f, tween(120))
        onFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize().graphicsLayer { alpha = opacity.value }
            .background(MaterialTheme.colorScheme.background)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { }
            .clearAndSetSemantics { contentDescription = "TuEmpuje, iniciando" },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.graphicsLayer { rotationY = flip.value; cameraDistance = 12 * density },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (logoVisible) {
                Image(painterResource(R.drawable.tuempuje_symbol), contentDescription = null, modifier = Modifier.size(142.dp))
                Text("TuEmpuje", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            } else {
                Empu(EmpuMood.HAPPY, Modifier.size(160.dp).graphicsLayer { rotationZ = wave.value })
                Text("¡Hola!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}
