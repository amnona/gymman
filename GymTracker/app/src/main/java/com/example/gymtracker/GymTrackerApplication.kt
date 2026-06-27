package com.example.gymtracker

import android.app.Application
import com.example.gymtracker.data.WorkoutDatabaseHelper
import com.example.gymtracker.data.WorkoutRepository

class GymTrackerApplication : Application() {
    val dbHelper by lazy { WorkoutDatabaseHelper(this) }
    val repository by lazy { WorkoutRepository(dbHelper) }
}
