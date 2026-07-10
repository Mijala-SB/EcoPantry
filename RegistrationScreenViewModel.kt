package com.ecopantry.app.model

/** Food category used when logging an inventory item (FR05). */
enum class FoodCategory(val label: String) {
    FRESH("Fresh Produce"),
    DAIRY("Dairy & Eggs"),
    BAKERY("Bakery"),
    CANNED("Canned / Jarred"),
    FROZEN("Frozen"),
    GRAIN("Grains & Pasta"),
    BEVERAGE("Beverages"),
    SNACK("Snacks"),
    OTHER("Other")
}
