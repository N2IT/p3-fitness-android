package com.fittrack.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fittrack.FitTrackApplication
import com.fittrack.data.entity.Exercise
import com.fittrack.data.entity.RoutineExercise
import com.fittrack.data.entity.WorkoutRoutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RoutineListUiState(
    val routines: List<WorkoutRoutine> = emptyList(),
    val routineExerciseCounts: Map<Int, Int> = emptyMap(),
    val routineExerciseNames: Map<Int, List<String>> = emptyMap(),
    val isLoading: Boolean = true
)

data class RoutineEditUiState(
    val routine: WorkoutRoutine? = null,
    val routineName: String = "",
    val exercises: List<RoutineExerciseWithDetails> = emptyList(),
    val allExercises: List<Exercise> = emptyList(),
    val muscleGroups: List<String> = emptyList(),
    val searchQuery: String = "",
    val selectedMuscleGroup: String? = null,
    val filteredExercises: List<Exercise> = emptyList(),
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val showExercisePicker: Boolean = false
)

data class RoutineExerciseWithDetails(
    val routineExercise: RoutineExercise,
    val exercise: Exercise
)

class RoutineViewModel(application: Application, private val userId: Int) : AndroidViewModel(application) {
    private val db = (application as FitTrackApplication).database
    private val routineDao = db.workoutRoutineDao()
    private val routineExerciseDao = db.routineExerciseDao()
    private val exerciseDao = db.exerciseDao()

    private val _listState = MutableStateFlow(RoutineListUiState())
    val listState: StateFlow<RoutineListUiState> = _listState.asStateFlow()

    private val _editState = MutableStateFlow(RoutineEditUiState())
    val editState: StateFlow<RoutineEditUiState> = _editState.asStateFlow()

    init {
        loadRoutines()
        loadExerciseLibrary()
    }

    private fun loadRoutines() {
        viewModelScope.launch {
            routineDao.getRoutinesForUser(userId).collect { routines ->
                val counts = mutableMapOf<Int, Int>()
                val names = mutableMapOf<Int, List<String>>()
                routines.forEach { routine ->
                    val exercises = routineExerciseDao.getExercisesForRoutineOnce(routine.id)
                    counts[routine.id] = exercises.size
                    names[routine.id] = exercises.take(3).mapNotNull { re ->
                        exerciseDao.getExerciseById(re.exerciseId)?.name
                    }
                }
                _listState.value = RoutineListUiState(
                    routines = routines,
                    routineExerciseCounts = counts,
                    routineExerciseNames = names,
                    isLoading = false
                )
            }
        }
    }

    private fun loadExerciseLibrary() {
        viewModelScope.launch {
            combine(
                exerciseDao.getAllExercises(),
                exerciseDao.getAllMuscleGroups()
            ) { exercises, groups ->
                _editState.value = _editState.value.copy(
                    allExercises = exercises,
                    muscleGroups = groups,
                    filteredExercises = exercises
                )
            }.collect()
        }
    }

    fun loadRoutineForEdit(routineId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val routine = routineDao.getRoutineById(routineId)
            val routineExercises = routineExerciseDao.getExercisesForRoutineOnce(routineId)
            val withDetails = routineExercises.mapNotNull { re ->
                exerciseDao.getExerciseById(re.exerciseId)?.let { exercise ->
                    RoutineExerciseWithDetails(re, exercise)
                }
            }
            _editState.value = _editState.value.copy(
                routine = routine,
                routineName = routine?.name ?: "",
                exercises = withDetails
            )
        }
    }

    fun initNewRoutine() {
        _editState.value = _editState.value.copy(
            routine = null,
            routineName = "",
            exercises = emptyList(),
            isSaved = false
        )
    }

    fun updateRoutineName(name: String) {
        _editState.value = _editState.value.copy(routineName = name)
    }

    fun updateSearchQuery(query: String) {
        _editState.value = _editState.value.copy(searchQuery = query)
        filterExercises()
    }

    fun selectMuscleGroup(group: String?) {
        _editState.value = _editState.value.copy(
            selectedMuscleGroup = if (_editState.value.selectedMuscleGroup == group) null else group
        )
        filterExercises()
    }

    private fun filterExercises() {
        val state = _editState.value
        val filtered = state.allExercises.filter { exercise ->
            val matchesSearch = state.searchQuery.isBlank() ||
                    exercise.name.contains(state.searchQuery, ignoreCase = true)
            val matchesGroup = state.selectedMuscleGroup == null ||
                    exercise.muscleGroup == state.selectedMuscleGroup
            matchesSearch && matchesGroup
        }
        _editState.value = state.copy(filteredExercises = filtered)
    }

    fun showExercisePicker() {
        _editState.value = _editState.value.copy(showExercisePicker = true, searchQuery = "", selectedMuscleGroup = null)
        filterExercises()
    }

    fun hideExercisePicker() {
        _editState.value = _editState.value.copy(showExercisePicker = false)
    }

    fun addExerciseToRoutine(exercise: Exercise) {
        val currentExercises = _editState.value.exercises
        val nextOrder = (currentExercises.maxOfOrNull { it.routineExercise.orderIndex } ?: -1) + 1
        val newRe = RoutineExercise(
            routineId = _editState.value.routine?.id ?: 0,
            exerciseId = exercise.id,
            orderIndex = nextOrder
        )
        _editState.value = _editState.value.copy(
            exercises = currentExercises + RoutineExerciseWithDetails(newRe, exercise),
            showExercisePicker = false
        )
    }

    fun removeExerciseAt(index: Int) {
        val current = _editState.value.exercises.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            // Reindex
            val reindexed = current.mapIndexed { i, item ->
                item.copy(routineExercise = item.routineExercise.copy(orderIndex = i))
            }
            _editState.value = _editState.value.copy(exercises = reindexed)
        }
    }

    fun moveExercise(fromIndex: Int, toIndex: Int) {
        val current = _editState.value.exercises.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            val reindexed = current.mapIndexed { i, ex ->
                ex.copy(routineExercise = ex.routineExercise.copy(orderIndex = i))
            }
            _editState.value = _editState.value.copy(exercises = reindexed)
        }
    }

    fun updateSetsReps(index: Int, sets: Int, reps: Int) {
        val current = _editState.value.exercises.toMutableList()
        if (index in current.indices) {
            current[index] = current[index].copy(
                routineExercise = current[index].routineExercise.copy(
                    defaultSets = sets, defaultReps = reps
                )
            )
            _editState.value = _editState.value.copy(exercises = current)
        }
    }

    fun saveRoutine() {
        val state = _editState.value
        if (state.routineName.isBlank()) return

        _editState.value = state.copy(isSaving = true)
        viewModelScope.launch(Dispatchers.IO) {
            val routineId = if (state.routine != null) {
                routineDao.update(state.routine.copy(name = state.routineName))
                routineExerciseDao.deleteAllForRoutine(state.routine.id)
                state.routine.id
            } else {
                routineDao.insert(
                    WorkoutRoutine(userId = userId, name = state.routineName)
                ).toInt()
            }

            val routineExercises = state.exercises.mapIndexed { index, item ->
                item.routineExercise.copy(
                    routineId = routineId,
                    orderIndex = index
                )
            }
            routineExerciseDao.insertAll(routineExercises)

            _editState.value = _editState.value.copy(isSaving = false, isSaved = true)
        }
    }

    fun deleteRoutine(routine: WorkoutRoutine) {
        viewModelScope.launch(Dispatchers.IO) {
            routineDao.delete(routine)
        }
    }

    class Factory(private val application: Application, private val userId: Int) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RoutineViewModel(application, userId) as T
        }
    }
}
