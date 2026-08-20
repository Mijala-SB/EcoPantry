package com.mishba.ecopantryapp.model

/**
 * A lightweight, offline recipe suggestion. Recipes are matched against the user's
 * current inventory by category/keyword so "Plan Weekly Meals" (Use Case 6) can
 * recommend meals that use up items already on hand.
 */
data class Recipe(
    val recipeId: String,
    val name: String,
    val slot: MealSlot,
    /** Categories/keywords this recipe draws on - matched loosely against inventory items. */
    val matchTags: List<String>,
    val description: String
)

/**
 * Small built-in recipe catalog. There is no network dependency here on purpose -
 * suggestions must work entirely offline from whatever is currently in the pantry.
 */
object RecipeCatalog {

    private val recipes = listOf(
        Recipe("r_veg_stirfry", "Veggie Stir-Fry", MealSlot.DINNER, listOf("fresh", "vegetable", "grain"), "Quick stir-fry using up fresh produce and any grains you have on hand."),
        Recipe("r_omelette", "Veggie Omelette", MealSlot.BREAKFAST, listOf("dairy", "egg", "fresh"), "Eggs and dairy paired with whatever fresh produce is closest to expiring."),
        Recipe("r_pasta_bake", "Pantry Pasta Bake", MealSlot.DINNER, listOf("grain", "canned", "dairy"), "Pasta baked with canned tomatoes/sauce and cheese from the fridge."),
        Recipe("r_fruit_bowl", "Fruit & Yogurt Bowl", MealSlot.SNACK, listOf("fresh", "dairy"), "A simple way to use up fresh fruit before it turns, with yogurt from the dairy shelf."),
        Recipe("r_soup", "Leftover Veg Soup", MealSlot.LUNCH, listOf("fresh", "canned", "frozen"), "A one-pot soup that mops up wilting vegetables and canned or frozen odds and ends."),
        Recipe("r_sandwich", "Bakery Sandwich", MealSlot.LUNCH, listOf("bakery", "dairy", "fresh"), "Bread nearing its best-before date, filled with whatever's fresh in the fridge."),
        Recipe("r_smoothie", "Frozen Fruit Smoothie", MealSlot.BREAKFAST, listOf("frozen", "dairy", "beverage"), "Blends frozen fruit and dairy/beverages before the freezer stash gets old."),
        Recipe("r_snack_mix", "Pantry Snack Mix", MealSlot.SNACK, listOf("snack", "grain", "canned"), "A mix of dry snacks and pantry staples that are approaching their expiry window.")
    )

    /** Generic fallback recipes shown when nothing in the inventory matches any tag (Alt. Course 4a). */
    private val genericFallback = listOf(
        Recipe("g_breakfast", "Simple Toast & Eggs", MealSlot.BREAKFAST, emptyList(), "An easy go-to breakfast when nothing specific is suggested by your inventory."),
        Recipe("g_lunch", "Quick Rice Bowl", MealSlot.LUNCH, emptyList(), "A flexible rice bowl you can build with whatever you like."),
        Recipe("g_dinner", "House Salad & Protein", MealSlot.DINNER, emptyList(), "A generic, balanced dinner idea to fall back on."),
        Recipe("g_snack", "Trail Mix", MealSlot.SNACK, emptyList(), "A generic snack idea when no pantry match is found.")
    )

    /**
     * Suggests recipes for [slot] based on the categories/names present in [inventoryTags].
     * Falls back to generic recipes for that slot if nothing matches (Alt. Course 4a).
     */
    fun suggestFor(slot: MealSlot, inventoryTags: Set<String>): List<Recipe> {
        val lowerTags = inventoryTags.map { it.lowercase() }.toSet()
        val matches = recipes.filter { recipe ->
            recipe.slot == slot && recipe.matchTags.any { tag -> lowerTags.any { it.contains(tag) } }
        }
        return if (matches.isNotEmpty()) matches else genericFallback.filter { it.slot == slot }
    }
}
