package com.mishba.ecopantryapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Pickup / household location (Table 10, Task 2). */
@Entity(tableName = "location_table")
data class LocationTable(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "location_id")
    val locationId: Int = 0,
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "address_line")
    val addressLine: String,
    @ColumnInfo(name = "city")
    val city: String,
    @ColumnInfo(name = "state")
    val state: String,
    @ColumnInfo(name = "postal_code")
    val postalCode: String,
    @ColumnInfo(name = "country")
    val country: String = "Malaysia"
)
