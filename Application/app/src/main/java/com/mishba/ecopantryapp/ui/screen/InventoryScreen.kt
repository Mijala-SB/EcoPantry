package com.mishba.ecopantryapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mishba.ecopantryapp.model.DonationStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    navigateToAddFood: () -> Unit,
    navigateToFoodDetail: (String) -> Unit,
    navigateToDonationDetail: (String) -> Unit
) {
    val context = LocalContext.current
    val vm: InventoryScreenViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return InventoryScreenViewModel(context) as T
            }
        }
    )
    val state by vm.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Inventory") }) },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(onClick = navigateToAddFood) {
                    Icon(Icons.Default.Add, contentDescription = "Add food item")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Food List") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1; vm.refreshMyDonations() }, text = { Text("Donation List") })
            }

            if (selectedTab == 0) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = vm::onSearchChange,
                    placeholder = { Text("Search food items") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                )

                val items = vm.filteredItems()
                if (items.isEmpty()) {
                    EmptyState("No food items yet. Tap + to add your first item.")
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(items, key = { it.itemId }) { item ->
                            Card(
                                onClick = { navigateToFoodDetail(item.itemId) },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(item.itemName, fontWeight = FontWeight.Bold)
                                        Text(
                                            "${item.category.label} • ${item.quantity}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        item.expiryDate?.let { dateFormat.format(Date(it)) } ?: "No expiry",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                if (state.myDonations.isEmpty()) {
                    EmptyState("You haven't donated anything yet. Convert an inventory item into a donation from its detail screen.")
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.myDonations, key = { it.donationId }) { donation ->
                            Card(
                                onClick = { navigateToDonationDetail(donation.donationId) },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(donation.itemName, fontWeight = FontWeight.Bold)
                                        Text(
                                            donation.pickupCity,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(if (donation.status == DonationStatus.CLAIMED.name) "Claimed" else "Available") }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(message, textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
