package com.fittrack.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fittrack.data.entity.Exercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<Exercise>)

    @Insert
    suspend fun insert(exercise: Exercise): Long

    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExerciseById(id: Int): Exercise?

    @Query("SELECT * FROM exercises WHERE muscleGroup = :muscleGroup ORDER BY name ASC")
    fun getExercisesByMuscleGroup(muscleGroup: String): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchExercises(query: String): Flow<List<Exercise>>

    @Query("SELECT DISTINCT muscleGroup FROM exercises ORDER BY muscleGroup ASC")
    fun getAllMuscleGroups(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getExerciseCount(): Int
}
