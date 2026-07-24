package com.mishba.ecopantryapp.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mishba.ecopantryapp.R
import com.mishba.ecopantryapp.ui.widget.InputFieldError

//.
@Composable
fun LoginScreen(
    navigateToRegister: () -> Unit,
    navigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val vm: LoginScreenViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return LoginScreenViewModel(context) as T
            }
        }
    )
    val state by vm.uiState.collectAsState()
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(state.loginSuccess) {
        if (state.loginSuccess) navigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(androidx.compose.ui.graphics.Color.White)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ecopantry_logo),
                    contentDescription = "EcoPantry",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = androidx.compose.ui.graphics.Color.White
            )
            Text(
                text = "Sign in to keep tracking your pantry",
                style = MaterialTheme.typography.bodyMedium,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f)
            )

            Spacer(Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = vm::onEmailChange,
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, null) },
                        isError = state.emailError.isNotBlank(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("emailField")
                    )
                    if (state.emailError.isNotBlank()) InputFieldError(state.emailError)

                    OutlinedTextField(
                        value = state.password,
                        onValueChange = vm::onPasswordChange,
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password"
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = state.passwordError.isNotBlank(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("passwordField")
                    )
                    if (state.passwordError.isNotBlank()) InputFieldError(state.passwordError)

                    if (state.errorMessage.isNotBlank()) {
                        Text(
                            text = state.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (state.resetEmailSent) {
                        Text(
                            text = "Password reset link sent — check your inbox.",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    TextButton(onClick = vm::sendPasswordReset, modifier = Modifier.align(Alignment.End)) {
                        Text("Forgot password?")
                    }

                    Button(
                        onClick = vm::login,
                        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("loginButton"),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Login", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Don't have an account?",
                    color = androidx.compose.ui.graphics.Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(onClick = navigateToRegister) {
                    Text(
                        "Register",
                        color = androidx.compose.ui.graphics.Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
