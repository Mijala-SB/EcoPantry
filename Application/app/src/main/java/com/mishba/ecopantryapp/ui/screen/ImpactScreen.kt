package com.mishba.ecopantryapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.mishba.ecopantryapp.model.ImpactRangeFilter
import com.mishba.ecopantryapp.ui.theme.EcoAmber40
import com.mishba.ecopantryapp.ui.theme.EcoGreen40
import com.mishba.ecopantryapp.ui.widget.BarChartEntry
import com.mishba.ecopantryapp.ui.widget.SimpleBarChart
import com.mishba.ecopantryapp.ui.widget.StatSummaryCard

@OptIn(ExperimentalMaterial3Api::class)
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

            // ── Date-range filter (Typical Course step 5-6: "monthly, weekly...") ──
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ImpactRangeFilter.entries.toList()) { filter ->
                    FilterChip(
                        selected = filter == state.rangeFilter,
                        onClick = { vm.selectRangeFilter(filter) },
                        label = { Text(filter.label) }
                    )
                }
            }

            if (state.hasNoData) {
                // Alternative Course 3a: no food-saving data found for this range yet.
                Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(28.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Eco, contentDescription = null, tint = EcoGreen40)
                        Text(
                            "No food-saving activity yet for ${state.rangeFilter.label.lowercase()}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            "Start logging food you use and donate to see your impact grow here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                return@Column
            }

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
                Text("Saved by Category (tap a bar to filter)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    SimpleBarChart(
                        entries = state.categoryBreakdown.entries.take(6).map {
                            BarChartEntry(
                                label = it.key.take(4),
                                value = it.value.toFloat(),
                                color = if (it.key == state.selectedCategory) EcoAmber40 else EcoGreen40,
                                id = it.key
                            )
                        },
                        modifier = Modifier.padding(12.dp),
                        onEntryClick = { entry -> vm.selectCategory(entry.id) }
                    )
                }

                // Data-point drill-down: activity behind the selected category (step 5-6).
                if (state.selectedCategory != null) {
                    Text(
                        "${state.selectedCategory} activity",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    state.filteredActivity.take(10).forEach { log ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${log.actionType.label} - ${log.itemName}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}
