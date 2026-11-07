package com.dearnovember.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertTasks(tasks: List<DailyTask>)
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertChapters(chapters: List<DailyChapter>)
    @Query("SELECT * FROM daily_task ORDER BY sortOrder") fun observeTasks(): Flow<List<DailyTask>>
    @Query("SELECT * FROM daily_task ORDER BY sortOrder") suspend fun tasks(): List<DailyTask>
    @Query("SELECT * FROM daily_chapter WHERE day = :day") fun observeChapter(day: Int): Flow<DailyChapter?>
    @Query("SELECT * FROM task_completion WHERE date = :date") fun observeCompletions(date: String): Flow<List<TaskCompletion>>
    @Query("SELECT * FROM task_completion WHERE date = :date AND taskKey = :key") suspend fun completion(date: String, key: String): TaskCompletion?
    @Query("SELECT taskKey FROM task_completion WHERE date = :date AND completed = 1") suspend fun doneKeys(date: String): List<String>
    @Upsert suspend fun upsert(c: TaskCompletion)
    @Query("SELECT * FROM task_completion WHERE completed = 1") fun observeAllDone(): Flow<List<TaskCompletion>>
}

@Dao
interface ProgressDao {
    @Upsert suspend fun upsert(p: DailyProgress)
    @Query("SELECT * FROM daily_progress ORDER BY date") fun observeAll(): Flow<List<DailyProgress>>
    @Query("SELECT * FROM daily_progress WHERE date = :date") fun observe(date: String): Flow<DailyProgress?>
}

@Dao
interface JournalDao {
    @Insert suspend fun insert(e: JournalEntry): Long
    @Update suspend fun update(e: JournalEntry)
    @Delete suspend fun delete(e: JournalEntry)
    @Query("SELECT * FROM journal_entry ORDER BY date DESC, createdAt DESC") fun observeEntries(): Flow<List<JournalEntry>>
    @Insert suspend fun insertLetter(l: Letter): Long
    @Update suspend fun updateLetter(l: Letter)
    @Delete suspend fun deleteLetter(l: Letter)
    @Query("SELECT * FROM letter ORDER BY date DESC, createdAt DESC") fun observeLetters(): Flow<List<Letter>>
}
