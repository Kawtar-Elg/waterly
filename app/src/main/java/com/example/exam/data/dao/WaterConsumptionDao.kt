package com.waterly.data.dao

import androidx.room.*
import com.waterly.data.entity.WaterConsumption
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

@Dao
interface WaterConsumptionDao {
    @Query("SELECT * FROM water_consumption WHERE userId = :userId AND date = :date ORDER BY timestamp DESC")
    fun getConsumptionByDate(userId: Long, date: String): Flow<List<WaterConsumption>>
    
    @Query("SELECT SUM(amount) FROM water_consumption WHERE userId = :userId AND date = :date")
    suspend fun getTotalConsumptionByDate(userId: Long, date: String): Double
    
    @Query("""
        SELECT SUM(amount) as total, date 
        FROM water_consumption 
        WHERE userId = :userId 
        AND date BETWEEN :startDate AND :endDate 
        GROUP BY date 
        ORDER BY date DESC
    """)
    fun getWeeklyConsumption(userId: Long, startDate: String, endDate: String): Flow<List<DailyConsumption>>
    
    @Query("SELECT SUM(amount) FROM water_consumption WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    suspend fun getMonthlyTotal(userId: Long, startDate: String, endDate: String): Double
    
    @Query("""
        SELECT date, SUM(amount) as total 
        FROM water_consumption 
        WHERE userId = :userId 
        AND date >= :startDate 
        GROUP BY date 
        ORDER BY total DESC 
        LIMIT 1
    """)
    suspend fun getTopHydrationDay(userId: Long, startDate: String): Pair<String, Double>?
    
    @Insert
    suspend fun insertConsumption(consumption: WaterConsumption): Long
    
    @Delete
    suspend fun deleteConsumption(consumption: WaterConsumption)
    
    data class DailyConsumption(
        val total: Double,
        val date: String
    )
}