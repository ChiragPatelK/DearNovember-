package com.dearnovember.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

const val DB_VERSION = 1

@Database(
    version = DB_VERSION, exportSchema = true,
    entities = [DailyProgress::class, DailyTask::class, TaskCompletion::class, Habit::class, HabitCompletion::class,
        Subject::class, Topic::class, Assignment::class, StudySession::class, Workout::class, Exercise::class,
        ExerciseSet::class, Meal::class, Food::class, MoodEntry::class, MeditationSession::class, SpeakingSession::class,
        VocabularyWord::class, ChessSession::class, CubeSolve::class, JournalEntry::class, Letter::class,
        DailyChapter::class, AppSettings::class, BackupHistory::class, SleepLog::class, InstagramLog::class,
        FocusSession::class, Reminder::class, ProgressPhoto::class, WaterLog::class, CubeStage::class]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun progressDao(): ProgressDao
    abstract fun journalDao(): JournalDao
    abstract fun moduleDao(): ModuleDao

    companion object {
        const val VERSION = DB_VERSION
        const val NAME = "dear_november.db"
        @Volatile private var instance: AppDatabase? = null
        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, NAME)
                // Future versions must add explicit Migration objects here. No destructive fallback.
                .build().also { instance = it }
        }
        fun close() = synchronized(this) { instance?.close(); instance = null }
    }
}
