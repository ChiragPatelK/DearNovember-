package com.dearnovember.app.data

import androidx.room.*

// Dates are stored as ISO strings (yyyy-MM-dd); timestamps as epoch millis.

@Entity(tableName = "daily_progress")
data class DailyProgress(@PrimaryKey val date: String, val score: Int, val completedTasks: Int, val totalTasks: Int, val updatedAt: Long)

@Entity(tableName = "daily_task")
data class DailyTask(@PrimaryKey val key: String, val name: String, val description: String, val category: String,
                     val points: Int, val recoveryName: String? = null, val recoveryDescription: String? = null, val sortOrder: Int = 0)

/** One row per (date, task): the unique index makes duplicate scoring impossible. */
@Entity(tableName = "task_completion",
    foreignKeys = [ForeignKey(DailyTask::class, ["key"], ["taskKey"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["date", "taskKey"], unique = true), Index("taskKey")])
data class TaskCompletion(@PrimaryKey(autoGenerate = true) val id: Long = 0, val date: String, val taskKey: String,
                          val completed: Boolean, val completedAt: Long? = null, val notes: String? = null)

@Entity(tableName = "habit") data class Habit(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String, val category: String)
@Entity(tableName = "habit_completion",
    foreignKeys = [ForeignKey(Habit::class, ["id"], ["habitId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["habitId", "date"], unique = true)])
data class HabitCompletion(@PrimaryKey(autoGenerate = true) val id: Long = 0, val habitId: Long, val date: String)

@Entity(tableName = "subject") data class Subject(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String, val kind: String)
@Entity(tableName = "topic",
    foreignKeys = [ForeignKey(Subject::class, ["id"], ["subjectId"], onDelete = ForeignKey.CASCADE)], indices = [Index("subjectId")])
data class Topic(@PrimaryKey(autoGenerate = true) val id: Long = 0, val subjectId: Long, val title: String, val done: Boolean = false, val revised: Boolean = false)
@Entity(tableName = "assignment",
    foreignKeys = [ForeignKey(Subject::class, ["id"], ["subjectId"], onDelete = ForeignKey.CASCADE)], indices = [Index("subjectId")])
data class Assignment(@PrimaryKey(autoGenerate = true) val id: Long = 0, val subjectId: Long, val title: String, val targetStart: String, val targetEnd: String, val deadline: String, val status: String = "PENDING")
@Entity(tableName = "study_session",
    foreignKeys = [ForeignKey(Subject::class, ["id"], ["subjectId"], onDelete = ForeignKey.CASCADE)], indices = [Index("subjectId"), Index("date")])
data class StudySession(@PrimaryKey(autoGenerate = true) val id: Long = 0, val subjectId: Long, val date: String, val minutes: Int, val isPractical: Boolean = false, val notes: String? = null)

@Entity(tableName = "exercise") data class Exercise(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String, val instructions: String, val defaultSets: Int, val defaultReps: Int, val restSeconds: Int, val easierVariation: String? = null)
@Entity(tableName = "workout", indices = [Index("date")]) data class Workout(@PrimaryKey(autoGenerate = true) val id: Long = 0, val date: String, val type: String, val completed: Boolean = false, val notes: String? = null)
@Entity(tableName = "exercise_set",
    foreignKeys = [ForeignKey(Workout::class, ["id"], ["workoutId"], onDelete = ForeignKey.CASCADE), ForeignKey(Exercise::class, ["id"], ["exerciseId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("workoutId"), Index("exerciseId")])
data class ExerciseSet(@PrimaryKey(autoGenerate = true) val id: Long = 0, val workoutId: Long, val exerciseId: Long, val setNumber: Int, val reps: Int, val weightKg: Double? = null)

@Entity(tableName = "food") data class Food(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String, val servingLabel: String, val proteinGramsEstimate: Double? = null, val isCustom: Boolean = false)
@Entity(tableName = "meal", indices = [Index("date")],
    foreignKeys = [ForeignKey(Food::class, ["id"], ["foodId"], onDelete = ForeignKey.SET_NULL)])
data class Meal(@PrimaryKey(autoGenerate = true) val id: Long = 0, val date: String, val slot: String, val foodId: Long? = null, val label: String, val servings: Double = 1.0, val proteinGramsEstimate: Double? = null)

@Entity(tableName = "mood_entry", indices = [Index("date")]) data class MoodEntry(@PrimaryKey(autoGenerate = true) val id: Long = 0, val date: String, val mood: Int, val note: String? = null, val trigger: String? = null, val factsOrAssumptions: String? = null, val withinControl: String? = null, val doDifferently: String? = null)
@Entity(tableName = "meditation_session", indices = [Index("date")]) data class MeditationSession(@PrimaryKey(autoGenerate = true) val id: Long = 0, val date: String, val minutes: Int)
@Entity(tableName = "speaking_session", indices = [Index(value = ["date"], unique = true)]) data class SpeakingSession(@PrimaryKey(autoGenerate = true) val id: Long = 0, val date: String, val warmup: Boolean, val reading: Boolean, val shadowing: Boolean, val topic: Boolean, val selfNote: String? = null, val audioPath: String? = null)
@Entity(tableName = "vocabulary_word") data class VocabularyWord(@PrimaryKey(autoGenerate = true) val id: Long = 0, val word: String, val meaning: String, val example: String? = null, val dateAdded: String)
@Entity(tableName = "chess_session", indices = [Index("date")]) data class ChessSession(@PrimaryKey(autoGenerate = true) val id: Long = 0, val date: String, val minutes: Int, val puzzles: Int = 0, val wins: Int = 0, val losses: Int = 0, val draws: Int = 0, val rating: Int? = null, val mistakes: String? = null)
@Entity(tableName = "cube_solve", indices = [Index("date")]) data class CubeSolve(@PrimaryKey(autoGenerate = true) val id: Long = 0, val date: String, val stage: Int, val solved: Boolean, val seconds: Int? = null, val notes: String? = null)

@Entity(tableName = "journal_entry", indices = [Index("date")]) data class JournalEntry(@PrimaryKey(autoGenerate = true) val id: Long = 0, val date: String, val title: String, val body: String, val createdAt: Long, val updatedAt: Long)
@Entity(tableName = "letter", indices = [Index("date")]) data class Letter(@PrimaryKey(autoGenerate = true) val id: Long = 0, val date: String, val title: String, val body: String, val mood: Int? = null, val createdAt: Long, val updatedAt: Long)

@Entity(tableName = "daily_chapter") data class DailyChapter(@PrimaryKey val day: Int, val title: String, val quote: String, val mission: String, val question: String)
@Entity(tableName = "app_settings") data class AppSettings(@PrimaryKey val id: Int = 1, val style: String = "ROMANTIC", val mode: String = "SYSTEM",
    val proteinTargetGrams: Int = 80, val studyTargetMinutes: Int = 210, val appLock: Boolean = false, val backupKeep: Int = 7,
    val backupEnabled: Boolean = true, val pushupBaseline: Int? = null, val pushupFinal: Int? = null)
@Entity(tableName = "backup_history") data class BackupHistory(@PrimaryKey(autoGenerate = true) val id: Long = 0, val createdAt: Long, val path: String, val sizeBytes: Long, val success: Boolean, val message: String? = null)

@Entity(tableName = "sleep_log") data class SleepLog(@PrimaryKey val date: String, val bedtime: String?, val wakeTime: String?)
@Entity(tableName = "instagram_log", indices = [Index("date")]) data class InstagramLog(@PrimaryKey(autoGenerate = true) val id: Long = 0, val date: String, val minutes: Int)
@Entity(tableName = "focus_session", indices = [Index("date")]) data class FocusSession(@PrimaryKey(autoGenerate = true) val id: Long = 0, val date: String, val minutes: Int)
@Entity(tableName = "reminder") data class Reminder(@PrimaryKey val key: String, val label: String, val enabled: Boolean, val hour: Int, val minute: Int)
@Entity(tableName = "progress_photo") data class ProgressPhoto(@PrimaryKey val slot: String, val path: String, val takenAt: Long)
@Entity(tableName = "water_log") data class WaterLog(@PrimaryKey val date: String, val glasses: Int)
@Entity(tableName = "cube_stage") data class CubeStage(@PrimaryKey val stage: Int, val done: Boolean, val doneDate: String?)
