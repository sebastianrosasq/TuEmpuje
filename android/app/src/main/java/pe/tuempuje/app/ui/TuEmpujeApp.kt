package pe.tuempuje.app.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
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
import androidx.compose.material.icons.rounded.WorkOutline
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.outlined.Explore as ExploreOutline
import androidx.compose.material.icons.outlined.AddCircleOutline as AddOutline
import androidx.compose.material.icons.outlined.BusinessCenter as BusinessOutline
import androidx.compose.material.icons.outlined.Person as PersonOutlineIcon
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import pe.tuempuje.app.R
import pe.tuempuje.app.BuildConfig
import pe.tuempuje.app.data.Opportunity
import pe.tuempuje.app.data.OpportunityDraft
import pe.tuempuje.app.data.OpportunityViewModel
import pe.tuempuje.app.data.OpportunityFilters
import pe.tuempuje.app.data.searchOpportunities
import pe.tuempuje.app.data.OpportunityViewModelFactory
import pe.tuempuje.app.data.FirebaseServices
import pe.tuempuje.app.notifications.NotificationScheduler
import pe.tuempuje.app.ui.auth.AccountScreen
import pe.tuempuje.app.ui.components.AppEmptyState
import pe.tuempuje.app.ui.components.AppNotice
import pe.tuempuje.app.ui.components.AppPrimaryButton
import pe.tuempuje.app.ui.components.AppLoadingState
import pe.tuempuje.app.ui.components.OpportunityLoadingState
import pe.tuempuje.app.ui.components.Empu
import pe.tuempuje.app.ui.components.EmpuAccessory
import pe.tuempuje.app.ui.components.EmpuArrival
import pe.tuempuje.app.ui.components.EmpuGreeting
import pe.tuempuje.app.ui.components.EmpuWalkIn
import pe.tuempuje.app.ui.components.EmpuMood
import pe.tuempuje.app.ui.components.EmpuProvider
import pe.tuempuje.app.ui.components.EmptyIllustration
import pe.tuempuje.app.ui.components.WelcomeIntro
import pe.tuempuje.app.ui.theme.EmpuLight
import pe.tuempuje.app.ui.theme.AppSpacing
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class Destination(val route: String, val label: String, val icon: ImageVector, val selectedIcon: ImageVector)

private data class WorkTemplate(val label: String, val title: String, val description: String, val requirements: String, val category: String)
private val workTemplates = listOf(
    WorkTemplate("Campaña", "Apoyo en campaña", "Apoyar en la preparación y atención durante una campaña, según las tareas que coordinemos", "Comunicación y puntualidad", "Eventos"),
    WorkTemplate("Contenido digital", "Edición de contenido", "Preparar y editar piezas de contenido con los materiales y pautas que proporcionaremos", "Conocimientos básicos de edición", "Diseño y edición"),
    WorkTemplate("Ayudantía", "Asistente para proyecto", "Apoyar al responsable del proyecto con la organización de materiales y las tareas acordadas", "Organización y disposición para aprender", "Ayudantía"),
    WorkTemplate("Apoyo en negocio", "Apoyo en atención", "Apoyar con la atención y organización de productos durante el horario acordado", "Trato cordial y responsabilidad", "Ventas y atención"),
)

private val destinations = listOf(
    Destination("explorar", "Explorar", Icons.Outlined.ExploreOutline, Icons.Rounded.Explore),
    Destination("publicar", "Publicar", Icons.Outlined.AddOutline, Icons.Rounded.AddCircle),
    Destination("actividad", "Actividad", Icons.Outlined.BusinessOutline, Icons.Rounded.BusinessCenter),
    Destination("mensajes", "Mensajes", Icons.Rounded.ChatBubbleOutline, Icons.Rounded.ChatBubble),
    Destination("perfil", "Perfil", Icons.Outlined.PersonOutlineIcon, Icons.Rounded.Person),
)

@Composable
fun TuEmpujeApp(services: FirebaseServices?, resetCode: String? = null, onResetConsumed: () -> Unit = {}, notificationDestination: String? = null, notificationRevision: Int = 0, model: OpportunityViewModel = viewModel(factory = OpportunityViewModelFactory(services))) {
    var showIntro by rememberSaveable { mutableStateOf(true) }
    val preferences = LocalAppPreferences.current
    val appContext = LocalContext.current.applicationContext
    LaunchedEffect(model.userId, preferences?.backgroundNotifications) {
        NotificationScheduler.update(appContext, model.userId, preferences?.backgroundNotifications == true)
    }
    var choosePurpose by rememberSaveable { mutableStateOf(preferences?.purpose == null) }
    var purposeSaving by remember { mutableStateOf(false) }
    var purposeError by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(model.userId, model.profileLoaded, model.profileRole, model.profileInterests) {
        if (model.profileLoaded && preferences != null) {
            if (model.profileRole != null) preferences.updatePurpose(model.profileRole!!, model.profileInterests)
            else preferences.purpose?.let { model.savePurpose(it, preferences.interests) }
        }
    }
    EmpuProvider {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.onBackground) {
            Box(Modifier.fillMaxSize()) {
                if (choosePurpose && resetCode == null) PurposeScreen(preferences?.purpose, preferences?.interests.orEmpty(), purposeSaving, purposeError) { role, interests ->
                    purposeError = null
                    if (model.userId == null) {
                        preferences?.updatePurpose(role, interests)
                        choosePurpose = false
                    } else {
                        purposeSaving = true
                        model.savePurpose(role, interests) { saved ->
                            purposeSaving = false
                            if (saved) { preferences?.updatePurpose(role, interests); choosePurpose = false }
                            else purposeError = "No se pudieron guardar tus preferencias, revisa tu conexión e inténtalo nuevamente"
                        }
                    }
                } else if (services != null && (model.userId == null || !model.profileLoaded)) {
                    Box(Modifier.fillMaxSize().safeDrawingPadding().imePadding(), contentAlignment = Alignment.TopCenter) {
                        Box(Modifier.widthIn(max = 600.dp).fillMaxWidth()) {
                            AccountScreen(services.auth, services.firestore, onSettings = {}, onSaved = {}, onChoosePurpose = { choosePurpose = true }, resetCode = resetCode, onResetConsumed = onResetConsumed, authGate = true)
                        }
                    }
                } else if (services != null) TuEmpujeContent(services, model, resetCode, onResetConsumed, notificationDestination, notificationRevision, onChoosePurpose = { choosePurpose = true })
                else Box(Modifier.safeDrawingPadding().padding(22.dp)) { AppNotice("No se pudo conectar con el servicio de cuentas") }
                if (showIntro) WelcomeIntro { showIntro = false }
            }
        }
    }
}

@Composable
private fun TuEmpujeContent(services: FirebaseServices?, model: OpportunityViewModel, resetCode: String?, onResetConsumed: () -> Unit, notificationDestination: String?, notificationRevision: Int, onChoosePurpose: () -> Unit) {
    val nav = rememberNavController()
    val preferences = LocalAppPreferences.current
    val reduceMotion = preferences?.reduceMotion == true
    LaunchedEffect(model.userId, preferences) { preferences?.selectAccount(model.userId) }
    LaunchedEffect(model.opportunities.toList(), model.feedFromCache, preferences?.savedIds) {
        if (!model.feedFromCache) preferences?.refreshSaved(model.opportunities.toList())
    }
    var activityRevision by rememberSaveable { mutableIntStateOf(0) }
    var openWorkProfile by rememberSaveable { mutableStateOf(false) }
    var cvOpportunityId by rememberSaveable { mutableStateOf<String?>(null) }
    var cvOpportunityTitle by rememberSaveable { mutableStateOf("") }
    var cvOpportunityRequirements by rememberSaveable { mutableStateOf("") }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val availableDestinations = if (preferences?.purpose == "employer") destinations else destinations.filterNot { it.route == "publicar" }
    val navigationGroups = if (LocalConfiguration.current.fontScale >= 1.8f) availableDestinations.chunked(if (availableDestinations.size > 4) 3 else 2) else listOf(availableDestinations)
    val openDestination: (String) -> Unit = { route ->
        val current = nav.currentDestination?.route
        if (route in availableDestinations.map { it.route } && current != route) nav.navigate(route) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    LaunchedEffect(resetCode) { if (resetCode != null) openDestination("perfil") }
    LaunchedEffect(notificationRevision) { if (notificationRevision > 0 && notificationDestination != null) openDestination(notificationDestination) }
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (currentRoute in destinations.map { it.route }) {
                Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 16.dp, end = 16.dp, bottom = 10.dp), contentAlignment = Alignment.Center) {
                    Surface(
                        modifier = Modifier.widthIn(max = 600.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        tonalElevation = 3.dp,
                        shadowElevation = 8.dp,
                    ) {
                        Column {
                            navigationGroups.forEach { group ->
                                NavigationBar(containerColor = Color.Transparent, windowInsets = WindowInsets(0, 0, 0, 0)) {
                                    group.forEach { item ->
                                        NavigationBarItem(
                                            selected = currentRoute == item.route,
                                            onClick = { openDestination(item.route) },
                                            icon = { Icon(if (currentRoute == item.route) item.selectedIcon else item.icon, contentDescription = null) },
                                            label = { Text(item.label, style = MaterialTheme.typography.labelSmall, maxLines = 2, textAlign = TextAlign.Center) },
                                            colors = NavigationBarItemDefaults.colors(
                                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
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
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).imePadding(), contentAlignment = Alignment.TopCenter) {
            NavHost(
                navController = nav,
                startDestination = if (preferences?.purpose == "employer") "publicar" else "explorar",
                modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth(),
                enterTransition = {
                    if (targetState.destination.route?.startsWith("detalle/") == true) {
                        fadeIn(tween(if (reduceMotion) 0 else 200)) + slideInHorizontally(tween(if (reduceMotion) 0 else 220)) { it / 14 }
                    } else if (initialState.destination.route in destinations.map { it.route } && targetState.destination.route in destinations.map { it.route }) {
                        EnterTransition.None
                    } else fadeIn(tween(if (reduceMotion) 0 else 180))
                },
                exitTransition = {
                    if (initialState.destination.route in destinations.map { it.route } && targetState.destination.route in destinations.map { it.route }) ExitTransition.None
                    else fadeOut(tween(if (reduceMotion) 0 else 120))
                },
                popEnterTransition = { if (reduceMotion) EnterTransition.None else slideInHorizontally(tween(220)) { -it / 4 } },
                popExitTransition = { if (reduceMotion) ExitTransition.None else slideOutHorizontally(tween(220)) { it } },
            ) {
                composable("explorar") {
                    DestinationSurface { ExploreScreen(model, onSaved = { nav.navigate("guardados") }, onSettings = { nav.navigate("ajustes") }, onAccount = { openDestination("perfil") }, onPublish = { openDestination("publicar") }) { nav.navigate("detalle/$it") { launchSingleTop = true } } }
                }
                composable("publicar") {
                    if (preferences?.purpose == "employer") DestinationSurface { PublishScreen(services, model, onBack = { nav.popBackStack() }, onAccount = { openDestination("perfil") }) {
                        activityRevision++
                        openDestination("actividad")
                        scope.launch { snackbar.showSnackbar("Oportunidad publicada") }
                    } }
                }
                composable("actividad") {
                    DestinationSurface { ActivityScreen(model, activityRevision, openDestination) { nav.navigate("detalle/$it") { launchSingleTop = true } } }
                }
                composable("mensajes") {
                    DestinationSurface { InboxScreen(services, model) { job, applicant -> nav.navigate("conversacion/$job/$applicant") } }
                }
                composable("perfil") {
                    DestinationSurface {
                    if (services != null) {
                        AccountScreen(services.auth, services.firestore, onSettings = { nav.navigate("ajustes") }, onSaved = { nav.navigate("guardados") }, onChoosePurpose = onChoosePurpose, resetCode = resetCode, onResetConsumed = onResetConsumed, onHelp = { nav.navigate("ayuda") }, openWorkProfile = openWorkProfile, onWorkProfileOpened = { openWorkProfile = false }, cvOpportunity = cvOpportunityId?.let { CvOpportunityContext(it, cvOpportunityTitle, cvOpportunityRequirements) }, onCvClosed = { cvOpportunityId = null; cvOpportunityTitle = ""; cvOpportunityRequirements = "" })
                    } else {
                        ProfileScreen(onSettings = { nav.navigate("ajustes") }, onSaved = { nav.navigate("guardados") })
                    }
                    }
                }
                composable("ajustes") { DestinationSurface { if (preferences != null) SettingsScreen(preferences) { nav.popBackStack() } } }
                composable("ayuda") { DestinationSurface { HelpScreen { nav.popBackStack() } } }
                composable("guardados") { DestinationSurface { SavedScreen(model, onBack = { nav.popBackStack() }, onExplore = { openDestination("explorar") }) { nav.navigate("detalle/$it") } } }
                composable("detalle/{id}") { entry ->
                    val opportunity = model.find(entry.arguments?.getString("id").orEmpty())
                    DestinationSurface { DetailScreen(opportunity, model, onBack = { nav.popBackStack() }, onCandidates = { nav.navigate("candidatos/$it") }, onEdit = { nav.navigate("editar/$it") }, onProfile = { openWorkProfile = true; nav.navigate("perfil") { launchSingleTop = true } }, onPrepareCv = { id -> model.find(id)?.let { job -> cvOpportunityId = job.id; cvOpportunityTitle = job.title; cvOpportunityRequirements = job.requirements; nav.navigate("perfil") { launchSingleTop = true } } }, onChat = { job, applicant -> nav.navigate("conversacion/$job/$applicant") }, onApplied = {
                        scope.launch { snackbar.showSnackbar("Postulación enviada") }
                    }) }
                }
                composable("editar/{id}") { entry ->
                    DestinationSurface { EditOpportunityScreen(model.find(entry.arguments?.getString("id").orEmpty()), model, onBack = { nav.popBackStack() }) }
                }
                composable("candidatos/{id}") { entry ->
                    val opportunity = model.find(entry.arguments?.getString("id").orEmpty())
                    DestinationSurface { CandidatesScreen(opportunity, model, onBack = { nav.popBackStack() }, onChat = { job, applicant -> nav.navigate("conversacion/$job/$applicant") }) }
                }
                composable("conversacion/{id}/{applicant}") { entry ->
                    val job = model.find(entry.arguments?.getString("id").orEmpty())
                    DestinationSurface { ConversationScreen(services, job, entry.arguments?.getString("applicant").orEmpty()) { nav.popBackStack() } }
                }
            }
        }
    }
}

@Composable
private fun DestinationSurface(content: @Composable () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.onBackground) { content() }
}
@Composable
private fun ExploreScreen(model: OpportunityViewModel, onSaved: () -> Unit, onSettings: () -> Unit, onAccount: () -> Unit, onPublish: () -> Unit, open: (String) -> Unit) {
    val reduceMotion = LocalAppPreferences.current?.reduceMotion == true
    var query by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("Todas") }
    var locationFilter by rememberSaveable { mutableStateOf("") }
    var modeFilter by rememberSaveable { mutableStateOf("") }
    var availableOnly by rememberSaveable { mutableStateOf(false) }
    var noExperienceOnly by rememberSaveable { mutableStateOf(false) }
    var recentDays by rememberSaveable { mutableIntStateOf(0) }
    var prioritizeInterests by rememberSaveable { mutableStateOf(true) }
    var showFilters by rememberSaveable { mutableStateOf(false) }
    fun clearFilters() {
        query = ""; selectedCategory = "Todas"; locationFilter = ""; modeFilter = ""
        availableOnly = false; noExperienceOnly = false; recentDays = 0; prioritizeInterests = true
    }
    val categories = listOf("Todas") + model.opportunities.map { it.category }.distinct().sorted()
    val interests = LocalAppPreferences.current?.interests.orEmpty()
    val activeFilters = listOf(selectedCategory != "Todas", locationFilter.isNotBlank(), modeFilter.isNotBlank(), availableOnly, noExperienceOnly, recentDays != 0).count { it }
    val opportunities = searchOpportunities(model.opportunities, OpportunityFilters(
        query, selectedCategory, locationFilter, modeFilter, availableOnly, noExperienceOnly, recentDays, prioritizeInterests,
    ), interests, Instant.now().epochSecond)
    LazyColumn(
        contentPadding = PaddingValues(start = AppSpacing.screen, end = AppSpacing.screen, top = 20.dp, bottom = 26.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { BrandHeader(onSaved, onSettings) }
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
            OutlinedButton(onClick = { showFilters = !showFilters }) {
                Icon(Icons.Rounded.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (showFilters) "Ocultar filtros" else if (activeFilters > 0) "Filtros ($activeFilters)" else "Filtrar")
            }
            AnimatedVisibility(visible = showFilters) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = locationFilter, onValueChange = { locationFilter = it.take(100) }, label = { Text("Distrito o ubicación") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Text("Modalidad", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Presencial", "Híbrido", "Remoto").forEach { mode ->
                            item { FilterChip(selected = modeFilter == mode, onClick = { modeFilter = if (modeFilter == mode) "" else mode }, label = { Text(mode) }) }
                        }
                    }
                    FilterChip(selected = availableOnly, onClick = { availableOnly = !availableOnly }, label = { Text("Con vacantes") })
                    FilterChip(selected = noExperienceOnly, onClick = { noExperienceOnly = !noExperienceOnly }, label = { Text("Sin experiencia requerida") })
                    if (noExperienceOnly) Text("Solo se incluyen publicaciones donde se indicó que no hace falta experiencia", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Fecha de publicación", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0 to "Cualquiera", 1 to "Últimas 24 horas", 7 to "Últimos 7 días", 30 to "Últimos 30 días").forEach { (days, label) ->
                            item { FilterChip(selected = recentDays == days, onClick = { recentDays = days }, label = { Text(label) }) }
                        }
                    }
                    Text("Ordenar por", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item { FilterChip(selected = prioritizeInterests, onClick = { prioritizeInterests = true }, label = { Text("Mis intereses") }) }
                        item { FilterChip(selected = !prioritizeInterests, onClick = { prioritizeInterests = false }, label = { Text("Más recientes") }) }
                    }
                    TextButton(onClick = ::clearFilters) { Text("Limpiar búsqueda y filtros") }
                }
            }
        }
        if (model.userId == null) {
            item { AppEmptyState("Encuentra tu próxima oportunidad", "Inicia sesión para consultar publicaciones y participar", "Ir a mi cuenta", EmptyIllustration.Search, showMascot = false, onAction = onAccount) }
        }
        (model.feedError ?: model.error)?.let { message -> item { AppNotice(message, isError = true); TextButton(onClick = model::retry) { Text("Reintentar") } } }
        if (interests.isNotEmpty() && prioritizeInterests && !model.loading) item { Text("Primero verás las categorías que elegiste", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Oportunidades", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (model.userId != null && !model.loading) Text("${opportunities.size} ${if (opportunities.size == 1) "resultado" else "resultados"}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (model.loading && opportunities.isEmpty()) item { OpportunityLoadingState() }
        if (model.userId != null && !model.loading && model.feedError == null && opportunities.isEmpty()) {
            item {
                if (model.opportunities.isEmpty()) AppEmptyState("Aún no hay oportunidades", "Cuando alguien publique una oportunidad aparecerá aquí")
                else AppEmptyState("No encontramos coincidencias", "Prueba con otra palabra o cambia los filtros", "Limpiar filtros", EmptyIllustration.Search) { clearFilters() }
            }
        }
        items(opportunities, key = { it.id }) { opportunity ->
            OpportunityCard(opportunity, modifier = if (reduceMotion) Modifier else Modifier.animateItem(), availabilityConfirmed = !model.feedFromCache) { open(opportunity.id) }
        }
        item { QuickActions(onSaved, onPublish, LocalAppPreferences.current?.purpose == "employer") }
    }
}

@Composable
private fun QuickActions(onSaved: () -> Unit, onPublish: () -> Unit, canPublish: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        QuickAction("Guardados", "Tus favoritos", Icons.Rounded.BookmarkBorder, Modifier.weight(1f), onSaved)
        if (canPublish) QuickAction("Publicar", "Ofrece una oportunidad", Icons.Rounded.AddCircleOutline, Modifier.weight(1f), onPublish)
    }
}

@Composable
private fun QuickAction(title: String, description: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable(role = Role.Button, onClick = onClick),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.large,
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun BrandHeader(onSaved: () -> Unit, onSettings: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(EmpuLight),
            contentAlignment = Alignment.Center,
        ) { Image(painterResource(R.drawable.tuempuje_symbol), contentDescription = "Símbolo de TuEmpuje", modifier = Modifier.size(24.dp)) }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("TuEmpuje", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Oportunidades que conectan", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onSaved) { Icon(Icons.Rounded.BookmarkBorder, contentDescription = "Oportunidades guardadas") }
        IconButton(onClick = onSettings) { Icon(Icons.Rounded.Settings, contentDescription = "Personalizar aplicación") }
    }
}

@Composable
private fun Hero() {
    val accessory = if (LocalAppPreferences.current?.purpose == "employer") EmpuAccessory.BUSINESS else EmpuAccessory.WORK
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        val colors = MaterialTheme.colorScheme
        Row(Modifier.background(Brush.linearGradient(listOf(colors.primaryContainer, colors.secondaryContainer))).padding(start = 20.dp, end = 6.dp, top = 12.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("HOLA, SOY EMPU", style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp, color = colors.onPrimaryContainer)
                Text("Tu próximo paso empieza aquí", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colors.onPrimaryContainer)
                Text("Encuentra oportunidades para ti", style = MaterialTheme.typography.bodyMedium, color = colors.onPrimaryContainer)
            }
            if (LocalConfiguration.current.fontScale < 1.3f && LocalAppPreferences.current?.showMascot != false) {
                Spacer(Modifier.width(10.dp))
                EmpuGreeting(Modifier.size(112.dp), accessory = accessory)
            }
        }
    }
}

@Composable
private fun SavedScreen(model: OpportunityViewModel, onBack: () -> Unit, onExplore: () -> Unit, open: (String) -> Unit) {
    val preferences = LocalAppPreferences.current
    val reduceMotion = preferences?.reduceMotion == true
    val saved = preferences?.savedItems.orEmpty()
    LazyColumn(contentPadding = PaddingValues(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { TextButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null); Text("Volver") } }
        item { Text("Tus guardados", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
        item { Text("Las oportunidades que marcas se guardan en este dispositivo", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        preferences?.savedError?.let { message -> item { AppNotice(message, isError = true) } }
        if (saved.isNotEmpty() && (model.feedFromCache || saved.any { model.find(it.id) == null })) {
            item { AppNotice("Algunas oportunidades son copias guardadas, su disponibilidad necesita conexión para confirmarse") }
        }
        if (preferences?.savedLoading == true) item { AppLoadingState("Cargando guardados") }
        if (saved.isEmpty() && preferences?.savedError == null && preferences?.savedLoading != true) {
            item { AppEmptyState("Aún no tienes guardados", "Marca una oportunidad para encontrarla aquí", "Explorar oportunidades", EmptyIllustration.Search, onAction = onExplore) }
        }
        items(saved, key = { it.id }) { item ->
            val current = model.find(item.id)
            val opportunity = current ?: item.asOpportunity()
            if (opportunity != null) {
                OpportunityCard(opportunity, modifier = if (reduceMotion) Modifier else Modifier.animateItem(), availabilityConfirmed = current != null && !model.feedFromCache, onClick = if (current != null) ({ open(item.id) }) else null)
            } else {
                Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
                    Column(Modifier.fillMaxWidth().padding(18.dp)) {
                        Text("Oportunidad guardada", style = MaterialTheme.typography.titleMedium)
                        Text("Conéctate para recuperar sus detalles", style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = { preferences?.removeSaved(item.id) }) { Text("Quitar de guardados") }
                    }
                }
            }
        }
    }
}

@Composable
private fun OpportunityCard(opportunity: Opportunity, status: String? = null, modifier: Modifier = Modifier, availabilityConfirmed: Boolean = true, onClick: (() -> Unit)?) {
    val preferences = LocalAppPreferences.current
    val saved = opportunity.id in preferences?.savedIds.orEmpty()
    Surface(
        modifier = modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(AppSpacing.large), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(opportunity.category.uppercase(Locale.forLanguageTag("es-PE")), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(opportunity.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(opportunity.publicBusinessName.ifBlank { opportunity.organization }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (opportunity.publicBusinessName.isNotBlank()) Text("Publicado por ${opportunity.organization}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (preferences != null) {
                    IconButton(onClick = { preferences.toggleSaved(opportunity) }) {
                        Icon(
                            if (saved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                            contentDescription = if (saved) "Quitar de guardados" else "Guardar oportunidad",
                            tint = if (saved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Rounded.Place, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp).size(17.dp))
                Text(listOfNotNull(opportunity.location, opportunity.workMode).joinToString(" · "), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (opportunity.noExperienceRequired == true) Text("Sin experiencia requerida", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            if (status != null) {
                val selected = status == "Seleccionada"
                Surface(shape = MaterialTheme.shapes.small, color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer) {
                    Text(status, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), style = MaterialTheme.typography.labelMedium, color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .55f), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(opportunity.pay, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(opportunity.duration, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val remaining = (opportunity.vacancies - opportunity.selectedCount).coerceAtLeast(0)
                    Text(when { opportunity.status == "closed" -> "Convocatoria cerrada"; opportunity.status != "published" -> "Publicación retirada"; !availabilityConfirmed -> "Vacantes por confirmar"; remaining == 1 -> "1 vacante disponible"; else -> "$remaining vacantes disponibles" }, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
            if (opportunity.createdAtSeconds > 0) {
                val date = remember(opportunity.createdAtSeconds) { Instant.ofEpochSecond(opportunity.createdAtSeconds).atZone(ZoneId.systemDefault()).toLocalDate().format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es-PE"))) }
                Text("Publicado el $date", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun DetailScreen(opportunity: Opportunity?, model: OpportunityViewModel, onBack: () -> Unit, onCandidates: (String) -> Unit, onEdit: (String) -> Unit, onProfile: () -> Unit, onPrepareCv: (String) -> Unit, onChat: (String, String) -> Unit, onApplied: () -> Unit) {
    var confirmApplication by rememberSaveable(opportunity?.id) { mutableStateOf(false) }
    var confirmWithdraw by rememberSaveable(opportunity?.id) { mutableStateOf(false) }
    var confirmRemove by rememberSaveable(opportunity?.id) { mutableStateOf(false) }
    var confirmClose by rememberSaveable(opportunity?.id) { mutableStateOf(false) }
    if (confirmClose && opportunity != null) AlertDialog(
        onDismissRequest = { confirmClose = false }, title = { Text("¿Cerrar la convocatoria?") },
        text = { Text("Dejará de recibir nuevas postulaciones, podrás seguir coordinando con quienes ya postularon") },
        confirmButton = { TextButton(enabled = !model.busy, onClick = { model.changeOpportunityStatus(opportunity.id, true) { confirmClose = false } }) { Text("Cerrar convocatoria") } },
        dismissButton = { TextButton(onClick = { confirmClose = false }) { Text("Cancelar") } },
    )
    if (confirmWithdraw && opportunity != null) AlertDialog(
        onDismissRequest = { confirmWithdraw = false }, icon = { Empu(EmpuMood.THINKING, Modifier.size(112.dp)) },
        title = { Text("¿Retirar tu postulación?") },
        text = { Text(if (model.applications[opportunity.id] == "selected") "Si ya fuiste seleccionado, la vacante volverá a estar disponible" else "Podrás volver a postular mientras la oportunidad esté disponible") },
        confirmButton = { TextButton(enabled = !model.busy, onClick = { model.withdraw(opportunity.id) { confirmWithdraw = false } }) { Text("Retirar") } },
        dismissButton = { TextButton(onClick = { confirmWithdraw = false }) { Text("Cancelar") } },
    )
    if (confirmRemove && opportunity != null) AlertDialog(
        onDismissRequest = { confirmRemove = false }, icon = { Empu(EmpuMood.SAD, Modifier.size(112.dp)) },
        title = { Text("¿Eliminar publicación?") },
        text = { Text("Dejará de aparecer en la búsqueda, quienes ya postularon conservarán su historial") },
        confirmButton = { TextButton(enabled = !model.busy, onClick = { model.removeOpportunity(opportunity.id) { confirmRemove = false; onBack() } }) { Text("Eliminar") } },
        dismissButton = { TextButton(onClick = { confirmRemove = false }) { Text("Cancelar") } },
    )
    var mapUnavailable by rememberSaveable { mutableStateOf(false) }
    var showReport by rememberSaveable(opportunity?.id) { mutableStateOf(false) }
    var reportReason by rememberSaveable(opportunity?.id) { mutableStateOf("Información engañosa") }
    var reportDetails by rememberSaveable(opportunity?.id) { mutableStateOf("") }
    var reportSent by rememberSaveable(opportunity?.id) { mutableStateOf(false) }
    var confirmHideBusiness by rememberSaveable(opportunity?.id) { mutableStateOf(false) }
    if (confirmHideBusiness && opportunity != null) AlertDialog(
        onDismissRequest = { confirmHideBusiness = false }, title = { Text("Dejar de mostrar datos del negocio") },
        text = { Text("El nombre, el rubro y la descripción del negocio se retirarán de esta publicación") },
        confirmButton = { TextButton(enabled = !model.busy, onClick = { model.removePublicBusiness(opportunity.id) { confirmHideBusiness = false } }) { Text("Dejar de mostrar") } },
        dismissButton = { TextButton(onClick = { confirmHideBusiness = false }) { Text("Cancelar") } },
    )
    var showReference by rememberSaveable(opportunity?.id) { mutableStateOf(false) }
    if (showReference && opportunity != null && model.userId != null) {
        ReferenceDialog(model, opportunity, model.userId!!, canWrite = false, onDismiss = { showReference = false })
    }
    var confirmExperience by rememberSaveable(opportunity?.id) { mutableStateOf(false) }
    if (confirmExperience && opportunity != null) AlertDialog(
        onDismissRequest = { confirmExperience = false },
        title = { Text("Confirmar experiencia") },
        text = { Text("Confirma que realizaste este trabajo, esta acción no confirma el pago ni constituye un certificado") },
        confirmButton = { TextButton(enabled = !model.busy, onClick = { model.confirmCompletedWork(opportunity.id); confirmExperience = false }) { Text("Confirmar") } },
        dismissButton = { TextButton(onClick = { confirmExperience = false }) { Text("Cancelar") } },
    )
    if (showReport && opportunity != null) {
        AlertDialog(
            onDismissRequest = { showReport = false },
            title = { Text("Reportar oportunidad") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    model.error?.let { AppNotice(it, isError = true) }
                    Text("Selecciona un motivo y explica lo ocurrido")
                    listOf("Información engañosa", "Pago o condiciones", "Seguridad", "Otro").forEach { reason ->
                        androidx.compose.material3.FilterChip(selected = reportReason == reason, onClick = { reportReason = reason }, label = { Text(reason) })
                    }
                    OutlinedTextField(value = reportDetails, onValueChange = { reportDetails = it.take(500) }, label = { Text("Detalles") }, supportingText = { Text("Entre 10 y 500 caracteres") })
                }
            },
            confirmButton = { TextButton(enabled = !model.busy, onClick = { model.reportOpportunity(opportunity.id, reportReason, reportDetails) { reportSent = true; showReport = false } }) { Text("Enviar reporte") } },
            dismissButton = { TextButton(onClick = { showReport = false }) { Text("Cancelar") } },
        )
    }
    val context = LocalContext.current
    if (confirmApplication && opportunity != null && !opportunity.own) {
        AlertDialog(
            onDismissRequest = { confirmApplication = false },
            icon = { Empu(EmpuMood.THINKING, Modifier.size(112.dp)) },
            title = { Text("Confirmar postulación") },
            text = {
                val profile = model.applicationProfile
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Estos datos de tu perfil se compartirán con quien publicó ${opportunity.title}")
                    DetailRow("Nombre", profile.name.ifBlank { "Sin registrar" })
                    DetailRow("Habilidades", profile.skills.ifBlank { "Sin registrar" })
                    DetailRow("Experiencia o proyectos", profile.experience.ifBlank { "Sin registrar" })
                    DetailRow("Disponibilidad", profile.availability.ifBlank { "Sin registrar" })
                    DetailRow("Distrito", profile.district.ifBlank { "Sin registrar" })
                    DetailRow("Estudios", profile.education.ifBlank { "Sin registrar" })
                    DetailRow("Portafolio", profile.portfolio.ifBlank { "Sin registrar" })
                    Text("Tu documento, celular y correo no se comparten en esta postulación", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = { TextButton(onClick = { model.apply(opportunity.id, onApplied); confirmApplication = false }, enabled = !model.busy) { Text("Enviar postulación") } },
            dismissButton = { TextButton(onClick = { confirmApplication = false }) { Text("Cancelar") } },
        )
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Volver")
        }
        model.error?.let { AppNotice(it, isError = true) }
        if (opportunity == null) {
            AppEmptyState("Oportunidad no disponible", "Vuelve al listado para elegir otra")
        } else {
            Text(opportunity.category.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(opportunity.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(opportunity.publicBusinessName.ifBlank { opportunity.organization }, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (opportunity.publicBusinessName.isNotBlank()) Text("Publicado por ${opportunity.organization}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailRow("Ubicación", opportunity.location)
                    opportunity.workMode?.let { DetailRow("Modalidad", it) }
                    if (opportunity.workMode != "Remoto" && opportunity.location.trim().lowercase(Locale.ROOT) !in setOf("remoto", "virtual", "en línea", "online")) {
                        TextButton(onClick = {
                            try {
                                val query = Uri.encode(opportunity.location)
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$query")))
                                mapUnavailable = false
                            } catch (_: ActivityNotFoundException) {
                                mapUnavailable = true
                            }
                        }) { Text("Buscar ubicación en el mapa") }
                        if (mapUnavailable) Text("No se encontró una aplicación de mapas", style = MaterialTheme.typography.bodySmall)
                        Text("Comprueba el lugar con quien publicó la oportunidad", style = MaterialTheme.typography.bodySmall)
                    }
                    DetailRow("Pago ofrecido", opportunity.pay)
                    DetailRow("Fecha y duración", opportunity.duration)
                    DetailRow("Vacantes", opportunity.vacancies.toString())
                }
            }
            Text("Sobre esta oportunidad", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(opportunity.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Conocimientos y requisitos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            opportunity.noExperienceRequired?.let { Text(if (it) "No requiere experiencia previa" else "Requiere experiencia previa", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary) }
            Text(opportunity.requirements, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!opportunity.own) AppNotice("Cuida tu información: no pagues por postular ni compartas contraseñas o códigos de verificación. Si algo te parece sospechoso, reporta la oportunidad")
            if (opportunity.publicBusinessName.isNotBlank()) {
                Text("Sobre el negocio", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (opportunity.publicBusinessSector.isNotBlank()) DetailRow("Rubro", opportunity.publicBusinessSector)
                if (opportunity.publicBusinessDescription.isNotBlank()) Text(opportunity.publicBusinessDescription, style = MaterialTheme.typography.bodyLarge)
                if (opportunity.publicBusinessSector.isBlank() && opportunity.publicBusinessDescription.isBlank()) Text(opportunity.publicBusinessName, style = MaterialTheme.typography.bodyLarge)
            }
            if (opportunity.status == "closed") AppNotice("La convocatoria está cerrada para nuevas postulaciones")
            else if (opportunity.status != "published") AppNotice("Esta publicación fue retirada, su información se conserva en el historial")
            if (!opportunity.own) {
                val status = model.applications[opportunity.id]
                val canReapply = status in setOf("withdrawn", "cancelled")
                val applied = status != null && !canReapply
                val full = opportunity.selectedCount >= opportunity.vacancies
                if (status == "cancelled") AppNotice("La selección fue cancelada por quien publicó la oportunidad")
                if (opportunity.status == "published" && !applied) {
                    Text("Por ahora la postulación comparte tu perfil laboral; el envío del CV todavía no está disponible", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (!model.profileReady) {
                        AppNotice("Completa tus habilidades y disponibilidad antes de postular")
                        OutlinedButton(onClick = onProfile, modifier = Modifier.fillMaxWidth()) { Text("Completar mi perfil") }
                    } else AppPrimaryButton(if (full) "Vacantes cubiertas" else if (canReapply) "Volver a postular" else "Postular", onClick = { confirmApplication = true }, enabled = !full && !model.busy)
                    OutlinedButton(onClick = { onPrepareCv(opportunity.id) }, modifier = Modifier.fillMaxWidth()) { Text("Preparar mi CV para esta oportunidad") }
                }
                if (applied) {
                    AppNotice(when (status) { "reviewing" -> "Tu postulación está en revisión"; "selected" -> "Fuiste seleccionado"; "rejected" -> "No fuiste seleccionado para esta oportunidad"; "completed" -> "Trabajo finalizado"; else -> "Postulación enviada" })
                    if (status == "completed") {
                        if (model.confirmedWork[opportunity.id] == true) {
                            AppNotice("Experiencia confirmada por ambas partes")
                            OutlinedButton(onClick = { showReference = true }, modifier = Modifier.fillMaxWidth()) { Text("Mi referencia") }
                        }
                        else {
                            Text("La persona que publicó indicó que el trabajo terminó, confirma si también lo finalizaste", style = MaterialTheme.typography.bodyMedium)
                            OutlinedButton(onClick = { confirmExperience = true }, enabled = !model.busy, modifier = Modifier.fillMaxWidth()) { Text("Confirmar mi experiencia") }
                        }
                    }
                    if (status in setOf("pending", "reviewing", "selected")) OutlinedButton(onClick = { confirmWithdraw = true }, enabled = !model.busy, modifier = Modifier.fillMaxWidth()) { Text("Retirar postulación") }
                }
                if (status != null) model.userId?.let { applicant ->
                    OutlinedButton(onClick = { onChat(opportunity.id, applicant) }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (status in setOf("withdrawn", "cancelled", "rejected")) "Ver conversación anterior" else "Conversar")
                    }
                }
                if (model.busy) AppLoadingState("Procesando solicitud")
                if (opportunity.status == "published") {
                    if (reportSent) AppNotice("Reporte enviado para revisión")
                    else TextButton(onClick = { showReport = true }) { Text("Reportar oportunidad") }
                }
            } else {
                Text("Es tu oportunidad · ${opportunity.selectedCount} de ${opportunity.vacancies} vacantes seleccionadas", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FilledTonalButton(onClick = { onCandidates(opportunity.id) }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Ver postulaciones") }
                if (opportunity.publicBusinessName.isNotBlank()) TextButton(enabled = !model.busy, onClick = { confirmHideBusiness = true }) { Text("Dejar de mostrar datos del negocio") }
                if (opportunity.status == "published") OutlinedButton(onClick = { confirmClose = true }, enabled = !model.busy, modifier = Modifier.fillMaxWidth()) { Text("Cerrar convocatoria") }
                if (opportunity.status == "closed") {
                    OutlinedButton(onClick = { model.changeOpportunityStatus(opportunity.id, false) {} }, enabled = !model.busy && opportunity.selectedCount < opportunity.vacancies, modifier = Modifier.fillMaxWidth()) { Text("Reabrir convocatoria") }
                    if (opportunity.selectedCount >= opportunity.vacancies) Text("No hay vacantes disponibles para reabrir", style = MaterialTheme.typography.bodySmall)
                }
                if (opportunity.status == "published") {
                    OutlinedButton(onClick = { onEdit(opportunity.id) }, enabled = opportunity.selectedCount == 0 && !model.busy, modifier = Modifier.fillMaxWidth()) { Text("Editar publicación") }
                    TextButton(onClick = { confirmRemove = true }, enabled = opportunity.selectedCount == 0 && !model.busy) { Text("Eliminar publicación") }
                    if (opportunity.selectedCount > 0) Text("No puedes editar ni retirar la publicación mientras haya personas seleccionadas", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun CandidatesScreen(opportunity: Opportunity?, model: OpportunityViewModel, onBack: () -> Unit, onChat: (String, String) -> Unit) {
    var selected by remember { mutableStateOf<pe.tuempuje.app.data.Candidate?>(null) }
    var completing by remember { mutableStateOf<pe.tuempuje.app.data.Candidate?>(null) }
    var rejecting by remember { mutableStateOf<pe.tuempuje.app.data.Candidate?>(null) }
    var cancelling by remember { mutableStateOf<pe.tuempuje.app.data.Candidate?>(null) }
    var referenceCandidate by remember { mutableStateOf<pe.tuempuje.app.data.Candidate?>(null) }
    var publicCandidate by remember { mutableStateOf<pe.tuempuje.app.data.Candidate?>(null) }
    referenceCandidate?.let { candidate -> opportunity?.let { job -> ReferenceDialog(model, job, candidate.id, canWrite = true, onDismiss = { referenceCandidate = null }) } }
    publicCandidate?.let { candidate -> PublicReferencesDialog(model, candidate.id, candidate.name, onDismiss = { publicCandidate = null }) }
    rejecting?.let { candidate -> AlertDialog(onDismissRequest = { rejecting = null }, title = { Text("¿No seleccionar a ${candidate.name}?") },
        text = { Text("La persona podrá ver que no fue seleccionada para esta oportunidad") },
        confirmButton = { TextButton(enabled = !model.busy, onClick = { opportunity?.let { model.updateCandidateStatus(it.id, candidate.id, "rejected") }; rejecting = null }) { Text("Confirmar") } },
        dismissButton = { TextButton(onClick = { rejecting = null }) { Text("Volver") } }) }
    cancelling?.let { candidate -> AlertDialog(onDismissRequest = { cancelling = null }, title = { Text("¿Cancelar selección?") },
        text = { Text("${candidate.name} dejará de estar seleccionado y la vacante se liberará") },
        confirmButton = { TextButton(enabled = !model.busy, onClick = { opportunity?.let { model.cancelSelection(it.id, candidate.id) }; cancelling = null }) { Text("Cancelar selección") } },
        dismissButton = { TextButton(onClick = { cancelling = null }) { Text("Volver") } }) }
    completing?.let { candidate ->
        AlertDialog(
            onDismissRequest = { completing = null },
            icon = { Empu(EmpuMood.THINKING, Modifier.size(112.dp)) },
            title = { Text("Finalizar trabajo") },
            text = { Text("Marcarás como finalizado el trabajo de ${candidate.name}, esto no confirma un pago") },
            confirmButton = { TextButton(enabled = !model.busy, onClick = { opportunity?.let { model.completeCandidate(it.id, candidate.id) }; completing = null }) { Text("Finalizar") } },
            dismissButton = { TextButton(onClick = { completing = null }) { Text("Cancelar") } },
        )
    }
    DisposableEffect(opportunity?.id) {
        if (opportunity?.own == true) model.observeCandidates(opportunity.id)
        onDispose { model.stopObservingCandidates() }
    }
    selected?.let { candidate ->
        AlertDialog(
            onDismissRequest = { selected = null },
            icon = { Empu(EmpuMood.THINKING, Modifier.size(112.dp)) },
            title = { Text("Seleccionar postulante") },
            text = { Text("Seleccionarás a ${candidate.name} para esta oportunidad, esto no confirma un pago") },
            confirmButton = { TextButton(enabled = !model.busy && opportunity != null, onClick = { opportunity?.let { model.selectCandidate(it.id, candidate.id) }; selected = null }) { Text("Seleccionar") } },
            dismissButton = { TextButton(onClick = { selected = null }) { Text("Cancelar") } },
        )
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Volver")
        }
        if (opportunity == null || !opportunity.own) {
            AppEmptyState("Publicación no disponible", "Regresa a tu actividad para revisar tus publicaciones")
        } else {
            Text("Candidatos", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(opportunity.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${opportunity.selectedCount} de ${opportunity.vacancies} vacantes seleccionadas", style = MaterialTheme.typography.bodyMedium)
            if (model.candidatesLoading) AppLoadingState("Cargando postulaciones")
            model.candidateError?.let { AppNotice(it, isError = true); TextButton(onClick = { model.observeCandidates(opportunity.id) }) { Text("Reintentar") } }
            model.error?.let { AppNotice(it, isError = true) }
            if (!model.candidatesLoading && model.candidateError == null && model.candidates.isEmpty()) {
                AppEmptyState("Aún no hay postulaciones", "Cuando alguien postule aparecerá en esta sección")
            }
            model.candidates.forEach { candidate ->
                CandidateCard(candidate, canManage = opportunity.status in setOf("published", "closed") && !model.busy, canSelect = candidate.status in setOf("pending", "reviewing") && opportunity.status in setOf("published", "closed") && opportunity.selectedCount < opportunity.vacancies && !model.busy,
                    onSelect = { selected = candidate }, onReview = { model.updateCandidateStatus(opportunity.id, candidate.id, "reviewing") }, onReject = { rejecting = candidate }, onCancelSelection = { cancelling = candidate },
                    onComplete = { completing = candidate }, onChat = { onChat(opportunity.id, candidate.id) }, onReference = { referenceCandidate = candidate }, onPublicReferences = { publicCandidate = candidate })
            }
        }
    }
}

@Composable
private fun CandidateCard(candidate: pe.tuempuje.app.data.Candidate, canManage: Boolean, canSelect: Boolean, onSelect: () -> Unit, onReview: () -> Unit, onReject: () -> Unit, onCancelSelection: () -> Unit, onComplete: () -> Unit, onChat: () -> Unit, onReference: () -> Unit, onPublicReferences: () -> Unit) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.PersonOutline, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
                Column {
                    Text(candidate.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(when (candidate.status) { "reviewing" -> "En revisión"; "selected" -> "Seleccionado"; "rejected" -> "No seleccionado"; "cancelled" -> "Selección cancelada"; "completed" -> if (candidate.confirmed) "Experiencia confirmada por ambas partes" else "Finalizado · falta confirmación"; "withdrawn" -> "Postulación retirada"; else -> "Postulación recibida" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (candidate.skills.isNotBlank()) DetailRow("Habilidades", candidate.skills)
            if (candidate.experience.isNotBlank()) DetailRow("Experiencia o proyectos", candidate.experience)
            if (candidate.availability.isNotBlank()) DetailRow("Disponibilidad", candidate.availability)
            if (candidate.district.isNotBlank()) DetailRow("Distrito", candidate.district)
            if (candidate.education.isNotBlank()) DetailRow("Estudios o formación", candidate.education)
            if (candidate.portfolio.isNotBlank()) DetailRow("Portafolio o proyectos", candidate.portfolio)
            TextButton(onClick = onPublicReferences) { Text("Ver referencias compartidas") }
            OutlinedButton(onClick = onChat, modifier = Modifier.fillMaxWidth()) { Text(if (candidate.status in setOf("withdrawn", "rejected", "cancelled")) "Ver conversación anterior" else "Conversar") }
            if (canManage && candidate.status == "pending") TextButton(onClick = onReview) { Text("Marcar en revisión") }
            if (canManage && candidate.status in setOf("pending", "reviewing")) {
                OutlinedButton(onClick = onSelect, enabled = canSelect, modifier = Modifier.fillMaxWidth()) { Text(if (canSelect) "Seleccionar" else "Vacantes cubiertas") }
                TextButton(onClick = onReject) { Text("No seleccionar") }
            }
            if (canManage && candidate.status == "selected") {
                OutlinedButton(onClick = onComplete, modifier = Modifier.fillMaxWidth()) { Text("Finalizar trabajo") }
                TextButton(onClick = onCancelSelection) { Text("Cancelar selección") }
            }
            if (candidate.status == "completed" && candidate.confirmed) OutlinedButton(onClick = onReference, modifier = Modifier.fillMaxWidth()) { Text("Referencia de este trabajo") }
        }
    }
}

@Composable
private fun ReferenceDialog(model: OpportunityViewModel, opportunity: Opportunity, workerId: String, canWrite: Boolean, onDismiss: () -> Unit) {
    var text by rememberSaveable(opportunity.id, workerId) { mutableStateOf("") }
    var dispute by rememberSaveable(opportunity.id, workerId) { mutableStateOf(false) }
    var sent by rememberSaveable(opportunity.id, workerId) { mutableStateOf(false) }
    DisposableEffect(opportunity.id, workerId) {
        model.observeReference(opportunity.id, workerId)
        onDispose { model.stopObservingReferences() }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Referencia del trabajo") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (model.referencesLoading) AppLoadingState("Cargando referencia")
                model.referencesError?.let { AppNotice(it, isError = true) }
                model.error?.let { AppNotice(it, isError = true) }
                val ref = model.currentReference
                if (!model.referencesLoading && model.referencesError == null) {
                    if (ref == null && canWrite) {
                        Text("Describe la colaboración. La persona decidirá si comparte esta referencia")
                        OutlinedTextField(value = text, onValueChange = { text = it.take(500) }, label = { Text("Referencia") }, supportingText = { Text("Entre 10 y 500 caracteres") }, minLines = 3)
                    } else if (ref == null) Text("Aún no se ha escrito una referencia para este trabajo")
                    else {
                        Text(ref.text, style = MaterialTheme.typography.bodyLarge)
                        Text("${ref.authorName} · ${ref.title}", style = MaterialTheme.typography.bodySmall)
                        Text(if (ref.moderationHidden) "Oculta después de la revisión" else if (ref.visible) "Visible para otras personas de TuEmpuje" else "Solo visible para las partes", style = MaterialTheme.typography.bodySmall)
                        if (!canWrite) {
                            if (!ref.moderationHidden) OutlinedButton(enabled = !model.busy, onClick = { model.setReferenceVisibility(ref, !ref.visible) }) { Text(if (ref.visible) "Dejar de compartir" else "Compartir referencia") }
                            if (sent) AppNotice("Reporte enviado para revisión")
                            else if (!dispute) TextButton(onClick = { dispute = true; text = "" }) { Text("Reportar esta referencia") }
                            else {
                                OutlinedTextField(value = text, onValueChange = { text = it.take(500) }, label = { Text("¿Qué información es incorrecta?") }, minLines = 2)
                                TextButton(enabled = !model.busy, onClick = { model.disputeReference(ref, text) { sent = true; dispute = false } }) { Text("Enviar reporte") }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (canWrite && model.currentReference == null && !model.referencesLoading && model.referencesError == null) {
                TextButton(enabled = !model.busy && text.trim().length >= 10, onClick = { model.writeReference(opportunity, workerId, text) { text = "" } }) { Text("Enviar referencia") }
            } else TextButton(onClick = onDismiss) { Text("Cerrar") }
        },
        dismissButton = if (canWrite && model.currentReference == null) ({ TextButton(onClick = onDismiss) { Text("Cancelar") } }) else null,
    )
}

@Composable
private fun PublicReferencesDialog(model: OpportunityViewModel, workerId: String, name: String, onDismiss: () -> Unit) {
    DisposableEffect(workerId) {
        model.observePublicReferences(workerId)
        onDispose { model.stopObservingReferences() }
    }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Referencias de $name") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (model.referencesLoading) AppLoadingState("Cargando referencias")
                model.referencesError?.let { AppNotice(it, isError = true) }
                if (!model.referencesLoading && model.referencesError == null && model.publicReferences.isEmpty()) Text("Aún no comparte referencias")
                model.publicReferences.forEach { reference ->
                    Text(reference.text, style = MaterialTheme.typography.bodyMedium)
                    Text("${reference.authorName} · ${reference.title}", style = MaterialTheme.typography.labelMedium)
                    HorizontalDivider()
                }
                Text("Las referencias reflejan lo declarado por quienes colaboraron, no verifican identidad ni pagos", style = MaterialTheme.typography.bodySmall)
            }
        }, confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } })
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun PublishScreen(services: FirebaseServices?, model: OpportunityViewModel, onBack: () -> Unit, onAccount: () -> Unit, onPublished: () -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var payAmount by rememberSaveable { mutableStateOf("") }
    var payUnit by rememberSaveable { mutableStateOf("Por tarea") }
    var showPayUnits by remember { mutableStateOf(false) }
    var duration by rememberSaveable { mutableStateOf("") }
    var selectedDate by rememberSaveable { mutableStateOf<Long?>(null) }
    var flexibleDate by rememberSaveable { mutableStateOf(false) }
    var requirements by rememberSaveable { mutableStateOf("") }
    var vacancies by rememberSaveable { mutableStateOf("1") }
    var category by rememberSaveable { mutableStateOf("Ayudantía") }
    var noExperience by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var workMode by rememberSaveable { mutableStateOf<String?>(null) }
    var includeBusiness by rememberSaveable { mutableStateOf(false) }
    var pendingTemplate by remember { mutableStateOf<WorkTemplate?>(null) }
    var showError by rememberSaveable { mutableStateOf(false) }
    var step by rememberSaveable { mutableIntStateOf(0) }
    var confirmDiscard by rememberSaveable { mutableStateOf(false) }
    var confirmPublish by rememberSaveable { mutableStateOf(false) }
    var showCalendar by rememberSaveable { mutableStateOf(false) }
    var showAiInfo by rememberSaveable { mutableStateOf(false) }
    var pendingAiDraft by remember { mutableStateOf<AssistantOpportunity?>(null) }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val today = LocalDate.now()
    val todayMillis = dateAsUtcMillis(today)
    val schedule = workSchedule(selectedDate, duration, flexibleDate)
    val payment = paymentText(payAmount, payUnit)
    val schedulePrefix = workSchedule(selectedDate, "", flexibleDate)
    val invalidVacancies = vacancies.toIntOrNull() !in 1..99
    val workErrors = listOf(
        title.trim().length !in 3..120,
        description.trim().length !in 10..3000,
        requirements.trim().length !in 2..1500,
        noExperience == null,
        includeBusiness && model.profileOrganization.trim().length < 2,
    ).count { it }
    val conditionErrors = listOf(
        location.trim().length !in 2..120,
        payment == null,
        schedule.length !in 2..120 || (selectedDate != null && selectedDate!! < todayMillis),
        invalidVacancies,
        workMode == null,
    ).count { it }
    val invalidCount = if (step == 0) workErrors else conditionErrors
    val hasDraft = listOf(title, description, location, payAmount, duration, requirements).any { it.isNotBlank() } || selectedDate != null || flexibleDate || vacancies != "1" || category != "Ayudantía" || payUnit != "Por tarea" || noExperience != null || workMode != null || includeBusiness

    fun applyTemplate(template: WorkTemplate) {
        title = template.title
        description = template.description
        requirements = template.requirements
        category = template.category
    }

    pendingTemplate?.let { template ->
        AlertDialog(onDismissRequest = { pendingTemplate = null }, title = { Text("¿Usar esta plantilla?") },
            text = { Text("Se reemplazarán el título, las tareas, los requisitos y la categoría que escribiste") },
            confirmButton = { TextButton(onClick = { applyTemplate(template); pendingTemplate = null }) { Text("Usar plantilla") } },
            dismissButton = { TextButton(onClick = { pendingTemplate = null }) { Text("Conservar") } })
    }

    BackHandler(enabled = hasDraft || step > 0) {
        if (step > 0) { step--; showError = false } else confirmDiscard = true
    }
    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            icon = { Empu(EmpuMood.SAD, Modifier.size(112.dp)) },
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
    fun applyAssistant(result: AssistantOpportunity) {
        title = result.draft.title; description = result.draft.tasks; requirements = result.draft.requirements
        category = result.category; noExperience = result.noExperience; workMode = result.mode
        location = result.location; payAmount = result.amount; payUnit = result.payUnit
        includeBusiness = result.includeBusiness; vacancies = "1"; duration = ""
        flexibleDate = result.dateChoice == "Por coordinar"
        selectedDate = when (result.dateChoice) { "Hoy" -> todayMillis; "Mañana" -> dateAsUtcMillis(today.plusDays(1)); else -> null }
        showError = false; step = 2
        scope.launch { scrollState.animateScrollTo(0) }
    }
    if (showAiInfo) OpportunityAssistantDialog(services, model.profileOrganization, model.applicationProfile.district, onDismiss = { showAiInfo = false }, onUse = { result ->
        showAiInfo = false
        if (hasDraft) pendingAiDraft = result else applyAssistant(result)
    })
    pendingAiDraft?.let { draft ->
        AlertDialog(onDismissRequest = { pendingAiDraft = null }, title = { Text("¿Usar este borrador?") },
            text = { Text("La publicación creada con Empu reemplazará los datos que ya escribiste") },
            confirmButton = { TextButton(onClick = {
                applyAssistant(draft)
                pendingAiDraft = null
            }) { Text("Usar publicación") } },
            dismissButton = { TextButton(onClick = { pendingAiDraft = null }) { Text("Conservar texto") } })
    }
    if (confirmPublish) {
        AlertDialog(
            onDismissRequest = { if (!model.busy) confirmPublish = false },
            icon = { Empu(EmpuMood.THINKING, Modifier.size(112.dp)) },
            title = { Text("¿Publicar esta oportunidad?") },
            text = { Text("Se mostrará a las personas de TuEmpuje con las condiciones que acabas de revisar") },
            confirmButton = {
                TextButton(enabled = !model.busy, onClick = {
                    model.publish(title, description, location, payment.orEmpty(), schedule, requirements, vacancies.toIntOrNull() ?: 0, category, noExperience == true, workMode.orEmpty(), includeBusiness) {
                        title = ""; description = ""; location = ""; payAmount = ""; payUnit = "Por tarea"; duration = ""; selectedDate = null; flexibleDate = false; requirements = ""; vacancies = "1"; category = "Ayudantía"
                        noExperience = null; workMode = null; includeBusiness = false; showError = false; confirmPublish = false; step = 0
                        onPublished()
                    }
                    confirmPublish = false
                }) { Text("Publicar") }
            },
            dismissButton = { TextButton(enabled = !model.busy, onClick = { confirmPublish = false }) { Text("Seguir revisando") } },
        )
    }
    if (showCalendar) {
        val context = LocalContext.current
        val currentConfiguration = LocalConfiguration.current
        val locale = remember { Locale.forLanguageTag("es-PE") }
        val configuration = remember(currentConfiguration, locale) { Configuration(currentConfiguration).apply { setLocale(locale) } }
        val localizedContext = remember(context, configuration) { context.createConfigurationContext(configuration) }
        val selectable = remember(todayMillis) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= todayMillis
                override fun isSelectableYear(year: Int) = year >= today.year
            }
        }
        val datePicker = remember(selectedDate, todayMillis) {
            DatePickerState(locale = locale, initialSelectedDateMillis = selectedDate?.takeIf { it >= todayMillis }, selectableDates = selectable)
        }
        CompositionLocalProvider(LocalContext provides localizedContext, LocalConfiguration provides configuration) {
            DatePickerDialog(
                onDismissRequest = { showCalendar = false },
                confirmButton = {
                    TextButton(enabled = datePicker.selectedDateMillis != null, onClick = {
                        selectedDate = datePicker.selectedDateMillis
                        flexibleDate = false
                        showCalendar = false
                    }) { Text("Usar fecha") }
                },
                dismissButton = { TextButton(onClick = { showCalendar = false }) { Text("Cancelar") } },
            ) {
                DatePicker(
                    state = datePicker,
                    title = { Text("Selecciona una fecha", modifier = Modifier.padding(start = 24.dp, top = 16.dp)) },
                    headline = { Text(datePicker.selectedDateMillis?.let(::displayWorkDate) ?: "Elige un día", modifier = Modifier.padding(start = 24.dp, bottom = 12.dp)) },
                )
            }
        }
    }
    Column(Modifier.fillMaxSize().verticalScroll(scrollState).padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(start = 18.dp, end = 6.dp, top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Publicar", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text("Cuéntanos qué apoyo necesitas y define las condiciones", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
                if (LocalConfiguration.current.fontScale < 1.3f && LocalAppPreferences.current?.showMascot != false) EmpuArrival(if (step == 2) EmpuMood.SUCCESS else EmpuMood.THINKING, Modifier.size(100.dp), accessory = EmpuAccessory.BUSINESS)
            }
        }
        if (model.userId == null) {
            AppNotice("Inicia sesión para publicar una oportunidad")
            OutlinedButton(onClick = onAccount, modifier = Modifier.fillMaxWidth()) { Text("Ir a mi cuenta") }
        }
        model.error?.let { AppNotice(it, isError = true) }
        Text("Paso ${step + 1} de 3 · ${listOf("Trabajo", "Condiciones", "Revisión")[step]}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        LinearProgressIndicator(progress = { (step + 1) / 3f }, modifier = Modifier.fillMaxWidth())
        AnimatedVisibility(visible = showError && invalidCount > 0) {
            AppNotice("Revisa los campos marcados, faltan o necesitan corrección: $invalidCount", isError = true)
        }
        if (step == 0) {
            FormSectionHeading("01", "Trabajo", "Describe el apoyo que necesitas")
            OutlinedButton(onClick = { showAiInfo = true }, modifier = Modifier.fillMaxWidth()) { Text("Redactar con Empu") }
            Text("Empezar con una plantilla", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Úsala como punto de partida y adapta las tareas a lo que necesitas", style = MaterialTheme.typography.bodySmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(workTemplates) { template ->
                    AssistChip(onClick = {
                        if (listOf(title, description, requirements).any { it.isNotBlank() } || category != "Ayudantía") pendingTemplate = template else applyTemplate(template)
                    }, label = { Text(template.label) })
                }
            }
            Text("Tipo de trabajo", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(workInterests) { item ->
                    FilterChip(selected = category == item, onClick = { category = item }, label = { Text(item) })
                }
            }
            DraftField(title, { title = it }, "Título de la oportunidad", "Escribe entre 3 y 120 caracteres", showError, minLength = 3, maxLength = 120)
            DraftField(description, { description = it }, "Tareas a realizar", "Escribe entre 10 y 3000 caracteres", showError, multiline = true, minLength = 10, maxLength = 3000)
            DraftField(requirements, { requirements = it }, "Conocimientos o requisitos", "Escribe entre 2 y 1500 caracteres", showError, multiline = true, minLength = 2, maxLength = 1500)
            Text("¿Necesita experiencia previa?", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = noExperience == true, onClick = { noExperience = true }, label = { Text("No es necesaria") })
                FilterChip(selected = noExperience == false, onClick = { noExperience = false }, label = { Text("Sí es necesaria") })
            }
            if (showError && noExperience == null) Text("Selecciona una opción", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Text("¿Quién ofrece esta oportunidad?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (model.profileOrganization.trim().length >= 2 || includeBusiness) {
                Row(Modifier.fillMaxWidth().toggleable(value = includeBusiness, role = Role.Checkbox, onValueChange = { includeBusiness = it }), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = includeBusiness, onCheckedChange = null)
                    Column(Modifier.weight(1f)) {
                        Text(if (model.profileOrganization.isBlank()) "Mostrar datos del negocio" else "Mostrar ${model.profileOrganization}", style = MaterialTheme.typography.bodyLarge)
                        Text("También se mostrará el rubro y la descripción guardados en tu perfil", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (includeBusiness && model.profileOrganization.trim().length < 2) Text("Completa el nombre del negocio o desactiva esta opción", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            } else {
                Text("Puedes publicar como persona o agregar datos de un negocio desde tu perfil", style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = onAccount) { Text("Ir a datos de mi perfil") }
            }
        } else if (step == 1) {
            FormSectionHeading("02", "Condiciones", "Aclara el tiempo, el lugar y el pago")
            Text("Modalidad de trabajo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Presencial", "Híbrido", "Remoto").forEach { mode ->
                    FilterChip(selected = workMode == mode, onClick = {
                        if (mode == "Remoto" && location.isBlank()) location = "Remoto"
                        else if (mode != "Remoto" && location.equals("Remoto", ignoreCase = true)) location = ""
                        workMode = mode
                    }, label = { Text(mode) })
                }
            }
            if (showError && workMode == null) Text("Selecciona una modalidad", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            DraftField(location, { location = it }, "Ubicación o remoto", "Escribe entre 2 y 120 caracteres", showError, minLength = 2, maxLength = 120)
            OutlinedTextField(
                value = payAmount,
                onValueChange = { payAmount = it.filter { char -> char.isDigit() || char == '.' || char == ',' }.take(9) },
                label = { Text("Monto en soles") },
                prefix = { Text("S/ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = showError && payment == null,
                supportingText = if (showError && payment == null) { { Text("Ingresa un monto desde S/ 1, con hasta dos decimales") } } else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
            )
            ExposedDropdownMenuBox(expanded = showPayUnits, onExpandedChange = { showPayUnits = it }) {
                OutlinedTextField(
                    value = payUnit, onValueChange = {}, readOnly = true,
                    label = { Text("Pago por") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPayUnits) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                )
                ExposedDropdownMenu(expanded = showPayUnits, onDismissRequest = { showPayUnits = false }) {
                    listOf("Por hora", "Por día", "Por tarea", "Por mes").forEach { unit ->
                        DropdownMenuItem(text = { Text(unit) }, onClick = { payUnit = unit; showPayUnits = false })
                    }
                }
            }
            Text("¿Cuándo será?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Puedes elegir hoy o una fecha futura, o dejar la fecha por coordinar", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { FilterChip(selected = selectedDate == todayMillis, onClick = { selectedDate = todayMillis; flexibleDate = false }, label = { Text("Hoy") }) }
                item { FilterChip(selected = selectedDate == dateAsUtcMillis(today.plusDays(1)), onClick = { selectedDate = dateAsUtcMillis(today.plusDays(1)); flexibleDate = false }, label = { Text("Mañana") }) }
                item { FilterChip(selected = flexibleDate, onClick = { selectedDate = null; flexibleDate = true }, label = { Text("Por coordinar") }) }
            }
            OutlinedButton(onClick = { showCalendar = true }, modifier = Modifier.fillMaxWidth()) {
                Text(selectedDate?.let { "Cambiar fecha · ${displayWorkDate(it)}" } ?: "Elegir una fecha en el calendario")
            }
            if (showError && selectedDate != null && selectedDate!! < todayMillis) Text("Elige una fecha de hoy en adelante", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            DraftField(
                duration, { duration = it }, "Duración u horario",
                if (schedulePrefix.isEmpty()) "Indica una duración u horario o elige una fecha" else "Acorta la duración u horario",
                showError, minLength = if (schedulePrefix.isEmpty()) 2 else 0, maxLength = if (schedulePrefix.isEmpty()) 120 else 117 - schedulePrefix.length,
            )
            Text("Por ejemplo: 4 horas, por las tardes o por coordinar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        } else {
            FormSectionHeading("03", "Revisión", "Comprueba la información antes de publicar")
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    DetailRow("Tipo de trabajo", category)
                    DetailRow("Título", title.trim())
                    DetailRow("Tareas", description.trim())
                    DetailRow("Conocimientos", requirements.trim())
                    DetailRow("Experiencia previa", if (noExperience == true) "No es necesaria" else "Es necesaria")
                    if (includeBusiness) {
                        DetailRow("Negocio", model.profileOrganization)
                        if (model.profileBusinessSector.isNotBlank()) DetailRow("Rubro", model.profileBusinessSector)
                        if (model.profileBusinessDescription.isNotBlank()) DetailRow("Descripción del negocio", model.profileBusinessDescription)
                    }
                    DetailRow("Ubicación", location.trim())
                    DetailRow("Modalidad", workMode.orEmpty())
                    DetailRow("Pago", payment.orEmpty())
                    DetailRow("Fecha y duración", schedule)
                    DetailRow("Vacantes", vacancies)
                }
            }
            Text("La publicación mostrará el nombre de tu cuenta", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { step = 0; showError = false; scope.launch { scrollState.animateScrollTo(0) } }) { Text("Editar trabajo") }
                OutlinedButton(onClick = { step = 1; showError = false; scope.launch { scrollState.animateScrollTo(0) } }) { Text("Editar condiciones") }
            }
        }
        if (step < 2) {
            AppPrimaryButton("Continuar", enabled = !model.busy, onClick = {
                if (invalidCount == 0) { step++; showError = false } else showError = true
                scope.launch { scrollState.animateScrollTo(0) }
            })
        } else {
            AppPrimaryButton("Publicar oportunidad", enabled = model.userId != null && !model.busy, onClick = {
                if (workErrors > 0 || conditionErrors > 0) {
                    step = if (workErrors > 0) 0 else 1
                    showError = true
                    scope.launch { scrollState.animateScrollTo(0) }
                } else confirmPublish = true
            })
        }
        if (step > 0) TextButton(enabled = !model.busy, onClick = { step--; showError = false; scope.launch { scrollState.animateScrollTo(0) } }) { Text("Paso anterior") }
        if (model.busy) AppLoadingState("Publicando oportunidad")
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
    minLength: Int = 1,
    maxLength: Int = Int.MAX_VALUE,
) {
    val isInvalid = showError && value.trim().length !in minLength..maxLength
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
private fun ActivityScreen(model: OpportunityViewModel, activityRevision: Int, openDestination: (String) -> Unit, open: (String) -> Unit) {
    val reduceMotion = LocalAppPreferences.current?.reduceMotion == true
    val myPosts = model.opportunities.filter { it.own }
    val myApplications = model.opportunities.filter { it.id in model.applications }
    val myExperiences = myApplications.filter { model.applications[it.id] == "completed" && model.confirmedWork[it.id] == true }
    val purpose = LocalAppPreferences.current?.purpose
    var section by rememberSaveable(activityRevision) { mutableStateOf(if (purpose == "worker") "Postulaciones" else "Publicaciones") }
    LazyColumn(contentPadding = PaddingValues(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(start = 18.dp, end = 6.dp, top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Tu actividad", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text("Revisa tus publicaciones y postulaciones", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    if (LocalConfiguration.current.fontScale < 1.3f && LocalAppPreferences.current?.showMascot != false) EmpuWalkIn(EmpuMood.IDLE, Modifier.size(90.dp), accessory = if (purpose == "employer") EmpuAccessory.BUSINESS else EmpuAccessory.WORK)
                }
            }
        }
        if (model.userId == null) item { AppEmptyState("Inicia sesión para ver tu actividad", "Tus publicaciones y postulaciones aparecerán aquí", "Ir a mi cuenta") { openDestination("perfil") } }
        if (model.loading || model.applicationsLoading) item { AppLoadingState("Cargando actividad") }
        (model.error ?: model.feedError ?: model.applicationsError ?: model.ownError)?.let { message -> item { AppNotice(message, isError = true); TextButton(onClick = model::retry) { Text("Reintentar") } } }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (purpose == "employer" || myPosts.isNotEmpty()) item { FilterChip(selected = section == "Publicaciones", onClick = { section = "Publicaciones" }, label = { Text("Publicaciones (${myPosts.size})") }) }
                item { FilterChip(selected = section == "Postulaciones", onClick = { section = "Postulaciones" }, label = { Text("Postulaciones (${myApplications.size})") }) }
                item { FilterChip(selected = section == "Experiencias", onClick = { section = "Experiencias" }, label = { Text("Experiencias (${myExperiences.size})") }) }
            }
        }
        if (model.userId != null) item {
            Text(when (section) { "Publicaciones" -> "Oportunidades que publicaste"; "Experiencias" -> "Trabajos confirmados por ambas partes"; else -> "Oportunidades a las que postulaste" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        if (model.userId == null || (section == "Publicaciones" && model.ownError != null && myPosts.isEmpty()) || (section != "Publicaciones" && (model.applicationsLoading || model.applicationsError != null) && myApplications.isEmpty())) {
            Unit
        } else if (section == "Experiencias") {
            if (myExperiences.isEmpty()) item { AppEmptyState("Aún no hay experiencias confirmadas", "Cuando ambas partes confirmen un trabajo aparecerá aquí") }
            items(myExperiences, key = { "experience-${it.id}" }) { item -> OpportunityCard(item, "Experiencia confirmada", if (reduceMotion) Modifier else Modifier.animateItem(), availabilityConfirmed = !model.feedFromCache) { open(item.id) } }
        } else if (myPosts.isEmpty() && myApplications.isEmpty()) {
            item {
                AnimatedContent(
                    targetState = section,
                    transitionSpec = { fadeIn(tween(if (reduceMotion) 0 else 180)) togetherWith fadeOut(tween(if (reduceMotion) 0 else 120)) },
                    label = "actividad",
                ) { current ->
                    if (current == "Publicaciones") {
                        AppEmptyState("Aún no tienes publicaciones", "Crea una oportunidad para encontrar apoyo", "Crear publicación", EmptyIllustration.Posts) { openDestination("publicar") }
                    } else {
                        AppEmptyState("Aún no tienes postulaciones", "Explora las oportunidades y encuentra una para ti", "Explorar oportunidades", EmptyIllustration.Applications) { openDestination("explorar") }
                    }
                }
            }
        } else if (section == "Publicaciones") {
            if (myPosts.isEmpty()) item { AppEmptyState("Aún no tienes publicaciones", "Crea una oportunidad para encontrar apoyo", "Crear publicación", EmptyIllustration.Posts) { openDestination("publicar") } }
            items(myPosts, key = { "post-${it.id}" }) { item -> OpportunityCard(item, when (item.status) { "removed" -> "Retirada"; "closed" -> "Cerrada"; else -> "Publicada" }, if (reduceMotion) Modifier else Modifier.animateItem(), availabilityConfirmed = !model.feedFromCache) { open(item.id) } }
        } else {
            if (myApplications.isEmpty()) item { AppEmptyState("Aún no tienes postulaciones", "Explora las oportunidades y encuentra una para ti", "Explorar oportunidades", EmptyIllustration.Applications) { openDestination("explorar") } }
            items(myApplications, key = { "application-${it.id}" }) { item -> OpportunityCard(item, when (model.applications[item.id]) { "reviewing" -> "En revisión"; "selected" -> "Seleccionada"; "rejected" -> "No seleccionada"; "cancelled" -> "Selección cancelada"; "completed" -> "Trabajo finalizado"; "withdrawn" -> "Postulación retirada"; else -> "Postulación enviada" }, if (reduceMotion) Modifier else Modifier.animateItem(), availabilityConfirmed = !model.feedFromCache) { open(item.id) } }
        }
    }
}

@Composable
private fun ProfileScreen(onSettings: () -> Unit, onSaved: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Perfil", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(72.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.PersonOutline, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(36.dp))
                }
                Spacer(Modifier.height(12.dp))
                Text("Tu cuenta", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("El acceso no está disponible en este momento", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text("TuEmpuje conecta oportunidades, habilidades y personas", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedButton(onClick = onSaved, modifier = Modifier.fillMaxWidth()) { Text("Oportunidades guardadas") }
        OutlinedButton(onClick = onSettings, modifier = Modifier.fillMaxWidth()) { Text("Personalizar aplicación") }
    }
}
