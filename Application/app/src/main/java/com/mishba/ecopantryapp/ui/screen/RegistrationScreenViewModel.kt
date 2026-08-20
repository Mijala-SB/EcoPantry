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

enum class RegistrationStep { FORM, OTP }

data class RegistrationUiState(
    val step: RegistrationStep = RegistrationStep.FORM,
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val householdSize: String = "1",
    val nameError: String = "",
    val emailError: String = "",
    val passwordError: String = "",
    val confirmPasswordError: String = "",
    val otpCode: String = "",
    val otpError: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val infoMessage: String = "",
    val registrationComplete: Boolean = false,
    val pendingUid: String = ""
)

/** Backs the Registration screen: form validation (FR01), then OTP verification (FR02, US 1.1-1.2). */
class RegistrationScreenViewModel(context: Context) : ViewModel() {

    private val authRepository = AuthRepository()
    private val repository = Repository(AppDatabase.getInstance(context))
    private val appDataStore = AppDataStore(context)

    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState = _uiState.asStateFlow()

    fun onNameChange(v: String) { _uiState.value = _uiState.value.copy(fullName = v, nameError = "") }
    fun onEmailChange(v: String) { _uiState.value = _uiState.value.copy(email = v, emailError = "") }
    fun onPasswordChange(v: String) { _uiState.value = _uiState.value.copy(password = v, passwordError = "") }
    fun onConfirmPasswordChange(v: String) { _uiState.value = _uiState.value.copy(confirmPassword = v, confirmPasswordError = "") }
    fun onHouseholdSizeChange(v: String) { _uiState.value = _uiState.value.copy(householdSize = v.filter { it.isDigit() }) }
    fun onOtpChange(v: String) { _uiState.value = _uiState.value.copy(otpCode = v.filter { it.isDigit() }.take(6), otpError = "") }

    /** Step 1 → creates the Firebase Auth account then sends the OTP (FR01, FR02). */
    fun submitRegistration() {
        val s = _uiState.value
        var valid = true
        var nameErr = ""; var emailErr = ""; var passErr = ""; var confirmErr = ""

        if (s.fullName.isBlank()) { nameErr = "Full name is required"; valid = false }
        if (s.email.isBlank()) { emailErr = "Email is required"; valid = false }
        else if (!Patterns.EMAIL_ADDRESS.matcher(s.email).matches()) { emailErr = "Invalid email address"; valid = false }
        if (s.password.length < 6) { passErr = "Password must be at least 6 characters"; valid = false }
        if (s.confirmPassword != s.password) { confirmErr = "Passwords do not match"; valid = false }

        if (!valid) {
            _uiState.value = s.copy(
                nameError = nameErr, emailError = emailErr,
                passwordError = passErr, confirmPasswordError = confirmErr
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = s.copy(isLoading = true, errorMessage = "")
            val registerResult = authRepository.register(s.email.trim(), s.password)
            registerResult.onSuccess { firebaseUser ->
                authRepository.saveUserProfile(
                    firebaseUser.uid, s.fullName.trim(), s.email.trim(),
                    s.householdSize.toIntOrNull() ?: 1
                )
                repository.cacheUser(
                    UserTable(
                        id = firebaseUser.uid,
                        fullName = s.fullName.trim(),
                        email = s.email.trim(),
                        householdSize = s.householdSize.toIntOrNull() ?: 1
                    )
                )
                val otpResult = authRepository.generateAndSendOtp(firebaseUser.uid, s.email.trim())
                otpResult.onSuccess {
                    Log.d("RegistrationViewModel", "OTP sent to ${s.email}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        step = RegistrationStep.OTP,
                        pendingUid = firebaseUser.uid,
                        infoMessage = "We've sent a 6-digit verification code to ${s.email}."
                    )
                }.onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message ?: "Could not send verification code.")
                }
            }.onFailure { e ->
                Log.w("RegistrationViewModel", "register() failed: ${e.message}")
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message ?: "Registration failed.")
            }
        }
    }

    /** Step 2 → validates the entered OTP and completes registration (US 1.2). */
    fun verifyOtp(onVerified: () -> Unit) {
        val s = _uiState.value
        if (s.otpCode.length != 6) {
            _uiState.value = s.copy(otpError = "Enter the 6-digit code sent to your email")
            return
        }
        viewModelScope.launch {
            _uiState.value = s.copy(isLoading = true, otpError = "")
            val result = authRepository.verifyOtp(s.pendingUid, s.otpCode)
            result.onSuccess { isValid ->
                if (isValid) {
                    appDataStore.saveLoggedInUserId(s.pendingUid)
                    val cached = repository.getUserById(s.pendingUid)
                    if (cached != null) repository.updateUser(cached.copy(isVerified = true))
                    _uiState.value = _uiState.value.copy(isLoading = false, registrationComplete = true)
                    onVerified()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        otpError = "Invalid or expired code. Please try again or resend."
                    )
                }
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, otpError = e.message ?: "Verification failed.")
            }
        }
    }

    fun resendOtp() {
        val s = _uiState.value
        if (s.pendingUid.isBlank()) return
        viewModelScope.launch {
            _uiState.value = s.copy(isLoading = true)
            authRepository.generateAndSendOtp(s.pendingUid, s.email.trim())
            _uiState.value = _uiState.value.copy(isLoading = false, infoMessage = "A new code has been sent to ${s.email}.")
        }
    }
}
