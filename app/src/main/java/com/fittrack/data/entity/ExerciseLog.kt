package com.fittrack.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercise_logs",
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("exerciseId"), Index("workoutSessionId")]
)
data class ExerciseLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val exerciseId: Int,
    val routineId: Int,
    val userId: Int,
    val workoutSessionId: String,
    val setNumber: Int,
    val weight: Double,
    val reps: Int,
    val completedAt: Long = System.currentTimeMillis()
)
