package com.mishba.ecopantryapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mishba.ecopantryapp.model.MealSlot
import java.util.UUID

/**
 * A single meal planned for a day/slot in the weekly meal calendar (Use Case 6 -
 * Plan Weekly Meals). [dayStartMillis] is normalised to midnight so a day's meals
 * can be queried together, and [linkedItemIds] records which inventory items were
 * reserved for this meal so they can be released again if the meal is removed.
 */
@Entity(tableName = "meal_plan_table")
data class MealPlanTable(
    @PrimaryKey
    @ColumnInfo(name = "plan_id")
    val planId: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "day_start")
    val dayStartMillis: Long,
    @ColumnInfo(name = "slot")
    val slot: MealSlot,
    @ColumnInfo(name = "meal_name")
    val mealName: String,
    @ColumnInfo(name = "recipe_id")
    val recipeId: String? = null,
    @ColumnInfo(name = "is_custom")
    val isCustom: Boolean = false,
    /** Comma-separated food_item_table ids reserved against this meal (converted via [AppTypeConverters]). */
    @ColumnInfo(name = "linked_item_ids")
    val linkedItemIds: List<String> = emptyList(),
    @ColumnInfo(name = "reminder_scheduled")
    val reminderScheduled: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

