package com.ecopantry.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ecopantry.app.ui.widget.InputFieldError

@Composable
fun AddDonationScreen(
    itemId: String,
    navigateBack: () -> Unit,
    navigateToInventory: () -> Unit
) {
    val context = LocalContext.current
    val vm: AddDonationScreenViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AddDonationScreenViewModel(context, itemId) as T
            }
        }
    )
    val state by vm.uiState.collectAsState()

    LaunchedEffect(state.saveComplete) { if (state.saveComplete) navigateToInventory() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Donate Item") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(state.itemName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Quantity: ${state.quantity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                "Provide pickup details so nearby users can arrange collection.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = state.pickupAddress,
                onValueChange = vm::onAddressChange,
                label = { Text("Pickup Address") },
                isError = state.addressError.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
            if (state.addressError.isNotBlank()) InputFieldError(state.addressError)

            OutlinedTextField(
                value = state.pickupCity,
                onValueChange = vm::onCityChange,
                label = { Text("City") },
                isError = state.cityError.isNotBlank(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (state.cityError.isNotBlank()) InputFieldError(state.cityError)

            OutlinedTextField(
                value = state.availability,
                onValueChange = vm::onAvailabilityChange,
                label = { Text("Availability (e.g. Weekday evenings)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.remarks,
                onValueChange = vm::onRemarksChange,
                label = { Text("Remarks (optional)") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            if (state.errorMessage.isNotBlank()) {
                Text(state.errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = vm::submit,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !state.isSaving
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Publish Donation", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
