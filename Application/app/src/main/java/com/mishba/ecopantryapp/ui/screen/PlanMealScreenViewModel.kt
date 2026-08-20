package com.mishba.ecopantryapp.ui.screen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mishba.ecopantryapp.data.AppDataStore
import com.mishba.ecopantryapp.data.AppDatabase
import com.mishba.ecopantryapp.data.FoodItemTable
import com.mishba.ecopantryapp.data.MealPlanTable
import com.mishba.ecopantryapp.data.Repository
import com.mishba.ecopantryapp.model.MealSlot
import com.mishba.ecopantryapp.model.Recipe
import com.mishba.ecopantryapp.model.RecipeCatalog
import com.mishba.ecopantryapp.utility.NotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

data class MealPlanUiState(
    val weekDayLabels: List<String> = emptyList(),
    val weekDayStarts: List<Long> = emptyList(),
    val selectedDayIndex: Int = 0,
    val selectedSlot: MealSlot = MealSlot.BREAKFAST,
    val allWeekPlans: List<MealPlanTable> = emptyList(),
    val availableInventory: List<FoodItemTable> = emptyList(),
    val selectedItemIds: Set<String> = emptySet(),
    val customMealName: String = "",
    val suggestedRecipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = true,
    val confirmationMessage: String? = null
) {
    val plansForSelectedDaySlot: List<MealPlanTable>
        get() {
            val day = weekDayStarts.getOrNull(selectedDayIndex) ?: return emptyList()
            return allWeekPlans.filter { it.dayStartMillis == day && it.slot == selectedSlot }
        }
}

/**
 * Backs "Plan Weekly Meals" (Use Case 6): shows a calendar of the current week, lets the
 * user assign meals to breakfast/lunch/dinner/snack slots (manually or from suggested
 * recipes based on their inventory), and on confirmation reserves the linked ingredients
 * and schedules a reminder for each planned meal.
 */
class PlanMealScreenViewModel(context: Context) : ViewModel() {

    private val appContext = context.applicationContext
    private val repository = Repository(AppDatabase.getInstance(context))
    private val appDataStore = AppDataStore(context)

    private val _uiState = MutableStateFlow(MealPlanUiState())
    val uiState = _uiState.asStateFlow()

    private var userId: String? = null
    private val dayLabelFormat = SimpleDateFormat("EEE d", Locale.getDefault())

    init {
        val (weekStart, weekEnd, dayStarts) = computeWeek()
        _uiState.value = _uiState.value.copy(
            weekDayStarts = dayStarts,
            weekDayLabels = dayStarts.map { dayLabelFormat.format(it) }
        )

        viewModelScope.launch {
            appDataStore.loggedInUserIdFlow().collectLatest { uid ->
                if (uid == null) return@collectLatest
                userId = uid
                combine(
                    repository.getAvailableForMealPlanning(uid),
                    repository.getMealPlansForWeek(uid, weekStart, weekEnd)
                ) { inventory, plans -> inventory to plans }
                    .collectLatest { (inventory, plans) ->
                        val current = _uiState.value
                        _uiState.value = current.copy(
                            availableInventory = inventory,
                            allWeekPlans = plans,
                            suggestedRecipes = suggestionsFor(current.selectedSlot, inventory),
                            isLoading = false
                        )
                    }
            }
        }
    }

    private fun computeWeek(): Triple<Long, Long, List<Long>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0)
        // Roll back to the start of the current week (Monday-first).
        val todayDow = calendar.get(Calendar.DAY_OF_WEEK) // SUNDAY=1..SATURDAY=7
        val daysSinceMonday = ((todayDow + 5) % 7)
        calendar.add(Calendar.DAY_OF_YEAR, -daysSinceMonday)

        val dayStarts = (0..6).map { offset ->
            val cal = calendar.clone() as Calendar
            cal.add(Calendar.DAY_OF_YEAR, offset)
            cal.timeInMillis
        }
        return Triple(dayStarts.first(), dayStarts.last() + TimeUnit.DAYS.toMillis(1) - 1, dayStarts)
    }

    private fun suggestionsFor(slot: MealSlot, inventory: List<FoodItemTable>): List<Recipe> {
        val tags = inventory.flatMap { listOf(it.category.name, it.category.label, it.itemName) }.toSet()
        return RecipeCatalog.suggestFor(slot, tags)
    }

    fun selectDay(index: Int) {
        _uiState.value = _uiState.value.copy(selectedDayIndex = index, selectedItemIds = emptySet())
    }

    fun selectSlot(slot: MealSlot) {
        _uiState.value = _uiState.value.copy(
            selectedSlot = slot,
            selectedItemIds = emptySet(),
            suggestedRecipes = suggestionsFor(slot, _uiState.value.availableInventory)
        )
    }

    fun toggleInventorySelection(itemId: String) {
        val current = _uiState.value.selectedItemIds
        _uiState.value = _uiState.value.copy(
            selectedItemIds = if (itemId in current) current - itemId else current + itemId
        )
    }

    fun updateCustomMealName(name: String) {
        _uiState.value = _uiState.value.copy(customMealName = name)
    }

    /** Adds a suggested recipe to the selected day/slot, linking any inventory items the user picked (step 5-6). */
    fun addSuggestedRecipe(recipe: Recipe) = addMeal(mealName = recipe.name, recipeId = recipe.recipeId, isCustom = false)

    /** Creates a custom meal for the selected day/slot instead of using a suggestion (step 5-6). */
    fun addCustomMeal() {
        val name = _uiState.value.customMealName.trim()
        if (name.isEmpty()) return
        addMeal(mealName = name, recipeId = null, isCustom = true)
    }

    private fun addMeal(mealName: String, recipeId: String?, isCustom: Boolean) {
        val uid = userId ?: return
        val state = _uiState.value
        val dayStart = state.weekDayStarts.getOrNull(state.selectedDayIndex) ?: return
        viewModelScope.launch {
            repository.addMealToPlan(
                MealPlanTable(
                    userId = uid,
                    dayStartMillis = dayStart,
                    slot = state.selectedSlot,
                    mealName = mealName,
                    recipeId = recipeId,
                    isCustom = isCustom,
                    linkedItemIds = state.selectedItemIds.toList()
                )
            )
            _uiState.value = _uiState.value.copy(selectedItemIds = emptySet(), customMealName = "")
        }
    }

    fun removeMeal(plan: MealPlanTable) {
        viewModelScope.launch {
            repository.deleteMealPlan(plan)
            NotificationScheduler.cancelMealReminder(appContext, plan.planId)
        }
    }

    /** Typical Course step 7-8: persists reservations and schedules a reminder for every planned meal. */
    fun confirmWeekPlan() {
        val uid = userId ?: return
        val plans = _uiState.value.allWeekPlans
        if (plans.isEmpty()) {
            _uiState.value = _uiState.value.copy(confirmationMessage = "Add at least one meal before confirming your plan.")
            return
        }
        viewModelScope.launch {
            val needingReminders = repository.confirmWeekPlan(plans)
            needingReminders.forEach { plan ->
                NotificationScheduler.scheduleMealReminder(
                    context = appContext,
                    planId = plan.planId,
                    userId = uid,
                    mealName = plan.mealName,
                    slotLabel = plan.slot.label,
                    delayMillis = reminderDelayFor(plan)
                )
            }
            _uiState.value = _uiState.value.copy(
                confirmationMessage = "Weekly plan confirmed! Ingredients reserved and reminders scheduled for ${plans.size} meal(s)."
            )
        }
    }

    fun dismissConfirmation() {
        _uiState.value = _uiState.value.copy(confirmationMessage = null)
    }

    /** Reminder fires 1 hour before the slot's typical time, or in 5 minutes if that's already passed. */
    private fun reminderDelayFor(plan: MealPlanTable): Long {
        val slotHour = when (plan.slot) {
            MealSlot.BREAKFAST -> 8
            MealSlot.LUNCH -> 12
            MealSlot.SNACK -> 15
            MealSlot.DINNER -> 18
        }
        val calendar = Calendar.getInstance().apply {
            timeInMillis = plan.dayStartMillis
            set(Calendar.HOUR_OF_DAY, slotHour); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
        }
        val reminderTime = calendar.timeInMillis - TimeUnit.HOURS.toMillis(1)
        val now = System.currentTimeMillis()
        return if (reminderTime > now) reminderTime - now else TimeUnit.MINUTES.toMillis(5)
    }
}
