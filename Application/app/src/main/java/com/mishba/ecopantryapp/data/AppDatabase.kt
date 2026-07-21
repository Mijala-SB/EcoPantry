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
import com.mishba.ecopantryapp.model.NotificationType
import com.mishba.ecopantryapp.model.StorageArea

@TypeConverters(AppTypeConverters::class)
@Database(
    entities = [
        UserTable::class,
        LocationTable::class,
        FoodItemTable::class,
        FoodLogTable::class,
        NotificationTable::class
    ],
    version = 1
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
                ).build().also { instance = it }
            }
        }
    }

    abstract fun getUserTableDao(): UserTableDao
    abstract fun getLocationTableDao(): LocationTableDao
    abstract fun getFoodItemTableDao(): FoodItemTableDao
    abstract fun getFoodLogTableDao(): FoodLogTableDao
    abstract fun getNotificationTableDao(): NotificationTableDao
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
}
