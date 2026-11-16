package pe.tuempuje.app.ui.components

import android.animation.ValueAnimator
import android.util.Log
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.rive.Result as RiveResult
import app.rive.RenderBackend
import app.rive.Rive
import app.rive.RiveFile
import app.rive.RiveFileSource
import app.rive.RivePointerInputMode
import app.rive.RiveLog
import app.rive.ViewModelSource
import app.rive.rememberRiveFile
import app.rive.rememberRiveWorkerOrNull
import app.rive.rememberViewModelInstanceResult
import app.rive.runtime.kotlin.core.Rive as RiveRuntime
import pe.tuempuje.app.BuildConfig
import pe.tuempuje.app.ui.LocalAppPreferences
import pe.tuempuje.app.R
import pe.tuempuje.app.ui.theme.EmpuInk
import pe.tuempuje.app.ui.theme.EmpuLight
import pe.tuempuje.app.ui.theme.EmpuTeal
import kotlin.math.sin
import kotlinx.coroutines.launch

private val LocalEmpuFile = staticCompositionLocalOf<RiveResult<RiveFile>?> { null }

@Composable
fun EmpuProvider(content: @Composable () -> Unit) {
    val preferences = LocalAppPreferences.current
    val context = LocalContext.current.applicationContext
    val nativeReady = remember(context) {
        try {
            if (BuildConfig.DEBUG) RiveLog.logger = RiveLog.LogcatLogger()
            RiveRuntime.init(context)
            true
        } catch (error: LinkageError) {
            Log.e("Empu", "No se pudo cargar el motor de Rive", error)
            false
        } catch (error: Exception) {
            Log.e("Empu", "No se pudo iniciar el motor de Rive", error)
            false
        }
    }
    val animationsEnabled = preferences?.showMascot != false && preferences?.reduceMotion != true && ValueAnimator.areAnimatorsEnabled()
    val file = if (nativeReady && animationsEnabled) {
        val error = remember { mutableStateOf<Throwable?>(null) }
        val worker = rememberRiveWorkerOrNull(error, renderBackend = RenderBackend.Vulkan)
        val resource = worker?.let { rememberRiveFile(RiveFileSource.RawRes.from(R.raw.empu), it) }
        LaunchedEffect(error.value, resource) {
            error.value?.let { Log.e("Empu", "No se pudo iniciar Rive", it) }
            if (resource is RiveResult.Error) Log.e("Empu", "No se pudo abrir el recurso", resource.throwable)
        }
        resource
    } else null
    CompositionLocalProvider(LocalEmpuFile provides file, content = content)
}

enum class EmpuAccessory { NONE, WORK, BUSINESS }

@Composable
fun Empu(mood: EmpuMood, modifier: Modifier = Modifier, accessory: EmpuAccessory = EmpuAccessory.NONE, walkPhase: Float = 0f, wave: Float = -1f) {
    val preferences = LocalAppPreferences.current
    if (preferences?.showMascot == false) {
        Spacer(modifier.aspectRatio(1f))
        return
    }
    val motionEnabled = preferences?.reduceMotion != true && ValueAnimator.areAnimatorsEnabled()
    val bounce = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    BoxWithConstraints(modifier.aspectRatio(1f).graphicsLayer { translationY = bounce.value * density; if (wave >= 0f) rotationZ = sin(wave * 6.2831855f) * 6f }
        .semantics { contentDescription = "Empu" }
        .clickable(enabled = motionEnabled, role = Role.Button, onClickLabel = "Saludar a Empu") {
            scope.launch { bounce.stop(); bounce.animateTo(-8f, tween(120)); bounce.animateTo(0f, tween(180)) }
        }) {
        Canvas(Modifier.matchParentSize()) {
            val u = size.minDimension / 240f
            fun foot(x: Float, direction: Float, phase: Float) {
                val stride = sin(phase) * 11f
                val lift = maxOf(0f, sin(phase)) * 7f
                drawLine(EmpuTeal, Offset(x * u, 181 * u), Offset((x + stride) * u, (199 - lift) * u), 8 * u, cap = StrokeCap.Round)
                drawLine(EmpuTeal, Offset((x + stride) * u, (199 - lift) * u), Offset((x + stride + direction * 9) * u, (199 - lift) * u), 8 * u, cap = StrokeCap.Round)
            }
            foot(88f, -1f, walkPhase)
            foot(161f, 1f, -walkPhase)
        }
        EmpuArtwork(mood, Modifier.matchParentSize())
        if (accessory != EmpuAccessory.NONE) Canvas(Modifier.matchParentSize()) {
            val u = size.minDimension / 240f
            if (accessory == EmpuAccessory.WORK) {
                val cap = Path().apply {
                    moveTo(139 * u, 61 * u); lineTo(145 * u, 50 * u); quadraticTo(163 * u, 40 * u, 181 * u, 51 * u)
                    lineTo(187 * u, 61 * u); close()
                }
                drawPath(cap, EmpuInk)
                drawLine(EmpuInk, Offset(135 * u, 61 * u), Offset(192 * u, 61 * u), 5 * u, cap = StrokeCap.Round)
            } else {
                val tie = Path().apply {
                    moveTo(113 * u, 160 * u); lineTo(121 * u, 165 * u); lineTo(129 * u, 160 * u)
                    lineTo(129 * u, 173 * u); lineTo(121 * u, 168 * u); lineTo(113 * u, 173 * u); close()
                }
                drawPath(tie, EmpuLight)
            }
        }
        when (mood) {
            EmpuMood.THINKING -> Text(
                "?", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold,
                fontSize = (maxWidth.value * .25f).sp,
                modifier = Modifier.align(Alignment.TopEnd).padding(end = maxWidth * .08f),
            )
            EmpuMood.SAD -> Icon(
                Icons.Rounded.WaterDrop, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.TopEnd).padding(end = maxWidth * .08f, top = maxWidth * .13f).size(maxWidth * .16f),
            )
            EmpuMood.SUCCESS -> Canvas(Modifier.matchParentSize()) {
                val u = size.minDimension / 240f
                val tint = EmpuTeal
                fun sparkle(x: Float, y: Float, radius: Float) {
                    drawLine(tint, Offset(x * u, (y - radius) * u), Offset(x * u, (y + radius) * u), 4 * u, cap = StrokeCap.Round)
                    drawLine(tint, Offset((x - radius) * u, y * u), Offset((x + radius) * u, y * u), 4 * u, cap = StrokeCap.Round)
                }
                sparkle(213f, 72f, 9f)
                sparkle(34f, 94f, 6f)
            }
            EmpuMood.HAPPY -> Canvas(Modifier.matchParentSize()) {
                val u = size.minDimension / 240f
                drawLine(EmpuTeal, Offset(24 * u, 99 * u), Offset(17 * u, 91 * u), 4 * u, cap = StrokeCap.Round)
                drawLine(EmpuTeal, Offset(28 * u, 87 * u), Offset(26 * u, 77 * u), 4 * u, cap = StrokeCap.Round)
            }
            EmpuMood.LOADING -> Canvas(Modifier.matchParentSize()) {
                val u = size.minDimension / 240f
                listOf(180f, 194f, 208f).forEach { x -> drawCircle(EmpuTeal, 3.5f * u, Offset(x * u, 45 * u)) }
            }
            else -> Unit
        }
    }
}

@Composable
private fun EmpuArtwork(mood: EmpuMood, modifier: Modifier = Modifier) {
    val file = LocalEmpuFile.current
    val preferences = LocalAppPreferences.current
    if (!ValueAnimator.areAnimatorsEnabled() || preferences?.reduceMotion == true || file !is RiveResult.Success) {
        EmpuFallback(mood, modifier)
        return
    }
    val instance = rememberViewModelInstanceResult(file.value, ViewModelSource.Named("EmpuData").defaultInstance())
    LaunchedEffect(instance) {
        if (instance is RiveResult.Error) Log.e("Empu", "No se pudo cargar el estado", instance.throwable)
        if (BuildConfig.DEBUG && instance is RiveResult.Success) Log.d("Empu", "Recurso y estados de Rive disponibles")
    }
    when (instance) {
        is RiveResult.Success -> {
            LaunchedEffect(instance.value, mood) { instance.value.setNumber("mood", mood.riveValue) }
            var frameReady by remember(file.value, instance.value) { mutableStateOf(false) }
            Box(modifier.aspectRatio(1f)) {
                if (!frameReady) EmpuFallback(mood, Modifier.fillMaxSize())
                Rive(
                    file = file.value,
                    viewModelInstance = instance.value,
                    modifier = Modifier.matchParentSize(),
                    pointerInputMode = RivePointerInputMode.Observe,
                    onBitmapAvailable = { frameReady = true },
                )
            }
        }
        else -> EmpuFallback(mood, modifier)
    }
}
