package com.dearnovember.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dearnovember.app.DearNovemberApp
import com.dearnovember.app.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class TaskRow(val template: TaskTemplate, val done: Boolean, val recovery: Boolean)

data class HomeState(
    val date: LocalDate = Challenge.displayDate(LocalDate.now()),
    val day: Int = 1,
    val chapter: ChapterContent = Chapters.forDay(1),
    val score: Int = 0,
    val rows: List<TaskRow> = emptyList(),
    val categoryScores: Map<Category, Int> = emptyMap(),
    val streak: StreakResult = StreakResult(0, 0),
    val overallCompletion: Float = 0f,
    val beforeStart: Boolean = false,
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as DearNovemberApp).repository
    private val today = LocalDate.now()
    private val date = Challenge.displayDate(today)

    val state: StateFlow<HomeState> = combine(
        repo.observeCompletions(date), repo.observeAllProgress()
    ) { completions, allProgress ->
        val done = completions.filter { it.completed }.map { it.taskKey }.toSet()
        val recovery = Challenge.isRecoveryDay(date)
        val scores = allProgress.associate { LocalDate.parse(it.date) to it.score }
        HomeState(
            date = date,
            day = Challenge.dayNumber(date) ?: 1,
            chapter = Chapters.forDay(Challenge.dayNumber(date) ?: 1),
            score = Scoring.score(done),
            rows = DefaultTasks.all.map { TaskRow(it, it.key in done, recovery && it.recoveryName != null) },
            categoryScores = Category.entries.associateWith { Scoring.categoryScore(it, done) },
            streak = Streaks.compute(scores, today),
            overallCompletion = ((allProgress.sumOf { it.score }) / (Challenge.TOTAL_DAYS * 100f)).coerceIn(0f, 1f),
            beforeStart = today.isBefore(Challenge.START),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeState())

    fun toggle(key: String, done: Boolean) {
        viewModelScope.launch { repo.setTaskDone(date, key, done) }
    }
}
