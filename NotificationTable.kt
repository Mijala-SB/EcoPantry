package com.ecopantry.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ecopantry.app.ui.theme.EcoAmber40
import com.ecopantry.app.ui.theme.EcoGreen40
import com.ecopantry.app.ui.widget.BarChartEntry
import com.ecopantry.app.ui.widget.SimpleBarChart
import com.ecopantry.app.ui.widget.StatSummaryCard

@Composable
fun ImpactScreen() {
    val context = LocalContext.current
    val vm: ImpactScreenViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ImpactScreenViewModel(context) as T
            }
        }
    )
    val state by vm.uiState.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Track My Impact") }) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { state.diversionRate },
                            modifier = Modifier.size(140.dp),
                            strokeWidth = 12.dp,
                            strokeCap = StrokeCap.Round,
                            color = EcoGreen40,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${(state.diversionRate * 100).toInt()}%",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text("Waste Diverted", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Icon(Icons.Default.Eco, contentDescription = null, tint = EcoGreen40)
                    Text(
                        "~${"%.1f".format(state.estimatedKgSaved)} kg of food saved from waste",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatSummaryCard("Used at Home", state.totalUsed.toString(), Icons.Default.RestaurantMenu, Modifier.weight(1f))
                StatSummaryCard("Donated", state.totalDonated.toString(), Icons.Default.VolunteerActivism, Modifier.weight(1f), EcoGreen40)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatSummaryCard("Wasted", state.totalWasted.toString(), Icons.Default.Delete, Modifier.weight(1f), EcoAmber40)
                StatSummaryCard("Listings Published", state.donationsPublished.toString(), Icons.Default.VolunteerActivism, Modifier.weight(1f))
            }

            if (state.categoryBreakdown.isNotEmpty()) {
                Text("Saved by Category", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    SimpleBarChart(
                        entries = state.categoryBreakdown.entries.take(6).map {
                            BarChartEntry(it.key.take(4), it.value.toFloat(), EcoGreen40)
                        },
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}
