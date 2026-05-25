package com.fittrack.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fittrack.FitTrackApplication
import com.fittrack.data.PrDetector
import com.fittrack.data.entity.Exercise
import com.fittrack.data.entity.ExerciseLog
import com.fittrack.data.entity.RoutineExercise
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class PrCelebration(
    val exerciseName: String,
    val recordType: String,
    val value: Double
)

data class WorkoutSet(
    val setNumber: Int,
    val weight: String = "",
    val reps: String = "",
    val isCompleted: Boolean = false,
    val isPr: Boolean = false,
    val previousWeight: Double? = null,
    val previousReps: Int? = null
)

data class WorkoutExerciseState(
    val exercise: Exercise,
    val routineExercise: RoutineExercise,
    val sets: List<WorkoutSet>
)

data class ActiveWorkoutUiState(
    val routineName: String = "",
    val exercises: List<WorkoutExerciseState> = emptyList(),
    val elapsedSeconds: Long = 0,
    val isLoading: Boolean = true,
    val isFinishing: Boolean = false,
    val isFinished: Boolean = false,
    val isDiscarded: Boolean = false,
    val totalSets: Int = 0,
    val completedSets: Int = 0,
    val activeExerciseIndex: Int = 0,
    val activeSetIndex: Int = -1,
    val inputMode: InputMode = InputMode.NONE,
    val prCelebration: PrCelebration? = null
)

enum class InputMode { NONE, WEIGHT, REPS }

class WorkoutViewModel(
    application: Application,
    private val userId: Int,
    private val routineId: Int
) : AndroidViewModel(application) {
    private val db = (application as FitTrackApplication).database
    private val routineDao = db.workoutRoutineDao()
    private val routineExerciseDao = db.routineExerciseDao()
    private val exerciseDao = db.exerciseDao()
    private val exerciseLogDao = db.exerciseLogDao()
    private val prDetector = PrDetector(db.personalRecordDao())

    private val sessionId = UUID.randomUUID().toString()
    private var startTime = System.currentTimeMillis()

    private val _uiState = MutableStateFlow(ActiveWorkoutUiState())
    val uiState: StateFlow<ActiveWorkoutUiState> = _uiState.asStateFlow()

    init {
        loadWorkout()
        startTimer()
    }

    private fun loadWorkout() {
        viewModelScope.launch(Dispatchers.IO) {
            val routine = routineDao.getRoutineById(routineId) ?: return@launch
            val routineExercises = routineExerciseDao.getExercisesForRoutineOnce(routineId)

            val exerciseStates = routineExercises.mapNotNull { re ->
                val exercise = exerciseDao.getExerciseById(re.exerciseId) ?: return@mapNotNull null
                val previousLogs = exerciseLogDao.getLastSessionLogsForExercise(re.exerciseId, userId)

                val sets = (1..re.defaultSets).map { setNum ->
                    val prev = previousLogs.find { it.setNumber == setNum }
                    WorkoutSet(
                        setNumber = setNum,
                        previousWeight = prev?.weight,
                        previousReps = prev?.reps,
                        weight = prev?.weight?.let { formatWeight(it) } ?: "",
                        reps = prev?.reps?.toString() ?: ""
                    )
                }
                WorkoutExerciseState(exercise, re, sets)
            }

            val totalSets = exerciseStates.sumOf { it.sets.size }
            _uiState.value = ActiveWorkoutUiState(
                routineName = routine.name,
                exercises = exerciseStates,
                isLoading = false,
                totalSets = totalSets
            )
        }
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                val elapsed = (System.currentTimeMillis() - startTime) / 1000
                _uiState.value = _uiState.value.copy(elapsedSeconds = elapsed)
            }
        }
    }

    private fun formatWeight(weight: Double): String {
        return if (weight == weight.toLong().toDouble()) {
            weight.toLong().toString()
        } else {
            weight.toString()
        }
    }

    fun updateWeight(exerciseIndex: Int, setIndex: Int, weight: String) {
        val filtered = weight.filter { it.isDigit() || it == '.' }
        updateSet(exerciseIndex, setIndex) { it.copy(weight = filtered) }
    }

    fun updateReps(exerciseIndex: Int, setIndex: Int, reps: String) {
        val filtered = reps.filter { it.isDigit() }
        updateSet(exerciseIndex, setIndex) { it.copy(reps = filtered) }
    }

    fun toggleSetCompleted(exerciseIndex: Int, setIndex: Int) {
        val state = _uiState.value
        val exercise = state.exercises.getOrNull(exerciseIndex) ?: return
        val set = exercise.sets.getOrNull(setIndex) ?: return

        val newCompleted = !set.isCompleted
        updateSet(exerciseIndex, setIndex) { it.copy(isCompleted = newCompleted) }

        // Recalculate completed count
        val updatedExercises = _uiState.value.exercises
        val completedCount = updatedExercises.sumOf { ex -> ex.sets.count { it.isCompleted } }
        _uiState.value = _uiState.value.copy(completedSets = completedCount)

        // Check for PRs when completing a set
        if (newCompleted) {
            val weight = set.weight.toDoubleOrNull() ?: return
            val reps = set.reps.toIntOrNull() ?: return
            viewModelScope.launch(Dispatchers.IO) {
                val prs = prDetector.checkForPrs(
                    exercise.exercise.id, userId, weight, reps, sessionId
                )
                if (prs.isNotEmpty()) {
                    val best = prs.first()
                    updateSet(exerciseIndex, setIndex) { it.copy(isPr = true) }
                    _uiState.value = _uiState.value.copy(
                        prCelebration = PrCelebration(
                            exerciseName = exercise.exercise.name,
                            recordType = best.recordType,
                            value = best.newValue
                        )
                    )
                    delay(2000)
                    _uiState.value = _uiState.value.copy(prCelebration = null)
                }
            }
        }
    }

    fun dismissPrCelebration() {
        _uiState.value = _uiState.value.copy(prCelebration = null)
    }

    fun addSet(exerciseIndex: Int) {
        val state = _uiState.value
        val exercise = state.exercises.getOrNull(exerciseIndex) ?: return
        val newSetNumber = exercise.sets.size + 1
        val lastSet = exercise.sets.lastOrNull()
        val newSet = WorkoutSet(
            setNumber = newSetNumber,
            weight = lastSet?.weight ?: "",
            reps = lastSet?.reps ?: ""
        )
        val updatedSets = exercise.sets + newSet
        val updatedExercise = exercise.copy(sets = updatedSets)
        val updatedExercises = state.exercises.toMutableList()
        updatedExercises[exerciseIndex] = updatedExercise
        _uiState.value = state.copy(
            exercises = updatedExercises,
            totalSets = state.totalSets + 1
        )
    }

    fun removeSet(exerciseIndex: Int, setIndex: Int) {
        val state = _uiState.value
        val exercise = state.exercises.getOrNull(exerciseIndex) ?: return
        if (exercise.sets.size <= 1) return
        val updatedSets = exercise.sets.toMutableList().apply { removeAt(setIndex) }
            .mapIndexed { i, set -> set.copy(setNumber = i + 1) }
        val updatedExercise = exercise.copy(sets = updatedSets)
        val updatedExercises = state.exercises.toMutableList()
        updatedExercises[exerciseIndex] = updatedExercise
        val completedCount = updatedExercises.sumOf { ex -> ex.sets.count { it.isCompleted } }
        _uiState.value = state.copy(
            exercises = updatedExercises,
            totalSets = state.totalSets - 1,
            completedSets = completedCount
        )
    }

    fun setActiveInput(exerciseIndex: Int, setIndex: Int, mode: InputMode) {
        _uiState.value = _uiState.value.copy(
            activeExerciseIndex = exerciseIndex,
            activeSetIndex = setIndex,
            inputMode = mode
        )
    }

    fun appendToInput(digit: String) {
        val state = _uiState.value
        val exerciseIndex = state.activeExerciseIndex
        val setIndex = state.activeSetIndex
        if (setIndex < 0) return

        val exercise = state.exercises.getOrNull(exerciseIndex) ?: return
        val set = exercise.sets.getOrNull(setIndex) ?: return

        when (state.inputMode) {
            InputMode.WEIGHT -> {
                val newWeight = set.weight + digit
                updateWeight(exerciseIndex, setIndex, newWeight)
            }
            InputMode.REPS -> {
                val newReps = set.reps + digit
                updateReps(exerciseIndex, setIndex, newReps)
            }
            InputMode.NONE -> {}
        }
    }

    fun backspaceInput() {
        val state = _uiState.value
        val exerciseIndex = state.activeExerciseIndex
        val setIndex = state.activeSetIndex
        if (setIndex < 0) return

        val exercise = state.exercises.getOrNull(exerciseIndex) ?: return
        val set = exercise.sets.getOrNull(setIndex) ?: return

        when (state.inputMode) {
            InputMode.WEIGHT -> {
                val newWeight = set.weight.dropLast(1)
                updateWeight(exerciseIndex, setIndex, newWeight)
            }
            InputMode.REPS -> {
                val newReps = set.reps.dropLast(1)
                updateReps(exerciseIndex, setIndex, newReps)
            }
            InputMode.NONE -> {}
        }
    }

    fun clearInput() {
        val state = _uiState.value
        val exerciseIndex = state.activeExerciseIndex
        val setIndex = state.activeSetIndex
        if (setIndex < 0) return

        when (state.inputMode) {
            InputMode.WEIGHT -> updateWeight(exerciseIndex, setIndex, "")
            InputMode.REPS -> updateReps(exerciseIndex, setIndex, "")
            InputMode.NONE -> {}
        }
    }

    fun dismissInput() {
        _uiState.value = _uiState.value.copy(inputMode = InputMode.NONE, activeSetIndex = -1)
    }

    fun finishWorkout() {
        _uiState.value = _uiState.value.copy(isFinishing = true)
        viewModelScope.launch(Dispatchers.IO) {
            val state = _uiState.value
            val logs = mutableListOf<ExerciseLog>()

            state.exercises.forEach { exerciseState ->
                exerciseState.sets.filter { it.isCompleted }.forEach { set ->
                    val weight = set.weight.toDoubleOrNull() ?: 0.0
                    val reps = set.reps.toIntOrNull() ?: 0
                    logs.add(
                        ExerciseLog(
                            exerciseId = exerciseState.exercise.id,
                            routineId = routineId,
                            userId = userId,
                            workoutSessionId = sessionId,
                            setNumber = set.setNumber,
                            weight = weight,
                            reps = reps
                        )
                    )
                }
            }

            if (logs.isNotEmpty()) {
                exerciseLogDao.insertAll(logs)
                routineDao.markPerformed(routineId)
            }

            _uiState.value = _uiState.value.copy(isFinishing = false, isFinished = true)
        }
    }

    fun discardWorkout() {
        _uiState.value = _uiState.value.copy(isDiscarded = true)
    }

    private fun updateSet(exerciseIndex: Int, setIndex: Int, transform: (WorkoutSet) -> WorkoutSet) {
        val state = _uiState.value
        val exercise = state.exercises.getOrNull(exerciseIndex) ?: return
        val set = exercise.sets.getOrNull(setIndex) ?: return
        val updatedSets = exercise.sets.toMutableList()
        updatedSets[setIndex] = transform(set)
        val updatedExercise = exercise.copy(sets = updatedSets)
        val updatedExercises = state.exercises.toMutableList()
        updatedExercises[exerciseIndex] = updatedExercise
        _uiState.value = state.copy(exercises = updatedExercises)
    }

    class Factory(
        private val application: Application,
        private val userId: Int,
        private val routineId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WorkoutViewModel(application, userId, routineId) as T
        }
    }
}
