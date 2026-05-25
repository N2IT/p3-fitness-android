package com.fittrack.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.fittrack.data.entity.PersonalRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalRecordDao {
    @Insert
    suspend fun insert(record: PersonalRecord): Long

    @Query("SELECT * FROM personal_records WHERE exerciseId = :exerciseId AND userId = :userId ORDER BY achievedAt DESC")
    fun getRecordsForExercise(exerciseId: Int, userId: Int): Flow<List<PersonalRecord>>

    @Query("SELECT * FROM personal_records WHERE exerciseId = :exerciseId AND userId = :userId AND recordType = :type ORDER BY value DESC LIMIT 1")
    suspend fun getBestRecord(exerciseId: Int, userId: Int, type: String): PersonalRecord?

    @Query("SELECT * FROM personal_records WHERE userId = :userId ORDER BY achievedAt DESC")
    fun getAllRecords(userId: Int): Flow<List<PersonalRecord>>

    @Query("SELECT * FROM personal_records WHERE userId = :userId ORDER BY achievedAt DESC LIMIT :limit")
    suspend fun getRecentRecords(userId: Int, limit: Int = 20): List<PersonalRecord>

    @Query("SELECT * FROM personal_records WHERE workoutSessionId = :sessionId")
    suspend fun getRecordsForSession(sessionId: String): List<PersonalRecord>
}
