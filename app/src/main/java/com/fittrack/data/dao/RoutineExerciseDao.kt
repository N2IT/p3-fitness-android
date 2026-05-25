package com.fittrack.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.fittrack.data.entity.RoutineExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineExerciseDao {
    @Insert
    suspend fun insert(routineExercise: RoutineExercise): Long

    @Insert
    suspend fun insertAll(routineExercises: List<RoutineExercise>)

    @Update
    suspend fun update(routineExercise: RoutineExercise)

    @Delete
    suspend fun delete(routineExercise: RoutineExercise)

    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId ORDER BY orderIndex ASC")
    fun getExercisesForRoutine(routineId: Int): Flow<List<RoutineExercise>>

    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId ORDER BY orderIndex ASC")
    suspend fun getExercisesForRoutineOnce(routineId: Int): List<RoutineExercise>

    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId")
    suspend fun deleteAllForRoutine(routineId: Int)

    @Query("SELECT MAX(orderIndex) FROM routine_exercises WHERE routineId = :routineId")
    suspend fun getMaxOrderIndex(routineId: Int): Int?
}
