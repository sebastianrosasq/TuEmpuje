package pe.tuempuje.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.BusinessCenter
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import pe.tuempuje.app.R
import pe.tuempuje.app.data.Opportunity
import pe.tuempuje.app.data.OpportunityViewModel
import pe.tuempuje.app.data.FirebaseServices
import pe.tuempuje.app.ui.auth.AccountScreen
import pe.tuempuje.app.ui.theme.Ink

private data class Destination(val route: String, val label: String, val icon: ImageVector)

private val destinations = listOf(
    Destination("explorar", "Explorar", Icons.Rounded.Explore),
    Destination("publicar", "Publicar", Icons.Rounded.AddCircleOutline),
    Destination("actividad", "Actividad", Icons.Rounded.BusinessCenter),
    Destination("perfil", "Perfil", Icons.Rounded.PersonOutline),
)

@Composable
fun TuEmpujeApp(services: FirebaseServices?, model: OpportunityViewModel = viewModel()) {
    val nav = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (currentRoute in destinations.map { it.route }) {
                Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 16.dp, end = 16.dp, bottom = 10.dp)) {
                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        tonalElevation = 3.dp,
                        shadowElevation = 8.dp,
                    ) {
                        NavigationBar(containerColor = Color.Transparent, windowInsets = WindowInsets(0, 0, 0, 0)) {
                            destinations.forEach { item ->
                                NavigationBarItem(
                                    selected = currentRoute == item.route,
                                    onClick = {
                                        nav.navigate(item.route) {
                                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(item.icon, contentDescription = null) },
                                    label = { Text(item.label, maxLines = 1) },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        },
    ) { padding ->
        NavHost(navController = nav, startDestination = "explorar", modifier = Modifier.padding(padding)) {
            composable("explorar") { ExploreScreen(model) { nav.navigate("detalle/$it") { launchSingleTop = true } } }
            composable("publicar") {
                PublishScreen(model, onBack = { nav.popBackStack() }) {
                    nav.navigate("actividad") {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
            composable("actividad") { ActivityScreen(model) { nav.navigate("detalle/$it") { launchSingleTop = true } } }
            composable("perfil") {
                if (services != null) {
                    AccountScreen(services.auth, services.firestore, services.isLocal)
                } else {
                    ProfileScreen()
                }
            }
            composable("detalle/{id}") { entry ->
                val opportunity = model.find(entry.arguments?.getString("id").orEmpty())
                DetailScreen(opportunity, model, onBack = { nav.popBackStack() })
            }
        }
    }
}

@Composable
private fun ExploreScreen(model: OpportunityViewModel, open: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val opportunities = model.opportunities.filter {
        query.isBlank() || listOf(it.title, it.organization, it.category, it.location)
            .any { field -> field.contains(query.trim(), ignoreCase = true) }
    }
    LazyColumn(
        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 26.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { BrandHeader() }
        item { DemoNotice() }
        item { Hero() }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Buscar oportunidades") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Oportunidades", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("${opportunities.size} disponibles", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (opportunities.isEmpty()) {
            item { EmptyMessage("No encontramos coincidencias", "Prueba con otra palabra o ubicación") }
        }
        items(opportunities, key = { it.id }) { opportunity ->
            OpportunityCard(opportunity) { open(opportunity.id) }
        }
    }
}

@Composable
private fun BrandHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(Ink),
            contentAlignment = Alignment.Center,
        ) { Image(painterResource(R.drawable.tuempuje_symbol), contentDescription = "Símbolo de TuEmpuje", modifier = Modifier.size(24.dp)) }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("TuEmpuje", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Oportunidades que conectan", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
            Text("AQ", modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun DemoNotice() {
    Text(
        "Vista de demostración · datos ficticios guardados solo durante esta sesión",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(12.dp),
    )
}

@Composable
private fun Hero() {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp))
            .background(Brush.linearGradient(listOf(Ink, Color(0xFF3A5269))))
            .padding(20.dp),
    ) {
        Column {
            Text("TU PRÓXIMO PASO", style = MaterialTheme.typography.labelSmall, color = Color(0xFFDEE8F2), letterSpacing = 1.4.sp)
            Spacer(Modifier.height(8.dp))
            Text("Tu próximo paso empieza aquí", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(6.dp))
            Text("Conecta tus habilidades con proyectos y personas", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE0E8EE))
        }
    }
}

@Composable
private fun OpportunityCard(opportunity: Opportunity, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Text(opportunity.category.uppercase(), modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
            Text(opportunity.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(opportunity.organization, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(2.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Rounded.Place, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(opportunity.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(opportunity.pay, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun DetailScreen(opportunity: Opportunity?, model: OpportunityViewModel, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Volver")
        }
        DemoNotice()
        if (opportunity == null) {
            EmptyMessage("Oportunidad no disponible", "Vuelve al listado para elegir otra")
        } else {
            Text(opportunity.category.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(opportunity.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(opportunity.organization, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailRow("Ubicación", opportunity.location)
                    DetailRow("Pago ofrecido", opportunity.pay)
                    DetailRow("Duración", opportunity.duration)
                }
            }
            Text("Sobre esta oportunidad", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(opportunity.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!opportunity.own) {
                val applied = opportunity.id in model.applications
                Button(onClick = { model.apply(opportunity.id) }, enabled = !applied, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp)) {
                    Text(if (applied) "Postulación simulada" else "Simular postulación")
                }
                Text("Esta acción solo cambia la demostración local, no envía una postulación real", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text("Esta es tu publicación de prueba", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PublishScreen(model: OpportunityViewModel, onBack: () -> Unit, onPublished: () -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var pay by rememberSaveable { mutableStateOf("") }
    var showError by rememberSaveable { mutableStateOf(false) }
    var confirmDiscard by rememberSaveable { mutableStateOf(false) }
    val hasDraft = listOf(title, description, location, pay).any { it.isNotBlank() }

    BackHandler(enabled = hasDraft) { confirmDiscard = true }
    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            title = { Text("¿Descartar esta publicación?") },
            text = { Text("Los datos que ingresaste se perderán") },
            confirmButton = {
                TextButton(onClick = { confirmDiscard = false; onBack() }) { Text("Descartar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDiscard = false }) { Text("Seguir editando") }
            },
        )
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Publicar", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Cuéntanos qué apoyo necesitas", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        DemoNotice()
        Text("Los datos se mostrarán solo en esta demostración", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        DraftField(title, { title = it }, "Título de la oportunidad", "Ingresa un título", showError)
        DraftField(description, { description = it }, "Tareas a realizar", "Describe las tareas", showError, multiline = true)
        DraftField(location, { location = it }, "Ubicación o remoto", "Indica la ubicación", showError)
        DraftField(pay, { pay = it }, "Pago y unidad, por ejemplo S/ 60 por día", "Indica el pago y su unidad", showError)
        if (showError) Text("Completa todos los campos para continuar", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        Button(onClick = {
            if (model.publish(title, description, location, pay)) {
                title = ""; description = ""; location = ""; pay = ""; showError = false
                onPublished()
            } else showError = true
        }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp)) {
            Text("Guardar publicación de prueba")
        }
    }
}

@Composable
private fun DraftField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    errorMessage: String,
    showError: Boolean,
    multiline: Boolean = false,
) {
    val isInvalid = showError && value.isBlank()
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = !multiline,
        minLines = if (multiline) 3 else 1,
        isError = isInvalid,
        supportingText = if (isInvalid) { { Text(errorMessage) } } else null,
        shape = MaterialTheme.shapes.medium,
    )
}

@Composable
private fun ActivityScreen(model: OpportunityViewModel, open: (String) -> Unit) {
    val myPosts = model.opportunities.filter { it.own }
    val myApplications = model.opportunities.filter { it.id in model.applications }
    LazyColumn(contentPadding = PaddingValues(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Actividad", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
        item { Text("Sigue tus movimientos de prueba", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { DemoNotice() }
        item { Text("Mis publicaciones", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        if (myPosts.isEmpty()) item { EmptyMessage("Aún no tienes publicaciones", "Puedes crear una desde Publicar") }
        items(myPosts, key = { "post-${it.id}" }) { item -> OpportunityCard(item) { open(item.id) } }
        item { Text("Mis postulaciones", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        if (myApplications.isEmpty()) item { EmptyMessage("Aún no tienes postulaciones", "Explora una oportunidad para simular una postulación") }
        items(myApplications, key = { "application-${it.id}" }) { item -> OpportunityCard(item) { open(item.id) } }
    }
}

@Composable
private fun ProfileScreen() {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Perfil", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        DemoNotice()
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(72.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.PersonOutline, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(36.dp))
                }
                Spacer(Modifier.height(12.dp))
                Text("Perfil de demostración", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("El registro y la edición de perfiles se incorporarán en una etapa posterior", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text("TuEmpuje conecta oportunidades, habilidades y personas", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyMessage(title: String, message: String) {
    Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
