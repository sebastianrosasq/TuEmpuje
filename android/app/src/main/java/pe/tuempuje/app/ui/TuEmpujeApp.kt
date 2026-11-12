package pe.tuempuje.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
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
    var activityRevision by rememberSaveable { mutableIntStateOf(0) }
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
                    activityRevision++
                    nav.navigate("actividad") {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
            composable("actividad") { ActivityScreen(model, activityRevision) { nav.navigate("detalle/$it") { launchSingleTop = true } } }
            composable("perfil") {
                if (services != null) {
                    AccountScreen(services.auth, services.firestore, services.isLocal)
                } else {
                    ProfileScreen()
                }
            }
            composable("detalle/{id}") { entry ->
                val opportunity = model.find(entry.arguments?.getString("id").orEmpty())
                DetailScreen(opportunity, model, onBack = { nav.popBackStack() }, onCandidates = { nav.navigate("candidatos/$it") })
            }
            composable("candidatos/{id}") { entry ->
                val opportunity = model.find(entry.arguments?.getString("id").orEmpty())
                CandidatesScreen(opportunity, onBack = { nav.popBackStack() })
            }
        }
    }
}

@Composable
private fun ExploreScreen(model: OpportunityViewModel, open: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("Todas") }
    val categories = listOf("Todas") + model.opportunities.map { it.category }.distinct().sorted()
    val opportunities = model.opportunities.filter {
        (selectedCategory == "Todas" || it.category == selectedCategory) &&
            (query.isBlank() || listOf(it.title, it.organization, it.category, it.location)
                .any { field -> field.contains(query.trim(), ignoreCase = true) })
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
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                    )
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Oportunidades", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("${opportunities.size} disponibles", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (opportunities.isEmpty()) {
            item { EmptyMessage("No encontramos coincidencias", "Prueba con otra palabra o categoría") }
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
private fun OpportunityCard(opportunity: Opportunity, status: String? = null, onClick: () -> Unit) {
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
            if (status != null) {
                Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.tertiaryContainer) {
                    Text(status, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
                }
            }
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
private fun DetailScreen(opportunity: Opportunity?, model: OpportunityViewModel, onBack: () -> Unit, onCandidates: (String) -> Unit) {
    var confirmApplication by rememberSaveable(opportunity?.id) { mutableStateOf(false) }
    if (confirmApplication && opportunity != null && !opportunity.own) {
        AlertDialog(
            onDismissRequest = { confirmApplication = false },
            title = { Text("Simular postulación") },
            text = { Text("Marcaremos tu interés en ${opportunity.title} solo en esta sesión, no se enviará una solicitud real") },
            confirmButton = { TextButton(onClick = { model.apply(opportunity.id); confirmApplication = false }) { Text("Confirmar simulación") } },
            dismissButton = { TextButton(onClick = { confirmApplication = false }) { Text("Cancelar") } },
        )
    }
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
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailRow("Ubicación", opportunity.location)
                    DetailRow("Pago ofrecido", opportunity.pay)
                    DetailRow("Duración", opportunity.duration)
                    DetailRow("Vacantes", opportunity.vacancies.toString())
                }
            }
            Text("Sobre esta oportunidad", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(opportunity.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Conocimientos y requisitos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(opportunity.requirements, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!opportunity.own) {
                val applied = opportunity.id in model.applications
                Button(onClick = { confirmApplication = true }, enabled = !applied, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp)) {
                    Text(if (applied) "Postulación simulada" else "Simular postulación")
                }
                Text("Esta acción solo cambia la demostración local, no envía una postulación real", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text("Esta es tu publicación de prueba", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FilledTonalButton(onClick = { onCandidates(opportunity.id) }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                    Text("Ver diseño de candidatos")
                }
            }
        }
    }
}

@Composable
private fun CandidatesScreen(opportunity: Opportunity?, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Volver")
        }
        if (opportunity == null || !opportunity.own) {
            EmptyMessage("Publicación no disponible", "Regresa a tu actividad para revisar tus publicaciones")
        } else {
            Text("Candidatos", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(opportunity.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Vista de diseño · estos perfiles son ejemplos y no son postulaciones reales", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.secondaryContainer).padding(14.dp), color = MaterialTheme.colorScheme.onSecondaryContainer)
            CandidatePreview("Lucía M.", "Estudiante · disponibilidad por las tardes", "Organización y comunicación")
            CandidatePreview("Diego R.", "Estudiante · disponibilidad de fin de semana", "Apoyo en eventos y atención")
            Text("La revisión de perfiles y la selección se conectarán en una próxima etapa", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CandidatePreview(name: String, availability: String, skills: String) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.PersonOutline, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
                Column {
                    Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(availability, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(skills, style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) { Text("Seleccionar · próximamente") }
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
    var duration by rememberSaveable { mutableStateOf("") }
    var requirements by rememberSaveable { mutableStateOf("") }
    var vacancies by rememberSaveable { mutableStateOf("1") }
    var category by rememberSaveable { mutableStateOf("Ayudantía") }
    var showError by rememberSaveable { mutableStateOf(false) }
    var confirmDiscard by rememberSaveable { mutableStateOf(false) }
    val hasDraft = listOf(title, description, location, pay, duration, requirements).any { it.isNotBlank() } || vacancies != "1" || category != "Ayudantía"

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
        FormSectionHeading("01", "La oportunidad", "Describe el apoyo que necesitas")
        Text("Tipo de trabajo", style = MaterialTheme.typography.labelLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("Ayudantía", "Creativo", "Eventos", "Otro")) { item ->
                FilterChip(selected = category == item, onClick = { category = item }, label = { Text(item) })
            }
        }
        DraftField(title, { title = it }, "Título de la oportunidad", "Ingresa un título", showError)
        DraftField(description, { description = it }, "Tareas a realizar", "Describe las tareas", showError, multiline = true)
        DraftField(requirements, { requirements = it }, "Conocimientos o requisitos", "Indica qué necesita saber la persona", showError, multiline = true)
        FormSectionHeading("02", "Condiciones", "Aclara el tiempo, el lugar y el pago")
        DraftField(location, { location = it }, "Ubicación o remoto", "Indica la ubicación", showError)
        DraftField(pay, { pay = it }, "Pago y unidad, por ejemplo S/ 60 por día", "Indica el pago y su unidad", showError)
        DraftField(duration, { duration = it }, "Duración o fecha", "Indica la duración o fecha", showError)
        val invalidVacancies = vacancies.toIntOrNull() !in 1..99
        OutlinedTextField(
            value = vacancies,
            onValueChange = { vacancies = it.filter(Char::isDigit).take(2) },
            label = { Text("Número de vacantes") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = showError && invalidVacancies,
            supportingText = if (showError && invalidVacancies) { { Text("Indica entre 1 y 99 vacantes") } } else null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
        )
        if (showError) Text("Completa todos los campos para continuar", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        Button(onClick = {
            if (model.publish(title, description, location, pay, duration, requirements, vacancies.toIntOrNull() ?: 0, category)) {
                title = ""; description = ""; location = ""; pay = ""; duration = ""; requirements = ""; vacancies = "1"; category = "Ayudantía"; showError = false
                onPublished()
            } else showError = true
        }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp)) {
            Text("Guardar publicación de prueba")
        }
    }
}

@Composable
private fun FormSectionHeading(number: String, title: String, description: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primaryContainer) {
            Text(number, modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
        }
        Column {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
private fun ActivityScreen(model: OpportunityViewModel, activityRevision: Int, open: (String) -> Unit) {
    val myPosts = model.opportunities.filter { it.own }
    val myApplications = model.opportunities.filter { it.id in model.applications }
    var section by rememberSaveable(activityRevision) { mutableStateOf("Publicaciones") }
    LazyColumn(contentPadding = PaddingValues(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Actividad", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
        item { Text("Sigue tus movimientos de prueba", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { DemoNotice() }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { FilterChip(selected = section == "Publicaciones", onClick = { section = "Publicaciones" }, label = { Text("Publicaciones (${myPosts.size})") }) }
                item { FilterChip(selected = section == "Postulaciones", onClick = { section = "Postulaciones" }, label = { Text("Postulaciones (${myApplications.size})") }) }
            }
        }
        if (section == "Publicaciones") {
            if (myPosts.isEmpty()) item { EmptyMessage("Aún no tienes publicaciones", "Puedes crear una desde Publicar") }
            items(myPosts, key = { "post-${it.id}" }) { item -> OpportunityCard(item, "Publicación de prueba") { open(item.id) } }
        } else {
            if (myApplications.isEmpty()) item { EmptyMessage("Aún no tienes postulaciones", "Explora una oportunidad para simular una postulación") }
            items(myApplications, key = { "application-${it.id}" }) { item -> OpportunityCard(item, "Postulación simulada") { open(item.id) } }
        }
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
