package com.example.gymtracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gymtracker.data.ExerciseEntity
import com.example.gymtracker.data.PlanEntity
import com.example.gymtracker.data.WorkoutLogEntity
import com.example.gymtracker.data.WorkoutRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import android.os.SystemClock

class WorkoutViewModel(private val repository: WorkoutRepository) : ViewModel() {

    val exercises: StateFlow<List<ExerciseEntity>> = repository.exercises

    val workoutLogs: StateFlow<List<WorkoutLogEntity>> = repository.workoutLogs

    val plans: StateFlow<List<PlanEntity>> = repository.plans

    val activePlan: StateFlow<PlanEntity?> = repository.activePlan

    fun addExercise(name: String, picturePath: String? = null) {
        viewModelScope.launch {
            repository.addExercise(name, picturePath)
        }
    }

    fun deleteExercise(exercise: ExerciseEntity) {
        viewModelScope.launch {
            repository.deleteExercise(exercise)
        }
    }

    fun moveExercise(exercise: ExerciseEntity, moveUp: Boolean) {
        viewModelScope.launch {
            val currentList = exercises.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == exercise.id }
            if (index == -1) return@launch

            val targetIndex = if (moveUp) index - 1 else index + 1
            if (targetIndex in 0 until currentList.size) {
                val temp = currentList[index]
                currentList[index] = currentList[targetIndex]
                currentList[targetIndex] = temp
                repository.reorderExercises(currentList)
            }
        }
    }

    fun updateExerciseDetails(
        exercise: ExerciseEntity,
        weight: Double,
        repeats: Int,
        sets: Int,
        isDone: Boolean
    ) {
        viewModelScope.launch {
            repository.updateExerciseDetails(exercise, weight, repeats, sets, isDone)
        }
    }

    fun resetSession() {
        viewModelScope.launch {
            repository.resetAllDoneStatus()
        }
    }

    // Plans operations
    fun setActivePlan(plan: PlanEntity?) {
        repository.setActivePlan(plan)
    }

    fun addPlan(name: String) {
        viewModelScope.launch {
            repository.addPlan(name)
        }
    }

    fun deletePlan(plan: PlanEntity) {
        viewModelScope.launch {
            repository.deletePlan(plan)
        }
    }

    fun addExerciseToPlan(planId: Int, exerciseId: Int) {
        viewModelScope.launch {
            repository.addExerciseToPlan(planId, exerciseId)
        }
    }

    fun removeExerciseFromPlan(planId: Int, exerciseId: Int) {
        viewModelScope.launch {
            repository.removeExerciseFromPlan(planId, exerciseId)
        }
    }

    fun addNewExerciseToPlan(planId: Int, name: String, picturePath: String? = null) {
        viewModelScope.launch {
            repository.addNewExerciseToPlan(planId, name, picturePath)
        }
    }

    fun updateExercisePicture(exercise: ExerciseEntity, picturePath: String) {
        viewModelScope.launch {
            repository.updateExercisePicture(exercise, picturePath)
        }
    }

     fun removeExercisePicture(exercise: ExerciseEntity) {
         viewModelScope.launch {
             repository.removeExercisePicture(exercise)
         }
     }

     fun renameExercise(exercise: ExerciseEntity, newName: String) {
         viewModelScope.launch {
             repository.renameExercise(exercise, newName)
         }
     }

     fun getExercisesForPlan(planId: Int): List<ExerciseEntity> {
        return repository.getExercisesForPlan(planId)
    }

    fun getExercisesNotInPlan(planId: Int): List<ExerciseEntity> {
        return repository.getExercisesNotInPlan(planId)
    }

    // Log deletion operations
    fun deleteWorkoutSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteWorkoutSession(sessionId)
        }
    }

    fun deleteExerciseLog(logId: Int) {
        viewModelScope.launch {
            repository.deleteExerciseLog(logId)
        }
    }

    // Stopwatch/Timer Implementation (real-time using SystemClock.elapsedRealtime)
    private var timerJob: kotlinx.coroutines.Job? = null
    private var endTimeMillis: Long? = null

    private val _countdownTime = kotlinx.coroutines.flow.MutableStateFlow(60L)
    val countdownTime: StateFlow<Long> = _countdownTime

    private val _timerRunning = kotlinx.coroutines.flow.MutableStateFlow(false)
    val timerRunning: StateFlow<Boolean> = _timerRunning

     fun startCountdownTimer(context: android.content.Context) {
         if (_timerRunning.value) return
         _timerRunning.value = true

         try {
             // Establish an end timestamp based on the current countdown seconds
             val now = SystemClock.elapsedRealtime()
             val computedEndTime = now + (_countdownTime.value * 1000L)
             endTimeMillis = computedEndTime

             // Persist end time so service or restarts can pick it up
             try {
                 val prefs = context.getSharedPreferences("timer_prefs", android.content.Context.MODE_PRIVATE)
                 prefs.edit().putLong("end_time", computedEndTime).apply()
             } catch (e: Exception) {
                 e.printStackTrace()
             }

             // Start the foreground service to keep timer running reliably in background
             try {
                 val intent = android.content.Intent(context, com.example.gymtracker.ui.timer.TimerForegroundService::class.java)
                 intent.action = com.example.gymtracker.ui.timer.TimerForegroundService.ACTION_START_TIMER
                 intent.putExtra(com.example.gymtracker.ui.timer.TimerForegroundService.EXTRA_END_TIME, computedEndTime)
                 androidx.core.content.ContextCompat.startForegroundService(context, intent)
             } catch (e: Exception) {
                 e.printStackTrace()
                 _timerRunning.value = false
                 throw e
             }

             // Start lightweight coroutine to update UI remaining time (service handles beep)
             timerJob = viewModelScope.launch(Dispatchers.Default) {
                 try {
                     while (isActive && _timerRunning.value) {
                         val current = SystemClock.elapsedRealtime()
                         val remainingMs = (endTimeMillis ?: current) - current
                         val remainingSec = kotlin.math.floor(remainingMs / 1000.0).toLong()
                         _countdownTime.value = remainingSec

                         if (remainingSec < -60L) {
                             // Give up after a minute overtime and reset
                             _timerRunning.value = false
                             _countdownTime.value = 60L
                             endTimeMillis = null
                             break
                         }

                         kotlinx.coroutines.delay(500L)
                     }
                 } catch (_: Exception) {
                     _timerRunning.value = false
                     endTimeMillis = null
                 }
             }
         } catch (e: Exception) {
             e.printStackTrace()
             _timerRunning.value = false
         }
     }

    fun stopCountdownTimer(context: android.content.Context) {
        _timerRunning.value = false
        timerJob?.cancel()
        timerJob = null
        endTimeMillis = null

        // Clear persisted end time
        try {
            val prefs = context.getSharedPreferences("timer_prefs", android.content.Context.MODE_PRIVATE)
            prefs.edit().remove("end_time").apply()
        } catch (_: Exception) {}

        // Tell service to stop
        try {
            val intent = android.content.Intent(context, com.example.gymtracker.ui.timer.TimerForegroundService::class.java).apply {
                action = com.example.gymtracker.ui.timer.TimerForegroundService.ACTION_STOP_TIMER
            }
            context.startService(intent)
        } catch (_: Exception) {}

        _countdownTime.value = 60L
    }

    private fun playBeeps() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            repeat(3) {
                playBeepSound()
                kotlinx.coroutines.delay(200L)
            }
        }
    }

    private fun playBeepSound() {
        try {
            val toneGenerator = android.media.ToneGenerator(android.media.AudioManager.STREAM_ALARM, 100)
            toneGenerator.startTone(android.media.ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100)
            toneGenerator.release()
        } catch (_: Exception) {
            // Audio not available, silently continue
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

class WorkoutViewModelFactory(private val repository: WorkoutRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkoutViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
