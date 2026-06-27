package com.example.gymtracker.data

data class ExerciseEntity(
    val id: Int = 0,
    val name: String,
    val displayOrder: Int = 0,
    val lastWeight: Double = 0.0,
    val lastRepeats: Int = 0,
    val lastSets: Int = 0,
    val isDone: Boolean = false,
    val picturePath: String? = null
)
