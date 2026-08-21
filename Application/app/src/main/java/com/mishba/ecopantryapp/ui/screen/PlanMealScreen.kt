package com.mishba.ecopantryapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mishba.ecopantryapp.ui.theme.EcoGreen40

/** "Plan Weekly Meals" (Use Case 6): calendar of the week + slot-by-slot meal planning. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanMealScreen(navigateBack: () -> Unit) {
    val context = LocalContext.current
    val vm: PlanMealScreenViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return PlanMealScreenViewModel(context) as T
            }
        }
    )
    val state by vm.uiState.collectAsState()

    if (state.confirmationMessage != null) {
        AlertDialog(
            onDismissRequest = vm::dismissConfirmation,
            confirmButton = { TextButton(onClick = vm::dismissConfirmation) { Text("OK") } },
            title = { Text("Weekly Plan") },
            text = { Text(state.confirmationMessage.orEmpty()) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plan Weekly Meals") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = vm::confirmWeekPlan,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) { Text("Confirm Plan for the Week") }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // ── Calendar view for the week (step 2) ─────────────────────────
            Text("This Week", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.weekDayLabels.size) { index ->
                    FilterChip(
                        selected = index == state.selectedDayIndex,
                        onClick = { vm.selectDay(index) },
                        label = { Text(state.weekDayLabels[index]) }
                    )
                }
            }

            // ── Meal slots (step 3) ─────────────────────────────────────────
            Text("Meal Slot", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                com.mishba.ecopantryapp.model.MealSlot.entries.forEach { slot ->
                    FilterChip(
                        selected = slot == state.selectedSlot,
                        onClick = { vm.selectSlot(slot) },
                        label = { Text(slot.label) }
                    )
                }
            }

            // ── Meals already planned for this day/slot ─────────────────────
            if (state.plansForSelectedDaySlot.isNotEmpty()) {
                Text("Planned", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                state.plansForSelectedDaySlot.forEach { plan ->
                    Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(plan.mealName, fontWeight = FontWeight.Medium)
                                if (plan.linkedItemIds.isNotEmpty()) {
                                    Text(
                                        "${plan.linkedItemIds.size} ingredient(s) reserved",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(onClick = { vm.removeMeal(plan) }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove meal")
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            // ── Inventory picker: link ingredients to the meal being added (step 4, 6) ──
            Text(
                "Inventory (focus on items nearing expiry)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            if (state.availableInventory.isEmpty()) {
                Text(
                    "Your inventory is empty. Add food items first so they can be planned into meals.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                FlowRowOfChips(
                    items = state.availableInventory.map { it.itemId to it.itemName },
                    selectedIds = state.selectedItemIds,
                    onToggle = vm::toggleInventorySelection
                )
            }

            // ── Add a custom meal manually (Alt. Course-friendly, step 4) ───
            Text("Add Manually", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.customMealName,
                    onValueChange = vm::updateCustomMealName,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("e.g. Leftover rice + veggies") },
                    singleLine = true
                )
                Button(onClick = vm::addCustomMeal, enabled = state.customMealName.isNotBlank()) { Text("Add") }
            }

            // ── Suggested recipes based on inventory (step 4, 5; Alt. Course 4a) ──
            Text("Suggested Recipes", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            if (state.suggestedRecipes.isEmpty()) {
                Text(
                    "No recipe suggestions available for this slot yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                state.suggestedRecipes.forEach { recipe ->
                    Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.RestaurantMenu, contentDescription = null, tint = EcoGreen40)
                                    Spacer(Modifier.width(6.dp))
                                    Text(recipe.name, fontWeight = FontWeight.Medium)
                                }
                                Text(
                                    recipe.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = { vm.addSuggestedRecipe(recipe) }) { Text("Add") }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FlowRowOfChips(
    items: List<Pair<String, String>>,
    selectedIds: Set<String>,
    onToggle: (String) -> Unit
) {
    // Simple wrap-free horizontal scroller keeps this dependency-free (see SimpleBarChart).
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items) { (id, name) ->
            FilterChip(
                selected = id in selectedIds,
                onClick = { onToggle(id) },
                label = { Text(name) }
            )
        }
    }
}

