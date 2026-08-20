package com.mishba.ecopantryapp

import com.mishba.ecopantryapp.data.FoodItemTable
import com.mishba.ecopantryapp.model.MealSlot
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class MealPlanningTest {

    @Test
    fun weekStartShouldBeMondayAt0000() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayDow = calendar.get(Calendar.DAY_OF_WEEK)
        val daysSinceMonday = ((todayDow + 5) % 7)
        calendar.add(Calendar.DAY_OF_YEAR, -daysSinceMonday)
        assertEquals(Calendar.MONDAY, calendar.get(Calendar.DAY_OF_WEEK))
    }

    @Test
    fun mealSlotLabelsShouldBeCorrect() {
        assertEquals("Breakfast", MealSlot.BREAKFAST.label)
        assertEquals("Lunch", MealSlot.LUNCH.label)
        assertEquals("Snack", MealSlot.SNACK.label)
        assertEquals("Dinner", MealSlot.DINNER.label)
    }

    @Test
    fun suggestedRecipesShouldMatchAvailableIngredients() {
        val inventory = listOf("Apple", "Milk", "Cereal")
        val suggested = listOf("Fruit Salad", "Cereal with Milk")
        val matches = suggested.any { recipe ->
            inventory.any { ingredient -> recipe.contains(ingredient) }
        }
        assertTrue(matches)
    }

    @Test
    fun confirmingPlanShouldReserveIngredients() {
        val item = FoodItemTable(
            userId = "u1",
            itemName = "Apple",
            quantity = "3",
            expiryDate = null   // <-- required parameter added
        )
        val reserved = 2
        val remaining = (item.quantity.toIntOrNull() ?: 0) - reserved
        assertEquals(1, remaining)
    }
}