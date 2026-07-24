package com.mishba.ecopantryapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationTableDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: LocationTable): Long

    @Update
    suspend fun update(location: LocationTable)

    @Delete
    suspend fun delete(location: LocationTable)

    @Query("SELECT * FROM location_table WHERE user_id = :userId")
    fun getAllForUser(userId: String): Flow<List<LocationTable>>

    @Query("SELECT * FROM location_table WHERE location_id = :id LIMIT 1")
    suspend fun getById(id: Int): LocationTable?
}

