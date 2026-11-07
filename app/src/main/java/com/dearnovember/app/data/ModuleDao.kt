package com.dearnovember.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ModuleDao {
    // Academics
    @Query("SELECT COUNT(*) FROM subject") suspend fun subjectCount(): Int
    @Insert suspend fun insertSubject(s: Subject): Long
    @Query("SELECT * FROM subject ORDER BY id") fun subjects(): Flow<List<Subject>>
    @Query("SELECT * FROM subject ORDER BY id") suspend fun subjectsOnce(): List<Subject>
    @Query("SELECT * FROM subject WHERE id = :id") fun subject(id: Long): Flow<Subject?>
    @Insert suspend fun insertTopic(t: Topic): Long
    @Update suspend fun updateTopic(t: Topic)
    @Delete suspend fun deleteTopic(t: Topic)
    @Query("SELECT * FROM topic ORDER BY id") fun allTopics(): Flow<List<Topic>>
    @Query("SELECT * FROM topic WHERE subjectId = :id ORDER BY id") fun topics(id: Long): Flow<List<Topic>>
    @Query("SELECT COUNT(*) FROM assignment") suspend fun assignmentCount(): Int
    @Insert suspend fun insertAssignment(a: Assignment): Long
    @Update suspend fun updateAssignment(a: Assignment)
    @Query("SELECT * FROM assignment ORDER BY targetStart, id") fun assignments(): Flow<List<Assignment>>
    @Insert suspend fun insertStudy(s: StudySession): Long
    @Delete suspend fun deleteStudy(s: StudySession)
    @Query("SELECT * FROM study_session ORDER BY date DESC, id DESC") fun studySessions(): Flow<List<StudySession>>

    // Physical
    @Query("SELECT COUNT(*) FROM exercise") suspend fun exerciseCount(): Int
    @Insert suspend fun insertExercise(e: Exercise): Long
    @Update suspend fun updateExercise(e: Exercise)
    @Query("SELECT * FROM exercise ORDER BY id") fun exercises(): Flow<List<Exercise>>
    @Query("SELECT * FROM workout WHERE date = :d LIMIT 1") fun workout(d: String): Flow<Workout?>
    @Query("SELECT * FROM workout WHERE date = :d LIMIT 1") suspend fun workoutOnce(d: String): Workout?
    @Insert suspend fun insertWorkout(w: Workout): Long
    @Update suspend fun updateWorkout(w: Workout)
    @Query("SELECT * FROM workout ORDER BY date DESC") fun workouts(): Flow<List<Workout>>
    @Insert suspend fun insertSet(s: ExerciseSet): Long
    @Delete suspend fun deleteSet(s: ExerciseSet)
    @Query("SELECT * FROM exercise_set ORDER BY id") fun allSets(): Flow<List<ExerciseSet>>
    @Query("SELECT COUNT(*) FROM exercise_set WHERE workoutId = :w AND exerciseId = :e") suspend fun setCount(w: Long, e: Long): Int

    // Nutrition
    @Query("SELECT COUNT(*) FROM food") suspend fun foodCount(): Int
    @Insert suspend fun insertFood(f: Food): Long
    @Query("SELECT * FROM food ORDER BY isCustom, name") fun foods(): Flow<List<Food>>
    @Insert suspend fun insertMeal(m: Meal): Long
    @Delete suspend fun deleteMeal(m: Meal)
    @Query("SELECT * FROM meal WHERE date = :d ORDER BY id") fun meals(d: String): Flow<List<Meal>>
    @Upsert suspend fun upsertWater(w: WaterLog)
    @Query("SELECT * FROM water_log WHERE date = :d") fun water(d: String): Flow<WaterLog?>
    @Query("SELECT * FROM water_log WHERE date = :d") suspend fun waterOnce(d: String): WaterLog?

    // Mind
    @Insert suspend fun insertMood(m: MoodEntry): Long
    @Delete suspend fun deleteMood(m: MoodEntry)
    @Query("SELECT * FROM mood_entry ORDER BY date DESC, id DESC") fun moods(): Flow<List<MoodEntry>>
    @Insert suspend fun insertMeditation(m: MeditationSession): Long
    @Delete suspend fun deleteMeditation(m: MeditationSession)
    @Query("SELECT * FROM meditation_session ORDER BY date DESC, id DESC") fun meditations(): Flow<List<MeditationSession>>

    // Speaking
    @Upsert suspend fun upsertSpeaking(s: SpeakingSession): Long
    @Query("SELECT * FROM speaking_session WHERE date = :d LIMIT 1") fun speaking(d: String): Flow<SpeakingSession?>
    @Query("SELECT * FROM speaking_session WHERE date = :d LIMIT 1") suspend fun speakingOnce(d: String): SpeakingSession?
    @Query("SELECT * FROM speaking_session ORDER BY date DESC") fun speakingAll(): Flow<List<SpeakingSession>>
    @Insert suspend fun insertWord(w: VocabularyWord): Long
    @Delete suspend fun deleteWord(w: VocabularyWord)
    @Query("SELECT * FROM vocabulary_word ORDER BY id DESC") fun words(): Flow<List<VocabularyWord>>

    // Discipline
    @Upsert suspend fun upsertSleep(s: SleepLog)
    @Query("SELECT * FROM sleep_log WHERE date = :d") fun sleep(d: String): Flow<SleepLog?>
    @Query("SELECT * FROM sleep_log ORDER BY date DESC") fun sleepAll(): Flow<List<SleepLog>>
    @Insert suspend fun insertInsta(i: InstagramLog): Long
    @Delete suspend fun deleteInsta(i: InstagramLog)
    @Query("SELECT * FROM instagram_log WHERE date = :d ORDER BY id DESC") fun insta(d: String): Flow<List<InstagramLog>>
    @Insert suspend fun insertFocus(f: FocusSession): Long
    @Query("SELECT * FROM focus_session ORDER BY id DESC") fun focusAll(): Flow<List<FocusSession>>

    // Intellectual
    @Insert suspend fun insertChess(c: ChessSession): Long
    @Delete suspend fun deleteChess(c: ChessSession)
    @Query("SELECT * FROM chess_session ORDER BY date DESC, id DESC") fun chess(): Flow<List<ChessSession>>
    @Insert suspend fun insertSolve(c: CubeSolve): Long
    @Delete suspend fun deleteSolve(c: CubeSolve)
    @Query("SELECT * FROM cube_solve ORDER BY date DESC, id DESC") fun solves(): Flow<List<CubeSolve>>
    @Upsert suspend fun upsertStage(s: CubeStage)
    @Query("SELECT * FROM cube_stage ORDER BY stage") fun stages(): Flow<List<CubeStage>>
    @Query("SELECT COUNT(*) FROM cube_stage") suspend fun stageCount(): Int

    // Settings, reminders, photos, backups
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertSettingsIfMissing(s: AppSettings)
    @Upsert suspend fun upsertSettings(s: AppSettings)
    @Query("SELECT * FROM app_settings WHERE id = 1") fun settings(): Flow<AppSettings?>
    @Query("SELECT * FROM app_settings WHERE id = 1") suspend fun settingsOnce(): AppSettings?
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertReminders(r: List<Reminder>)
    @Upsert suspend fun upsertReminder(r: Reminder)
    @Query("SELECT * FROM reminder ORDER BY rowid") fun reminders(): Flow<List<Reminder>>
    @Query("SELECT * FROM reminder ORDER BY rowid") suspend fun remindersOnce(): List<Reminder>
    @Query("SELECT * FROM reminder WHERE `key` = :key") suspend fun reminder(key: String): Reminder?
    @Upsert suspend fun upsertPhoto(p: ProgressPhoto)
    @Query("SELECT * FROM progress_photo") fun photos(): Flow<List<ProgressPhoto>>
    @Insert suspend fun insertBackup(b: BackupHistory): Long
    @Query("SELECT * FROM backup_history ORDER BY createdAt DESC LIMIT 5") fun recentBackups(): Flow<List<BackupHistory>>

    // Reset (used only by the archive-and-restart flow)
    @Query("DELETE FROM task_completion") suspend fun clearCompletions()
    @Query("DELETE FROM daily_progress") suspend fun clearProgress()
    @Query("DELETE FROM exercise_set") suspend fun clearSets()
    @Query("DELETE FROM workout") suspend fun clearWorkouts()
    @Query("DELETE FROM meal") suspend fun clearMeals()
    @Query("DELETE FROM water_log") suspend fun clearWater()
    @Query("DELETE FROM mood_entry") suspend fun clearMoods()
    @Query("DELETE FROM meditation_session") suspend fun clearMeditations()
    @Query("DELETE FROM speaking_session") suspend fun clearSpeaking()
    @Query("DELETE FROM sleep_log") suspend fun clearSleep()
    @Query("DELETE FROM instagram_log") suspend fun clearInsta()
    @Query("DELETE FROM focus_session") suspend fun clearFocus()
    @Query("DELETE FROM chess_session") suspend fun clearChess()
    @Query("DELETE FROM cube_solve") suspend fun clearSolves()
    @Query("DELETE FROM cube_stage") suspend fun clearStages()
    @Query("DELETE FROM study_session") suspend fun clearStudy()
    @Query("UPDATE topic SET done = 0, revised = 0") suspend fun resetTopics()
    @Query("UPDATE assignment SET status = 'PENDING'") suspend fun resetAssignments()
    @Query("DELETE FROM progress_photo") suspend fun clearPhotos()
}
