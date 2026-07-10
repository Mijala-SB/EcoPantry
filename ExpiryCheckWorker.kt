package com.ecopantry.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ecopantry.app.model.FoodCategory
import com.ecopantry.app.model.StorageArea
import com.ecopantry.app.ui.widget.InputFieldError
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFoodScreen(
    itemId: String?,
    navigateBack: () -> Unit
) {
    val context = LocalContext.current
    val vm: AddEditFoodScreenViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AddEditFoodScreenViewModel(context, itemId) as T
            }
        }
    )
    val state by vm.uiState.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showStorageMenu by remember { mutableStateOf(false) }

    LaunchedEffect(state.saveComplete) {
        if (state.saveComplete) navigateBack()
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.expiryDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    vm.onExpiryChange(datePickerState.selectedDateMillis)
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (itemId == null) "Add Food Item" else "Edit Food Item") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = state.itemName,
                onValueChange = vm::onNameChange,
                label = { Text("Item Name") },
                isError = state.nameError.isNotBlank(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (state.nameError.isNotBlank()) InputFieldError(state.nameError)

            OutlinedTextField(
                value = state.quantity,
                onValueChange = vm::onQuantityChange,
                label = { Text("Quantity (e.g. 2 pcs, 500g)") },
                isError = state.quantityError.isNotBlank(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (state.quantityError.isNotBlank()) InputFieldError(state.quantityError)

            OutlinedTextField(
                value = state.expiryDateMillis?.let { dateFormat.format(Date(it)) } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Expiry Date") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Pick date")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(expanded = showCategoryMenu, onExpandedChange = { showCategoryMenu = it }) {
                OutlinedTextField(
                    value = state.category.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = showCategoryMenu, onDismissRequest = { showCategoryMenu = false }) {
                    FoodCategory.entries.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.label) },
                            onClick = { vm.onCategoryChange(category); showCategoryMenu = false }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(expanded = showStorageMenu, onExpandedChange = { showStorageMenu = it }) {
                OutlinedTextField(
                    value = state.storageArea.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Storage Location") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showStorageMenu) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = showStorageMenu, onDismissRequest = { showStorageMenu = false }) {
                    StorageArea.entries.forEach { area ->
                        DropdownMenuItem(
                            text = { Text(area.label) },
                            onClick = { vm.onStorageChange(area); showStorageMenu = false }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.remarks,
                onValueChange = vm::onRemarksChange,
                label = { Text("Remarks (optional)") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Visible to other users", fontWeight = FontWeight.Medium)
                    Text(
                        "Off by default (FR04) — only turn on when you're ready to donate.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = state.isPublic, onCheckedChange = vm::onPublicToggle)
            }

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = vm::save,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !state.isSaving
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(if (itemId == null) "Add Item" else "Save Changes", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
