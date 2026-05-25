package com.fittrack.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "personal_records",
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("exerciseId")]
)
data class PersonalRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val exerciseId: Int,
    val userId: Int,
    val recordType: String, // "max_weight", "max_reps", "max_volume", "est_1rm"
    val value: Double,
    val weight: Double = 0.0,
    val reps: Int = 0,
    val workoutSessionId: String = "",
    val achievedAt: Long = System.currentTimeMillis()
)
