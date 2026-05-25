package com.fittrack.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.fittrack.data.entity.WorkoutRoutine
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutRoutineDao {
    @Insert
    suspend fun insert(routine: WorkoutRoutine): Long

    @Update
    suspend fun update(routine: WorkoutRoutine)

    @Delete
    suspend fun delete(routine: WorkoutRoutine)

    @Query("SELECT * FROM workout_routines WHERE userId = :userId ORDER BY lastPerformedAt DESC, createdAt DESC")
    fun getRoutinesForUser(userId: Int): Flow<List<WorkoutRoutine>>

    @Query("SELECT * FROM workout_routines WHERE id = :id")
    suspend fun getRoutineById(id: Int): WorkoutRoutine?

    @Query("SELECT * FROM workout_routines WHERE id = :id")
    fun observeRoutine(id: Int): Flow<WorkoutRoutine?>

    @Query("UPDATE workout_routines SET lastPerformedAt = :timestamp, timesPerformed = timesPerformed + 1 WHERE id = :routineId")
    suspend fun markPerformed(routineId: Int, timestamp: Long = System.currentTimeMillis())
}
