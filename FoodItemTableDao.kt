package com.ecopantry.app.ui.screen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecopantry.app.data.AppDataStore
import com.ecopantry.app.data.AppDatabase
import com.ecopantry.app.data.NotificationTable
import com.ecopantry.app.data.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class NotificationUiState(
    val notifications: List<NotificationTable> = emptyList(),
    val userId: String = ""
)

/** Backs the Notifications screen (FR11, FR12, US 4.1-4.3). */
class NotificationScreenViewModel(context: Context) : ViewModel() {

    private val repository = Repository(AppDatabase.getInstance(context))
    private val appDataStore = AppDataStore(context)

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appDataStore.loggedInUserIdFlow().collectLatest { userId ->
                if (userId == null) return@collectLatest
                _uiState.value = _uiState.value.copy(userId = userId)
                repository.getAllNotifications(userId).collectLatest { list ->
                    _uiState.value = _uiState.value.copy(notifications = list)
                }
            }
        }
    }

    fun markAsRead(id: String) = viewModelScope.launch { repository.markNotificationAsRead(id) }

    fun markAllAsRead() = viewModelScope.launch {
        if (_uiState.value.userId.isNotBlank()) repository.markAllNotificationsAsRead(_uiState.value.userId)
    }

    fun clearAll() = viewModelScope.launch {
        if (_uiState.value.userId.isNotBlank()) repository.clearAllNotifications(_uiState.value.userId)
    }
}
