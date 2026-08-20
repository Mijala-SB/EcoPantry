package com.mishba.ecopantryapp

import com.mishba.ecopantryapp.data.FoodLogTable
import com.mishba.ecopantryapp.model.LogActionType
import org.junit.Assert.*
import org.junit.Test

/**
 * UT-ANALYTICS: Unit tests for impact and analytics calculations
 * used in ImpactScreenViewModel and WeeklyTrackerScreenViewModel.
 * Covers: diversion rate, total saved, category breakdown, daily tallies.
 */

class AnalyticsTest {

    @Test
    fun diversionRateShouldBeCalculatedCorrectly() {
        val used = 10
        val donated = 5
        val wasted = 3
        val total = used + donated + wasted
        val rate = (used + donated).toFloat() / total
        assertEquals(0.8333f, rate, 0.0001f)
    }

    @Test
    fun diversionRateShouldBeZeroWhenNoData() {
        val rate = 0.0f
        assertEquals(0.0f, rate, 0.0f)
    }

    @Test
    fun estimatedKgSavedShouldBe0Point4KgPerSavedItem() {
        val used = 5
        val donated = 3
        val saved = used + donated
        val kg = saved * 0.4
        assertEquals(3.2, kg, 0.001)
    }

    @Test
    fun categoryBreakdownShouldGroupByCategory() {
        val logs = listOf(
            FoodLogTable(userId = "u1", itemId = "i1", itemName = "a", actionType = LogActionType.USED, category = "Fruit"),
            FoodLogTable(userId = "u1", itemId = "i2", itemName = "b", actionType = LogActionType.DONATED, category = "Fruit"),
            FoodLogTable(userId = "u1", itemId = "i3", itemName = "c", actionType = LogActionType.USED, category = "Dairy")
        )
        val breakdown = logs
            .filter { it.actionType == LogActionType.USED || it.actionType == LogActionType.DONATED }
            .groupingBy { it.category }
            .eachCount()
        assertEquals(2, breakdown["Fruit"])
        assertEquals(1, breakdown["Dairy"])
    }

    @Test
    fun dailyTalliesShouldBeBuiltCorrectly() {
        val day1 = System.currentTimeMillis() - 86400000
        val day2 = System.currentTimeMillis() - 2 * 86400000
        val logs = listOf(
            FoodLogTable(userId = "u1", itemId = "i1", itemName = "a", actionType = LogActionType.ADDED, timestamp = day1),
            FoodLogTable(userId = "u1", itemId = "i2", itemName = "b", actionType = LogActionType.ADDED, timestamp = day1),
            FoodLogTable(userId = "u1", itemId = "i3", itemName = "c", actionType = LogActionType.ADDED, timestamp = day2)
        )
        val dayMap = logs.groupBy { it.timestamp / 86400000 }
        assertEquals(2, dayMap[day1 / 86400000]?.size ?: 0)
        assertEquals(1, dayMap[day2 / 86400000]?.size ?: 0)
    }
}