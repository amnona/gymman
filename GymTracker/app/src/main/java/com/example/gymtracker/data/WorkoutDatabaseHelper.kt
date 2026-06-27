package com.example.gymtracker.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class WorkoutDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "workout_companion.db"
        private const val DATABASE_VERSION = 4

        // Table Exercises
        private const val TABLE_EXERCISES = "exercises"
        private const val COLUMN_EX_ID = "id"
        private const val COLUMN_EX_NAME = "name"
        private const val COLUMN_EX_ORDER = "display_order"
        private const val COLUMN_EX_LAST_WEIGHT = "last_weight"
        private const val COLUMN_EX_LAST_REPEATS = "last_repeats"
        private const val COLUMN_EX_LAST_SETS = "last_sets"
        private const val COLUMN_EX_IS_DONE = "is_done"
        private const val COLUMN_EX_PICTURE_PATH = "picture_path"

        // Table Workout Logs
        private const val TABLE_LOGS = "workout_logs"
        private const val COLUMN_LOG_ID = "id"
        private const val COLUMN_LOG_SESSION_ID = "session_id"
        private const val COLUMN_LOG_TIMESTAMP = "timestamp"
        private const val COLUMN_LOG_EX_NAME = "exercise_name"
        private const val COLUMN_LOG_WEIGHT = "weight"
        private const val COLUMN_LOG_REPEATS = "repeats"
        private const val COLUMN_LOG_SETS = "sets"
        private const val COLUMN_LOG_PLAN_NAME = "plan_name"

        // Table Plans
        private const val TABLE_PLANS = "plans"
        private const val COLUMN_PLAN_ID = "id"
        private const val COLUMN_PLAN_NAME = "name"

        // Table Plan Exercises
        private const val TABLE_PLAN_EXERCISES = "plan_exercises"
        private const val COLUMN_PE_PLAN_ID = "plan_id"
        private const val COLUMN_PE_EXERCISE_ID = "exercise_id"
        private const val COLUMN_PE_ORDER = "display_order"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createExercisesTable = """
            CREATE TABLE $TABLE_EXERCISES (
                $COLUMN_EX_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_EX_NAME TEXT,
                $COLUMN_EX_ORDER INTEGER,
                $COLUMN_EX_LAST_WEIGHT REAL,
                $COLUMN_EX_LAST_REPEATS INTEGER,
                $COLUMN_EX_LAST_SETS INTEGER,
                $COLUMN_EX_IS_DONE INTEGER DEFAULT 0,
                $COLUMN_EX_PICTURE_PATH TEXT
            )
        """.trimIndent()

        val createLogsTable = """
            CREATE TABLE $TABLE_LOGS (
                $COLUMN_LOG_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_LOG_SESSION_ID INTEGER,
                $COLUMN_LOG_TIMESTAMP INTEGER,
                $COLUMN_LOG_EX_NAME TEXT,
                $COLUMN_LOG_WEIGHT REAL,
                $COLUMN_LOG_REPEATS INTEGER,
                $COLUMN_LOG_SETS INTEGER,
                $COLUMN_LOG_PLAN_NAME TEXT
            )
        """.trimIndent()

        val createPlansTable = """
            CREATE TABLE $TABLE_PLANS (
                $COLUMN_PLAN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_PLAN_NAME TEXT
            )
        """.trimIndent()

        val createPlanExercisesTable = """
            CREATE TABLE $TABLE_PLAN_EXERCISES (
                $COLUMN_PE_PLAN_ID INTEGER,
                $COLUMN_PE_EXERCISE_ID INTEGER,
                $COLUMN_PE_ORDER INTEGER,
                PRIMARY KEY ($COLUMN_PE_PLAN_ID, $COLUMN_PE_EXERCISE_ID)
            )
        """.trimIndent()

        db.execSQL(createExercisesTable)
        db.execSQL(createLogsTable)
        db.execSQL(createPlansTable)
        db.execSQL(createPlanExercisesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE $TABLE_EXERCISES ADD COLUMN $COLUMN_EX_PICTURE_PATH TEXT")
        }
    }

    private fun cursorToExercise(cursor: Cursor, orderColumn: String): ExerciseEntity {
        val picturePathIndex = cursor.getColumnIndex(COLUMN_EX_PICTURE_PATH)
        return ExerciseEntity(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EX_ID)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EX_NAME)),
            displayOrder = cursor.getInt(cursor.getColumnIndexOrThrow(orderColumn)),
            lastWeight = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_EX_LAST_WEIGHT)),
            lastRepeats = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EX_LAST_REPEATS)),
            lastSets = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EX_LAST_SETS)),
            isDone = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EX_IS_DONE)) == 1,
            picturePath = if (picturePathIndex >= 0 && !cursor.isNull(picturePathIndex)) {
                cursor.getString(picturePathIndex)
            } else {
                null
            }
        )
    }

    fun getAllExercises(): List<ExerciseEntity> {
        val list = mutableListOf<ExerciseEntity>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_EXERCISES ORDER BY $COLUMN_EX_IS_DONE ASC, $COLUMN_EX_ORDER ASC",
            null
        )
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToExercise(cursor, COLUMN_EX_ORDER))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getMaxDisplayOrder(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT MAX($COLUMN_EX_ORDER) FROM $TABLE_EXERCISES", null)
        var max = 0
        if (cursor.moveToFirst()) {
            max = cursor.getInt(0)
        }
        cursor.close()
        return max
    }

    fun addExercise(name: String, order: Int, picturePath: String? = null): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EX_NAME, name)
            put(COLUMN_EX_ORDER, order)
            put(COLUMN_EX_LAST_WEIGHT, 0.0)
            put(COLUMN_EX_LAST_REPEATS, 0)
            put(COLUMN_EX_LAST_SETS, 0)
            put(COLUMN_EX_IS_DONE, 0)
            put(COLUMN_EX_PICTURE_PATH, picturePath)
        }
        return db.insert(TABLE_EXERCISES, null, values)
    }

    fun updateExercise(exercise: ExerciseEntity) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EX_NAME, exercise.name)
            put(COLUMN_EX_ORDER, exercise.displayOrder)
            put(COLUMN_EX_LAST_WEIGHT, exercise.lastWeight)
            put(COLUMN_EX_LAST_REPEATS, exercise.lastRepeats)
            put(COLUMN_EX_LAST_SETS, exercise.lastSets)
            put(COLUMN_EX_IS_DONE, if (exercise.isDone) 1 else 0)
            put(COLUMN_EX_PICTURE_PATH, exercise.picturePath)
        }
        db.update(TABLE_EXERCISES, values, "$COLUMN_EX_ID = ?", arrayOf(exercise.id.toString()))
    }

     fun updateExercisePicture(exerciseId: Int, picturePath: String?) {
         val db = writableDatabase
         val values = ContentValues().apply {
             put(COLUMN_EX_PICTURE_PATH, picturePath)
         }
         db.update(TABLE_EXERCISES, values, "$COLUMN_EX_ID = ?", arrayOf(exerciseId.toString()))
     }

     fun renameExercise(exerciseId: Int, oldName: String, newName: String) {
         val db = writableDatabase
         db.beginTransaction()
         try {
             // Update the exercise name
             val exerciseValues = ContentValues().apply {
                 put(COLUMN_EX_NAME, newName)
             }
             db.update(TABLE_EXERCISES, exerciseValues, "$COLUMN_EX_ID = ?", arrayOf(exerciseId.toString()))

             // Update all workout logs with the old name to use the new name
             val logValues = ContentValues().apply {
                 put(COLUMN_LOG_EX_NAME, newName)
             }
             db.update(TABLE_LOGS, logValues, "$COLUMN_LOG_EX_NAME = ?", arrayOf(oldName))

             db.setTransactionSuccessful()
         } finally {
             db.endTransaction()
         }
     }

    fun updateExercisesOrder(exercises: List<ExerciseEntity>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            exercises.forEachIndexed { index, exercise ->
                val values = ContentValues().apply {
                    put(COLUMN_EX_ORDER, index)
                }
                db.update(TABLE_EXERCISES, values, "$COLUMN_EX_ID = ?", arrayOf(exercise.id.toString()))
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun deleteExercise(id: Int) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_EXERCISES, "$COLUMN_EX_ID = ?", arrayOf(id.toString()))
            db.delete(TABLE_PLAN_EXERCISES, "$COLUMN_PE_EXERCISE_ID = ?", arrayOf(id.toString()))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun insertWorkoutLog(log: WorkoutLogEntity): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_LOG_SESSION_ID, log.sessionId)
            put(COLUMN_LOG_TIMESTAMP, log.timestamp)
            put(COLUMN_LOG_EX_NAME, log.exerciseName)
            put(COLUMN_LOG_WEIGHT, log.weight)
            put(COLUMN_LOG_REPEATS, log.repeats)
            put(COLUMN_LOG_SETS, log.sets)
            put(COLUMN_LOG_PLAN_NAME, log.planName)
        }
        return db.insert(TABLE_LOGS, null, values)
    }

    fun getAllLogs(): List<WorkoutLogEntity> {
        val list = mutableListOf<WorkoutLogEntity>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_LOGS ORDER BY $COLUMN_LOG_TIMESTAMP DESC", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LOG_ID))
                val sessionId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LOG_SESSION_ID))
                val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LOG_TIMESTAMP))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOG_EX_NAME))
                val weight = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LOG_WEIGHT))
                val repeats = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LOG_REPEATS))
                val sets = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LOG_SETS))
                val planName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOG_PLAN_NAME))

                list.add(WorkoutLogEntity(id, sessionId, timestamp, name, weight, repeats, sets, planName))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun resetAllDoneStatus() {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EX_IS_DONE, 0)
        }
        db.update(TABLE_EXERCISES, values, null, null)
    }

    // Plans operations
    fun getAllPlans(): List<PlanEntity> {
        val list = mutableListOf<PlanEntity>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_PLANS ORDER BY $COLUMN_PLAN_NAME ASC", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PLAN_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLAN_NAME))
                list.add(PlanEntity(id, name))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun addPlan(name: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PLAN_NAME, name)
        }
        return db.insert(TABLE_PLANS, null, values)
    }

    fun deletePlan(id: Int) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_PLANS, "$COLUMN_PLAN_ID = ?", arrayOf(id.toString()))
            db.delete(TABLE_PLAN_EXERCISES, "$COLUMN_PE_PLAN_ID = ?", arrayOf(id.toString()))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getExercisesForPlan(planId: Int): List<ExerciseEntity> {
        val list = mutableListOf<ExerciseEntity>()
        val db = readableDatabase
        val query = """
            SELECT e.*, pe.$COLUMN_PE_ORDER AS plan_order 
            FROM $TABLE_EXERCISES e
            INNER JOIN $TABLE_PLAN_EXERCISES pe ON e.$COLUMN_EX_ID = pe.$COLUMN_PE_EXERCISE_ID
            WHERE pe.$COLUMN_PE_PLAN_ID = ?
            ORDER BY e.$COLUMN_EX_IS_DONE ASC, pe.$COLUMN_PE_ORDER ASC
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(planId.toString()))
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToExercise(cursor, "plan_order"))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getExercisesNotInPlan(planId: Int): List<ExerciseEntity> {
        val list = mutableListOf<ExerciseEntity>()
        val db = readableDatabase
        val query = """
            SELECT * FROM $TABLE_EXERCISES 
            WHERE $COLUMN_EX_ID NOT IN (
                SELECT $COLUMN_PE_EXERCISE_ID FROM $TABLE_PLAN_EXERCISES WHERE $COLUMN_PE_PLAN_ID = ?
            )
            ORDER BY $COLUMN_EX_ORDER ASC
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(planId.toString()))
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToExercise(cursor, COLUMN_EX_ORDER))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun addExerciseToPlan(planId: Int, exerciseId: Int) {
        val db = writableDatabase
        val cursor = db.rawQuery(
            "SELECT MAX($COLUMN_PE_ORDER) FROM $TABLE_PLAN_EXERCISES WHERE $COLUMN_PE_PLAN_ID = ?",
            arrayOf(planId.toString())
        )
        var maxOrder = 0
        if (cursor.moveToFirst()) {
            maxOrder = cursor.getInt(0)
        }
        cursor.close()

        val values = ContentValues().apply {
            put(COLUMN_PE_PLAN_ID, planId)
            put(COLUMN_PE_EXERCISE_ID, exerciseId)
            put(COLUMN_PE_ORDER, maxOrder + 1)
        }
        db.insert(TABLE_PLAN_EXERCISES, null, values)
    }

    fun removeExerciseFromPlan(planId: Int, exerciseId: Int) {
        val db = writableDatabase
        db.delete(
            TABLE_PLAN_EXERCISES,
            "$COLUMN_PE_PLAN_ID = ? AND $COLUMN_PE_EXERCISE_ID = ?",
            arrayOf(planId.toString(), exerciseId.toString())
        )
    }

    fun updatePlanExercisesOrder(planId: Int, exercises: List<ExerciseEntity>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            exercises.forEachIndexed { index, exercise ->
                val values = ContentValues().apply {
                    put(COLUMN_PE_ORDER, index)
                }
                db.update(
                    TABLE_PLAN_EXERCISES,
                    values,
                    "$COLUMN_PE_PLAN_ID = ? AND $COLUMN_PE_EXERCISE_ID = ?",
                    arrayOf(planId.toString(), exercise.id.toString())
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

     // Log deletion operations
     fun deleteLogBySessionId(sessionId: Long) {
         val db = writableDatabase
         db.delete(TABLE_LOGS, "$COLUMN_LOG_SESSION_ID = ?", arrayOf(sessionId.toString()))
     }

     fun deleteLogById(logId: Int) {
         val db = writableDatabase
         db.delete(TABLE_LOGS, "$COLUMN_LOG_ID = ?", arrayOf(logId.toString()))
     }

     fun deleteLogBySessionAndExercise(sessionId: Long, exerciseName: String) {
         val db = writableDatabase
         db.delete(
             TABLE_LOGS,
             "$COLUMN_LOG_SESSION_ID = ? AND $COLUMN_LOG_EX_NAME = ?",
             arrayOf(sessionId.toString(), exerciseName)
         )
     }

    fun clearDatabase() {
        val db = writableDatabase
        db.delete(TABLE_EXERCISES, null, null)
        db.delete(TABLE_LOGS, null, null)
        db.delete(TABLE_PLANS, null, null)
        db.delete(TABLE_PLAN_EXERCISES, null, null)
    }
}
