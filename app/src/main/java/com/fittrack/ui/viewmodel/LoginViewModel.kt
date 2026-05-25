package com.fittrack.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fittrack.FitTrackApplication
import com.fittrack.data.entity.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "",
    val unitPreference: String = "lbs",
    val isLoading: Boolean = false,
    val isDbReady: Boolean = false,
    val loggedInUserId: Int? = null,
    val error: String? = null
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as FitTrackApplication).database
    private val userDao = db.userDao()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        // Trigger database creation (seeds exercises)
        viewModelScope.launch(Dispatchers.IO) {
            db.exerciseDao().getExerciseCount()
            _uiState.value = _uiState.value.copy(isDbReady = true)
        }
    }

    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(username = username, error = null)
    }

    fun toggleUnit() {
        val newUnit = if (_uiState.value.unitPreference == "lbs") "kg" else "lbs"
        _uiState.value = _uiState.value.copy(unitPreference = newUnit)
    }

    fun login() {
        val username = _uiState.value.username.trim()
        if (username.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please enter a username")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch(Dispatchers.IO) {
            val existingUser = userDao.getUserByUsername(username)
            val userId = if (existingUser != null) {
                existingUser.id
            } else {
                userDao.insert(
                    User(username = username, unitPreference = _uiState.value.unitPreference)
                ).toInt()
            }
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                loggedInUserId = userId
            )
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(application) as T
        }
    }
}
