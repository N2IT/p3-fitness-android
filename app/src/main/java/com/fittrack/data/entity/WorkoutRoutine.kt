package com.fittrack.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_routines",
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
data class WorkoutRoutine(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val name: String,
    val lastPerformedAt: Long? = null,
    val timesPerformed: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
