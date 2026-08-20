package com.mishba.ecopantryapp.model

/** Date-range filter for the "Track My Impact" report (Use Case 4, step 5-6). */
enum class ImpactRangeFilter(val label: String) {
    WEEKLY("This Week"),
    MONTHLY("This Month"),
    ALL_TIME("All Time")
}
