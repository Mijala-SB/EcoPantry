package com.mishba.ecopantryapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mishba.ecopantryapp.model.LogActionType
import java.util.UUID

/**
 * One row per inventory change (add / edit / used / donated / deleted).
 * Feeds the Weekly Food Tracker (US 3.2, 6.1, 6.2) and the impact dashboard (FR10).
 */
@Entity(tableName = "food_log_table")
data class FoodLogTable(
    @PrimaryKey
    @ColumnInfo(name = "log_id")
    val logId: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "item_id")
    val itemId: String,
    @ColumnInfo(name = "item_name")
    val itemName: String,
    @ColumnInfo(name = "action_type")
    val actionType: LogActionType,
    @ColumnInfo(name = "category")
    val category: String = "",
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
