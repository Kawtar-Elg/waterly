package com.example.exam.data.dao

import androidx.room.*
import com.example.exam.data.entity.WaterConsumption
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterConsumptionDao {

    // Get all consumptions for a specific date
    @Query("SELECT * FROM water_consumption WHERE userId = :userId AND date = :date ORDER BY timestamp DESC")
    fun getConsumptionByDate(userId: Long, date: String): Flow<List<WaterConsumption>>

    // Get total consumption for a specific date
    @Query("SELECT COALESCE(SUM(amount), 0) FROM water_consumption WHERE userId = :userId AND date = :date")
    suspend fun getTotalConsumptionByDate(userId: Long, date: String): Double

    // Get daily total consumption for a week
    @Query("""
        SELECT SUM(amount) as total, date 
        FROM water_consumption 
        WHERE userId = :userId 
        AND date BETWEEN :startDate AND :endDate 
        GROUP BY date 
        ORDER BY date DESC
    """)
    fun getWeeklyConsumption(userId: Long, startDate: String, endDate: String): Flow<List<DailyConsumption>>

    // Get monthly total consumption
    @Query("SELECT SUM(amount) FROM water_consumption WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    suspend fun getMonthlyTotal(userId: Long, startDate: String, endDate: String): Double

    // Get total consumption between dates
    @Query("SELECT COALESCE(SUM(amount), 0) FROM water_consumption WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalConsumptionBetweenDates(userId: Long, startDate: String, endDate: String): Double

    // Get the day with the highest hydration (fixed: use data class instead of Pair)
    @Query("""
        SELECT date, SUM(amount) as total 
        FROM water_consumption 
        WHERE userId = :userId 
        AND date >= :startDate 
        GROUP BY date 
        ORDER BY total DESC 
        LIMIT 1
    """)
    suspend fun getTopHydrationDay(userId: Long, startDate: String): TopHydrationDay?

    // Insert a new water consumption record
    @Insert
    suspend fun insertConsumption(consumption: WaterConsumption): Long

    // Delete a water consumption record
    @Delete
    suspend fun deleteConsumption(consumption: WaterConsumption)

    // --- Helper data classes for queries ---
    data class DailyConsumption(
        val total: Double,
        val date: String
    )

    data class TopHydrationDay(
        val date: String,
        val total: Double
    )
}
