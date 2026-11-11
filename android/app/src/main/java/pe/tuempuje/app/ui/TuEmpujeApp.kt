package pe.tuempuje.app.ui

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
import androidx.compose.ui.text.style.TextOverflow
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
import pe.tuempuje.app.ui.theme.Canvas
import pe.tuempuje.app.ui.theme.Ink
import pe.tuempuje.app.ui.theme.Quiet
import pe.tuempuje.app.ui.theme.Slate

private data class Destination(val route: String, val label: String, val icon: ImageVector)

private val destinations = listOf(
    Destination("explorar", "Explorar", Icons.Rounded.Explore),
    Destination("publicar", "Publicar", Icons.Rounded.AddCircleOutline),
    Destination("actividad", "Actividad", Icons.Rounded.BusinessCenter),
    Destination("perfil", "Perfil", Icons.Rounded.PersonOutline),
)

@Composable
fun TuEmpujeApp(model: OpportunityViewModel = viewModel()) {
    val nav = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    Scaffold(
        containerColor = Canvas,
        bottomBar = {
            if (currentRoute in destinations.map { it.route }) {
                Surface(color = Color.White.copy(alpha = 0.94f), tonalElevation = 3.dp, shadowElevation = 8.dp) {
                    NavigationBar(containerColor = Color.Transparent) {
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
                                    indicatorColor = Quiet,
                                    selectedIconColor = Ink,
                                    selectedTextColor = Ink,
                                    unselectedIconColor = Slate,
                                    unselectedTextColor = Slate,
                                ),
                            )
                        }
                    }
                }
            }
        },
    ) { padding ->
        NavHost(navController = nav, startDestination = "explorar", modifier = Modifier.padding(padding)) {
            composable("explorar") { ExploreScreen(model) { nav.navigate("detalle/$it") } }
            composable("publicar") {
                PublishScreen(model) {
                    nav.navigate("actividad") {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
            composable("actividad") { ActivityScreen(model) { nav.navigate("detalle/$it") } }
            composable("perfil") { ProfileScreen() }
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
                Text("${opportunities.size} disponibles", style = MaterialTheme.typography.labelMedium, color = Slate)
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
            Text("Oportunidades que conectan", style = MaterialTheme.typography.labelSmall, color = Slate)
        }
        Surface(shape = CircleShape, color = Quiet) {
            Text("AQ", modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp), color = Ink, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun DemoNotice() {
    Text(
        "Vista de demostración · datos ficticios guardados solo durante esta sesión",
        style = MaterialTheme.typography.labelMedium,
        color = Ink,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFE6ECF2)).padding(12.dp),
    )
}

@Composable
private fun Hero() {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp))
            .background(Brush.linearGradient(listOf(Ink, Color(0xFF3A5269))))
            .padding(22.dp),
    ) {
        Column {
            Text("TU PRÓXIMO PASO", style = MaterialTheme.typography.labelSmall, color = Color(0xFFDEE8F2), letterSpacing = 1.4.sp)
            Spacer(Modifier.height(12.dp))
            Text("Una oportunidad\npuede abrir camino", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(10.dp))
            Text("Conecta tus habilidades con proyectos y personas", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE0E8EE))
            Spacer(Modifier.height(18.dp))
            Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.16f), border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))) {
                Text("HECHO PARA EMPEZAR", modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
        }
    }
}

@Composable
private fun OpportunityCard(opportunity: Opportunity, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE3E8ED)),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(10.dp), color = Quiet) {
                    Text(opportunity.category.uppercase(), modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall, color = Ink)
                }
                Spacer(Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = Slate, modifier = Modifier.size(18.dp))
            }
            Text(opportunity.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(opportunity.organization, style = MaterialTheme.typography.bodyMedium, color = Slate)
            Spacer(Modifier.height(2.dp))
            HorizontalDivider(color = Quiet)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Rounded.Place, contentDescription = null, tint = Slate, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(opportunity.location, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall, color = Slate)
                Spacer(Modifier.width(8.dp))
                Text(opportunity.pay, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Ink)
            }
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
            Text(opportunity.category.uppercase(), style = MaterialTheme.typography.labelMedium, color = Slate)
            Text(opportunity.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(opportunity.organization, style = MaterialTheme.typography.titleMedium, color = Slate)
            Surface(shape = RoundedCornerShape(22.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailRow("Ubicación", opportunity.location)
                    DetailRow("Pago ofrecido", opportunity.pay)
                    DetailRow("Duración", opportunity.duration)
                }
            }
            Text("Sobre esta oportunidad", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(opportunity.description, style = MaterialTheme.typography.bodyLarge, color = Slate)
            if (!opportunity.own) {
                val applied = opportunity.id in model.applications
                Button(onClick = { model.apply(opportunity.id) }, enabled = !applied, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp)) {
                    Text(if (applied) "Postulación simulada" else "Simular postulación")
                }
                Text("Esta acción solo cambia la demostración local, no envía una postulación real", style = MaterialTheme.typography.bodySmall, color = Slate)
            } else {
                Text("Esta es tu publicación de prueba", style = MaterialTheme.typography.bodyMedium, color = Slate)
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Slate)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PublishScreen(model: OpportunityViewModel, onPublished: () -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var pay by rememberSaveable { mutableStateOf("") }
    var showError by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Publicar", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Cuéntanos qué apoyo necesitas", style = MaterialTheme.typography.bodyLarge, color = Slate)
        DemoNotice()
        Text("Los datos se mostrarán solo en esta demostración", style = MaterialTheme.typography.bodyMedium, color = Slate)
        OutlinedTextField(title, { title = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Título de la oportunidad") }, singleLine = true, shape = RoundedCornerShape(16.dp))
        OutlinedTextField(description, { description = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Tareas a realizar") }, minLines = 3, shape = RoundedCornerShape(16.dp))
        OutlinedTextField(location, { location = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Ubicación o remoto") }, singleLine = true, shape = RoundedCornerShape(16.dp))
        OutlinedTextField(pay, { pay = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Pago y unidad, por ejemplo S/ 60 por día") }, singleLine = true, shape = RoundedCornerShape(16.dp))
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
private fun ActivityScreen(model: OpportunityViewModel, open: (String) -> Unit) {
    val myPosts = model.opportunities.filter { it.own }
    val myApplications = model.opportunities.filter { it.id in model.applications }
    LazyColumn(contentPadding = PaddingValues(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Actividad", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
        item { Text("Sigue tus movimientos de prueba", style = MaterialTheme.typography.bodyLarge, color = Slate) }
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
        Surface(shape = RoundedCornerShape(24.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(72.dp).clip(CircleShape).background(Quiet), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.PersonOutline, contentDescription = null, tint = Ink, modifier = Modifier.size(36.dp))
                }
                Spacer(Modifier.height(12.dp))
                Text("Perfil de demostración", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("El registro y la edición de perfiles se incorporarán en una etapa posterior", style = MaterialTheme.typography.bodyMedium, color = Slate)
            }
        }
        Text("TuEmpuje conecta oportunidades, habilidades y personas", style = MaterialTheme.typography.bodyMedium, color = Slate)
    }
}

@Composable
private fun EmptyMessage(title: String, message: String) {
    Surface(shape = RoundedCornerShape(18.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = Slate)
        }
    }
}
