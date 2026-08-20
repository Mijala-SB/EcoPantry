package com.mishba.ecopantryapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MealPlanTableDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plan: MealPlanTable)

    @Update
    suspend fun update(plan: MealPlanTable)

    @Delete
    suspend fun delete(plan: MealPlanTable)

    @Query("SELECT * FROM meal_plan_table WHERE plan_id = :id LIMIT 1")
    suspend fun getById(id: String): MealPlanTable?

    /** All meals for the week containing [weekStart]..[weekEnd] (both normalised to day boundaries). */
    @Query(
        "SELECT * FROM meal_plan_table WHERE user_id = :userId AND day_start BETWEEN :weekStart AND :weekEnd " +
        "ORDER BY day_start ASC"
    )
    fun getForWeek(userId: String, weekStart: Long, weekEnd: Long): Flow<List<MealPlanTable>>

    @Query("DELETE FROM meal_plan_table WHERE plan_id = :id")
    suspend fun deleteById(id: String)
}
