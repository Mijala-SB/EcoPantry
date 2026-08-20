package com.mishba.ecopantryapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mishba.ecopantryapp.model.FoodStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodItemTableDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: FoodItemTable)

    @Update
    suspend fun update(item: FoodItemTable)

    @Delete
    suspend fun delete(item: FoodItemTable)

    @Query("SELECT * FROM food_item_table WHERE user_id = :userId ORDER BY expiry_date ASC")
    fun getAllForUser(userId: String): Flow<List<FoodItemTable>>

    @Query("SELECT * FROM food_item_table WHERE user_id = :userId AND status = :status ORDER BY expiry_date ASC")
    fun getByStatus(userId: String, status: FoodStatus): Flow<List<FoodItemTable>>

    @Query("SELECT * FROM food_item_table WHERE item_id = :id LIMIT 1")
    suspend fun getById(id: String): FoodItemTable?

    /** Items expiring within [windowEnd] (epoch millis) that are still active — used by the expiry worker (FR11). */
    @Query(
        "SELECT * FROM food_item_table WHERE status = 'ACTIVE' AND expiry_date IS NOT NULL " +
        "AND expiry_date BETWEEN :nowMillis AND :windowEnd"
    )
    suspend fun getItemsExpiringSoon(nowMillis: Long, windowEnd: Long): List<FoodItemTable>

    @Query("SELECT COUNT(*) FROM food_item_table WHERE user_id = :userId AND status = 'ACTIVE'")
    fun getActiveItemCount(userId: String): Flow<Int>

    /** Active items not already reserved for another meal - candidates for meal planning (UC6). */
    @Query(
        "SELECT * FROM food_item_table WHERE user_id = :userId AND status = 'ACTIVE' " +
        "AND (reserved_for_meal_plan_id IS NULL OR reserved_for_meal_plan_id = :excludingPlanId) " +
        "ORDER BY expiry_date ASC"
    )
    fun getAvailableForMealPlanning(userId: String, excludingPlanId: String? = null): Flow<List<FoodItemTable>>

    @Query("UPDATE food_item_table SET reserved_for_meal_plan_id = :planId WHERE item_id IN (:itemIds)")
    suspend fun reserveItems(itemIds: List<String>, planId: String)

    @Query("UPDATE food_item_table SET reserved_for_meal_plan_id = NULL WHERE reserved_for_meal_plan_id = :planId")
    suspend fun releaseItemsForPlan(planId: String)
}
