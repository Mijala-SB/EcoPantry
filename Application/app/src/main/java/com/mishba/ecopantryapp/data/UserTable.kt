package com.mishba.ecopantryapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "user_table")
data class UserTable(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "full_name")
    val fullName: String,
    @ColumnInfo(name = "email")
    val email: String,
    @ColumnInfo(name = "household_size")
    val householdSize: Int = 1,
    @ColumnInfo(name = "is_verified")
    val isVerified: Boolean = false,
    @ColumnInfo(name = "two_factor_enabled")
    val twoFactorEnabled: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
