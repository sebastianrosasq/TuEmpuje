package pe.tuempuje.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.tuempuje.app.ui.theme.AppSpacing
import pe.tuempuje.app.ui.LocalAppPreferences
import pe.tuempuje.app.R

@Composable
fun AppNotice(message: String, modifier: Modifier = Modifier, isError: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
        shape = MaterialTheme.shapes.medium,
        color = if (isError) colors.errorContainer else colors.secondaryContainer,
        contentColor = if (isError) colors.onErrorContainer else colors.onSecondaryContainer,
    ) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
            Icon(if (isError) Icons.Rounded.ErrorOutline else Icons.Rounded.Info, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun AppLoadingState(message: String) {
    Row(
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
        Text(message, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun OpportunityLoadingState() {
    val placeholder = MaterialTheme.colorScheme.surfaceVariant
    Column(Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite; contentDescription = "Cargando oportunidades" }, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(2) {
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().clearAndSetSemantics {}) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.width(112.dp).height(20.dp).background(placeholder, RoundedCornerShape(8.dp)))
                    Box(Modifier.fillMaxWidth(.78f).height(24.dp).background(placeholder, RoundedCornerShape(8.dp)))
                    Box(Modifier.fillMaxWidth(.52f).height(14.dp).background(placeholder, RoundedCornerShape(8.dp)))
                    Spacer(Modifier.height(4.dp))
                    Box(Modifier.fillMaxWidth().height(48.dp).background(placeholder, RoundedCornerShape(12.dp)))
                }
            }
        }
    }
}

enum class EmptyIllustration { General, Intro, Search, Posts, Applications, Messages }

@Composable
fun AppEmptyState(title: String, message: String, action: String? = null, illustration: EmptyIllustration = EmptyIllustration.General, showMascot: Boolean = true, onAction: (() -> Unit)? = null) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(AppSpacing.large), verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
            val picture = when (illustration) {
                EmptyIllustration.Intro -> R.drawable.ilustracion_explorar
                EmptyIllustration.Search -> R.drawable.ilustracion_sin_resultados
                EmptyIllustration.Applications -> R.drawable.ilustracion_actividad
                EmptyIllustration.Messages -> R.drawable.ilustracion_mensajes
                else -> null
            }
            if (picture != null) {
                val maxHeight = if (LocalConfiguration.current.fontScale >= 1.5f) 104.dp else 144.dp
                BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Image(painterResource(picture), contentDescription = null, contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth().height(minOf(maxWidth / 2.1f, maxHeight)))
                }
            } else if (showMascot && LocalAppPreferences.current?.showMascot != false) {
                Empu(EmpuMood.EMPTY, Modifier.size(96.dp))
            } else EmptyOpportunityIllustration(illustration, Modifier.size(70.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (action != null && onAction != null) TextButton(onClick = onAction) { Text(action) }
        }
    }
}

@Composable
private fun EmptyOpportunityIllustration(kind: EmptyIllustration, modifier: Modifier = Modifier) {
    val paper = MaterialTheme.colorScheme.primaryContainer
    val line = MaterialTheme.colorScheme.onPrimaryContainer
    val accent = MaterialTheme.colorScheme.secondaryContainer
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawRoundRect(accent, topLeft = Offset(.10f * w, .25f * h), size = Size(.72f * w, .58f * h), cornerRadius = CornerRadius(.13f * w))
        drawRoundRect(paper, topLeft = Offset(.24f * w, .12f * h), size = Size(.63f * w, .65f * h), cornerRadius = CornerRadius(.12f * w))
        drawLine(line, Offset(.36f * w, .30f * h), Offset(.73f * w, .30f * h), strokeWidth = .035f * w)
        drawLine(line, Offset(.36f * w, .42f * h), Offset(.66f * w, .42f * h), strokeWidth = .035f * w)
        drawLine(line, Offset(.36f * w, .54f * h), Offset(.58f * w, .54f * h), strokeWidth = .035f * w)
        when (kind) {
            EmptyIllustration.Search -> {
                drawCircle(line, radius = .12f * w, center = Offset(.72f * w, .69f * h), style = Stroke(.035f * w))
                drawLine(line, Offset(.81f * w, .78f * h), Offset(.91f * w, .88f * h), strokeWidth = .04f * w)
            }
            EmptyIllustration.Posts -> {
                drawCircle(accent, radius = .16f * w, center = Offset(.76f * w, .73f * h))
                drawLine(line, Offset(.68f * w, .73f * h), Offset(.84f * w, .73f * h), strokeWidth = .035f * w)
                drawLine(line, Offset(.76f * w, .65f * h), Offset(.76f * w, .81f * h), strokeWidth = .035f * w)
            }
            EmptyIllustration.Applications -> {
                drawCircle(accent, radius = .16f * w, center = Offset(.76f * w, .73f * h))
                drawLine(line, Offset(.68f * w, .73f * h), Offset(.74f * w, .79f * h), strokeWidth = .035f * w)
                drawLine(line, Offset(.74f * w, .79f * h), Offset(.85f * w, .67f * h), strokeWidth = .035f * w)
            }
            EmptyIllustration.Intro, EmptyIllustration.Messages -> Unit
            EmptyIllustration.General -> Unit
        }
    }
}

@Composable
fun AppPrimaryButton(label: String, onClick: () -> Unit, enabled: Boolean = true) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val reducedMotion = LocalAppPreferences.current?.reduceMotion == true
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.985f else 1f,
        animationSpec = if (reducedMotion) tween(0) else spring(stiffness = Spring.StiffnessMedium),
        label = "pulsación",
    )
    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interaction,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).graphicsLayer { scaleX = scale; scaleY = scale },
    ) { Text(label) }
}
