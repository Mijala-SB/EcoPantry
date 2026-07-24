package com.mishba.ecopantryapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mishba.ecopantryapp.model.FoodStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailScreen(
    itemId: String,
    navigateBack: () -> Unit,
    navigateToEdit: (String) -> Unit,
    navigateToAddDonation: (String) -> Unit
) {
    val context = LocalContext.current
    val vm: FoodDetailScreenViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return FoodDetailScreenViewModel(context, itemId) as T
            }
        }
    )
    val state by vm.uiState.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }

    LaunchedEffect(state.deleted) { if (state.deleted) navigateBack() }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete item?") },
            text = { Text("This will permanently remove it from your inventory.") },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; vm.deleteItem() }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Item Details") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = { navigateToEdit(itemId) }) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
                    IconButton(onClick = { showDeleteConfirm = true }) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
                }
            )
        }
    ) { padding ->
        val item = state.item
        if (state.isLoading || item == null) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                if (state.isLoading) CircularProgressIndicator() else Text("Item not found")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(item.itemName, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))

            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DetailRow("Quantity", item.quantity)
                    DetailRow("Category", item.category.label)
                    DetailRow("Storage", item.storageLocation.label)
                    DetailRow("Expiry Date", item.expiryDate?.let { dateFormat.format(Date(it)) } ?: "Not set")
                    DetailRow("Status", item.status.name)
                    if (item.remarks.isNotBlank()) DetailRow("Remarks", item.remarks)
                }
            }

            if (item.status == FoodStatus.ACTIVE) {
                Button(
                    onClick = vm::markAsUsed,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Mark as Used")
                }

                OutlinedButton(
                    onClick = { navigateToAddDonation(itemId) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.VolunteerActivism, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Donate this Item")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

