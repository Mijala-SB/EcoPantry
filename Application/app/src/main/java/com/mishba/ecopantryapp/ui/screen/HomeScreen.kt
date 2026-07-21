package com.mishba.ecopantryapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mishba.ecopantryapp.ui.widget.StatSummaryCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateToInventory: () -> Unit,
    navigateToBrowseDonations: () -> Unit,
    navigateToTracker: () -> Unit,
    navigateToProfile: () -> Unit,
    navigateToAddFood: () -> Unit,
    navigateToNotifications: () -> Unit,
    navigateToFoodDetail: (String) -> Unit
) {
    val context = LocalContext.current
    val vm: HomeScreenViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return HomeScreenViewModel(context) as T
            }
        }
    )
    val state by vm.uiState.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hi, ${state.userName} 👋") },
                actions = {
                    BadgedBox(
                        badge = {
                            if (state.unreadNotifications > 0) {
                                Badge { Text(state.unreadNotifications.toString()) }
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        IconButton(onClick = navigateToNotifications) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = navigateToAddFood,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Food") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatSummaryCard(
                        label = "Active Items",
                        value = state.activeItemCount.toString(),
                        icon = Icons.Default.Inventory2,
                        modifier = Modifier.weight(1f)
                    )
                    StatSummaryCard(
                        label = "Expiring Soon",
                        value = state.expiringSoon.size.toString(),
                        icon = Icons.Default.Warning,
                        modifier = Modifier.weight(1f),
                        accentColor = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    QuickActionCard(
                        label = "My Inventory",
                        icon = Icons.Default.Inventory2,
                        modifier = Modifier.weight(1f),
                        onClick = navigateToInventory
                    )
                    QuickActionCard(
                        label = "Browse Donations",
                        icon = Icons.Default.VolunteerActivism,
                        modifier = Modifier.weight(1f),
                        onClick = navigateToBrowseDonations
                    )
                }
            }

            item {
                Text(
                    "Expiring Soon",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (state.expiringSoon.isEmpty()) {
                item {
                    Text(
                        "Nothing expiring in the next 3 days. Nice work!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(state.expiringSoon, key = { it.itemId }) { item ->
                    Card(
                        onClick = { navigateToFoodDetail(item.itemId) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(item.itemName, fontWeight = FontWeight.Bold)
                                Text(
                                    item.category.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = item.expiryDate?.let { "Exp: ${dateFormat.format(Date(it))}" } ?: "",
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(
                label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
