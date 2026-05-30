package com.fittrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fittrack.ui.navigation.NavRoutes
import com.fittrack.ui.screen.*
import com.fittrack.ui.theme.*
import com.fittrack.ui.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitTrackTheme {
                FitTrackApp()
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(NavRoutes.ROUTINE_LIST, "Workout", Icons.Default.FitnessCenter),
    BottomNavItem(NavRoutes.HISTORY, "History", Icons.Default.Schedule),
    BottomNavItem(NavRoutes.PROGRESS, "Progress", Icons.Default.ShowChart),
    BottomNavItem(NavRoutes.IMPORT_ROUTINE, "Tools", Icons.Default.Build),
    BottomNavItem(NavRoutes.SETTINGS, "Profile", Icons.Default.Person)
)

// Routes where bottom nav should be visible
val bottomNavRoutes = setOf(
    NavRoutes.ROUTINE_LIST, NavRoutes.HISTORY, NavRoutes.PROGRESS,
    NavRoutes.IMPORT_ROUTINE, NavRoutes.SETTINGS, NavRoutes.BODY_WEIGHT,
    NavRoutes.CALCULATOR, NavRoutes.ACHIEVEMENTS
)

@Composable
fun FitTrackApp() {
    val navController = rememberNavController()
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as FitTrackApplication
    val savedUserId = remember { app.sessionManager.getUserId() }
    var currentUserId by rememberSaveable { mutableIntStateOf(savedUserId) }
    val startDestination = if (savedUserId != -1) NavRoutes.ROUTINE_LIST else NavRoutes.LOGIN
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomNav = currentRoute in bottomNavRoutes

    // If the DB was wiped (e.g. destructive migration) while a session was saved,
    // the stored userId won't exist. Detect that and send back to login.
    LaunchedEffect(Unit) {
        if (savedUserId != -1) {
            val userExists = withContext(Dispatchers.IO) {
                app.database.userDao().getUserById(savedUserId) != null
            }
            if (!userExists) {
                app.sessionManager.clearSession()
                currentUserId = -1
                navController.navigate(NavRoutes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            if (showBottomNav) {
                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = TextPrimary
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(item.icon, item.label)
                            },
                            label = {
                                Text(
                                    item.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ElectricBlue,
                                selectedTextColor = ElectricBlue,
                                unselectedIconColor = TextTertiary,
                                unselectedTextColor = TextTertiary,
                                indicatorColor = ElectricBlue.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            enterTransition = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 4 } },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { -it / 4 } },
            popExitTransition = { fadeOut(tween(200)) }
        ) {
            // === LOGIN ===
            composable(NavRoutes.LOGIN) {
                val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as FitTrackApplication
                val vm: LoginViewModel = viewModel(factory = LoginViewModel.Factory(app))
                val uiState by vm.uiState.collectAsState()

                LaunchedEffect(uiState.loggedInUserId) {
                    uiState.loggedInUserId?.let { userId ->
                        currentUserId = userId
                        navController.navigate(NavRoutes.ROUTINE_LIST) {
                            popUpTo(NavRoutes.LOGIN) { inclusive = true }
                        }
                    }
                }

                LoginScreen(
                    uiState = uiState,
                    onUsernameChanged = vm::updateUsername,
                    onUnitToggle = vm::toggleUnit,
                    onLogin = vm::login
                )
            }

            // === ROUTINE LIST ===
            composable(NavRoutes.ROUTINE_LIST) {
                val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as FitTrackApplication
                val vm: RoutineViewModel = viewModel(
                    key = "routines_$currentUserId",
                    factory = RoutineViewModel.Factory(app, currentUserId)
                )
                val listState by vm.listState.collectAsState()

                RoutineListScreen(
                    uiState = listState,
                    onCreateRoutine = {
                        vm.initNewRoutine()
                        navController.navigate(NavRoutes.ROUTINE_CREATE)
                    },
                    onEditRoutine = { navController.navigate(NavRoutes.routineEdit(it)) },
                    onStartWorkout = { navController.navigate(NavRoutes.activeWorkout(it)) },
                    onDeleteRoutine = vm::deleteRoutine
                )
            }

            // === ROUTINE CREATE ===
            composable(NavRoutes.ROUTINE_CREATE) {
                val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as FitTrackApplication
                val vm: RoutineViewModel = viewModel(factory = RoutineViewModel.Factory(app, currentUserId))
                val editState by vm.editState.collectAsState()

                LaunchedEffect(Unit) { vm.initNewRoutine() }
                LaunchedEffect(editState.isSaved) {
                    if (editState.isSaved) navController.popBackStack()
                }

                RoutineEditScreen(
                    uiState = editState,
                    onNameChanged = vm::updateRoutineName,
                    onAddExercise = vm::showExercisePicker,
                    onRemoveExercise = vm::removeExerciseAt,
                    onMoveExercise = vm::moveExercise,
                    onUpdateSetsReps = vm::updateSetsReps,
                    onSave = vm::saveRoutine,
                    onBack = { navController.popBackStack() },
                    onSearchChanged = vm::updateSearchQuery,
                    onMuscleGroupSelected = vm::selectMuscleGroup,
                    onExercisePicked = vm::addExerciseToRoutine,
                    onDismissExercisePicker = vm::hideExercisePicker
                )
            }

            // === ROUTINE EDIT ===
            composable(
                route = NavRoutes.ROUTINE_EDIT,
                arguments = listOf(navArgument("routineId") { type = NavType.IntType })
            ) { entry ->
                val routineId = entry.arguments?.getInt("routineId") ?: return@composable
                val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as FitTrackApplication
                val vm: RoutineViewModel = viewModel(factory = RoutineViewModel.Factory(app, currentUserId))
                val editState by vm.editState.collectAsState()

                LaunchedEffect(routineId) { vm.loadRoutineForEdit(routineId) }
                LaunchedEffect(editState.isSaved) {
                    if (editState.isSaved) navController.popBackStack()
                }

                RoutineEditScreen(
                    uiState = editState,
                    onNameChanged = vm::updateRoutineName,
                    onAddExercise = vm::showExercisePicker,
                    onRemoveExercise = vm::removeExerciseAt,
                    onMoveExercise = vm::moveExercise,
                    onUpdateSetsReps = vm::updateSetsReps,
                    onSave = vm::saveRoutine,
                    onBack = { navController.popBackStack() },
                    onSearchChanged = vm::updateSearchQuery,
                    onMuscleGroupSelected = vm::selectMuscleGroup,
                    onExercisePicked = vm::addExerciseToRoutine,
                    onDismissExercisePicker = vm::hideExercisePicker
                )
            }

            // === ACTIVE WORKOUT ===
            composable(
                route = NavRoutes.ACTIVE_WORKOUT,
                arguments = listOf(navArgument("routineId") { type = NavType.IntType })
            ) { entry ->
                val routineId = entry.arguments?.getInt("routineId") ?: return@composable
                val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as FitTrackApplication
                val vm: WorkoutViewModel = viewModel(
                    factory = WorkoutViewModel.Factory(app, currentUserId, routineId),
                    key = "workout_$routineId"
                )
                val uiState by vm.uiState.collectAsState()

                LaunchedEffect(uiState.isFinished, uiState.isDiscarded) {
                    if (uiState.isFinished || uiState.isDiscarded) navController.popBackStack()
                }

                ActiveWorkoutScreen(
                    uiState = uiState,
                    onUpdateWeight = vm::updateWeight,
                    onUpdateReps = vm::updateReps,
                    onToggleSet = vm::toggleSetCompleted,
                    onAddSet = vm::addSet,
                    onRemoveSet = vm::removeSet,
                    onSetActiveInput = vm::setActiveInput,
                    onAppendInput = vm::appendToInput,
                    onBackspaceInput = vm::backspaceInput,
                    onClearInput = vm::clearInput,
                    onDismissInput = vm::dismissInput,
                    onFinish = vm::finishWorkout,
                    onDiscard = vm::discardWorkout
                )
            }

            // === HISTORY ===
            composable(NavRoutes.HISTORY) {
                val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as FitTrackApplication
                val vm: HistoryViewModel = viewModel(
                    key = "history_$currentUserId",
                    factory = HistoryViewModel.Factory(app, currentUserId)
                )
                val historyState by vm.historyState.collectAsState()

                HistoryScreen(
                    uiState = historyState,
                    onSessionClick = { navController.navigate(NavRoutes.workoutDetail(it)) },
                    onDeleteSession = vm::deleteSession
                )
            }

            // === WORKOUT DETAIL ===
            composable(
                route = NavRoutes.WORKOUT_DETAIL,
                arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
            ) { entry ->
                val sessionId = entry.arguments?.getString("sessionId") ?: return@composable
                val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as FitTrackApplication
                val vm: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory(app, currentUserId))
                val detailState by vm.detailState.collectAsState()

                LaunchedEffect(sessionId) { vm.loadWorkoutDetail(sessionId) }

                WorkoutDetailScreen(
                    uiState = detailState,
                    onBack = { navController.popBackStack() }
                )
            }

            // === PROGRESS ===
            composable(NavRoutes.PROGRESS) {
                val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as FitTrackApplication
                val vm: ProgressViewModel = viewModel(
                    key = "progress_$currentUserId",
                    factory = ProgressViewModel.Factory(app, currentUserId)
                )
                val progressState by vm.progressState.collectAsState()

                ProgressScreen(
                    uiState = progressState,
                    onExerciseSelected = vm::selectExercise,
                    onMetricChanged = vm::setChartMetric,
                    onTimeRangeChanged = vm::setTimeRange
                )
            }

            // === BODY WEIGHT ===
            composable(NavRoutes.BODY_WEIGHT) {
                val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as FitTrackApplication
                val vm: ProgressViewModel = viewModel(factory = ProgressViewModel.Factory(app, currentUserId))
                val bodyWeightState by vm.bodyWeightState.collectAsState()

                BodyWeightScreen(
                    uiState = bodyWeightState,
                    onWeightChanged = vm::updateWeightInput,
                    onLog = vm::logBodyWeight,
                    onBack = { navController.popBackStack() }
                )
            }

            // === IMPORT ROUTINE ===
            composable(NavRoutes.IMPORT_ROUTINE) {
                val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as FitTrackApplication
                val vm: ImportViewModel = viewModel(factory = ImportViewModel.Factory(app, currentUserId))
                val uiState by vm.uiState.collectAsState()

                LaunchedEffect(uiState.importState) {
                    if (uiState.importState is com.fittrack.data.api.ImportState.Success) {
                        // Stay on screen, user can import another
                    }
                }

                ImportRoutineScreen(
                    uiState = uiState,
                    onModeChanged = vm::setMode,
                    onUrlChanged = vm::updateUrl,
                    onTextChanged = vm::updateText,
                    onRoutineNameChanged = vm::updateRoutineName,
                    onStartImport = vm::startImport,
                    onSaveRoutine = vm::saveImportedRoutine,
                    onReset = vm::resetState,
                    onNavigateToSettings = { navController.navigate(NavRoutes.SETTINGS) }
                )
            }

            // === SETTINGS ===
            composable(NavRoutes.SETTINGS) {
                val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as FitTrackApplication
                val vm: SettingsViewModel = viewModel(
                    key = "settings_$currentUserId",
                    factory = SettingsViewModel.Factory(app, currentUserId)
                )
                val uiState by vm.uiState.collectAsState()

                SettingsScreen(
                    uiState = uiState,
                    onToggleUnit = vm::toggleUnit,
                    onSelectProvider = vm::selectProvider,
                    onApiKeyChanged = vm::updateApiKey,
                    onSaveApiKey = vm::saveApiKey,
                    onClearApiKey = vm::clearApiKey,
                    onExportData = vm::exportData,
                    onNavigateToAchievements = { navController.navigate(NavRoutes.ACHIEVEMENTS) },
                    onNavigateToBodyWeight = { navController.navigate(NavRoutes.BODY_WEIGHT) },
                    onLogout = {
                        vm.logout()
                        currentUserId = -1
                        navController.navigate(NavRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // === ACHIEVEMENTS ===
            composable(NavRoutes.ACHIEVEMENTS) {
                val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as FitTrackApplication
                val vm: SettingsViewModel = viewModel(
                    key = "settings_$currentUserId",
                    factory = SettingsViewModel.Factory(app, currentUserId)
                )
                val uiState by vm.uiState.collectAsState()

                AchievementsScreen(
                    totalWorkouts = uiState.totalWorkouts,
                    onBack = { navController.popBackStack() }
                )
            }

            // === CALCULATOR ===
            composable(NavRoutes.CALCULATOR) {
                CalculatorScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
