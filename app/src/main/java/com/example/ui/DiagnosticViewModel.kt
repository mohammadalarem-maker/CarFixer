package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DiagnosticRepository
import com.example.data.DiagnosticSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DiagnosticViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = DiagnosticRepository(db.diagnosticDao())

    val allSessions: StateFlow<List<DiagnosticSession>> = repository.allSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentSessionId = MutableStateFlow<Int?>(null)
    val currentSessionId: StateFlow<Int?> = _currentSessionId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentSession: StateFlow<DiagnosticSession?> = _currentSessionId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                repository.getSessionById(id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _isAILoading = MutableStateFlow(false)
    val isAILoading: StateFlow<Boolean> = _isAILoading.asStateFlow()

    fun selectSession(sessionId: Int?) {
        _currentSessionId.value = sessionId
    }

    fun startNewSession(carModel: String, symptom: String, category: String) {
        viewModelScope.launch {
            _isAILoading.value = true
            val id = repository.createNewSession(carModel, symptom, category)
            _currentSessionId.value = id.toInt()
            _isAILoading.value = false
        }
    }

    fun sendMessage(userMessage: String) {
        val sessionId = _currentSessionId.value ?: return
        viewModelScope.launch {
            _isAILoading.value = true
            repository.addMessageToSession(sessionId, userMessage)
            _isAILoading.value = false
        }
    }

    fun deleteSession(sessionId: Int) {
        viewModelScope.launch {
            if (_currentSessionId.value == sessionId) {
                _currentSessionId.value = null
            }
            repository.deleteSession(sessionId)
        }
    }
}
