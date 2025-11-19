package com.example.exam.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_consumption")
data class WaterConsumption(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val amount: Double, // in liters
    val timestamp: Long = System.currentTimeMillis(),
    val date: String // format: YYYY-MM-DD
)