package com.fittrack.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fittrack.FitTrackApplication
import com.fittrack.data.api.ApiKeyManager
import com.fittrack.data.api.LlmProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class SettingsUiState(
    val username: String = "",
    val unitPreference: String = "lbs",
    val selectedProvider: LlmProvider = LlmProvider.ANTHROPIC,
    val apiKey: String = "",
    val hasApiKey: Boolean = false,
    val apiKeyTestResult: String? = null,
    val configuredProviders: List<LlmProvider> = emptyList(),
    val totalWorkouts: Int = 0,
    val exportStatus: String? = null
)

class SettingsViewModel(
    application: Application,
    private val userId: Int
) : AndroidViewModel(application) {
    private val db = (application as FitTrackApplication).database
    private val userDao = db.userDao()
    private val exerciseLogDao = db.exerciseLogDao()
    private val apiKeyManager = ApiKeyManager(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = userDao.getUserById(userId)
            val totalWorkouts = exerciseLogDao.getTotalWorkoutCount(userId)
            val provider = apiKeyManager.getSelectedProvider()
            _uiState.value = SettingsUiState(
                username = user?.username ?: "",
                unitPreference = user?.unitPreference ?: "lbs",
                selectedProvider = provider,
                hasApiKey = apiKeyManager.hasApiKey(provider),
                apiKey = if (apiKeyManager.hasApiKey(provider)) "••••••••" else "",
                configuredProviders = apiKeyManager.getConfiguredProviders(),
                totalWorkouts = totalWorkouts
            )
        }
    }

    fun toggleUnit() {
        val newUnit = if (_uiState.value.unitPreference == "lbs") "kg" else "lbs"
        _uiState.value = _uiState.value.copy(unitPreference = newUnit)
        viewModelScope.launch(Dispatchers.IO) {
            val user = userDao.getUserById(userId)
            user?.let { userDao.update(it.copy(unitPreference = newUnit)) }
        }
    }

    fun selectProvider(provider: LlmProvider) {
        apiKeyManager.setSelectedProvider(provider)
        _uiState.value = _uiState.value.copy(
            selectedProvider = provider,
            hasApiKey = apiKeyManager.hasApiKey(provider),
            apiKey = if (apiKeyManager.hasApiKey(provider)) "••••••••" else "",
            apiKeyTestResult = null
        )
    }

    fun updateApiKey(key: String) {
        _uiState.value = _uiState.value.copy(apiKey = key, apiKeyTestResult = null)
    }

    fun saveApiKey() {
        val key = _uiState.value.apiKey
        val provider = _uiState.value.selectedProvider
        if (key.isNotBlank() && key != "••••••••") {
            apiKeyManager.setApiKey(key, provider)
            _uiState.value = _uiState.value.copy(
                hasApiKey = true,
                apiKey = "••••••••",
                apiKeyTestResult = "${provider.displayName} key saved",
                configuredProviders = apiKeyManager.getConfiguredProviders()
            )
        }
    }

    fun clearApiKey() {
        val provider = _uiState.value.selectedProvider
        apiKeyManager.clearApiKey(provider)
        _uiState.value = _uiState.value.copy(
            hasApiKey = false,
            apiKey = "",
            apiKeyTestResult = "${provider.displayName} key removed",
            configuredProviders = apiKeyManager.getConfiguredProviders()
        )
    }

    fun exportData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val logs = exerciseLogDao.getRecentLogsForExercise(0, userId, Int.MAX_VALUE)
                val allLogs = mutableListOf<com.fittrack.data.entity.ExerciseLog>()
                val db2 = db
                val sessions = exerciseLogDao.getWorkoutSessions(userId)
                    .let { flow ->
                        var result = emptyList<String>()
                        val job = viewModelScope.launch {
                            flow.collect { result = it; return@collect }
                        }
                        kotlinx.coroutines.delay(200)
                        job.cancel()
                        result
                    }
                sessions.forEach { sessionId ->
                    allLogs.addAll(exerciseLogDao.getLogsForSessionOnce(sessionId))
                }

                val csv = buildString {
                    appendLine("Date,Exercise,Set,Weight,Reps,SessionId")
                    allLogs.forEach { log ->
                        val exercise = db.exerciseDao().getExerciseById(log.exerciseId)
                        appendLine("${log.completedAt},${exercise?.name ?: "Unknown"},${log.setNumber},${log.weight},${log.reps},${log.workoutSessionId}")
                    }
                }

                val app = getApplication<FitTrackApplication>()
                val file = File(app.getExternalFilesDir(null), "fittrack_export.csv")
                file.writeText(csv)

                _uiState.value = _uiState.value.copy(exportStatus = "Exported to ${file.absolutePath}")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(exportStatus = "Export failed: ${e.message}")
            }
        }
    }

    class Factory(private val application: Application, private val userId: Int) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(application, userId) as T
        }
    }
}
