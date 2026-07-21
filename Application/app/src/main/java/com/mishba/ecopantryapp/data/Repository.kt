package com.mishba.ecopantryapp.data

import android.util.Log
import com.mishba.ecopantryapp.model.FoodStatus
import kotlinx.coroutines.flow.Flow

/**
 * Facade over the local Room database (device-side inventory, logs, notifications,
 * and the cached user profile). Cloud operations (auth, donations) live in
 * [AuthRepository] / [DonationRepository] since they talk to Firebase instead.
 */
class Repository(private val appDatabase: AppDatabase) {

    // ── User (local cache of Firebase profile) ──────────────────────────────
    suspend fun cacheUser(user: UserTable) = appDatabase.getUserTableDao().insert(user)

    suspend fun getUserById(id: String): UserTable? = appDatabase.getUserTableDao().getUserById(id)

    fun observeUser(id: String): Flow<UserTable?> = appDatabase.getUserTableDao().observeUserById(id)

    suspend fun updateUser(user: UserTable) = appDatabase.getUserTableDao().update(user)

    // ── Locations ─────────────────────────────────────────────────────────
    fun getLocationsForUser(userId: String): Flow<List<LocationTable>> =
        appDatabase.getLocationTableDao().getAllForUser(userId)

    suspend fun saveLocation(location: LocationTable): Long =
        appDatabase.getLocationTableDao().insert(location)

    // ── Food Inventory (FR05-FR07) ───────────────────────────────────────
    fun getAllFoodItems(userId: String): Flow<List<FoodItemTable>> =
        appDatabase.getFoodItemTableDao().getAllForUser(userId)

    fun getFoodItemsByStatus(userId: String, status: FoodStatus): Flow<List<FoodItemTable>> =
        appDatabase.getFoodItemTableDao().getByStatus(userId, status)

    suspend fun getFoodItemById(id: String): FoodItemTable? =
        appDatabase.getFoodItemTableDao().getById(id)

    suspend fun insertFoodItem(item: FoodItemTable) {
        Log.d("Repository", "insertFoodItem() name=${item.itemName}")
        appDatabase.getFoodItemTableDao().insert(item)
    }

    suspend fun updateFoodItem(item: FoodItemTable) = appDatabase.getFoodItemTableDao().update(item)

    suspend fun deleteFoodItem(item: FoodItemTable) = appDatabase.getFoodItemTableDao().delete(item)

    suspend fun getItemsExpiringSoon(nowMillis: Long, windowEnd: Long): List<FoodItemTable> =
        appDatabase.getFoodItemTableDao().getItemsExpiringSoon(nowMillis, windowEnd)

    fun getActiveItemCount(userId: String): Flow<Int> =
        appDatabase.getFoodItemTableDao().getActiveItemCount(userId)

    // ── Food Log / Weekly Tracker (US 3.2, 6.1, 6.2) ─────────────────────
    suspend fun insertFoodLog(log: FoodLogTable) = appDatabase.getFoodLogTableDao().insert(log)

    fun getAllFoodLogs(userId: String): Flow<List<FoodLogTable>> =
        appDatabase.getFoodLogTableDao().getAllForUser(userId)

    fun getFoodLogsByDateRange(userId: String, start: Long, end: Long): Flow<List<FoodLogTable>> =
        appDatabase.getFoodLogTableDao().getByDateRange(userId, start, end)

    suspend fun countLogsByAction(userId: String, actionType: String, start: Long, end: Long): Int =
        appDatabase.getFoodLogTableDao().countByAction(userId, actionType, start, end)

    // ── Notifications (FR11, FR12, US 4.1-4.3) ───────────────────────────
    suspend fun insertNotification(notification: NotificationTable) =
        appDatabase.getNotificationTableDao().insert(notification)

    fun getAllNotifications(userId: String): Flow<List<NotificationTable>> =
        appDatabase.getNotificationTableDao().getAllForUser(userId)

    fun getUnreadNotificationCount(userId: String): Flow<Int> =
        appDatabase.getNotificationTableDao().getUnreadCount(userId)

    suspend fun markNotificationAsRead(id: String) =
        appDatabase.getNotificationTableDao().markAsRead(id)

    suspend fun markAllNotificationsAsRead(userId: String) =
        appDatabase.getNotificationTableDao().markAllAsRead(userId)

    suspend fun deleteNotification(id: String) = appDatabase.getNotificationTableDao().delete(id)

    suspend fun clearAllNotifications(userId: String) =
        appDatabase.getNotificationTableDao().clearAll(userId)

    suspend fun countRecentExpiryAlertsFor(itemId: String, sinceMillis: Long): Int =
        appDatabase.getNotificationTableDao().countRecentExpiryAlertsFor(itemId, sinceMillis)
}
