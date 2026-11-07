package com.dearnovember.app.data

import android.content.Context
import androidx.room.withTransaction
import com.dearnovember.app.domain.*
import java.io.File
import java.time.LocalDate

class Repository(private val db: AppDatabase) {
    private val tasks = db.taskDao()
    private val progress = db.progressDao()
    val m: ModuleDao = db.moduleDao()
    val journal get() = db.journalDao()
    val tasksDao get() = tasks

    fun observeTasks() = tasks.observeTasks()
    fun observeCompletions(date: LocalDate) = tasks.observeCompletions(date.toString())
    fun observeAllDone() = tasks.observeAllDone()
    fun observeChapter(day: Int) = tasks.observeChapter(day)
    fun observeAllProgress() = progress.observeAll()

    /** Idempotent: safe to run at every launch. */
    suspend fun seedIfNeeded() = db.withTransaction {
        tasks.insertTasks(DefaultTasks.all.mapIndexed { i, t ->
            DailyTask(t.key, t.name, t.description, t.category.name, t.points, t.recoveryName, t.recoveryDescription, i)
        })
        tasks.insertChapters(Chapters.all.map { DailyChapter(it.day, it.title, it.quote, it.mission, it.question) })
        m.insertSettingsIfMissing(AppSettings())
        m.insertReminders(Seeds.reminders)
        if (m.subjectCount() == 0) Seeds.subjects.forEach { m.insertSubject(Subject(name = it.first, kind = it.second)) }
        if (m.assignmentCount() == 0) {
            val byName = m.subjectsOnce().associateBy { it.name }
            Seeds.assignments.forEach { a ->
                byName[a.subject]?.let { s -> m.insertAssignment(Assignment(subjectId = s.id, title = a.title, targetStart = a.start, targetEnd = a.end, deadline = Seeds.ASSIGNMENT_DEADLINE)) }
            }
        }
        if (m.exerciseCount() == 0) Seeds.exercises.forEach { m.insertExercise(it) }
        if (m.foodCount() == 0) Seeds.foods.forEach { m.insertFood(it) }
        if (m.stageCount() == 0) Prompts.cubeStages.indices.forEach { m.upsertStage(CubeStage(it + 1, false, null)) }
    }

    /** Saves the completion and recomputes the day's score in one transaction. */
    suspend fun setTaskDone(date: LocalDate, key: String, done: Boolean) = db.withTransaction {
        val d = date.toString()
        val existing = tasks.completion(d, key)
        val stamp = if (done) (existing?.takeIf { it.completed }?.completedAt ?: System.currentTimeMillis()) else null
        tasks.upsert(TaskCompletion(existing?.id ?: 0, d, key, done, stamp, existing?.notes))
        recompute(date)
    }

    suspend fun setTaskNote(date: LocalDate, key: String, note: String) = db.withTransaction {
        val d = date.toString()
        val e = tasks.completion(d, key)
        tasks.upsert(TaskCompletion(e?.id ?: 0, d, key, e?.completed ?: false, e?.completedAt, note.ifBlank { null }))
    }

    private suspend fun recompute(date: LocalDate) {
        val done = tasks.doneKeys(date.toString())
        val templates = tasks.tasks().mapNotNull { DefaultTasks.find(it.key) }
        progress.upsert(DailyProgress(date.toString(), Scoring.score(done, templates),
            done.toSet().count { k -> templates.any { it.key == k } }, templates.size, System.currentTimeMillis()))
    }

    /** Marks a task done when a module log reaches its target. Never un-marks. */
    suspend fun autoComplete(date: LocalDate, key: String) {
        if (tasks.completion(date.toString(), key)?.completed != true) setTaskDone(date, key, true)
    }

    suspend fun ensureWorkout(date: LocalDate, type: String): Workout {
        val d = date.toString()
        m.workoutOnce(d)?.let { return it }
        val id = m.insertWorkout(Workout(date = d, type = type))
        return Workout(id, d, type)
    }

    /** Clears tracking data for a fresh run. Journal, letters, subjects, topics and settings are kept. */
    suspend fun clearChallengeData(context: Context) = db.withTransaction {
        m.clearCompletions(); m.clearProgress(); m.clearSets(); m.clearWorkouts(); m.clearMeals(); m.clearWater()
        m.clearMoods(); m.clearMeditations(); m.clearSpeaking(); m.clearSleep(); m.clearInsta(); m.clearFocus()
        m.clearChess(); m.clearSolves(); m.clearStages(); m.clearStudy(); m.resetTopics(); m.resetAssignments(); m.clearPhotos()
        Prompts.cubeStages.indices.forEach { m.upsertStage(CubeStage(it + 1, false, null)) }
        val s = m.settingsOnce(); if (s != null) m.upsertSettings(s.copy(pushupBaseline = null, pushupFinal = null))
        File(context.filesDir, "photos").deleteRecursively()
    }
}
