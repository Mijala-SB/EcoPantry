package com.ecopantry.app.ui.screen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecopantry.app.data.AppDataStore
import com.ecopantry.app.data.AppDatabase
import com.ecopantry.app.data.FoodItemTable
import com.ecopantry.app.data.Repository
import com.ecopantry.app.data.UserTable
import com.ecopantry.app.model.FoodStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class HomeUiState(
    val userName: String = "",
    val activeItemCount: Int = 0,
    val expiringSoon: List<FoodItemTable> = emptyList(),
    val unreadNotifications: Int = 0,
    val isLoading: Boolean = true
)

class HomeScreenViewModel(context: Context) : ViewModel() {

    private val repository = Repository(AppDatabase.getInstance(context))
    private val appDataStore = AppDataStore(context)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appDataStore.loggedInUserIdFlow().collectLatest { userId ->
                if (userId == null) return@collectLatest
                val user = repository.getUserById(userId)
                loadDashboard(userId, user)
            }
        }
    }

    private fun loadDashboard(userId: String, user: UserTable?) {
        viewModelScope.launch {
            repository.getActiveItemCount(userId).collectLatest { count ->
                _uiState.value = _uiState.value.copy(
                    userName = user?.fullName ?: "there",
                    activeItemCount = count,
                    isLoading = false
                )
            }
        }
        viewModelScope.launch {
            repository.getFoodItemsByStatus(userId, FoodStatus.ACTIVE).collectLatest { items ->
                val now = System.currentTimeMillis()
                val soon = now + TimeUnit.DAYS.toMillis(3)
                val expiring = items.filter { it.expiryDate != null && it.expiryDate in now..soon }
                    .sortedBy { it.expiryDate }
                _uiState.value = _uiState.value.copy(expiringSoon = expiring)
            }
        }
        viewModelScope.launch {
            repository.getUnreadNotificationCount(userId).collectLatest { count ->
                _uiState.value = _uiState.value.copy(unreadNotifications = count)
            }
        }
    }
}
