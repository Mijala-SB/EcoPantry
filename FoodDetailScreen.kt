package com.ecopantry.app.model

/** Where a food item is physically stored in the household (FR05). */
enum class StorageArea(val label: String) {
    FRIDGE("Fridge"),
    FREEZER("Freezer"),
    PANTRY("Pantry"),
    COUNTERTOP("Countertop"),
    OTHER("Other")
}
