package com.dearnovember.app.domain

import java.time.LocalDate

enum class Category(val label: String, val maxPoints: Int) {
    ACADEMICS("Academics", 30),
    FITNESS("Fitness & nutrition", 20),
    DISCIPLINE("Discipline & sleep", 15),
    PERSONAL("Personal development", 15),
    MENTAL("Mental growth", 10),
    ROUTINE("Routine & reflection", 10),
}

data class TaskTemplate(
    val key: String,
    val name: String,
    val description: String,
    val category: Category,
    val points: Int,
    val recoveryName: String? = null,
    val recoveryDescription: String? = null,
)

object DefaultTasks {
    val all: List<TaskTemplate> = listOf(
        TaskTemplate("study_1", "Focused study block 1", "Phone away. Pick one subject and stay with it.", Category.ACADEMICS, 10),
        TaskTemplate("study_2", "Focused study block 2", "A second block, ideally a different subject.", Category.ACADEMICS, 10),
        TaskTemplate("assignment", "Assignment or practical work", "Move today's assignment target forward.", Category.ACADEMICS, 10),

        TaskTemplate("workout", "Workout", "Today's session from the weekly plan.", Category.FITNESS, 10,
            "Recovery and stretching", "Gentle stretching, easy walk. Rest is part of training."),
        TaskTemplate("protein", "Reach protein target", "Estimated, not exact. Milk, dal, soya, whey all count.", Category.FITNESS, 5),
        TaskTemplate("water", "Water goal", "Keep a bottle nearby and sip through the day.", Category.FITNESS, 5),

        TaskTemplate("wake", "Wake up on time", "Meet the target for today's step in the schedule.", Category.DISCIPLINE, 5),
        TaskTemplate("instagram", "Instagram within 30 minutes", "Log it honestly. No scrolling during study or workouts.", Category.DISCIPLINE, 5),
        TaskTemplate("bedtime", "Bedtime on track", "Wind down and aim for your current sleep target.", Category.DISCIPLINE, 5),

        TaskTemplate("speaking", "Speaking practice", "Warm-up, reading aloud, shadowing, one random topic.", Category.PERSONAL, 5),
        TaskTemplate("chess", "Chess, 20 minutes", "Puzzles or a game, then note one mistake.", Category.PERSONAL, 5),
        TaskTemplate("cube", "Cube practice, 20 minutes", "Work on today's stage of the beginner method.", Category.PERSONAL, 5),

        TaskTemplate("meditation", "Meditation, 5 to 10 minutes", "Sit, breathe, return when the mind wanders.", Category.MENTAL, 4),
        TaskTemplate("mood", "Mood check-in", "Name what you feel. Facts or assumptions?", Category.MENTAL, 3),
        TaskTemplate("mission", "Mental mission", "Do today's small mental mission.", Category.MENTAL, 3),

        TaskTemplate("plan", "Plan tomorrow", "Three priorities for tomorrow.", Category.ROUTINE, 5),
        TaskTemplate("reflect", "Evening reflection", "Answer today's reflection question in your journal.", Category.ROUTINE, 5),
    )

    private val byKey = all.associateBy { it.key }
    fun find(key: String) = byKey[key]
}

enum class ScoreBand(val label: String) {
    EXCELLENT("Excellent"), STRONG("Strong"), DECENT("Decent"), NEEDS_IMPROVEMENT("Needs improvement")
}

object Scoring {
    const val TOTAL = 100
    const val STREAK_THRESHOLD = 60

    /** Sum of points for completed, known, distinct tasks. Never negative, never above 100. */
    fun score(completedKeys: Collection<String>, tasks: List<TaskTemplate> = DefaultTasks.all): Int {
        val done = completedKeys.toSet()
        return tasks.filter { it.key in done }.sumOf { it.points }.coerceIn(0, TOTAL)
    }

    fun categoryScore(category: Category, completedKeys: Collection<String>, tasks: List<TaskTemplate> = DefaultTasks.all): Int {
        val done = completedKeys.toSet()
        return tasks.filter { it.category == category && it.key in done }.sumOf { it.points }
    }

    fun band(score: Int) = when {
        score >= 90 -> ScoreBand.EXCELLENT
        score >= 75 -> ScoreBand.STRONG
        score >= 60 -> ScoreBand.DECENT
        else -> ScoreBand.NEEDS_IMPROVEMENT
    }
}

data class StreakResult(val current: Int, val longest: Int)

/**
 * Streak rules (also shown to the user):
 *  - A day counts when its score is at least 60.
 *  - One missed day pauses the streak without resetting it ("never miss twice").
 *  - Two missed days in a row reset it to zero.
 *  - Today is only judged once it qualifies; an unfinished today never breaks a streak.
 *  - Only real challenge dates are evaluated.
 */
object Streaks {
    fun compute(scores: Map<LocalDate, Int>, today: LocalDate): StreakResult {
        val last = minOf(today, Challenge.END)
        if (last.isBefore(Challenge.START)) return StreakResult(0, 0)
        var current = 0
        var longest = 0
        var misses = 0
        var d = Challenge.START
        while (!d.isAfter(last)) {
            val qualifies = (scores[d] ?: 0) >= Scoring.STREAK_THRESHOLD
            val unfinishedToday = d == today && !qualifies
            if (qualifies) {
                current++; misses = 0
                if (current > longest) longest = current
            } else if (!unfinishedToday) {
                misses++
                if (misses >= 2) current = 0
            }
            d = d.plusDays(1)
        }
        return StreakResult(current, longest)
    }
}
