package com.ecopantry.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ecopantry.app.model.LogActionType
import com.ecopantry.app.ui.theme.EcoAmber40
import com.ecopantry.app.ui.theme.EcoGreen40
import com.ecopantry.app.ui.theme.EcoGreenGrey40
import com.ecopantry.app.ui.widget.BarChartEntry
import com.ecopantry.app.ui.widget.SimpleBarChart
import com.ecopantry.app.ui.widget.StatSummaryCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WeeklyTrackerScreen() {
    val context = LocalContext.current
    val vm: WeeklyTrackerScreenViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return WeeklyTrackerScreenViewModel(context) as T
            }
        }
    )
    val state by vm.uiState.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

    Scaffold(topBar = { TopAppBar(title = { Text("Weekly Tracker") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatSummaryCard("Added", state.totalAdded.toString(), Icons.Default.AddCircle, Modifier.weight(1f))
                    StatSummaryCard("Used", state.totalUsed.toString(), Icons.Default.RestaurantMenu, Modifier.weight(1f), EcoGreenGrey40)
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatSummaryCard("Donated", state.totalDonated.toString(), Icons.Default.VolunteerActivism, Modifier.weight(1f), EcoGreen40)
                    StatSummaryCard("Wasted", state.totalWasted.toString(), Icons.Default.Delete, Modifier.weight(1f), EcoAmber40)
                }
            }

            item {
                Text("Last 7 Days - Items Added", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
            item {
                Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    SimpleBarChart(
                        entries = state.dailyTallies.map { BarChartEntry(it.label, it.added.toFloat(), EcoGreen40) },
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            item {
                Text("Recent Activity", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }

            if (state.logs.isEmpty()) {
                item {
                    Text(
                        "No activity logged this week yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(state.logs.take(30), key = { it.logId }) { log ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("${log.actionType.label} - ${log.itemName}", fontWeight = FontWeight.Medium)
                            Text(
                                log.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            dateFormat.format(Date(log.timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}
