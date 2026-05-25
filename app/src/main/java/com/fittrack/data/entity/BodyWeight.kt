package com.fittrack.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "body_weights",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class BodyWeight(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val weight: Double,
    val loggedAt: Long = System.currentTimeMillis()
)
