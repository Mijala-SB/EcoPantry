package com.mishba.ecopantryapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mishba.ecopantryapp.ui.widget.InputFieldError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    navigateBack: () -> Unit,
    navigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val vm: RegistrationScreenViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return RegistrationScreenViewModel(context) as T
            }
        }
    )
    val state by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.step == RegistrationStep.FORM) "Create Account" else "Verify Email") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (state.step == RegistrationStep.FORM) {
                RegistrationForm(state = state, vm = vm)
            } else {
                OtpVerificationForm(state = state, vm = vm, navigateToHome = navigateToHome)
            }
        }
    }
}

@Composable
private fun RegistrationForm(state: RegistrationUiState, vm: RegistrationScreenViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "Join EcoPantry and start reducing food waste at home.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = state.fullName,
            onValueChange = vm::onNameChange,
            label = { Text("Full Name") },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            isError = state.nameError.isNotBlank(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (state.nameError.isNotBlank()) InputFieldError(state.nameError)

        OutlinedTextField(
            value = state.email,
            onValueChange = vm::onEmailChange,
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            isError = state.emailError.isNotBlank(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (state.emailError.isNotBlank()) InputFieldError(state.emailError)

        OutlinedTextField(
            value = state.householdSize,
            onValueChange = vm::onHouseholdSizeChange,
            label = { Text("Household Size (optional)") },
            leadingIcon = { Icon(Icons.Default.Group, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.password,
            onValueChange = vm::onPasswordChange,
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            visualTransformation = PasswordVisualTransformation(),
            isError = state.passwordError.isNotBlank(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (state.passwordError.isNotBlank()) InputFieldError(state.passwordError)

        OutlinedTextField(
            value = state.confirmPassword,
            onValueChange = vm::onConfirmPasswordChange,
            label = { Text("Confirm Password") },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            visualTransformation = PasswordVisualTransformation(),
            isError = state.confirmPasswordError.isNotBlank(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (state.confirmPasswordError.isNotBlank()) InputFieldError(state.confirmPasswordError)

        if (state.errorMessage.isNotBlank()) {
            Text(state.errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(4.dp))

        Button(
            onClick = vm::submitRegistration,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Register", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun OtpVerificationForm(
    state: RegistrationUiState,
    vm: RegistrationScreenViewModel,
    navigateToHome: () -> Unit
) {
    LaunchedEffect(state.registrationComplete) {
        if (state.registrationComplete) navigateToHome()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            Icons.Default.MarkEmailRead,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            "Check your email",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        if (state.infoMessage.isNotBlank()) {
            Text(
                state.infoMessage,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        OutlinedTextField(
            value = state.otpCode,
            onValueChange = vm::onOtpChange,
            label = { Text("6-digit code") },
            isError = state.otpError.isNotBlank(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (state.otpError.isNotBlank()) InputFieldError(state.otpError)

        Button(
            onClick = { vm.verifyOtp(navigateToHome) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Verify & Continue", fontWeight = FontWeight.Bold)
            }
        }

        TextButton(onClick = vm::resendOtp, enabled = !state.isLoading) {
            Text("Resend code")
        }
    }
}
