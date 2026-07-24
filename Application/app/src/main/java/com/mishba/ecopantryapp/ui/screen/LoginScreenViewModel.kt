package com.mishba.ecopantryapp.ui.screen

import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mishba.ecopantryapp.data.AppDataStore
import com.mishba.ecopantryapp.data.AppDatabase
import com.mishba.ecopantryapp.data.AuthRepository
import com.mishba.ecopantryapp.data.Repository
import com.mishba.ecopantryapp.data.UserTable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String = "",
    val passwordError: String = "",
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val errorMessage: String = "",
    val resetEmailSent: Boolean = false
)

class LoginScreenViewModel(context: Context) : ViewModel() {

    private val authRepository = AuthRepository()
    private val repository = Repository(AppDatabase.getInstance(context))
    private val appDataStore = AppDataStore(context)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, emailError = "", errorMessage = "")
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, passwordError = "", errorMessage = "")
    }

    fun login() {
        val state = _uiState.value
        var valid = true
        var emailErr = ""
        var passErr = ""

        if (state.email.isBlank()) { emailErr = "Email is required"; valid = false }
        else if (!Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            emailErr = "Invalid email address"; valid = false
        }
        if (state.password.isBlank()) { passErr = "Password is required"; valid = false }

        if (!valid) {
            _uiState.value = state.copy(emailError = emailErr, passwordError = passErr)
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = "")
            val result = authRepository.login(state.email.trim(), state.password)
            result.onSuccess { firebaseUser ->
                appDataStore.saveLoggedInUserId(firebaseUser.uid)
                // Ensure a local cache row exists so offline screens have something to read.
                val existing = repository.getUserById(firebaseUser.uid)
                if (existing == null) {
                    repository.cacheUser(
                        UserTable(
                            id = firebaseUser.uid,
                            fullName = firebaseUser.displayName ?: state.email.substringBefore("@"),
                            email = firebaseUser.email ?: state.email,
                            isVerified = firebaseUser.isEmailVerified
                        )
                    )
                }
                Log.d("LoginViewModel", "login() success uid=${firebaseUser.uid}")
                _uiState.value = _uiState.value.copy(isLoading = false, loginSuccess = true)
            }.onFailure { e ->
                Log.w("LoginViewModel", "login() failed: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Invalid email or password."
                )
            }
        }
    }
//
    fun sendPasswordReset() {
        val email = _uiState.value.email
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = _uiState.value.copy(emailError = "Enter a valid email first")
            return
        }
        viewModelScope.launch {
            authRepository.sendPasswordReset(email.trim())
            _uiState.value = _uiState.value.copy(resetEmailSent = true)
        }
    }
}

