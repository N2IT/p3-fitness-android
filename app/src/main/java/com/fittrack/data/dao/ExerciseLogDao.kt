package com.fittrack.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.fittrack.data.entity.ExerciseLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseLogDao {
    @Insert
    suspend fun insert(log: ExerciseLog): Long

    @Insert
    suspend fun insertAll(logs: List<ExerciseLog>)

    @Query("SELECT * FROM exercise_logs WHERE userId = :userId ORDER BY completedAt DESC")
    fun getLogsForUser(userId: Int): Flow<List<ExerciseLog>>

    @Query("SELECT * FROM exercise_logs WHERE workoutSessionId = :sessionId ORDER BY exerciseId, setNumber")
    fun getLogsForSession(sessionId: String): Flow<List<ExerciseLog>>

    @Query("SELECT * FROM exercise_logs WHERE workoutSessionId = :sessionId ORDER BY exerciseId, setNumber")
    suspend fun getLogsForSessionOnce(sessionId: String): List<ExerciseLog>

    @Query("SELECT * FROM exercise_logs WHERE exerciseId = :exerciseId AND userId = :userId ORDER BY completedAt DESC")
    fun getLogsForExercise(exerciseId: Int, userId: Int): Flow<List<ExerciseLog>>

    @Query("""
        SELECT * FROM exercise_logs
        WHERE exerciseId = :exerciseId AND userId = :userId
        ORDER BY completedAt DESC
        LIMIT :limit
    """)
    suspend fun getRecentLogsForExercise(exerciseId: Int, userId: Int, limit: Int = 50): List<ExerciseLog>

    @Query("""
        SELECT el.* FROM exercise_logs el
        WHERE el.exerciseId = :exerciseId AND el.userId = :userId
        AND el.workoutSessionId = (
            SELECT workoutSessionId FROM exercise_logs
            WHERE exerciseId = :exerciseId AND userId = :userId
            ORDER BY completedAt DESC LIMIT 1
        )
        ORDER BY el.setNumber ASC
    """)
    suspend fun getLastSessionLogsForExercise(exerciseId: Int, userId: Int): List<ExerciseLog>

    @Query("SELECT DISTINCT workoutSessionId FROM exercise_logs WHERE userId = :userId ORDER BY completedAt DESC")
    fun getWorkoutSessions(userId: Int): Flow<List<String>>

    @Query("DELETE FROM exercise_logs WHERE workoutSessionId = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("SELECT MAX(weight) FROM exercise_logs WHERE exerciseId = :exerciseId AND userId = :userId")
    suspend fun getMaxWeight(exerciseId: Int, userId: Int): Double?

    @Query("SELECT COUNT(DISTINCT workoutSessionId) FROM exercise_logs WHERE userId = :userId")
    suspend fun getTotalWorkoutCount(userId: Int): Int
}
