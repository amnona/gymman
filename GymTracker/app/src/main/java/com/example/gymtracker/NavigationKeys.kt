package com.example.gymtracker

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Main : NavKey
@Serializable data class Details(val exerciseId: Int) : NavKey
@Serializable data object Stats : NavKey
@Serializable data class History(val exerciseName: String) : NavKey
@Serializable data object PlansList : NavKey
@Serializable data class EditPlan(val planId: Int) : NavKey


