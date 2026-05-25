package com.fittrack.ui.navigation

object NavRoutes {
    // Phase 1
    const val LOGIN = "login"
    const val ROUTINE_LIST = "routine_list"
    const val ROUTINE_EDIT = "routine_edit/{routineId}"
    const val ROUTINE_CREATE = "routine_create"
    const val ACTIVE_WORKOUT = "active_workout/{routineId}"

    // Phase 2
    const val HISTORY = "history"
    const val WORKOUT_DETAIL = "workout_detail/{sessionId}"
    const val PROGRESS = "progress"
    const val BODY_WEIGHT = "body_weight"

    // Phase 3
    const val IMPORT_ROUTINE = "import_routine"

    // Phase 4
    const val ONBOARDING = "onboarding"
    const val ACHIEVEMENTS = "achievements"
    const val CALCULATOR = "calculator"
    const val SETTINGS = "settings"

    // Helpers
    fun routineEdit(routineId: Int) = "routine_edit/$routineId"
    fun activeWorkout(routineId: Int) = "active_workout/$routineId"
    fun workoutDetail(sessionId: String) = "workout_detail/$sessionId"
}
