package com.ecopantry.app.model

/** Action recorded to food_log every time inventory changes (US 3.2, 6.1). */
enum class LogActionType(val label: String) {
    ADDED("Added"),
    EDITED("Edited"),
    USED("Used"),
    DONATED("Donated"),
    DELETED("Deleted")
}
