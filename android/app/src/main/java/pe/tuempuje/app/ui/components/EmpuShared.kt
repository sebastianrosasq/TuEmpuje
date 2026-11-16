package pe.tuempuje.app.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import pe.tuempuje.app.ui.LocalAppPreferences

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalEmpuSharedScope = staticCompositionLocalOf<SharedTransitionScope?> { null }
val LocalEmpuVisibilityScope = staticCompositionLocalOf<AnimatedVisibilityScope?> { null }

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedEmpu(size: Dp, content: @Composable (Modifier) -> Unit) {
    val shared = LocalEmpuSharedScope.current
    val visibility = LocalEmpuVisibilityScope.current
    val reduceMotion = LocalAppPreferences.current?.reduceMotion == true || !android.animation.ValueAnimator.areAnimatorsEnabled()
    val lift = with(LocalDensity.current) { 56.toFloat() * density }
    if (shared == null || visibility == null || reduceMotion) {
        content(Modifier.size(size)); return
    }
    with(shared) {
        val modifier = Modifier.sharedElement(
            sharedContentState = rememberSharedContentState(key = "empu-principal"),
            animatedVisibilityScope = visibility,
            boundsTransform = { initial, target ->
                val mid = Rect(
                    left = (initial.left + target.left) / 2f,
                    top = (initial.top + target.top) / 2f - lift,
                    right = (initial.right + target.right) / 2f,
                    bottom = (initial.bottom + target.bottom) / 2f - lift,
                )
                keyframes { durationMillis = 520; initial at 0; mid at 260; target at 520 }
            },
        ).size(size)
        content(modifier)
    }
}
