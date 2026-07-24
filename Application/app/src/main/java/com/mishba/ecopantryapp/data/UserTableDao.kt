package com.mishba.ecopantryapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO (Data Access Object) for the "user_table" table.
 * Defines all database operations (CRUD) available for UserTable entities.
 * Room generates the implementation of this interface automatically at compile time.
 */
@Dao
interface UserTableDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserTable)

    @Update
    suspend fun update(user: UserTable)

    @Query("SELECT * FROM user_table WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserTable?

    @Query("SELECT * FROM user_table WHERE id = :id LIMIT 1")
    fun observeUserById(id: String): Flow<UserTable?>

    @Query("SELECT * FROM user_table WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserTable?

    @Query("DELETE FROM user_table WHERE id = :id")
    suspend fun deleteById(id: String)
}
