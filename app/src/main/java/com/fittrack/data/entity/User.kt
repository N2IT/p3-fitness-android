package com.fittrack.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val unitPreference: String = "lbs", // "lbs" or "kg"
    val createdAt: Long = System.currentTimeMillis()
)
