package com.mishba.ecopantryapp.model

/** A time-of-day slot within a day's meal plan (Use Case 6 - Plan Weekly Meals). */
enum class MealSlot(val label: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    SNACK("Snack")
}
