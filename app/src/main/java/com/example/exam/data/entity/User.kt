package com.example.exam.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


@Parcelize
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val email: String,
    val password: String,
    val birthday: String,
    val dailyGoal: Double = 2.0, // Default 2 liters
    val language: String = "fr",
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable