package com.fittrack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fittrack.data.dao.*
import com.fittrack.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        WorkoutRoutine::class,
        Exercise::class,
        RoutineExercise::class,
        ExerciseLog::class,
        PersonalRecord::class,
        BodyWeight::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FitTrackDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun workoutRoutineDao(): WorkoutRoutineDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun routineExerciseDao(): RoutineExerciseDao
    abstract fun exerciseLogDao(): ExerciseLogDao
    abstract fun personalRecordDao(): PersonalRecordDao
    abstract fun bodyWeightDao(): BodyWeightDao

    companion object {
        @Volatile
        private var INSTANCE: FitTrackDatabase? = null

        fun getDatabase(context: Context): FitTrackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitTrackDatabase::class.java,
                    "fittrack_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(SeedDatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SeedDatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    database.exerciseDao().insertAll(ExerciseSeedData.exercises)
                }
            }
        }
    }
}
