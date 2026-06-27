package com.example.gymtracker.data

import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WorkoutRepository(private val dbHelper: WorkoutDatabaseHelper) {

    private val _exercises = MutableStateFlow<List<ExerciseEntity>>(emptyList())
    val exercises: StateFlow<List<ExerciseEntity>> = _exercises.asStateFlow()

    private val _workoutLogs = MutableStateFlow<List<WorkoutLogEntity>>(emptyList())
    val workoutLogs: StateFlow<List<WorkoutLogEntity>> = _workoutLogs.asStateFlow()

    private val _plans = MutableStateFlow<List<PlanEntity>>(emptyList())
    val plans: StateFlow<List<PlanEntity>> = _plans.asStateFlow()

    private val _activePlan = MutableStateFlow<PlanEntity?>(null)
    val activePlan: StateFlow<PlanEntity?> = _activePlan.asStateFlow()

    // Represents the active session ID.
    // "a new gym session is when i press reset or the app loads."
    private var currentSessionId: Long = System.currentTimeMillis()

    init {
        refreshPlans()
        refreshExercises()
        refreshLogs()
    }

    fun refreshPlans() {
        _plans.value = dbHelper.getAllPlans()
    }

    fun refreshExercises() {
        val active = _activePlan.value
        _exercises.value = if (active == null) {
            dbHelper.getAllExercises()
        } else {
            dbHelper.getExercisesForPlan(active.id)
        }
    }

    private fun refreshLogs() {
        _workoutLogs.value = dbHelper.getAllLogs()
    }

    fun setActivePlan(plan: PlanEntity?) {
        _activePlan.value = plan
        refreshExercises()
    }

    fun addPlan(name: String) {
        dbHelper.addPlan(name)
        refreshPlans()
    }

    fun deletePlan(plan: PlanEntity) {
        dbHelper.deletePlan(plan.id)
        if (_activePlan.value?.id == plan.id) {
            _activePlan.value = null
        }
        refreshPlans()
        refreshExercises()
    }

    fun addExerciseToPlan(planId: Int, exerciseId: Int) {
        dbHelper.addExerciseToPlan(planId, exerciseId)
        refreshExercises()
    }

    fun removeExerciseFromPlan(planId: Int, exerciseId: Int) {
        dbHelper.removeExerciseFromPlan(planId, exerciseId)
        refreshExercises()
    }

    fun getExercisesForPlan(planId: Int): List<ExerciseEntity> {
        return dbHelper.getExercisesForPlan(planId)
    }

    fun getExercisesNotInPlan(planId: Int): List<ExerciseEntity> {
        return dbHelper.getExercisesNotInPlan(planId)
    }

    fun addExercise(name: String, picturePath: String? = null) {
        val maxOrder = dbHelper.getMaxDisplayOrder()
        val newId = dbHelper.addExercise(name, maxOrder + 1, picturePath)
        val active = _activePlan.value
        if (active != null && newId > 0) {
            dbHelper.addExerciseToPlan(active.id, newId.toInt())
        }
        refreshExercises()
    }

    fun addNewExerciseToPlan(planId: Int, name: String, picturePath: String? = null) {
        val maxOrder = dbHelper.getMaxDisplayOrder()
        val newId = dbHelper.addExercise(name, maxOrder + 1, picturePath)
        if (newId > 0) {
            dbHelper.addExerciseToPlan(planId, newId.toInt())
        }
        refreshExercises()
    }

     fun deleteExercise(exercise: ExerciseEntity) {
         exercise.picturePath?.let { File(it).delete() }
         dbHelper.deleteExercise(exercise.id)
         refreshExercises()
     }

     fun renameExercise(exercise: ExerciseEntity, newName: String) {
         dbHelper.renameExercise(exercise.id, exercise.name, newName)
         refreshExercises()
         refreshLogs()
     }

    fun updateExercisePicture(exercise: ExerciseEntity, picturePath: String) {
        val previousPath = exercise.picturePath
        dbHelper.updateExercisePicture(exercise.id, picturePath)
        if (!previousPath.isNullOrBlank() && previousPath != picturePath) {
            File(previousPath).delete()
        }
        refreshExercises()
    }

    fun removeExercisePicture(exercise: ExerciseEntity) {
        exercise.picturePath?.let { File(it).delete() }
        dbHelper.updateExercisePicture(exercise.id, null)
        refreshExercises()
    }

    fun reorderExercises(orderedExercises: List<ExerciseEntity>) {
        val active = _activePlan.value
        if (active != null) {
            dbHelper.updatePlanExercisesOrder(active.id, orderedExercises)
        } else {
            dbHelper.updateExercisesOrder(orderedExercises)
        }
        refreshExercises()
    }

     fun updateExerciseDetails(
         exercise: ExerciseEntity,
         weight: Double,
         repeats: Int,
         sets: Int,
         isDone: Boolean
     ) {
         val updatedExercise = exercise.copy(
             lastWeight = weight,
             lastRepeats = repeats,
             lastSets = sets,
             isDone = isDone
         )
         dbHelper.updateExercise(updatedExercise)

         if (isDone) {
             val log = WorkoutLogEntity(
                 sessionId = currentSessionId,
                 timestamp = System.currentTimeMillis(),
                 exerciseName = exercise.name,
                 weight = weight,
                 repeats = repeats,
                 sets = sets,
                 planName = _activePlan.value?.name
             )
             dbHelper.insertWorkoutLog(log)
             refreshLogs()
         } else {
             // When exercise is unchecked, remove it from the current session's history
             dbHelper.deleteLogBySessionAndExercise(currentSessionId, exercise.name)
             refreshLogs()
         }
         refreshExercises()
     }

    fun deleteWorkoutSession(sessionId: Long) {
        dbHelper.deleteLogBySessionId(sessionId)
        refreshLogs()
    }

    fun deleteExerciseLog(logId: Int) {
        dbHelper.deleteLogById(logId)
        refreshLogs()
    }

    fun resetAllDoneStatus() {
        // Generating a new session ID when session resets
        currentSessionId = System.currentTimeMillis()
        dbHelper.resetAllDoneStatus()
        refreshExercises()
    }

    fun clearDatabase() {
        exercises.value.forEach { exercise ->
            exercise.picturePath?.let { File(it).delete() }
        }
        dbHelper.clearDatabase()
        _activePlan.value = null
        refreshPlans()
        refreshExercises()
        refreshLogs()
    }
}
