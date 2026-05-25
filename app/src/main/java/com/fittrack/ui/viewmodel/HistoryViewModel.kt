package com.fittrack.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fittrack.FitTrackApplication
import com.fittrack.data.entity.ExerciseLog
import com.fittrack.data.entity.PersonalRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class WorkoutSession(
    val sessionId: String,
    val routineName: String,
    val date: Long,
    val durationSeconds: Long,
    val totalVolume: Double,
    val setCount: Int,
    val exerciseCount: Int
)

data class WorkoutDetailInfo(
    val session: WorkoutSession,
    val exerciseLogs: Map<String, List<ExerciseLog>>, // exerciseName -> logs
    val sessionPrs: List<PersonalRecord>
)

data class HistoryUiState(
    val sessions: List<WorkoutSession> = emptyList(),
    val isLoading: Boolean = true
)

data class WorkoutDetailUiState(
    val detail: WorkoutDetailInfo? = null,
    val isLoading: Boolean = true
)

class HistoryViewModel(application: Application, private val userId: Int) : AndroidViewModel(application) {
    private val db = (application as FitTrackApplication).database
    private val exerciseLogDao = db.exerciseLogDao()
    private val routineDao = db.workoutRoutineDao()
    private val exerciseDao = db.exerciseDao()
    private val personalRecordDao = db.personalRecordDao()

    private val _historyState = MutableStateFlow(HistoryUiState())
    val historyState: StateFlow<HistoryUiState> = _historyState.asStateFlow()

    private val _detailState = MutableStateFlow(WorkoutDetailUiState())
    val detailState: StateFlow<WorkoutDetailUiState> = _detailState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            exerciseLogDao.getWorkoutSessions(userId).collect { sessionIds ->
                val sessions = sessionIds.mapNotNull { sessionId ->
                    buildSession(sessionId)
                }
                _historyState.value = HistoryUiState(sessions = sessions, isLoading = false)
            }
        }
    }

    private suspend fun buildSession(sessionId: String): WorkoutSession? {
        val logs = exerciseLogDao.getLogsForSessionOnce(sessionId)
        if (logs.isEmpty()) return null

        val firstLog = logs.minByOrNull { it.completedAt } ?: return null
        val lastLog = logs.maxByOrNull { it.completedAt } ?: return null
        val routine = routineDao.getRoutineById(firstLog.routineId)

        return WorkoutSession(
            sessionId = sessionId,
            routineName = routine?.name ?: "Workout",
            date = firstLog.completedAt,
            durationSeconds = (lastLog.completedAt - firstLog.completedAt) / 1000,
            totalVolume = logs.sumOf { it.weight * it.reps },
            setCount = logs.size,
            exerciseCount = logs.map { it.exerciseId }.distinct().size
        )
    }

    fun loadWorkoutDetail(sessionId: String) {
        _detailState.value = WorkoutDetailUiState(isLoading = true)
        viewModelScope.launch(Dispatchers.IO) {
            val session = buildSession(sessionId) ?: return@launch
            val logs = exerciseLogDao.getLogsForSessionOnce(sessionId)
            val prs = personalRecordDao.getRecordsForSession(sessionId)

            val grouped = logs.groupBy { it.exerciseId }.mapKeys { (exerciseId, _) ->
                exerciseDao.getExerciseById(exerciseId)?.name ?: "Unknown"
            }

            _detailState.value = WorkoutDetailUiState(
                detail = WorkoutDetailInfo(session, grouped, prs),
                isLoading = false
            )
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            exerciseLogDao.deleteSession(sessionId)
        }
    }

    class Factory(private val application: Application, private val userId: Int) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(application, userId) as T
        }
    }
}
