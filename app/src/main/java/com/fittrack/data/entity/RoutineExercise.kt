package com.fittrack.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "routine_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutRoutine::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("routineId"), Index("exerciseId")]
)
data class RoutineExercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val routineId: Int,
    val exerciseId: Int,
    val orderIndex: Int,
    val defaultSets: Int = 3,
    val defaultReps: Int = 10
)
