package com.mishba.ecopantryapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.mishba.ecopantryapp.model.FoodCategory
import com.mishba.ecopantryapp.model.FoodStatus
import com.mishba.ecopantryapp.model.LogActionType
import com.mishba.ecopantryapp.model.MealSlot
import com.mishba.ecopantryapp.model.NotificationType
import com.mishba.ecopantryapp.model.StorageArea

@TypeConverters(AppTypeConverters::class)
@Database(
    entities = [
        UserTable::class,
        LocationTable::class,
        FoodItemTable::class,
        FoodLogTable::class,
        NotificationTable::class,
        MealPlanTable::class
    ],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ecopantry_database"
                )
                    // Meal planning (UC6) added the meal_plan_table + a new food_item column in
                    // version 2. There's no shipped data to preserve yet, so a destructive
                    // migration keeps things simple rather than hand-writing an ALTER TABLE.
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
        }
    }

    abstract fun getUserTableDao(): UserTableDao
    abstract fun getLocationTableDao(): LocationTableDao
    abstract fun getFoodItemTableDao(): FoodItemTableDao
    abstract fun getFoodLogTableDao(): FoodLogTableDao
    abstract fun getNotificationTableDao(): NotificationTableDao
    abstract fun getMealPlanTableDao(): MealPlanTableDao
}

class AppTypeConverters {
    @TypeConverter
    fun fromFoodCategory(value: FoodCategory): String = value.name

    @TypeConverter
    fun toFoodCategory(value: String): FoodCategory =
        runCatching { FoodCategory.valueOf(value) }.getOrDefault(FoodCategory.OTHER)

    @TypeConverter
    fun fromStorageArea(value: StorageArea): String = value.name

    @TypeConverter
    fun toStorageArea(value: String): StorageArea =
        runCatching { StorageArea.valueOf(value) }.getOrDefault(StorageArea.OTHER)

    @TypeConverter
    fun fromFoodStatus(value: FoodStatus): String = value.name

    @TypeConverter
    fun toFoodStatus(value: String): FoodStatus =
        runCatching { FoodStatus.valueOf(value) }.getOrDefault(FoodStatus.ACTIVE)

    @TypeConverter
    fun fromLogActionType(value: LogActionType): String = value.name

    @TypeConverter
    fun toLogActionType(value: String): LogActionType =
        runCatching { LogActionType.valueOf(value) }.getOrDefault(LogActionType.ADDED)

    @TypeConverter
    fun fromNotificationType(value: NotificationType): String = value.name

    @TypeConverter
    fun toNotificationType(value: String): NotificationType =
        runCatching { NotificationType.valueOf(value) }.getOrDefault(NotificationType.EXPIRY_ALERT)

    @TypeConverter
    fun fromMealSlot(value: MealSlot): String = value.name

    @TypeConverter
    fun toMealSlot(value: String): MealSlot =
        runCatching { MealSlot.valueOf(value) }.getOrDefault(MealSlot.DINNER)

    @TypeConverter
    fun fromStringList(value: List<String>): String = value.joinToString(",")

    @TypeConverter
    fun toStringList(value: String): List<String> =
        if (value.isBlank()) emptyList() else value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}
