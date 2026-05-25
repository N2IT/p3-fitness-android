package com.fittrack.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fittrack.FitTrackApplication
import com.fittrack.data.api.ApiKeyManager
import com.fittrack.data.api.ImportState
import com.fittrack.data.api.ImportedRoutine
import com.fittrack.data.api.RoutineImporter
import com.fittrack.data.entity.Exercise
import com.fittrack.data.entity.RoutineExercise
import com.fittrack.data.entity.WorkoutRoutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ImportUiState(
    val mode: ImportMode = ImportMode.URL,
    val urlInput: String = "",
    val textInput: String = "",
    val importState: ImportState = ImportState.Idle,
    val editedRoutineName: String = "",
    val hasApiKey: Boolean = false
)

enum class ImportMode { URL, TEXT }

class ImportViewModel(
    application: Application,
    private val userId: Int
) : AndroidViewModel(application) {
    private val db = (application as FitTrackApplication).database
    private val routineDao = db.workoutRoutineDao()
    private val routineExerciseDao = db.routineExerciseDao()
    private val exerciseDao = db.exerciseDao()
    private val apiKeyManager = ApiKeyManager(application)
    private val importer = RoutineImporter(apiKeyManager)

    private val _uiState = MutableStateFlow(ImportUiState(hasApiKey = apiKeyManager.hasApiKey()))
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    fun setMode(mode: ImportMode) {
        _uiState.value = _uiState.value.copy(mode = mode, importState = ImportState.Idle)
    }

    fun updateUrl(url: String) {
        _uiState.value = _uiState.value.copy(urlInput = url)
    }

    fun updateText(text: String) {
        _uiState.value = _uiState.value.copy(textInput = text)
    }

    fun updateRoutineName(name: String) {
        _uiState.value = _uiState.value.copy(editedRoutineName = name)
    }

    fun startImport() {
        val state = _uiState.value
        viewModelScope.launch(Dispatchers.IO) {
            when (state.mode) {
                ImportMode.URL -> importer.importFromUrl(state.urlInput) { result ->
                    _uiState.value = _uiState.value.copy(
                        importState = result,
                        editedRoutineName = if (result is ImportState.Preview) result.routine.name else ""
                    )
                }
                ImportMode.TEXT -> importer.importFromText(state.textInput) { result ->
                    _uiState.value = _uiState.value.copy(
                        importState = result,
                        editedRoutineName = if (result is ImportState.Preview) result.routine.name else ""
                    )
                }
            }
        }
    }

    fun saveImportedRoutine(routine: ImportedRoutine) {
        viewModelScope.launch(Dispatchers.IO) {
            val name = _uiState.value.editedRoutineName.ifBlank { routine.name }
            val routineId = routineDao.insert(
                WorkoutRoutine(userId = userId, name = name)
            ).toInt()

            val allExercises = exerciseDao.getAllExercises().first()

            routine.exercises.forEachIndexed { index, imported ->
                // Find best match from existing exercises
                val match = allExercises.firstOrNull {
                    it.name.equals(imported.name, ignoreCase = true)
                }

                val exerciseId = match?.id ?: exerciseDao.insert(
                    Exercise(
                        name = imported.name,
                        muscleGroup = imported.muscleGroup.ifBlank { "Other" },
                        equipment = imported.equipment.ifBlank { "Bodyweight" },
                        isCustom = true
                    )
                ).toInt()

                routineExerciseDao.insert(
                    RoutineExercise(
                        routineId = routineId,
                        exerciseId = exerciseId,
                        orderIndex = index,
                        defaultSets = imported.sets,
                        defaultReps = imported.reps
                    )
                )
            }

            _uiState.value = _uiState.value.copy(importState = ImportState.Success)
        }
    }

    fun resetState() {
        _uiState.value = ImportUiState(hasApiKey = apiKeyManager.hasApiKey())
    }

    class Factory(
        private val application: Application,
        private val userId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ImportViewModel(application, userId) as T
        }
    }
}
