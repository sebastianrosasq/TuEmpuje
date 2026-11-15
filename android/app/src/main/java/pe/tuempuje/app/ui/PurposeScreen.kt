package pe.tuempuje.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.BusinessCenter
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.tuempuje.app.ui.components.Empu
import pe.tuempuje.app.ui.components.EmpuAccessory
import pe.tuempuje.app.ui.components.EmpuMood

internal val workInterests = listOf("Ayudantía", "Creativo", "Diseño y edición", "Programación", "Ventas y atención", "Administración", "Eventos", "Logística", "Educación", "Servicios", "Otro")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PurposeScreen(initialRole: String?, initialInterests: Set<String>, saving: Boolean = false, saveError: String? = null, onComplete: (String, Set<String>) -> Unit) {
    var role by rememberSaveable { mutableStateOf(initialRole) }
    var step by rememberSaveable { mutableIntStateOf(0) }
    var selected by rememberSaveable { mutableStateOf(initialInterests.toList()) }
    BackHandler(enabled = step == 1) { step = 0 }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 3.dp, shadowElevation = 4.dp) {
                Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 22.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(
                        onClick = { if (step == 0 && role == "worker") step = 1 else onComplete(role ?: return@Button, selected.toSet()) },
                        enabled = !saving && if (step == 0) role != null else selected.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                        shape = MaterialTheme.shapes.large,
                    ) {
                        Text(if (saving) "Guardando…" else if (step == 1) "Ver oportunidades" else "Continuar")
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                    if (step == 1) TextButton(onClick = { step = 0 }, enabled = !saving, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Volver") }
                    if (step == 1 && selected.isEmpty()) TextButton(onClick = { onComplete("worker", emptySet()) }, enabled = !saving, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Aún no me decido") }
                }
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("TuEmpuje", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                    Text("TU PRÓXIMO PASO EMPIEZA AQUÍ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                    Text(if (step == 0) "01 / 02" else "02 / 02", modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
            GuideHero(step, role)
            saveError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
            if (step == 0) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("¿Qué quieres hacer?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Text("Puedes cambiar de opción cuando lo necesites", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                PurposeCard("Buscar trabajo", "Encuentra oportunidades para tus habilidades y tus primeras experiencias", Icons.Rounded.BusinessCenter, role == "worker") { role = "worker" }
                PurposeCard("Necesito ayuda", "Publica una tarea y conecta con personas que pueden apoyarte", Icons.Rounded.Storefront, role == "employer") { role = "employer" }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Elige tus intereses", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Text("Selecciona tus intereses para ver primero las oportunidades relacionadas, también puedes elegirlos después", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    workInterests.forEach { interest ->
                        val checked = interest in selected
                        FilterChip(
                            selected = checked,
                            onClick = { selected = if (checked) selected - interest else selected + interest },
                            label = { Text(interest) },
                            leadingIcon = if (checked) { { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp)) } } else null,
                            modifier = Modifier.heightIn(min = 48.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface,
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                    }
                }
                Text(if (selected.isEmpty()) "Puedes elegir tus intereses más adelante" else "${selected.size} ${if (selected.size == 1) "interés elegido" else "intereses elegidos"}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun GuideHero(step: Int, role: String?) {
    val colors = MaterialTheme.colorScheme
    Surface(shape = MaterialTheme.shapes.extraLarge, color = colors.primaryContainer, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.background(Brush.linearGradient(listOf(colors.primaryContainer, colors.secondaryContainer))).padding(start = 20.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(if (step == 0) "HOLA, SOY EMPU" else "VAMOS JUNTOS", style = MaterialTheme.typography.labelSmall, color = colors.onPrimaryContainer)
                Text(if (step == 0) "Cada oportunidad empieza contigo" else "Encontraremos tu camino", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colors.onPrimaryContainer)
                Text(if (step == 0) "Te acompaño en cada paso" else "Cuéntame qué te gustaría hacer", style = MaterialTheme.typography.bodySmall, color = colors.onPrimaryContainer)
            }
            Empu(if (step == 0) EmpuMood.HAPPY else EmpuMood.THINKING, Modifier.size(136.dp), accessory = if (role == "employer") EmpuAccessory.BUSINESS else EmpuAccessory.WORK)
        }
    }
}

@Composable
private fun PurposeCard(title: String, description: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val duration = if (LocalAppPreferences.current?.reduceMotion == true) 0 else 180
    val background by animateColorAsState(if (selected) colors.primaryContainer else colors.surface, animationSpec = tween(duration), label = "selección")
    val content = if (selected) colors.onPrimaryContainer else colors.onSurface
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = background,
        contentColor = content,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outlineVariant),
        modifier = Modifier.fillMaxWidth().selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(shape = MaterialTheme.shapes.medium, color = if (selected) colors.primary else colors.secondaryContainer, modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = if (selected) colors.onPrimary else colors.onSecondaryContainer) }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = content)
                Text(description, style = MaterialTheme.typography.bodySmall, color = content)
            }
            RadioButton(selected = selected, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = colors.primary, unselectedColor = colors.onSurfaceVariant))
        }
    }
}
