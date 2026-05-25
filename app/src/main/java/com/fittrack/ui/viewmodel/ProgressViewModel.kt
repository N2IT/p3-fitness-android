package com.fittrack.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fittrack.FitTrackApplication
import com.fittrack.data.entity.BodyWeight
import com.fittrack.data.entity.Exercise
import com.fittrack.data.entity.PersonalRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProgressDataPoint(
    val date: Long,
    val value: Double
)

data class ProgressUiState(
    val exercises: List<Exercise> = emptyList(),
    val selectedExercise: Exercise? = null,
    val chartMetric: ChartMetric = ChartMetric.MAX_WEIGHT,
    val timeRange: TimeRange = TimeRange.ALL,
    val dataPoints: List<ProgressDataPoint> = emptyList(),
    val prHistory: List<PersonalRecord> = emptyList(),
    val isLoading: Boolean = true
)

data class BodyWeightUiState(
    val weights: List<BodyWeight> = emptyList(),
    val weightInput: String = "",
    val isLoading: Boolean = true
)

enum class ChartMetric(val label: String) {
    MAX_WEIGHT("Max Weight"),
    TOTAL_VOLUME("Total Volume"),
    EST_1RM("Est. 1RM")
}

enum class TimeRange(val label: String, val days: Int) {
    ONE_MONTH("1M", 30),
    THREE_MONTHS("3M", 90),
    SIX_MONTHS("6M", 180),
    ONE_YEAR("1Y", 365),
    ALL("All", Int.MAX_VALUE)
}

class ProgressViewModel(application: Application, private val userId: Int) : AndroidViewModel(application) {
    private val db = (application as FitTrackApplication).database
    private val exerciseDao = db.exerciseDao()
    private val exerciseLogDao = db.exerciseLogDao()
    private val personalRecordDao = db.personalRecordDao()
    private val bodyWeightDao = db.bodyWeightDao()

    private val _progressState = MutableStateFlow(ProgressUiState())
    val progressState: StateFlow<ProgressUiState> = _progressState.asStateFlow()

    private val _bodyWeightState = MutableStateFlow(BodyWeightUiState())
    val bodyWeightState: StateFlow<BodyWeightUiState> = _bodyWeightState.asStateFlow()

    init {
        loadExercises()
        loadBodyWeights()
    }

    private fun loadExercises() {
        viewModelScope.launch {
            exerciseDao.getAllExercises().collect { exercises ->
                _progressState.value = _progressState.value.copy(
                    exercises = exercises,
                    isLoading = false
                )
            }
        }
    }

    fun selectExercise(exercise: Exercise) {
        _progressState.value = _progressState.value.copy(selectedExercise = exercise)
        loadProgressData()
    }

    fun setChartMetric(metric: ChartMetric) {
        _progressState.value = _progressState.value.copy(chartMetric = metric)
        loadProgressData()
    }

    fun setTimeRange(range: TimeRange) {
        _progressState.value = _progressState.value.copy(timeRange = range)
        loadProgressData()
    }

    private fun loadProgressData() {
        val exercise = _progressState.value.selectedExercise ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val logs = exerciseLogDao.getRecentLogsForExercise(exercise.id, userId, 500)
            val state = _progressState.value
            val cutoff = if (state.timeRange.days == Int.MAX_VALUE) 0L
            else System.currentTimeMillis() - state.timeRange.days * 86400000L

            // Group logs by session
            val sessionGroups = logs.filter { it.completedAt >= cutoff }
                .groupBy { it.workoutSessionId }

            val points = sessionGroups.map { (_, sessionLogs) ->
                val date = sessionLogs.minOf { it.completedAt }
                val value = when (state.chartMetric) {
                    ChartMetric.MAX_WEIGHT -> sessionLogs.maxOf { it.weight }
                    ChartMetric.TOTAL_VOLUME -> sessionLogs.sumOf { it.weight * it.reps }
                    ChartMetric.EST_1RM -> sessionLogs.maxOf { log ->
                        if (log.reps == 1) log.weight else log.weight * (1 + log.reps / 30.0)
                    }
                }
                ProgressDataPoint(date, value)
            }.sortedBy { it.date }

            val prs = personalRecordDao.getRecordsForExercise(exercise.id, userId).first()
                .filter { it.achievedAt >= cutoff }

            _progressState.value = state.copy(
                dataPoints = points,
                prHistory = prs
            )
        }
    }

    private fun loadBodyWeights() {
        viewModelScope.launch {
            bodyWeightDao.getWeightsForUser(userId).collect { weights ->
                _bodyWeightState.value = _bodyWeightState.value.copy(
                    weights = weights,
                    isLoading = false
                )
            }
        }
    }

    fun updateWeightInput(input: String) {
        _bodyWeightState.value = _bodyWeightState.value.copy(weightInput = input)
    }

    fun logBodyWeight() {
        val weight = _bodyWeightState.value.weightInput.toDoubleOrNull() ?: return
        viewModelScope.launch(Dispatchers.IO) {
            bodyWeightDao.insert(BodyWeight(userId = userId, weight = weight))
            _bodyWeightState.value = _bodyWeightState.value.copy(weightInput = "")
        }
    }

    class Factory(private val application: Application, private val userId: Int) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProgressViewModel(application, userId) as T
        }
    }
}
