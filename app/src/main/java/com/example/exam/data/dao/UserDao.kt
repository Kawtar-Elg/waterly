package com.example.exam.data.dao

import androidx.room.*
import com.example.exam.data.entity.User

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?
    
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): User?
    
    @Insert
    suspend fun insertUser(user: User): Long
    
    @Update
    suspend fun updateUser(user: User)
    
    @Query("UPDATE users SET dailyGoal = :goal WHERE id = :userId")
    suspend fun updateDailyGoal(userId: Long, goal: Double)
    
    @Query("UPDATE users SET language = :language WHERE id = :userId")
    suspend fun updateLanguage(userId: Long, language: String)
    
    @Delete
    suspend fun deleteUser(user: User)
}