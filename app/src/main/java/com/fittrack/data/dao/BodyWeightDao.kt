package com.fittrack.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.fittrack.data.entity.BodyWeight
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyWeightDao {
    @Insert
    suspend fun insert(bodyWeight: BodyWeight): Long

    @Query("SELECT * FROM body_weights WHERE userId = :userId ORDER BY loggedAt DESC")
    fun getWeightsForUser(userId: Int): Flow<List<BodyWeight>>

    @Query("SELECT * FROM body_weights WHERE userId = :userId ORDER BY loggedAt DESC LIMIT 1")
    suspend fun getLatestWeight(userId: Int): BodyWeight?

    @Query("SELECT * FROM body_weights WHERE userId = :userId ORDER BY loggedAt ASC")
    fun getWeightsChronological(userId: Int): Flow<List<BodyWeight>>
}
