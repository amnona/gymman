package com.example.gymtracker.data

data class WorkoutLogEntity(
    val id: Int = 0,
    val sessionId: Long,
    val timestamp: Long,
    val exerciseName: String,
    val weight: Double,
    val repeats: Int,
    val sets: Int,
    val planName: String? = null
)
