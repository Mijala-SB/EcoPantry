package com.mishba.ecopantryapp.ui.screen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mishba.ecopantryapp.data.AppDataStore
import com.mishba.ecopantryapp.data.AppDatabase
import com.mishba.ecopantryapp.data.AuthRepository
import com.mishba.ecopantryapp.data.Repository
import com.mishba.ecopantryapp.data.UserTable
import com.mishba.ecopantryapp.model.LightOrDarkMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: UserTable? = null,
    val darkMode: LightOrDarkMode = LightOrDarkMode.System,
    val loggedOut: Boolean = false
)

/** Backs the Profile screen: view/update profile, theme, 2FA toggle, logout (FR03, FR04, US 1.3). */
class ProfileScreenViewModel(context: Context) : ViewModel() {

    private val repository = Repository(AppDatabase.getInstance(context))
    private val authRepository = AuthRepository()
    private val appDataStore = AppDataStore(context)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appDataStore.loggedInUserIdFlow().collectLatest { userId ->
                if (userId == null) return@collectLatest
                repository.observeUser(userId).collectLatest { user ->
                    _uiState.value = _uiState.value.copy(user = user)
                }
            }
        }
        viewModelScope.launch {
            appDataStore.lightOrDarkModeFlow().collectLatest { mode ->
                _uiState.value = _uiState.value.copy(darkMode = mode ?: LightOrDarkMode.System)
            }
        }
    }

    fun setDarkMode(mode: LightOrDarkMode) = viewModelScope.launch { appDataStore.saveLightOrDarkMode(mode) }

    fun setTwoFactorEnabled(enabled: Boolean) {
        val user = _uiState.value.user ?: return
        viewModelScope.launch {
            repository.updateUser(user.copy(twoFactorEnabled = enabled))
            authRepository.setTwoFactorEnabled(user.id, enabled)
        }
    }

    fun updateHouseholdSize(size: Int) {
        val user = _uiState.value.user ?: return
        viewModelScope.launch { repository.updateUser(user.copy(householdSize = size)) }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
            appDataStore.saveLoggedInUserId(null)
            _uiState.value = _uiState.value.copy(loggedOut = true)
        }
    }
}
