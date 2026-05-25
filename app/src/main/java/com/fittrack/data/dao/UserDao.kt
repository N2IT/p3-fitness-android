package com.fittrack.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: com.fittrack.data.entity.User): Long

    @Update
    suspend fun update(user: com.fittrack.data.entity.User)

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): com.fittrack.data.entity.User?

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): com.fittrack.data.entity.User?

    @Query("SELECT * FROM users WHERE id = :id")
    fun observeUser(id: Int): Flow<com.fittrack.data.entity.User?>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}
